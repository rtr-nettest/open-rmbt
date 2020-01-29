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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import at.rtr.rmbt.client.QualityOfServiceTest;
import at.rtr.rmbt.client.v2.task.result.QoSTestResult;
import at.rtr.rmbt.client.v2.task.service.TestSettings;
import mockit.Expectations;
import mockit.Mocked;

import static org.junit.Assert.assertEquals;

/**
 * @author Felix Kendlbacher (fk@alladin.at)
 */
public class EchoProtocolTcpTaskTest {

    private static final String CORRECT_MESSAGE = "TCP payload!";

    private static final String WRONG_MESSAGE_ADDITIONAL_WHITESPACE = "TCP payload! ";

    private static final String WRONG_MESSAGE = "UDP payload!";

    private static final String ECHO_SERVICE_HOST = "echo.host";

    private OutputStream testOutputStream;

    private QualityOfServiceTest qosTest;

    private TestSettings testSettings;

    private RMBTClient clientHolder;

    @Before
    public void init() {
        testOutputStream = new ByteArrayOutputStream();

        testSettings = new TestSettings();
        testSettings.setUseSsl(false);
        testSettings.setStartTimeNs(12);
        clientHolder = RMBTClientTestStub.getInstance(TaskDescriptionHelper.createTaskDescList("host", "80",
                null, null, ECHO_SERVICE_HOST, new int[]{76}, new int[0]), null);

        qosTest = new QualityOfServiceTest(clientHolder, testSettings);

    }

    @Test
    public void testCorrectTcpEchoResponse(@Mocked final Socket socket) throws Exception {

        new Expectations() {{

            //return an outputstream we control
            socket.getOutputStream();
            result = testOutputStream;

            //return the sent message
            socket.getInputStream();
            result = new ByteArrayInputStream(CORRECT_MESSAGE.getBytes());

        }};

        final EchoProtocolTcpTask task = new EchoProtocolTcpTask(qosTest, clientHolder.getTaskDescList().get(0), 0);
        final QoSTestResult res = task.call();

        // the server receives the message with a \n at the end, thus the comparison needs be made against a message w/appended \n
        assertEquals("Payload sent to server != " + CORRECT_MESSAGE, CORRECT_MESSAGE + "\n", testOutputStream.toString());
        assertEquals("Result did not return OK","OK", res.getResultMap().get(AbstractEchoProtocolTask.RESULT_STATUS));
        assertEquals("Wrong echo service host in result", ECHO_SERVICE_HOST, res.getResultMap().get("echo_protocol_objective_host"));
        assertEquals("Wrong protocol type in result", AbstractEchoProtocolTask.PROTOCOL_TCP, res.getResultMap().get(AbstractEchoProtocolTask.RESULT_PROTOCOL));
        assertEquals("Payload stored in result !=" + CORRECT_MESSAGE, CORRECT_MESSAGE, res.getResultMap().get(AbstractEchoProtocolTask.RESULT));

    }

    @Test
    public void testWrongTcpEchoResponse (@Mocked final Socket socket) throws Exception {

        new Expectations() {{

            //return an outputstream we control
            socket.getOutputStream();
            result = testOutputStream;

            //return the sent message
            socket.getInputStream();
            result = new ByteArrayInputStream(WRONG_MESSAGE.getBytes());

        }};

        final EchoProtocolTcpTask task = new EchoProtocolTcpTask(qosTest, clientHolder.getTaskDescList().get(0), 0);
        final QoSTestResult res = task.call();

        // the server receives the message with a \n at the end, thus the comparison needs be made against a message w/appended \n
        assertEquals("Payload sent to server != " + CORRECT_MESSAGE, CORRECT_MESSAGE + "\n", testOutputStream.toString());
        assertEquals("Result did not return ERROR","ERROR", res.getResultMap().get(AbstractEchoProtocolTask.RESULT_STATUS));
        assertEquals("Wrong echo service host in result", ECHO_SERVICE_HOST, res.getResultMap().get("echo_protocol_objective_host"));
        assertEquals("Wrong protocol type in result", AbstractEchoProtocolTask.PROTOCOL_TCP, res.getResultMap().get(AbstractEchoProtocolTask.RESULT_PROTOCOL));
        assertEquals("Payload stored in result !=" + WRONG_MESSAGE, WRONG_MESSAGE, res.getResultMap().get(AbstractEchoProtocolTask.RESULT));
    }

    @Test
    public void testCorrectTcpEchoResponseWithAdditionalWhiteSpace (@Mocked final Socket socket) throws Exception {

        new Expectations() {{

            //return an outputstream we control
            socket.getOutputStream();
            result = testOutputStream;

            //return the sent message
            socket.getInputStream();
            result = new ByteArrayInputStream(WRONG_MESSAGE_ADDITIONAL_WHITESPACE.getBytes());

        }};

        final EchoProtocolTcpTask task = new EchoProtocolTcpTask(qosTest, clientHolder.getTaskDescList().get(0), 0);
        final QoSTestResult res = task.call();

        // the server receives the message with a \n at the end, thus the comparison needs be made against a message w/appended \n
        assertEquals("Payload sent to server != " + CORRECT_MESSAGE, CORRECT_MESSAGE + "\n", testOutputStream.toString());
        assertEquals("Result did not return ERROR","ERROR", res.getResultMap().get(AbstractEchoProtocolTask.RESULT_STATUS));
        assertEquals("Wrong echo service host in result", ECHO_SERVICE_HOST, res.getResultMap().get("echo_protocol_objective_host"));
        assertEquals("Wrong protocol type in result", AbstractEchoProtocolTask.PROTOCOL_TCP, res.getResultMap().get(AbstractEchoProtocolTask.RESULT_PROTOCOL));
        assertEquals("Payload stored in result !=" + WRONG_MESSAGE_ADDITIONAL_WHITESPACE, WRONG_MESSAGE_ADDITIONAL_WHITESPACE, res.getResultMap().get(AbstractEchoProtocolTask.RESULT));
    }

    @Test
    public void testTcpEchoResponseWithSocketError (@Mocked final Socket socket) throws Exception {

        new Expectations() {{

            //return a custom exception (failed connection)
            socket.getOutputStream();
            result = new SocketException("Forcefully thrown exception");

            //ignore
            socket.connect((SocketAddress) any, anyInt);

        }};

        final EchoProtocolTcpTask task = new EchoProtocolTcpTask(qosTest, clientHolder.getTaskDescList().get(0), 0);
        final QoSTestResult res = task.call();

        assertEquals("Result did not return ERROR","ERROR", res.getResultMap().get(AbstractEchoProtocolTask.RESULT_STATUS));
        assertEquals("Wrong echo service host in result", ECHO_SERVICE_HOST, res.getResultMap().get("echo_protocol_objective_host"));
        assertEquals("Wrong protocol type in result", AbstractEchoProtocolTask.PROTOCOL_TCP, res.getResultMap().get(AbstractEchoProtocolTask.RESULT_PROTOCOL));
    }

    @Test
    public void testTcpEchoResponseWithSocketTimeoutError(@Mocked final Socket socket) throws Exception {

        new Expectations() {{

            //return an outputstream we control
            socket.getOutputStream();
            result = testOutputStream;

            socket.getInputStream();
            result = new SocketTimeoutException("Forcefully thrown exception");

        }};

        final EchoProtocolTcpTask task = new EchoProtocolTcpTask(qosTest, clientHolder.getTaskDescList().get(0), 0);
        final QoSTestResult res = task.call();

        assertEquals("Result did not return TIMEOUT","TIMEOUT", res.getResultMap().get(AbstractEchoProtocolTask.RESULT_STATUS));
        assertEquals("Wrong echo service host in result", ECHO_SERVICE_HOST, res.getResultMap().get("echo_protocol_objective_host"));
        assertEquals("Wrong protocol type in result", AbstractEchoProtocolTask.PROTOCOL_TCP, res.getResultMap().get(AbstractEchoProtocolTask.RESULT_PROTOCOL));
    }
}
