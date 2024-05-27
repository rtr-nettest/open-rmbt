= RTR Multithreaded Broadband Test (RMBT): Specification
Christoph Sölder; Leonhard Wimmer; Dietmar Zlabinger; Ulrich Latzenhofer; Ursula Prinzl; Philipp Sandner; Lukasz Budryk; Ulrich Liener; Thomas Schreiber
v1.0.0, 2017-06-22
:doctype: book
:encoding: utf-8
:lang: en
:stem:
:mathematical-format: svg
:toc: left
:sectnums:
:chapter-label:
:pdf-page-size: A4
:phase_1: Phase 1: Initialization
:phase_2: Phase 2: Downlink pre-test
:phase_3: Phase 3: Latency test
:phase_4: Phase 4: Downlink RMBT
:phase_5: Phase 5: Uplink pre-test
:phase_6: Phase 6: Uplink RMBT
:phase_7: Phase 7: Finalization

Version {revnumber} from {revdate}

Rundfunk und Telekom Regulierungs-GmbH +
Austrian Regulatory Authority for Broadcasting and Telecommunications (RTR) +
Mariahilfer Straße 77–79, 1060 Wien, Austria, T +43 1 580580 +
https://www.rtr.at/, rtr@rtr.at

[preface]
== Preface
=== Scope
The aim of this document is to specify an end-user oriented broadband test,
which measures the download and upload data rate as well as the latency of the Internet connection.
The document provides a brief overview of the test system and a detailed description of the test procedure.

This document only covers the parts of the system which are relevant for the said measurements.
Only the relevant parts of the Control Server are documented.
Other servers and other functions of the Control Server are out of scope of this document.

Sections <<General requirements>> and <<System components>> provide general information.
With sections <<Global constants>> and <<Test procedure>> details of the RMBT are given.
Information regarding measurement data are provided in section <<Measurement data>>.
The communication protocol is specified in section <<Communication protocol>>.
The section <<Communication examples>> gives examples of the used protocol.



=== Identification
The test specified in this document is identified (at present) as _RTR Multithreaded Broadband Test_ (abbr. _RMBT_).

=== Open source
The implementation of this specification for the RTR-Netztest is provided as open-source software. Please see https://www.netztest.at/ for details.

== General requirements
The test delivers an accurate measurement of the maximum bandwidth available over a given Internet connection.
This is achieved by transferring multiple parallel data streams over separate TCP connections within a predefined amount of time.

The transferred data consist of randomly generated data with high entropy.
It is not expected that the (pseudo) random number generator meets cryptographic requirements.
However it must effectively prohibit data compression during the transmission.

In order to increase the probability that the test can be performed even within networks protected by firewalls and proxy servers,
the data is transferred over a TLSfootnote:[Dierks, T. and E. Rescorla, "`The Transport Layer Security (TLS) Protocol Version 1.2`", RFC 5246, DOI 10.17487/RFC5246, August 2008, <http://www.rfc-editor.org/info/rfc5246>.] connection.

The test may make use of HTTPS/Websocketfootnote:[Fette, I. and A. Melnikov, "`The WebSocket Protocol`", RFC 6455, DOI 10.17487/RFC6455, December 2011, <http://www.rfc-editor.org/info/rfc6455>.]
connections.

== System components
The RMBT system is composed as follows:

.System overview
[plantuml, format=svg]
....
actor Client as cl #LightBlue
component [RMBT Server] as rs #LightSalmon
component [Control Server] as cs #Khaki
database "Database" as db

cs <-- cl : 1. Test request
cs --> cl : 2. Test parameters
cl <--> rs : 3. Measurement
cs <-- cl : 4. Test results
cs <-> db : Read/Write
....

By default, data is transferred between client and server over the TCP port 443 in order to avoid interference with firewalls as much as possible.
The ports for communication and data transfers between the different servers themselves are configurable. 

In general, the format of the transferred data is JSONfootnote:[Bray, T., Ed., "`The JavaScript Object Notation (JSON) Data Interchange Format`", RFC 7159, DOI 10.17487/RFC7159, March 2014, <http://www.rfc-editor.org/info/rfc7159>.].
Only the RMBT Server uses a different protocol to keep the communication simple and therefore the workload of the client and the server at a minimum.

== Global constants
The following constants are used throughout the specification:


[options="header,autowidth",cols="2*,>,1"]
|===================================================
|Constant |Description |Default |Unit

|[[const_n_d]]latexmath:[n_d] |nominal number of parallel TCP connections +
used for *downlink* measurement (latexmath:[n_d \geqslant 1])|3 |(quantity)
|[[const_n_u]]latexmath:[n_u] |nominal number of parallel TCP connections +
used for *uplink* measurement (latexmath:[n_u \geqslant 1]) |3 |(quantity)
|[[const_s_min]]latexmath:[s_{min}] |minimal size of a data chunk sent during the test; |4096 |byte
|[[const_s_max]]latexmath:[s_{max}] |maximum size of a data chunk sent during the test; |4194304  |byte
|[[const_s]]_s_ |size of data chunk sent during the test (latexmath:[s_{min} \leqslant s \leqslant s_{max}]); |4096 |byte
|[[const_d]]_d_ |nominal duration of pre-tests |2 |second
|[[const_t]]_t_ |nominal duration of uplink and downlink measurements |7 |second
|[[const_p]]_p_ |number of "`pings`" during latency test |10 |(quantity)
|[[const_r]]_r_ |minimum interval between two consecutive statistical reports from the RMBT Server to the client when processing PUT (cf. <<PUT>>) |0.001 |second
|[[const_w1]]latexmath:[w_1] |wait constant 1 for <<phase_6>>| 0.1 |second
|[[const_w2]]latexmath:[w_2] |wait constant 2 for <<phase_6>>| 3 |second
|[[const_w3]]latexmath:[w_3] |wait constant 3 for <<phase_6>>| 1 |second
|===================================================

== Test procedure
The test follows the procedure outlined below.
The test consists of seven phases which are carried out one after each other, i.e., phase _m_ starts after phase _m_ – 1 has finished.
That means that the phases do not overlap.

[[phase_1]]
=== {phase_1}
The RMBT Client tries to connect to the Control Server on the TCP port 443 using TLS.
In order to pass through certain firewalls, which might block unencrypted data transmissions, this might be necessary.

The server authentication key has a short length in order to reduce delays due to the TLS handshake.
TLS-security is not an objective.

After establishing a proper connection, client and server exchange the information, which is required for running the test.

The client sends a test request to the Control Server.
The Control Server determines the RMBT Server to be used.
It then generates a token consisting of the following components:

* a unique test ID (a UUID in accordance withfootnote:[Leach, P., Mealling, M., and R. Salz, "`A Universally Unique IDentifier (UUID) URN Namespace`", RFC 4122, DOI 10.17487/RFC4122, July 2005, <http://www.rfc-editor.org/info/rfc4122>.]);

* the time at which the measurement is allowed to start (a string representing Unix time);

* the Base64-encoded HMAC-SHA-1 valuefootnote:[Krawczyk, H., Bellare, M., and R. Canetti, "`HMAC: Keyed-Hashing for Message Authentication`", RFC 2104, DOI 10.17487/RFC2104, February 1997, <http://www.rfc-editor.org/info/rfc2104>.]
of the string composed of the components mentioned above, where the components are separated by underscore characters. The HMAC value is computed using a key, which is only to be known to the RMBT and Control servers.

The time at which the measurement is allowed to start is included to allow congestion control. Normally the client will be allowed to start immediately. If the number of tests on a RMBT server is too high the server may choose to permit the client at a later time. The server will allow the client to measure only in a specific window around the specified time. For details cf. section <<token_token>>.

The Control Server then transmits the token as well as all the additional test parameters (DNS name or IP address of the RMBT Server, number of parallel TCP connections, number of pings, test duration …) to the client.
The token is used for identifying the test session.

In Phase 1, the client opens latexmath:[n:=n_d] TCP connections to the assigned RMBT Server. The same token has to be submitted on each connection.

NOTE: For an example for phase 1 see <<phase_1_example>>

[[phase_2]]
=== {phase_2}
The pre-test ensures that the Internet connection is in an "`active`" state, e.g. that dedicated radio resources are available (e.g. CELL_DCH state in UMTS or L0 state in DSL).
This makes the outcome of the test independent of the prior usage of the broadband access and thus leads to reproducible test results.
In addition the pre-test gives a rough estimate of the bandwidth.
If the estimate for the available bandwidth is very low, the test continues with 1 connection instead of _n_ (for details see below).

Within each of the _n_ TCP connections, the client requests and the server sends a data block of size <<const_s,_s_>> (randomly generated data with high entropy, cf. section <<General requirements>>).

While the duration of the pre-test has not exceeded <<const_d,_d_>>, the client requests a data block of double size compared to the last iteration step.
The transfer of the last data block will be finished even if the duration has already exceeded _d_.

At the end of the pre-test, all TCP connections are left open for further use if more than four chunks have been transmitted in the downlink pre-test.
Otherwise, _n_ is reduced to 1 and all connections except one are terminated.

NOTE: For an example for phase 2 see <<phase_2_example>>

[[phase_3]]
=== {phase_3}
During this phase, the client sends <<const_p,_p_>> "`pings`" in short intervals to the RMBT Server to test the latency of the connection.
One "`ping`" consists of the transmission of short strings via one of the TCP connections to the Server, which returns short strings as acknowledgement.
The other connections are idle during phase 3.

The client measures the time between sending and receiving the return message, while the server measures the time between sending its return message and the client's reception response.
The client stores all measurements, and the median of all server measurements is used as result.

NOTE: For an example for phase 3 see <<phase_3_example>>

[[phase_4]]
=== {phase_4}
Within each of the <<const_n,_n_>> TCP connections opened during <<phase_1, phase 1>> (which are still open after phase 2),
the client simultaneously requests and the server continuously sends data streams consisting of fixed-size chunks of size <<const_s,_s_>>
(randomly generated data with high entropy, cf. section <<General requirements>>) for time <<const_t,_t_>>.

Let latexmath:[T := \{1, \dots, n\}] be the set of threads. +
latexmath:[\forall\:k\!\in\!T\!: t_k^{(0)}:=0 \land b_k^{(0)}:=0]

All transmissions start at the same time, which is denoted as relative time 0.
For each TCP connection latexmath:[k\!\in\!T], the client records the relative time latexmath:[t_k^{(j)}]
and the total amount latexmath:[b_k^{(j)}] of data received on this connection from time 0 to latexmath:[t_k^{(j)}]
for successive values of latexmath:[j], starting with latexmath:[j := 1] for the first chunk received.
Let latexmath:[m_k] be the number of pairs latexmath:[\Big(t_k^{(j)},b_k^{(j)}\Big)] which have been recorded for TCP connection latexmath:[k].

After time <<const_t,_t_>> the RMBT Server stops sending further chunks on all connections and sends a last chunk with the <<termination_byte, termination byte>> set.
If the client hasn't received the chunks with the termination bytes on all connections after time _t_, the client may choose to stop listening for further chunks and may terminate the connections.
(This might happen mainly because of a slow connection.)

Let latexmath:[t^* := \min\big(\{t_k^{(m_k)} | k\!\in\!T \}\big)] +
and latexmath:[\forall\:k\!\in\!T\!: l_k := \min\big(\{j\!\in\!\mathbb{N} \:|\: 1 \leqslant j \leqslant m_k \land t_k^{(j)} \geqslant t^* \}\big)] +
(latexmath:[l_k] being the index of the chunk received on thread latexmath:[k] at latexmath:[t^*] or right after latexmath:[t^*])

Then the amount latexmath:[b_k] of data received over TCP connection latexmath:[k] from time 0 to time latexmath:[t^*] is approximately

[latexmath]
++++
b_k :\approx b_k^{(l_k-1)} + \frac{t^* - t_k^{(l_k-1)}}{t_k^{(l_k)} - t_k^{(l_k-1)}} (b_k^{(l_k)}-b_k^{(l_k-1)})
++++

(For the thread latexmath:[k] where latexmath:[t^* = t_k^{(m_k)} \implies l_k = m_k] and therefore this simplifies to latexmath:[b_k = b_k^{(m_k)}].)

The data rate latexmath:[R] for all TCP connections together is
[latexmath]
++++
R := \frac{1}{t^*} \sum_{k=1}^n b_k \approx \frac{1}{t^*} \sum_{k=1}^n \Bigg(b_k^{(l_k-1)} + \frac{t^* - t_k^{(l_k-1)}}{t_k^{(l_k)} - t_k^{(l_k-1)}} \; (b_k^{(l_k)}-b_k^{(l_k-1)})\Bigg).
++++

This value is taken as an approximation of the download rate.

NOTE: For an example for phase 4 see <<phase_4_example>>

[[phase_5]]
=== {phase_5}
Phase 5 works analogous to <<phase_2, phase 2>>, but with the client as sender. The rationale for the uplink pre-test is the same as for the downlink pre-test.

If the TCP connections were terminated in <<phase_4, phase 4>> the client opens latexmath:[n:=n_u] TCP connections to the assigned RMBT Server again, otherwise the still open connections are reused.
If latexmath:[n] was reduced to 1 in phase 2, it may also be reduced to 1 for the uplink measurements.

Within each TCP connection, the client sends a data block of size s (randomly generated data with high entropy, cf. section <<General requirements>>).

While the duration of the pre-test has not exceeded <<const_d,_d_>>, the client sends a data block of double size compared to the last iteration step.
The transfer of the last data block will be finished even if the duration has already exceeded _d_.
At the end of the pre-test, the TCP connections are left open for further use.

NOTE: For an example for phase 5 see <<phase_5_example>>

[[phase_6]]
=== {phase_6}
Phase 6 works analogous to <<phase_4, phase 4>>, but this time the *client* is sending <<const_s,_s_>> size chunks continuously to the server for time <<const_t,_t_>>.
The main difference is that in this case the server has to measure the time and reports the measurements back to the client.
This is done at a minimum interval of <<const_r,_r_>>.

Let latexmath:[T := \{1, \dots, n\}] be the set of threads. +
latexmath:[\forall\:k\!\in\!T\!: t_k^{(0)}:=0 \land b_k^{(0)}:=0]

All transmissions start at the same time, which is denoted as relative time 0.
For each TCP connection latexmath:[k\!\in\!T], the server gives feedback to the client by sending the relative time
latexmath:[t_k^{(j)}] and the amount latexmath:[b_k^{(j)}] of data received from time 0 to latexmath:[t_k^{(j)}]
for successive values of latexmath:[j], starting with latexmath:[j := 1] for the first chunk received.
The feedback is sent immediately after a chunk has been received, but only if the last feedback was sent longer than <<const_r,_r_>> ago. 

After time <<const_t,_t_>> the client sends the last chunk (marked with the <<termination_byte,termination byte>>) and stops sending further chunks on all connections.
The following takes place for each connection independently: The client now waits a fixed time of <<const_w1,latexmath:[w_1]>> and checks if the feedback for all sent chunks has been received.
If feedback for some chunks is still missing (because of a slow connection) the client waits additionally a maximum of <<const_w2,latexmath:[w_2]>> to receive at least all chunks where latexmath:[t_k^{(j)} < t-w_3].

For each latexmath:[k\!\in\!T], let latexmath:[m_k] be the number of pairs latexmath:[\Big(t_k^{(j)},b_k^{(j)}\Big)]
which have been recorded for TCP connection _k_.

Let latexmath:[t^* := \min\big(\{t_k^{(m_k)} | k\!\in\!T \}\big)] +
and latexmath:[\forall\:k\!\in\!T\!: l_k := \min\big(\{j\!\in\!\mathbb{N} \:|\: 1 \leqslant j \leqslant m_k \land t_k^{(j)} \geqslant t^* \}\big)]

Then the amount latexmath:[b_k] of data received over TCP connection latexmath:[k] from time 0 to time latexmath:[t^*] is approximately

[latexmath]
++++
b_k :\approx b_k^{(l_k-1)} + \frac{t^* - t_k^{(l_k-1)}}{t_k^{(l_k)} - t_k^{(l_k-1)}} (b_k^{(l_k)}-b_k^{(l_k-1)})
++++

The data rate latexmath:[R] for all TCP connections together is
[latexmath]
++++
R := \frac{1}{t^*} \sum_{k=1}^n b_k \approx \frac{1}{t^*} \sum_{k=1}^n \left(b_k^{(l_k-1)} + \frac{t^* - t_k^{(l_k-1)}}{t_k^{(l_k)} - t_k^{(l_k-1)}} \; (b_k^{(l_k)}-b_k^{(l_k-1)})\right).
++++

This value is taken as an approximation of the upload rate.

NOTE: For an example for phase 6 see <<phase_6_example>>

[[phase_7]]
=== {phase_7}
After finishing all tests, the client sends the collected data to the Control Server.
All results collected on the client are transferred directly to the Control Server.
All tests, successful or unsuccessful, are stored by the Data Server.

NOTE: For an example for phase 7 see <<phase_7_example>>

== Measurement data
The following information is transmitted to the Control Server for each completed test:

* Latency
* Uplink data rate
* Downlink data rate
* Test UUID
* Date and time
* The number _n_ of concurrent TCP connections actually used
* (optionally) the individual tuples latexmath:[\Big(t_k^{(j)},b_k^{(j)}\Big)] measured during uplink and downlink measurement.
In order to keep the data size small, not all tuples might be transmitted.
In these cases an exact recalculation of the measured data rates based on the transmitted tuples might not possible.

NOTE: Further parameters might be collected and transmitted additionally to these information. These additional parameters are out of scope for this specification.

== Communication protocol

Data streams are sent using chunks of size <<const_s,_s_>>.
The initial <<token_chunksize,CHUNKSIZE>> is defined by the server during the handshake (e.g., 4096 bytes). The chunk size can be dynamically changed with the commands <<token_getchunks,GETCHUNKS>>, <<token_gettime,GETTIME>>, <<PUTNORESULT>>, <<PUT>>.

[[termination_byte]]
=== Termination byte
The last byte of each chunk is used to signal the receiving side if another chunk will follow.
The last byte is set to a byte with all bits set to 0 (i.e. 0x00) for all but the last chunks.
The last chunk has a last byte with all bits set to 1 (i.e. 0xFF).

This definition is valid for the data streams following the commands <<token_getchunks,GETCHUNKS>>, <<token_gettime,GETTIME>>, <<PUT>>, <<PUTNORESULT>>.


=== Client

[[token_token]]
==== TOKEN <VALUE>
Before the client is authorized to perform any measurements it has provide the server with a token which is generated by the Control Server and transmitted to the client.
The client transmits the verification token, which contains the necessary values for running the test.
The token is constructed as follows: <UUID>_<TIMSTAMP>_<HMAC> (the right and left angle brackets are not part of the token).

* UUID: the test UUID generated by the Control Server for each test;
* TIMESTAMP: standard Unix timestamp of the earliest allowed start time of the test;
* HMAC: base64 coded HMAC-SHA1 value of <UUID>_<TIMESTAMP>

The HMAC is calulated using a shared secret between the Control Server and the RMBT server.
If the HMAC is invalid the server immediately terminates the connection.
Also if the token is syntactically invalid the connection is terminated.

The client is only accepted if the current time is in a defined window depending on the timestamp in the token.
If the client is early, but in the defined window, the server sleeps until the time is reached.
If the client is at the time or late, but in the defined window, the client is allowed to start measuring immediately.
The current server configuration used a window of 20 seconds early, 90 seconds late.

==== PING
The server is expected to reply immediately with a <<PONG>>.

[[token_gettime]]
==== GETTIME <DURATION> <CHUNKSIZE>
The client requests a data stream from the RMBT Server with a duration of <DURATION> seconds consisting of chunks of the previously specified size <<const_s,_s_>> or the size optionally given in the <CHUNKSIZE> argument.
The server responds by sending the data stream.
The data stream ends after <DURATION> seconds have passed on the server side.
After the client received the last chunk, marked by the <<termination_byte,termination byte>>, the client sends an OK to the server.

[[token_getchunks]]
==== GETCHUNKS <CHUNKS> <CHUNKSIZE>
The client requests a data stream from the RMBT Server, consisting of <CHUNKS> chunks of the previously specified size <<const_s,_s_>> or the size optionally given in the <CHUNKSIZE> argument.
The last byte will be marked with the <<termination_byte,termination byte>>.
After all chunks have been received by the client, the client is expected to send an OK to the server.

==== PUT <CHUNKSIZE>
The client requests to send a data stream to the RMBT Server consisting of chunks of the previously specified size <<const_s,_s_>> or the size optionally given in the <CHUNKSIZE> argument.
The server responds with <<token_ok_server,OK>>.
The data stream ends with a <<termination_byte,termination byte>> on the last position of the transmitted chunks in the data stream.
After a chunk has been received--but only if the time <<const_r,_r_>> has passed since the last sending--the server sends a <<token_time_bytes,TIME <t> BYTES <b> >> back to the client,
telling the client

* <t>: the number of nanoseconds passed since it has received the PUT,
* and <b>: the number of bytes received.

The last chunk is signaled with a termination byte by the client.
After the server received the last chunk it responds with a <<token_time,TIME <t> >>.

==== PUTNORESULT <CHUNKSIZE>
Works identical to <<PUT>>, but the server sends no intermediate <<token_time_bytes,TIME <t> BYTES <b> >> results. The  <<token_time,TIME <t>>> is sent nevertheless.

[[token_ok_client]]
==== OK
OK is the client's response to a successfully received transmission from the RMBT server.
The transmission can either be a <<PONG>> or a data stream initiated by <<token_getchunks,GETCHUNKS>> or <<token_gettime,GETTIME>>.

==== ERR
ERR is the client's response to any unsuccessful request.

==== QUIT
With QUIT the client requests to quit the test. The server responds with a <<BYE>> and immediately closes the connection thereafter.

=== Server

==== <VERSION>
<VERSION> is the version of the running server as string, e.g. "`RMBTv1.0.0`".

==== ACCEPT <VALUES>
ACCEPT <VALUES> is the list of procedures currently allowed or expected by the server.
Possible values are <<token_token,TOKEN>>, <<token_getchunks,GETCHUNKS>>, <<token_gettime,GETTIME>>, <<PUT>>, <<PUTNORESULT>>, <<PING>>, <<QUIT>>, in no specific order and separated from each other by space characters.

[[token_chunksize]]
==== CHUNKSIZE <CHUNKSIZE> <CHUNKSIZE_MIN> <CHUNKSIZE_MAX>
The server defines the used chunk-size (size <<const_s,_s_>> of initial data block) and chunk-size range in bytes (octets).

==== PONG
PONG is the answer to a <<PING>> from the client.
The client responds with <<token_ok_client,OK>>.

[[token_time]]
==== TIME <t>
The number of nanoseconds <t> passed since the beginning of the measurement. See <<PUT>> and <<PUTNORESULT>>.

[[token_time_bytes]]
==== TIME <t> BYTES <b>
The number of nanoseconds <t>, and the number of bytes <b> received since the beginning of the current measurement. See <<PUT>>.

[[token_ok_server]]
==== OK
OK is the server's regular response to <<token_token,TOKEN>>, <<PUT>>, <<PUTNORESULT>>.

==== ERR
ERR is the server's response to any unsuccessful request.

==== BYE
BYE is the server's regular response to <<QUIT>>.
The server closes the connection right after responding.


== Communication examples

* _<n CHUNK(S)>_ denotes the sending of n chunks of _CHUNKSIZE_ with the <<Termination byte>> set to 0x00.
* _<CHUNKS>_ denotes the sending of multiple chunks of _CHUNKSIZE_ with the termination byte set to 0x00 until the desired time is reached. +
  Dots (...) before _CHUNKS_ denote that the chunks are sent concurrently and independent of the communication in the other direction.
* _<ENDCHUNK>_ denoted the sending of 1 chunk with the termination byte set to 0xFF

[[phase_1_example]]
=== Example for phase 1

.Communcation example between client and Control Server for phase 1
[plantuml, format=svg]
....
hide footbox
participant Client #LightBlue
participant "Control Server" #Khaki
Client -[#blue]> "Control Server": Test request
Client <[#orange]- "Control Server": Test parameters
....

.Communication example for <<phase_1>>
[plantuml, format=svg]
....
hide footbox
participant Client #LightBlue
participant "RMBT Server" #LightSalmon
Client <[#red]- "RMBT Server": RMBTv1.0.0
Client <[#red]- "RMBT Server": ACCEPT TOKEN QUIT
Client -[#blue]> "RMBT Server": TOKEN 629786f4-....
Client <[#red]- "RMBT Server": OK
Client <[#red]- "RMBT Server": CHUNKSIZE 4096 4096 4194304
Client <[#red]- "RMBT Server": ACCEPT GETCHUNKS GETTIME PUT PUTNORESULT PING QUIT
....

[[phase_2_example]]
=== Example for phase 2

.Communication example for <<phase_2>>
[plantuml, format=svg]
....
hide footbox
participant Client #LightBlue
participant "RMBT Server" #LightSalmon
Client -[#blue]> "RMBT Server": GETCHUNKS 1 4096
Client <[#red]- "RMBT Server": <1 CHUNK>
Client -[#blue]> "RMBT Server": OK
Client <[#red]- "RMBT Server": ACCEPT GETCHUNKS GETTIME PUT PUTNORESULT PING QUIT
Client -[#blue]> "RMBT Server": GETCHUNKS 2 4096
Client <[#red]- "RMBT Server": <2 CHUNKS>
Client -[#blue]> "RMBT Server": OK
Client <[#red]- "RMBT Server": ACCEPT GETCHUNKS GETTIME PUT PUTNORESULT PING QUIT
....

[[phase_3_example]]
=== Example for phase 3

.Communication example for <<phase_3>>
[plantuml, format=svg]
....
hide footbox
participant Client #LightBlue
participant "RMBT Server" #LightSalmon
Client -[#blue]> "RMBT Server": PING
Client <[#red]- "RMBT Server": PONG
Client -[#blue]> "RMBT Server": OK
Client <[#red]- "RMBT Server": ACCEPT GETCHUNKS GETTIME PUT PUTNORESULT PING QUIT
....

[[phase_4_example]]
=== Example for phase 4

.Communication example for <<phase_4>>
[plantuml, format=svg]
....
hide footbox
participant Client #LightBlue
participant "RMBT Server" #LightSalmon
Client -[#blue]> "RMBT Server": GETTIME 7 16384
Client <[#red]- "RMBT Server": <CHUNKS>
Client <[#red]- "RMBT Server": <ENDCHUNK>
Client -[#blue]> "RMBT Server": OK
Client <[#red]- "RMBT Server": ACCEPT GETCHUNKS GETTIME PUT PUTNORESULT PING QUIT
....

[[phase_5_example]]
=== Example for phase 5

.Communication example for <<phase_5>>
[plantuml, format=svg]
....
hide footbox
participant Client #LightBlue
participant "RMBT Server" #LightSalmon
Client -[#blue]> "RMBT Server": PUTNORESULT 4096
Client <[#red]- "RMBT Server": OK
Client -[#blue]> "RMBT Server": <1 CHUNK>
Client <[#red]- "RMBT Server": TIME 123456
Client <[#red]- "RMBT Server": ACCEPT GETCHUNKS GETTIME PUT PUTNORESULT PING QUIT
Client -[#blue]> "RMBT Server": PUTNORESULT 4096
Client <[#red]- "RMBT Server": OK
Client -[#blue]> "RMBT Server": <2 CHUNKS>
Client <[#red]- "RMBT Server": TIME 123456
Client <[#red]- "RMBT Server": ACCEPT GETCHUNKS GETTIME PUT PUTNORESULT PING QUIT
....

[[phase_6_example]]
=== Example for phase 6

.Communication example for <<phase_6>>
[plantuml, format=svg]
....
hide footbox
participant Client #LightBlue
participant "RMBT Server" #LightSalmon
Client -[#blue]> "RMBT Server": PUT 32768
Client <[#red]- "RMBT Server": OK
Client -[#blue]> "RMBT Server": <CHUNKS...>
Client <[#red]- "RMBT Server": TIME 123456 BYTES 163840
Client -[#blue]> "RMBT Server": <...CHUNKS...>
Client <[#red]- "RMBT Server": TIME 234567 BYTES 327680
Client -[#blue]> "RMBT Server": <...CHUNKS>
Client -[#blue]> "RMBT Server": <ENDCHUNK>
Client <[#red]- "RMBT Server": TIME 123456
Client <[#red]- "RMBT Server": ACCEPT GETCHUNKS GETTIME PUT PUTNORESULT PING QUIT
....

[[phase_7_example]]
=== Example for phase 7

.Communication example for <<phase_7>>
[plantuml, format=svg]
....
hide footbox
participant Client #LightBlue
participant "RMBT Server" #LightSalmon
Client -[#blue]> "RMBT Server": QUIT
Client <[#red]- "RMBT Server": BYE
....

.Communcation example between client and Control Server for phase 7
[plantuml, format=svg]
....
hide footbox
participant Client #LightBlue
participant "Control Server" #Khaki
Client -[#blue]> "Control Server": Test results
....

:sectnums!:
== Change history

[options="header,autowidth"]
|===
|Version |Date |Comment
|1.0.0 |2017-06-22 a|
* Support for dynamic chunk sizes

|0.8.0 |2015-10-16 a|
* First HTML version
* Updates and clarifications, mainly for formulas and communication examples
* System overview figure updated for functionality
* Minor refinements and error corrections
* Change history added
|===

[small]#Git-Blob-Id: $Id$#
