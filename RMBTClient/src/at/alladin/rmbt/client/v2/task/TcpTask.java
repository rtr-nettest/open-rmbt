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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import at.alladin.rmbt.client.QualityOfServiceTest;
import at.alladin.rmbt.client.v2.task.result.QoSTestResult;
import at.alladin.rmbt.client.v2.task.result.QoSTestResultEnum;


public class TcpTask extends AbstractQoSTask {
	
	private final Integer testPortOut;
	
	private final Integer testPortIn;
	
	private final long timeout;
	
	private final static long DEFAULT_TIMEOUT = 3000000000L;
	
	public final static String PARAM_PORT_IN = "in_port";
	
	public final static String PARAM_PORT_OUT = "out_port";
	
	public final static String PARAM_TIMEOUT = "timeout";
	
	public final static String RESULT_PORT_IN = "tcp_objective_in_port";
	
	public final static String RESULT_PORT_OUT = "tcp_objective_out_port";
	
	public final static String RESULT_TIMEOUT = "tcp_objective_timeout";
	
	public final static String RESULT_IN = "tcp_result_in";
	
	public final static String RESULT_OUT = "tcp_result_out";
	
	public final static String RESULT_RESPONSE_OUT = "tcp_result_out_response";
	
	public final static String RESULT_RESPONSE_IN = "tcp_result_in_response";
	
	/**
	 * 
	 * @param taskDesc
	 */
	public TcpTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId) {
		super(nnTest, taskDesc, threadId, threadId);

		String value = (String) taskDesc.getParams().get(PARAM_PORT_IN);
		this.testPortIn = value != null ? Integer.valueOf(value) : null;	

		value = (String) taskDesc.getParams().get(PARAM_PORT_OUT);
		this.testPortOut = value != null ? Integer.valueOf(value) : null;	
		
		value = (String) taskDesc.getParams().get(PARAM_TIMEOUT);
		this.timeout = value != null ? Long.valueOf(value) : DEFAULT_TIMEOUT;
	}

	/**
	 * 
	 */
	public QoSTestResult call() throws Exception {
		final QoSTestResult result = initQoSTestResult(QoSTestResultEnum.TCP);
		try {
			onStart(result);
			
			if (this.testPortIn != null) {
				result.getResultMap().put(RESULT_IN, "FAILED");			
			}
			if (this.testPortOut != null) {
				result.getResultMap().put(RESULT_OUT, "FAILED");			
			}
			
			Socket socketIn = null;
			
			try {
				System.out.println("TCPTASK: " + getTestServerAddr() + ":" + getTestServerPort());
		    	
		    	String response = null;
		    	
		    	if (this.testPortOut != null) {
		    		//needed for timeout:
		    		final CountDownLatch latch = new CountDownLatch(1);
		    		
		    		//response handler
		    		final ControlConnectionResponseCallback callback = new ControlConnectionResponseCallback() {
						
						public void onResponse(final String response, final String request) {
				    		if (response != null && response.startsWith("OK")) {
				    			System.out.println("got response: " + response);
				    			Socket socketOut = null;
				    			
				    			try {
					    			socketOut = getSocket(getTestServerAddr(), testPortOut, false, (int)(timeout/1000000));
					    			socketOut.setSoTimeout((int)(timeout/1000000));
					    			sendMessage(socketOut, "PING\n");
					    			final String testResponse = readLine(socketOut);
								
					    			System.out.println("TCP OUT TEST response: " + testResponse);
								
					    			result.getResultMap().put(RESULT_RESPONSE_OUT, testResponse);
					    			socketOut.close();
					    			result.getResultMap().put(RESULT_OUT, "OK");
				    			}
				    			catch (SocketTimeoutException e) {
				    				result.getResultMap().put(RESULT_OUT, "TIMEOUT");
				    			}
				    			catch (Exception e) {
				    				result.getResultMap().put(RESULT_OUT, "ERROR");
				    			}
				    			finally {
					    			latch.countDown();
				    				if (socketOut != null && !socketOut.isClosed()) {
				    					try {
											socketOut.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
				    				}
				    			}
				    		}
						}
					};	    				
		    		
					sendCommand("TCPTEST OUT " + testPortOut, callback);
					if(!latch.await(timeout, TimeUnit.NANOSECONDS)) {
						result.getResultMap().put(RESULT_OUT, "TIMEOUT");
					}
		    	}
				
		    	if (this.testPortIn != null) {
		    		ServerSocket serverSocket = null;
		    		try {
						serverSocket = new ServerSocket(testPortIn);
						sendCommand("TCPTEST IN " + testPortIn, null);

						serverSocket.setSoTimeout((int)(timeout/1000000));
						socketIn = serverSocket.accept();
						socketIn.setSoTimeout((int)(timeout/1000000));
						response = readLine(socketIn);
						System.out.println("TCP IN TEST response: " + response);						
						result.getResultMap().put(RESULT_RESPONSE_IN, response);
						socketIn.close();
						result.getResultMap().put(RESULT_IN, "OK");	    				    			
		    		}
		    		finally {
		    			if (serverSocket != null) {
		    				serverSocket.close();
		    			}
		    		}
		    	}
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			if (this.testPortIn != null) {
				result.getResultMap().put(RESULT_PORT_IN, testPortIn);			
			}

			if (this.testPortOut != null) {
				result.getResultMap().put(RESULT_PORT_OUT, testPortOut);	
			}
			
			result.getResultMap().put(RESULT_TIMEOUT, timeout);
			
			return result;			
		}
		catch (Exception e)  {
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
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#getTestType()
	 */
	public QoSTestResultEnum getTestType() {
		return QoSTestResultEnum.TCP;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#needsQoSControlConnection()
	 */
	public boolean needsQoSControlConnection() {
		return true;
	}
}
