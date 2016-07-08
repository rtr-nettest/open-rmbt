/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
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
 *******************************************************************************/
/**
 * This package evaluates and compares QoS test results. It also is used to document the functionality of the {@link at.alladin.rmbt.qos.testscript.TestScriptInterpreter}
 * 
 * <br><br>
 * <h1>Documentation:</h1>
 * <ol>
 * <li>QoS test tesult and JSON specification</li>
 * <li>Database definitions</li>
 * <li>TestScript</li>
 * <li>Examples</li>
 * </ol>
 * 
 * <h2>1.1 QoS test tesult</h2>
 * There are 6 different test results so far. The numbers in the brackets equal the numbers as defined by the RTR in the document "Pflichtenheft Open-RMBT 2" (02.10.2013)<br>
 * <ul>
 * <li>WEBSITE (1.1)</li>
 * <li>HTTP PROXY (1.2)</li>
 * <li>NON TRANSPARENT PROXY (1.3)</li>
 * <li>DNS (1.4)</li>
 * <li>TCP (1.5)</li>
 * <li>UDP (1.5)</li>
 * </ul>
 * 
 * <br>
 * The test results are sent by the client as a {@link org.json.JSONArray} and will look something like this:<br>
 * <code>
 * [{"dns_objective_resolver":"8.8.8.8","dns_result_duration":5890432,"dns_result_entries":[{"dns_result_address":"www.orf.at.","dns_result_ttl":"12560"}],"dns_objective_dns_record":"CNAME","qos_test_uid":23,"test_type":"dns","dns_objective_host":"sport.orf.at"},<br>
 * {"dns_objective_resolver":"8.8.8.8","dns_result_duration":5951473,"dns_result_entries":[{"dns_result_address":"194.232.104.142","dns_result_ttl":"8065"},{"dns_result_address":"194.232.104.140","dns_result_ttl":"8065"},{"dns_result_address":"194.232.104.141","dns_result_ttl":"8065"},{"dns_result_address":"194.232.104.139","dns_result_ttl":"8065"}],"dns_objective_dns_record":"A","qos_test_uid":28,"test_type":"dns","dns_objective_host":"orf.at"},
 * {"dns_objective_resolver":"8.8.8.8","dns_result_duration":26583244,"dns_result_entries":[{"dns_result_priority":"5","dns_result_address":"mail.rtr.at.","dns_result_ttl":"7200"},{"dns_result_priority":"10","dns_result_address":"mailbackup.rtr.at.","dns_result_ttl":"7200"}],"dns_objective_dns_record":"MX","qos_test_uid":5,"test_type":"dns","dns_objective_host":"rtr.at"},
 * {"tcp_objective_timeout":3000,"tcp_result_in_response":"HELLO TO 20002","tcp_objective_in_port":20002,"qos_test_uid":8,"test_type":"tcp","tcp_result_in":"OK"},
 * {"tcp_objective_timeout":3000,"tcp_result_in_response":"HELLO TO 21450","tcp_objective_in_port":21450,"qos_test_uid":7,"test_type":"tcp","tcp_result_in":"OK"},
 * {"http_objective_range":"bytes=0-999","http_result_header":"Date: Wed, 27 Nov 2013 15:54:27 GMT\nServer: Apache\nLocation: https://www.rtr.at/\nContent-Length: 287\nKeep-Alive: timeout=5, max=100\nConnection: Keep-Alive\nContent-Type: text/html; charset=iso-8859-1\n","http_result_length":287,"http_result_status":301,"http_objective_target":"http://www.rtr.at","http_result_duration":48527394,"qos_test_uid":27,"test_type":"http_proxy","http_result_hash":"ae346f806ba6a2e2ab59764122be48f2"},
 * {"http_objective_range":"bytes=0-999","http_result_header":"Date: Wed, 27 Nov 2013 15:54:27 GMT\nServer: Apache\nStrict-Transport-Security: max-age=31536000\nLast-Modified: Wed, 27 Nov 2013 14:13:05 GMT\nETag: \"180184-5c7b-4ec293225c521\"\nAccept-Ranges: bytes\nContent-Length: 1000\nCache-Control: max-age=3600\nExpires: Wed, 27 Nov 2013 16:54:27 GMT\nContent-Range: bytes 0-999/23675\nKeep-Alive: timeout=5, max=100\nConnection: Keep-Alive\nContent-Type: text/html; charset=UTF-8\n","http_result_length":1000,"http_result_status":206,"http_objective_target":"https://www.rtr.at","http_result_duration":78986724,"qos_test_uid":25,"test_type":"http_proxy","http_result_hash":"e51c2c8b5bf7ac03920f3f9f6cf7bf33"},
 * {"udp_result_incoming_num_packets":8,"udp_objective_incoming_num_packets":8,"udp_objective_incoming_port":18465,"qos_test_uid":10,"test_type":"udp"},
 * {"tcp_objective_timeout":3000,"tcp_result_out_response":"PING","tcp_result_out":"OK","tcp_objective_out_port":31944,"qos_test_uid":9,"test_type":"tcp"},
 * {"tcp_objective_timeout":3000,"tcp_result_out_response":"PING","tcp_result_out":"OK","tcp_objective_out_port":22752,"qos_test_uid":13,"test_type":"tcp"},
 * {"tcp_objective_timeout":3000,"tcp_result_out_response":"PING","tcp_result_out":"OK","tcp_objective_out_port":38489,"qos_test_uid":15,"test_type":"tcp"},
 * {"http_objective_range":"bytes=0-999","http_result_header":"Location: http://www.google.at/\nContent-Type: text/html; charset=UTF-8\nDate: Wed, 27 Nov 2013 15:54:27 GMT\nExpires: Fri, 27 Dec 2013 15:54:27 GMT\nCache-Control: public, max-age=2592000\nServer: gws\nContent-Length: 218\nX-XSS-Protection: 1; mode=block\nX-Frame-Options: SAMEORIGIN\n","http_result_length":216,"http_result_status":301,"http_objective_target":"http://google.at","http_result_duration":115733252,"qos_test_uid":2,"test_type":"http_proxy","http_result_hash":"fe6450ad3a7b276a67c79b8ff4adb02d"},
 * {"nontransproxy_result_response":"GET / HTTR/7.9","nontransproxy_objective_port":54514,"nontransproxy_objective_request":"GET / HTTR/7.9\n","qos_test_uid":4,"test_type":"non_transparent_proxy"},
 * {"nontransproxy_result_response":"GET ","nontransproxy_objective_port":18246,"nontransproxy_objective_request":"GET \n","qos_test_uid":3,"test_type":"non_transparent_proxy"},
 * {"udp_objective_outgoing_port":10020,"udp_result_outgoing_num_packets":6,"qos_test_uid":12,"udp_objective_outgoing_num_packets":6,"test_type":"udp"},
 * {"udp_objective_outgoing_port":10034,"udp_result_outgoing_num_packets":14,"qos_test_uid":11,"udp_objective_outgoing_num_packets":14,"test_type":"udp"},
 * {"website_result_tx_bytes":1653,"website_objective_url":"http://alladin.at","website_result_rx_bytes":18535,"website_result_info":"OK","website_result_duration":1473035250,"website_result_status":200,"website_objective_timeout":"10000","qos_test_uid":16,"test_type":"website"},
 * {"website_result_tx_bytes":0,"website_objective_url":"http://fhjrhwqop.com","website_result_rx_bytes":0,"website_result_info":"ERROR","website_result_duration":26796886,"website_result_status":-1,"website_objective_timeout":"10000","qos_test_uid":17,"test_type":"website"},
 * {"website_result_tx_bytes":1189,"website_objective_url":"http://alladin.at/blabla","website_result_rx_bytes":2859,"website_result_info":"OK","website_result_duration":1003113078,"website_result_status":404,"website_objective_timeout":"10000","qos_test_uid":22,"test_type":"website"}]
 * </code>
 * <br><br>
 * Basically the test result json keys are build out of 3 components:<br>
 * <b>TESTTYPE</b>_<b>RESULTTYPE</b>_<b>LABEL</b> where:
 * <ul>
 * <li><b>TESTTYPE</b> defines the type of the test (see list below) and can have the following values (they represent the list below in the same order) 
 * 		<ul>
 * 		<li>website</li>
 * 		<li>http</li>
 * 		<li>nontransproxy</li>
 * 		<li>dns</li>
 * 		<li>tcp</li>
 * 		<li>udp</li> 
 * 		</ul>
 * </li>
 * <li><b>RESULTTYPE</b> can be either <ul><li>objective</li> or <li>result</li></ul>
 * 		</i>objective means that this was a given objective from the control server, whereas <i>result</i> is used for test results
 * </li>
 * <li><b>LABEL</b> is only a label to make the results more human readable</li>
 * </ul>
 * <h2>1.2 JSON specification</h2>
 * Following these naming conventions the specification has been set up. Here are all JSON (and HSTORE) keys grouped by the test type:
 * <ul>
 * <li>
 * 		<b>all tests (1.1 - 1.6)</b> (= these key/value paris can be accessed from all tests)
 * 		<ul>
 * 		<li><i>start_time_ns</i> => starting time of the test in ns relative to the speedtest start</li>
 * 		<li><i>duration_ns</i> => duration of the test in ns</li>
 * 		</ul>
 * </li>
 * <li>
 * 		<b>website</b> (= Website download test, 1.1)
 * 		<ul>
 * 		<li><i>website_objective_url</i> => the target URL of this test</li>
 * 		<li><i>website_objective_timeout</i> => test timeout</li>
 * 		<li><i>website_result_tx_bytes</i> => bytes transferred during the test</li>
 * 		<li><i>website_result_rx_bytes</i> => bytes received during the test</li>
 * 		<li><i>website_result_duration</i> => time needed to download and render the website in nanoseconds</li>
 * 		<li><i>website_result_status</i> => the status code (http header), this can have the value -1 if the website was unreachable</li>
 * 		<li><i>website_result_info</i> => information about the test procedure; it is generates by the client itself, depending on how the test ended; possible values: 
 * 			<ul>
 * 				<li><b>OK</b> => test worked as expected</li>
 * 				<li><b>ERROR</b> => some error occured (most of the cases: unreachable target)</li>
 * 				<li><b>TIMEOUT</b> => timeout exceeded (see parameter: <b>website_objective_timeout</b>)</li>
 * 			</ul>
 * 		</ul>
 * </li>
 * <li>
 * 		<b>http</b> (= Http proxy test, 1.2)
 * 		<ul>
 * 		<li><i>http_objective_range</i> (optional) => defines the string that will be passed to the range request header</li>
 * 		<li><i>http_objective_url</i> => target of this test</li>
 * 		<li><i>http_result_header</i> => response header</li>
 * 		<li><i>http_result_length</i> => content length</li>
 * 		<li><i>http_result_hash</i> => checksum of the content, this field also contains information about the final result of the test<br>It may contain the following values:
 * 			<ul>
 * 				<li><b>any hexadecimal value</b> - the md5 checksum of the content that was downloaded</li>
 * 				<li><b>TIMEOUT</b> - the download timeout has been reached</li>
 * 				<li><b>ERROR</b> - another error occured (host not available, connection timeout, etc.)</li>
 * 			</ul>
 * 		</li>
 * 		<li><i>http_result_status</i> => status code in the response header (or -1 if there was no response)</li>
 * 		</ul>
 * </li>
 * <li>
 * 		<b>nontransproxy</b> (= Non transparent proxy test, 1.3)
 * 		<ul>
 * 		<li><i>nontransproxy_objective_request</i> => request string for this test</li>
 * 		<li><i>nontransproxy_objective_port</i> => test port</li>
 * 		<li><i>nontransproxy_objective_timeout</i> => timeout</li>
 * 		<li><i>nontransproxy_result</i> => enum that represents the result status of this test. Possible values are:
 * 			<ul>
 * 				<li><b>OK</b> - the test was successful (=test execution; regardless of the test result) 
 * 				<li><b>TIMEOUT</b> - the download timeout has been reached</li>
 * 				<li><b>ERROR</b> - another error occured (host not available, connection timeout, etc.)</li>
 * 			</ul>
 * 		</li>
 * 		<li><i>nontransproxy_result_response</i> => response (echo from test server)</li>
 * 		</ul>
 * </li>
 * <li>
 * 		<b>dns</b> (= DNS test, 1.4)
 * 		<ul>
 * 		<li><i>dns_objective_host</i> => target host of this test</li>
 * 		<li><i>dns_objective_dns_record</i> => dns record to request</li>
 * 		<li><i>dns_objective_resolver</i> => dns resolver to be used for this test</li>
 * 		<li><i>dns_objective_timeout</i> => dns query timeout in ns</li>
 * 		<li><i>dns_result_duration</i> => time needed to complete the test in ns</li>
 *  	<li><i>dns_result_info</i> => enum that represents the result status of this test. Possible values are:
 * 			<ul>
 * 				<li><b>OK</b> - the test was successful (=test execution; regardless of the test result) 
 * 				<li><b>TIMEOUT</b> - the dns query timeout has been reached</li>
 * 				<li><b>ERROR</b> - another error occured</li>
 * 			</ul>
 * 		</li>
 * 		<li><i>dns_result_status</i> => the query status; the most common values are: 
 * 			<ul>
 * 				<li><i>NOERROR</i> => request completed without any error</li>
 * 				<li><i>NXDOMAIN</i> => non existent domain</li>
 * 			</ul>
 * 		</li>
 * 		<li><i>dns_result_entries_found</i> => number of entries found</li>
 * 		<li><i>dns_result_entries</i> => result of this test containing all dns entries that were found (<b>IMPORTANT:</b> the value of this object is always an <b>array</b>, even if there is only one entry)
 * 			<ul>an entry is composed of the following:
 * 				<li><i>dns_result_ttl</i> => the time to live of the dns entry</li>
 * 				<li><i>dns_result_address</i> => the address this dns entry points to</li>
 * 				<li><i>dns_result_priority</i> => priority, if exists (as in <b>MX</b> or <b>SRV</b> record)</li>
 * 			</ul>
 * 		</li>
 * 		</ul>
 * </li>
 * <li>
 * 		<b>tcp</b> (= TCP incoming/outgoing test, 1.5)  
 * 		<ul>
 * 		<li><i>tcp_objective_timeout</i> => test timeout</li>
 *  	<li><i>tcp_objective_in_port</i> => port number used for the incoming test</li>
 *  	<li><i>tcp_result_in</i> => enum; <b>OK</b> if incoming test succeeded, <b>FAILED</b> if incoming test failed.
 * 		<li><i>tcp_result_in_response</i> => server message received after a connection was established</li>
 * 		<li><i>tcp_objective_out_port</i> => port number used for the outgoing test</li>
 * 		<li><i>tcp_result_out</i> => enum; <b>OK</b> if outgoing test succeeded, <b>FAILED</b> if outgoing test failed. 		
 * 		<li><i>tcp_result_out_response</i> => response that the client received after sending an message to the test server</li>
 * 		</ul>
 * </li>
 * <li>
 * 		<b>udp</b> (= UDP incoming/outgoing test, 1.5)
 * 		<ul>
 * 		<li><i>udp_objective_timeout</i> => test timeout</li>
 * 		<li><i>udp_objective_delay</i> => delay between packets (in ns)</li>
 *  	<li><i>udp_objective_out_port</i> => port number used for the outgoing test</li>
 *  	<li><i>udp_objective_out_num_packets</i> => the number of packets to be sent by the client</li>
 *  	<li><i>udp_result_out_num_packets</i> => the number of packets received by the test server</li>
 *  	<li><i>udp_result_out_packet_loss_rate</i> => outgoing packet loss rate</li>
 *  	<li><i>udp_result_out_response_num_packets</i> => responses to outgoing packets</li>
 *  	<li><i>udp_objective_in_port</i> => port number used for the incoming test</li>
 *  	<li><i>udp_objective_in_num_packets</i> => the number of packets to be sent by the test server</li>
 *  	<li><i>udp_result_in_num_packets</i> => the number of packets received by the client</li>
 *  	<li><i>udp_result_in_response_num_packets</i> => responses to incoming packets received from server</li>
 *  	<li><i>udp_result_in_packet_loss_rate</i> => incoming packet loss rate</li>
 * 		</ul>
 * </li>
 * </ul>
 * 
 * 
 * <h2>2. Database definitions</h2>
 * Quality of service tests use four tables and an enum
 * <ul>
 * 		<li><i>qos_test_objective</i> => table (see 2.1); contains all test objectives</li>
 * 		<li><i>qos_test_result</i> => table (see 2.2); contains all test results</li>
 * 		<li><i>qos_test_desc</i> => table (see 2.3); language table that contains all test result descriptions</li>
 * 		<li><i>qos_test_type_desc</i> => table (see 2.4); contains test types and their references to an entry in qos_test_desc, which contains the "global" descriptions
 * 		<li><i>nntest</i> => enum; contains values that represent all qos tests:
 * 			<ul>
 * 				<li>website</li>
 * 				<li>http_proxy</li>
 * 				<li>non_transparent_proxy</li>
 * 				<li>dns</li>
 * 				<li>tcp</li>
 * 				<li>udp</li>
 * 			</ul>
 * 		</li>
 * </ul>
 * <h3>2.1 qos_test_objective</h3>
 * Fields:
 * <ul>
 * 		<li><i>uid</i> => primary key</li>
 * 		<li><i>test</i> type: <i>nntest</i> => the test type</li>
 * 		<li><i>param</i> type: <i>hstore</i> => (see 2.1.1) test objectives.</li>
 * 		<li><i>test_class</i> type: <i>integer</i> => (see 2.1.2) test class (currently not in use)</li>
 * 		<li><i>result</i> type: <i>hstore[]</i> => (see 2.1.3) expected test results.</li>
 * 		<li><i>test_server</i> type: <i>integer</i> => (see 2.1.4) refrences table test_server; set the test server for the current test.</li>
 * 		<li><i>concurrency_group</i> type: <i>integer</i> => (see 2.1.5) tests that belong to the same group are executed simultaneously. This is also the order of test execution.</li>
 * 		<li><i>test_desc</i> type: <i>text</i> => (see 2.1.6) references the key of an text entry in the <b>qos_test_desc</b> table. This is the longer and more technical test summary.</li>
 * 		<li><i>test_summary</i> type: <i>text</i> => (see 2.1.7) references the key of an text entry in the <b>qos_test_desc</b> table. This is the short test summary.</li>
 * </ul>
 * <h4>2.1.1 Column <i>param</i></h4>
 * 		Test parameters are basically json keys without <b>TESTTYPE</b> and <b>RESULTTYPE</b>, where <b>RESULTTYPE</b> must be <b>objective</b>. A website test json key: <b>website_objective_url</b> would be transformed to </b>url</b>. The following parameters are supported (sorted by test type):
 * 		<b>WEBSITE (1.1):</b>
 * 		Example: <b>"url"=>"http://alladin.at", "timeout"=>"10000"</b>
 * 		<ul>
 * 			<li><i>url</i> => see website_objective_url</li>
 * 			<li><i>timeout</i> => see website_objective_timeout</li>
 * 		</ul>
 * 		<b>HTTP PROXY (1.2):</b>
 *		Example: <b>"range"=>"bytes=0-999", "target"=>"https://www.rtr.at", "conn_timeout"=>"5000", "download_timeout"=>"15000"</b>
 *		<ul>
 * 			<li><i>range</i> => see http_objective_range</li>
 * 			<li><i>url</i> => see http_objective_url</li>
 * 			<li><i>conn_timeout</i> => connection timeout in ns (no equivalent json key = not in result table)</li>
 * 			<li><i>download_timeout</i> => download timeout in ns (no equivalent json key = not in result table)</li>
 * 		</ul>
 * 		<b>NON TRANSPARENT PROXY (1.3):</b>
 *		Example: <b>"port"=>"%RANDOM 50000 55000%", "request"=>"GET / HTTR/7.9"</b>
 *		<ul>
 * 			<li><i>port</i> => see nontransproxy_objective_port</li>
 * 			<li><i>request</i> => see nontransproxy_objective_request</li>
 * 			<li><i>timeout</i> => see nontransproxy_objective_timeout</li>
 * 		</ul>  
 * 		<b>DNS (1.4):</b>
 *		Example: <b>"host"=>"rtr.at", "record"=>"MX", "resolver"=>"8.8.8.8"</b>
 *		<ul>
 * 			<li><i>host</i> => see dns_objective_host</li>
 * 			<li><i>record</i> => see dns_objective_record</li>
 * 			<li><i>resolver</i> => see dns_objective_resolver <b>(optional) - if not set the standard system resolver is used</b></li>
 * 			<li><i>timeout</i> => see dns_objective_timeout <b>(optional) - default: 5 s)</b></li>
 * 		</ul>  
 * 		<b>TCP (1.5):</b>
 *		Example: <b>"timeout"=>"3000", "out_port"=>"%RANDOM 20000 40000%"</b>
 *		<ul>
 * 			<li><i>timeout</i> => see tcp_objective_timeout</li>
 * 			<li><i>out_port</i> => see tcp_objective_out_port</li>
 * 			<li><i>in_port</i> => see tcp_objective_in_port</li>
 * 		</ul>  
 * 		<b>UDP (1.5):</b>
 *		Example: <B>"in_port"=>"%RANDOM 10000 50000%", "timeout"=>"2500", "in_num_packets"=>"%RANDOM 8 12%", "delay"=>"500"</b>
 *		<ul>
 *			<li><i>delay</i> => see udp_objective_delay</li><b>(optional) - default: 300ms</b>
 * 			<li><i>timeout</i> => see udp_objective_timeout</li>
 * 			<li><i>in_port</i> => see udp_objective_in_port</li>
 * 			<li><i>in_num_packets</i> => udp_objective_in_num_packets</li>
 * 			<li><i>out_port</i> => see udp_objective_out_port</li>
 * 			<li><i>out_num_packets</i> => udp_objective_out_num_packets</li>
 * 		</ul>  
 * <h4>2.1.2 Column <i>test_class</i></h4>
 *		<i>currently only the value <b>1</b> is supported. Tests with a different value won't be executed.</i>
 * <h4>2.1.3 Column <i>result</i></h4>
 * 		This column contains the expected test results (from now on: ETR) and the behaviour in case of failure and/or success<br>
 * 		The reason that an ETR is an array is the method of evaluation: an ETR can have multiple conditions.<br>
 * 		As a pseudo script the ETR looks like this (the following objective will be used as an example: "port"=>"%RANDOM 5000 25000%", "request"=>"GET / HTTR/7.9")<br>
 * 		<i>ETR[0]:</i>
 * 		<ul> 
 * 			<li>compare: "nontransproxy_result_response" with "nontransproxy_objective_request" using the sign: "equals".</li> 
 * 			<li>if test fails display message: "ntp.failure"</li>
 * 			<li>if test succeeds display message: "ntp.success"</li>
 * 		</ul>
 * 		</i>ETR[1]:</i>
 * 		<ul> 
 * 			<li>compare: "nontransproxy_objective_port" with "10000" using the sign: "lower or equal".</li> 
 * 			<li>if test secceeds display message: "ntp.port_not_over_10000"</li>
 * 			<li>if test fails display message: "ntp.port_over_10000"</li>
 * 		</ul>
 * 		This is how the ETR method works. All parameters inside quotes are values that are needed for the evaluation of the test results. They have equivalent hstore keys in which they are stored:
 * 		<br><i>ETR[0]</i> would be stored as: <b>"operator"=>"eq", "on_success"=>"ntp.success", "on_failure"=>"ntp.failure", "nontransproxy_result_response"=>"%PARAM nontransproxy_objective_request%"</b>
 * 		<br><i>ETR[1]</i> would be stored as: <b>"operator"=>"le", "on_success"=>"ntp.port_not_over_10000", "on_failure"=>"ntp.port_over_10000", "nontransproxy_object_port"=>"10000"</b>
 *		<br>Summary:<br>Each ETR entry needs to contain at least two key-value pairs (one condition operator and at least one event)
 *		<ul>
 *			<li>condition operators are:
 *			<ul>
 * 				<li>key: <i>operator</i> => the operator used in this entry to evaluate test results. An operator may be:
 * 					<ul>
 * 						<li><i>eq</i> => equals</li>
 * 						<li><i>ne</i> => not equals</li>
 * 						<li><i>lt</i> => lower than</li>
 * 						<li><i>gt</i> => greater than</li>
 * 						<li><i>le</i> => lower or equal</li>
 * 						<li><i>ge</i> => greater or equal</li>
 * 					</ul>
 * 				</li>
 * 				<li>key: <i>evaluate</i> => the value needs to be an %EVAL % TestScript command (see 3. TestScript). In this case the variable "result" can hold only 2 values: a boolean <i>true</i> if the test was successful or a boolean <i>false</i> if the test failed.</li>
 * 			</ul>
 *			</li>
 * 			
 * 			<li>events are:
 * 			<ul>
 *				<li>key: <i>on_success</i> => represents the key of an text entry in the <b>qos_test_desc</b> table. Shown if evaluation succeeded. <b>IMPORTANT</b>: it always counts as a test success (= green list on device).<br> If this parameter is empty or contains a non existent key then nothing is shown on success.</li>
 * 				<li>key: <i>on_failure</i> => represents the key of an text entry in the <b>qos_test_desc</b> table. Shown if evaluation failed. <b>IMPORTANT</b>: it always counts as a test failure (= red list on device).<br> If this parameter is empty or contains a non existent key then nothing is shown on failure.</li>
 * 			</ul> 
 * 			</li>
 * 		</ul>
 * 		All other parameters are optional and can have keys as defined in the json specification (depending on the test)<br>
 * <h4>2.1.4 Column <i>test_server</i></h4>
 * 		References table <i>test_server</i>. It defines the test server for the current test.
 * <h4>2.1.5 Column <i>concurrency_group</i></h4>
 * 		It defines the group the test belongs to. Tests that belong to the same group are executed simultaneously. The group order is ascending: 0..n (zero to n).
 * <h4>2.1.6 Column <i>test_desc</i></h4>
 * 		References the key of an text entry in the <b>qos_test_desc</b> table. The text represents the longer (and more technical) description of a test (not to be confused with the test type description and test summary) and is shown in the app regardless of wether the test results are positive or nevative
 * <h4>2.1.7 Column <i>test_summary</i></h4>
 * 		References the key of an text entry in the <b>qos_test_desc</b> table. The text represents the short (and simple) description of a test (not to be confused with the test type description and test description) and is shown in the app regardless of wether the test results are positive or nevative
 * 
 * <h3>2.2 qos_test_result</h3>
 * 		This table contains all results collected by the client.<br>For a detailed description of the test result see 1.1 and 1.2.<br>
 * 		Fields:
 * 		<ul>
 * 			<li><i>uid</i> => primary key</li>
 * 			<li><i>result</i> type: <i>hstore</i> => contains test results and objectives.</li>
 * 			<li><i>test_uid</i> type: <i>text</i> => references the test table.</li>
 * 			<li><i>qos_test_uid</i> type: <i>text</i> => references qos_test_objective.</li>
 * 			<li><i>success_count</i> type: <i>text</i> => the number of successful test evaluations.</li>
 * 			<li><i>failure_count</i> type: <i>text</i> => the number of failed test evaluations.</li>
 *		</ul>
 * <h3>2.3 qos_test_desc</h3>
 * 		This table contains messages for test results. <br>
 * 		Fields:
 * 		<ul>
 * 			<li><i>uid</i> => primary key</li>
 * 			<li><i>desc_key</i> type: <i>text</i> => a text field that is used as a key and is referenced by qos_test_result, qos_test_objective and qos_test_type_desc.</li>
 * 			<li><i>value</i> type: <i>text</i> => this field contains the value.</li>
 * 			<li><i>lang</i> type: <i>text</i> => the value for a specific language defined in ISO 639</li>
 * 		</ul> 
 * <h3>2.4 qos_test_type_desc</h3>
 * 		Fields:
 * 		<ul>
 * 			<li><i>uid</i> => primary key</li>
 * 			<li><i>test</i> type: <i>nntest</i> => the test type</li>
 * 			<li><i>test_desc</i> type: <i>text</i> => refrence to qos_test_desc.desc_key, it's the description of this test type category</li>
 * 			<li><i>test_name</i> type: <i>text</i> => refrence to qos_test_desc.desc_key, it's the title of this test type category</li>
 * 		</ul> 
 * <h2>3. TestScript</h2>
 * To add some dynamic content and to make test descriptions themselves contain results or objectives a pseudo script has been added to the DB:<br>
 * The syntax looks as follows: <b>%COMMAND param1 param2 ...%</b><br>
 * The supported commands are:
 * <ul>
 * 		<li><i>RANDOM param1 [param2]</i> => generates a random number. If param2 is set then the result is between param1 (inclusive) and param2 (exclusive). If param2 is not set, then the result is between 0 (zero, inclusive) and param1 (exclusive)</li>
 * 		<li><i>PARAM param ...</i> => returns the value of a test/objective parameter as defined by the json specification (see 1.2)<br>
 * 			The reason for multiple parameters are arrays (see 1.2, DNS test, <i>dns_result_entries</i>). To access the value of <i>dns_result_ttl</i> of the first entry the following syntax is used: <b>%PARAM dns_result_entries[0] dns_result_ttl%</b>. Only one child can be accessed with one command. The index is zero-based (-> first element = 0, second = 1, etc.)</li>
 * 		<li><i>RANDOMURL prefix number_of_random_digits suffix%</i> => generates a random url by using the prefix and suffix as constants and generating a random hexadecimal hash with the length of <i>number_of_random_digits</i>. Example: <b>%RANDOMURL www.unknown 10 .com%</b> could genrate the following url: <b>www.unknown4e87a4be91.com</b></li>
 * 		<li><i>EVAL javascript</i> => this command is put through a JS parser. To reaplce this command with a value (like PARAM or RANDOM) the variable <i>result</i> needs to be set in the javascript code
 * 			<p>
 * 			The <i>result</i> object can contain any value, for example:
 * 			<ul>
 * 				<li>result = 10;</li>
 * 				<li>result = true;</li>
 * 				<li>result = "resultString";</li>
 * 			</ul>
 * 			Or it may contain an array object, that needs to contain 2 key-value pairs: <b>type</b> and <b>key</b>. This can be used to replace the complicated "<b>operator</b> and <b>on_success</b> and/or <b>on_failure</b>" syntax.
 * 			<br>This object should be used only for the <b>evaluate</b> field. Here is a description of what these parameters can hold and are used for:
 * 			<ul>
 * 				<li>type: either <b>"SUCCESS"</b> or <b>"FAILURE"</b> - it tells the script interpreter that the result will be of this type (replaces: on_success and on_failure)</li>
 * 				<li>key: holds the message key</li>
 * 			</ul>
 * 			Code example (what it should look like in the database, all special characters escaped):
 * 			<pre>
 * 			\"evaluate\"=>\"%EVAL 
 * 				if (tcp_result_out=='TIMEOUT') result = {type: 'FAILURE', key: 'tcp.timeout'};
 * 				else if (tcp_result_out=='ERROR') result = {type: 'FAILURE', key: 'tcp.error'};
 * 				else if (tcp_result_out=='OK') result = {type: 'SUCCESS', key: 'tcp.success'};
 * 				else result=null;%\"
 * 			</pre>
 * 			</p>
 * 			<p>
 * 			All test result values can be used in the js code, the variables have the same names as defined in 1.2<br>
 * 			This js parser has an own QoSTestScript library included to make some evaluations easier. To access functions from this library you need to address the "nn" object. The following functions are available:
 * 			<ul>
 * 				<li><i>nn.isEmpty(someArray[])</i> returns true if the array "someArray" is null or doesn't have any elements</li>
 * 				<li><i>nn.getCount(someArray[])</i> returns the number of elements of an array</li>
 * 				<li><i>nn.isNull(someObject)</i> returns ture if the object "someObject" is null, otherwise it returns false</li>
 *			</ul>
 * 			</p>
 * 		</li>
 * </ul>
 * There are rules that must be followed:
 * <ul>
 * 		<li>The interpreter doesn't work recursive: commands inside commands will not work (yet)</li>
 * 		<li>As said above, only one parameter can be accessed at a time. Some examples: 
 * 			<ul>
 * 				<li>correct: <b>%PARAM dns_objective_resolver%</b></li>
 * 				<li>correct: <b>%PARAM dns_result_entries[0] dns_result_priority%</b></li>
 * 				<li>incorrect: <b>%PARAM dns_objective_resolver dns_result_duration%</b></li>
 * 				<li>incorrect: <b>%PARAM dns_result_entries[0] dns_result_priority dns_result_ttl%</b></li> 	
 *  		</ul>
 *  	</li>
 *  	<li>The <b>PARAM</b> command can be also used to format numbers. In this case the following syntax is needed:<br>
 *  		<i>PARAM param divisor precision grouping</i> (e.g.: <b>%PARAM duration_ns 1000000 0 f%</b> where:
 *  		<ul>
 *  			<li><i>divisor</i> is the divisor that the parameter value will be divided by</li>
 *  			<li><i>precision</i> is the precision of the division</li>
 *  			<li><i>grouping</i> tells the pasres to group numbers in 1000s (like: 1,435,535.42); allowed values are: <b>t</b> = true or <b>f</b> = false, where false is the default value, therefor this third parameter is not needed</li>
 * 			</ul>
 *  	</li>
 *  	<li>If used as objective or expected result parameter (see 2. database usage) the TestScript command must be the only one parameter. Examples:
 *  		<ul>
 *  			<li>corret: <b>"timeout"=>"%RANDOM 10000 20000%"</b></li>
 *  			<li>incorret: <b>"timeout"=>"%RANDOM 100 200%00"</b></li>
 *  			<li>correct: <b>"tcp_objective_in_port"=>"%PARAM tcp_objective_in_port%"</b></li>
 *  			<li>incorret: <b>"http_objective_target"=>"http://www.test.abc/item%RANDOM 10000 20000%.html"</b></li>
 *  		</ul>
 *  	</li>
 * </ul>
 * <br>
 * <h2>4. Examples:</h2>
 * <h3>4.1 DNS</h3>
 * Let's say we want to configure a test that requests the A record of a random <b>non existent</b> (this is important!) url. The only one correct response would be an empty result list (no addresses found), and a "NXDOMAIN" (non existent domain) status code.<br>
 * A short summary of the objectives and results would be:<br>
 * <ul>
 * 		<li>A random url with a length of 16 (not including www. and .com)</li>
 * 		<li>We will use the default timeout of 5 seconds</li>
 * 		<li>The record we are looking for is A</li>
 * 		<li>We will also use the standard resolver from our provider</li>
 * </ul>
 * The equivalent database string would be: <b>"url"=>"%RANDOMURL www. 10 .com%", "record" => "A"</b>
 * <br>
 * The parameters <i>resolver</i> and <i>timeout</i> are not needed as the default values will suit our test scenario (timeout default value = 5000ms, resolver default value = provider's reolver). 
 * <br>
 * Now a short summary of the objectives (=expected results in this test scenario):<br>
 * <ul>
 * 		<li>if the response list has more than 0 (zero) entries and the test result info was "OK" the test failed
 * 			(in case of "ERROR" or "TIMEOUT" the number of entries would be zero too, but this would not mean that this domain doesn't exist).</li>
 * 		<li>a different approach (and an easier one) would be to compare the test result parameter <b>dns_result_status</b> which should hold a "NXDOMAIN" string. In this case we know that the test was executed successfully and the result is what we were expecting.</li> 
 * 		<li>as an optional result we could display a messsage in the failed test section when the timeout was reached (<b>dns_result_info</b> = "TIMEOUT")</li>
 * </ul>
 * Database string in the first case would be: <b>{"\"operator\"=>\"eq\", \"on_failure\"=>\"dns.unknowndomain.failure\", \"on_success\"=>\"dns.unknowndomain.success\", \"dns_result_entries_found\"=>\"0\", \"dns_result_info\"=>\"OK\""}</b><br>
 * <b>dns_result_entries_found</b> and <b>dns_result_info</b> are beeing compared to some values (here: "0" and "OK"). The comparison uses an equals sign ("operator" = "eq"). If both comparisons are true then the on_success string will be returned by the control server, else (that means that at least one condition is false) the on_failure string will be returned.<br><br>
 * The second case would produce the following entry: <b>{"\"operator\"=>\"ne\", \"on_failure\"=>\"dns.unknowndomain.failure\", \"on_success\"=>\"dns.unknowndomain.success\", \"dns_result_status\"=>\"NXDOMAIN\""}</b><br>
 * We are comparing <b>dns_result_status</b> with "NXDOMAIN". That's it.<br><br>
 * Optional we would like to display an error message if the timeout has been exceeded during the test. This would be database string: <b>{"\"operator\"=>\"ne\", \"on_failure\"=>\"test.timeout.exceeded\", \"dns_result_info\"=>\"TIMEOUT\""}</b><br>
 * Maybe you noticed this special case. As mentioned in 2.1.3 on_success not only means that the comparison was successful but it also signalizes the device to show this message in the "green list" (= test was successful). If we want this message to be displayed in the "red list" (= test failed) we need to set a different condition. This is solved by using the "ne" (= not equals) operator:<br>
 * If dns_result_info does not hold the value: "TIMEOUT" (dns_result_info != "TIMEOUT) on_success is beeing used, else on_failure will be returned. The other new thing in this example is the missing "on_success". As also mentioned in 2.1.3 a result message can be omitted if the corresponding on_success or on_failure is missing.<br><br>
 * In the last step we need to put these conditions together. Let's take the second option (because of its simplicity) and the timeout-condition:<br><b>
 * {"\"operator\"=>\"eq\", \"on_failure\"=>\"dns.unknowndomain.failure\", \"on_success\"=>\"dns.unknowndomain.success\", \"dns_result_status\"=>\"NXDOMAIN\"","\"operator\"=>\"ne\", \"on_failure\"=>\"test.timeout.exceeded\", \"dns_result_info\"=>\"TIMEOUT\""}<br> 
 */
package at.alladin.rmbt.qos;