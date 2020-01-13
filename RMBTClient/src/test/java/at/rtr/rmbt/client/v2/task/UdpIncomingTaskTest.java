/*******************************************************************************
 * Copyright 2019 alladin-IT GmbH
 * Copyright 2020 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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

import at.rtr.rmbt.client.RMBTClient;
import at.rtr.rmbt.client.RMBTClientTestStub;
import org.junit.Before;
import org.junit.Test;

import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.rtr.rmbt.shared.qos.UdpPayload;
import at.rtr.rmbt.shared.qos.util.UdpPayloadUtil;
import at.rtr.rmbt.client.QualityOfServiceTest;
import at.rtr.rmbt.client.v2.task.result.QoSTestResult;
import at.rtr.rmbt.client.v2.task.service.TestSettings;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;

import static org.junit.Assert.assertEquals;

/**
 * @author Felix Kendlbacher (fk@alladin.at)
 */
public class UdpIncomingTaskTest {

    private InetAddress loopback;

    private TestSettings testSettings;

    private RMBTClient clientHolder;

    private QualityOfServiceTest qosTest;

    private TaskDesc incomingTaskDesc;

    private List<UdpPayload> udpPayloadList;

    private String taskCmd;

    @Before
    public void init () {
        loopback = InetAddress.getLoopbackAddress();
        testSettings = new TestSettings();
        testSettings.setUseSsl(false);
        testSettings.setStartTimeNs(12);
        clientHolder = RMBTClientTestStub.getInstance(TaskDescriptionHelper.createTaskDescList("host", "80",
                null, new int[] {80}, "host", null, null), null);

        qosTest = new QualityOfServiceTest(clientHolder, testSettings);

        // provide params for incoming tcp test
        final Map<String, Object> params = new HashMap<>();
        params.put(UdpTask.PARAM_PORT, "80");
        params.put(AbstractQoSTask.PARAM_QOS_TEST_OBJECTIVE_ID, 1);
        params.put(AbstractQoSTask.PARAM_QOS_CONCURRENCY_GROUP, 200);
        params.put(UdpTask.PARAM_NUM_PACKETS_INCOMING, "1");

        incomingTaskDesc = new TaskDesc("host", 80, false, "token", 80,
                1, 1, 1, params);
        clientHolder.getTaskDescList().add(incomingTaskDesc);

        //prepare payloads for sending
        udpPayloadList = new ArrayList<>();
        UdpPayload payload = new UdpPayload();
        payload.setCommunicationFlag(1);
        payload.setPacketNumber(0);
        payload.setUuid("48b7f4db-2f60-43f7-861b-321c269e1d8" + 0);
        payload.setTimestamp(System.currentTimeMillis());
        udpPayloadList.add(payload);

        payload = new UdpPayload();
        payload.setCommunicationFlag(1);
        payload.setPacketNumber(1);
        payload.setUuid("48b7f4db-2f60-43f7-861b-321c269e1d8" + 1);
        payload.setTimestamp(System.currentTimeMillis());
        udpPayloadList.add(payload);

        payload = new UdpPayload();
        payload.setCommunicationFlag(1);
        payload.setPacketNumber(2);
        payload.setUuid("48b7f4db-2f60-43f7-861b-321c269e1d8" + 2);
        payload.setTimestamp(System.currentTimeMillis());
        udpPayloadList.add(payload);

        payload = new UdpPayload();
        payload.setCommunicationFlag(1);
        payload.setPacketNumber(3);
        payload.setUuid("48b7f4db-2f60-43f7-861b-321c269e1d8" + 3);
        payload.setTimestamp(System.currentTimeMillis());
        udpPayloadList.add(payload);

        payload = new UdpPayload();
        payload.setCommunicationFlag(1);
        payload.setPacketNumber(4);
        payload.setUuid("48b7f4db-2f60-43f7-861b-321c269e1d8" + 4);
        payload.setTimestamp(System.currentTimeMillis());
        udpPayloadList.add(payload);

    }

    /**
     * Incoming udp
     */

    @Test
    public void testUdpIncomingSingleWorking (@Mocked final QoSControlConnection controlConnection, @Mocked final DatagramSocket socket,
                                 @Mocked final DataOutputStream dataOutputStream) throws Exception {

        new Expectations() {{

            controlConnection.sendTaskCommand((AbstractQoSTask) any, withPrefix("UDPTEST IN"), (ControlConnectionResponseCallback) any);
            times = 1;
            result = new Delegate() {
                public void delegate (AbstractQoSTask qoSTask, String cmd, ControlConnectionResponseCallback callback) {
                    taskCmd = cmd;
                }
            };

            controlConnection.sendTaskCommand((AbstractQoSTask) any, "GET UDPRESULT IN 80", (ControlConnectionResponseCallback) any);
            result = new Delegate() {
                public void delegate (AbstractQoSTask qoSTask, String cmd, ControlConnectionResponseCallback callback) {
                    callback.onResponse("RCV 1 1", cmd);
                }
            };

            socket.receive((DatagramPacket) any);
            result = new Delegate() {
                public void delegate (DatagramPacket packet) {
                    packet.setData(UdpPayloadUtil.toBytes(udpPayloadList.get(0)));
                    packet.setPort(80);
                    packet.setAddress(loopback);
                }
            };

        }};

        final UdpTask udpTask = new UdpTask(qosTest, incomingTaskDesc, 1);
        udpTask.setControlConnection(controlConnection);
        final QoSTestResult res = udpTask.call();

        assertEquals("Did not receive incoming packets",1, res.getResultMap().get(UdpTask.RESULT_NUM_PACKETS_INCOMING));
        assertEquals("Did not receive incoming packets",1, res.getResultMap().get(UdpTask.RESULT_INCOMING_PACKETS));
        assertEquals("Unexpected packet loss rate", "0", res.getResultMap().get(UdpTask.RESULT_INCOMING_PLR));
        assertEquals("Sent command to qos service is not as expected", "UDPTEST IN 80 1", taskCmd);
    }

    @Test
    public void testUdpIncomingMultipleRandomOrderWorking (@Mocked final QoSControlConnection controlConnection, @Mocked final DatagramSocket socket,
                                              @Mocked final DataOutputStream dataOutputStream) throws Exception {

        new Expectations() {{

            controlConnection.sendTaskCommand((AbstractQoSTask) any, withPrefix("UDPTEST IN"), (ControlConnectionResponseCallback) any);
            times = 1;
            result = new Delegate() {
                public void delegate (AbstractQoSTask qoSTask, String cmd, ControlConnectionResponseCallback callback) {
                    taskCmd = cmd;
                }
            };

            controlConnection.sendTaskCommand((AbstractQoSTask) any, "GET UDPRESULT IN 80", (ControlConnectionResponseCallback) any);
            result = new Delegate() {
                public void delegate (AbstractQoSTask qoSTask, String cmd, ControlConnectionResponseCallback callback) {
                    callback.onResponse("RCV 4 2", cmd);
                }
            };

            // send packets 0, 1, 1, 2 in order (the duplicate shall be registered as such)
            socket.receive((DatagramPacket) any);
            returns(
                    new Delegate() {
                        public void delegate (DatagramPacket packet) {
                            packet.setData(UdpPayloadUtil.toBytes(udpPayloadList.get(0)));
                            packet.setPort(80);
                            packet.setAddress(loopback);
                        }
                    }
                    ,
                    new Delegate() {
                        public void delegate (DatagramPacket packet) {
                            packet.setData(UdpPayloadUtil.toBytes(udpPayloadList.get(3)));
                            packet.setPort(80);
                            packet.setAddress(loopback);
                        }
                    }
                    ,
                    new Delegate() {
                        public void delegate (DatagramPacket packet) {
                            packet.setData(UdpPayloadUtil.toBytes(udpPayloadList.get(1)));
                            packet.setPort(80);
                            packet.setAddress(loopback);
                        }
                    }
            );
            result = new Delegate() {
                public void delegate (DatagramPacket packet) {
                    packet.setData(UdpPayloadUtil.toBytes(udpPayloadList.get(2)));
                    packet.setPort(80);
                    packet.setAddress(loopback);
                }
            };

        }};

        incomingTaskDesc.getParams().put(UdpTask.PARAM_NUM_PACKETS_INCOMING, "4");

        final UdpTask udpTask = new UdpTask(qosTest, incomingTaskDesc, 1);
        udpTask.setControlConnection(controlConnection);
        final QoSTestResult res = udpTask.call();

        System.out.println(res.toString());

        assertEquals("Did not receive incoming packets",4, res.getResultMap().get(UdpTask.RESULT_NUM_PACKETS_INCOMING));
        assertEquals("Did not receive incoming packets",4, res.getResultMap().get(UdpTask.RESULT_INCOMING_PACKETS));
        assertEquals("Unexpected packet loss rate", "0", res.getResultMap().get(UdpTask.RESULT_INCOMING_PLR));
        assertEquals("Sent command to qos service is not as expected", "UDPTEST IN 80 4", taskCmd);
    }

    @Test
    public void testUdpIncomingMultipleDuplicatesWorking (@Mocked final QoSControlConnection controlConnection, @Mocked final DatagramSocket socket,
                                                           @Mocked final DataOutputStream dataOutputStream) throws Exception {

        new Expectations() {{

            controlConnection.sendTaskCommand((AbstractQoSTask) any, withPrefix("UDPTEST IN"), (ControlConnectionResponseCallback) any);
            times = 1;
            result = new Delegate() {
                public void delegate (AbstractQoSTask qoSTask, String cmd, ControlConnectionResponseCallback callback) {
                    taskCmd = cmd;
                }
            };

            controlConnection.sendTaskCommand((AbstractQoSTask) any, "GET UDPRESULT IN 80", (ControlConnectionResponseCallback) any);
            result = new Delegate() {
                public void delegate (AbstractQoSTask qoSTask, String cmd, ControlConnectionResponseCallback callback) {
                    callback.onResponse("RCV 2 2", cmd);
                }
            };

            // send packets 0, 1, 1, 2 in order (the duplicate shall be registered as such)
            socket.receive((DatagramPacket) any);
            returns(
                    new Delegate() {
                        public void delegate (DatagramPacket packet) {
                            packet.setData(UdpPayloadUtil.toBytes(udpPayloadList.get(0)));
                            packet.setPort(80);
                            packet.setAddress(loopback);
                        }
                    }
                    ,
                    new Delegate() {
                        public void delegate (DatagramPacket packet) {
                            packet.setData(UdpPayloadUtil.toBytes(udpPayloadList.get(3)));
                            packet.setPort(80);
                            packet.setAddress(loopback);
                        }
                    }
                    ,
                    new Delegate() {
                        public void delegate (DatagramPacket packet) {
                            packet.setData(UdpPayloadUtil.toBytes(udpPayloadList.get(3)));
                            packet.setPort(80);
                            packet.setAddress(loopback);
                        }
                    }
            );
            result = new Delegate() {
                public void delegate (DatagramPacket packet) {
                    packet.setData(UdpPayloadUtil.toBytes(udpPayloadList.get(0)));
                    packet.setPort(80);
                    packet.setAddress(loopback);
                }
            };

        }};

        incomingTaskDesc.getParams().put(UdpTask.PARAM_NUM_PACKETS_INCOMING, "4");

        final UdpTask udpTask = new UdpTask(qosTest, incomingTaskDesc, 1);
        udpTask.setControlConnection(controlConnection);
        final QoSTestResult res = udpTask.call();

        assertEquals("Did not receive incoming packets",4, res.getResultMap().get(UdpTask.RESULT_NUM_PACKETS_INCOMING));
        //we expect 2 packets, as two of them are a duplicate
        assertEquals("Did not receive incoming packets",2, res.getResultMap().get(UdpTask.RESULT_INCOMING_PACKETS));
        assertEquals("Unexpected packet loss rate", "50", res.getResultMap().get(UdpTask.RESULT_INCOMING_PLR));
        assertEquals("Sent command to qos service is not as expected", "UDPTEST IN 80 4", taskCmd);
    }

    @Test
    public void testUdpIncomingWithSocketException (@Mocked final QoSControlConnection controlConnection, @Mocked final DatagramSocket socket,
                                                            @Mocked final DataOutputStream dataOutputStream) throws Exception {

        new Expectations() {{

            controlConnection.sendTaskCommand((AbstractQoSTask) any, withPrefix("UDPTEST IN"), (ControlConnectionResponseCallback) any);
            times = 1;
            result = new Delegate() {
                public void delegate (AbstractQoSTask qoSTask, String cmd, ControlConnectionResponseCallback callback) {
                    taskCmd = cmd;
                }
            };

            controlConnection.sendTaskCommand((AbstractQoSTask) any, "GET UDPRESULT IN 80", (ControlConnectionResponseCallback) any);
            result = new Delegate() {
                public void delegate (AbstractQoSTask qoSTask, String cmd, ControlConnectionResponseCallback callback) {
                    callback.onResponse("RCV 1 2", cmd);
                }
            };

            // send packets 0, 1, 1, 2 in order (the duplicate shall be registered as such)
            socket.receive((DatagramPacket) any);
            result = new Delegate() {
                        public void delegate (DatagramPacket packet) {
                            packet.setData(UdpPayloadUtil.toBytes(udpPayloadList.get(0)));
                            packet.setPort(80);
                            packet.setAddress(loopback);
                        }
                    };
            result = new SocketException("Forcefully thrown exception");

        }};

        incomingTaskDesc.getParams().put(UdpTask.PARAM_NUM_PACKETS_INCOMING, "2");

        final UdpTask udpTask = new UdpTask(qosTest, incomingTaskDesc, 1);
        udpTask.setControlConnection(controlConnection);
        final QoSTestResult res = udpTask.call();

        assertEquals("Did not receive incoming packets",2, res.getResultMap().get(UdpTask.RESULT_NUM_PACKETS_INCOMING));
        assertEquals("Did not receive incoming packets",1, res.getResultMap().get(UdpTask.RESULT_INCOMING_PACKETS));
        assertEquals("Unexpected packet loss rate", "50", res.getResultMap().get(UdpTask.RESULT_INCOMING_PLR));
        assertEquals("Sent command to qos service is not as expected", "UDPTEST IN 80 2", taskCmd);

    }

    @Test
    public void testUdpIncomingWithControlConnectException (@Mocked final QoSControlConnection controlConnection, @Mocked final DatagramSocket socket,
                                                            @Mocked final DataOutputStream dataOutputStream) throws Exception {

        new Expectations() {{

            controlConnection.sendTaskCommand((AbstractQoSTask) any, withPrefix("UDPTEST IN"), (ControlConnectionResponseCallback) any);
            times = 1;
            result = new Delegate() {
                public void delegate (AbstractQoSTask qoSTask, String cmd, ControlConnectionResponseCallback callback) {
                    taskCmd = cmd;
                }
            };

            controlConnection.sendTaskCommand((AbstractQoSTask) any, "GET UDPRESULT IN 80", (ControlConnectionResponseCallback) any);
            times = 1;
            result = new Delegate() {
                public void delegate (AbstractQoSTask qoSTask, String cmd, ControlConnectionResponseCallback callback) {
                    callback.onResponse("NEW RCV 2 1", cmd);
                }
            };

            socket.receive((DatagramPacket) any);
            result = new Delegate() {
                public void delegate (DatagramPacket packet) {
                    packet.setData(UdpPayloadUtil.toBytes(udpPayloadList.get(0)));
                    packet.setPort(80);
                    packet.setAddress(loopback);
                }
            };

        }};

        final UdpTask udpTask = new UdpTask(qosTest, incomingTaskDesc, 1);
        udpTask.setControlConnection(controlConnection);
        final QoSTestResult res = udpTask.call();

        assertEquals("Did not receive incoming packets",1, res.getResultMap().get(UdpTask.RESULT_NUM_PACKETS_INCOMING));
        assertEquals("Did not receive incoming packets",1, res.getResultMap().get(UdpTask.RESULT_INCOMING_PACKETS));
        //PLR is 100 because the control connection responded with the new protocol
        assertEquals("Unexpected packet loss rate", "100", res.getResultMap().get(UdpTask.RESULT_INCOMING_PLR));
        assertEquals("Sent command to qos service is not as expected", "UDPTEST IN 80 1", taskCmd);

    }

}
