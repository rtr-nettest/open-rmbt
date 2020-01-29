/*******************************************************************************
 * Copyright 2013-2019 alladin-IT GmbH
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

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import at.rtr.rmbt.shared.qos.QosMeasurementType;
import at.rtr.rmbt.client.QualityOfServiceTest;
import at.rtr.rmbt.client.v2.task.result.QoSTestResult;
import at.rtr.rmbt.shared.qos.util.SipTaskHelper;
import at.rtr.rmbt.util.net.sip.SipRequestMessage;
import at.rtr.rmbt.util.net.sip.SipResponseMessage;
import at.rtr.rmbt.util.net.sip.SipUtil;

/**
 * 
 * @author lb
 *
 */
public class SipTask extends AbstractQoSTask {

	private final int port;
	
	private final long timeout;

	private final long count;

	private final long callDuration;
	
	private final String to;
	
	private final String from;
	
	private final String via;

	private float callSetupSuccessRate = 0f;

	private float callCompletionSuccessRate = 0f;

	/**
	 * 
	 * @param taskDesc
	 */
	public SipTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId) {
		super(nnTest, taskDesc, threadId, threadId);
		
		this.port = Integer.valueOf((String)taskDesc.getParams().get(SipTaskHelper.PARAM_PORT));
		
		String value = (String) taskDesc.getParams().get(SipTaskHelper.PARAM_TIMEOUT);
		this.timeout = value != null ? Long.valueOf(value) : SipTaskHelper.DEFAULT_TIMEOUT;

        value = (String) taskDesc.getParams().get(SipTaskHelper.PARAM_COUNT);
        this.count = value != null ? Long.valueOf(value) : SipTaskHelper.DEFAULT_COUNT;

        value = (String) taskDesc.getParams().get(SipTaskHelper.PARAM_CALL_DURATION);
		this.callDuration = value != null ? Long.valueOf(value) : SipTaskHelper.DEFAULT_CALL_DURATION;
		
		value = (String) taskDesc.getParams().get(SipTaskHelper.PARAM_TO);
		this.to = value != null ? value : "";
		
		value = (String) taskDesc.getParams().get(SipTaskHelper.PARAM_FROM);
		this.from = value != null ? value : "";
		
		value = (String) taskDesc.getParams().get(SipTaskHelper.PARAM_VIA);
		this.via = value != null ? value : "";
}

	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	public QoSTestResult call() throws Exception {
		final QoSTestResult result = initQoSTestResult(QosMeasurementType.SIP);
		try {
			onStart(result);

			result.getResultMap().put(SipTaskHelper.PARAM_OBJECTIVE_PORT, port);
            result.getResultMap().put(SipTaskHelper.PARAM_OBJECTIVE_COUNT, count);
			result.getResultMap().put(SipTaskHelper.PARAM_OBJECTIVE_CALL_DURATION, callDuration);
			result.getResultMap().put(SipTaskHelper.PARAM_OBJECTIVE_FROM, from);
			result.getResultMap().put(SipTaskHelper.PARAM_OBJECTIVE_TO, to);
			result.getResultMap().put(SipTaskHelper.PARAM_OBJECTIVE_VIA, via);
			result.getResultMap().put(SipTaskHelper.PARAM_OBJECTIVE_TIMEOUT, timeout);

			for (int i = 0; i < count; i++) {
                runSipTest(result.getResultMap());
            }

			final float ccsr = (float) callCompletionSuccessRate / (float) count;
			final float cssr = (float) callSetupSuccessRate / (float) count;

			BigDecimal bigCcsr = new BigDecimal(""+ccsr);
			BigDecimal bigOne = new BigDecimal("1.0");

			result.getResultMap().put(SipTaskHelper.PARAM_RESULT_CCSR, ccsr);
            result.getResultMap().put(SipTaskHelper.PARAM_RESULT_CSSR, cssr);
            result.getResultMap().put(SipTaskHelper.PARAM_RESULT_DCR,
					bigOne.subtract(bigCcsr).floatValue());
			
			return result;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			onEnd(result);
		}

	}

	public void runSipTest(final HashMap<String, Object> result) throws IOException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        final ControlConnectionResponseCallback callback = new ControlConnectionResponseCallback() {

            public void onResponse(final String controlResponse, final String controlRequest) {
                try {
                    //wait for ok -> server has opened requested socket
                    if (controlResponse.startsWith("OK")) {
                        //reset response string:
                        //open test socket
                        InetSocketAddress socketAddr = new InetSocketAddress(InetAddress.getByName(getTestServerAddr()), port);
                        System.out.println("SIP Connecting to: " + socketAddr.toString());
                        Socket testSocket = new Socket();
                        testSocket.connect(socketAddr, (int)(timeout/1000000));
                        testSocket.setSoTimeout((int)(timeout/1000000));

                        System.out.println("SIP is connected: " + testSocket.isConnected());

                        resetSocketBufferedReader(testSocket);

                        //send INVITE request to qos service
                        final SipRequestMessage msgInvite = SipUtil.generateInviteMessage(from, to, via);
                        sendMessage(testSocket, msgInvite.getDataAsString());

                        //wait for TRYING response:
                        String response = readMultiLine(testSocket);
                        final SipResponseMessage responseTrying = SipUtil.parseResponseData(response);
                        System.out.println("SIP response #1: " + responseTrying);

                        if (!SipResponseMessage.SipResponseType.TRYING.equals(responseTrying.getType())) {
                            return;
                        }

                        //wait for RINGING response:
                        response = readMultiLine(testSocket);
                        final SipResponseMessage responseRinging = SipUtil.parseResponseData(response);
                        System.out.println("SIP response #2: " + responseRinging);

                        if (!SipResponseMessage.SipResponseType.RINGING.equals(responseRinging.getType())) {
                            return;
                        }

                        //increase call setup success rate
                        callSetupSuccessRate++;

                        //simulate call by waiting for the duration of the call
                        Thread.sleep((int)(callDuration/1000000));

                        //send BYE request to qos service
                        final SipRequestMessage msgBye = SipUtil.generateByeMessage(from, to, via);
                        sendMessage(testSocket, msgBye.getDataAsString());

                        //wait for OK response:
                        response = readMultiLine(testSocket);
                        final SipResponseMessage responseOk = SipUtil.parseResponseData(response);
                        System.out.println("SIP response #3: " + responseOk);

                        if (!SipResponseMessage.SipResponseType.OK.equals(responseOk.getType())) {
                            return;
                        }

                        //increase call completion success rate
                        callCompletionSuccessRate++;

                        result.put(SipTaskHelper.PARAM_RESULT, "OK");
                    }
                    else {
                        result.put(SipTaskHelper.PARAM_RESULT, "ERROR");
                    }
                }
                catch (SocketTimeoutException e) {
                    e.printStackTrace();
                    result.put(SipTaskHelper.PARAM_RESULT, "TIMEOUT");
                }
                catch (Exception e) {
                    e.printStackTrace();
                    result.put(SipTaskHelper.PARAM_RESULT, "ERROR");
                }
                finally {
                    latch.countDown();
                }
            }
        };

        sendCommand("SIPTEST " + port, callback);
        if (!latch.await(timeout, TimeUnit.NANOSECONDS)) {
            result.put(SipTaskHelper.PARAM_RESULT, "TIMEOUT");
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
	public QosMeasurementType getTestType() {
		return QosMeasurementType.SIP;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#needsQoSControlConnection()
	 */
	public boolean needsQoSControlConnection() {
		return true;
	}

}
