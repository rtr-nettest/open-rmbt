/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

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
	
	public static class Md5Result {
		String md5;
		long contentLength = 0;
		long generatingTimeNs = 0;
	}
	
	/**
	 * 
	 * @param taskDesc
	 */
	public HttpProxyTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId) {
		super(nnTest, taskDesc, threadId, threadId);
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
			result.getResultMap().put(RESULT_RANGE, range);
			result.getResultMap().put(RESULT_TARGET, target);	

			onStart(result);
			
			Future<QoSTestResult> httpTimeoutTask = RMBTClient.getCommonThreadPool().submit(new Callable<QoSTestResult>() {

				public QoSTestResult call() throws Exception {
					httpGet(result);
					return result;
				}
				
			});
			
			final QoSTestResult testResult = httpTimeoutTask.get(downloadTimeout, TimeUnit.NANOSECONDS);
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
	
	private QoSTestResult httpGet(final QoSTestResult result) throws Exception {
		final URL url = new URL(this.target);
		final HttpURLConnection httpGet;
		String hash = null;
		
		try {
			Thread timeoutThread = new Thread(new Runnable() {
				
				public void run() {
					try {
						System.out.println("HTTP PROXY TIMEOUT THREAD: " + downloadTimeout + " ms");
						Thread.sleep((int)(downloadTimeout / 1000000));
						
						if (!downloadCompleted.get()) {
							timeOutReached.set(true);
							System.out.println("HTTP PROXY TIMEOUT REACHED");
						}
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		                
				}
		     });
			
			timeoutThread.start();
		
			final long start = System.nanoTime();
			httpGet = (HttpURLConnection) url.openConnection();
			if (range != null && range.startsWith("bytes")) {
				httpGet.addRequestProperty("Range", range);	
			}
			
			httpGet.setConnectTimeout((int) TimeUnit.MILLISECONDS.convert(connectionTimeout, TimeUnit.NANOSECONDS));
			httpGet.setReadTimeout((int) TimeUnit.MILLISECONDS.convert(downloadTimeout, TimeUnit.NANOSECONDS));
			httpGet.setInstanceFollowRedirects(false);		

			Md5Result md5 = generateChecksum(httpGet.getInputStream());
			downloadCompleted.set(true);
			hash = md5.md5;
			
			final long duration = System.nanoTime() - start;
			result.getResultMap().put(RESULT_DURATION, duration - md5.generatingTimeNs);
			result.getResultMap().put(RESULT_STATUS, httpGet.getResponseCode());
			result.getResultMap().put(RESULT_LENGTH, md5.contentLength);
			
			final String headers;
			if (httpGet.getHeaderFields() != null) {
				final StringBuilder sb = new StringBuilder();
				final Iterator<Entry<String, List<String>>> headerIterator = httpGet.getHeaderFields().entrySet().iterator();
				while (headerIterator.hasNext()) {
					final Entry<String, List<String>> e = headerIterator.next();
					if (e.getKey() != null && !e.getKey().equals("null")) {
						sb.append(e.getKey());
						sb.append(": ");
						final List<String> values = e.getValue();
						for (int i = 0; i < values.size(); i++) {
							sb.append(values.get(i));
							if (i+1 < values.size()) {
								sb.append(",");
							}
						}
						sb.append("\n");
					}
				}				
				headers = sb.toString();
			}
			else {
				headers = null;
			}
			
			result.getResultMap().put(RESULT_HEADER, headers);
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
	public static Md5Result generateChecksum(File file) throws NoSuchAlgorithmException, IOException {
        return generateChecksum(new FileInputStream(file)); 
	}
	
	/**
	 * 
	 * @param inputStream
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static Md5Result generateChecksum(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
		Md5Result md5 = new Md5Result();
		MessageDigest md = MessageDigest.getInstance("MD5");
		DigestInputStream dis = new DigestInputStream(inputStream, md);
		
		byte[] dataBytes = new byte[4096];
       
        int nread = 0; 
        while ((nread = dis.read(dataBytes)) != -1) {
        	md5.contentLength += nread;
        };
        
        dis.close();
        
        long startNs = System.nanoTime();
        md5.md5 = generateChecksumFromDigest(md.digest());
        md5.generatingTimeNs = System.nanoTime() - startNs;
        
        return md5;
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

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#needsQoSControlConnection()
	 */
	public boolean needsQoSControlConnection() {
		return false;
	}
}
