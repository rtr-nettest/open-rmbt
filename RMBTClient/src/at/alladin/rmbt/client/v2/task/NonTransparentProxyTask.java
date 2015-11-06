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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import at.alladin.rmbt.client.QualityOfServiceTest;
import at.alladin.rmbt.client.v2.task.result.QoSTestResult;
import at.alladin.rmbt.client.v2.task.result.QoSTestResultEnum;

/**
 * 
 * @author lb
 *
 */
public class NonTransparentProxyTask extends AbstractQoSTask {

	public final static long DEFAULT_TIMEOUT = 5000000000L;
	
	private final String testRequest;
	
	private final int port;
	
	private final long timeout;
	
	public final static String PARAM_PROXY_REQUEST = "request";
	
	public final static String PARAM_PROXY_PORT = "port";
	
	public final static String PARAM_PROXY_TIMEOUT = "timeout";

	public final static String RESULT_RESPONSE = "nontransproxy_result_response";
	
	public final static String RESULT_REQUEST = "nontransproxy_objective_request";
	
	public final static String RESULT_PORT = "nontransproxy_objective_port";
	
	public final static String RESULT_TIMEOUT = "nontransproxy_objective_timeout";
	
	public final static String RESULT_STATUS = "nontransproxy_result";
	
	
	/**
	 * 
	 * @param taskDesc
	 */
	public NonTransparentProxyTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId) {
		super(nnTest, taskDesc, threadId, threadId);
		
		String requestString = (String)taskDesc.getParams().get(PARAM_PROXY_REQUEST);;
		
		if (!requestString.endsWith("\n")) {
			requestString += "\n";
		}

		this.testRequest = requestString;
		this.port = Integer.valueOf((String)taskDesc.getParams().get(PARAM_PROXY_PORT));
		
		String value = (String) taskDesc.getParams().get(PARAM_PROXY_TIMEOUT);
		this.timeout = value != null ? Long.valueOf(value) : DEFAULT_TIMEOUT;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	public QoSTestResult call() throws Exception {
		final QoSTestResult result = initQoSTestResult(QoSTestResultEnum.NON_TRANSPARENT_PROXY);
		try {
			onStart(result);
			result.getResultMap().put(RESULT_PORT, port);

			final CountDownLatch latch = new CountDownLatch(1);
			
			final ControlConnectionResponseCallback callback = new ControlConnectionResponseCallback() {
				
				public void onResponse(final String controlResponse, final String controlRequest) {
					try {
						//wait for ok -> server has opened requested socket
						if (controlResponse.startsWith("OK")) {
							//reset response string:
							//open test socket
							InetSocketAddress socketAddr = new InetSocketAddress(InetAddress.getByName(getTestServerAddr()), port);
							Socket testSocket = new Socket();
							testSocket.connect(socketAddr, (int)(timeout/1000000));
							testSocket.setSoTimeout((int)(timeout/1000000));
							
							//send request to echo service				
							sendMessage(testSocket, testRequest);
							
							//read response from echo service
							String testResponse = readLine(testSocket);
							System.out.println("NON_TRANSPARENT_PROXY response: " + testResponse);
							if (testResponse != null) {		
								testResponse = String.format(Locale.US, "%s", testResponse);
								result.getResultMap().put(RESULT_RESPONSE, (testResponse != null ? testResponse.trim() : ""));
							}
							else {
								throw new IOException();
							}
							
							result.getResultMap().put(RESULT_STATUS, "OK");
						}
						else {
							result.getResultMap().put(RESULT_STATUS, "ERROR");
						}
					}
					catch (SocketTimeoutException e) {
						e.printStackTrace();
						result.getResultMap().put(RESULT_STATUS, "TIMEOUT");
					}
					catch (Exception e) {
						e.printStackTrace();
						result.getResultMap().put(RESULT_STATUS, "ERROR");
					}
					finally {
						latch.countDown();
					}
				}
			};
			
			sendCommand("NTPTEST " + port, callback);
			if (!latch.await(timeout, TimeUnit.NANOSECONDS)) {
				result.getResultMap().put(RESULT_STATUS, "TIMEOUT");
			}
			
			if (!result.getResultMap().containsKey(RESULT_RESPONSE)) {
				result.getResultMap().put(RESULT_RESPONSE, "");
			}
			result.getResultMap().put(RESULT_REQUEST, (testRequest != null ? testRequest.trim() : ""));
			result.getResultMap().put(RESULT_TIMEOUT, timeout);
			
			return result;			
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			onEnd(result);
		}

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
		return QoSTestResultEnum.NON_TRANSPARENT_PROXY;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#needsQoSControlConnection()
	 */
	public boolean needsQoSControlConnection() {
		return true;
	}

}
