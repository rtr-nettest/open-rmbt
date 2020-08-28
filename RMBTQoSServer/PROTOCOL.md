Documentation of the protocol:
==============

1.  QoS test tesult and JSON specification
2.  Database definitions
3.  TestScript
4.  Examples

1.1 QoS test tesult
-------------------

There are 6 different test results so far. The numbers in the brackets equal the numbers as defined by the RTR in the document "Pflichtenheft RTR-Netztest 2" (02.10.2013)  

*   WEBSITE (1.1)
*   HTTP PROXY (1.2)
*   NON TRANSPARENT PROXY (1.3)
*   DNS (1.4)
*   TCP (1.5)
*   UDP (1.5)

  
The test results are sent by the client as a {@link org.json.JSONArray} and will look something like this:  
```json
[{
        "dns_objective_resolver": "8.8.8.8",
        "dns_result_duration": 5890432,
        "dns_result_entries": [{
            "dns_result_address": "www.orf.at.",
            "dns_result_ttl": "12560"
        }],
        "dns_objective_dns_record": "CNAME",
        "qos_test_uid": 23,
        "test_type": "dns",
        "dns_objective_host": "sport.orf.at"
    },
    {
        "dns_objective_resolver": "8.8.8.8",
        "dns_result_duration": 5951473,
        "dns_result_entries": [{
            "dns_result_address": "194.232.104.142",
            "dns_result_ttl": "8065"
        }, {
            "dns_result_address": "194.232.104.140",
            "dns_result_ttl": "8065"
        }, {
            "dns_result_address": "194.232.104.141",
            "dns_result_ttl": "8065"
        }, {
            "dns_result_address": "194.232.104.139",
            "dns_result_ttl": "8065"
        }],
        "dns_objective_dns_record": "A",
        "qos_test_uid": 28,
        "test_type": "dns",
        "dns_objective_host": "orf.at"
    }, {
        "dns_objective_resolver": "8.8.8.8",
        "dns_result_duration": 26583244,
        "dns_result_entries": [{
            "dns_result_priority": "5",
            "dns_result_address": "mail.rtr.at.",
            "dns_result_ttl": "7200"
        }, {
            "dns_result_priority": "10",
            "dns_result_address": "mailbackup.rtr.at.",
            "dns_result_ttl": "7200"
        }],
        "dns_objective_dns_record": "MX",
        "qos_test_uid": 5,
        "test_type": "dns",
        "dns_objective_host": "rtr.at"
    }, {
        "tcp_objective_timeout": 3000,
        "tcp_result_in_response": "HELLO TO 20002",
        "tcp_objective_in_port": 20002,
        "qos_test_uid": 8,
        "test_type": "tcp",
        "tcp_result_in": "OK"
    }, {
        "tcp_objective_timeout": 3000,
        "tcp_result_in_response": "HELLO TO 21450",
        "tcp_objective_in_port": 21450,
        "qos_test_uid": 7,
        "test_type": "tcp",
        "tcp_result_in": "OK"
    }, {
        "http_objective_range": "bytes=0-999",
        "http_result_header": "Date: Wed, 27 Nov 2013 15:54:27 GMT\nServer: Apache\nLocation: https://www.rtr.at/\nContent-Length: 287\nKeep-Alive: timeout=5, max=100\nConnection: Keep-Alive\nContent-Type: text/html; charset=iso-8859-1\n",
        "http_result_length": 287,
        "http_result_status": 301,
        "http_objective_target": "http://www.rtr.at",
        "http_result_duration": 48527394,
        "qos_test_uid": 27,
        "test_type": "http_proxy",
        "http_result_hash": "ae346f806ba6a2e2ab59764122be48f2"
    }, {
        "http_objective_range": "bytes=0-999",
        "http_result_header": "Date: Wed, 27 Nov 2013 15:54:27 GMT\nServer: Apache\nStrict-Transport-Security: max-age=31536000\nLast-Modified: Wed, 27 Nov 2013 14:13:05 GMT\nETag: \"180184-5c7b-4ec293225c521\"\nAccept-Ranges: bytes\nContent-Length: 1000\nCache-Control: max-age=3600\nExpires: Wed, 27 Nov 2013 16:54:27 GMT\nContent-Range: bytes 0-999/23675\nKeep-Alive: timeout=5, max=100\nConnection: Keep-Alive\nContent-Type: text/html; charset=UTF-8\n",
        "http_result_length": 1000,
        "http_result_status": 206,
        "http_objective_target": "https://www.rtr.at",
        "http_result_duration": 78986724,
        "qos_test_uid": 25,
        "test_type": "http_proxy",
        "http_result_hash": "e51c2c8b5bf7ac03920f3f9f6cf7bf33"
    }, {
        "udp_result_incoming_num_packets": 8,
        "udp_objective_incoming_num_packets": 8,
        "udp_objective_incoming_port": 18465,
        "qos_test_uid": 10,
        "test_type": "udp"
    }, {
        "tcp_objective_timeout": 3000,
        "tcp_result_out_response": "PING",
        "tcp_result_out": "OK",
        "tcp_objective_out_port": 31944,
        "qos_test_uid": 9,
        "test_type": "tcp"
    }, {
        "tcp_objective_timeout": 3000,
        "tcp_result_out_response": "PING",
        "tcp_result_out": "OK",
        "tcp_objective_out_port": 22752,
        "qos_test_uid": 13,
        "test_type": "tcp"
    }, {
        "tcp_objective_timeout": 3000,
        "tcp_result_out_response": "PING",
        "tcp_result_out": "OK",
        "tcp_objective_out_port": 38489,
        "qos_test_uid": 15,
        "test_type": "tcp"
    }, {
        "http_objective_range": "bytes=0-999",
        "http_result_header": "Location: http://www.google.at/\nContent-Type: text/html; charset=UTF-8\nDate: Wed, 27 Nov 2013 15:54:27 GMT\nExpires: Fri, 27 Dec 2013 15:54:27 GMT\nCache-Control: public, max-age=2592000\nServer: gws\nContent-Length: 218\nX-XSS-Protection: 1; mode=block\nX-Frame-Options: SAMEORIGIN\n",
        "http_result_length": 216,
        "http_result_status": 301,
        "http_objective_target": "http://google.at",
        "http_result_duration": 115733252,
        "qos_test_uid": 2,
        "test_type": "http_proxy",
        "http_result_hash": "fe6450ad3a7b276a67c79b8ff4adb02d"
    }, {
        "nontransproxy_result_response": "GET / HTTR/7.9",
        "nontransproxy_objective_port": 54514,
        "nontransproxy_objective_request": "GET / HTTR/7.9\n",
        "qos_test_uid": 4,
        "test_type": "non_transparent_proxy"
    }, {
        "nontransproxy_result_response": "GET ",
        "nontransproxy_objective_port": 18246,
        "nontransproxy_objective_request": "GET \n",
        "qos_test_uid": 3,
        "test_type": "non_transparent_proxy"
    }, {
        "udp_objective_outgoing_port": 10020,
        "udp_result_outgoing_num_packets": 6,
        "qos_test_uid": 12,
        "udp_objective_outgoing_num_packets": 6,
        "test_type": "udp"
    }, {
        "udp_objective_outgoing_port": 10034,
        "udp_result_outgoing_num_packets": 14,
        "qos_test_uid": 11,
        "udp_objective_outgoing_num_packets": 14,
        "test_type": "udp"
    }, {
        "website_result_tx_bytes": 1653,
        "website_objective_url": "http://alladin.at",
        "website_result_rx_bytes": 18535,
        "website_result_info": "OK",
        "website_result_duration": 1473035250,
        "website_result_status": 200,
        "website_objective_timeout": "10000",
        "qos_test_uid": 16,
        "test_type": "website"
    }, {
        "website_result_tx_bytes": 0,
        "website_objective_url": "http://fhjrhwqop.com",
        "website_result_rx_bytes": 0,
        "website_result_info": "ERROR",
        "website_result_duration": 26796886,
        "website_result_status": -1,
        "website_objective_timeout": "10000",
        "qos_test_uid": 17,
        "test_type": "website"
    }, {
        "website_result_tx_bytes": 1189,
        "website_objective_url": "http://alladin.at/blabla",
        "website_result_rx_bytes": 2859,
        "website_result_info": "OK",
        "website_result_duration": 1003113078,
        "website_result_status": 404,
        "website_objective_timeout": "10000",
        "qos_test_uid": 22,
        "test_type": "website"
    }
]
```    
  
Basically the test result json keys are build out of 3 components:  
**TESTTYPE**\_**RESULTTYPE**\_**LABEL** where:

*   **TESTTYPE** defines the type of the test (see list below) and can have the following values (they represent the list below in the same order)
    *   website
    *   http
    *   nontransproxy
    *   dns
    *   tcp
    *   udp
*   **RESULTTYPE** can be either
    
    *   objective
    or*   result
    
    objective means that this was a given objective from the control server, whereas _result_ is used for test results
*   **LABEL** is only a label to make the results more human readable

1.2 JSON specification
----------------------

Following these naming conventions the specification has been set up. Here are all JSON (and HSTORE) keys grouped by the test type:

*   **all tests (1.1 - 1.6)** (= these key/value paris can be accessed from all tests)
    *   _start\_time\_ns_ => starting time of the test in ns relative to the speedtest start
    *   _duration\_ns_ => duration of the test in ns
*   **website** (= Website download test, 1.1)
    *   _website\_objective\_url_ => the target URL of this test
    *   _website\_objective\_timeout_ => test timeout
    *   _website\_result\_tx\_bytes_ => bytes transferred during the test
    *   _website\_result\_rx\_bytes_ => bytes received during the test
    *   _website\_result\_duration_ => time needed to download and render the website in nanoseconds
    *   _website\_result\_status_ => the status code (http header), this can have the value -1 if the website was unreachable
    *   _website\_result\_info_ => information about the test procedure; it is generates by the client itself, depending on how the test ended; possible values:
        *   **OK** => test worked as expected
        *   **ERROR** => some error occured (most of the cases: unreachable target)
        *   **TIMEOUT** => timeout exceeded (see parameter: **website\_objective\_timeout**)
*   **http** (= Http proxy test, 1.2)
    *   _http\_objective\_range_ (optional) => defines the string that will be passed to the range request header
    *   _http\_objective\_url_ => target of this test
    *   _http\_result\_header_ => response header
    *   _http\_result\_length_ => content length
    *   _http\_result\_hash_ => checksum of the content, this field also contains information about the final result of the test  
        It may contain the following values:
        *   **any hexadecimal value** - the md5 checksum of the content that was downloaded
        *   **TIMEOUT** - the download timeout has been reached
        *   **ERROR** - another error occured (host not available, connection timeout, etc.)
    *   _http\_result\_status_ => status code in the response header (or -1 if there was no response)
*   **nontransproxy** (= Non transparent proxy test, 1.3)
    *   _nontransproxy\_objective\_request_ => request string for this test
    *   _nontransproxy\_objective\_port_ => test port
    *   _nontransproxy\_objective\_timeout_ => timeout
    *   _nontransproxy\_result_ => enum that represents the result status of this test. Possible values are:
        *   **OK** - the test was successful (=test execution; regardless of the test result)
        *   **TIMEOUT** - the download timeout has been reached
        *   **ERROR** - another error occured (host not available, connection timeout, etc.)
    *   _nontransproxy\_result\_response_ => response (echo from test server)
*   **dns** (= DNS test, 1.4)
    *   _dns\_objective\_host_ => target host of this test
    *   _dns\_objective\_dns\_record_ => dns record to request
    *   _dns\_objective\_resolver_ => dns resolver to be used for this test
    *   _dns\_objective\_timeout_ => dns query timeout in ns
    *   _dns\_result\_duration_ => time needed to complete the test in ns
    *   _dns\_result\_info_ => enum that represents the result status of this test. Possible values are:
        *   **OK** - the test was successful (=test execution; regardless of the test result)
        *   **TIMEOUT** - the dns query timeout has been reached
        *   **ERROR** - another error occured
    *   _dns\_result\_status_ => the query status; the most common values are:
        *   _NOERROR_ => request completed without any error
        *   _NXDOMAIN_ => non existent domain
    *   _dns\_result\_entries\_found_ => number of entries found
    *   _dns\_result\_entries_ => result of this test containing all dns entries that were found (**IMPORTANT:** the value of this object is always an **array**, even if there is only one entry)
        an entry is composed of the following:*   _dns\_result\_ttl_ => the time to live of the dns entry
        *   _dns\_result\_address_ => the address this dns entry points to
        *   _dns\_result\_priority_ => priority, if exists (as in **MX** or **SRV** record)
*   **tcp** (= TCP incoming/outgoing test, 1.5)
    *   _tcp\_objective\_timeout_ => test timeout
    *   _tcp\_objective\_in\_port_ => port number used for the incoming test
    *   _tcp\_result\_in_ => enum; **OK** if incoming test succeeded, **FAILED** if incoming test failed.
    *   _tcp\_result\_in\_response_ => server message received after a connection was established
    *   _tcp\_objective\_out\_port_ => port number used for the outgoing test
    *   _tcp\_result\_out_ => enum; **OK** if outgoing test succeeded, **FAILED** if outgoing test failed.
    *   _tcp\_result\_out\_response_ => response that the client received after sending an message to the test server
*   **udp** (= UDP incoming/outgoing test, 1.5)
    *   _udp\_objective\_timeout_ => test timeout
    *   _udp\_objective\_delay_ => delay between packets (in ns)
    *   _udp\_objective\_out\_port_ => port number used for the outgoing test
    *   _udp\_objective\_out\_num\_packets_ => the number of packets to be sent by the client
    *   _udp\_result\_out\_num\_packets_ => the number of packets received by the test server
    *   _udp\_result\_out\_packet\_loss\_rate_ => outgoing packet loss rate
    *   _udp\_result\_out\_response\_num\_packets_ => responses to outgoing packets
    *   _udp\_objective\_in\_port_ => port number used for the incoming test
    *   _udp\_objective\_in\_num\_packets_ => the number of packets to be sent by the test server
    *   _udp\_result\_in\_num\_packets_ => the number of packets received by the client
    *   _udp\_result\_in\_response\_num\_packets_ => responses to incoming packets received from server
    *   _udp\_result\_in\_packet\_loss\_rate_ => incoming packet loss rate

2\. Database definitions
------------------------

Quality of service tests use four tables and an enum

*   _qos\_test\_objective_ => table (see 2.1); contains all test objectives
*   _qos\_test\_result_ => table (see 2.2); contains all test results
*   _qos\_test\_desc_ => table (see 2.3); language table that contains all test result descriptions
*   _qos\_test\_type\_desc_ => table (see 2.4); contains test types and their references to an entry in qos\_test\_desc, which contains the "global" descriptions
*   _nntest_ => enum; contains values that represent all qos tests:
    *   website
    *   http\_proxy
    *   non\_transparent\_proxy
    *   dns
    *   tcp
    *   udp

### 2.1 qos\_test\_objective

Fields:

*   _uid_ => primary key
*   _test_ type: _nntest_ => the test type
*   _param_ type: _hstore_ => (see 2.1.1) test objectives.
*   _test\_class_ type: _integer_ => (see 2.1.2) test class (currently not in use)
*   _result_ type: _hstore\[\]_ => (see 2.1.3) expected test results.
*   _test\_server_ type: _integer_ => (see 2.1.4) refrences table test\_server; set the test server for the current test.
*   _concurrency\_group_ type: _integer_ => (see 2.1.5) tests that belong to the same group are executed simultaneously. This is also the order of test execution.
*   _test\_desc_ type: _text_ => (see 2.1.6) references the key of an text entry in the **qos\_test\_desc** table. This is the longer and more technical test summary.
*   _test\_summary_ type: _text_ => (see 2.1.7) references the key of an text entry in the **qos\_test\_desc** table. This is the short test summary.

#### 2.1.1 Column _param_

Test parameters are basically json keys without **TESTTYPE** and **RESULTTYPE**, where **RESULTTYPE** must be **objective**. A website test json key: **website\_objective\_url** would be transformed to url. The following parameters are supported (sorted by test type): **WEBSITE (1.1):** Example: **"url"=>"http://netztest.at", "timeout"=>"10000"**

*   _url_ => see website\_objective\_url
*   _timeout_ => see website\_objective\_timeout

**HTTP PROXY (1.2):** Example: **"range"=>"bytes=0-999", "target"=>"https://www.rtr.at", "conn\_timeout"=>"5000", "download\_timeout"=>"15000"**

*   _range_ => see http\_objective\_range
*   _url_ => see http\_objective\_url
*   _conn\_timeout_ => connection timeout in ns (no equivalent json key = not in result table)
*   _download\_timeout_ => download timeout in ns (no equivalent json key = not in result table)

**NON TRANSPARENT PROXY (1.3):** Example: **"port"=>"%RANDOM 50000 55000%", "request"=>"GET / HTTR/7.9"**

*   _port_ => see nontransproxy\_objective\_port
*   _request_ => see nontransproxy\_objective\_request
*   _timeout_ => see nontransproxy\_objective\_timeout

**DNS (1.4):** Example: **"host"=>"rtr.at", "record"=>"MX", "resolver"=>"8.8.8.8"**

*   _host_ => see dns\_objective\_host
*   _record_ => see dns\_objective\_record
*   _resolver_ => see dns\_objective\_resolver **(optional) - if not set the standard system resolver is used**
*   _timeout_ => see dns\_objective\_timeout **(optional) - default: 5 s)**

**TCP (1.5):** Example: **"timeout"=>"3000", "out\_port"=>"%RANDOM 20000 40000%"**

*   _timeout_ => see tcp\_objective\_timeout
*   _out\_port_ => see tcp\_objective\_out\_port
*   _in\_port_ => see tcp\_objective\_in\_port

**UDP (1.5):** Example: **"in\_port"=>"%RANDOM 10000 50000%", "timeout"=>"2500", "in\_num\_packets"=>"%RANDOM 8 12%", "delay"=>"500"**

*   _delay_ => see udp\_objective\_delay
**(optional) - default: 300ms***   _timeout_ => see udp\_objective\_timeout
*   _in\_port_ => see udp\_objective\_in\_port
*   _in\_num\_packets_ => udp\_objective\_in\_num\_packets
*   _out\_port_ => see udp\_objective\_out\_port
*   _out\_num\_packets_ => udp\_objective\_out\_num\_packets

#### 2.1.2 Column _test\_class_

_currently only the value **1** is supported. Tests with a different value won't be executed._

#### 2.1.3 Column _result_

This column contains the expected test results (from now on: ETR) and the behaviour in case of failure and/or success  
The reason that an ETR is an array is the method of evaluation: an ETR can have multiple conditions.  
As a pseudo script the ETR looks like this (the following objective will be used as an example: "port"=>"%RANDOM 5000 25000%", "request"=>"GET / HTTR/7.9")  
_ETR\[0\]:_

*   compare: "nontransproxy\_result\_response" with "nontransproxy\_objective\_request" using the sign: "equals".
*   if test fails display message: "ntp.failure"
*   if test succeeds display message: "ntp.success"

ETR\[1\]:

*   compare: "nontransproxy\_objective\_port" with "10000" using the sign: "lower or equal".
*   if test secceeds display message: "ntp.port\_not\_over\_10000"
*   if test fails display message: "ntp.port\_over\_10000"

This is how the ETR method works. All parameters inside quotes are values that are needed for the evaluation of the test results. They have equivalent hstore keys in which they are stored:  
_ETR\[0\]_ would be stored as: **"operator"=>"eq", "on\_success"=>"ntp.success", "on\_failure"=>"ntp.failure", "nontransproxy\_result\_response"=>"%PARAM nontransproxy\_objective\_request%"**  
_ETR\[1\]_ would be stored as: **"operator"=>"le", "on\_success"=>"ntp.port\_not\_over\_10000", "on\_failure"=>"ntp.port\_over\_10000", "nontransproxy\_object\_port"=>"10000"**  
Summary:  
Each ETR entry needs to contain at least two key-value pairs (one condition operator and at least one event)

*   condition operators are:
    *   key: _operator_ => the operator used in this entry to evaluate test results. An operator may be:
        *   _eq_ => equals
        *   _ne_ => not equals
        *   _lt_ => lower than
        *   _gt_ => greater than
        *   _le_ => lower or equal
        *   _ge_ => greater or equal
    *   key: _evaluate_ => the value needs to be an %EVAL % TestScript command (see 3. TestScript). In this case the variable "result" can hold only 2 values: a boolean _true_ if the test was successful or a boolean _false_ if the test failed.
*   events are:
    *   key: _on\_success_ => represents the key of an text entry in the **qos\_test\_desc** table. Shown if evaluation succeeded. **IMPORTANT**: it always counts as a test success (= green list on device).  
        If this parameter is empty or contains a non existent key then nothing is shown on success.
    *   key: _on\_failure_ => represents the key of an text entry in the **qos\_test\_desc** table. Shown if evaluation failed. **IMPORTANT**: it always counts as a test failure (= red list on device).  
        If this parameter is empty or contains a non existent key then nothing is shown on failure.

All other parameters are optional and can have keys as defined in the json specification (depending on the test)  

#### 2.1.4 Column _test\_server_

References table _test\_server_. It defines the test server for the current test.

#### 2.1.5 Column _concurrency\_group_

It defines the group the test belongs to. Tests that belong to the same group are executed simultaneously. The group order is ascending: 0..n (zero to n).

#### 2.1.6 Column _test\_desc_

References the key of an text entry in the **qos\_test\_desc** table. The text represents the longer (and more technical) description of a test (not to be confused with the test type description and test summary) and is shown in the app regardless of wether the test results are positive or nevative

#### 2.1.7 Column _test\_summary_

References the key of an text entry in the **qos\_test\_desc** table. The text represents the short (and simple) description of a test (not to be confused with the test type description and test description) and is shown in the app regardless of wether the test results are positive or nevative

### 2.2 qos\_test\_result

This table contains all results collected by the client.  
For a detailed description of the test result see 1.1 and 1.2.  
Fields:

*   _uid_ => primary key
*   _result_ type: _hstore_ => contains test results and objectives.
*   _test\_uid_ type: _text_ => references the test table.
*   _qos\_test\_uid_ type: _text_ => references qos\_test\_objective.
*   _success\_count_ type: _text_ => the number of successful test evaluations.
*   _failure\_count_ type: _text_ => the number of failed test evaluations.

### 2.3 qos\_test\_desc

This table contains messages for test results.  
Fields:

*   _uid_ => primary key
*   _desc\_key_ type: _text_ => a text field that is used as a key and is referenced by qos\_test\_result, qos\_test\_objective and qos\_test\_type\_desc.
*   _value_ type: _text_ => this field contains the value.
*   _lang_ type: _text_ => the value for a specific language defined in ISO 639

### 2.4 qos\_test\_type\_desc

Fields:

*   _uid_ => primary key
*   _test_ type: _nntest_ => the test type
*   _test\_desc_ type: _text_ => refrence to qos\_test\_desc.desc\_key, it's the description of this test type category
*   _test\_name_ type: _text_ => refrence to qos\_test\_desc.desc\_key, it's the title of this test type category

3\. TestScript
--------------

To add some dynamic content and to make test descriptions themselves contain results or objectives a pseudo script has been added to the DB:  
The syntax looks as follows: **%COMMAND param1 param2 ...%**  
The supported commands are:

*   _RANDOM param1 \[param2\]_ => generates a random number. If param2 is set then the result is between param1 (inclusive) and param2 (exclusive). If param2 is not set, then the result is between 0 (zero, inclusive) and param1 (exclusive)
*   _PARAM param ..._ => returns the value of a test/objective parameter as defined by the json specification (see 1.2)  
    The reason for multiple parameters are arrays (see 1.2, DNS test, _dns\_result\_entries_). To access the value of _dns\_result\_ttl_ of the first entry the following syntax is used: **%PARAM dns\_result\_entries\[0\] dns\_result\_ttl%**. Only one child can be accessed with one command. The index is zero-based (-> first element = 0, second = 1, etc.)
*   _RANDOMURL prefix number\_of\_random\_digits suffix%_ => generates a random url by using the prefix and suffix as constants and generating a random hexadecimal hash with the length of _number\_of\_random\_digits_. Example: **%RANDOMURL www.unknown 10 .com%** could genrate the following url: **www.unknown4e87a4be91.com**
*   _EVAL javascript_ => this command is put through a JS parser. To reaplce this command with a value (like PARAM or RANDOM) the variable _result_ needs to be set in the javascript code
    
    The _result_ object can contain any value, for example:
    
    *   result = 10;
    *   result = true;
    *   result = "resultString";
    
    Or it may contain an array object, that needs to contain 2 key-value pairs: **type** and **key**. This can be used to replace the complicated "**operator** and **on\_success** and/or **on\_failure**" syntax.  
    This object should be used only for the **evaluate** field. Here is a description of what these parameters can hold and are used for:
    
    *   type: either **"SUCCESS"** or **"FAILURE"** - it tells the script interpreter that the result will be of this type (replaces: on\_success and on\_failure)
    *   key: holds the message key
    
    Code example (what it should look like in the database, all special characters escaped):
```    
\\"evaluate\\"=>\\"%EVAL 
if (tcp\_result\_out=='TIMEOUT') result = {type: 'FAILURE', key: 'tcp.timeout'};
else if (tcp\_result\_out=='ERROR') result = {type: 'FAILURE', key: 'tcp.error'};
else if (tcp\_result\_out=='OK') result = {type: 'SUCCESS', key: 'tcp.success'};
else result=null;%\\"
```
    
All test result values can be used in the js code, the variables have the same names as defined in 1.2  
This js parser has an own QoSTestScript library included to make some evaluations easier. To access functions from this library you need to address the "nn" object. The following functions are available:
    
*   _nn.isEmpty(someArray\[\])_ returns true if the array "someArray" is null or doesn't have any elements
*   _nn.getCount(someArray\[\])_ returns the number of elements of an array
*   _nn.isNull(someObject)_ returns ture if the object "someObject" is null, otherwise it returns false
    

There are rules that must be followed:

*   The interpreter doesn't work recursive: commands inside commands will not work (yet)
*   As said above, only one parameter can be accessed at a time. Some examples:
    *   correct: **%PARAM dns\_objective\_resolver%**
    *   correct: **%PARAM dns\_result\_entries\[0\] dns\_result\_priority%**
    *   incorrect: **%PARAM dns\_objective\_resolver dns\_result\_duration%**
    *   incorrect: **%PARAM dns\_result\_entries\[0\] dns\_result\_priority dns\_result\_ttl%**
*   The **PARAM** command can be also used to format numbers. In this case the following syntax is needed:  
    _PARAM param divisor precision grouping_ (e.g.: **%PARAM duration\_ns 1000000 0 f%** where:
    *   _divisor_ is the divisor that the parameter value will be divided by
    *   _precision_ is the precision of the division
    *   _grouping_ tells the pasres to group numbers in 1000s (like: 1,435,535.42); allowed values are: **t** = true or **f** = false, where false is the default value, therefor this third parameter is not needed
*   If used as objective or expected result parameter (see 2. database usage) the TestScript command must be the only one parameter. Examples:
    *   corret: **"timeout"=>"%RANDOM 10000 20000%"**
    *   incorret: **"timeout"=>"%RANDOM 100 200%00"**
    *   correct: **"tcp\_objective\_in\_port"=>"%PARAM tcp\_objective\_in\_port%"**
    *   incorret: **"http\_objective\_target"=>"http://www.test.abc/item%RANDOM 10000 20000%.html"**

  

4\. Examples:
-------------

### 4.1 DNS

Let's say we want to configure a test that requests the A record of a random **non existent** (this is important!) url. The only one correct response would be an empty result list (no addresses found), and a "NXDOMAIN" (non existent domain) status code.  
A short summary of the objectives and results would be:  

*   A random url with a length of 16 (not including www. and .com)
*   We will use the default timeout of 5 seconds
*   The record we are looking for is A
*   We will also use the standard resolver from our provider

The equivalent database string would be: **"url"=>"%RANDOMURL www. 10 .com%", "record" => "A"**  
The parameters _resolver_ and _timeout_ are not needed as the default values will suit our test scenario (timeout default value = 5000ms, resolver default value = provider's reolver).  
Now a short summary of the objectives (=expected results in this test scenario):  

*   if the response list has more than 0 (zero) entries and the test result info was "OK" the test failed (in case of "ERROR" or "TIMEOUT" the number of entries would be zero too, but this would not mean that this domain doesn't exist).
*   a different approach (and an easier one) would be to compare the test result parameter **dns\_result\_status** which should hold a "NXDOMAIN" string. In this case we know that the test was executed successfully and the result is what we were expecting.
*   as an optional result we could display a messsage in the failed test section when the timeout was reached (**dns\_result\_info** = "TIMEOUT")