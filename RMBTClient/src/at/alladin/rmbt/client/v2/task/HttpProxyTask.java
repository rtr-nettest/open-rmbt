/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.alladin.rmbt.client.v2.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import at.alladin.rmbt.client.QualityOfServiceTest;
import at.alladin.rmbt.client.RMBTClient;
import at.alladin.rmbt.client.v2.task.result.QoSTestResult;
import at.alladin.rmbt.client.v2.task.result.QoSTestResultEnum;

/**
 * 
 * @author lb
 *
 */
public class HttpProxyTask extends AbstractQoSTask {

	private final String target;
	
	private final String range;
	
	private final long connectionTimeout;
	
	private final long downloadTimeout;
	
	public final static long DEFAULT_CONNECTION_TIMEOUT = 5000000000L;
	
	public final static long DEFAULT_DOWNLOAD_TIMEOUT = 10000000000L;
	
	public final static String PARAM_TARGET = "url";
	
	public final static String PARAM_RANGE = "range";
	
	public final static String PARAM_CONNECTION_TIMEOUT = "conn_timeout";
	
	public final static String PARAM_DOWNLOAD_TIMEOUT = "download_timeout";
	
	public final static String RESULT_STATUS = "http_result_status";
	
	public final static String RESULT_DURATION = "http_result_duration";
	
	public final static String RESULT_LENGTH = "http_result_length";
	
	public final static String RESULT_HEADER = "http_result_header";
	
	public final static String RESULT_RANGE = "http_objective_range";
	
	public final static String RESULT_TARGET = "http_objective_url";
	
	public final static String RESULT_HASH = "http_result_hash";
	
	public final AtomicBoolean downloadCompleted = new AtomicBoolean(false);
	
	public final AtomicBoolean timeOutReached = new AtomicBoolean(false);
	
	/**
	 * 
	 * @param taskDesc
	 */
	public HttpProxyTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId) {
		super(nnTest, taskDesc, threadId);
		this.target = (String)taskDesc.getParams().get(PARAM_TARGET);
		this.range = (String)taskDesc.getParams().get(PARAM_RANGE);
		
		String value = (String) taskDesc.getParams().get(PARAM_CONNECTION_TIMEOUT);
		this.connectionTimeout = value != null ? Long.valueOf(value) : DEFAULT_CONNECTION_TIMEOUT;

		value = (String) taskDesc.getParams().get(PARAM_DOWNLOAD_TIMEOUT);
		this.downloadTimeout = value != null ? Long.valueOf(value) : DEFAULT_DOWNLOAD_TIMEOUT;
	}
	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	public QoSTestResult call() throws Exception {
		final QoSTestResult result = initQoSTestResult(QoSTestResultEnum.HTTP_PROXY);
		try {
			onStart(result);
			
			Future<QoSTestResult> httpTimeoutTask = RMBTClient.getCommonThreadPool().submit(new Callable<QoSTestResult>() {

				public QoSTestResult call() throws Exception {
					httpGet(result);
					return result;
				}
				
			});
			
			final QoSTestResult testResult = httpTimeoutTask.get((int)(downloadTimeout/1000000), TimeUnit.SECONDS);
			return testResult;	
		}
		catch (TimeoutException e) {
			e.printStackTrace();
			result.getResultMap().put(RESULT_HASH, "TIMEOUT");
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			onEnd(result);
		}
		
		return result;
	}
	
	/**
	 * 
	 * @return
	 */
	private QoSTestResult httpGet(final QoSTestResult result) throws Exception {
		final HttpGet httpGet = new HttpGet(new URI(this.target));
		
		if (range != null && range.startsWith("bytes")) {
			httpGet.addHeader("Range", range);	
		}

		HttpParams httpParameters = new BasicHttpParams();

		// Set the timeout
		HttpConnectionParams.setConnectionTimeout(httpParameters, (int)(connectionTimeout / 1000000));
		// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
		HttpConnectionParams.setSoTimeout(httpParameters, (int)(downloadTimeout / 1000000));

		System.out.println("Downloading: " + target);
		
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
		
		//prevent redirects:
		httpClient.setRedirectHandler(new RedirectHandler() {
			
			public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
				return false;
			}
			
			public URI getLocationURI(HttpResponse response, HttpContext context)
					throws ProtocolException {
				return null;
			}
		});
		
		
		Thread timeoutThread = new Thread(new Runnable() {
			
			public void run() {
				try {
					System.out.println("HTTP PROXY TIMEOUT THREAD: " + downloadTimeout + " ms");
					Thread.sleep((int)(downloadTimeout / 1000000));
					
					if (!downloadCompleted.get()) {
						if (httpGet != null && !httpGet.isAborted()) {
							httpGet.abort();
						}
						timeOutReached.set(true);
						System.out.println("HTTP PROXY TIMEOUT REACHED");
					}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	                
			}
	     });
		
		timeoutThread.start();

		String hash = null;
		long duration = 0;
		
		try {
			final long start = System.nanoTime();
			HttpResponse response = httpClient.execute(httpGet);
			
			//get the content:
			long contentLength = -1;
			if (getQoSTest().getTestSettings().getCacheFolder() != null) {
				File cacheFile = new File(getQoSTest().getTestSettings().getCacheFolder(), "proxy" + threadId);
				contentLength = writeFileFromInputStream(response.getEntity().getContent(), cacheFile);
				duration = System.nanoTime() - start;
				hash = generateChecksum(cacheFile);
				cacheFile.delete();
			}
			else {
				//get Content:
				String content = getStringFromInputStream(response.getEntity().getContent());
				duration = System.nanoTime() - start;
				//calculate md5 hash:
				hash = generateChecksum(content.getBytes("UTF-8"));
			}
			
			//result.getResultMap().put(RESULT_DURATION, (duration / 1000000));
			result.getResultMap().put(RESULT_STATUS, response.getStatusLine().getStatusCode());
			result.getResultMap().put(RESULT_LENGTH, contentLength);
			
			StringBuilder header = new StringBuilder();
			
			for (Header h : response.getAllHeaders()) {
				header.append(h.getName());
				header.append(": ");
				header.append(h.getValue());
				header.append("\n");
			}
			
			result.getResultMap().put(RESULT_HEADER, header.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
			result.getResultMap().put(RESULT_STATUS, "");
			result.getResultMap().put(RESULT_LENGTH, 0);
			result.getResultMap().put(RESULT_HEADER, "");
		}
		finally {
			if (timeOutReached.get()) {
				result.getResultMap().put(RESULT_HASH, "TIMEOUT");
			}
			else if (hash != null) {
				result.getResultMap().put(RESULT_HASH, hash);	
			}
			else {
				result.getResultMap().put(RESULT_HASH, "ERROR");
			}
		}

		result.getResultMap().put(RESULT_RANGE, range);
		result.getResultMap().put(RESULT_TARGET, target);	
		
		return result;
	}

	/**
	 * 
	 * @param is
	 * @return
	 */
	public static String getStringFromInputStream(InputStream is) {
		 
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
 
		String line;
		try {
 
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
 
		return sb.toString();
	}
	
	public long writeFileFromInputStream(InputStream is, File outputFile) throws FileNotFoundException, IOException {
		return copyInputStreamToOutputStream(is, new FileOutputStream(outputFile));
	}
	
	public long copyInputStreamToOutputStream(InputStream input, OutputStream output) throws IOException 
	{
		byte[] buffer = new byte[4096];
		long count = 0L;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			if (timeOutReached.get()) {
				break;
			}
			output.write(buffer, 0, n);
			count += n;
		}
		downloadCompleted.set(true);
		output.close();
		return count;
	}
	
	/**
	 * 
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String generateChecksum(byte[] input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");	
		byte[] hash = md.digest(input);

		return generateChecksumFromDigest(hash);
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException 
	 */
	public static String generateChecksum(File file) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		DigestInputStream dis = new DigestInputStream(new FileInputStream(file), md);
		int ch;
		while ((ch = dis.read()) != -1) { 
			//empty block
		}
		dis.close();
		return generateChecksumFromDigest(md.digest());
	}

	/**
	 * 
	 * @param digest
	 * @return
	 */
	public static String generateChecksumFromDigest(byte[] digest) {
		StringBuilder hexString = new StringBuilder();
		
        for (int i = 0; i < digest.length; i++) {
            if ((0xff & digest[i]) < 0x10) {
                hexString.append("0"
                        + Integer.toHexString((0xFF & digest[i])));
            } else {
                hexString.append(Integer.toHexString(0xFF & digest[i]));
            }
        }
		return hexString.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.AbstractRmbtTask#initTask()
	 */
	@Override
	public void initTask() {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#getTestType()
	 */
	public QoSTestResultEnum getTestType() {
		return QoSTestResultEnum.HTTP_PROXY;
	}
}
