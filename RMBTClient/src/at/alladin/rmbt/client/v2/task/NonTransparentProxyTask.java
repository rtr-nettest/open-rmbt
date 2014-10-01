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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Locale;

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
	
	private final String request;
	
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
		super(nnTest, taskDesc, threadId);
		
		String requestString = (String)taskDesc.getParams().get(PARAM_PROXY_REQUEST);;
		
		if (!requestString.endsWith("\n")) {
			requestString += "\n";
		}

		this.request = requestString;
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
			String response = "";
			
			Socket initSocket = null;
			try {
				try {
					initSocket = connect(result, InetAddress.getByName(getTestServerAddr()), getTestServerPort(), 
		    			QOS_SERVER_PROTOCOL_VERSION, "ACCEPT", getQoSTest().getTestSettings().isUseSsl(), CONTROL_CONNECTION_TIMEOUT);
				}
				catch (IOException e) {
                    result.setFatalError(true);
                    throw e;
                }

		    	initSocket.setSoTimeout((int)(timeout/1000000));
		    	
				sendMessage("NTPTEST " + port + "\n");
				response = reader.readLine();
				
				//wait for ok -> server has opened requested socket
				if (response.startsWith("OK")) {
					//reset response string:
					response = "";
					//open test socket
					InetSocketAddress socketAddr = new InetSocketAddress(InetAddress.getByName(getTestServerAddr()), port);
					Socket testSocket = new Socket();
					testSocket.connect(socketAddr, (int)(timeout/1000000));
					testSocket.setSoTimeout((int)(timeout/1000000));
					
					//send request to echo service				
					sendMessage(testSocket, request);
					
					//read response from echo service
					response = readLine(testSocket);
					System.out.println("NON_TRANSPARENT_PROXY response: " + response);
					if (response != null) {		
						response = String.format(Locale.US, "%s", response);
					}
					else {
						throw new IOException();
					}

					//close connection:
					sendMessage("QUIT\n");
				}
				
				result.getResultMap().put(RESULT_STATUS, "OK");
			}
			catch (SocketTimeoutException e) {
				e.printStackTrace();
				result.getResultMap().put(RESULT_STATUS, "TIMEOUT");
			}
			catch (Exception e) {
				e.printStackTrace();
				result.getResultMap().put(RESULT_STATUS, "ERROR");
			}
			
			result.getResultMap().put(RESULT_REQUEST, (request != null ? request.trim() : ""));
			result.getResultMap().put(RESULT_RESPONSE, (response != null ? response.trim() : ""));
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

}
