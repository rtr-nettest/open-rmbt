/*******************************************************************************
 * Copyright 2019 alladin-IT GmbH
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

package at.rtr.rmbt.client.v2.task;

import java.net.Socket;
import java.net.SocketTimeoutException;

import at.rtr.rmbt.shared.qos.QosMeasurementType;
import at.rtr.rmbt.client.QualityOfServiceTest;
import at.rtr.rmbt.client.v2.task.result.QoSTestResult;

/**
 *
 */
public class EchoProtocolTcpTask extends AbstractEchoProtocolTask {

	/**
	 *
	 * @param taskDesc
	 */

	public EchoProtocolTcpTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId) {
		super(nnTest, taskDesc, threadId, threadId);
	}

	/**
	 * 
	 */
	public QoSTestResult call() throws Exception {
		final QoSTestResult result = initQoSTestResult(QosMeasurementType.ECHO_PROTOCOL);
		try {
			onStart(result);

			try {
				System.out.println("ECHO_PROTOCOL_TCP_TASK: " + getTestServerAddr() + ":" + getTestServerPort());
		    	

		    	if (this.testPort != null && this.testHost != null) {
					try (Socket socket = getSocket(testHost, testPort, false, (int)(timeout/1000000))){
						socket.setSoTimeout((int)(timeout/1000000));
						
						final long startTime = System.nanoTime();
						sendMessage(socket, this.payload + "\n");
						final String testResponse = readLine(socket);
						final long duration = System.nanoTime() - startTime;

						result.getResultMap().put(RESULT, testResponse);
						socket.close();
						if (this.payload.equals(testResponse)) {
							result.getResultMap().put(RESULT_STATUS, "OK");
						} else {
							result.getResultMap().put(RESULT_STATUS, "ERROR");
						}
						result.getResultMap().put(RESULT_RTT_NS, Long.toString(duration));
					}
					catch (SocketTimeoutException e) {
						result.getResultMap().put(RESULT_STATUS, "TIMEOUT");
					}
					catch (Exception e) {
						result.getResultMap().put(RESULT_STATUS, "ERROR");
					}
		    	} else {
		    		result.getResultMap().put(RESULT_STATUS, "ERROR");
				}
				
			}
			catch (Exception e) {
				e.printStackTrace();
				result.getResultMap().put(RESULT_STATUS, "ERROR");
			}
			
			return result;
		}
		catch (Exception e)  {
			throw e;
		}
		finally {

			if (this.testPort != null) {
				result.getResultMap().put(RESULT_PORT, testPort);
			}

			if (this.testHost != null) {
				result.getResultMap().put("echo_protocol_objective_host", this.testHost);
			}

			result.getResultMap().put(RESULT_TIMEOUT, timeout);

			if (this.payload != null) {
				result.getResultMap().put(AbstractEchoProtocolTask.PARAM_PAYLOAD, this.payload);
			}

			result.getResultMap().put(AbstractEchoProtocolTask.RESULT_PROTOCOL, AbstractEchoProtocolTask.PROTOCOL_TCP);

			onEnd(result);
		}
	}

}
