--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

--
-- Data for Name: qos_test_desc; Type: TABLE DATA; Schema: public; Owner: rmbt
--

COPY qos_test_desc (uid, desc_key, value, lang) FROM stdin;
61	timeout	Test konnte nicht beendet werden. Timeout überschritten!	de
16	tcp.failure	Der TCP-Test war nicht erfolgreich. Es konnte keine Verbindung aufgebaut werden.	de
17	udp.success	Der UDP-Test war erfolgreich. Alle Pakete sind angekommen.	de
15	tcp.success	Der TCP-Test war erfolgreich. Es konnte eine Verbindung aufgebaut werden.	de
18	udp.failure	Der UDP-Test war nicht erfolgreich. Pakete sind verloren gegangen.	de
84	website.error	There has been an error during the test.	en
85	website.error	Während des Tests ist ein Fehler aufgetreten.	de
24	dns.failure	DNS request failed (resolver: %PARAM dns_objective_resolver%)	en
2	http.failure	Der übertragene Inhalt entspricht nicht dem Original, er wurde modifiziert.	de
23	dns.success	DNS request successful (resolver: %PARAM dns_objective_resolver%)	en
30	udp.failure	The UDP test failed. Some packets have been lost.	en
8	website.short	Die Übertragung von %PARAM website_objective_url% dauerte weniger als %PARAM website_objective_timeout 1000000000 0 f% s.\nDauer: %PARAM website_result_duration_ns 1000000000 1 f% s	de
7	website.long	Die Übertragung von %PARAM website_objective_url% dauerte mehr als %PARAM website_objective_timeout 1000000000 0 f% s.\nDauer:  %PARAM website_result_duration_ns 1000000000 1 f% s.	de
26	website.short	Transfer of %PARAM website_objective_url% took less than %PARAM website_objective_timeout 1000000000 0 f% s.\nDuration: %PARAM website_result_duration_ns 1000000000 1 f% s	en
65	test.description	n/a	en
20	http.failure	The received content is not the same as the original one, hence looks like been modified.	en
54	tcp.out.testinfo	TCP Outgoing:\nEs wurde versucht, eine ausgehende Verbindung zum QoS-Testserver über den Port: %PARAM tcp_objective_out_port% aufzubauen.	de
55	tcp.out.testinfo	TCP outgoing:\nIt has been attempted to establish an outgoing connection to the QoS test server on port: %PARAM tcp_objective_out_port%	en
25	website.long	Transfer of %PARAM website_objective_url% took more than %PARAM website_objective_timeout 1000000000 0 f% s.\nDuration: %PARAM website_result_duration_ns 1000000000 1 f% s	en
57	dns.testinfo	DNS request for the domain: %PARAM dns_objective_host%\nRequested record: %PARAM dns_objective_dns_record%\n\r\nTest result:\nDNS status: %PARAM dns_result_status%\nDNS entries: %PARAM dns_result_entries%\r\nTest duration: %PARAM duration_ns 1000000 0 f% ms	en
38	http.testinfo	Target: '%PARAM http_objective_url%'\nRange: %PARAM http_objective_range%\nDuration: %PARAM duration_ns 1000000000 1 f% s\nLength: %PARAM http_result_length%\r\nStatus code: %PARAM http_result_status%\nHash: %PARAM http_result_hash%\nHeader: \r\n%PARAM http_result_header%	en
64	test.description	n/a	de
63	website.not_found	The test web site could not be reached.	en
44	dns.unknowndomain.success	A DNS request for a not existing domain: succeeded, no entries have been returned.	en
43	dns.unknowndomain.success	Eine DNS-Anfrage für eine nicht existierende Domain war erfolgreich, es wurden keine Einträge gefunden.	de
41	dns.unknowndomain.failure	Eine DNS-Anfrage für eine nicht existierende Domain hat ein unzulässiger Ergebnis geliefert: %PARAM dns_result_entries%.	de
56	dns.testinfo	DNS Anfrage für die Domain: %PARAM dns_objective_host%\nAbgefragter Typ:  %PARAM dns_objective_dns_record%-Record.\n\nTestergebnis: \nDNS Status: %PARAM dns_result_status%\nDNS-Einträge: %PARAM dns_result_entries%\r\nDauer: %PARAM duration_ns 1000000 0 f% ms	de
13	test.dns	DNS ist ein fundamentaler Internetdienst. Er wird zur Übersetzung von Domain-Namen auf IP-Adressen verwendet. Es wird - je nach Test - getestet, ob der Dienst verfügbar ist, ob die Antworten korrekt sind und wie schnell der Server antwortet.	de
33	test.udp	UDP is an important connectionless Internet protocol. It is used for real-time communications, e.g. for VoIP and video.	en
27	tcp.success	The test was successful. A connection could be established.	en
28	tcp.failure	The test was not successful. A connection could not be established.	en
42	dns.unknowndomain.failure	A DNS request for a not existing domain has returned an invalid result: %PARAM dns_result_entries%	en
29	udp.success	The UDP test was successful. All packets have been transferred successfully.	en
34	test.tcp	TCP is an important connection oriented Internet protocol. It is used for example for web pages or e-mail.	en
11	test.udp	UDP ist ein wichtiges verbindungsloses Internet-Protokoll, das für Echtzeitübertragungen (zB. VoIP, Video) verwendet wird.	de
12	test.tcp	TCP ist ein wichtiges verbindungsorientiertes Internetprotokoll. Es wird z.B. für die Übertragung von Webseiten und Mails verwendet.	de
35	test.dns	DNS is a fundamental Internet service. It is used to translate domain names to IP addresses. Depending on the test it is checked if the service is available, if the answers are correct and how fast the server responds.	en
9	test.http	Bei diesem Test wird ein Test-Webobjekt (z.B. Bild) heruntergeladen und überprüft, ob es beim Transport verändert wurde.	de
36	test.website	The website test downloads a reference web page (mobile Kepler page by ETSI). It is verified, if the page can be transferred and how long the download of the page takes.	en
79	test.desc.ntp	Port: %PARAM nontransproxy_objective_port%\nRequest: %PARAM nontransproxy_objective_request%	en
66	test.desc.http	Ziel: %PARAM http_objective_url%	de
67	test.desc.http	Target: %PARAM http_objective_url%	en
70	test.desc.tcp.in	TCP Incoming, Port: %PARAM tcp_objective_in_port%	de
72	test.desc.tcp.out	TCP Outgoing, Port: %PARAM tcp_objective_out_port%	de
73	test.desc.tcp.out	TCP outgoing, port: %PARAM tcp_objective_out_port%	en
74	test.desc.udp.in	UDP Incoming, Port: %PARAM udp_objective_in_port%, Anzahl Pakete: %PARAM udp_objective_in_num_packets%	de
75	test.desc.udp.in	UDP incoming, port: %PARAM udp_objective_in_port%, number of packets: %PARAM udp_objective_in_num_packets%	en
76	test.desc.udp.out	UDP Outgoing, Port: %PARAM udp_objective_out_port%, Anzahl Pakete: %PARAM udp_objective_out_num_packets%	de
77	test.desc.udp.out	UDP outgoing, port: %PARAM udp_objective_out_port%, number of packets: %PARAM udp_objective_out_num_packets%	en
80	test.desc.website	Ziel: %PARAM website_objective_url%	de
81	test.desc.website	Target: %PARAM website_objective_url%	en
92	name.dns	DNS	de
93	name.dns	DNS	en
71	test.desc.tcp.in	TCP incoming, port: %PARAM tcp_objective_in_port%	en
98	test.timeout.exceeded	Test-Timeout überschritten. Der Test konnte nicht erfolgreich durchgeführt werden.	de
99	test.timeout.exceeded	Test timeout exceeded. The test could not be completed successfully.	en
62	website.not_found	Die Test-Webseite konnte nicht erreicht werden.	de
5	dns.success	DNS Abfrage erfolgreich (verwendeter DNS-Server: %PARAM dns_objective_resolver%)	de
6	dns.failure	DNS Abfrage fehlgeschlagen (verwendeter DNS-Server: %PARAM dns_objective_resolver%)	de
90	name.non_transparent_proxy	Transparente Verbindung	de
91	name.non_transparent_proxy	Transparent connection	en
78	test.desc.ntp	Port: %PARAM nontransproxy_objective_port%\nAnfrage: %PARAM nontransproxy_objective_request%	de
89	name.http_proxy	Unmodified content	en
52	tcp.in.testinfo	TCP Incoming:\nEs wurde versucht, eine eingehende Verbindung über den Port: %PARAM tcp_objective_in_port% aufzubauen, Ergebnis: %PARAM tcp_result_in%.	de
46	udp.in.testinfo	UDP Incoming:\nAnzahl angeforderter Pakete: %PARAM udp_objective_in_num_packets%, am Client erhalten: %PARAM udp_result_in_num_packets%, am Server zurückgekommen: %PARAM udp_result_in_response_num_packets%.\nPaketverlustrate: %PARAM udp_result_in_packet_loss_rate%\\%	de
40	dns.unknowndomain.info	A DNS request for a non-existent domain (%PARAM dns_objective_host%) has been run to check the response for the request of the domain's DNS %PARAM dns_objective_dns_record% record.\rThe correct answer would be 'NXDOMAIN' (non-existend domain).\rDNS status: '%PARAM dns_result_status%';\rDuration:%PARAM duration_ns 1000000 0 f% ms\r	en
53	tcp.in.testinfo	TCP Incoming:\nIt has been attempted to establish an incoming connection on port: %PARAM tcp_objective_in_port%, result: %PARAM tcp_result_in%.	en
37	http.testinfo	Ziel: '%PARAM http_objective_url%'\nIntervall: %PARAM http_objective_range%\nDauer: %PARAM duration_ns 1000000000 1 f% s\nLänge: %PARAM http_result_length%\r\nStatus Code: %PARAM http_result_status%\nHash: %PARAM http_result_hash%\nHeader: \r\n%PARAM http_result_header%	de
59	website.testinfo	The transfer of %PARAM website_objective_url% took %PARAM duration_ns 1000000000 1 f% s.\r\n\nTransferred data downlink: %PARAM website_result_rx_bytes 1000 1 f% kB\nTransferred data uplink: %PARAM website_result_tx_bytes 1000 1 f% kB\nHTTP status code: %PARAM website_result_status%	en
69	test.desc.dns	Target: %PARAM dns_objective_host% \nEntry: %PARAM dns_objective_dns_record%\nResolver: %PARAM dns_objective_resolver%	en
97	name.udp	UDP ports	en
58	website.testinfo	Die Übertragung von %PARAM website_objective_url% dauerte %PARAM duration_ns 1000000000 1 f% s.\r\n\nDatenvolumen Downlink: %PARAM website_result_rx_bytes 1000 1 f% kB\nDatenvolumen Uplink: %PARAM website_result_tx_bytes 1000 1 f% kB\nHTTP Status code: %PARAM website_result_status%	de
88	name.http_proxy	Unveränderter Inhalt	de
68	test.desc.dns	Ziel: %PARAM dns_objective_host% \nEintrag: %PARAM dns_objective_dns_record%\nDNS-Auflöser: %PARAM dns_objective_resolver%	de
96	name.udp	UDP Ports	de
1	http.success	Der übertragene Inhalt entspricht exakt dem Original, er wurde nicht modifiziert.	de
31	test.http	This test downloads a test web ressource (e.g. image) and checks if it was modified during transport.	en
19	http.success	The received content is exactly the same as the original one, hence has not been modified.	en
82	website.200	The web page has been transferred successfully.	en
95	name.tcp	TCP ports	en
94	name.tcp	TCP Ports	de
83	website.200	Die Webseite wurde erfolgreich übertragen.	de
86	name.website	Webseite	de
47	udp.in.testinfo	UDP Incoming:\nNumber of packets requested: %PARAM udp_objective_in_num_packets%, received by the client: %PARAM udp_result_in_num_packets%, came back to the server: %PARAM udp_result_in_response_num_packets%.\nPacket loss rate: %PARAM udp_result_in_packet_loss_rate%\\%	en
60	timeout	Test could not be completed. Timeout exceeded!	en
39	dns.unknowndomain.info	Eine DNS Anfrage zu einer nicht existierende Domain (%PARAM dns_objective_host%) wurde ausgeführt, um zu überprüfen, ob ein  %PARAM dns_objective_dns_record% -Eintrag gefunden wird. Korrekt wäre die Antwort 'NXDOMAIN' (Non-Existent Domain, nicht existierende Domain).\rDNS Status: '%PARAM dns_result_status%';\rDauer: %PARAM duration_ns 1000000 0 f% ms\r	de
87	name.website	Web page	en
14	test.website	Beim Webseiten-Test wird eine Referenz-Webseite (mobile Kepler-Seite der ETSI) heruntergeladen. Es wird dabei überprüft, ob die Übertragung der Seite möglich ist, und wie lange die Übertragung der Seite dauert.	de
104	test.desc.tcp.out.21	Übertragung von Dateien (FTP, TCP-Port 21 ausgehend)	de
105	test.desc.tcp.out.21	File transfer protocol (FTP, TCP port 21 outgoing)	en
106	test.desc.tcp.out.22	Verschlüsselte Fernwartung und Dateiübertragung (SSH, TCP-Port 22 ausgehend)	de
50	ntp.testinfo	Eine Anfrage mit dem Inhalt: '%PARAM nontransproxy_objective_request%' wurde an den Testserver (Port: %PARAM nontransproxy_objective_port%) geschickt.\nDie Antwort war: '%PARAM nontransproxy_result_response%'	de
107	test.desc.tcp.out.22	Secure logins and file transfers (SSH, TCP port 22 outgoing)	en
108	test.desc.tcp.out.25	E-Mail-Versand (SMTP, TCP-Port 25 ausgehend)	de
109	test.desc.tcp.out.25	E-mail transmission (SMTP, TCP port 25 outgoing)	en
110	test.desc.tcp.out.53	Namensauflösung von Rechnern und Diensten (DNS, TCP-Port 53 ausgehend)	de
111	test.desc.tcp.out.53	Name resolving for computers and services (DNS, TCP port 53 outgoing)	en
112	test.desc.tcp.out.80	Webseiten-Protokoll (HTTP, TCP-Port 80 ausgehend)	de
113	test.desc.tcp.out.80	Web site protocol (HTTP, TCP port 80 outgoing)	en
114	test.desc.tcp.out.110	E-Mail-Abruf (POP3, TCP-Port 110 ausgehend)	de
115	test.desc.tcp.out.110	E-mail retreival (POP3, TCP port 110 outgoing)	en
116	test.desc.tcp.out.143	E-Mail-Abruf und -Ablage (IMAP, TCP-Port 143 ausgehend)	de
117	test.desc.tcp.out.143	E-mail retrieval and storage (IMAP, TCP port 143 outgoing)	en
118	test.desc.tcp.out.465	Sicherer E-Mail-Versand (SMTPS, TCP-Port 465 ausgehend)	de
119	test.desc.tcp.out.465	Secure e-mail transmission (SMTPS, TCP-Port 465 ausgehend)	en
120	test.desc.tcp.out.554	Steuerung von Übertragung/Streaming audiovisueller Daten (RTSP, TCP-Port 554 ausgehend)	de
168	name.trace	Traceroute	en
121	test.desc.tcp.out.554	Control of streaming of audio and visual media (RTSP, TCP port 554 outgoing)	en
122	test.desc.tcp.out.585	Sicherer E-Mail-Abruf und -Ablage (IMAPS, TCP-Port 585 ausgehend)	de
123	test.desc.tcp.out.585	Secure e-mail retrieval and storage (IMAPS, TCP port 585 outgoing)	en
124	test.desc.tcp.out.587	E-Mail-Versand (SMTP, TCP-Port 587 ausgehend)	de
125	test.desc.tcp.out.587	E-mail transmission (SMTP, TCP port 587 outgoing)	en
126	test.desc.tcp.out.993	Sicherer E-Mail-Abruf und -Ablage (IMAPS, TCP-Port 993 ausgehend)	de
127	test.desc.tcp.out.993	Secure e-mail retrieval and storage (IMAPS, TCP port 993 outgoing)	en
128	test.desc.tcp.out.995	Sicherer E-Mail-Abruf (POP3S, TCP-Port 995 ausgehend)	de
129	test.desc.tcp.out.995	Secure e-mail retreival (POP3S, TCP port 995 outgoing)	en
130	test.desc.tcp.out.5060	Steuerung von Kommunikationssitzungen (SIP, TCP-Port 5060 ausgehend)	de
131	test.desc.tcp.out.5060	Control of communication sessions (SIP, TCP port 5060 outgoing)	en
132	test.desc.tcp.out.6881	Dateienaustausch zwischen Nutzern (BitTorrent, TCP-Port 6881 ausgehend)	de
133	test.desc.tcp.out.6881	Peer to peer file sharing (BitTorrent, TCP port 6881 outgoing)	en
134	test.desc.tcp.out.9001	Anonymisierung von Verbindungsdaten (TOR, TCP-Port 9001 ausgehend)	de
135	test.desc.tcp.out.9001	Online anonymity (TOR, TCP port 9001 outgoing)	en
136	test.desc.udp.out.53	Namensauflösung von Rechnern und Diensten (DNS, UDP-Port 53 ausgehend)	de
137	test.desc.udp.out.53	Name resolving for computers and services (DNS, UDP port 53 outgoing)	en
138	test.desc.udp.out.123	Zeit-Synchronisation (NTP, UDP-Port 123 ausgehend)	de
139	test.desc.udp.out.123	Time synchronisation (NTP, UDP port 123 outgoing)	en
140	test.desc.udp.out.500	Aufbau und Anwendung von sicheren Diensten (ISAKMP, UDP-Port 500 ausgehend)	de
141	test.desc.udp.out.500	Establishment and usage of secure services (ISAKMP, UDP port 500 outgoing)	en
142	test.desc.udp.out.554	Steuerung von Übertragung/Streaming audiovisueller Daten (RTSP, UDP-Port 554 ausgehend)	de
143	test.desc.udp.out.554	Control of streaming of audio and visual media (RTSP, UDP port 554 outgoing)	en
144	test.desc.udp.out.5004	Übertragung/Streaming audiovisueller Daten (RTP, UDP-Port 5004 ausgehend)	de
145	test.desc.udp.out.5004	Streaming of audio and visual media (RTP, UDP port 5004 outgoing)	en
146	test.desc.udp.out.5005	Dienstegüte für Übertragung/Streaming audiovisueller Daten (RTCP, UDP-Port 5005 ausgehend)	de
147	test.desc.udp.out.5005	Quality of service for streaming of audio and visual media (RTCP, UDP port 5005 outgoing)	en
148	test.desc.udp.out.5060	Steuerung von Kommunikationssitzungen (SIP, UDP-Port 5060 ausgehend)	de
149	test.desc.udp.out.5060	Control of communication sessions (SIP, UDP port 5060 outgoing)	en
150	test.desc.udp.out.7078	Sprache über Internet (VoIP, UDP-Port 7078 ausgehend)	de
151	test.desc.udp.out.7078	Voice over Internet (VoIP, UDP port 7078 outgoing)	en
152	test.desc.udp.out.7082	Sprache über Internet (VoIP, UDP-Port 7082 ausgehend)	de
153	test.desc.udp.out.7082	Voice over Internet (VoIP, UDP port 7082 outgoing)	en
154	test.desc.udp.out.27005	Online-Spiele (Steam gaming, UDP-Port 27005 ausgehend)	de
155	test.desc.udp.out.27005	Online gaming (Steam gaming, UDP port 27005 outgoing)	en
156	test.desc.udp.out.27015	Online-Spiele (Steam gaming, UDP-Port 27015 ausgehend)	de
157	test.desc.udp.out.27015	Online gaming (Steam gaming, UDP port 27015 outgoing)	en
49	udp.out.testinfo	UDP Outgoing:\nIt has been attempted to send packets to the QoS test server on port: %PARAM udp_objective_out_port% and receive them back.\r\nNumber of sent packets: %PARAM udp_objective_out_num_packets%, received by the server: %PARAM udp_result_out_num_packets%, came back to the client: %PARAM udp_result_out_response_num_packets%.\nPacket loss rate: %PARAM udp_result_out_packet_loss_rate%\\%	en
3	ntp.success	The request to the test server was not modified.	en
48	udp.out.testinfo	UDP Outgoing:\nEs wurde versucht, Pakete zum QoS-Testserver über den Port: %PARAM udp_objective_out_port% zu senden und empfangen.\r\nAnzahl verschickter Pakete: %PARAM udp_objective_out_num_packets%, am Server angekommen: %PARAM udp_result_out_num_packets%, am Client zurückgekommen: %PARAM udp_result_out_response_num_packets%.\nPaketverlustrate: %PARAM udp_result_out_packet_loss_rate%\\%	de
21	ntp.success	Die Anfrage an den QoS-Testserver wurde nicht verfälscht.	de
4	ntp.failure	Die Anfrage an den Testserver wurde verfälscht.	de
32	test.ntp	This test checks if a request is modified by a proxy or other middlebox.	en
10	test.ntp	Bei diesem Test wird überprüft, ob die Anfrage durch einen Proxy oder eine andere "Middlebox" verändert wird.	de
22	ntp.failure	Die Anfrage to the QoS test server has been manipulated.	en
51	ntp.testinfo	A request with the content: '%PARAM nontransproxy_objective_request%' has been sent to the test server.\nThe answer was: '%PARAM nontransproxy_result_response%'	en
158	traceroute.failure	There has been an error during the traceroute test.	en
159	traceroute.failure	Während des Traceroute Tests ist ein Fehler aufgetreten.	de
160	traceroute.success	There has been no error during the traceroute test.	en
161	traceroute.success	Während des Traceroute Tests ist kein Fehler aufgetreten.	de
162	trace.testinfo	Traceroute test parameters:\nHost: %PARAM traceroute_objective_host%\nMax hops: %PARAM traceroute_objective_max_hops%\n\nTraceroute test results:\nHops needed: %PARAM traceroute_result_hops%\nTraceroute result: %PARAM traceroute_result_status%\n\nFull route:\n%EVAL result=String(nn.parseTraceroute(traceroute_result_details))%	en
163	trace.testinfo	Traceroute Test Parameter:\nHost: %PARAM traceroute_objective_host%\nMax hops: %PARAM traceroute_objective_max_hops%\n\nTraceroute Ergebnis:\nHops benötigt: %PARAM traceroute_result_hops%\nTestergebnis: %PARAM traceroute_result_status%\n\nVollständige Route:\n%EVAL result=String(nn.parseTraceroute(traceroute_result_details))%	de
164	test.desc.trace	Traceroute target: %PARAM traceroute_objective_host%	en
165	test.desc.trace	Traceroute Ziel: %PARAM traceroute_objective_host%	de
166	test.trace	Traceroute is a tool for displaying the route across IP based networks.	en
167	test.trace	Traceroute ist ein Tool für die Ermittlung der Route in IP basierten Netzwerken.	de
169	name.trace	Traceroute	de
170	name.voip	Voice over IP	en
171	name.voip	Voice over IP	de
172	test.voip	VoIP (Voice over IP) is a technology for the delivery of voice across IP based networks.	en
174	voip.incoming.packet.failure	Incoming voice is missing. \nAll incoming voice packets have not arrived at the target location.	en
176	voip.outgoing.packet.failure	Incoming voice is missing. \nAll outgoing voice packets have not arrived at the target location.	en
183	voip.testinfo	%$IF voip_result_status!='OK'%\nThere has been an error during the VoIP test. No results available.\n%$ENDIF voip_result_status!='OK'%\n%$IF voip_result_status=='OK'%\nTEST PARAMETERS\nSample rate: %PARAM voip_objective_sample_rate%, bits per sample: %PARAM voip_objective_bits_per_sample%\nCall duration: %PARAM voip_objective_call_duration 1000000 1 f% ms\nPacket interval: %PARAM voip_objective_delay 1000000 1 f% ms\nPayload type: %EVAL result=String(nn.getPayloadType(voip_objective_payload))%\nTarget port: %PARAM voip_objective_out_port%\n\nTEST RESULTS\n\nIncoming voice stream:\nmax. jitter: %PARAM voip_result_in_max_jitter 1000000 2 f% ms\nmean jitter: %PARAM voip_result_in_mean_jitter 1000000 2 f% ms\nmax. delta: %PARAM voip_result_in_max_delta 1000000 2 f% ms\npackets sent: %EVAL result=String(parseInt(voip_objective_call_duration/voip_objective_delay));%\npackets received: %PARAM voip_result_in_num_packets%\npacket lost percentage: %EVAL var _sent= parseInt(voip_objective_call_duration/voip_objective_delay); result=(100 * ((_sent - voip_result_in_num_packets) / _sent)); %\\%\nsequence errors: %PARAM voip_result_in_sequence_error%\nshortest / longest sequence: %PARAM voip_result_in_short_seq% / %PARAM voip_result_in_long_seq%\n\nOutgoing voice stream:\nmax. jitter: %PARAM voip_result_out_max_jitter 1000000 2 f% ms\nmean jitter: %PARAM voip_result_out_mean_jitter 1000000 2 f% ms\nmax. delta: %PARAM voip_result_out_max_delta 1000000 2 f% ms\npackets sent: %EVAL result=String(parseInt(voip_objective_call_duration/voip_objective_delay));%\npackets received: %PARAM voip_result_out_num_packets%\npacket lost percentage: %EVAL var _sent= parseInt(voip_objective_call_duration/voip_objective_delay); result=(100 * ((_sent - voip_result_out_num_packets) / _sent)); %\\%\nsequence errors: %PARAM voip_result_out_sequence_error%\nshortest / longest sequence: %PARAM voip_result_out_short_seq% / %PARAM voip_result_out_long_seq%\n%$ENDIF voip_result_status=='OK'%	en
175	voip.incoming.packet.success	It is possible to receive voice packets.	en
220	name.dns	DNS	fr
187	voip.incoming.packet_loss.success	The incoming packet loss rate is lower than 5%!	en
221	name.non_transparent_proxy	Connexion transparente	fr
188	voip.outgoing.packet_loss.success	The outgoing packet loss rate is lower than 5%!	en
223	name.udp	Ports UDP	fr
224	name.tcp	Ports TCP	fr
225	name.website	Site Web	fr
226	name.trace	Traceroute	fr
173	test.voip	VoIP (Voice over IP) ist eine Technologie, die das Telefonieren über IP-basierte Netzwerke ermöglicht.	de
227	name.voip	Voix sur IP	fr
177	voip.outgoing.packet.success	It is possible to send voice packets to port %PARAM voip_objective_out_port%.	en
178	voip.jitter.incoming.failure	The incoming mean jitter is too high or empty because of missing outgoing voice packets.	en
179	voip.jitter.incoming.success	The incoming mean jitter is acceptable for a VoIP connection.	en
180	voip.jitter.outgoing.failure	The outgoing mean jitter is too high or empty because of missing outgoing voice packets.	en
181	voip.jitter.outgoing.success	The outgoing mean jitter is acceptable for a VoIP connection.	en
182	voip.timeout	The test took too much time and ran into a timeout.	en
184	test.desc.voip	Simulated VoIP call with a duration of %PARAM voip_objective_call_duration 1000000 1 f% ms.	en
222	name.http_proxy	Contenu non altéré	fr
185	voip.incoming.packet_loss.failure	The incoming packet loss rate is greater than 5%!	en
186	voip.outgoing.packet_loss.failure	The outgoing packet loss rate is greater than 5%!	en
\.


--
-- Name: qos_test_desc_uid_seq; Type: SEQUENCE SET; Schema: public; Owner: rmbt
--

SELECT pg_catalog.setval('qos_test_desc_uid_seq', 227, true);


--
-- Data for Name: qos_test_objective; Type: TABLE DATA; Schema: public; Owner: rmbt
--

COPY qos_test_objective (uid, test, test_class, test_server, concurrency_group, test_desc, test_summary, param, results) FROM stdin;
85	dns	1	2	600	dns.testinfo	test.desc.dns	{"host": "ebay.at", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
86	dns	1	2	600	dns.testinfo	test.desc.dns	{"host": "ebay.de", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
87	dns	1	2	600	dns.testinfo	test.desc.dns	{"host": "facebook.com", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
45	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.587	{"timeout": "5000000000", "out_port": "587"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
46	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.993	{"timeout": "5000000000", "out_port": "993"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
47	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.995	{"timeout": "5000000000", "out_port": "995"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
48	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.5060	{"timeout": "5000000000", "out_port": "5060"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
50	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.9001	{"timeout": "5000000000", "out_port": "9001"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
51	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.554	{"timeout": "5000000000", "out_port": "554"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
9	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.80	{"timeout": "5000000000", "out_port": "80"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
36	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.21	{"timeout": "5000000000", "out_port": "21"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
37	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.22	{"timeout": "5000000000", "out_port": "22"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
39	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.53	{"timeout": "5000000000", "out_port": "53"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
61	udp	1	2	200	udp.out.testinfo	test.desc.udp.out.27015	{"timeout": "5000000000", "out_port": "27015", "out_num_packets": "1"}	[{"operator": "eq", "on_failure": "udp.failure", "on_success": "udp.success", "udp_result_out_response_num_packets": "%PARAM udp_objective_out_num_packets%"}]
52	udp	1	2	200	udp.out.testinfo	test.desc.udp.out.53	{"timeout": "5000000000", "out_port": "53", "out_num_packets": "1"}	[{"operator": "eq", "on_failure": "udp.failure", "on_success": "udp.success", "udp_result_out_response_num_packets": "%PARAM udp_objective_out_num_packets%"}]
106	dns	1	2	600	dns.testinfo	test.desc.dns	{"host": "sparkasse.at", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
107	dns	1	2	600	dns.testinfo	test.desc.dns	{"host": "spiegel.de", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
109	dns	1	2	610	dns.testinfo	test.desc.dns	{"host": "twitter.com", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
110	dns	1	2	610	dns.testinfo	test.desc.dns	{"host": "wikipedia.org", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
113	dns	1	2	610	dns.testinfo	test.desc.dns	{"host": "yahoo.com", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
58	udp	1	2	200	udp.out.testinfo	test.desc.udp.out.7078	{"timeout": "5000000000", "out_port": "7078", "out_num_packets": "1"}	[{"operator": "eq", "on_failure": "udp.failure", "on_success": "udp.success", "udp_result_out_response_num_packets": "%PARAM udp_objective_out_num_packets%"}]
80	dns	1	2	600	dns.testinfo	test.desc.dns	{"host": "apple.com", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
3	non_transparent_proxy	1	2	300	ntp.testinfo	test.desc.ntp	{"port": "80", "request": "GET ", "timeout": "5000000000"}	[{"operator": "eq", "on_failure": "ntp.failure", "on_success": "ntp.success", "nontransproxy_result_response": "%PARAM nontransproxy_objective_request%"}]
4	non_transparent_proxy	1	2	300	ntp.testinfo	test.desc.ntp	{"port": "%RANDOM 20000 55000%", "request": "GET / HTTR/7.9", "timeout": "5000000000"}	[{"operator": "eq", "on_failure": "ntp.failure", "on_success": "ntp.success", "nontransproxy_result_response": "%PARAM nontransproxy_objective_request%"}]
32	dns	1	2	600	dns.unknowndomain.info	test.desc.dns	{"host": "%RANDOMURL ftp. 10 .com%", "record": "A", "timeout": "5000000000"}	[{"operator": "eq", "on_failure": "dns.unknowndomain.failure", "on_success": "dns.unknowndomain.success", "dns_result_entries_found": "0"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
129	voip	1	2	100	voip.testinfo	test.desc.voip	{"timeout": "6000000000", "out_port": "5060", "call_duration": "2000000000"}	[\n  {\n    "evaluate": "%EVAL if (nn.coalesce(voip_result_out_mean_jitter, 50000000) < 50000000) result=true; else result=false;%",\n    "on_failure": "voip.jitter.outgoing.failure",\n    "on_success": "voip.jitter.outgoing.success"\n  },\n  {\n    "evaluate": "%EVAL if (nn.coalesce(voip_result_in_mean_jitter, 50000000) < 50000000) result=true; else result=false;%",\n    "on_failure": "voip.jitter.incoming.failure",\n    "on_success": "voip.jitter.incoming.success"\n  },\n  {\n  "evaluate": "%EVAL if (nn.coalesce(voip_result_out_num_packets, 0) > 0) result=true; else result=false;%",\n  "on_failure": "voip.outgoing.packet.failure",\n    "on_success": "voip.outgoing.packet.success"\n  },\n  {\n    "evaluate": "%EVAL if (nn.coalesce(voip_result_in_num_packets, 0) > 0) result=true; else result=false;%",\n    "on_failure": "voip.incoming.packet.failure",\n    "on_success": "voip.incoming.packet.success"\n  },\r\n  {\r\n    "evaluate": "%EVAL var _sent= parseInt(voip_objective_call_duration/voip_objective_delay); var _plr=parseInt(100 * ((_sent - voip_result_in_num_packets) / _sent)); if (_plr > 5) result=false; else result=true;%",\r\n    "on_failure": "voip.incoming.packet_loss.failure"\r\n  },\r\n  {\r\n    "evaluate": "%EVAL var _sent= parseInt(voip_objective_call_duration/voip_objective_delay); var _plr=parseInt(100 * ((_sent - voip_result_out_num_packets) / _sent)); if (_plr > 5) result=false; else result=true;%",\r\n    "on_failure": "voip.outgoing.packet_loss.failure"\r\n  }, \n  {\n    "evaluate": "%EVAL if(voip_result_status=='TIMEOUT') result={type: 'failure', key: 'voip.timeout'}%"\n  }\n]\n
49	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.6881	{"timeout": "5000000000", "out_port": "6881"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
108	dns	1	2	610	dns.unknowndomain.info	test.desc.dns	{"host": "touch.darkspace.netztest.at", "record": "A", "timeout": "5000000000"}	[{"operator": "eq", "on_failure": "dns.unknowndomain.failure", "on_success": "dns.unknowndomain.success", "dns_result_entries_found": "0"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
81	dns	1	2	610	dns.testinfo	test.desc.dns	{"host": "bankaustria.at", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
82	dns	1	2	610	dns.testinfo	test.desc.dns	{"host": "derstandard.at", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
41	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.143	{"timeout": "5000000000", "out_port": "143"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
43	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.465	{"timeout": "5000000000", "out_port": "465"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
44	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.585	{"timeout": "5000000000", "out_port": "585"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
54	udp	1	2	200	udp.out.testinfo	test.desc.udp.out.500	{"timeout": "5000000000", "out_port": "500", "out_num_packets": "1"}	[{"operator": "eq", "on_failure": "udp.failure", "on_success": "udp.success", "udp_result_out_response_num_packets": "%PARAM udp_objective_out_num_packets%"}]
57	udp	1	2	200	udp.out.testinfo	test.desc.udp.out.5060	{"timeout": "5000000000", "out_port": "5060", "out_num_packets": "1"}	[{"operator": "eq", "on_failure": "udp.failure", "on_success": "udp.success", "udp_result_out_response_num_packets": "%PARAM udp_objective_out_num_packets%"}]
60	udp	1	2	200	udp.out.testinfo	test.desc.udp.out.27005	{"timeout": "5000000000", "out_port": "27005", "out_num_packets": "1"}	[{"operator": "eq", "on_failure": "udp.failure", "on_success": "udp.success", "udp_result_out_response_num_packets": "%PARAM udp_objective_out_num_packets%"}]
62	udp	1	2	200	udp.out.testinfo	test.desc.udp.out.554	{"timeout": "5000000000", "out_port": "554", "out_num_packets": "1"}	[{"operator": "eq", "on_failure": "udp.failure", "on_success": "udp.success", "udp_result_out_response_num_packets": "%PARAM udp_objective_out_num_packets%"}]
56	udp	1	2	200	udp.out.testinfo	test.desc.udp.out.5005	{"timeout": "5000000000", "out_port": "5005", "out_num_packets": "1"}	[{"operator": "eq", "on_failure": "udp.failure", "on_success": "udp.success", "udp_result_out_response_num_packets": "%PARAM udp_objective_out_num_packets%"}]
88	dns	1	2	640	dns.testinfo	test.desc.dns	{"host": "facebook.com", "record": "AAAA", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
93	dns	1	2	640	dns.testinfo	test.desc.dns	{"host": "google.com", "record": "AAAA", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
91	dns	1	2	640	dns.testinfo	test.desc.dns	{"host": "google.at", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
89	dns	1	2	640	dns.testinfo	test.desc.dns	{"host": "geizhals.at", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
90	dns	1	2	640	dns.testinfo	test.desc.dns	{"host": "gmx.net", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
83	dns	1	2	610	dns.testinfo	test.desc.dns	{"host": "dict.cc", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
84	dns	1	2	620	dns.testinfo	test.desc.dns	{"host": "diepresse.com", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
75	dns	1	2	600	dns.unknowndomain.info	test.desc.dns	{"host": "%RANDOMURL www. 10 .net%", "record": "A", "timeout": "5000000000"}	[{"operator": "eq", "on_failure": "dns.unknowndomain.failure", "on_success": "dns.unknowndomain.success", "dns_result_entries_found": "0"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
94	dns	1	2	620	dns.testinfo	test.desc.dns	{"host": "google.de", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
96	dns	1	2	620	dns.testinfo	test.desc.dns	{"host": "heise.de", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
98	dns	1	2	620	dns.testinfo	test.desc.dns	{"host": "help.gv.at", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
55	udp	1	2	200	udp.out.testinfo	test.desc.udp.out.5004	{"timeout": "5000000000", "out_port": "5004", "out_num_packets": "1"}	[{"operator": "eq", "on_failure": "udp.failure", "on_success": "udp.success", "udp_result_out_response_num_packets": "%PARAM udp_objective_out_num_packets%"}]
53	udp	1	2	200	udp.out.testinfo	test.desc.udp.out.123	{"timeout": "5000000000", "out_port": "123", "out_num_packets": "1"}	[{"operator": "eq", "on_failure": "udp.failure", "on_success": "udp.success", "udp_result_out_response_num_packets": "%PARAM udp_objective_out_num_packets%"}]
100	dns	1	2	620	dns.testinfo	test.desc.dns	{"host": "kurier.at", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
101	dns	1	2	620	dns.testinfo	test.desc.dns	{"host": "microsoft.com", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
102	dns	1	2	620	dns.testinfo	test.desc.dns	{"host": "orf.at", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
123	non_transparent_proxy	1	2	300	ntp.testinfo	test.desc.ntp	{"port": "%RANDOM 20000 55000%", "request": "GET ", "timeout": "5000000000"}	[{"operator": "eq", "on_failure": "ntp.failure", "on_success": "ntp.success", "nontransproxy_result_response": "%PARAM nontransproxy_objective_request%"}]
105	dns	1	2	620	dns.testinfo	test.desc.dns	{"host": "rtr.at", "record": "A", "timeout": "5000000000", "resolver": "8.8.8.8"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
104	dns	1	2	630	dns.testinfo	test.desc.dns	{"host": "rtr.at", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
114	dns	1	2	630	dns.testinfo	test.desc.dns	{"host": "youtube.com", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
76	dns	1	2	630	dns.unknowndomain.info	test.desc.dns	{"host": "%RANDOMURL www. 10 .darknet.netztest.at%", "record": "A", "timeout": "5000000000"}	[{"operator": "eq", "on_failure": "dns.unknowndomain.failure", "on_success": "dns.unknowndomain.success", "dns_result_entries_found": "0"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
78	dns	1	2	630	dns.testinfo	test.desc.dns	{"host": "amazon.com", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
99	dns	1	2	630	dns.testinfo	test.desc.dns	{"host": "krone.at", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
115	dns	1	2	630	dns.testinfo	test.desc.dns	{"host": "youtube.com", "record": "AAAA", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
112	dns	1	2	640	dns.testinfo	test.desc.dns	{"host": "willhaben.at", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
130	traceroute	0	2	10	trace.testinfo	test.desc.trace	{"host": "8.8.8.8", "timeout": "35000000000"}	[{"operator": "eq", "on_failure": "traceroute.failure", "on_success": "traceroute.success", "traceroute_result_status": "OK"}]
40	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.110	{"timeout": "5000000000", "out_port": "110"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
92	dns	1	2	640	dns.testinfo	test.desc.dns	{"host": "google.com", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
73	dns	0	2	600	dns.testinfo	test.desc.dns	{"host": "rtr.at", "record": "A", "timeout": "2000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
122	dns	1	2	640	dns.testinfo	test.desc.dns	{"host": "nic.at", "record": "MX", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
111	dns	1	2	640	dns.testinfo	test.desc.dns	{"host": "wikipedia.org", "record": "AAAA", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
33	dns	1	2	610	dns.unknowndomain.info	test.desc.dns	{"host": "%RANDOMURL www. 20 .com%", "record": "A", "timeout": "5000000000"}	[{"operator": "eq", "on_failure": "dns.unknowndomain.failure", "on_success": "dns.unknowndomain.success", "dns_result_entries_found": "0"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
119	dns	1	2	630	dns.testinfo	test.desc.dns	{"host": "gmx.at", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
38	tcp	1	2	200	tcp.out.testinfo	test.desc.tcp.out.25	{"timeout": "5000000000", "out_port": "25"}	[{"operator": "eq", "on_failure": "tcp.failure", "on_success": "tcp.success", "tcp_result_out": "OK"}]
79	dns	1	2	610	dns.testinfo	test.desc.dns	{"host": "amazon.de", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
120	udp	0	2	200	udp.out.testinfo	test.desc.udp.out.554	{"timeout": "2000000000", "out_port": "554", "out_num_packets": "5"}	[{"operator": "eq", "on_failure": "udp.failure", "on_success": "udp.success", "udp_result_out_response_num_packets": "%PARAM udp_objective_out_num_packets%"}]
97	dns	1	2	600	dns.testinfo	test.desc.dns	{"host": "heise.de", "record": "AAAA", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
95	dns	0	2	600	dns.testinfo	test.desc.dns	{"host": "gxm.at", "record": "A", "timeout": "2000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
125	http_proxy	0	2	401	http.testinfo	test.desc.http	{"url": "http://webtest.nettest.at/qostest/reference13.png", "conn_timeout": "5000000000", "download_timeout": "999000000000"}	\N
127	http_proxy	0	2	402	http.testinfo	test.desc.http	{"url": "http://webtest.nettest.at/qostest/ref45mb.bin", "conn_timeout": "5000000000", "download_timeout": "999000000000"}	\N
124	non_transparent_proxy	1	2	300	ntp.testinfo	test.desc.ntp	{"port": "80", "request": "GET / HTTR/7.9", "timeout": "5000000000"}	[{"operator": "eq", "on_failure": "ntp.failure", "on_success": "ntp.success", "nontransproxy_result_response": "%PARAM nontransproxy_objective_request%"}]
72	dns	0	2	600	dns.testinfo	test.desc.dns	{"host": "rtr.at", "record": "A", "timeout": "2000000000", "resolver": "8.8.8.8"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
77	dns	1	2	630	dns.unknowndomain.info	test.desc.dns	{"host": "%RANDOMURL invalidname. 10 .com%", "record": "A", "timeout": "5000000000"}	[{"operator": "eq", "on_failure": "dns.unknowndomain.failure", "on_success": "dns.unknowndomain.success", "dns_result_entries_found": "0"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
103	dns	1	2	620	dns.testinfo	test.desc.dns	{"host": "raiffeisen.at", "record": "A", "timeout": "5000000000"}	[{"operator": "ge", "on_failure": "dns.failure", "on_success": "dns.success", "dns_result_entries_found": "1"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
121	dns	1	2	630	dns.unknowndomain.info	test.desc.dns	{"host": "%RANDOMURL www. 10 .darknet.netztest.at%", "record": "A", "timeout": "5000000000", "resolver": "8.8.8.8"}	[{"operator": "eq", "on_failure": "dns.unknowndomain.failure", "on_success": "dns.unknowndomain.success", "dns_result_entries_found": "0"},{"operator": "ne", "on_failure": "test.timeout.exceeded", "dns_result_info": "TIMEOUT"}]
59	udp	1	2	200	udp.out.testinfo	test.desc.udp.out.7082	{"timeout": "5000000000", "out_port": "7082", "out_num_packets": "1"}	[{"operator": "eq", "on_failure": "udp.failure", "on_success": "udp.success", "udp_result_out_response_num_packets": "%PARAM udp_objective_out_num_packets%"}]
128	non_transparent_proxy	1	2	300	ntp.testinfo	test.desc.ntp	{"port": "25", "request": "SMTP Transparent", "timeout": "5000000000"}	[{"operator": "eq", "on_failure": "ntp.failure", "on_success": "ntp.success", "nontransproxy_result_response": "%PARAM nontransproxy_objective_request%"}]
16	website	1	2	500	website.testinfo	test.desc.website	{"url": "http://webtest.nettest.at/kepler", "timeout": "10000000000"}	[{"operator": "eq", "on_failure": "website.error", "on_success": "website.200", "website_result_status": "200"}]
27	http_proxy	1	2	400	http.testinfo	test.desc.http	{"url": "http://webtest.nettest.at/qostest/reference05.jpg", "conn_timeout": "5000000000", "download_timeout": "10000000000"}	[{"operator": "eq", "on_failure": "http.failure", "on_success": "http.success", "http_result_hash": "ae9592475c364fa01909dab663417ab5"}]
126	website	0	2	501	website.testinfo	test.desc.website	{"url": "http://webtest.nettest.at/qostest/reference13.png", "timeout": "999000000000"}	\N
118	http_proxy	1	2	400	http.testinfo	test.desc.http	{"url": "http://webtest.nettest.at/qostest/reference01.jpg", "range": "bytes=1000000-1004999", "conn_timeout": "5000000000", "download_timeout": "10000000000"}	[{"operator": "eq", "on_failure": "http.failure", "on_success": "http.success", "http_result_hash": "fc563e1e80b8cb964d712982fa2143c8"}]
\.


--
-- Name: qos_test_objective_uid_seq; Type: SEQUENCE SET; Schema: public; Owner: rmbt
--

SELECT pg_catalog.setval('qos_test_objective_uid_seq', 161, true);


--
-- Data for Name: qos_test_type_desc; Type: TABLE DATA; Schema: public; Owner: rmbt
--

COPY qos_test_type_desc (uid, test, test_desc, test_name) FROM stdin;
1	website	test.website	name.website
2	http_proxy	test.http	name.http_proxy
3	non_transparent_proxy	test.ntp	name.non_transparent_proxy
4	dns	test.dns	name.dns
5	tcp	test.tcp	name.tcp
6	udp	test.udp	name.udp
7	voip	test.voip	name.voip
8	traceroute	test.trace	name.trace
\.


--
-- Name: qos_test_type_desc_uid_seq; Type: SEQUENCE SET; Schema: public; Owner: rmbt
--

SELECT pg_catalog.setval('qos_test_type_desc_uid_seq', 39, true);


--
-- PostgreSQL database dump complete
--

