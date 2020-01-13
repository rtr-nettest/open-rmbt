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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.rtr.rmbt.client.v2.task.AbstractEchoProtocolTask;
import at.rtr.rmbt.client.v2.task.TaskDesc;
import at.rtr.rmbt.client.helper.Config;

/**
 * @author Felix Kendlbacher (alladin-IT GmbH)
 */
public class TaskDescriptionHelper {

    public static List<TaskDesc> createTaskDescList(final String host, final String controlConnectionPort, final int[] tcpTestPorts, final int[] udpTestPorts,
                                                    final String echoServiceHost, final int[] echoServiceTcpPorts, final int[] echoServiceUdpPorts) {
        final List<TaskDesc> taskDescList = new ArrayList<>();
        // The fake token will only be accepted by qos-servers w/out any token checks (it will NOT work in a production env)
        final String fakeToken = "bbd1ee96-0779-4619-b993-bb4bf7089754_1528136454_3gr2gw9lVhtVONV0XO62Vamu/uw=";

        //TCP
        if (tcpTestPorts != null) {
            int tcpUuid = 100; //provide temp uuids
            for (Integer port : tcpTestPorts) {
                final Map<String, Object> params = new HashMap<>();
                params.put("concurrency_group", "200");
                params.put("server_port", controlConnectionPort);
                params.put("qos_test_uid", Integer.toString(tcpUuid++));
                params.put("server_addr", host);
                params.put("timeout", "5000000000");
                params.put("out_port", Integer.toString(port));
                params.put("qostest", "tcp");

                final TaskDesc task = new TaskDesc(host, Integer.parseInt(controlConnectionPort), Config.RMBT_QOS_SSL, fakeToken,
                        0, 1, 0, System.nanoTime(), params, "tcp");
                taskDescList.add(task);
            }
        }


        //UDP
        if (udpTestPorts != null) {
            int udpUuid = 300;  //provide temp uuids
            for (Integer port : udpTestPorts) {
                final Map<String, Object> params = new HashMap<>();
                params.put("concurrency_group", "201");
                params.put("out_num_packets", "1");
                params.put("server_port", controlConnectionPort);
                params.put("qos_test_uid", Integer.toString(udpUuid++));
                params.put("server_addr", host);
                params.put("timeout", "5000000000");
                params.put("out_port", Integer.toString(port));
                params.put("qostest", "udp");

                final TaskDesc task = new TaskDesc(host, Integer.parseInt(controlConnectionPort), Config.RMBT_QOS_SSL, fakeToken,
                        0, 1, 0, System.nanoTime(), params, "udp");
                taskDescList.add(task);
            }
        }

        //ECHO PROTOCOL
        if (echoServiceHost != null) {
            int echoUuid = 400;

            if (echoServiceTcpPorts != null) {
                for (int port : echoServiceTcpPorts) {
                    final Map<String, Object> params = new HashMap<>();
                    params.put("concurrency_group", "301");
                    params.put("qos_test_uid", Integer.toString(echoUuid++));
                    params.put("timeout", "5000000000");
                    params.put("qostest", "echo_protocol");
                    params.put(AbstractEchoProtocolTask.RESULT_PROTOCOL, "tcp");
                    params.put(AbstractEchoProtocolTask.PARAM_PAYLOAD, "TCP payload!");
                    params.put(AbstractEchoProtocolTask.PARAM_SERVER_ADDRESS, echoServiceHost);
                    params.put(AbstractEchoProtocolTask.PARAM_SERVER_PORT, Integer.toString(port));
                    final TaskDesc task = new TaskDesc(echoServiceHost, port, false, fakeToken,
                            0, 1, 0, System.nanoTime(), params, "echo_protocol");
                    taskDescList.add(task);
                }
            }

            if (echoServiceUdpPorts != null) {
                for (int port : echoServiceUdpPorts) {

                    final Map<String, Object> params = new HashMap<>();
                    params.put("concurrency_group", "301");
                    params.put("qos_test_uid", Integer.toString(echoUuid++));
                    params.put("timeout", "5000000000");
                    params.put("qostest", "echo_protocol");
                    params.put(AbstractEchoProtocolTask.RESULT_PROTOCOL, "udp");
                    params.put(AbstractEchoProtocolTask.PARAM_PAYLOAD, "UDP payload!");
                    params.put(AbstractEchoProtocolTask.PARAM_SERVER_ADDRESS, echoServiceHost);
                    params.put(AbstractEchoProtocolTask.PARAM_SERVER_PORT, Integer.toString(port));
                    final TaskDesc task = new TaskDesc(echoServiceHost, port, false, fakeToken,
                            0, 1, 0, System.nanoTime(), params, "echo_protocol");
                    taskDescList.add(task);
                }
            }

        }

        return taskDescList;
    }
}
