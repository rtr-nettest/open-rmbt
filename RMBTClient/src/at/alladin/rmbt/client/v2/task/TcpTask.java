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
import java.net.ServerSocket;
import java.net.Socket;

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
		super(nnTest, taskDesc, threadId);

		String value = (String) taskDesc.getParams().get(PARAM_PORT_IN);
		this.testPortIn = value != null ? new Integer(value) : null;	

		value = (String) taskDesc.getParams().get(PARAM_PORT_OUT);
		this.testPortOut = value != null ? new Integer(value) : null;	
		
		value = (String) taskDesc.getParams().get(PARAM_TIMEOUT);
		this.timeout = value != null ? new Long(value) : DEFAULT_TIMEOUT;
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
			
			Socket initSocket = null;
			Socket socketOut = null;
			Socket socketIn = null;
			
			try {
				System.out.println("TCPTASK: " + getTestServerAddr() + ":" + getTestServerPort());
		    	
				//TODO: Secure connection
				try {
					initSocket = connect(result, InetAddress.getByName(getTestServerAddr()), getTestServerPort(), 
							QOS_SERVER_PROTOCOL_VERSION, "ACCEPT", getQoSTest().getTestSettings().isUseSsl() , CONTROL_CONNECTION_TIMEOUT);
				}
				catch (IOException e) {
					result.setFatalError(true);
					throw e;
				}
				
		    	initSocket.setSoTimeout(5000);
		    	
		    	String response = null;
		    	
		    	if (this.testPortOut != null) {
		    		sendMessage("TCPTEST OUT " + testPortOut + "\n");
		    		response = reader.readLine();
		    		if (response != null && response.startsWith("OK")) {
		    			socketOut = getSocket(getTestServerAddr(), testPortOut, false, (int)(timeout/1000000));
		    			socketOut.setSoTimeout((int)(timeout/1000000));
		    			sendMessage(socketOut, "PING\n");
		    			response = readLine(socketOut);
					
		    			System.out.println("TCP OUT TEST response: " + response);
					
		    			result.getResultMap().put(RESULT_RESPONSE_OUT, response);
		    			socketOut.close();
		    			result.getResultMap().put(RESULT_OUT, "OK");
		    		}
		    	}
				
		    	if (this.testPortIn != null) {
		    		ServerSocket serverSocket = null;
		    		try {
						serverSocket = new ServerSocket(testPortIn);
						sendMessage("TCPTEST IN " + testPortIn + "\n");
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
				
				sendMessage("QUIT\n");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if (initSocket != null && initSocket.isConnected()) {
					initSocket.close();
				}
				if (socketOut != null && socketOut.isConnected()) {
					socketOut.close();
				}
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
}
