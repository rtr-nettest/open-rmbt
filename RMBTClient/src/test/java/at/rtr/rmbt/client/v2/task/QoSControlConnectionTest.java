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
import at.rtr.rmbt.client.RMBTTestParameter;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author Felix Kendlbacher (fk@alladin.at)
 */
public class QoSControlConnectionTest {

    private QoSControlConnection controlConnection;

    private RMBTClient clientHolder;

    private RMBTTestParameter testParameter;

    private RMBTTestParameter sslTestParameter;

    private InetAddress loopbackAddress;

    private OutputStream testOutputStream;

    private int tcpCallbackCount;

    private String tcpCallbackRequest, tcpCallbackResponse;

    private String dnsCallbackRequest, dnsCallbackResponse;

    @Before
    public void init () {
        clientHolder = RMBTClientTestStub.getInstance(TaskDescriptionHelper.createTaskDescList("host", "80",
                null, null, "host", null, null), null);

        testParameter = new RMBTTestParameter("host", 80, false, "token", 1, 1, 1, 1);
        sslTestParameter = new RMBTTestParameter("host", 80, true, 1, 1, 1);

        loopbackAddress = InetAddress.getLoopbackAddress();

        testOutputStream = new ByteArrayOutputStream();

        tcpCallbackCount = 0;
    }

    @Test
    public void basicConnectionTest (@Mocked final Socket socket, @Mocked final InetAddress inetAddress,
            @Mocked final BufferedReader bufferedReader) throws Exception {

        new Expectations() {{

            InetAddress.getByName(anyString);
            result = loopbackAddress;

            bufferedReader.readLine();
            times = 4;
            returns(
                    AbstractQoSTask.QOS_SERVER_PROTOCOL_VERSION,
                    "ACCEPT ",
                    "OK"
            );
            result = " ";

            socket.getOutputStream();
            times = 1;
            result = testOutputStream;

            socket.connect((SocketAddress) any, anyInt);
            times = 1;

            socket.close();
            times = 1;


        }};

        controlConnection = new QoSControlConnection(clientHolder, testParameter);
        controlConnection.connect();
        assertTrue("QoSControlConnection did not start running", controlConnection.isRunning.get());
        assertFalse("QoSControlConnection shows unexpected connection issue", controlConnection.couldNotConnect.get());
        assertTrue("Unexpected sent token", testOutputStream.toString().startsWith("TOKEN token\n"));
        assertFalse("Message already contained a quit", testOutputStream.toString().contains("QUIT"));

        controlConnection.close();
        assertFalse("QoSControlConnection still running after call to close", controlConnection.isRunning.get());
        assertTrue("No Quit command sent to control service", testOutputStream.toString().endsWith("QUIT\n"));
    }

    @Test(expected = SocketException.class)
    public void basicConnectionExceptionTest (@Mocked final Socket socket, @Mocked final InetAddress inetAddress) throws Exception {

        new Expectations() {{

            InetAddress.getByName(anyString);
            result = loopbackAddress;


            socket.connect((SocketAddress) any, anyInt);
            times = 1;
            result = new SocketException("Forcefully thrown exception");

        }};

        controlConnection = new QoSControlConnection(clientHolder, testParameter);

        try {
            controlConnection.connect();
        } catch (SocketException ex) {
            assertEquals("Forcefully thrown exception", ex.getMessage());
            assertFalse("ControlConnection wrongly says it's running", controlConnection.isRunning.get());
            assertTrue("ControlConnection wrongly says it could connect", controlConnection.couldNotConnect.get());
            throw ex;
        }

    }

    @Test
    public void connectSendTaskCommandsWithCallbacks (@Mocked final Socket socket, @Mocked final InetAddress inetAddress,
                                                      @Mocked final BufferedReader bufferedReader,
                                                      @Mocked final DnsTask dnsTask, @Mocked final TcpTask tcpTask) throws Exception {

        new Expectations() {{

            InetAddress.getByName(anyString);
            result = loopbackAddress;

            bufferedReader.readLine();
            returns(
                    AbstractQoSTask.QOS_SERVER_PROTOCOL_VERSION,
                    "ACCEPT ",
                    "OK",
                    " ",
                    "INVALID SERVER RETURN",
                    "TCP TASK +ID2",
                    "TCP TASK +ID2",
                    "DNS TASK +ID1"
            );
            result = null;

            socket.getOutputStream();
            times = 1;
            result = testOutputStream;

            socket.connect((SocketAddress) any, anyInt);
            times = 1;

            socket.close();

            dnsTask.getId();
            result = 1;

            tcpTask.getId();
            result = 2;

        }};

        controlConnection = new QoSControlConnection(clientHolder, testParameter);
        controlConnection.connect();
        assertTrue("QoSControlConnection did not start running", controlConnection.isRunning.get());
        assertFalse("QoSControlConnection shows unexpected connection issue", controlConnection.couldNotConnect.get());
        assertTrue("Unexpected sent token", testOutputStream.toString().startsWith("TOKEN token\n"));
        assertFalse("Message already contained a quit", testOutputStream.toString().contains("QUIT"));

        final CountDownLatch latch = new CountDownLatch(1);

        final ControlConnectionResponseCallback tcpCallback = new ControlConnectionResponseCallback() {
            @Override
            public void onResponse(String response, String request) {
                tcpCallbackCount++;
                tcpCallbackResponse = response;
                tcpCallbackRequest = request;
                latch.countDown();
            }
        };

        final ControlConnectionResponseCallback dnsCallback = new ControlConnectionResponseCallback() {
            @Override
            public void onResponse(String response, String request) {
                dnsCallbackRequest = request;
                dnsCallbackResponse = response;
                try {
                    controlConnection.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    fail("Exception thrown when closing connection");
                }
            }
        };

        controlConnection.sendTaskCommand(dnsTask, "CMD DNS", dnsCallback);
        System.out.println(testOutputStream.toString());
        assertTrue("No DnsTask cmd sent to server", testOutputStream.toString().endsWith("CMD DNS +ID1\n"));


        controlConnection.sendTaskCommand(tcpTask, "CMD TCP", tcpCallback);
        System.out.println(testOutputStream.toString());
        assertTrue("No TCPTask cmd sent to server", testOutputStream.toString().endsWith("CMD TCP +ID2\n"));

        // the run loops until the tcp callback was called
        controlConnection.run();

        latch.await(10, TimeUnit.SECONDS);

        assertEquals("TCP callback executed too many times", 1, tcpCallbackCount);
        assertEquals("Wrong callback response", "TCP TASK +ID2", tcpCallbackResponse);
        assertEquals("Wrong callback request", "CMD TCP", tcpCallbackRequest);
        assertEquals("Wrong callback response", "DNS TASK +ID1", dnsCallbackResponse);
        assertEquals("Wrong callback request", "CMD DNS", dnsCallbackRequest);

        assertFalse("QoSControlConnection still running after call to close", controlConnection.isRunning.get());
        assertTrue("No Quit command sent to control service", testOutputStream.toString().endsWith("QUIT\n"));

    }

    @Test
    public void basicConnectionProtocolErrorTest (@Mocked final Socket socket, @Mocked final InetAddress inetAddress,
                                     @Mocked final BufferedReader bufferedReader) throws Exception {

        new Expectations() {{

            InetAddress.getByName(anyString);
            result = loopbackAddress;

            bufferedReader.readLine();
            result = "NOT A PROTOCOL";

            socket.getOutputStream();
            times = 1;
            result = testOutputStream;

        }};


        controlConnection = new QoSControlConnection(clientHolder, sslTestParameter);
        controlConnection.connect();
        assertNull("Control socket is not null despite protocol error", controlConnection.controlSocket);
        assertFalse("QoSControlConnection reports as running", controlConnection.isRunning.get());
        assertTrue("QoSControlConnection does not show connection issue", controlConnection.couldNotConnect.get());

    }

    @Test
    public void basicConnectionNoAcceptErrorTest (@Mocked final Socket socket, @Mocked final InetAddress inetAddress,
                                                  @Mocked final BufferedReader bufferedReader) throws Exception {

        new Expectations() {{

            InetAddress.getByName(anyString);
            result = loopbackAddress;

            bufferedReader.readLine();
            returns(AbstractQoSTask.QOS_SERVER_PROTOCOL_VERSION,
                    "NOT A PROTOCOL");

            socket.getOutputStream();
            times = 1;
            result = testOutputStream;

        }};


        controlConnection = new QoSControlConnection(clientHolder, testParameter);
        controlConnection.connect();
        assertNull("Control socket is not null despite protocol error", controlConnection.controlSocket);
        assertFalse("QoSControlConnection reports as running", controlConnection.isRunning.get());
        assertTrue("QoSControlConnection does not show connection issue", controlConnection.couldNotConnect.get());

    }

    @Test
    public void basicConnectionNoOKErrorTest (@Mocked final Socket socket, @Mocked final InetAddress inetAddress,
                                                  @Mocked final BufferedReader bufferedReader) throws Exception {

        new Expectations() {{

            InetAddress.getByName(anyString);
            result = loopbackAddress;

            bufferedReader.readLine();
            returns(AbstractQoSTask.QOS_SERVER_PROTOCOL_VERSION,
                    "ACCEPT",
                    "NOT A PROTOCOL");

            socket.getOutputStream();
            times = 1;
            result = testOutputStream;

        }};


        controlConnection = new QoSControlConnection(clientHolder, testParameter);
        controlConnection.connect();
        assertNull("Control socket is not null despite protocol error", controlConnection.controlSocket);
        assertFalse("QoSControlConnection reports as running", controlConnection.isRunning.get());
        assertTrue("QoSControlConnection does not show connection issue", controlConnection.couldNotConnect.get());

    }

    @Test
    public void basicConnectionNoOKNullErrorTest (@Mocked final Socket socket, @Mocked final InetAddress inetAddress,
                                              @Mocked final BufferedReader bufferedReader) throws Exception {

        new Expectations() {{

            InetAddress.getByName(anyString);
            result = loopbackAddress;

            bufferedReader.readLine();
            returns(AbstractQoSTask.QOS_SERVER_PROTOCOL_VERSION,
                    "ACCEPT",
                    (String) null);

            socket.getOutputStream();
            times = 1;
            result = testOutputStream;

        }};


        controlConnection = new QoSControlConnection(clientHolder, testParameter);
        controlConnection.connect();
        assertNull("Control socket is not null despite protocol error", controlConnection.controlSocket);
        assertFalse("QoSControlConnection reports as running", controlConnection.isRunning.get());
        assertTrue("QoSControlConnection does not show connection issue", controlConnection.couldNotConnect.get());

    }

}
