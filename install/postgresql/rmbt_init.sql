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
-- Name: android_device_map_uid_seq; Type: SEQUENCE SET; Schema: public; Owner: rmbt
--

SELECT pg_catalog.setval('android_device_map_uid_seq', 6151, true);


--
-- Data for Name: provider; Type: TABLE DATA; Schema: public; Owner: rmbt
--

COPY provider (uid, name, mcc_mnc, shortname, map_filter) FROM stdin;
3	Orange Austria Telecommunication GmbH (alt)	232-05	Orange AT (alt)	f
4	Hutchison 3G Austria GmbH (alt)	232-10	3AT (alt)	f
7	tele.ring	232-07	tele.ring	f
8	Bob	232-11	Bob	f
9	YESSS! Telekommunikation GmbH	232-12	Yesss!	f
1	A1 Telekom Austria AG - Mobilnetz	232-01	A1 TA Mobilnetz	t
12	Tele2 Telecommunication GmbH	\N	Tele2	t
14	Colt Technology Services GmbH	\N	Colt	t
13	T-Systems Austria GesmbH	\N	T-Systems	t
16	LIWEST Kabelmedien GmbH	\N	LIWEST	t
17	AT&T Global Network Services Austria GmbH	\N	AT&T	t
18	Belgacom International Carrier Services S.A.	\N	Belgacom	t
20	Verizon Austria GmbH	\N	Verizon	t
21	BT Austria GmbH	\N	BT Austria	t
22	WIEN ENERGIE GmbH	\N	WIEN ENERGIE	t
23	ACOnet	\N	ACOnet	t
24	next layer Telekommunikationsdienstleistungs-GmbH	\N	next layer	t
25	A1 Telekom Austria AG - Festnetz	\N	A1 TA Festnetz	t
27	ÖBB Telekom Service GmbH	\N	ÖBB	t
28	KAPPER NETWORK-COMMUNICATIONS GmbH	\N	Kapper	t
29	WVNET Information und Kommunikation GmbH	\N	WVNET	t
30	Flughafen Wien AG	\N	Flughafen Wien	t
31	Technische Universität Wien	\N	TU Wien	t
32	Universität Wien	\N	Uni Wien	t
33	Wirtschaftsuniversität Wien	\N	WU Wien	t
34	Citycom Telekommunikation GmbH	\N	Citycom	t
35	i3B - Internetbreitband GmbH	\N	i3B	t
2	T-Mobile Austria GmbH	232-03	T-Mobile AT	t
36	Hutchison Drei Austria GmbH	232-10	Hutchison Drei	t
5	UPC Austria GmbH	\N	UPC AT	t
37	Gamsjäger Kabel-TV & ISP Betriebs GmbH	\N	Gamsjäger	t
39	Lycamobile Austria Ltd	232-08	Lycamobile	f
41	UPC Austria Services GmbH - Mobilnetz	232-13	UPC Mobilnetz	f
10	Mundio Mobile (Austria) Limited	232-15	Mundio	f
42	MASS Response Service GmbH	232-17	MASS Response	f
43	smartspace GmbH	232-18	smartspace	f
45	Tele2 Telecommunication GmbH - Mobilnetz	232-19	Tele2 Mobilnetz	f
46	MTEL Austrija GmbH	232-20	MTEL	f
48	ÖBB - Infrastruktur AG - Mobilnetz	232-91	ÖBB Mobilnetz	f
49	ArgoNET GmbH	232-92	ArgoNET	f
6	kabelplus GmbH	\N	kabelplus	t
15	Salzburg AG für Energie, Verkehr und Telekommunikation	\N	Salzburg AG	t
50	Salzburg AG für Energie, Verkehr und Telekommunikation - Mobilnetz	232-21	Salzburg AG Mobilnetz	f
51	Energie AG Oberösterreich Data GmbH	\N	Energie AG	t
52	NETcompany - WLAN Internet Provider GmbH	\N	NETcompany	t
\.


--
-- Data for Name: as2provider; Type: TABLE DATA; Schema: public; Owner: rmbt
--

COPY as2provider (uid, asn, dns_part, provider_id) FROM stdin;
6	16305	\N	1
7	8412	\N	2
8	8339	\N	6
9	29287	\N	22
10	1853	\N	23
11	1764	\N	24
4	8447	\N	25
15	12605	\N	16
16	3330	\N	27
17	48943	\N	28
18	29081	\N	29
20	8445	\N	15
21	1776	\N	33
22	760	\N	32
23	679	\N	31
24	1901	\N	25
14	1257	\N	12
25	8437	\N	12
19	28771	\N	30
13	3248	\N	12
26	15824	\N	25
27	29056	\N	34
28	39912	\N	35
3	12635	\N	36
5	25255	\N	36
1	6830	%.at	5
29	43848	\N	37
30	49808	\N	51
63	50226	\N	52
\.


--
-- Name: as2provider_uid_seq; Type: SEQUENCE SET; Schema: public; Owner: rmbt
--

SELECT pg_catalog.setval('as2provider_uid_seq', 95, true);


--
-- Data for Name: client_type; Type: TABLE DATA; Schema: public; Owner: rmbt
--

COPY client_type (uid, name) FROM stdin;
1	DESKTOP
2	MOBILE
\.


--
-- Name: client_type_uid_seq; Type: SEQUENCE SET; Schema: public; Owner: rmbt
--

SELECT pg_catalog.setval('client_type_uid_seq', 2, true);


--
-- Data for Name: device_map; Type: TABLE DATA; Schema: public; Owner: rmbt
--

COPY device_map (uid, codename, fullname, source, "timestamp") FROM stdin;
141	GT-N7000	Galaxy Note	manual	2012-10-30 12:27:55.955292+01
\.


--
-- Data for Name: mcc2country; Type: TABLE DATA; Schema: public; Owner: rmbt
--

COPY mcc2country (mcc, country) FROM stdin;
202	gr
204	nl
206	be
208	fr
213	ad
214	es
216	hu
218	ba
219	hr
220	rs
222	it
226	ro
228	ch
230	cz
231	sk
232	at
234	gb
235	gb
238	dk
240	se
242	no
244	fi
246	lt
247	lv
248	ee
250	ru
255	ua
257	by
259	md
260	pl
262	de
266	gi
268	pt
270	lu
272	ie
274	is
276	al
278	mt
280	cy
282	ge
284	bg
286	tr
288	fo
290	gl
292	sm
293	si
294	mk
295	li
297	me
302	ca
308	pm
310	us
311	us
312	us
313	us
316	us
334	mx
338	jm
340	gp
342	bb
344	ag
346	ky
348	vg
350	bm
352	gd
354	ms
356	kn
358	lc
360	vc
362	cw
363	aw
365	ai
366	dm
368	cu
370	do
372	ht
374	tt
376	tc
400	az
401	kz
402	bt
404	in
405	in
410	pk
412	af
413	lk
414	mm
415	lb
416	jo
417	sy
418	iq
419	kw
420	sa
421	ye
422	om
424	ae
425	il
426	bh
427	qa
428	mn
429	np
432	ir
434	uz
436	tj
437	kg
438	tm
440	jp
441	jp
450	kr
452	vn
454	hk
455	mo
456	kh
457	la
460	cn
470	bd
472	mv
502	my
505	au
510	id
514	tl
515	ph
520	th
525	sg
528	bn
530	nz
537	pg
539	to
540	sb
541	vu
542	fj
546	nc
547	pf
548	ck
549	ws
550	fm
552	pw
553	tv
555	nu
602	eg
603	dz
604	ma
605	tn
607	gm
608	sn
609	mr
610	ml
611	gn
612	ci
613	bf
614	ne
615	tg
616	bj
617	mu
618	lr
619	sl
620	gh
621	ng
622	td
623	cf
624	cm
625	cv
626	st
627	gq
628	ga
629	cg
630	cd
631	ao
632	gw
633	sc
634	sd
635	rw
636	et
637	so
638	dj
639	ke
640	tz
641	ug
642	bi
643	mz
645	zm
646	mg
648	zw
649	na
650	mw
651	ls
652	bw
653	sz
654	km
655	za
659	ss
702	bz
704	gt
706	sv
708	hn
710	ni
712	cr
714	pa
716	pe
722	ar
724	br
730	cl
732	co
734	ve
736	bo
738	gy
740	ec
744	py
746	sr
748	uy
750	fk
\.


--
-- Data for Name: mccmnc2name; Type: TABLE DATA; Schema: public; Owner: rmbt
--

COPY mccmnc2name (uid, mccmnc, valid_from, valid_to, country, name, shortname, use_for_sim, use_for_network, mcc_mnc_network_mapping, comment, mapped_uid) FROM stdin;
1614	412-01	0001-01-01	9999-12-31	af	AWCC	AWCC	t	t	\N	\N	\N
1615	412-20	0001-01-01	9999-12-31	af	Roshan	Roshan	t	t	\N	\N	\N
1616	412-30	0001-01-01	9999-12-31	af	New1	\N	t	t	\N	\N	\N
1617	412-40	0001-01-01	9999-12-31	af	Areeba Afghanistan	MTN	t	t	\N	\N	\N
1618	412-88	0001-01-01	9999-12-31	af	Afghan Telecom	\N	t	t	\N	\N	\N
1619	276-01	0001-01-01	9999-12-31	al	Albanian Mobile Communications (AMC)	AMS	t	t	\N	\N	\N
1621	276-03	0001-01-01	9999-12-31	al	Eagle Mobile	Eagle Mobile	t	t	\N	\N	\N
1622	276-04	0001-01-01	9999-12-31	al	Mobile 4 AL	Plus Communication	t	t	\N	\N	\N
1623	603-01	0001-01-01	9999-12-31	dz	Algérie Telecom	Mobilis	t	t	\N	\N	\N
1624	603-02	0001-01-01	9999-12-31	dz	Orascom Telecom Algérie	Djezzy	t	t	\N	\N	\N
1625	213-03	0001-01-01	9999-12-31	ad	Mobiland	Mobiland	t	t	\N	\N	\N
1626	631-02	0001-01-01	9999-12-31	ao	Unitel	Unitel	t	t	\N	\N	\N
1627	631-04	0001-01-01	9999-12-31	ao	Movicel	Movicel	t	t	\N	\N	\N
1628	365-010	0001-01-01	9999-12-31	ai	Weblinks Limited	\N	t	t	\N	\N	\N
1629	365-840	0001-01-01	9999-12-31	ai	Cable and Wireless (Anguilla) Ltd trading as Lime	Cable & Wireless	t	t	\N	\N	\N
1630	344-030	0001-01-01	9999-12-31	ag	APUA PCS	\N	t	t	\N	\N	\N
1631	344-920	0001-01-01	9999-12-31	ag	Cable & Wireless (Antigua) trading as Lime	Lime	t	t	\N	\N	\N
1632	344-930	0001-01-01	9999-12-31	ag	AT&T Wireless (Antigua)	\N	t	t	\N	\N	\N
1633	722-010	0001-01-01	9999-12-31	ar	Compañia de Radiocomunicaciones Moviles S.A.	\N	t	t	\N	\N	\N
1634	722-020	0001-01-01	9999-12-31	ar	Nextel Argentina srl	\N	t	t	\N	\N	\N
1635	722-070	0001-01-01	9999-12-31	ar	Telefónica Comunicaciones Personales S.A.	\N	t	t	\N	\N	\N
1636	722-310	0001-01-01	9999-12-31	ar	CTI PCS S.A.	Claro	t	t	\N	\N	\N
1637	722-320	0001-01-01	9999-12-31	ar	Compañia de Telefonos del Interior Norte S.A.	Claro	t	t	\N	\N	\N
1638	722-330	0001-01-01	9999-12-31	ar	Compañia de Telefonos del Interior S.A.	Claro	t	t	\N	\N	\N
1639	722-341	0001-01-01	9999-12-31	ar	Telecom Personal S.A.	Personal	t	t	\N	\N	\N
1640	363-01	0001-01-01	9999-12-31	aw	Setar GSM	SETAR	t	t	\N	\N	\N
1641	505-01	0001-01-01	9999-12-31	au	Telstra Corporation Ltd.	Telstra	t	t	\N	\N	\N
1642	505-02	0001-01-01	9999-12-31	au	Optus Mobile Pty. Ltd.	Optus	t	t	\N	\N	\N
1644	505-04	0001-01-01	9999-12-31	au	Department of Defence	Department of Defence	t	t	\N	\N	\N
1645	505-05	0001-01-01	9999-12-31	au	The Ozitel Network Pty. Ltd.	Ozitel	t	t	\N	\N	\N
1646	505-06	0001-01-01	9999-12-31	au	Hutchison 3G Australia Pty. Ltd.	Hi3G	t	t	\N	\N	\N
1647	505-07	0001-01-01	9999-12-31	au	Vodafone Network Pty. Ltd.	\N	t	t	\N	\N	\N
1648	505-08	0001-01-01	9999-12-31	au	One.Tel GSM 1800 Pty. Ltd.	One.Tel	t	t	\N	\N	\N
1649	505-09	0001-01-01	9999-12-31	au	Airnet Commercial Australia Ltd.	Airnet	t	t	\N	\N	\N
1650	505-10	0001-01-01	9999-12-31	au	Norfolk Telecom	\N	t	t	\N	\N	\N
1651	505-11	0001-01-01	9999-12-31	au	Telstra Corporation Ltd.	Telstra Corporation Ltd.	t	t	\N	\N	\N
1652	505-12	0001-01-01	9999-12-31	au	Hutchison Telecommunications (Australia) Pty. Ltd.	Hi3G	t	t	\N	\N	\N
1653	505-13	0001-01-01	9999-12-31	au	Railcorp	Railcorp	t	t	\N	\N	\N
1654	505-14	0001-01-01	9999-12-31	au	AAPT Ltd.	AAPT	t	t	\N	\N	\N
1655	505-15	0001-01-01	9999-12-31	au	3GIS Pty Ltd. (Telstra & Hutchison 3G)	3GIS	t	t	\N	\N	\N
1656	505-16	0001-01-01	9999-12-31	au	Victorian Rail Track	\N	t	t	\N	\N	\N
1657	505-17	0001-01-01	9999-12-31	au	Vivid Wireless Pty Ltd	\N	t	t	\N	\N	\N
1658	505-18	0001-01-01	9999-12-31	au	Pactel International Pty Ltd	\N	t	t	\N	\N	\N
1659	505-19	0001-01-01	9999-12-31	au	Lycamobile Pty Ltd	\N	t	t	\N	\N	\N
1660	505-20	0001-01-01	9999-12-31	au	Ausgrid Corporation	\N	t	t	\N	\N	\N
1661	505-21	0001-01-01	9999-12-31	au	Queensland Rail Limited	SOUL	t	t	\N	\N	\N
1662	505-22	0001-01-01	9999-12-31	au	iiNet Ltd	\N	t	t	\N	\N	\N
1663	505-23	0001-01-01	9999-12-31	au	Challenge Networks Pty Ltd	\N	t	t	\N	\N	\N
1664	505-24	0001-01-01	9999-12-31	au	Advanced Communications Technologies Pty. Ltd.	Advanced Communications Technologies Pty. Ltd.	t	t	\N	\N	\N
1665	505-25	0001-01-01	9999-12-31	au	Pilbara Iron Company Services Pty Ltd	\N	t	t	\N	\N	\N
1666	505-26	0001-01-01	9999-12-31	au	Dialogue Communications Pty Ltd	\N	t	t	\N	\N	\N
1667	505-27	0001-01-01	9999-12-31	au	Nexium Telecommunications	\N	t	t	\N	\N	\N
1668	505-62	0001-01-01	9999-12-31	au	NBNCo Limited	\N	t	t	\N	\N	\N
1669	505-68	0001-01-01	9999-12-31	au	NBNCo Limited	\N	t	t	\N	\N	\N
1670	505-71	0001-01-01	9999-12-31	au	Telstra Corporation Ltd.	Telstra	t	t	\N	\N	\N
1671	505-72	0001-01-01	9999-12-31	au	Telstra Corporation Ltd.	Telstra	t	t	\N	\N	\N
1672	505-88	0001-01-01	9999-12-31	au	Localstar Holding Pty. Ltd.	Localstar Holding Pty. Ltd.	t	t	\N	\N	\N
1673	505-90	0001-01-01	9999-12-31	au	Optus Ltd.	Optus	t	t	\N	\N	\N
1674	505-99	0001-01-01	9999-12-31	au	One.Tel GSM 1800 Pty. Ltd.	One.Tel GSM 1800 Pty. Ltd.	t	t	\N	\N	\N
1675	400-01	0001-01-01	9999-12-31	az	Azercell Limited Liability Joint Venture	Azercell	t	t	\N	\N	\N
1676	400-02	0001-01-01	9999-12-31	az	Bakcell Limited Liabil;ity Company	Bakcell	t	t	\N	\N	\N
1677	400-03	0001-01-01	9999-12-31	az	Catel JV	FONEX	t	t	\N	\N	\N
1678	400-04	0001-01-01	9999-12-31	az	Azerphone LLC	Nar Mobile (Azerfon)	t	t	\N	\N	\N
1679	426-01	0001-01-01	9999-12-31	bh	BATELCO	Batelco	t	t	\N	\N	\N
1680	426-02	0001-01-01	9999-12-31	bh	Zain Bahrain	Zain BH	t	t	\N	\N	\N
1681	426-03	0001-01-01	9999-12-31	bh	Civil Aviation Authority	\N	t	t	\N	\N	\N
1682	426-04	0001-01-01	9999-12-31	bh	STC Bahrain	Viva	t	t	\N	\N	\N
1683	426-05	0001-01-01	9999-12-31	bh	Royal Court	\N	t	t	\N	\N	\N
1684	470-01	0001-01-01	9999-12-31	bd	GramenPhone	GramenPhone	t	t	\N	\N	\N
1685	470-02	0001-01-01	9999-12-31	bd	Aktel	Robi	t	t	\N	\N	\N
1686	470-03	0001-01-01	9999-12-31	bd	Mobile 2000	Banglalink	t	t	\N	\N	\N
1687	342-600	0001-01-01	9999-12-31	bb	Cable & Wireless (Barbados) Ltd. trading as Lime	Lime (Cable & Wireless)	t	t	\N	\N	\N
1688	342-820	0001-01-01	9999-12-31	bb	Sunbeach Communications	Sunbeach Communications	t	t	\N	\N	\N
1689	257-01	0001-01-01	9999-12-31	by	MDC Velcom	Velcom	t	t	\N	\N	\N
1690	257-02	0001-01-01	9999-12-31	by	MTS	MTS	t	t	\N	\N	\N
1691	257-03	0001-01-01	9999-12-31	by	BelCel Joint Venture (JV)	DIALLOG	t	t	\N	\N	\N
1692	257-04	0001-01-01	9999-12-31	by	Closed joint-stock company "Belarusian telecommunication network"	life :)	t	t	\N	\N	\N
1693	257-05	0001-01-01	9999-12-31	by	Republican Unitary Telecommunication Enterprise (RUE) Beltelecom (National Telecommunications Operat	\N	t	t	\N	\N	\N
1694	257-06	0001-01-01	9999-12-31	by	Yota Bel Foreign Limited Liability Company (FLLC)	\N	t	t	\N	\N	\N
1695	206-01	0001-01-01	9999-12-31	be	Proximus	Proximus	t	t	\N	\N	\N
1696	206-10	0001-01-01	9999-12-31	be	Mobistar	Mobistar	t	t	\N	\N	\N
1697	206-20	0001-01-01	9999-12-31	be	Base	Base	t	t	\N	\N	\N
1698	702-67	0001-01-01	9999-12-31	bz	Belize Telecommunications Ltd., GSM 1900	DigiCell	t	t	\N	\N	\N
1699	702-68	0001-01-01	9999-12-31	bz	International Telecommunications Ltd. (INTELCO)	IntelCo	t	t	\N	\N	\N
1700	616-01	0001-01-01	9999-12-31	bj	Libercom	Libercom	t	t	\N	\N	\N
1701	616-02	0001-01-01	9999-12-31	bj	Telecel	Moov	t	t	\N	\N	\N
1702	616-03	0001-01-01	9999-12-31	bj	Spacetel Benin	MTN	t	t	\N	\N	\N
1703	350-000	0001-01-01	9999-12-31	bm	Bermuda Digital Communications Ltd (CellOne)	\N	t	t	\N	\N	\N
1704	402-11	0001-01-01	9999-12-31	bt	Bhutan Telecom Ltd	B-Mobile	t	t	\N	\N	\N
1705	402-17	0001-01-01	9999-12-31	bt	B-Mobile of Bhutan Telecom	B-Mobile of Bhutan Telecom	t	t	\N	\N	\N
1706	736-01	0001-01-01	9999-12-31	bo	Nuevatel S.A.	Nuevatel	t	t	\N	\N	\N
1707	736-02	0001-01-01	9999-12-31	bo	ENTEL S.A.	Entel	t	t	\N	\N	\N
1708	736-03	0001-01-01	9999-12-31	bo	Telecel S.A.	Tigo	t	t	\N	\N	\N
1709	218-03	0001-01-01	9999-12-31	ba	Eronet Mobile Communications Ltd.	HT-Eronet	t	t	\N	\N	\N
1710	218-05	0001-01-01	9999-12-31	ba	MOBIS (Mobilina Srpske)	m:tel	t	t	\N	\N	\N
1711	218-90	0001-01-01	9999-12-31	ba	GSMBIH	BH Mobile	t	t	\N	\N	\N
1712	652-01	0001-01-01	9999-12-31	bw	Mascom Wireless (Pty) Ltd.	Mascom	t	t	\N	\N	\N
1714	652-04	0001-01-01	9999-12-31	bw	Botswana Telecommunications Corporation (BTC)	BTC Mobile	t	t	\N	\N	\N
1715	724-00	0001-01-01	9999-12-31	br	NEXTEL	Nextel	t	t	\N	\N	\N
1716	724-01	0001-01-01	9999-12-31	br	SISTEER DO BRASIL TELECOMUNICAÇÔES (MVNO)	CRT Cellular	t	t	\N	\N	\N
1717	724-02	0001-01-01	9999-12-31	br	TIM REGIÂO I	TIM	t	t	\N	\N	\N
1718	724-03	0001-01-01	9999-12-31	br	TIM REGIÂO III	TIM	t	t	\N	\N	\N
1719	724-04	0001-01-01	9999-12-31	br	TIM REGIÂO III	TIM	t	t	\N	\N	\N
1720	724-05	0001-01-01	9999-12-31	br	CLARO	Claro	t	t	\N	\N	\N
1721	724-06	0001-01-01	9999-12-31	br	VIVO REGIÂO II	Vivo	t	t	\N	\N	\N
1722	724-10	0001-01-01	9999-12-31	br	VIVO REGIÂO III	Vivo	t	t	\N	\N	\N
1723	724-11	0001-01-01	9999-12-31	br	VIVO REGIÂO I	Vivo	t	t	\N	\N	\N
1724	724-15	0001-01-01	9999-12-31	br	SERCOMTEL	Sercomtel	t	t	\N	\N	\N
1725	724-16	0001-01-01	9999-12-31	br	BRT CELULAR	Oi	t	t	\N	\N	\N
1726	724-18	0001-01-01	9999-12-31	br	DATORA (MVNO)	Norte Brasil Tel	t	t	\N	\N	\N
1727	724-23	0001-01-01	9999-12-31	br	TELEMIG CELULAR	Oi	t	t	\N	\N	\N
1728	724-24	0001-01-01	9999-12-31	br	AMAZONIA CELULAR	Oi / Brasil Telecom	t	t	\N	\N	\N
1729	724-30	0001-01-01	9999-12-31	br	TNL PCS Oi	\N	t	t	\N	\N	\N
1730	724-31	0001-01-01	9999-12-31	br	TNL PCS Oi	Oi	t	t	\N	\N	\N
1731	724-32	0001-01-01	9999-12-31	br	CTBC CELULAR R III	CTBC Celular	t	t	\N	\N	\N
1732	724-33	0001-01-01	9999-12-31	br	CTBC CELULAR R II	CTBC Celular	t	t	\N	\N	\N
1733	724-34	0001-01-01	9999-12-31	br	CTBC CELULAR R I	CTBC Celular	t	t	\N	\N	\N
1734	724-35	0001-01-01	9999-12-31	br	TELCOM	Telebahia Cel	t	t	\N	\N	\N
1735	724-36	0001-01-01	9999-12-31	br	OPTIONS	\N	t	t	\N	\N	\N
1736	724-37	0001-01-01	9999-12-31	br	UNICEL	Aeiou	t	t	\N	\N	\N
1737	724-38	0001-01-01	9999-12-31	br	CLARO	\N	t	t	\N	\N	\N
1738	724-39	0001-01-01	9999-12-31	br	NEXTEL (SMP)	Nextel	t	t	\N	\N	\N
1739	724-54	0001-01-01	9999-12-31	br	PORTO SEGURO TELECOMUNICAÇÔES (MVNO)	\N	t	t	\N	\N	\N
1740	724-99	0001-01-01	9999-12-31	br	LOCAL (STFC)	\N	t	t	\N	\N	\N
1741	348-170	0001-01-01	9999-12-31	vg	Cable & Wireless (BVI) Ltd trading as lime	Cabel & Wireless	t	t	\N	\N	\N
1742	348-370	0001-01-01	9999-12-31	vg	BVI Cable TV Ltd	\N	t	t	\N	\N	\N
1743	348-570	0001-01-01	9999-12-31	vg	Caribbean Cellular Telephone Ltd.	CCT Boatphone	t	t	\N	\N	\N
1744	348-770	0001-01-01	9999-12-31	vg	Digicel (BVI) Ltd	Digicel	t	t	\N	\N	\N
1745	528-11	0001-01-01	9999-12-31	bn	DST Com	DSTCom	t	t	\N	\N	\N
1746	284-01	0001-01-01	9999-12-31	bg	Mobiltel EAD	M-Tel	t	t	\N	\N	\N
1747	284-05	0001-01-01	9999-12-31	bg	Globul	GLOBUL	t	t	\N	\N	\N
1748	613-02	0001-01-01	9999-12-31	bf	Celtel	Zain	t	t	\N	\N	\N
1749	613-03	0001-01-01	9999-12-31	bf	Telecel	Telcel Faso	t	t	\N	\N	\N
1750	642-01	0001-01-01	9999-12-31	bi	Econet	Spacetel	t	t	\N	\N	\N
1751	642-02	0001-01-01	9999-12-31	bi	Africell	Africell	t	t	\N	\N	\N
1752	642-03	0001-01-01	9999-12-31	bi	ONAMOB	Onatel	t	t	\N	\N	\N
1753	642-07	0001-01-01	9999-12-31	bi	LACELL	Smart Mobile	t	t	\N	\N	\N
1754	642-08	0001-01-01	9999-12-31	bi	HITS TELECOM	HiTs Telecom	t	t	\N	\N	\N
1755	642-82	0001-01-01	9999-12-31	bi	U.COM	U-COM Burundi	t	t	\N	\N	\N
1756	456-01	0001-01-01	9999-12-31	kh	Mobitel (Cam GSM)	Mobitel	t	t	\N	\N	\N
1757	456-02	0001-01-01	9999-12-31	kh	Hello	Hello	t	t	\N	\N	\N
1758	456-03	0001-01-01	9999-12-31	kh	S Telecom (CDMA)	S Telecom	t	t	\N	\N	\N
1759	456-04	0001-01-01	9999-12-31	kh	Cadcomms	QB	t	t	\N	\N	\N
1760	456-05	0001-01-01	9999-12-31	kh	Starcell	Star-Cell	t	t	\N	\N	\N
1761	456-06	0001-01-01	9999-12-31	kh	Smart	Smart Mobile	t	t	\N	\N	\N
1762	456-08	0001-01-01	9999-12-31	kh	Viettel	Mefone	t	t	\N	\N	\N
1763	456-18	0001-01-01	9999-12-31	kh	Mfone	Mfone	t	t	\N	\N	\N
1764	624-01	0001-01-01	9999-12-31	cm	Mobile Telephone Networks Cameroon	MTN Cameroon	t	t	\N	\N	\N
1766	302-220	0001-01-01	9999-12-31	ca	Telus Mobility	Telus	t	t	\N	\N	\N
1767	302-221	0001-01-01	9999-12-31	ca	Telus Mobility	Telus	t	t	\N	\N	\N
1768	302-222	0001-01-01	9999-12-31	ca	Telus Mobility	\N	t	t	\N	\N	\N
1769	302-250	0001-01-01	9999-12-31	ca	ALO Mobile Inc	\N	t	t	\N	\N	\N
1770	302-270	0001-01-01	9999-12-31	ca	Bragg Communications	\N	t	t	\N	\N	\N
1771	302-290	0001-01-01	9999-12-31	ca	Airtel Wireless	Aurtek Wurekess	t	t	\N	\N	\N
1772	302-320	0001-01-01	9999-12-31	ca	Dave Wireless	Mobilicity	t	t	\N	\N	\N
1773	302-340	0001-01-01	9999-12-31	ca	Execulink	\N	t	t	\N	\N	\N
1774	302-360	0001-01-01	9999-12-31	ca	Telus Mobility	MiKE	t	t	\N	\N	\N
1775	302-370	0001-01-01	9999-12-31	ca	Microcell	Fido	t	t	\N	\N	\N
1776	302-380	0001-01-01	9999-12-31	ca	Dryden Mobility	DMTS	t	t	\N	\N	\N
1777	302-390	0001-01-01	9999-12-31	ca	Dryden Mobility	\N	t	t	\N	\N	\N
1778	302-490	0001-01-01	9999-12-31	ca	Globalive Wireless	WIND Mobile	t	t	\N	\N	\N
1779	302-500	0001-01-01	9999-12-31	ca	Videotron Ltd	Videotron	t	t	\N	\N	\N
1780	302-510	0001-01-01	9999-12-31	ca	Videotron Ltd	Videotron	t	t	\N	\N	\N
1781	302-530	0001-01-01	9999-12-31	ca	Keewatinook Okimacinac	\N	t	t	\N	\N	\N
1782	302-560	0001-01-01	9999-12-31	ca	Lynx Mobility	\N	t	t	\N	\N	\N
1783	302-570	0001-01-01	9999-12-31	ca	Light Squared	\N	t	t	\N	\N	\N
1784	302-590	0001-01-01	9999-12-31	ca	Quadro Communication	\N	t	t	\N	\N	\N
1785	302-610	0001-01-01	9999-12-31	ca	Bell Mobility	Bell	t	t	\N	\N	\N
1786	302-620	0001-01-01	9999-12-31	ca	Ice Wireless	ICE Wireless	t	t	\N	\N	\N
1787	302-630	0001-01-01	9999-12-31	ca	Aliant Mobility	\N	t	t	\N	\N	\N
1788	302-640	0001-01-01	9999-12-31	ca	Bell Mobility	Bell	t	t	\N	\N	\N
1789	302-656	0001-01-01	9999-12-31	ca	Tbay Mobility	TBay	t	t	\N	\N	\N
1790	302-660	0001-01-01	9999-12-31	ca	MTS Mobility	\N	t	t	\N	\N	\N
1791	302-670	0001-01-01	9999-12-31	ca	CityTel Mobility	\N	t	t	\N	\N	\N
1792	302-680	0001-01-01	9999-12-31	ca	Sask Tel Mobility	SaskTel	t	t	\N	\N	\N
1793	302-690	0001-01-01	9999-12-31	ca	Bell Mobility	\N	t	t	\N	\N	\N
1794	302-710	0001-01-01	9999-12-31	ca	Globalstar	Globalstar	t	t	\N	\N	\N
1795	302-720	0001-01-01	9999-12-31	ca	Rogers Wireless	Rogers Wireless	t	t	\N	\N	\N
1796	302-730	0001-01-01	9999-12-31	ca	TerreStar Solutions	\N	t	t	\N	\N	\N
1797	302-740	0001-01-01	9999-12-31	ca	Shaw Telecom G.P.	\N	t	t	\N	\N	\N
1798	302-760	0001-01-01	9999-12-31	ca	Public Mobile Inc	\N	t	t	\N	\N	\N
1799	302-770	0001-01-01	9999-12-31	ca	Rural Com	\N	t	t	\N	\N	\N
1800	302-780	0001-01-01	9999-12-31	ca	Sask Tel Mobility	SaskTel	t	t	\N	\N	\N
1801	302-860	0001-01-01	9999-12-31	ca	Telus Mobility	\N	t	t	\N	\N	\N
1802	302-880	0001-01-01	9999-12-31	ca	Telus/Bell shared	Bell / Telus / SaskTel	t	t	\N	\N	\N
1803	302-940	0001-01-01	9999-12-31	ca	Wightman Telecom	\N	t	t	\N	\N	\N
1804	302-990	0001-01-01	9999-12-31	ca	Test	\N	t	t	\N	\N	\N
1805	625-01	0001-01-01	9999-12-31	cv	Cabo Verde Telecom	CVMOVEL	t	t	\N	\N	\N
1806	625-02	0001-01-01	9999-12-31	cv	T+Telecomunicaçôes	T+	t	t	\N	\N	\N
1807	346-140	0001-01-01	9999-12-31	ky	Cable & Wireless (Cayman) trading as Lime	Cable & Wireless (Lime)	t	t	\N	\N	\N
1808	623-01	0001-01-01	9999-12-31	cf	Centrafrique Telecom Plus (CTP)	MOOV	t	t	\N	\N	\N
1809	623-02	0001-01-01	9999-12-31	cf	Telecel Centrafrique (TC)	TC	t	t	\N	\N	\N
1811	622-01	0001-01-01	9999-12-31	td	Celtel	Zain	t	t	\N	\N	\N
1812	622-02	0001-01-01	9999-12-31	td	Tchad Mobile	Tawali	t	t	\N	\N	\N
1813	730-01	0001-01-01	9999-12-31	cl	Entel Telefónica Móvil	entel	t	t	\N	\N	\N
1814	730-02	0001-01-01	9999-12-31	cl	Telefónica Móvil	movistar	t	t	\N	\N	\N
1815	730-03	0001-01-01	9999-12-31	cl	Smartcom	Claro	t	t	\N	\N	\N
1816	730-04	0001-01-01	9999-12-31	cl	Centennial Cayman Corp. Chile S.A.	Nextel	t	t	\N	\N	\N
1817	730-05	0001-01-01	9999-12-31	cl	Multikom S.A.	VTR Móvil	t	t	\N	\N	\N
1818	730-06	0001-01-01	9999-12-31	cl	Blue Two Chile SA	\N	t	t	\N	\N	\N
1819	730-07	0001-01-01	9999-12-31	cl	Telefónica Móviles Chile S.A.	\N	t	t	\N	\N	\N
1820	730-08	0001-01-01	9999-12-31	cl	VTR Móvil S.A.	\N	t	t	\N	\N	\N
1821	730-09	0001-01-01	9999-12-31	cl	Centennial Cayman Corp. Chile S.A.	Nextel	t	t	\N	\N	\N
1822	730-10	0001-01-01	9999-12-31	cl	Entel	entel	t	t	\N	\N	\N
1823	730-11	0001-01-01	9999-12-31	cl	Celupago S.A.	\N	t	t	\N	\N	\N
1824	730-12	0001-01-01	9999-12-31	cl	Telestar Móvil S.A.	\N	t	t	\N	\N	\N
1825	730-13	0001-01-01	9999-12-31	cl	TRIBE Mobile Chile SPA	\N	t	t	\N	\N	\N
1826	730-14	0001-01-01	9999-12-31	cl	Netline Telefónica Móvil Ltda	\N	t	t	\N	\N	\N
1827	460-00	0001-01-01	9999-12-31	cn	China Mobile	China Mobile	t	t	\N	\N	\N
1828	460-01	0001-01-01	9999-12-31	cn	China Unicom	China Unicom	t	t	\N	\N	\N
1829	460-03	0001-01-01	9999-12-31	cn	China Unicom CDMA	China Unicom CDMA	t	t	\N	\N	\N
1830	460-04	0001-01-01	9999-12-31	cn	China Satellite Global Star Network	China Satellite Global Star Network	t	t	\N	\N	\N
1831	732-001	0001-01-01	9999-12-31	co	Colombia Telecomunicaciones S.A. - Telecom	\N	t	t	\N	\N	\N
1832	732-002	0001-01-01	9999-12-31	co	Edatel S.A.	\N	t	t	\N	\N	\N
1833	732-020	0001-01-01	9999-12-31	co	Emtelsa	\N	t	t	\N	\N	\N
1834	732-099	0001-01-01	9999-12-31	co	Emcali	\N	t	t	\N	\N	\N
1835	732-101	0001-01-01	9999-12-31	co	Comcel S.A. Occel S.A./Celcaribe	Comcel	t	t	\N	\N	\N
1836	732-102	0001-01-01	9999-12-31	co	Bellsouth Colombia S.A.	Movistar	t	t	\N	\N	\N
1837	732-103	0001-01-01	9999-12-31	co	Colombia Móvil S.A.	Tigo	t	t	\N	\N	\N
1838	732-111	0001-01-01	9999-12-31	co	Colombia Móvil S.A.	Tigo	t	t	\N	\N	\N
1839	732-123	0001-01-01	9999-12-31	co	Telefónica Móviles Colombia S.A.	Movistar	t	t	\N	\N	\N
1840	732-130	0001-01-01	9999-12-31	co	Avantel	Avantel	t	t	\N	\N	\N
1841	654-01	0001-01-01	9999-12-31	km	HURI - SNPT	HURI - SNPT	t	t	\N	\N	\N
1842	629-01	0001-01-01	9999-12-31	cg	Celtel	Zain	t	t	\N	\N	\N
1843	629-10	0001-01-01	9999-12-31	cg	Libertis Telecom	Libertis Telecom	t	t	\N	\N	\N
1844	548-01	0001-01-01	9999-12-31	ck	Telecom Cook	Telecom Cook	t	t	\N	\N	\N
1845	712-01	0001-01-01	9999-12-31	cr	Instituto Costarricense de Electricidad - ICE	ICE	t	t	\N	\N	\N
1846	712-02	0001-01-01	9999-12-31	cr	Instituto Costarricense de Electricidad - ICE	ICE	t	t	\N	\N	\N
1847	712-03	0001-01-01	9999-12-31	cr	CLARO CR Telecomunicaciones S.A.	ICE	t	t	\N	\N	\N
1848	712-04	0001-01-01	9999-12-31	cr	Telefónica de Costa Rica TC, S.A.	\N	t	t	\N	\N	\N
1849	712-20	0001-01-01	9999-12-31	cr	Virtualis	\N	t	t	\N	\N	\N
1850	612-02	0001-01-01	9999-12-31	ci	Atlantique Cellulaire	Moov	t	t	\N	\N	\N
1852	612-04	0001-01-01	9999-12-31	ci	Comium Côte dIvoire	Koz	t	t	\N	\N	\N
1853	612-05	0001-01-01	9999-12-31	ci	Loteny Telecom	MTN	t	t	\N	\N	\N
1854	612-06	0001-01-01	9999-12-31	ci	Oricel Côte dIvoire	OriCel	t	t	\N	\N	\N
1855	612-07	0001-01-01	9999-12-31	ci	Aircomm Côte dIvoire	\N	t	t	\N	\N	\N
1857	219-02	0001-01-01	9999-12-31	hr	Tele2/Tele2 d.o.o.	Tele2	t	t	\N	\N	\N
1858	219-10	0001-01-01	9999-12-31	hr	VIPnet/VIPnet d.o.o.	VIPnet	t	t	\N	\N	\N
1859	368-01	0001-01-01	9999-12-31	cu	ETECSA	Cubacel	t	t	\N	\N	\N
1860	362-51	0001-01-01	9999-12-31	cw	TELCELL GSM	TelCell	t	t	\N	\N	\N
1861	362-69	0001-01-01	9999-12-31	cw	CT GSM	Digicel	t	t	\N	\N	\N
1862	362-91	0001-01-01	9999-12-31	cw	SETEL GSM	UTS	t	t	\N	\N	\N
1863	280-01	0001-01-01	9999-12-31	cy	CYTA	Cytamobile-Vodafone	t	t	\N	\N	\N
1864	280-10	0001-01-01	9999-12-31	cy	Scancom (Cyprus) Ltd.	MTN	t	t	\N	\N	\N
1865	280-20	0001-01-01	9999-12-31	cy	Primetel PLC	Primetel	t	t	\N	\N	\N
1866	280-22	0001-01-01	9999-12-31	cy	Lemontel Ltd	\N	t	t	\N	\N	\N
1868	230-02	0001-01-01	9999-12-31	cz	Telefónica O2 Czech Republic a.s.	O2	t	t	\N	\N	\N
1870	230-04	0001-01-01	9999-12-31	cz	Mobilkom a.s.	U:fon	t	t	\N	\N	\N
1871	230-05	0001-01-01	9999-12-31	cz	Travel Telekommunikation, s.r.o.	Travel Telecomunication s.r.o.	t	t	\N	\N	\N
1872	230-08	0001-01-01	9999-12-31	cz	Compatel s.r.o	\N	t	t	\N	\N	\N
1873	230-98	0001-01-01	9999-12-31	cz	Sprava Zeleznicni Dopravni Cesty	SŽDC s.o.	t	t	\N	\N	\N
1874	630-01	0001-01-01	9999-12-31	cd	Vodacom Congo RDC sprl	Vodacom	t	t	\N	\N	\N
1875	630-02	0001-01-01	9999-12-31	cd	AIRTEL sprl	Zain	t	t	\N	\N	\N
1876	630-05	0001-01-01	9999-12-31	cd	Supercell Sprl	Supercell	t	t	\N	\N	\N
1877	630-86	0001-01-01	9999-12-31	cd	Congo-Chine Telecom s.a.r.l.	CCT	t	t	\N	\N	\N
1878	630-88	0001-01-01	9999-12-31	cd	YOZMA TIMETURNS sprl	Yozma Timeturns	t	t	\N	\N	\N
1879	630-89	0001-01-01	9999-12-31	cd	OASIS sprl	SAIT Telecom	t	t	\N	\N	\N
1880	630-90	0001-01-01	9999-12-31	cd	Africell RDC	\N	t	t	\N	\N	\N
1869	230-03	0001-01-01	9999-12-31	cz	Vodafone Czech Republic a.s.	Vodafone cz	t	t	\N	\N	\N
1810	623-03	0001-01-01	9999-12-31	cf	Celca (Socatel)	Orange cf	t	t	\N	\N	\N
1851	612-03	0001-01-01	9999-12-31	ci	Orange Côte dIvoire	Orange ci	t	t	\N	\N	\N
1881	238-01	0001-01-01	9999-12-31	dk	TDC Mobil	TDC	t	t	\N	\N	\N
1882	238-02	0001-01-01	9999-12-31	dk	Sonofon	Telenor	t	t	\N	\N	\N
1883	238-03	0001-01-01	9999-12-31	dk	MIGway A/S	End2End	t	t	\N	\N	\N
1884	238-04	0001-01-01	9999-12-31	dk	NextGen Mobile Ltd T/A CardBoardFish	\N	t	t	\N	\N	\N
1886	238-07	0001-01-01	9999-12-31	dk	Barablu Mobile Ltd.	Mundio Mobile	t	t	\N	\N	\N
1887	238-08	0001-01-01	9999-12-31	dk	Nordisk Mobiltelefon Danmark A/S	Nordisk Mobiltelefon	t	t	\N	\N	\N
1888	238-10	0001-01-01	9999-12-31	dk	TDC Mobil	TDC	t	t	\N	\N	\N
1889	238-12	0001-01-01	9999-12-31	dk	Lycamobile Denmark	Lyca	t	t	\N	\N	\N
1890	238-20	0001-01-01	9999-12-31	dk	Telia	Telia	t	t	\N	\N	\N
1891	238-28	0001-01-01	9999-12-31	dk	CoolTEL	\N	t	t	\N	\N	\N
1892	238-66	0001-01-01	9999-12-31	dk	TT-Netvaerket P/S	\N	t	t	\N	\N	\N
1893	238-77	0001-01-01	9999-12-31	dk	Tele2	Telenor	t	t	\N	\N	\N
1894	638-01	0001-01-01	9999-12-31	dj	Evatis	Evatis	t	t	\N	\N	\N
1895	366-110	0001-01-01	9999-12-31	dm	Cable & Wireless Dominica Ltd trading as Lime	Cable & Wireless Dominica Ltd.	t	t	\N	\N	\N
1897	370-02	0001-01-01	9999-12-31	do	Verizon Dominicana S.A.	Claro	t	t	\N	\N	\N
1898	370-03	0001-01-01	9999-12-31	do	Tricom S.A.	Tricom	t	t	\N	\N	\N
1899	370-04	0001-01-01	9999-12-31	do	CentennialDominicana	Viva	t	t	\N	\N	\N
1900	740-00	0001-01-01	9999-12-31	ec	Otecel S.A. - Bellsouth	Moviestar	t	t	\N	\N	\N
1901	740-01	0001-01-01	9999-12-31	ec	Porta GSM	Porta	t	t	\N	\N	\N
1902	740-02	0001-01-01	9999-12-31	ec	Telecsa S.A.	Alegro	t	t	\N	\N	\N
1903	602-01	0001-01-01	9999-12-31	eg	Mobinil	Mobinil	t	t	\N	\N	\N
1905	602-03	0001-01-01	9999-12-31	eg	Etisalat	Etisalat	t	t	\N	\N	\N
1906	706-01	0001-01-01	9999-12-31	sv	CTE Telecom Personal, S.A. de C.V.	CTW Telecom Personal	t	t	\N	\N	\N
1907	706-02	0001-01-01	9999-12-31	sv	Digicel, S.A. de C.V.	Digicel	t	t	\N	\N	\N
1908	706-03	0001-01-01	9999-12-31	sv	Telemóvil El Salvador, S.A.	Tigo	t	t	\N	\N	\N
1909	627-01	0001-01-01	9999-12-31	gq	Guinea Ecuatorial de Telecomunicaciones Sociedad Anónima (GETESA)	Orange GQ	t	t	\N	\N	\N
1910	248-01	0001-01-01	9999-12-31	ee	EMT GSM	EMT	t	t	\N	\N	\N
1911	248-02	0001-01-01	9999-12-31	ee	RLE	Elisa	t	t	\N	\N	\N
1912	248-03	0001-01-01	9999-12-31	ee	Tele2	Tele 2	t	t	\N	\N	\N
1913	248-04	0001-01-01	9999-12-31	ee	OY Top Connect	OY Top Connect	t	t	\N	\N	\N
1914	248-05	0001-01-01	9999-12-31	ee	AS Bravocom Mobiil	AS Bravocom Mobiil	t	t	\N	\N	\N
1915	248-06	0001-01-01	9999-12-31	ee	ProGroup Holding OY	OY ViaTel (UMTS)	t	t	\N	\N	\N
1916	248-07	0001-01-01	9999-12-31	ee	Televõrgu AS	Televõrgu AS	t	t	\N	\N	\N
1917	248-71	0001-01-01	9999-12-31	ee	Siseministeerium (Ministry of Interior)	Siseministeerium (Ministry of Interior)	t	t	\N	\N	\N
1918	636-01	0001-01-01	9999-12-31	et	ETH MTN	ETH MTN	t	t	\N	\N	\N
1919	750-001	0001-01-01	9999-12-31	fk	Touch	\N	t	t	\N	\N	\N
1920	288-01	0001-01-01	9999-12-31	fo	Faroese Telecom - GSM	Faroese Telecom - GSM	t	t	\N	\N	\N
1922	288-03	0001-01-01	9999-12-31	fo	Edge Mobile Sp/F	\N	t	t	\N	\N	\N
1924	542-02	0001-01-01	9999-12-31	fj	Digicel (Fiji) Ltd	Digicel	t	t	\N	\N	\N
1925	542-03	0001-01-01	9999-12-31	fj	Telecom Fiji Ltd (CDMA)	\N	t	t	\N	\N	\N
1926	244-03	0001-01-01	9999-12-31	fi	DNA Oy	DNA	t	t	\N	\N	\N
1927	244-04	0001-01-01	9999-12-31	fi	DNA Oy	\N	t	t	\N	\N	\N
1928	244-05	0001-01-01	9999-12-31	fi	Elisa Oy	Elisa	t	t	\N	\N	\N
1929	244-09	0001-01-01	9999-12-31	fi	Nokia Siemens Networks Oy	Finnet	t	t	\N	\N	\N
1930	244-10	0001-01-01	9999-12-31	fi	TDC Oy FINLAND	TDC	t	t	\N	\N	\N
1931	244-12	0001-01-01	9999-12-31	fi	DNA Oy	DNA	t	t	\N	\N	\N
1932	244-13	0001-01-01	9999-12-31	fi	DNA Oy	\N	t	t	\N	\N	\N
1933	244-14	0001-01-01	9999-12-31	fi	Alands Mobilteleofn Ab	AMT	t	t	\N	\N	\N
1934	244-16	0001-01-01	9999-12-31	fi	Oy Finland Tele2 AB	Oy Finland Tele2 AB	t	t	\N	\N	\N
1935	244-21	0001-01-01	9999-12-31	fi	Saunalahti Group Oyj	Saunalahti	t	t	\N	\N	\N
1936	244-29	0001-01-01	9999-12-31	fi	SCNL TRUPHONE	Scnl Truphone	t	t	\N	\N	\N
1937	244-91	0001-01-01	9999-12-31	fi	TeliaSonera Finland Oyj	Sonera	t	t	\N	\N	\N
1940	208-03	0001-01-01	9999-12-31	fr	MobiquiThings	\N	t	t	\N	\N	\N
1941	208-04	0001-01-01	9999-12-31	fr	Sisteer	\N	t	t	\N	\N	\N
1942	208-05	0001-01-01	9999-12-31	fr	Globalstar Europe	Globalstar Europe	t	t	\N	\N	\N
1943	208-06	0001-01-01	9999-12-31	fr	Globalstar Europe	Globalstar Europe	t	t	\N	\N	\N
1944	208-07	0001-01-01	9999-12-31	fr	Globalstar Europe	Globalstar Europe	t	t	\N	\N	\N
1945	208-09	0001-01-01	9999-12-31	fr	SFR	\N	t	t	\N	\N	\N
1946	208-10	0001-01-01	9999-12-31	fr	S.F.R.	SFR	t	t	\N	\N	\N
1947	208-11	0001-01-01	9999-12-31	fr	S.F.R.	SFR	t	t	\N	\N	\N
1948	208-13	0001-01-01	9999-12-31	fr	SFR	SFR	t	t	\N	\N	\N
1949	208-14	0001-01-01	9999-12-31	fr	RFF	Free Mobile	t	t	\N	\N	\N
1950	208-15	0001-01-01	9999-12-31	fr	Free Mobile	Free Mobile	t	t	\N	\N	\N
1951	208-20	0001-01-01	9999-12-31	fr	Bouygues Telecom	Bouygues	t	t	\N	\N	\N
1952	208-21	0001-01-01	9999-12-31	fr	Bouygues Telecom	Bouygues	t	t	\N	\N	\N
1953	208-22	0001-01-01	9999-12-31	fr	Transatel	\N	t	t	\N	\N	\N
1954	208-23	0001-01-01	9999-12-31	fr	Omer Telecom Ltd	\N	t	t	\N	\N	\N
1955	208-24	0001-01-01	9999-12-31	fr	Mobiqui Things	\N	t	t	\N	\N	\N
1956	208-25	0001-01-01	9999-12-31	fr	Lycamobile	\N	t	t	\N	\N	\N
1957	208-26	0001-01-01	9999-12-31	fr	NRJ Mobile	\N	t	t	\N	\N	\N
1958	208-27	0001-01-01	9999-12-31	fr	Afone	\N	t	t	\N	\N	\N
1959	208-28	0001-01-01	9999-12-31	fr	Astrium	\N	t	t	\N	\N	\N
1960	208-29	0001-01-01	9999-12-31	fr	Société International Mobile Communication	\N	t	t	\N	\N	\N
1961	208-30	0001-01-01	9999-12-31	fr	Symacom	\N	t	t	\N	\N	\N
1962	208-31	0001-01-01	9999-12-31	fr	Mundio Mobile	\N	t	t	\N	\N	\N
1963	208-88	0001-01-01	9999-12-31	fr	Bouygues Telecom	Bouygues	t	t	\N	\N	\N
1964	208-89	0001-01-01	9999-12-31	fr	Omer Telecom Ltd	\N	t	t	\N	\N	\N
1965	208-90	0001-01-01	9999-12-31	fr	Images & Réseaux	\N	t	t	\N	\N	\N
1966	208-91	0001-01-01	9999-12-31	fr	Orange France	\N	t	t	\N	\N	\N
1967	340-11	0001-01-01	9999-12-31	gf	Guyane Téléphone Mobile	\N	t	t	\N	\N	\N
1968	547-10	0001-01-01	9999-12-31	pf	Mara Telecom	\N	t	t	\N	\N	\N
1969	547-15	0001-01-01	9999-12-31	pf	Pacific Mobile Telecom	\N	t	t	\N	\N	\N
1970	547-20	0001-01-01	9999-12-31	pf	Tikiphone	Tikiphone	t	t	\N	\N	\N
1971	628-01	0001-01-01	9999-12-31	ga	LIBERTIS	Libertis	t	t	\N	\N	\N
1972	628-02	0001-01-01	9999-12-31	ga	MOOV	Moov	t	t	\N	\N	\N
1973	628-03	0001-01-01	9999-12-31	ga	CELTEL	Airtel	t	t	\N	\N	\N
1896	370-01	0001-01-01	9999-12-31	do	Orange Dominicana, S.A.	Orange do	t	t	\N	\N	\N
1938	208-01	0001-01-01	9999-12-31	fr	Orange France	Orange fr	t	t	\N	\N	\N
1939	208-02	0001-01-01	9999-12-31	fr	Orange France	Orange fr	t	t	\N	\N	\N
1974	628-04	0001-01-01	9999-12-31	ga	USAN GABON	Azur	t	t	\N	\N	\N
1975	628-05	0001-01-01	9999-12-31	ga	Réseau de l’Administration Gabonaise (RAG)	RAG	t	t	\N	\N	\N
1976	607-01	0001-01-01	9999-12-31	gm	Gamcel	Gamcel	t	t	\N	\N	\N
1977	607-02	0001-01-01	9999-12-31	gm	Africell	Africell	t	t	\N	\N	\N
1978	607-03	0001-01-01	9999-12-31	gm	Comium Services Ltd	Comium	t	t	\N	\N	\N
1979	607-04	0001-01-01	9999-12-31	gm	Qcell	QCell	t	t	\N	\N	\N
1980	282-01	0001-01-01	9999-12-31	ge	Geocell Ltd.	Geocell	t	t	\N	\N	\N
1981	282-02	0001-01-01	9999-12-31	ge	Magti GSM Ltd.	MagtiCom	t	t	\N	\N	\N
1982	282-03	0001-01-01	9999-12-31	ge	Iberiatel Ltd.	Iberiatel	t	t	\N	\N	\N
1983	282-04	0001-01-01	9999-12-31	ge	Mobitel Ltd.	Beeline	t	t	\N	\N	\N
1984	282-05	0001-01-01	9999-12-31	ge	Silknet JSC	SLINKNET	t	t	\N	\N	\N
1987	262-03	0001-01-01	9999-12-31	de	E-Plus Mobilfunk GmbH & Co. KG	E-plus	t	t	\N	\N	\N
1988	262-04	0001-01-01	9999-12-31	de	Vodafone D2 GmbH	Vodafone (Reserved)	t	t	\N	\N	\N
1989	262-05	0001-01-01	9999-12-31	de	E-Plus Mobilfunk GmbH & Co. KG	E-Plus (Reserved)	t	t	\N	\N	\N
1990	262-06	0001-01-01	9999-12-31	de	T-Mobile Deutschland GmbH	T-Mobile (Reserved)	t	t	\N	\N	\N
1991	262-07	0001-01-01	9999-12-31	de	O2 (Germany) GmbH & Co. OHG	O2	t	t	\N	\N	\N
1992	262-08	0001-01-01	9999-12-31	de	O2 (Germany) GmbH & Co. OHG	O2	t	t	\N	\N	\N
1994	262-10	0001-01-01	9999-12-31	de	Arcor AG & Co.	Arcor AG & Co. (GSM-R)	t	t	\N	\N	\N
1995	262-11	0001-01-01	9999-12-31	de	O2 (Germany) GmbH & Co. OHG	O2 (RESERVED)	t	t	\N	\N	\N
1996	262-12	0001-01-01	9999-12-31	de	Dolphin Telecom (Deutschland) GmbH	Dolphin Telecom (Deutschland) GmbH	t	t	\N	\N	\N
1997	262-13	0001-01-01	9999-12-31	de	Mobilcom Multimedia GmbH	Mobilcom Multimedia GmbH	t	t	\N	\N	\N
1998	262-14	0001-01-01	9999-12-31	de	Group 3G UMTS GmbH (Quam)	Group 3G UMTS GmbH (Quam)	t	t	\N	\N	\N
1999	262-15	0001-01-01	9999-12-31	de	Airdata AG	Airdata	t	t	\N	\N	\N
2000	262-76	0001-01-01	9999-12-31	de	Siemens AG, ICMNPGUSTA	Siemens AG,	t	t	\N	\N	\N
2001	262-77	0001-01-01	9999-12-31	de	E-Plus Mobilfunk GmbH & Co. KG	E-Plus	t	t	\N	\N	\N
2002	620-01	0001-01-01	9999-12-31	gh	Spacefon	MTN	t	t	\N	\N	\N
2004	620-03	0001-01-01	9999-12-31	gh	Mobitel	tiGO	t	t	\N	\N	\N
2005	620-04	0001-01-01	9999-12-31	gh	Kasapa Telecom Ltd.	Expresso	t	t	\N	\N	\N
2006	620-11	0001-01-01	9999-12-31	gh	Netafriques Dot Com Ltd	\N	t	t	\N	\N	\N
2007	266-01	0001-01-01	9999-12-31	gi	Gibtelecom GSM	GibTel	t	t	\N	\N	\N
2008	266-06	0001-01-01	9999-12-31	gi	CTS	CTS Mobile	t	t	\N	\N	\N
2009	266-09	0001-01-01	9999-12-31	gi	Eazi Telecom Limited	Cloud9 Mobile Communications	t	t	\N	\N	\N
2010	202-01	0001-01-01	9999-12-31	gr	Cosmote	Cosmote	t	t	\N	\N	\N
2011	202-02	0001-01-01	9999-12-31	gr	Cosmote	\N	t	t	\N	\N	\N
2012	202-03	0001-01-01	9999-12-31	gr	OTE	\N	t	t	\N	\N	\N
2013	202-04	0001-01-01	9999-12-31	gr	EDISY	\N	t	t	\N	\N	\N
2015	202-06	0001-01-01	9999-12-31	gr	COSMOLINE	\N	t	t	\N	\N	\N
2016	202-07	0001-01-01	9999-12-31	gr	AMD TELECOM	\N	t	t	\N	\N	\N
2017	202-09	0001-01-01	9999-12-31	gr	WIND	Wind	t	t	\N	\N	\N
2018	202-10	0001-01-01	9999-12-31	gr	WIND	Wind	t	t	\N	\N	\N
2019	290-01	0001-01-01	9999-12-31	gl	Tele Greenland	Tele Greenland	t	t	\N	\N	\N
2020	352-110	0001-01-01	9999-12-31	gd	Cable & Wireless Grenada ltd trading as lime	Cable & Wireless	t	t	\N	\N	\N
2022	340-02	0001-01-01	9999-12-31	gp	Outremer Telecom	Outremer	t	t	\N	\N	\N
2023	340-03	0001-01-01	9999-12-31	gp	Saint Martin et Saint Barthelemy Telcell Sarl	Telcell	t	t	\N	\N	\N
2024	340-08	0001-01-01	9999-12-31	gp	Dauphin Telecom	MIO GSM	t	t	\N	\N	\N
2025	340-10	0001-01-01	9999-12-31	gp	Guadeloupe Téléphone Mobile	\N	t	t	\N	\N	\N
2026	340-20	0001-01-01	9999-12-31	gp	Bouygues Telecom Caraïbe	Digicel	t	t	\N	\N	\N
2027	704-01	0001-01-01	9999-12-31	gt	Servicios de Comunicaciones Personales Inalámbricas, S.A. (SERCOM, S.A	Claro	t	t	\N	\N	\N
2028	704-02	0001-01-01	9999-12-31	gt	Comunicaciones Celulares S.A.	Comcel / Tigo	t	t	\N	\N	\N
2029	704-03	0001-01-01	9999-12-31	gt	Telefónica Centroamérica Guatemala S.A.	Movistar	t	t	\N	\N	\N
2030	611-01	0001-01-01	9999-12-31	gn	Orange Guinée	Orange S.A.	t	t	\N	\N	\N
2031	611-02	0001-01-01	9999-12-31	gn	Sotelgui	Sotelgui	t	t	\N	\N	\N
2032	611-05	0001-01-01	9999-12-31	gn	Cellcom Guinée SA	Cellcom	t	t	\N	\N	\N
2033	632-01	0001-01-01	9999-12-31	gw	Guinétel S.A.	Guinétel S.A.	t	t	\N	\N	\N
2034	632-02	0001-01-01	9999-12-31	gw	Spacetel Guiné-Bissau S.A.	Spacetel Guiné-Bissau S.A.	t	t	\N	\N	\N
2035	738-01	0001-01-01	9999-12-31	gy	Cel*Star (Guyana) Inc.	Digicel	t	t	\N	\N	\N
2036	372-01	0001-01-01	9999-12-31	ht	Comcel	Voila	t	t	\N	\N	\N
2037	372-02	0001-01-01	9999-12-31	ht	Digicel	Digicel	t	t	\N	\N	\N
2038	372-03	0001-01-01	9999-12-31	ht	Rectel	NATCOM	t	t	\N	\N	\N
2039	708-001	0001-01-01	9999-12-31	hn	Megatel	\N	t	t	\N	\N	\N
2040	708-002	0001-01-01	9999-12-31	hn	Celtel	\N	t	t	\N	\N	\N
2041	708-040	0001-01-01	9999-12-31	hn	Digicel Honduras	\N	t	t	\N	\N	\N
2042	454-00	0001-01-01	9999-12-31	hk	GSM900/HKCSL	1O1O / One2Free	t	t	\N	\N	\N
2043	454-01	0001-01-01	9999-12-31	hk	MVNO/CITIC	CITIC Telecom 1616	t	t	\N	\N	\N
2044	454-02	0001-01-01	9999-12-31	hk	3G Radio System/HKCSL3G	CSL	t	t	\N	\N	\N
2045	454-03	0001-01-01	9999-12-31	hk	3G Radio System/Hutchison 3G	3 (3G)	t	t	\N	\N	\N
2046	454-04	0001-01-01	9999-12-31	hk	GSM900/GSM1800/Hutchison	3 (2G)	t	t	\N	\N	\N
2047	454-05	0001-01-01	9999-12-31	hk	CDMA/Hutchison	3 (CDMA)	t	t	\N	\N	\N
2048	454-06	0001-01-01	9999-12-31	hk	GSM900/SmarTone	SmarTone-Vodafone	t	t	\N	\N	\N
2049	454-07	0001-01-01	9999-12-31	hk	MVNO/China Unicom International Ltd.	China Unicom (Hong Kong) Ltd	t	t	\N	\N	\N
2050	454-08	0001-01-01	9999-12-31	hk	MVNO/Trident	Trident Telecom	t	t	\N	\N	\N
2051	454-09	0001-01-01	9999-12-31	hk	MVNO/China Motion Telecom (HK) Ltd.	China Motion Telecom	t	t	\N	\N	\N
2052	454-10	0001-01-01	9999-12-31	hk	GSM1800New World PCS Ltd.	New World Mobility	t	t	\N	\N	\N
2053	454-11	0001-01-01	9999-12-31	hk	MVNO/CHKTL	China-Hong Kong Telecom	t	t	\N	\N	\N
2054	454-12	0001-01-01	9999-12-31	hk	GSM1800/Peoples Telephone Company Ltd.	CMCC HK	t	t	\N	\N	\N
2055	454-15	0001-01-01	9999-12-31	hk	3G Radio System/SMT3G	3G Radio System/SMT3G	t	t	\N	\N	\N
2056	454-16	0001-01-01	9999-12-31	hk	GSM1800/Mandarin Communications Ltd.	PCCW Mobile	t	t	\N	\N	\N
2057	454-18	0001-01-01	9999-12-31	hk	GSM7800/Hong Kong CSL Ltd.	CSL	t	t	\N	\N	\N
2058	454-19	0001-01-01	9999-12-31	hk	3G Radio System/Sunday3G	PCCW Mobile	t	t	\N	\N	\N
2059	454-2X	0001-01-01	9999-12-31	hk	Public Mobile Networks/Reserved	\N	t	t	\N	\N	\N
1986	262-02	0001-01-01	9999-12-31	de	Vodafone D2 GmbH	Vodafone de	t	t	\N	\N	\N
2021	340-01	0001-01-01	9999-12-31	gp	Orange Caraïbe Mobiles	Orange gp	t	t	\N	\N	\N
2060	454-3X	0001-01-01	9999-12-31	hk	Public Mobile Networks/Reserved	\N	t	t	\N	\N	\N
2061	216-01	0001-01-01	9999-12-31	hu	Telenor Hungary Ltd	Telenor	t	t	\N	\N	\N
2064	274-01	0001-01-01	9999-12-31	is	Iceland Telecom Ltd.	Siminn	t	t	\N	\N	\N
2067	274-04	0001-01-01	9999-12-31	is	IMC Islande ehf	Viking	t	t	\N	\N	\N
2068	274-07	0001-01-01	9999-12-31	is	IceCell ehf	IceCell	t	t	\N	\N	\N
2069	404-00	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, Madhya Pradesh	Sistema Shyam	t	t	\N	\N	\N
2071	404-02	0001-01-01	9999-12-31	in	Bharti Airtel Ltd., Punjab	Airtel	t	t	\N	\N	\N
2072	404-03	0001-01-01	9999-12-31	in	Bharti Airtel Ltd., H.P.	Airtel	t	t	\N	\N	\N
2073	404-04	0001-01-01	9999-12-31	in	Idea Cellular Ltd., Delhi	Idea	t	t	\N	\N	\N
2075	404-06	0001-01-01	9999-12-31	in	Bharti Airtel Ltd., Karnataka	\N	t	t	\N	\N	\N
2076	404-07	0001-01-01	9999-12-31	in	Idea Cellular Ltd., Andhra Pradesh	Idea	t	t	\N	\N	\N
2077	404-09	0001-01-01	9999-12-31	in	Reliance Telecom Ltd., Assam	Reliance Telecom	t	t	\N	\N	\N
2078	404-10	0001-01-01	9999-12-31	in	Bharti Airtel Ltd., Delhi	Airtel	t	t	\N	\N	\N
2080	404-12	0001-01-01	9999-12-31	in	Idea Mobile Communications Ltd., Haryana	Idea	t	t	\N	\N	\N
2082	404-14	0001-01-01	9999-12-31	in	Spice Communications PVT Ltd., Punjab	Idea	t	t	\N	\N	\N
2084	404-16	0001-01-01	9999-12-31	in	Bharti Airtel Ltd, North East	Airtel	t	t	\N	\N	\N
2085	404-17	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, West Bengal	Aircel	t	t	\N	\N	\N
2086	404-18	0001-01-01	9999-12-31	in	Reliance Telecom Ltd., H.P.	Reliance Telecom	t	t	\N	\N	\N
2087	404-19	0001-01-01	9999-12-31	in	Idea Mobile Communications Ltd., Kerala	Idea	t	t	\N	\N	\N
2089	404-21	0001-01-01	9999-12-31	in	BPL Mobile Communications Ltd., Mumbai	LOOP	t	t	\N	\N	\N
2090	404-22	0001-01-01	9999-12-31	in	Idea Cellular Ltd., Maharashtra	Idea	t	t	\N	\N	\N
2091	404-23	0001-01-01	9999-12-31	in	Idea Cellular Ltd, Maharashtra	\N	t	t	\N	\N	\N
2092	404-24	0001-01-01	9999-12-31	in	Idea Cellular Ltd., Gujarat	Idea	t	t	\N	\N	\N
2093	404-25	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, Bihar	Aircel	t	t	\N	\N	\N
2095	404-29	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, Assam	Aircel	t	t	\N	\N	\N
2097	404-31	0001-01-01	9999-12-31	in	Bharti Airtel Ltd., Kolkata	Airtel	t	t	\N	\N	\N
2098	404-33	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, North East	Aircel	t	t	\N	\N	\N
2099	404-34	0001-01-01	9999-12-31	in	BSNL, Haryana	BSNL	t	t	\N	\N	\N
2100	404-35	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, Himachal Pradesh	Aircel	t	t	\N	\N	\N
2101	404-36	0001-01-01	9999-12-31	in	Reliance Telecom Ltd., Bihar	Reliance Telecom	t	t	\N	\N	\N
2102	404-37	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, J&K	Aircel	t	t	\N	\N	\N
2103	404-38	0001-01-01	9999-12-31	in	BSNL, Assam	BSNL	t	t	\N	\N	\N
2104	404-40	0001-01-01	9999-12-31	in	Bharti Airtel Ltd., Chennai	Airtel	t	t	\N	\N	\N
2105	404-41	0001-01-01	9999-12-31	in	Aircell Cellular Ltd, Chennai	Aircel	t	t	\N	\N	\N
2106	404-42	0001-01-01	9999-12-31	in	Aircel Ltd., Tamil Nadu	Aircel	t	t	\N	\N	\N
2108	404-44	0001-01-01	9999-12-31	in	Spice Communications PVT Ltd., Karnataka	Idea	t	t	\N	\N	\N
2110	404-48	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, UP (West)	\N	t	t	\N	\N	\N
2111	404-49	0001-01-01	9999-12-31	in	Bharti Airtel Ltd., Andra Pradesh	Airtel	t	t	\N	\N	\N
2112	404-50	0001-01-01	9999-12-31	in	Reliance Telecom Ltd., North East	Reliance Telecom	t	t	\N	\N	\N
2113	404-51	0001-01-01	9999-12-31	in	BSNL, H.P.	BSNL	t	t	\N	\N	\N
2114	404-52	0001-01-01	9999-12-31	in	Reliance Telecom Ltd., Orissa	Reliance Telecom	t	t	\N	\N	\N
2115	404-53	0001-01-01	9999-12-31	in	BSNL, Punjab	BSNL	t	t	\N	\N	\N
2116	404-54	0001-01-01	9999-12-31	in	BSNL, UP (West)	BSNL	t	t	\N	\N	\N
2117	404-55	0001-01-01	9999-12-31	in	BSNL, UP (East)	BSNL	t	t	\N	\N	\N
2118	404-56	0001-01-01	9999-12-31	in	Idea Mobile Communications Ltd., UP (West)	Idea	t	t	\N	\N	\N
2119	404-57	0001-01-01	9999-12-31	in	BSNL, Gujarat	BSNL	t	t	\N	\N	\N
2120	404-58	0001-01-01	9999-12-31	in	BSNL, Madhya Pradesh	BSNL	t	t	\N	\N	\N
2121	404-59	0001-01-01	9999-12-31	in	BSNL, Rajasthan	BSNL	t	t	\N	\N	\N
2123	404-61	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, Punjab	\N	t	t	\N	\N	\N
2124	404-62	0001-01-01	9999-12-31	in	BSNL, J&K	BSNL	t	t	\N	\N	\N
2125	404-63	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, Haryana	\N	t	t	\N	\N	\N
2126	404-64	0001-01-01	9999-12-31	in	BSNL, Chennai	BSNL	t	t	\N	\N	\N
2127	404-65	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, UP (East)	\N	t	t	\N	\N	\N
2128	404-66	0001-01-01	9999-12-31	in	BSNL, Maharashtra	BSNL	t	t	\N	\N	\N
2129	404-67	0001-01-01	9999-12-31	in	Reliance Telecom Ltd., Madhya Pradesh	Reliance Telecom	t	t	\N	\N	\N
2130	404-68	0001-01-01	9999-12-31	in	MTNL, Delhi	Dolphin	t	t	\N	\N	\N
2131	404-69	0001-01-01	9999-12-31	in	MTNL, Mumbai	Dolphin	t	t	\N	\N	\N
2132	404-70	0001-01-01	9999-12-31	in	Bharti Hexacom Ltd, Rajasthan	Airtel	t	t	\N	\N	\N
2133	404-71	0001-01-01	9999-12-31	in	BSNL, Karnataka	BSNL	t	t	\N	\N	\N
2134	404-72	0001-01-01	9999-12-31	in	BSNL, Kerala	BSNL	t	t	\N	\N	\N
2135	404-73	0001-01-01	9999-12-31	in	BSNL, Andhra Pradesh	BSNL	t	t	\N	\N	\N
2136	404-74	0001-01-01	9999-12-31	in	BSNL, West Bengal	BSNL	t	t	\N	\N	\N
2137	404-75	0001-01-01	9999-12-31	in	BSNL, Bihar	BSNL	t	t	\N	\N	\N
2138	404-76	0001-01-01	9999-12-31	in	BSNL, Orissa	BSNL	t	t	\N	\N	\N
2139	404-77	0001-01-01	9999-12-31	in	BSNL, North East	BSNL	t	t	\N	\N	\N
2140	404-78	0001-01-01	9999-12-31	in	BTA Cellcom Ltd., Madhya Pradesh	Idea	t	t	\N	\N	\N
2141	404-79	0001-01-01	9999-12-31	in	BSNL, Andaman & Nicobar	BSNL	t	t	\N	\N	\N
2142	404-80	0001-01-01	9999-12-31	in	BSNL, Tamil Nadu	BSNL	t	t	\N	\N	\N
2063	216-70	0001-01-01	9999-12-31	hu	Vodafone	Vodafone hu	t	t	\N	\N	\N
2143	404-81	0001-01-01	9999-12-31	in	BSNL, Kolkata	BSNL	t	t	\N	\N	\N
2144	404-82	0001-01-01	9999-12-31	in	Idea Telecommunications Ltd, H.P.	Idea	t	t	\N	\N	\N
2145	404-83	0001-01-01	9999-12-31	in	Reliable Internet Services Ltd., Kolkata	Reliance Telecom	t	t	\N	\N	\N
2147	404-85	0001-01-01	9999-12-31	in	Reliance Telecom Ltd., W.B. & A.N.	Reliance Telecom	t	t	\N	\N	\N
2149	404-87	0001-01-01	9999-12-31	in	Idea Telecommunications Ltd, Rajasthan	Idea	t	t	\N	\N	\N
2151	404-89	0001-01-01	9999-12-31	in	Idea Telecommunications Ltd, UP (East)	Idea	t	t	\N	\N	\N
2152	404-90	0001-01-01	9999-12-31	in	Bharti Airtel Ltd., Maharashtra	Airtel	t	t	\N	\N	\N
2153	404-91	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, Kolkata	Aircel	t	t	\N	\N	\N
2154	404-92	0001-01-01	9999-12-31	in	Bharti Airtel Ltd., Mumbai	Airtel	t	t	\N	\N	\N
2155	404-93	0001-01-01	9999-12-31	in	Bharti Airtel Ltd., Madhya Pradesh	Airtel	t	t	\N	\N	\N
2156	404-94	0001-01-01	9999-12-31	in	Bharti Airtel Ltd., Tamil Nadu	Airtel	t	t	\N	\N	\N
2157	404-95	0001-01-01	9999-12-31	in	Bharti Airtel Ltd., Kerala	Airtel	t	t	\N	\N	\N
2158	404-96	0001-01-01	9999-12-31	in	Bharti Airtel Ltd., Haryana	Airtel	t	t	\N	\N	\N
2159	404-97	0001-01-01	9999-12-31	in	Bharti Airtel Ltd., UP (West)	Airtel	t	t	\N	\N	\N
2160	404-98	0001-01-01	9999-12-31	in	Bharti Airtel Ltd., Gujarat	Airtel	t	t	\N	\N	\N
2161	404-99	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, Kerala	\N	t	t	\N	\N	\N
2162	405-000	0001-01-01	9999-12-31	in	Shyam Telelink Ltd, Rajasthan	\N	t	t	\N	\N	\N
2163	405-005	0001-01-01	9999-12-31	in	Reliance Communications Ltd/GSM, Delhi	\N	t	t	\N	\N	\N
2164	405-006	0001-01-01	9999-12-31	in	Reliance Communications Ltd/GSM, Gujarat	\N	t	t	\N	\N	\N
2165	405-007	0001-01-01	9999-12-31	in	Reliance Communications Ltd/GSM, Haryana	\N	t	t	\N	\N	\N
2166	405-009	0001-01-01	9999-12-31	in	Reliance Communications Ltd/GSM, J&K	\N	t	t	\N	\N	\N
2167	405-010	0001-01-01	9999-12-31	in	Reliance Communications Ltd,/GSM Karnataka	\N	t	t	\N	\N	\N
2168	405-011	0001-01-01	9999-12-31	in	Reliance Communications Ltd/GSM, Kerala	\N	t	t	\N	\N	\N
2169	405-012	0001-01-01	9999-12-31	in	Reliance Infocomm Ltd, Andhra Pradesh	\N	t	t	\N	\N	\N
2170	405-013	0001-01-01	9999-12-31	in	Reliance Communications Ltd/GSM, Maharashtra	\N	t	t	\N	\N	\N
2171	405-014	0001-01-01	9999-12-31	in	Reliance Communications Ltd/GSM, Madhya Pradesh	\N	t	t	\N	\N	\N
2172	405-018	0001-01-01	9999-12-31	in	Reliance Communications Ltd/GSM, Punjab	\N	t	t	\N	\N	\N
2173	405-020	0001-01-01	9999-12-31	in	Reliance Communications Ltd/GSM, Tamilnadu	\N	t	t	\N	\N	\N
2174	405-021	0001-01-01	9999-12-31	in	Reliance Communications Ltd/GSM, UP (East)	\N	t	t	\N	\N	\N
2175	405-022	0001-01-01	9999-12-31	in	Reliance Communications Ltd/GSM, UP (West)	\N	t	t	\N	\N	\N
2176	405-025	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, Andhra Pradesh	\N	t	t	\N	\N	\N
2177	405-027	0001-01-01	9999-12-31	in	Tata Teleservices Ltd,/GSM Bihar	\N	t	t	\N	\N	\N
2178	405-029	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, Delhi	\N	t	t	\N	\N	\N
2179	405-030	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, Gujarat	\N	t	t	\N	\N	\N
2180	405-031	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, Haryana	\N	t	t	\N	\N	\N
2181	405-032	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, Himachal Pradesh	\N	t	t	\N	\N	\N
2182	405-033	0001-01-01	9999-12-31	in	Reliance Infocomm Ltd, Bihar	\N	t	t	\N	\N	\N
2183	405-034	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, Kamataka	\N	t	t	\N	\N	\N
2184	405-035	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, Kerala	\N	t	t	\N	\N	\N
2185	405-036	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, Kolkata	\N	t	t	\N	\N	\N
2186	405-037	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, Maharashtra	\N	t	t	\N	\N	\N
2187	405-038	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, Madhya Pradesh	\N	t	t	\N	\N	\N
2188	405-039	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, Mumbai	\N	t	t	\N	\N	\N
2189	405-040	0001-01-01	9999-12-31	in	Reliance Infocomm Ltd, Chennai	\N	t	t	\N	\N	\N
2190	405-041	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, Orissa	\N	t	t	\N	\N	\N
2191	405-042	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, Punjab	\N	t	t	\N	\N	\N
2192	405-043	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, Rajasthan	\N	t	t	\N	\N	\N
2193	405-044	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, Tamilnadu	\N	t	t	\N	\N	\N
2194	405-045	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, UP (East)	\N	t	t	\N	\N	\N
2195	405-046	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, UP (West)	\N	t	t	\N	\N	\N
2196	405-047	0001-01-01	9999-12-31	in	Tata Teleservices Ltd/GSM, West Bengal	\N	t	t	\N	\N	\N
2197	405-08	0001-01-01	9999-12-31	in	Reliance Infocomm Ltd, Himachal Pradesh	Reliance Telecom	t	t	\N	\N	\N
2198	405-12	0001-01-01	9999-12-31	in	Reliance Infocomm Ltd, Kolkata	Reliance Telecom	t	t	\N	\N	\N
2199	405-15	0001-01-01	9999-12-31	in	Reliance Infocomm Ltd, Mumbai	Reliance Telecom	t	t	\N	\N	\N
2200	405-17	0001-01-01	9999-12-31	in	Reliance Infocomm Ltd, Orissa	Reliance Telecom	t	t	\N	\N	\N
2201	405-23	0001-01-01	9999-12-31	in	Reliance Infocomm Ltd, West bengal	Reliance Telecom	t	t	\N	\N	\N
2202	405-28	0001-01-01	9999-12-31	in	Tata Teleservices Ltd, Chennai	TATA Teleservices	t	t	\N	\N	\N
2203	405-52	0001-01-01	9999-12-31	in	Bharti Airtel Ltd, Bihar	Airtel	t	t	\N	\N	\N
2204	405-53	0001-01-01	9999-12-31	in	Bharti Airtel Ltd, Orissa	Airtel	t	t	\N	\N	\N
2205	405-54	0001-01-01	9999-12-31	in	Bharti Airtel Ltd, UP (East)	Airtel	t	t	\N	\N	\N
2206	405-55	0001-01-01	9999-12-31	in	Bharti Airtel Ltd, J&K	Airtel	t	t	\N	\N	\N
2207	405-56	0001-01-01	9999-12-31	in	Bharti Airtel Ltd, Assam	Airtel	t	t	\N	\N	\N
2210	405-68	0001-01-01	9999-12-31	in	Vodaphone/Hutchison, Madhya Pradesh	\N	t	t	\N	\N	\N
2211	405-70	0001-01-01	9999-12-31	in	Aditya Birla Telecom Ltd, Bihar	Idea	t	t	\N	\N	\N
2212	405-71	0001-01-01	9999-12-31	in	Essar Spacetel Ltd, Himachal Pradesh	\N	t	t	\N	\N	\N
2213	405-72	0001-01-01	9999-12-31	in	Essar Spacetel Ltd, North East	\N	t	t	\N	\N	\N
2214	405-73	0001-01-01	9999-12-31	in	Essar Spacetel Ltd, Assam	\N	t	t	\N	\N	\N
2215	405-74	0001-01-01	9999-12-31	in	Essar Spacetel Ltd, J&K	\N	t	t	\N	\N	\N
2222	405-76	0001-01-01	9999-12-31	in	Essar Spacetel Ltd, Orissa	\N	t	t	\N	\N	\N
2223	405-77	0001-01-01	9999-12-31	in	Essar Spacetel Ltd, Maharashtra	\N	t	t	\N	\N	\N
2224	405-799	0001-01-01	9999-12-31	in	Idea Cellular Ltd, MUMBAI	Idea	t	t	\N	\N	\N
2225	405-800	0001-01-01	9999-12-31	in	Aircell Ltd, Delhi	Aircel	t	t	\N	\N	\N
2226	405-801	0001-01-01	9999-12-31	in	Aircell Ltd, Andhra Pradesh	Aircel	t	t	\N	\N	\N
2227	405-802	0001-01-01	9999-12-31	in	Aircell Ltd, Gujarat	Aircel	t	t	\N	\N	\N
2228	405-803	0001-01-01	9999-12-31	in	Aircell Ltd, Kamataka	Aircel	t	t	\N	\N	\N
2229	405-804	0001-01-01	9999-12-31	in	Aircell Ltd, Maharashtra	Aircel	t	t	\N	\N	\N
2230	405-805	0001-01-01	9999-12-31	in	Aircell Ltd, Mumbai	Aircel	t	t	\N	\N	\N
2231	405-806	0001-01-01	9999-12-31	in	Aircell Ltd, Rajasthan	Aircel	t	t	\N	\N	\N
2232	405-807	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, Haryana	Aircel	t	t	\N	\N	\N
2233	405-808	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, Madhya Pradesh	Aircel	t	t	\N	\N	\N
2234	405-809	0001-01-01	9999-12-31	in	Dishnet Wireless Ltd, Kerala	Aircel	t	t	\N	\N	\N
2235	405-81	0001-01-01	9999-12-31	in	Aircell Ltd, Delhi	\N	t	t	\N	\N	\N
2236	405-82	0001-01-01	9999-12-31	in	Aircell Ltd, Andhra Pradesh	\N	t	t	\N	\N	\N
2237	405-83	0001-01-01	9999-12-31	in	Aircell Ltd, Gujarat	\N	t	t	\N	\N	\N
2238	405-84	0001-01-01	9999-12-31	in	Aircell Ltd, Maharashtra	\N	t	t	\N	\N	\N
2239	405-85	0001-01-01	9999-12-31	in	Aircell Ltd, Mumbai	\N	t	t	\N	\N	\N
2240	405-86	0001-01-01	9999-12-31	in	Aircell Ltd, Rajasthan	\N	t	t	\N	\N	\N
2241	510-00	0001-01-01	9999-12-31	id	PSN	PSN	t	t	\N	\N	\N
2242	510-01	0001-01-01	9999-12-31	id	Satelindo	INDOSAT	t	t	\N	\N	\N
2243	510-08	0001-01-01	9999-12-31	id	Natrindo (Lippo Telecom)	AXIS	t	t	\N	\N	\N
2244	510-10	0001-01-01	9999-12-31	id	Telkomsel	Telkomsel	t	t	\N	\N	\N
2245	510-11	0001-01-01	9999-12-31	id	Excelcomindo	XL	t	t	\N	\N	\N
2246	510-21	0001-01-01	9999-12-31	id	Indosat - M3	IM3	t	t	\N	\N	\N
2247	510-28	0001-01-01	9999-12-31	id	Komselindo	Fren/Hepi	t	t	\N	\N	\N
2248	432-11	0001-01-01	9999-12-31	ir	Telecommunication Company of Iran (TCI)	IR-MCI	t	t	\N	\N	\N
2249	432-14	0001-01-01	9999-12-31	ir	Telecommunication Kish Co. (KIFZO)	TKC	t	t	\N	\N	\N
2250	432-19	0001-01-01	9999-12-31	ir	Telecommunication Company of Iran (TCI) - Isfahan Celcom GSM	MTCE	t	t	\N	\N	\N
2251	418-05	0001-01-01	9999-12-31	iq	Asia Cell	Asia Cell	t	t	\N	\N	\N
2252	418-20	0001-01-01	9999-12-31	iq	Zain Iraq (previously Atheer)	Zain	t	t	\N	\N	\N
2253	418-30	0001-01-01	9999-12-31	iq	Zain Iraq (previously Iraqna)	Zain	t	t	\N	\N	\N
2254	418-40	0001-01-01	9999-12-31	iq	Korek Telecom	Korek	t	t	\N	\N	\N
2255	418-47	0001-01-01	9999-12-31	iq	Iraq Central Cooperative Association for Communication and Transportat	\N	t	t	\N	\N	\N
2256	418-48	0001-01-01	9999-12-31	iq	ITC Fanoos	\N	t	t	\N	\N	\N
2257	418-49	0001-01-01	9999-12-31	iq	Iraqtel	\N	t	t	\N	\N	\N
2258	418-62	0001-01-01	9999-12-31	iq	Itisaluna	\N	t	t	\N	\N	\N
2259	418-70	0001-01-01	9999-12-31	iq	Kalimat	\N	t	t	\N	\N	\N
2260	418-80	0001-01-01	9999-12-31	iq	Iraqi Telecommunications & Post Company (ITPC)	\N	t	t	\N	\N	\N
2261	418-81	0001-01-01	9999-12-31	iq	ITPC (Al-Mazaya)	\N	t	t	\N	\N	\N
2262	418-83	0001-01-01	9999-12-31	iq	ITPC (Sader Al-Iraq)	\N	t	t	\N	\N	\N
2263	418-84	0001-01-01	9999-12-31	iq	ITPC (Eaamar Albasrah)	\N	t	t	\N	\N	\N
2264	418-85	0001-01-01	9999-12-31	iq	ITPC (Anwar Yagotat Alkhalee)	\N	t	t	\N	\N	\N
2265	418-86	0001-01-01	9999-12-31	iq	ITPC (Furatfone)	\N	t	t	\N	\N	\N
2266	418-87	0001-01-01	9999-12-31	iq	ITPC (Al-Seraj)	\N	t	t	\N	\N	\N
2267	418-88	0001-01-01	9999-12-31	iq	ITPC (High Link)	\N	t	t	\N	\N	\N
2268	418-89	0001-01-01	9999-12-31	iq	ITPC (Al-Shams)	\N	t	t	\N	\N	\N
2269	418-91	0001-01-01	9999-12-31	iq	ITPC (Belad Babel)	\N	t	t	\N	\N	\N
2270	418-92	0001-01-01	9999-12-31	iq	ITPC (Al Nakheel)	Omnnea	t	t	\N	\N	\N
2271	418-93	0001-01-01	9999-12-31	iq	ITPC (Iraqcell)	\N	t	t	\N	\N	\N
2272	418-94	0001-01-01	9999-12-31	iq	ITPC (Shaly)	\N	t	t	\N	\N	\N
2274	272-02	0001-01-01	9999-12-31	ie	Telefonica Ltd	O2	t	t	\N	\N	\N
2275	272-03	0001-01-01	9999-12-31	ie	Meteor Mobile Communications Ltd.	Meteor	t	t	\N	\N	\N
2276	272-07	0001-01-01	9999-12-31	ie	Eircom	Eircom	t	t	\N	\N	\N
2277	272-09	0001-01-01	9999-12-31	ie	Clever Communications Ltd.	Clever Communications Ltd.	t	t	\N	\N	\N
2279	425-02	0001-01-01	9999-12-31	il	Cellcom Israel Ltd.	Cellcom	t	t	\N	\N	\N
2280	425-03	0001-01-01	9999-12-31	il	Pelephone Communications Ltd.	Pelephone	t	t	\N	\N	\N
2281	425-04	0001-01-01	9999-12-31	il	Globalsim Ltd	\N	t	t	\N	\N	\N
2282	425-06	0001-01-01	9999-12-31	il	Wataniya	Wataniya Palestine	t	t	\N	\N	\N
2283	425-07	0001-01-01	9999-12-31	il	Mirs Ltd	Hot Mobile	t	t	\N	\N	\N
2284	425-08	0001-01-01	9999-12-31	il	Golan Telecom Ltd	Golan Telecom	t	t	\N	\N	\N
2285	425-11	0001-01-01	9999-12-31	il	365 Telecom (MVNO)	\N	t	t	\N	\N	\N
2286	425-12	0001-01-01	9999-12-31	il	Free Telecom (MVNO)	\N	t	t	\N	\N	\N
2287	425-13	0001-01-01	9999-12-31	il	Ituran Cellular Communications	\N	t	t	\N	\N	\N
2288	425-14	0001-01-01	9999-12-31	il	Alon Cellular Ltd.	\N	t	t	\N	\N	\N
2289	425-15	0001-01-01	9999-12-31	il	Home Cellular (MVNO)	\N	t	t	\N	\N	\N
2290	425-16	0001-01-01	9999-12-31	il	Rami Levi (MVNO)	\N	t	t	\N	\N	\N
2291	425-17	0001-01-01	9999-12-31	il	Gale Phone (MVNO)	\N	t	t	\N	\N	\N
2292	425-18	0001-01-01	9999-12-31	il	Cellact Communications Ltd (MVNO)	\N	t	t	\N	\N	\N
2293	425-20	0001-01-01	9999-12-31	il	Bezeq Ltd	\N	t	t	\N	\N	\N
2294	222-01	0001-01-01	9999-12-31	it	Telecom Italia Mobile (TIM)	TIM	t	t	\N	\N	\N
2295	222-02	0001-01-01	9999-12-31	it	Elsacom	Elsacom	t	t	\N	\N	\N
2297	222-77	0001-01-01	9999-12-31	it	IPSE 2000	IPSE 2000	t	t	\N	\N	\N
2298	222-88	0001-01-01	9999-12-31	it	Wind	Wind	t	t	\N	\N	\N
2299	222-98	0001-01-01	9999-12-31	it	Blu	Blu	t	t	\N	\N	\N
2300	222-99	0001-01-01	9999-12-31	it	H3G	3 Italia	t	t	\N	\N	\N
2301	338-020	0001-01-01	9999-12-31	jm	Cable & Wireless Jamaica Ltd.	\N	t	t	\N	\N	\N
2302	338-050	0001-01-01	9999-12-31	jm	Digicel (Jamaica) Ltd.	\N	t	t	\N	\N	\N
2303	338-110	0001-01-01	9999-12-31	jm	Cable & Wireless Jamaica Ltd trading as Lime	\N	t	t	\N	\N	\N
2304	440-01	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2305	440-02	0001-01-01	9999-12-31	jp	NTT DoCoMo Kansai Inc.	DoCoMo	t	t	\N	\N	\N
2306	440-03	0001-01-01	9999-12-31	jp	NTT DoCoMo Hokuriku Inc.	DoCoMo	t	t	\N	\N	\N
2307	440-04	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2308	440-06	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2309	440-07	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2310	440-08	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2278	425-01	0001-01-01	9999-12-31	il	Partner Communications Co. Ltd.	Orange il	t	t	\N	\N	\N
2311	440-09	0001-01-01	9999-12-31	jp	NTT DoCoMo Kansai Inc.	DoCoMo	t	t	\N	\N	\N
2312	440-10	0001-01-01	9999-12-31	jp	NTT DoCoMo Kansai Inc.	DoCoMo	t	t	\N	\N	\N
2313	440-11	0001-01-01	9999-12-31	jp	NTT DoCoMo Tokai Inc.	DoCoMo	t	t	\N	\N	\N
2314	440-12	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2315	440-13	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2316	440-14	0001-01-01	9999-12-31	jp	NTT DoCoMo Tohoku Inc.	DoCoMo	t	t	\N	\N	\N
2317	440-15	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2318	440-16	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2319	440-17	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2320	440-18	0001-01-01	9999-12-31	jp	NTT DoCoMo Tokai Inc.	DoCoMo	t	t	\N	\N	\N
2321	440-19	0001-01-01	9999-12-31	jp	NTT DoCoMo Hokkaido Inc.	DoCoMo	t	t	\N	\N	\N
2322	440-20	0001-01-01	9999-12-31	jp	NTT DoCoMo Hokuriku Inc.	SoftBank	t	t	\N	\N	\N
2323	440-21	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2324	440-22	0001-01-01	9999-12-31	jp	NTT DoCoMo Kansai Inc.	DoCoMo	t	t	\N	\N	\N
2325	440-23	0001-01-01	9999-12-31	jp	NTT DoCoMo Tokai Inc.	DoCoMo	t	t	\N	\N	\N
2326	440-24	0001-01-01	9999-12-31	jp	NTT DoCoMo Chugoku Inc.	DoCoMo	t	t	\N	\N	\N
2327	440-25	0001-01-01	9999-12-31	jp	NTT DoCoMo Hokkaido Inc.	DoCoMo	t	t	\N	\N	\N
2328	440-26	0001-01-01	9999-12-31	jp	NTT DoCoMo Kyushu Inc.	DoCoMo	t	t	\N	\N	\N
2329	440-27	0001-01-01	9999-12-31	jp	NTT DoCoMo Tohoku Inc.	DoCoMo	t	t	\N	\N	\N
2330	440-28	0001-01-01	9999-12-31	jp	NTT DoCoMo Shikoku Inc.	DoCoMo	t	t	\N	\N	\N
2331	440-29	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2332	440-30	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2333	440-31	0001-01-01	9999-12-31	jp	NTT DoCoMo Kansai Inc.	DoCoMo	t	t	\N	\N	\N
2334	440-32	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2335	440-33	0001-01-01	9999-12-31	jp	NTT DoCoMo Tokai Inc.	DoCoMo	t	t	\N	\N	\N
2336	440-34	0001-01-01	9999-12-31	jp	NTT DoCoMo Kyushu Inc.	DoCoMo	t	t	\N	\N	\N
2337	440-35	0001-01-01	9999-12-31	jp	NTT DoCoMo Kansai Inc.	DoCoMo	t	t	\N	\N	\N
2338	440-36	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2339	440-37	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2340	440-38	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2341	440-39	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2342	440-40	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2343	440-41	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2344	440-42	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2345	440-43	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2346	440-44	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2347	440-45	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2348	440-46	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2349	440-47	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2350	440-48	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2351	440-49	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	KDDI	t	t	\N	\N	\N
2352	440-50	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2353	440-51	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2354	440-52	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2355	440-53	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2356	440-54	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2357	440-55	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2358	440-56	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2359	440-58	0001-01-01	9999-12-31	jp	NTT DoCoMo Kansai Inc.	DoCoMo	t	t	\N	\N	\N
2360	440-60	0001-01-01	9999-12-31	jp	NTT DoCoMo Kansai Inc.	DoCoMo	t	t	\N	\N	\N
2361	440-61	0001-01-01	9999-12-31	jp	NTT DoCoMo Chugoku Inc.	DoCoMo	t	t	\N	\N	\N
2362	440-62	0001-01-01	9999-12-31	jp	NTT DoCoMo Kyushu Inc.	DoCoMo	t	t	\N	\N	\N
2363	440-63	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2364	440-64	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	Softbank	t	t	\N	\N	\N
2365	440-65	0001-01-01	9999-12-31	jp	NTT DoCoMo Shikoku Inc.	DoCoMo	t	t	\N	\N	\N
2366	440-66	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2367	440-67	0001-01-01	9999-12-31	jp	NTT DoCoMo Tohoku Inc.	DoCoMo	t	t	\N	\N	\N
2368	440-68	0001-01-01	9999-12-31	jp	NTT DoCoMo Kyushu Inc.	DoCoMo	t	t	\N	\N	\N
2369	440-69	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2370	440-70	0001-01-01	9999-12-31	jp	KDDI Corporation	Au	t	t	\N	\N	\N
2371	440-71	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2372	440-72	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2373	440-73	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2374	440-74	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2375	440-75	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2376	440-76	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2377	440-77	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2378	440-78	0001-01-01	9999-12-31	jp	Okinawa Cellular Telephone	Okinawa	t	t	\N	\N	\N
2379	440-79	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2380	440-80	0001-01-01	9999-12-31	jp	TU-KA Cellular Tokyo Inc.	TU-KA	t	t	\N	\N	\N
2381	440-81	0001-01-01	9999-12-31	jp	TU-KA Cellular Tokyo Inc.	TU-KA	t	t	\N	\N	\N
2382	440-82	0001-01-01	9999-12-31	jp	TU-KA Phone Kansai Inc.	TU-KA	t	t	\N	\N	\N
2383	440-83	0001-01-01	9999-12-31	jp	TU-KA Cellular Tokai Inc.	TU-KA	t	t	\N	\N	\N
2384	440-84	0001-01-01	9999-12-31	jp	TU-KA Phone Kansai Inc.	\N	t	t	\N	\N	\N
2385	440-85	0001-01-01	9999-12-31	jp	TU-KA Cellular Tokai Inc.	TU-KA	t	t	\N	\N	\N
2386	440-86	0001-01-01	9999-12-31	jp	TU-KA Cellular Tokyo Inc.	TU-KA	t	t	\N	\N	\N
2387	440-87	0001-01-01	9999-12-31	jp	NTT DoCoMo Chugoku Inc.	DoCoMo	t	t	\N	\N	\N
2388	440-88	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2389	440-89	0001-01-01	9999-12-31	jp	KDDI Corporation	KDDI	t	t	\N	\N	\N
2390	440-90	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2391	440-92	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2392	440-93	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2393	440-94	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2394	440-95	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2395	440-96	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2396	440-97	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2397	440-98	0001-01-01	9999-12-31	jp	Vodafone	Softbank	t	t	\N	\N	\N
2398	440-99	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	DoCoMo	t	t	\N	\N	\N
2399	441-40	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	\N	t	t	\N	\N	\N
2400	441-41	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	\N	t	t	\N	\N	\N
2401	441-42	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	\N	t	t	\N	\N	\N
2402	441-43	0001-01-01	9999-12-31	jp	NTT DoCoMo Kansai Inc.	\N	t	t	\N	\N	\N
2403	441-44	0001-01-01	9999-12-31	jp	NTT DoCoMo Chugoku Inc.	\N	t	t	\N	\N	\N
2404	441-45	0001-01-01	9999-12-31	jp	NTT DoCoMo Shikoku Inc.	\N	t	t	\N	\N	\N
2405	441-50	0001-01-01	9999-12-31	jp	TU-KA Cellular Tokyo Inc.	\N	t	t	\N	\N	\N
2406	441-51	0001-01-01	9999-12-31	jp	TU-KA Phone Kansai Inc.	\N	t	t	\N	\N	\N
2407	441-61	0001-01-01	9999-12-31	jp	Vodafone	\N	t	t	\N	\N	\N
2408	441-62	0001-01-01	9999-12-31	jp	Vodafone	\N	t	t	\N	\N	\N
2409	441-63	0001-01-01	9999-12-31	jp	Vodafone	\N	t	t	\N	\N	\N
2410	441-64	0001-01-01	9999-12-31	jp	Vodafone	\N	t	t	\N	\N	\N
2411	441-65	0001-01-01	9999-12-31	jp	Vodafone	\N	t	t	\N	\N	\N
2412	441-70	0001-01-01	9999-12-31	jp	KDDI Corporation	\N	t	t	\N	\N	\N
2413	441-90	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	\N	t	t	\N	\N	\N
2414	441-91	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	\N	t	t	\N	\N	\N
2415	441-92	0001-01-01	9999-12-31	jp	NTT DoCoMo Inc.	\N	t	t	\N	\N	\N
2416	441-93	0001-01-01	9999-12-31	jp	NTT DoCoMo Hokkaido Inc.	\N	t	t	\N	\N	\N
2417	441-94	0001-01-01	9999-12-31	jp	NTT DoCoMo Tohoku Inc.	\N	t	t	\N	\N	\N
2418	441-98	0001-01-01	9999-12-31	jp	NTT DoCoMo Kyushu Inc.	\N	t	t	\N	\N	\N
2419	441-99	0001-01-01	9999-12-31	jp	NTT DoCoMo Kyushu Inc.	\N	t	t	\N	\N	\N
2420	416-01	0001-01-01	9999-12-31	jo	Fastlink	zain JO	t	t	\N	\N	\N
2421	416-02	0001-01-01	9999-12-31	jo	Xpress	XPress Telecom	t	t	\N	\N	\N
2422	416-03	0001-01-01	9999-12-31	jo	Umniah	Umniah	t	t	\N	\N	\N
2424	401-01	0001-01-01	9999-12-31	kz	Kar-Tel llc	Beeline	t	t	\N	\N	\N
2425	401-02	0001-01-01	9999-12-31	kz	TSC Kazak Telecom	Kcell	t	t	\N	\N	\N
2426	639-02	0001-01-01	9999-12-31	ke	Safaricom Ltd.	Safaricom	t	t	\N	\N	\N
2427	639-03	0001-01-01	9999-12-31	ke	Kencell Communications Ltd.	Zain	t	t	\N	\N	\N
2428	450-02	0001-01-01	9999-12-31	kr	KT Freetel	KT	t	t	\N	\N	\N
2429	450-03	0001-01-01	9999-12-31	kr	SK Telecom	Digital 017	t	t	\N	\N	\N
2430	450-06	0001-01-01	9999-12-31	kr	LG UPLUS	LGT	t	t	\N	\N	\N
2431	419-02	0001-01-01	9999-12-31	kw	Mobile Telecommunications Company	Zain	t	t	\N	\N	\N
2432	419-03	0001-01-01	9999-12-31	kw	Wataniya Telecom	Wataniya	t	t	\N	\N	\N
2433	419-04	0001-01-01	9999-12-31	kw	Viva	Viva	t	t	\N	\N	\N
2434	437-01	0001-01-01	9999-12-31	kg	Bitel GSM	Bitel GSM	t	t	\N	\N	\N
2435	457-01	0001-01-01	9999-12-31	la	Lao Telecommunications	LaoTel	t	t	\N	\N	\N
2436	457-02	0001-01-01	9999-12-31	la	ETL Mobile	ETL	t	t	\N	\N	\N
2437	457-08	0001-01-01	9999-12-31	la	Millicom	Tigo	t	t	\N	\N	\N
2438	247-01	0001-01-01	9999-12-31	lv	Latvijas Mobilais Telefons SIA	LMT	t	t	\N	\N	\N
2439	247-02	0001-01-01	9999-12-31	lv	Tele2	Tele2	t	t	\N	\N	\N
2440	247-03	0001-01-01	9999-12-31	lv	Telekom Baltija	Triatel	t	t	\N	\N	\N
2441	247-04	0001-01-01	9999-12-31	lv	Beta Telecom	\N	t	t	\N	\N	\N
2442	247-05	0001-01-01	9999-12-31	lv	Bite Mobile	Bite	t	t	\N	\N	\N
2443	247-06	0001-01-01	9999-12-31	lv	Rigatta	Rigatta	t	t	\N	\N	\N
2444	247-07	0001-01-01	9999-12-31	lv	Master Telecom	Master Telecom (MTS)	t	t	\N	\N	\N
2445	247-08	0001-01-01	9999-12-31	lv	IZZI	IZZI	t	t	\N	\N	\N
2446	415-05	0001-01-01	9999-12-31	lb	Ogero Telecom	Ogero Mobile	t	t	\N	\N	\N
2447	415-32	0001-01-01	9999-12-31	lb	Cellis	\N	t	t	\N	\N	\N
2448	415-33	0001-01-01	9999-12-31	lb	Cellis	\N	t	t	\N	\N	\N
2449	415-34	0001-01-01	9999-12-31	lb	Cellis	\N	t	t	\N	\N	\N
2450	415-35	0001-01-01	9999-12-31	lb	Cellis	\N	t	t	\N	\N	\N
2451	415-36	0001-01-01	9999-12-31	lb	Libancell	\N	t	t	\N	\N	\N
2452	415-37	0001-01-01	9999-12-31	lb	Libancell	\N	t	t	\N	\N	\N
2453	415-38	0001-01-01	9999-12-31	lb	Libancell	\N	t	t	\N	\N	\N
2454	415-39	0001-01-01	9999-12-31	lb	Libancell	\N	t	t	\N	\N	\N
2455	651-01	0001-01-01	9999-12-31	ls	Vodacom Lesotho (pty) Ltd.	Vodacom	t	t	\N	\N	\N
2456	651-02	0001-01-01	9999-12-31	ls	Econet Ezin-cel	Econet Ezin-cel	t	t	\N	\N	\N
2457	618-04	0001-01-01	9999-12-31	lr	Comium Liberia	Comium	t	t	\N	\N	\N
2458	295-01	0001-01-01	9999-12-31	li	Swisscom Schweiz AG	Swisscom	t	t	\N	\N	\N
2460	295-05	0001-01-01	9999-12-31	li	Mobilkom (Liechtenstein) AG	FL1	t	t	\N	\N	\N
2461	295-06	0001-01-01	9999-12-31	li	Cubic AG	\N	t	t	\N	\N	\N
2462	246-01	0001-01-01	9999-12-31	lt	Omnitel	Omnitel	t	t	\N	\N	\N
2463	246-02	0001-01-01	9999-12-31	lt	Bité GSM	Bite GSM	t	t	\N	\N	\N
2464	246-03	0001-01-01	9999-12-31	lt	Tele2	Tele2	t	t	\N	\N	\N
2465	270-01	0001-01-01	9999-12-31	lu	P&T Luxembourg	LuxGSM	t	t	\N	\N	\N
2466	270-77	0001-01-01	9999-12-31	lu	Tango	Tango	t	t	\N	\N	\N
2468	455-00	0001-01-01	9999-12-31	mo	SmarTone – Comunicações Mõveis, S.A.	SmarTone	t	t	\N	\N	\N
2469	455-01	0001-01-01	9999-12-31	mo	Companhia de Telecomunicações de Macau S.A.R.L.	CTM	t	t	\N	\N	\N
2470	455-02	0001-01-01	9999-12-31	mo	China Telecom (Macau) Limitada	China Telecom	t	t	\N	\N	\N
2472	455-04	0001-01-01	9999-12-31	mo	Companhia de Telecomunicações de Macau S.A.R.L.	CTM	t	t	\N	\N	\N
2474	455-06	0001-01-01	9999-12-31	mo	SmarTone – Comunicações Mõveis, S.A.	\N	t	t	\N	\N	\N
2475	646-01	0001-01-01	9999-12-31	mg	Celtel Madagascar (Zain), GSM	Zain	t	t	\N	\N	\N
2477	646-04	0001-01-01	9999-12-31	mg	Telecom Malagasy Mobile, GSM	Telma	t	t	\N	\N	\N
2478	650-01	0001-01-01	9999-12-31	mw	Telekom Network Ltd.	TNM	t	t	\N	\N	\N
2479	650-10	0001-01-01	9999-12-31	mw	Celtel ltd.	Airtel	t	t	\N	\N	\N
2480	502-10	0001-01-01	9999-12-31	my	DIGI Telecommunications	\N	t	t	\N	\N	\N
2481	502-12	0001-01-01	9999-12-31	my	Malaysian Mobile Services Sdn Bhd	Maxis	t	t	\N	\N	\N
2482	502-13	0001-01-01	9999-12-31	my	Celcom (Malaysia) Berhad	Celcom	t	t	\N	\N	\N
2483	502-14	0001-01-01	9999-12-31	my	Telekom Malaysia Berhad	\N	t	t	\N	\N	\N
2484	502-16	0001-01-01	9999-12-31	my	DIGI Telecommunications	DiGi	t	t	\N	\N	\N
2485	502-17	0001-01-01	9999-12-31	my	Malaysian Mobile Services Sdn Bhd	Maxis	t	t	\N	\N	\N
2486	502-18	0001-01-01	9999-12-31	my	U Mobile Sdn. Bhd.	U Mobile	t	t	\N	\N	\N
2487	502-19	0001-01-01	9999-12-31	my	Celcom (Malaysia) Berhad	Celcom	t	t	\N	\N	\N
2488	502-20	0001-01-01	9999-12-31	my	Electcoms Wireless Sdn Bhd	\N	t	t	\N	\N	\N
2489	472-01	0001-01-01	9999-12-31	mv	DhiMobile	Dhiraagu	t	t	\N	\N	\N
2490	610-01	0001-01-01	9999-12-31	ml	Malitel	Malitel	t	t	\N	\N	\N
2492	278-21	0001-01-01	9999-12-31	mt	go mobile	GO	t	t	\N	\N	\N
2493	278-77	0001-01-01	9999-12-31	mt	3G Telecommunications Ltd	Melita	t	t	\N	\N	\N
2494	340-12	0001-01-01	9999-12-31	mq	Martinique Téléphone Mobile	\N	t	t	\N	\N	\N
2423	416-77	0001-01-01	9999-12-31	jo	Mobilecom	Orange jo	t	t	\N	\N	\N
2495	609-01	0001-01-01	9999-12-31	mr	Mattel S.A.	Mattel	t	t	\N	\N	\N
2496	609-02	0001-01-01	9999-12-31	mr	Chinguitel S.A.	Chinguitel S.A.	t	t	\N	\N	\N
2497	609-10	0001-01-01	9999-12-31	mr	Mauritel Mobiles	Mauritel	t	t	\N	\N	\N
2499	617-02	0001-01-01	9999-12-31	mu	Mahanagar Telephone (Mauritius) Ltd.	MTML	t	t	\N	\N	\N
2500	617-03	0001-01-01	9999-12-31	mu	Mahanagar Telephone (Mauritius) Ltd.	\N	t	t	\N	\N	\N
2501	617-10	0001-01-01	9999-12-31	mu	Emtel	Emtel	t	t	\N	\N	\N
2502	334-020	0001-01-01	9999-12-31	mx	Telcel	\N	t	t	\N	\N	\N
2503	550-01	0001-01-01	9999-12-31	fm	FSM Telecom	FSM Telecom	t	t	\N	\N	\N
2505	259-02	0001-01-01	9999-12-31	md	Moldcell GSM	Moldcell	t	t	\N	\N	\N
2506	259-04	0001-01-01	9999-12-31	md	Eventis Mobile GSM	Evntis	t	t	\N	\N	\N
2507	259-05	0001-01-01	9999-12-31	md	J.S.C. Moldtelecom/3G UMTS (W-CDMA)	\N	t	t	\N	\N	\N
2508	259-99	0001-01-01	9999-12-31	md	J.S.C. Moldtelecom	\N	t	t	\N	\N	\N
2509	428-99	0001-01-01	9999-12-31	mn	Mobicom	MobiCom	t	t	\N	\N	\N
2510	297-01	0001-01-01	9999-12-31	me	Telenor Montenegro	Telenor	t	t	\N	\N	\N
2512	297-03	0001-01-01	9999-12-31	me	Mtel Montenegro	m:tel CG	t	t	\N	\N	\N
2513	354-860	0001-01-01	9999-12-31	ms	Cable & Wireless (West Indies) Ltd trading as Lime	Cable & Wireless West Indies (Montserrat)	t	t	\N	\N	\N
2514	604-00	0001-01-01	9999-12-31	ma	Méditélécom (GSM)	Méditel	t	t	\N	\N	\N
2515	604-01	0001-01-01	9999-12-31	ma	Ittissalat Al Maghrid	IAM	t	t	\N	\N	\N
2516	643-01	0001-01-01	9999-12-31	mz	T.D.M. GSM	mCel	t	t	\N	\N	\N
2517	643-03	0001-01-01	9999-12-31	mz	Movitel	Movitel	t	t	\N	\N	\N
2518	643-04	0001-01-01	9999-12-31	mz	VM Sarl	Vodacom	t	t	\N	\N	\N
2519	414-01	0001-01-01	9999-12-31	mm	Myanmar Post and Telecommunication	MPT	t	t	\N	\N	\N
2520	649-01	0001-01-01	9999-12-31	na	Mobile Telecommunications Ltd.	MTC	t	t	\N	\N	\N
2521	649-02	0001-01-01	9999-12-31	na	Telecom Namibia	Switch	t	t	\N	\N	\N
2522	649-03	0001-01-01	9999-12-31	na	Powercom Pty Ltd (leo)	Leo	t	t	\N	\N	\N
2523	429-01	0001-01-01	9999-12-31	np	Nepal Telecommunications	Namaste / Nt Mobile	t	t	\N	\N	\N
2524	204-02	0001-01-01	9999-12-31	nl	Tele2 (Netherlands) B.V.	Tele2	t	t	\N	\N	\N
2525	204-03	0001-01-01	9999-12-31	nl	Blyk N.V.	Voiceworks B.V	t	t	\N	\N	\N
2527	204-05	0001-01-01	9999-12-31	nl	Elephant Talk Comm. Premium Rate Serv. Neth. B.V.	Elephant Talk Communications	t	t	\N	\N	\N
2528	204-06	0001-01-01	9999-12-31	nl	Barablu Mobile Benelux Ltd	Mundio Mobile	t	t	\N	\N	\N
2529	204-07	0001-01-01	9999-12-31	nl	Teleena holding B.V.	Teleena	t	t	\N	\N	\N
2530	204-08	0001-01-01	9999-12-31	nl	KPN Mobile The Netherlands B.V.	KPN	t	t	\N	\N	\N
2531	204-10	0001-01-01	9999-12-31	nl	KPN B.V.	KPN	t	t	\N	\N	\N
2532	204-12	0001-01-01	9999-12-31	nl	Telfort B.V.	Telfort	t	t	\N	\N	\N
2533	204-14	0001-01-01	9999-12-31	nl	INMO B.V.	6GMobile	t	t	\N	\N	\N
2534	204-16	0001-01-01	9999-12-31	nl	T-Mobile Netherlands B.V.	T-mobile	t	t	\N	\N	\N
2535	204-18	0001-01-01	9999-12-31	nl	Telfort B.V.	UPC	t	t	\N	\N	\N
2536	204-20	0001-01-01	9999-12-31	nl	Orange Nederland N.V.	T-mobile	t	t	\N	\N	\N
2537	204-21	0001-01-01	9999-12-31	nl	ProRail B.V.	ProRail B.V	t	t	\N	\N	\N
2538	204-60	0001-01-01	9999-12-31	nl	KPN B.V.	\N	t	t	\N	\N	\N
2539	204-69	0001-01-01	9999-12-31	nl	KPN Mobile The Netherlands B.V.	KPN	t	t	\N	\N	\N
2540	546-01	0001-01-01	9999-12-31	nc	OPT Mobilis	Mobilis	t	t	\N	\N	\N
2541	530-00	0001-01-01	9999-12-31	nz	Reserved for AMPS MIN based IMSIs	Telecom	t	t	\N	\N	\N
2543	530-02	0001-01-01	9999-12-31	nz	Teleom New Zealand CDMA Network	Telecom	t	t	\N	\N	\N
2544	530-03	0001-01-01	9999-12-31	nz	Woosh Wireless - CDMA Network	Woosh	t	t	\N	\N	\N
2545	530-04	0001-01-01	9999-12-31	nz	TelstraClear - GSM Network	TelstraClear	t	t	\N	\N	\N
2629	260-27	0001-01-01	9999-12-31	pl	Intertelcom / Intertelcom Sp. z o.o.	\N	t	t	\N	\N	\N
2546	530-05	0001-01-01	9999-12-31	nz	Telecom New Zealand - UMTS Ntework	XT Mobile (Telecom)	t	t	\N	\N	\N
2547	530-06	0001-01-01	9999-12-31	nz	FX Networks Ltd	Skinny	t	t	\N	\N	\N
2548	530-07	0001-01-01	9999-12-31	nz	Bluereach Limited	\N	t	t	\N	\N	\N
2549	530-24	0001-01-01	9999-12-31	nz	NZ Communications - UMTS Network	2degrees	t	t	\N	\N	\N
2550	710-21	0001-01-01	9999-12-31	ni	Empresa Nicaragüense de Telecomunicaciones, S.A. (ENITEL)	Claro	t	t	\N	\N	\N
2551	710-73	0001-01-01	9999-12-31	ni	Servicios de Comunicaciones, S.A. (SERCOM)	SERCOM	t	t	\N	\N	\N
2552	614-01	0001-01-01	9999-12-31	ne	Sahel.Com	SahelCom	t	t	\N	\N	\N
2553	614-02	0001-01-01	9999-12-31	ne	Celtel	Zain	t	t	\N	\N	\N
2554	614-03	0001-01-01	9999-12-31	ne	Telecel	Telecel	t	t	\N	\N	\N
2555	621-20	0001-01-01	9999-12-31	ng	Econet Wireless Nigeria Ltd.	Airtel	t	t	\N	\N	\N
2556	621-30	0001-01-01	9999-12-31	ng	MTN Nigeria Communications	MTN	t	t	\N	\N	\N
2557	621-40	0001-01-01	9999-12-31	ng	MTEL	M-Tel	t	t	\N	\N	\N
2558	621-50	0001-01-01	9999-12-31	ng	Globacom	Glo	t	t	\N	\N	\N
2559	621-60	0001-01-01	9999-12-31	ng	EMTS	Etisalat	t	t	\N	\N	\N
2560	555-01	0001-01-01	9999-12-31	nu	Telecom Niue	\N	t	t	\N	\N	\N
2561	242-01	0001-01-01	9999-12-31	no	Telenor Norge AS	Telenor	t	t	\N	\N	\N
2562	242-02	0001-01-01	9999-12-31	no	NetCom AS	Netcom	t	t	\N	\N	\N
2563	242-03	0001-01-01	9999-12-31	no	Teletopia Gruppen AS	MTU	t	t	\N	\N	\N
2564	242-04	0001-01-01	9999-12-31	no	Tele2 Norge AS	Tele2	t	t	\N	\N	\N
2565	242-05	0001-01-01	9999-12-31	no	Network Norway AS	Network Norway	t	t	\N	\N	\N
2566	242-06	0001-01-01	9999-12-31	no	ICE Norge AS	ICE	t	t	\N	\N	\N
2567	242-07	0001-01-01	9999-12-31	no	Ventelo Bedrift AS	Ventelo	t	t	\N	\N	\N
2568	242-08	0001-01-01	9999-12-31	no	TDC AS	TDC	t	t	\N	\N	\N
2569	242-09	0001-01-01	9999-12-31	no	Com4 AS	Com4	t	t	\N	\N	\N
2570	242-10	0001-01-01	9999-12-31	no	Post-og teletilsynet	\N	t	t	\N	\N	\N
2571	242-11	0001-01-01	9999-12-31	no	Systemnet AS	Systemnet AS	t	t	\N	\N	\N
2572	242-12	0001-01-01	9999-12-31	no	Telenor Norge AS	Telenor	t	t	\N	\N	\N
2573	242-20	0001-01-01	9999-12-31	no	Jernbaneverket	Jernbaneverket	t	t	\N	\N	\N
2574	242-21	0001-01-01	9999-12-31	no	Jernbaneverket	Jernbaneverket	t	t	\N	\N	\N
2575	242-22	0001-01-01	9999-12-31	no	Network Norway AS	Network Norway	t	t	\N	\N	\N
2576	242-23	0001-01-01	9999-12-31	no	Lycamobile Norway Ltd	Lyca Mobile Ltd	t	t	\N	\N	\N
2577	242-24	0001-01-01	9999-12-31	no	Mobile Norway AS	\N	t	t	\N	\N	\N
2578	422-02	0001-01-01	9999-12-31	om	Oman Mobile Telecommunications Company (Oman Mobile)	Oman Mobile	t	t	\N	\N	\N
2579	422-03	0001-01-01	9999-12-31	om	Oman Qatari Telecommunications Company (Nawras)	Nawras	t	t	\N	\N	\N
2580	422-04	0001-01-01	9999-12-31	om	Oman Telecommunications Company (Omantel)	Oman Telecommunications Company (Omantel)	t	t	\N	\N	\N
2526	204-04	0001-01-01	9999-12-31	nl	Vodafone Libertel N.V.	Vodafone nl	t	t	\N	\N	\N
2498	617-01	0001-01-01	9999-12-31	mu	Cellplus	Orange mu	t	t	\N	\N	\N
2504	259-01	0001-01-01	9999-12-31	md	Orange Moldova GSM	Orange md	t	t	\N	\N	\N
2581	410-01	0001-01-01	9999-12-31	pk	Mobilink	Mobilink	t	t	\N	\N	\N
2582	410-03	0001-01-01	9999-12-31	pk	PAK Telecom Mobile Ltd. (UFONE)	Ufone	t	t	\N	\N	\N
2583	410-04	0001-01-01	9999-12-31	pk	CMPak	Zong	t	t	\N	\N	\N
2584	410-06	0001-01-01	9999-12-31	pk	Telenor Pakistan	Telenor	t	t	\N	\N	\N
2585	410-07	0001-01-01	9999-12-31	pk	Warid Telecom	Warid	t	t	\N	\N	\N
2586	552-01	0001-01-01	9999-12-31	pw	Palau National Communications Corp. (a.k.a. PNCC)	PNCC	t	t	\N	\N	\N
2587	714-01	0001-01-01	9999-12-31	pa	Cable & Wireless Panama S.A.	Cable & Wireless	t	t	\N	\N	\N
2588	714-02	0001-01-01	9999-12-31	pa	BSC de Panama S.A.	movistar	t	t	\N	\N	\N
2589	714-020	0001-01-01	9999-12-31	pa	Telefónica Móviles Panamá S.A.	\N	t	t	\N	\N	\N
2590	714-03	0001-01-01	9999-12-31	pa	Claro Panamá, S.A.	Claro	t	t	\N	\N	\N
2591	714-04	0001-01-01	9999-12-31	pa	Digicel (Panamá), S.A.	Digicel	t	t	\N	\N	\N
2592	537-01	0001-01-01	9999-12-31	pg	Bmobile	B-Mobile	t	t	\N	\N	\N
2593	537-02	0001-01-01	9999-12-31	pg	Greencom	Greencom	t	t	\N	\N	\N
2594	537-03	0001-01-01	9999-12-31	pg	Digicel Ltd	Digicel	t	t	\N	\N	\N
2595	744-01	0001-01-01	9999-12-31	py	Hola Paraguay S.A.	VOX	t	t	\N	\N	\N
2596	744-02	0001-01-01	9999-12-31	py	Hutchison Telecom S.A.	Claro	t	t	\N	\N	\N
2597	744-03	0001-01-01	9999-12-31	py	Compañia Privada de Comunicaciones S.A.	Compañia Privada de Comunicaciones S.A.	t	t	\N	\N	\N
2598	716-10	0001-01-01	9999-12-31	pe	TIM Peru	Claro	t	t	\N	\N	\N
2599	515-01	0001-01-01	9999-12-31	ph	Islacom	Islacom	t	t	\N	\N	\N
2600	515-02	0001-01-01	9999-12-31	ph	Globe Telecom	Globe	t	t	\N	\N	\N
2601	515-03	0001-01-01	9999-12-31	ph	Smart Communications	Smart	t	t	\N	\N	\N
2602	515-05	0001-01-01	9999-12-31	ph	Digitel	Sun	t	t	\N	\N	\N
2603	260-01	0001-01-01	9999-12-31	pl	Plus / Polkomtel S.A.	Plus (Polkomtel)	t	t	\N	\N	\N
2606	260-04	0001-01-01	9999-12-31	pl	LTE / CenterNet S.A.	Tele 2 (Netia)	t	t	\N	\N	\N
2607	260-05	0001-01-01	9999-12-31	pl	Orange(UMTS) / PTK Centertel Sp. z o.o.	Polska Telefonia Komórkowa Centertel Sp. z o.o.	t	t	\N	\N	\N
2608	260-06	0001-01-01	9999-12-31	pl	Play / P4 Sp. z o.o.	Play (P4)	t	t	\N	\N	\N
2609	260-07	0001-01-01	9999-12-31	pl	Netia / Netia S.A.	Netia (Using P4 Nw)	t	t	\N	\N	\N
2610	260-08	0001-01-01	9999-12-31	pl	E-Telko / E-Telko Sp. z o.o.	E-Telko Sp. z o.o.	t	t	\N	\N	\N
2611	260-09	0001-01-01	9999-12-31	pl	Lycamobile / Lycamobile Sp. z o.o.	Telekomunikacja Kolejowa (GSM-R)	t	t	\N	\N	\N
2612	260-10	0001-01-01	9999-12-31	pl	Sferia / Sferia S.A.	Sferia (Using T-mobile)	t	t	\N	\N	\N
2613	260-11	0001-01-01	9999-12-31	pl	Nordisk Polska / Nordisk Polska Sp. z o.o.	\N	t	t	\N	\N	\N
2614	260-12	0001-01-01	9999-12-31	pl	Cyfrowy Polsat / Cyfrowy Polsat S.A.	Cyfrowy Polsat	t	t	\N	\N	\N
2615	260-13	0001-01-01	9999-12-31	pl	Sferia / Sferia S.A.	\N	t	t	\N	\N	\N
2616	260-14	0001-01-01	9999-12-31	pl	Sferia / Sferia S.A.	Sferia (Using T-mobile)	t	t	\N	\N	\N
2617	260-15	0001-01-01	9999-12-31	pl	CenterNet / CenterNet S.A.	CenterNet (UMTS Data only)	t	t	\N	\N	\N
2618	260-16	0001-01-01	9999-12-31	pl	Mobyland / Mobyland Sp. z o.o.	Mobyland (UMTS)	t	t	\N	\N	\N
2619	260-17	0001-01-01	9999-12-31	pl	Aero 2 / Aero 2 Sp. z o.o.	Aero2 (UMTS)	t	t	\N	\N	\N
2620	260-18	0001-01-01	9999-12-31	pl	AMD Telecom / AMD Telecom S.A.	\N	t	t	\N	\N	\N
2621	260-19	0001-01-01	9999-12-31	pl	Teleena / Teleena Holding BV	\N	t	t	\N	\N	\N
2622	260-20	0001-01-01	9999-12-31	pl	Mobile.Net / Mobile.Net Sp. z o.o.	\N	t	t	\N	\N	\N
2623	260-21	0001-01-01	9999-12-31	pl	Exteri / Exteri Sp. z o.o.	\N	t	t	\N	\N	\N
2624	260-22	0001-01-01	9999-12-31	pl	Arcomm / Arcomm Sp. z o.o.	\N	t	t	\N	\N	\N
2625	260-23	0001-01-01	9999-12-31	pl	Amicomm / Amicomm Sp. z o.o.	\N	t	t	\N	\N	\N
2626	260-24	0001-01-01	9999-12-31	pl	WideNet / WideNet Sp. z o.o.	\N	t	t	\N	\N	\N
2627	260-25	0001-01-01	9999-12-31	pl	BS&T / Best Solutions & Technology Sp. z o.o.	\N	t	t	\N	\N	\N
2628	260-26	0001-01-01	9999-12-31	pl	ATE / ATE-Advanced Technology & Experience Sp. z o.o.	\N	t	t	\N	\N	\N
2630	260-28	0001-01-01	9999-12-31	pl	PhoneNet / PhoneNet Sp. z o.o.	\N	t	t	\N	\N	\N
2631	260-29	0001-01-01	9999-12-31	pl	Interfonica / Interfonica Sp. z o.o.	\N	t	t	\N	\N	\N
2632	260-30	0001-01-01	9999-12-31	pl	GrandTel / GrandTel Sp. z o.o.	\N	t	t	\N	\N	\N
2633	260-31	0001-01-01	9999-12-31	pl	Phone IT / Phone IT Sp. z o.o.	\N	t	t	\N	\N	\N
2634	260-32	0001-01-01	9999-12-31	pl	Compatel Ltd / COMPATEL LIMITED	\N	t	t	\N	\N	\N
2635	260-33	0001-01-01	9999-12-31	pl	Truphone Poland / Truphone Poland Sp. Z o.o.	\N	t	t	\N	\N	\N
2636	260-34	0001-01-01	9999-12-31	pl	T-Mobile / PTC S.A.	\N	t	t	\N	\N	\N
2637	260-98	0001-01-01	9999-12-31	pl	Play (testowy) / P4 Sp. z o.o.	\N	t	t	\N	\N	\N
2639	268-03	0001-01-01	9999-12-31	pt	Optimus - Telecomunicaçôes, S.A.	Optimus	t	t	\N	\N	\N
2640	268-05	0001-01-01	9999-12-31	pt	Oniway - Inforcomunicaçôes, S.A.	Oniway - Inforcomunicaçôes, S.A.	t	t	\N	\N	\N
2641	268-06	0001-01-01	9999-12-31	pt	TMN - Telecomunicaçôes Movéis Nacionais, S.A.	TMN	t	t	\N	\N	\N
2642	427-01	0001-01-01	9999-12-31	qa	QATARNET	Qtel	t	t	\N	\N	\N
2643	226-01	0001-01-01	9999-12-31	ro	Vodafone	Vodafone Romania SA	t	t	\N	\N	\N
2644	226-02	0001-01-01	9999-12-31	ro	Romtelecom	Romtelecom	t	t	\N	\N	\N
2645	226-03	0001-01-01	9999-12-31	ro	Cosmote	Cosmote (Zapp)	t	t	\N	\N	\N
2646	226-04	0001-01-01	9999-12-31	ro	Cosmote	Cosmote (Zapp)	t	t	\N	\N	\N
2647	226-05	0001-01-01	9999-12-31	ro	Digi.Mobil	Digi mobil	t	t	\N	\N	\N
2648	226-06	0001-01-01	9999-12-31	ro	Cosmote	Cosmote	t	t	\N	\N	\N
2649	226-10	0001-01-01	9999-12-31	ro	Orange	Orange Romania	t	t	\N	\N	\N
2650	226-11	0001-01-01	9999-12-31	ro	Enigma-System	\N	t	t	\N	\N	\N
2651	250-01	0001-01-01	9999-12-31	ru	Mobile Telesystems	MTS	t	t	\N	\N	\N
2652	250-02	0001-01-01	9999-12-31	ru	Megafon	MegaFon	t	t	\N	\N	\N
2653	250-03	0001-01-01	9999-12-31	ru	Nizhegorodskaya Cellular Communications	NCC	t	t	\N	\N	\N
2654	250-04	0001-01-01	9999-12-31	ru	Sibchallenge	Sibchallenge	t	t	\N	\N	\N
2655	250-05	0001-01-01	9999-12-31	ru	Mobile Comms System	ETK	t	t	\N	\N	\N
2656	250-07	0001-01-01	9999-12-31	ru	BM Telecom	SMARTS	t	t	\N	\N	\N
2657	250-10	0001-01-01	9999-12-31	ru	Don Telecom	DTC	t	t	\N	\N	\N
2658	250-11	0001-01-01	9999-12-31	ru	Orensot	Orensot	t	t	\N	\N	\N
2659	250-12	0001-01-01	9999-12-31	ru	Baykal Westcom	Baykal	t	t	\N	\N	\N
2660	250-13	0001-01-01	9999-12-31	ru	Kuban GSM	KUGSM	t	t	\N	\N	\N
2661	250-16	0001-01-01	9999-12-31	ru	New Telephone Company	NTC	t	t	\N	\N	\N
2662	250-17	0001-01-01	9999-12-31	ru	Ermak RMS	Utel	t	t	\N	\N	\N
2663	250-19	0001-01-01	9999-12-31	ru	Volgograd Mobile	Indigo	t	t	\N	\N	\N
2664	250-20	0001-01-01	9999-12-31	ru	ECC	Tele2	t	t	\N	\N	\N
2665	250-28	0001-01-01	9999-12-31	ru	Extel	Beeline	t	t	\N	\N	\N
2605	260-03	0001-01-01	9999-12-31	pl	Orange / PTK Centertel Sp. z o.o.	Orange pl	t	t	\N	\N	\N
2666	250-39	0001-01-01	9999-12-31	ru	Uralsvyazinform	Utel	t	t	\N	\N	\N
2667	250-44	0001-01-01	9999-12-31	ru	Stuvtelesot	Stuvtelesot	t	t	\N	\N	\N
2668	250-92	0001-01-01	9999-12-31	ru	Printelefone	MTS - Primtelefon	t	t	\N	\N	\N
2669	250-93	0001-01-01	9999-12-31	ru	Telecom XXI	Telecom XXI	t	t	\N	\N	\N
2670	250-99	0001-01-01	9999-12-31	ru	Beeline	Beeline	t	t	\N	\N	\N
2671	635-10	0001-01-01	9999-12-31	rw	MTN Rwandacell	MTN	t	t	\N	\N	\N
2672	635-14	0001-01-01	9999-12-31	rw	AIRTEL RWANDA Ltd	\N	t	t	\N	\N	\N
2673	356-110	0001-01-01	9999-12-31	kn	Cable & Wireless St Kitts & Nevis Ltd trading as Lime	LIME	t	t	\N	\N	\N
2674	358-110	0001-01-01	9999-12-31	lc	Cable & Wireless (St Lucia) Ltd trading as Lime	Lime (Cable & Wireless)	t	t	\N	\N	\N
2675	308-01	0001-01-01	9999-12-31	pm	St. Pierre-et-Miquelon Télécom	Ameris	t	t	\N	\N	\N
2676	360-110	0001-01-01	9999-12-31	vc	Cable & Wireless St Vincent and the Grenadines Ltd trading as lime	Lime (Cable & Wireless)	t	t	\N	\N	\N
2677	549-01	0001-01-01	9999-12-31	ws	Telecom Samoa Cellular Ltd.	Digicel	t	t	\N	\N	\N
2678	549-27	0001-01-01	9999-12-31	ws	GoMobile SamoaTel Ltd	SamoaTel	t	t	\N	\N	\N
2679	292-01	0001-01-01	9999-12-31	sm	Prima San Marino / San Marino Telecom	\N	t	t	\N	\N	\N
2680	626-01	0001-01-01	9999-12-31	st	Companhia Santomese de Telecomunicações	CSTmovel	t	t	\N	\N	\N
2681	420-01	0001-01-01	9999-12-31	sa	Saudi Telecom	Al Jawal	t	t	\N	\N	\N
2682	420-03	0001-01-01	9999-12-31	sa	Etihad Etisalat Company (Mobily)	Mobily	t	t	\N	\N	\N
2684	608-02	0001-01-01	9999-12-31	sn	Sentel GSM	Tigo	t	t	\N	\N	\N
2685	608-03	0001-01-01	9999-12-31	sn	Expresso Sénégal	Expresso	t	t	\N	\N	\N
2686	220-01	0001-01-01	9999-12-31	rs	Telenor d.o.o.	Telenor	t	t	\N	\N	\N
2687	220-03	0001-01-01	9999-12-31	rs	Telekom Srbija a.d.	mt:s	t	t	\N	\N	\N
2688	220-05	0001-01-01	9999-12-31	rs	Vip mobile d.o.o.	VIP	t	t	\N	\N	\N
2689	633-01	0001-01-01	9999-12-31	sc	Cable & Wireless (Seychelles) Ltd.	Cable & Wireless	t	t	\N	\N	\N
2690	633-02	0001-01-01	9999-12-31	sc	Mediatech International Ltd.	Mediatech International	t	t	\N	\N	\N
2691	633-10	0001-01-01	9999-12-31	sc	Telecom (Seychelles) Ltd.	Airtel	t	t	\N	\N	\N
2692	619-01	0001-01-01	9999-12-31	sl	Celtel	Airtel	t	t	\N	\N	\N
2693	619-02	0001-01-01	9999-12-31	sl	Millicom	Tigo	t	t	\N	\N	\N
2694	619-03	0001-01-01	9999-12-31	sl	Africell	Africell	t	t	\N	\N	\N
2695	619-04	0001-01-01	9999-12-31	sl	Comium (Sierra Leone) Ltd.	Comium	t	t	\N	\N	\N
2696	619-05	0001-01-01	9999-12-31	sl	Lintel (Sierra Leone) Ltd.	Africell	t	t	\N	\N	\N
2697	619-25	0001-01-01	9999-12-31	sl	Mobitel	Mobitel	t	t	\N	\N	\N
2698	619-40	0001-01-01	9999-12-31	sl	Datatel (SL) Ltd GSM	Datatel (SL) Ltd GSM	t	t	\N	\N	\N
2699	619-50	0001-01-01	9999-12-31	sl	Datatel (SL) Ltd CDMA	Dtatel (SL) Ltd CDMA	t	t	\N	\N	\N
2700	525-01	0001-01-01	9999-12-31	sg	SingTel ST GSM900	SingTel	t	t	\N	\N	\N
2701	525-02	0001-01-01	9999-12-31	sg	SingTel ST GSM1800	SingTel-G18	t	t	\N	\N	\N
2702	525-03	0001-01-01	9999-12-31	sg	MobileOne	M1	t	t	\N	\N	\N
2703	525-05	0001-01-01	9999-12-31	sg	Starhub	StarHub	t	t	\N	\N	\N
2704	525-12	0001-01-01	9999-12-31	sg	Digital Trunked Radio Network	Digital Trunked Radio Network	t	t	\N	\N	\N
2709	293-40	0001-01-01	9999-12-31	si	SI Mobil	Si.mobil	t	t	\N	\N	\N
2710	293-41	0001-01-01	9999-12-31	si	Mobitel	Mobitel	t	t	\N	\N	\N
2711	293-64	0001-01-01	9999-12-31	si	T-2 d.o.o.	T-2	t	t	\N	\N	\N
2712	293-70	0001-01-01	9999-12-31	si	Tusmobil d.o.o.	Tušmobil	t	t	\N	\N	\N
2713	540-02	0001-01-01	9999-12-31	sb	Bemobile (BMobile (SI) Ltd)	\N	t	t	\N	\N	\N
2714	637-30	0001-01-01	9999-12-31	so	Golis Telecommunications Company	Golis	t	t	\N	\N	\N
2715	637-70	0001-01-01	9999-12-31	so	Onkod Telecom Ltd.	\N	t	t	\N	\N	\N
2716	655-01	0001-01-01	9999-12-31	za	Vodacom (Pty) Ltd.	Vodacom	t	t	\N	\N	\N
2717	655-02	0001-01-01	9999-12-31	za	Telkom SA Ltd	Telkom Mobile / 8.ta	t	t	\N	\N	\N
2718	655-04	0001-01-01	9999-12-31	za	Sasol (Pty) Ltd	Sasol (PTY) LTD	t	t	\N	\N	\N
2719	655-06	0001-01-01	9999-12-31	za	Sentech (Pty) Ltd.	Sentech	t	t	\N	\N	\N
2720	655-07	0001-01-01	9999-12-31	za	Cell C (Pty) Ltd.	Cell C & Virgin	t	t	\N	\N	\N
2721	655-10	0001-01-01	9999-12-31	za	Mobile Telephone Networks (MTN) Pty Ltd	MTN	t	t	\N	\N	\N
2722	655-11	0001-01-01	9999-12-31	za	SAPS Gauteng	SAPS Gauteng	t	t	\N	\N	\N
2723	655-12	0001-01-01	9999-12-31	za	Mobile Telephone Networks (MTN) Pty Ltd	\N	t	t	\N	\N	\N
2724	655-13	0001-01-01	9999-12-31	za	Neotel Pty Ltd	Neotel	t	t	\N	\N	\N
2725	655-19	0001-01-01	9999-12-31	za	Wireless Business Solutions (iBurst)	iBurst	t	t	\N	\N	\N
2726	655-21	0001-01-01	9999-12-31	za	Cape Town Metropolitan Council	Cape Town Metropolitan Council	t	t	\N	\N	\N
2727	655-25	0001-01-01	9999-12-31	za	Wirels Connect	Wirels Connect	t	t	\N	\N	\N
2728	655-27	0001-01-01	9999-12-31	za	A to Z Vaal Industrial Supplies Pty Ltd	\N	t	t	\N	\N	\N
2729	655-30	0001-01-01	9999-12-31	za	Bokamoso Consortium Pty Ltd	Bokamoso Consortium	t	t	\N	\N	\N
2730	655-31	0001-01-01	9999-12-31	za	Karabo Telecoms (Pty) Ltd.	Karabo Telecoms (Pty) Ltd.	t	t	\N	\N	\N
2731	655-32	0001-01-01	9999-12-31	za	Ilizwi Telecommunications Pty Ltd	Ilizwi Telecommunications	t	t	\N	\N	\N
2732	655-33	0001-01-01	9999-12-31	za	Thinta Thinta Telecommunications Pty Ltd	Thinta Thinta Telecommunications	t	t	\N	\N	\N
2733	655-34	0001-01-01	9999-12-31	za	Bokone Telecoms Pty Ltd	Bokone Telecoms	t	t	\N	\N	\N
2734	655-35	0001-01-01	9999-12-31	za	Kingdom Communications Pty Ltd	Kingdom Communications	t	t	\N	\N	\N
2735	655-36	0001-01-01	9999-12-31	za	Amatole Telecommunication Pty Ltd	Amatole Telecommunication Services	t	t	\N	\N	\N
2736	655-41	0001-01-01	9999-12-31	za	South African Police Service	South African Police Service	t	t	\N	\N	\N
2737	659-12	0001-01-01	9999-12-31	ss	Sudani/Sudatel	\N	t	t	\N	\N	\N
2738	659-91	0001-01-01	9999-12-31	ss	Zain-South Sudan	\N	t	t	\N	\N	\N
2739	659-92	0001-01-01	9999-12-31	ss	MTN-South Sudan	\N	t	t	\N	\N	\N
2740	659-95	0001-01-01	9999-12-31	ss	Vivacel/NOW	\N	t	t	\N	\N	\N
2741	659-97	0001-01-01	9999-12-31	ss	Gemtel	\N	t	t	\N	\N	\N
2744	214-04	0001-01-01	9999-12-31	es	Xfera Móviles, SA	Yoigo	t	t	\N	\N	\N
2745	214-05	0001-01-01	9999-12-31	es	Telefónica Móviles España, SAU	TME	t	t	\N	\N	\N
2747	214-07	0001-01-01	9999-12-31	es	Telefónica Móviles España, SAU	movistar	t	t	\N	\N	\N
2748	214-08	0001-01-01	9999-12-31	es	Euskaltel, SA	Euskaltel	t	t	\N	\N	\N
2742	214-01	0001-01-01	9999-12-31	es	Vodafone España, SAU	Vodafone es	t	t	\N	\N	\N
2746	214-06	0001-01-01	9999-12-31	es	Vodafone España, SAU	Vodafone es	t	t	\N	\N	\N
2683	608-01	0001-01-01	9999-12-31	sn	Sonatel	Orange sn	t	t	\N	\N	\N
2705	231-01	0001-01-01	9999-12-31	sk	Orange, GSM	Orange sk	t	t	\N	\N	\N
2750	214-10	0001-01-01	9999-12-31	es	Operadora de Telecomunicaciones Opera SL	\N	t	t	\N	\N	\N
2751	214-11	0001-01-01	9999-12-31	es	France Telecom España SA	\N	t	t	\N	\N	\N
2752	214-12	0001-01-01	9999-12-31	es	Contacta Servicios Avanzados de Telecomunicaciones SL	\N	t	t	\N	\N	\N
2753	214-13	0001-01-01	9999-12-31	es	Incotel Ingeniera y Consultaria SL	\N	t	t	\N	\N	\N
2754	214-14	0001-01-01	9999-12-31	es	Incotel Servicioz Avanzados SL	\N	t	t	\N	\N	\N
2755	214-15	0001-01-01	9999-12-31	es	BT España Compañia de Servicios Globales de Telecomunicaciones, SAU	BT	t	t	\N	\N	\N
2756	214-16	0001-01-01	9999-12-31	es	Telecable de Asturias, SAU	TeleCable	t	t	\N	\N	\N
2757	214-17	0001-01-01	9999-12-31	es	R Cable y Telecomunicaciones Galicia, SA	Móbil R	t	t	\N	\N	\N
2758	214-18	0001-01-01	9999-12-31	es	Cableuropa, SAU	ONO	t	t	\N	\N	\N
2759	214-19	0001-01-01	9999-12-31	es	E-Plus Móviles, SL	Simyo	t	t	\N	\N	\N
2760	214-20	0001-01-01	9999-12-31	es	Fonyou Telecom, SL	Fonyou	t	t	\N	\N	\N
2761	214-21	0001-01-01	9999-12-31	es	Jazz Telecom, SAU	Jazztel	t	t	\N	\N	\N
2762	214-22	0001-01-01	9999-12-31	es	Best Spain Telecom, SL	DigiMobil	t	t	\N	\N	\N
2763	214-24	0001-01-01	9999-12-31	es	Vizzavi España, SL	Eroski	t	t	\N	\N	\N
2764	214-25	0001-01-01	9999-12-31	es	Lycamobile, SL	LycaMobile	t	t	\N	\N	\N
2765	214-26	0001-01-01	9999-12-31	es	Lleida Networks Serveis Telemátics, SL	\N	t	t	\N	\N	\N
2766	214-27	0001-01-01	9999-12-31	es	SCN Truphone SL	\N	t	t	\N	\N	\N
2767	413-02	0001-01-01	9999-12-31	lk	MTN Network Ltd.	Dialog	t	t	\N	\N	\N
2768	413-03	0001-01-01	9999-12-31	lk	Celtel Lanka Ltd.	Etisalat	t	t	\N	\N	\N
2769	634-01	0001-01-01	9999-12-31	sd	SD Mobitel	Zain SD	t	t	\N	\N	\N
2770	634-02	0001-01-01	9999-12-31	sd	Areeba-Sudan	MTN	t	t	\N	\N	\N
2771	634-05	0001-01-01	9999-12-31	sd	Network of the World Ltd (NOW)	Vivacell (NOW)	t	t	\N	\N	\N
2772	634-06	0001-01-01	9999-12-31	sd	Zain Sudan	\N	t	t	\N	\N	\N
2773	634-99	0001-01-01	9999-12-31	sd	MTN Sudan	\N	t	t	\N	\N	\N
2774	746-02	0001-01-01	9999-12-31	sr	Telesur	Telesur	t	t	\N	\N	\N
2775	746-03	0001-01-01	9999-12-31	sr	Digicel	Digicel	t	t	\N	\N	\N
2776	746-04	0001-01-01	9999-12-31	sr	Intelsur	Uniqa	t	t	\N	\N	\N
2777	746-05	0001-01-01	9999-12-31	sr	Telesur (CDMA)	\N	t	t	\N	\N	\N
2778	653-01	0001-01-01	9999-12-31	sz	SPTC	\N	t	t	\N	\N	\N
2779	653-10	0001-01-01	9999-12-31	sz	Swazi MTN	Swazi MTN	t	t	\N	\N	\N
2780	240-01	0001-01-01	9999-12-31	se	Telia Sonera Sverige AB	Telia	t	t	\N	\N	\N
2782	240-03	0001-01-01	9999-12-31	se	AINMT Sverige AB	Netett Sverige AB	t	t	\N	\N	\N
2783	240-04	0001-01-01	9999-12-31	se	3G Infrastructure Services AB	\N	t	t	\N	\N	\N
2784	240-05	0001-01-01	9999-12-31	se	Svenska UMTS-Nät AB	Sweden 3G (Telia/Tele2)	t	t	\N	\N	\N
2785	240-06	0001-01-01	9999-12-31	se	Telenor Sverige AB	Telenor	t	t	\N	\N	\N
2786	240-07	0001-01-01	9999-12-31	se	Tele2 Sverige AB	Tele2	t	t	\N	\N	\N
2787	240-08	0001-01-01	9999-12-31	se	Telenor Sverige AB	Telenor	t	t	\N	\N	\N
2788	240-09	0001-01-01	9999-12-31	se	Djuice Mobile Sweden, filial till Telenor Mobile Sweden AS	Djuice Mobile Sweden	t	t	\N	\N	\N
2789	240-10	0001-01-01	9999-12-31	se	Spring Mobil AB	Spring	t	t	\N	\N	\N
2790	240-11	0001-01-01	9999-12-31	se	Linholmen Science Park AB	Lindholmen Science Park	t	t	\N	\N	\N
2791	240-12	0001-01-01	9999-12-31	se	Barablu Mobile Scandinavia Ltd	Lycamobile	t	t	\N	\N	\N
2792	240-13	0001-01-01	9999-12-31	se	Ventelo Sverige AB	Ventelo	t	t	\N	\N	\N
2793	240-14	0001-01-01	9999-12-31	se	TDC Sverige AB	TDC	t	t	\N	\N	\N
2794	240-15	0001-01-01	9999-12-31	se	Wireless Maingate Nordic AB	Wireless Maingate	t	t	\N	\N	\N
2795	240-16	0001-01-01	9999-12-31	se	42IT AB	42 Telecom AB	t	t	\N	\N	\N
2796	240-17	0001-01-01	9999-12-31	se	Götalandsnätet AB	Götalandsnätet AB	t	t	\N	\N	\N
2797	240-18	0001-01-01	9999-12-31	se	Generic Mobile Systems Sweden AB	Generic Mobile Systems Sweden AB	t	t	\N	\N	\N
2798	240-19	0001-01-01	9999-12-31	se	Mundio Mobile Sweden Ltd	Mudio Mobile	t	t	\N	\N	\N
2799	240-20	0001-01-01	9999-12-31	se	iMEZ AB	Imez AB	t	t	\N	\N	\N
2800	240-21	0001-01-01	9999-12-31	se	Banverket	\N	t	t	\N	\N	\N
2801	240-22	0001-01-01	9999-12-31	se	EuTel AB	EuTel	t	t	\N	\N	\N
2802	240-23	0001-01-01	9999-12-31	se	Infobip LTD	Infobip Ltd	t	t	\N	\N	\N
2803	240-24	0001-01-01	9999-12-31	se	Net4Mobility HB	\N	t	t	\N	\N	\N
2804	240-26	0001-01-01	9999-12-31	se	Beepsend A.B.	Beepsend	t	t	\N	\N	\N
2805	240-27	0001-01-01	9999-12-31	se	MyIndian AB	MyIndian AB	t	t	\N	\N	\N
2806	240-28	0001-01-01	9999-12-31	se	CoolTEL Aps A.B.	CoolTEL Aps	t	t	\N	\N	\N
2807	240-29	0001-01-01	9999-12-31	se	Mercury International Carrier Services	Mercury International Carrier Services	t	t	\N	\N	\N
2808	240-30	0001-01-01	9999-12-31	se	NextGen Mobile Ltd	NextGen Mobile Ltd	t	t	\N	\N	\N
2809	240-31	0001-01-01	9999-12-31	se	Mobimax AB	\N	t	t	\N	\N	\N
2810	240-32	0001-01-01	9999-12-31	se	Compatel Ltd.	CompaTel Ltd.	t	t	\N	\N	\N
2811	240-33	0001-01-01	9999-12-31	se	Mobile Arts AB	\N	t	t	\N	\N	\N
2812	240-34	0001-01-01	9999-12-31	se	Tigo Ltd	Tigo LTD	t	t	\N	\N	\N
2813	240-35	0001-01-01	9999-12-31	se	42 Telecom LTD	\N	t	t	\N	\N	\N
2814	240-36	0001-01-01	9999-12-31	se	Interactive Digital Media GmbH	IDM	t	t	\N	\N	\N
2815	240-40	0001-01-01	9999-12-31	se	ReWiCom Scandinavia AB	\N	t	t	\N	\N	\N
2816	228-01	0001-01-01	9999-12-31	ch	Swisscom Schweiz AG	Swisscom	t	t	\N	\N	\N
2817	228-02	0001-01-01	9999-12-31	ch	Sunrise Communications AG	Sunrise	t	t	\N	\N	\N
2819	228-05	0001-01-01	9999-12-31	ch	Comfone AG	Togewanet AG (Comfone)	t	t	\N	\N	\N
2820	228-06	0001-01-01	9999-12-31	ch	SBB AG	SBB AG	t	t	\N	\N	\N
2821	228-08	0001-01-01	9999-12-31	ch	Tele2 Telecommunications AG	Tele2	t	t	\N	\N	\N
2822	228-12	0001-01-01	9999-12-31	ch	Sunrise Communications AG	Sunrise	t	t	\N	\N	\N
2823	228-51	0001-01-01	9999-12-31	ch	Bebbicell AG	Bebbicell AG	t	t	\N	\N	\N
2824	417-01	0001-01-01	9999-12-31	sy	Syriatel	Syriatel	t	t	\N	\N	\N
2825	417-02	0001-01-01	9999-12-31	sy	Spacetel Syria	MTN	t	t	\N	\N	\N
2826	417-09	0001-01-01	9999-12-31	sy	Syrian Telecom	\N	t	t	\N	\N	\N
2827	436-01	0001-01-01	9999-12-31	tj	JC Somoncom	Tcell	t	t	\N	\N	\N
2828	436-02	0001-01-01	9999-12-31	tj	CJSC Indigo Tajikistan	Tcell	t	t	\N	\N	\N
2829	436-03	0001-01-01	9999-12-31	tj	TT mobile	MLT	t	t	\N	\N	\N
2830	436-04	0001-01-01	9999-12-31	tj	Josa Babilon-T	Babilon-M	t	t	\N	\N	\N
2831	436-05	0001-01-01	9999-12-31	tj	CTJTHSC Tajik-tel	Beeline	t	t	\N	\N	\N
2832	640-02	0001-01-01	9999-12-31	tz	MIC (T) Ltd.	tiGO	t	t	\N	\N	\N
2833	640-03	0001-01-01	9999-12-31	tz	Zantel	Zantel	t	t	\N	\N	\N
2834	640-04	0001-01-01	9999-12-31	tz	Vodacom (T) Ltd.	Vodacom	t	t	\N	\N	\N
2835	640-05	0001-01-01	9999-12-31	tz	Celtel (T) Ltd.	Airtel	t	t	\N	\N	\N
2836	520-00	0001-01-01	9999-12-31	th	CAT CDMA	Hutch	t	t	\N	\N	\N
2837	520-01	0001-01-01	9999-12-31	th	AIS GSM	AIS	t	t	\N	\N	\N
2838	520-15	0001-01-01	9999-12-31	th	ACT Mobile	TOT 3G	t	t	\N	\N	\N
2840	294-02	0001-01-01	9999-12-31	mk	Cosmofon	Cosmofon	t	t	\N	\N	\N
2841	294-03	0001-01-01	9999-12-31	mk	Nov Operator	VIP Operator	t	t	\N	\N	\N
2842	294-10	0001-01-01	9999-12-31	mk	WTI Macedonia	\N	t	t	\N	\N	\N
2843	294-11	0001-01-01	9999-12-31	mk	MOBIK TELEKOMUNIKACII DOOEL- Skopje	\N	t	t	\N	\N	\N
2844	514-01	0001-01-01	9999-12-31	tl	Telin Timor-Leste	\N	t	t	\N	\N	\N
2845	514-02	0001-01-01	9999-12-31	tl	Timor Telecom	Timor Telecom	t	t	\N	\N	\N
2846	514-03	0001-01-01	9999-12-31	tl	Viettel Timor-Leste	\N	t	t	\N	\N	\N
2847	615-01	0001-01-01	9999-12-31	tg	Togo Telecom	Togo Cell	t	t	\N	\N	\N
2848	539-01	0001-01-01	9999-12-31	to	Tonga Communications Corporation	Tonga Communications Corporation	t	t	\N	\N	\N
2849	539-43	0001-01-01	9999-12-31	to	Digicel	Shoreline Communication	t	t	\N	\N	\N
2850	539-88	0001-01-01	9999-12-31	to	Digicel (Tonga) Ltd	Digicel	t	t	\N	\N	\N
2851	374-12	0001-01-01	9999-12-31	tt	TSTT Mobile	bMobile	t	t	\N	\N	\N
2852	374-130	0001-01-01	9999-12-31	tt	Digicel Trinidad and Tobago Ltd.	\N	t	t	\N	\N	\N
2853	374-140	0001-01-01	9999-12-31	tt	LaqTel Ltd.	LaqTel Ltd.	t	t	\N	\N	\N
2854	605-02	0001-01-01	9999-12-31	tn	Tunisie Telecom	Tunicell	t	t	\N	\N	\N
2855	605-03	0001-01-01	9999-12-31	tn	Orascom Telecom	Tunisiana	t	t	\N	\N	\N
2856	286-01	0001-01-01	9999-12-31	tr	Turkcell	Turkcell	t	t	\N	\N	\N
2858	286-03	0001-01-01	9999-12-31	tr	Aria	Avea	t	t	\N	\N	\N
2859	286-04	0001-01-01	9999-12-31	tr	Aycell	Aycell	t	t	\N	\N	\N
2860	438-01	0001-01-01	9999-12-31	tm	Barash Communication Technologies (BCTI)	MTS	t	t	\N	\N	\N
2861	438-02	0001-01-01	9999-12-31	tm	TM-Cell	TM-Cell	t	t	\N	\N	\N
2862	376-350	0001-01-01	9999-12-31	tc	Cable & Wireless (TCI) Ltd trading asLime	Lime (Cable & Wireless)	t	t	\N	\N	\N
2863	376-352	0001-01-01	9999-12-31	tc	IslandCom Communications Ltd.	Islandcom	t	t	\N	\N	\N
2864	376-360	0001-01-01	9999-12-31	tc	IslandCom Communication Ltd	\N	t	t	\N	\N	\N
2865	553-01	0001-01-01	9999-12-31	tv	Tuvalu Telecommunications Corporation	\N	t	t	\N	\N	\N
2866	641-01	0001-01-01	9999-12-31	ug	Celtel Uganda	Zain	t	t	\N	\N	\N
2867	641-10	0001-01-01	9999-12-31	ug	MTN Uganda Ltd.	MTN	t	t	\N	\N	\N
2868	641-11	0001-01-01	9999-12-31	ug	Uganda Telecom Ltd.	Uganda Telecom	t	t	\N	\N	\N
2870	641-18	0001-01-01	9999-12-31	ug	Sure Telecom Uganda Limited	\N	t	t	\N	\N	\N
2871	641-22	0001-01-01	9999-12-31	ug	Warid Telecom Uganda Ltd.	Warid Telecom	t	t	\N	\N	\N
2872	641-30	0001-01-01	9999-12-31	ug	Anupam Global Soft Uganda Limited	\N	t	t	\N	\N	\N
2873	641-33	0001-01-01	9999-12-31	ug	Smile Communications Uganda Limited	\N	t	t	\N	\N	\N
2874	641-40	0001-01-01	9999-12-31	ug	Civil Aviation Authority (CAA)	\N	t	t	\N	\N	\N
2875	641-44	0001-01-01	9999-12-31	ug	K2 Telecom Ltd	\N	t	t	\N	\N	\N
2876	641-66	0001-01-01	9999-12-31	ug	i-Tel Ltd	\N	t	t	\N	\N	\N
2877	255-01	0001-01-01	9999-12-31	ua	Ukrainian Mobile Communication, UMC	MTS	t	t	\N	\N	\N
2878	255-02	0001-01-01	9999-12-31	ua	Ukranian Radio Systems, URS	Beeline	t	t	\N	\N	\N
2879	255-03	0001-01-01	9999-12-31	ua	Kyivstar GSM	Kyivstar	t	t	\N	\N	\N
2880	255-04	0001-01-01	9999-12-31	ua	International Telecommunications Ltd.	IT	t	t	\N	\N	\N
2881	255-05	0001-01-01	9999-12-31	ua	Golden Telecom	Golden Telecom	t	t	\N	\N	\N
2882	255-06	0001-01-01	9999-12-31	ua	Astelit	life:)	t	t	\N	\N	\N
2883	255-07	0001-01-01	9999-12-31	ua	Ukrtelecom	Ukrtelecom	t	t	\N	\N	\N
2884	255-21	0001-01-01	9999-12-31	ua	CJSC - Telesystems of Ukraine	PEOPLEnet	t	t	\N	\N	\N
2885	424-02	0001-01-01	9999-12-31	ae	Etisalat	Etisalat	t	t	\N	\N	\N
2886	234-00	0001-01-01	9999-12-31	gb	British Telecom	BT	t	t	\N	\N	\N
2887	234-01	0001-01-01	9999-12-31	gb	Mapesbury Communications Ltd.	Vectone MObile	t	t	\N	\N	\N
2888	234-02	0001-01-01	9999-12-31	gb	O2 UK Ltd.	O2	t	t	\N	\N	\N
2889	234-03	0001-01-01	9999-12-31	gb	Jersey Airtel Ltd	Airtel-Vodafone	t	t	\N	\N	\N
2890	234-04	0001-01-01	9999-12-31	gb	FMS Solutions Ltd	FMS Solutions Ltd	t	t	\N	\N	\N
2891	234-05	0001-01-01	9999-12-31	gb	Colt Mobile Telecommunications Ltd	COLT Mobile Telecommunications Ltd	t	t	\N	\N	\N
2892	234-06	0001-01-01	9999-12-31	gb	Internet Computer Bureau Ltd	Internet Computer Bureau Ltd	t	t	\N	\N	\N
2893	234-07	0001-01-01	9999-12-31	gb	Cable & Wireless UK	Cable and Wireless Plc	t	t	\N	\N	\N
2894	234-08	0001-01-01	9999-12-31	gb	OnePhone (UK) Ltd	OnePhone Ltd	t	t	\N	\N	\N
2895	234-09	0001-01-01	9999-12-31	gb	Tismi BV	Tismi BV	t	t	\N	\N	\N
2896	234-10	0001-01-01	9999-12-31	gb	O2 UK Ltd.	O2	t	t	\N	\N	\N
2897	234-11	0001-01-01	9999-12-31	gb	O2 UK Ltd.	O2	t	t	\N	\N	\N
2898	234-12	0001-01-01	9999-12-31	gb	Network Rail Infrastructure Ltd	Railtrack Plc (UK)	t	t	\N	\N	\N
2899	234-13	0001-01-01	9999-12-31	gb	Network Rail Infrastructure Ltd	Railtrack Plc (UK)	t	t	\N	\N	\N
2900	234-14	0001-01-01	9999-12-31	gb	Hay Systems Ltd	Hay Systems Ltd	t	t	\N	\N	\N
2902	234-16	0001-01-01	9999-12-31	gb	Opal Telecom Ltd	Talk Talk	t	t	\N	\N	\N
2903	234-17	0001-01-01	9999-12-31	gb	Flextel Ltd	Flextel Ltd	t	t	\N	\N	\N
2904	234-18	0001-01-01	9999-12-31	gb	Cloud9	Cloud9	t	t	\N	\N	\N
2905	234-19	0001-01-01	9999-12-31	gb	Teleware plc	Teleware	t	t	\N	\N	\N
2907	234-21	0001-01-01	9999-12-31	gb	LogicStar Ltd	\N	t	t	\N	\N	\N
2908	234-22	0001-01-01	9999-12-31	gb	Routo Telecommunications Ltd	RoutoMessaging	t	t	\N	\N	\N
2909	234-23	0001-01-01	9999-12-31	gb	Vectone Network Ltd	\N	t	t	\N	\N	\N
2910	234-24	0001-01-01	9999-12-31	gb	Stour Marine Ltd	Greenfone	t	t	\N	\N	\N
2911	234-25	0001-01-01	9999-12-31	gb	Software Cellular Network Ltd	Truphone (UK)	t	t	\N	\N	\N
2912	234-26	0001-01-01	9999-12-31	gb	Lycamobile UK Limited	\N	t	t	\N	\N	\N
2913	234-27	0001-01-01	9999-12-31	gb	Teleena UK Limited	\N	t	t	\N	\N	\N
2914	234-28	0001-01-01	9999-12-31	gb	Marathon Telecom Limited	\N	t	t	\N	\N	\N
2915	234-29	0001-01-01	9999-12-31	gb	(aq) Limited T/A aql	\N	t	t	\N	\N	\N
2916	234-30	0001-01-01	9999-12-31	gb	T-Mobile UK	T-mobile	t	t	\N	\N	\N
2917	234-31	0001-01-01	9999-12-31	gb	T-Mobile UK	Virgin	t	t	\N	\N	\N
2918	234-32	0001-01-01	9999-12-31	gb	T-Mobile UK	Virgin	t	t	\N	\N	\N
2921	234-50	0001-01-01	9999-12-31	gb	Jersey Telecom	JT-Wave	t	t	\N	\N	\N
2922	234-55	0001-01-01	9999-12-31	gb	Cable and Wireless Guensey Ltd	Cable and Wireless	t	t	\N	\N	\N
2923	234-58	0001-01-01	9999-12-31	gb	Manx Telecom	Manx Telecom	t	t	\N	\N	\N
2924	234-76	0001-01-01	9999-12-31	gb	British Telecom	BT	t	t	\N	\N	\N
2857	286-02	0001-01-01	9999-12-31	tr	Telsim GSM	Vodafone tr	t	t	\N	\N	\N
2869	641-14	0001-01-01	9999-12-31	ug	House of Integrated Technology and Systems Uganda Ltd (HiTs Telecom)	Orange ug	t	t	\N	\N	\N
2925	234-78	0001-01-01	9999-12-31	gb	Airwave mmO2 Ltd	Airwave	t	t	\N	\N	\N
2926	235-00	0001-01-01	9999-12-31	gb	Mundlo Mobile Limited	\N	t	t	\N	\N	\N
2927	235-77	0001-01-01	9999-12-31	gb	British Telecom	\N	t	t	\N	\N	\N
2928	235-91	0001-01-01	9999-12-31	gb	Vodafone Ltd	\N	t	t	\N	\N	\N
2929	235-92	0001-01-01	9999-12-31	gb	Cable & Wireless UK	\N	t	t	\N	\N	\N
2930	235-94	0001-01-01	9999-12-31	gb	Hutchison 3G UK Ltd.	\N	t	t	\N	\N	\N
2931	235-95	0001-01-01	9999-12-31	gb	Network Rail Infrastructure Ltd	\N	t	t	\N	\N	\N
2932	310-010	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
2933	310-012	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
2934	310-013	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
2935	310-016	0001-01-01	9999-12-31	us	Cricket Communications	\N	t	t	\N	\N	\N
2936	310-017	0001-01-01	9999-12-31	us	North Sight Communications Inc	\N	t	t	\N	\N	\N
2937	310-020	0001-01-01	9999-12-31	us	Union Telephone Company	\N	t	t	\N	\N	\N
2938	310-030	0001-01-01	9999-12-31	us	Centennial Communications	\N	t	t	\N	\N	\N
2939	310-035	0001-01-01	9999-12-31	us	ETEX Communications dba ETEX Wireless	\N	t	t	\N	\N	\N
2940	310-040	0001-01-01	9999-12-31	us	MTA Communications dba MTA Wireless	\N	t	t	\N	\N	\N
2941	310-050	0001-01-01	9999-12-31	us	Alaska Communications	\N	t	t	\N	\N	\N
2942	310-060	0001-01-01	9999-12-31	us	Consolidated Telcom	\N	t	t	\N	\N	\N
2943	310-070	0001-01-01	9999-12-31	us	AT&T	\N	t	t	\N	\N	\N
2944	310-080	0001-01-01	9999-12-31	us	Corr Wireless Communications LLC	\N	t	t	\N	\N	\N
2945	310-090	0001-01-01	9999-12-31	us	Cricket Communications	\N	t	t	\N	\N	\N
2946	310-100	0001-01-01	9999-12-31	us	New Mexico RSA 4 East Ltd. Partnership	Plateau Wireless	t	t	\N	\N	\N
2947	310-110	0001-01-01	9999-12-31	us	Pacific Telecom Inc	Verizon	t	t	\N	\N	\N
2948	310-120	0001-01-01	9999-12-31	us	Sprintcom Inc	Sprint	t	t	\N	\N	\N
2949	310-130	0001-01-01	9999-12-31	us	Carolina West Wireless	Carolina West Wireless	t	t	\N	\N	\N
2950	310-140	0001-01-01	9999-12-31	us	GTA Wireless LLC	GTA Wireless LLC	t	t	\N	\N	\N
2951	310-150	0001-01-01	9999-12-31	us	Cingular Wireless	AT&T	t	t	\N	\N	\N
2953	310-170	0001-01-01	9999-12-31	us	Cingular Wireless	Cingular Wireless	t	t	\N	\N	\N
2954	310-180	0001-01-01	9999-12-31	us	West Central Wireless	West Central	t	t	\N	\N	\N
2955	310-190	0001-01-01	9999-12-31	us	Alaska Wireless Communications LLC	Dutch Harbor	t	t	\N	\N	\N
2964	310-280	0001-01-01	9999-12-31	us	Contennial Puerto Rio License Corp.	Verizon	t	t	\N	\N	\N
2965	310-290	0001-01-01	9999-12-31	us	Nep Cellcorp Inc.	Nep Cellcorp Inc.	t	t	\N	\N	\N
2966	310-300	0001-01-01	9999-12-31	us	Blanca Telephone Company	iSmart Mobile	t	t	\N	\N	\N
2968	310-320	0001-01-01	9999-12-31	us	Smith Bagley Inc, dba Cellular One	Cellular One	t	t	\N	\N	\N
2969	310-330	0001-01-01	9999-12-31	us	AWCC	AN Subsidiary LLC	t	t	\N	\N	\N
2970	310-340	0001-01-01	9999-12-31	us	High Plains Midwest LLC, dba Westlink Communications	Westlink	t	t	\N	\N	\N
2971	310-350	0001-01-01	9999-12-31	us	Mohave Cellular L.P.	Mohave Cellular L.P.	t	t	\N	\N	\N
2972	310-360	0001-01-01	9999-12-31	us	Cellular Network Partnership dba Pioneer Cellular	Cellular Network Partnership dba Pioneer Cellular	t	t	\N	\N	\N
2973	310-370	0001-01-01	9999-12-31	us	Docomo Pacific Inc	Guamcell Cellular and Paging	t	t	\N	\N	\N
2974	310-380	0001-01-01	9999-12-31	us	New Cingular Wireless PCS, LLC	New Cingular Wireless PCS, LLC	t	t	\N	\N	\N
2975	310-390	0001-01-01	9999-12-31	us	TX-11 Acquistion LLC	Verizon	t	t	\N	\N	\N
2976	310-400	0001-01-01	9999-12-31	us	Wave Runner LLC	i CAN_GSM	t	t	\N	\N	\N
2977	310-410	0001-01-01	9999-12-31	us	Cingular Wireless	AT&T	t	t	\N	\N	\N
2978	310-420	0001-01-01	9999-12-31	us	Cincinnati Bell Wireless LLC	Cincinnati Bell	t	t	\N	\N	\N
2979	310-430	0001-01-01	9999-12-31	us	GCI Communications Corp	Alaska Digitel LLC	t	t	\N	\N	\N
2980	310-440	0001-01-01	9999-12-31	us	Numerex Corp	Numerex Corp.	t	t	\N	\N	\N
2981	310-450	0001-01-01	9999-12-31	us	North East Cellular Inc.	Viaero	t	t	\N	\N	\N
2982	310-460	0001-01-01	9999-12-31	us	Newcore Wireless	Simmetry	t	t	\N	\N	\N
2983	310-470	0001-01-01	9999-12-31	us	nTELOS Communications Inc	Omnipoint	t	t	\N	\N	\N
2984	310-480	0001-01-01	9999-12-31	us	Choice Phone LLC	Verizon	t	t	\N	\N	\N
2986	310-500	0001-01-01	9999-12-31	us	Public Service Cellular, Inc.	Alltel	t	t	\N	\N	\N
2987	310-510	0001-01-01	9999-12-31	us	Nsighttel Wireless Inc	Airtel	t	t	\N	\N	\N
2988	310-520	0001-01-01	9999-12-31	us	Transactions Network Services	VeriSign	t	t	\N	\N	\N
2989	310-530	0001-01-01	9999-12-31	us	Iowa Wireless Services LLC	West Virginia Wireless	t	t	\N	\N	\N
2990	310-540	0001-01-01	9999-12-31	us	Oklahoma Western Telephone Company	Oklahoma Western	t	t	\N	\N	\N
2991	310-550	0001-01-01	9999-12-31	us	Wireless Solutions International	AT&T	t	t	\N	\N	\N
2992	310-560	0001-01-01	9999-12-31	us	AT&T	AT&T	t	t	\N	\N	\N
2993	310-570	0001-01-01	9999-12-31	us	MTPCS LLC	Cellular One	t	t	\N	\N	\N
2994	310-580	0001-01-01	9999-12-31	us	Inland Cellular Telephone Company	Inland Cellular Telephone Company	t	t	\N	\N	\N
2995	310-590	0001-01-01	9999-12-31	us	Verizon Wireless	Alltel	t	t	\N	\N	\N
2996	310-600	0001-01-01	9999-12-31	us	New Cell Inc. dba Cellcom	New Cell Inc. dba Cellcom	t	t	\N	\N	\N
2997	310-610	0001-01-01	9999-12-31	us	Elkhart Telephone Co. Inc. dba Epic Touch Co.	Epic Touch	t	t	\N	\N	\N
2998	310-620	0001-01-01	9999-12-31	us	Nsighttel Wireless Inc	Coleman County Telecom	t	t	\N	\N	\N
2999	310-640	0001-01-01	9999-12-31	us	Airadigm Communications	Airadigm	t	t	\N	\N	\N
3000	310-650	0001-01-01	9999-12-31	us	Jasper Wireless Inc.	Jasper	t	t	\N	\N	\N
3001	310-660	0001-01-01	9999-12-31	us	T-Mobile USA	MetroPCS	t	t	\N	\N	\N
3002	310-670	0001-01-01	9999-12-31	us	AT&T Mobility Vanguard Services	Northstar	t	t	\N	\N	\N
3003	310-680	0001-01-01	9999-12-31	us	AT&T	AT&T	t	t	\N	\N	\N
3004	310-690	0001-01-01	9999-12-31	us	Keystone Wireless LLC	Conestoga	t	t	\N	\N	\N
3005	310-700	0001-01-01	9999-12-31	us	Cross Valiant Cellular Partnership	Cross Valiant Cellular Partnership	t	t	\N	\N	\N
3006	310-710	0001-01-01	9999-12-31	us	Arctic Slope Telephone Association Cooperative	Arctic Slopo Telephone Association Cooperative	t	t	\N	\N	\N
3007	310-720	0001-01-01	9999-12-31	us	Wireless Solutions International Inc.	Wireless Solutions International Inc.	t	t	\N	\N	\N
3008	310-730	0001-01-01	9999-12-31	us	US Cellular	SeaMobile	t	t	\N	\N	\N
3009	310-740	0001-01-01	9999-12-31	us	Convey Communications Inc	Convey	t	t	\N	\N	\N
3010	310-750	0001-01-01	9999-12-31	us	East Kentucky Network LLC dba Appalachian Wireless	East Kentucky Network LLC dba Appalachian Wireless	t	t	\N	\N	\N
3011	310-760	0001-01-01	9999-12-31	us	Lynch 3G Communications Corporation	Panhandle	t	t	\N	\N	\N
3012	310-770	0001-01-01	9999-12-31	us	Iowa Wireless Services LLC dba I Wireless	i wireless	t	t	\N	\N	\N
3013	310-780	0001-01-01	9999-12-31	us	D.D. Inc	Connect Net Inc	t	t	\N	\N	\N
3014	310-790	0001-01-01	9999-12-31	us	PinPoint Communications Inc.	PinPoint	t	t	\N	\N	\N
3016	310-810	0001-01-01	9999-12-31	us	LCFR LLC	Brazos Cellular Communications Ltd.	t	t	\N	\N	\N
3017	310-820	0001-01-01	9999-12-31	us	South Canaan Cellular Communications Co. LP	South Canaan Cellular Communications Co. LP	t	t	\N	\N	\N
3018	310-830	0001-01-01	9999-12-31	us	Clearwire Corporation	Caprock	t	t	\N	\N	\N
3019	310-840	0001-01-01	9999-12-31	us	Telecom North America Mobile Inc	telna Mobile	t	t	\N	\N	\N
3020	310-850	0001-01-01	9999-12-31	us	Aeris Communications, Inc.	Aeris	t	t	\N	\N	\N
3021	310-860	0001-01-01	9999-12-31	us	TX RSA 15B2, LP dba Five Star Wireless	TX RSA 15B2, LP dba Five Star Wireless	t	t	\N	\N	\N
3022	310-870	0001-01-01	9999-12-31	us	Kaplan Telephone Company Inc.	PACE	t	t	\N	\N	\N
3023	310-880	0001-01-01	9999-12-31	us	Advantage Cellular Systems, Inc.	Advantage	t	t	\N	\N	\N
3024	310-890	0001-01-01	9999-12-31	us	Verizon Wireless	Verizon	t	t	\N	\N	\N
3025	310-900	0001-01-01	9999-12-31	us	Cable & Communications Corporation dba Mid-Rivers Wireless	Mid-Rivers Wireless	t	t	\N	\N	\N
3026	310-910	0001-01-01	9999-12-31	us	Verizon Wireless	Verizon	t	t	\N	\N	\N
3027	310-920	0001-01-01	9999-12-31	us	James Valley Wireless LLC	Get Mobile	t	t	\N	\N	\N
3028	310-930	0001-01-01	9999-12-31	us	Copper Valley Wireless	Copper Valley Wireless	t	t	\N	\N	\N
3029	310-940	0001-01-01	9999-12-31	us	Iris Wireless LLC	Poka Lambro Telecommunications Ltd.	t	t	\N	\N	\N
3030	310-950	0001-01-01	9999-12-31	us	Texas RSA 1 dba XIT Wireless	XIT Wireless	t	t	\N	\N	\N
3031	310-960	0001-01-01	9999-12-31	us	UBET Wireless	Plateau Wireless	t	t	\N	\N	\N
3032	310-970	0001-01-01	9999-12-31	us	Globalstar USA	Globalstar	t	t	\N	\N	\N
3033	310-980	0001-01-01	9999-12-31	us	Texas RSA 7B3 dba Peoples Wireless Services	New Cingular Wireless PCS LLC	t	t	\N	\N	\N
3034	310-990	0001-01-01	9999-12-31	us	Worldcall Interconnect	E.N.M.R. Telephone Cooperative	t	t	\N	\N	\N
3035	311-000	0001-01-01	9999-12-31	us	Mid-Tex Cellular Ltd.	\N	t	t	\N	\N	\N
3036	311-010	0001-01-01	9999-12-31	us	Chariton Valley Communications Corp., Inc.	\N	t	t	\N	\N	\N
3037	311-020	0001-01-01	9999-12-31	us	Missouri RSA No. 5 Partnership	\N	t	t	\N	\N	\N
3038	311-030	0001-01-01	9999-12-31	us	Indigo Wireless, Inc.	\N	t	t	\N	\N	\N
3039	311-040	0001-01-01	9999-12-31	us	Commnet Wireless LLC	\N	t	t	\N	\N	\N
3040	311-050	0001-01-01	9999-12-31	us	Thumb Cellular Limited Partnership	\N	t	t	\N	\N	\N
3041	311-060	0001-01-01	9999-12-31	us	Space Data Corporation	\N	t	t	\N	\N	\N
3042	311-070	0001-01-01	9999-12-31	us	Wisconsin RSA #7 Limited Partnership	\N	t	t	\N	\N	\N
3043	311-080	0001-01-01	9999-12-31	us	Pine Telephone Company dba Pine Cellular	\N	t	t	\N	\N	\N
3044	311-090	0001-01-01	9999-12-31	us	LongLines Wireless	\N	t	t	\N	\N	\N
3045	311-100	0001-01-01	9999-12-31	us	Nex-Tech Wireless LLC	High Plains Wireless	t	t	\N	\N	\N
3046	311-110	0001-01-01	9999-12-31	us	Verizon Wireless	High Plains Wireless	t	t	\N	\N	\N
3047	311-120	0001-01-01	9999-12-31	us	Choice Phone LLC	\N	t	t	\N	\N	\N
3048	311-130	0001-01-01	9999-12-31	us	Light Squared LP	Alltel	t	t	\N	\N	\N
3049	311-140	0001-01-01	9999-12-31	us	Cross Telephone Company	Sprocket	t	t	\N	\N	\N
3050	311-150	0001-01-01	9999-12-31	us	Wilkes Cellular Inc.	Wilkes Cellular	t	t	\N	\N	\N
3051	311-160	0001-01-01	9999-12-31	us	Light Squared LP	\N	t	t	\N	\N	\N
3052	311-170	0001-01-01	9999-12-31	us	PetroCom LLC	PetroCom	t	t	\N	\N	\N
3053	311-180	0001-01-01	9999-12-31	us	Cingular Wireless, Licensee Pacific Telesis Mobile Services, LLC	\N	t	t	\N	\N	\N
3054	311-190	0001-01-01	9999-12-31	us	Cellular Properties Inc.	\N	t	t	\N	\N	\N
3055	311-200	0001-01-01	9999-12-31	us	ARINC	\N	t	t	\N	\N	\N
3056	311-210	0001-01-01	9999-12-31	us	Emery Telecom-Wireless Inc	Farmers Cellular	t	t	\N	\N	\N
3057	311-220	0001-01-01	9999-12-31	us	United States Cellular	\N	t	t	\N	\N	\N
3058	311-230	0001-01-01	9999-12-31	us	Cellular South Inc	\N	t	t	\N	\N	\N
3059	311-240	0001-01-01	9999-12-31	us	Cordova Wireless Communications Inc	\N	t	t	\N	\N	\N
3060	311-250	0001-01-01	9999-12-31	us	Wave Runner LLC	\N	t	t	\N	\N	\N
3061	311-260	0001-01-01	9999-12-31	us	Clearwire Corporation	Cellular One	t	t	\N	\N	\N
3062	311-270	0001-01-01	9999-12-31	us	Verizon Wireless	Lamar Country Cellular	t	t	\N	\N	\N
3063	311-271	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3064	311-272	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3065	311-273	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3066	311-274	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3067	311-275	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3068	311-276	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3069	311-277	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3070	311-278	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3071	311-279	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3072	311-280	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3073	311-281	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3074	311-282	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3075	311-283	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3076	311-284	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3077	311-285	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3078	311-286	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3079	311-287	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3080	311-288	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3081	311-289	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3082	311-290	0001-01-01	9999-12-31	us	Pinpoint Wireless Inc.	NEP Wireless	t	t	\N	\N	\N
3083	311-300	0001-01-01	9999-12-31	us	Nexus Communications Inc	\N	t	t	\N	\N	\N
3084	311-310	0001-01-01	9999-12-31	us	Leaco Rural Telephone Company Inc	\N	t	t	\N	\N	\N
3085	311-320	0001-01-01	9999-12-31	us	Commnet Wireless LLC	\N	t	t	\N	\N	\N
3086	311-330	0001-01-01	9999-12-31	us	Bug Tussel Wireless LLC	Bug Tussel Wireless	t	t	\N	\N	\N
3087	311-340	0001-01-01	9999-12-31	us	Illinois Valley Cellular	\N	t	t	\N	\N	\N
3088	311-350	0001-01-01	9999-12-31	us	Sagebrush Cellular Inc dba Nemont	\N	t	t	\N	\N	\N
3089	311-360	0001-01-01	9999-12-31	us	Stelera Wireless LLC	\N	t	t	\N	\N	\N
3090	311-370	0001-01-01	9999-12-31	us	GCI Communications Corp.	GCI Wireless in Alaska	t	t	\N	\N	\N
3091	311-380	0001-01-01	9999-12-31	us	New Dimension Wireless Ltd	\N	t	t	\N	\N	\N
3092	311-390	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3093	311-410	0001-01-01	9999-12-31	us	Iowa RSA No.2 Ltd Partnership	\N	t	t	\N	\N	\N
3094	311-420	0001-01-01	9999-12-31	us	Northwest Missouri Cellular Limited Partnership	\N	t	t	\N	\N	\N
3095	311-430	0001-01-01	9999-12-31	us	RSA 1 Limited Partnership dba Cellular 29 Plus	\N	t	t	\N	\N	\N
3096	311-440	0001-01-01	9999-12-31	us	Bluegrass Cellular LLC	\N	t	t	\N	\N	\N
3097	311-450	0001-01-01	9999-12-31	us	Panhandle Telecommunication Systems Inc.	\N	t	t	\N	\N	\N
3098	311-460	0001-01-01	9999-12-31	us	Fisher Wireless Services Inc	\N	t	t	\N	\N	\N
3099	311-470	0001-01-01	9999-12-31	us	Vitelcom Cellular Inc dba Innovative Wireless	\N	t	t	\N	\N	\N
3100	311-480	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3101	311-481	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3102	311-482	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3103	311-483	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3104	311-484	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3105	311-485	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3106	311-486	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3107	311-487	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3108	311-488	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3109	311-489	0001-01-01	9999-12-31	us	Verizon Wireless	\N	t	t	\N	\N	\N
3110	311-490	0001-01-01	9999-12-31	us	Sprintcom Inc	\N	t	t	\N	\N	\N
3111	311-500	0001-01-01	9999-12-31	us	Mosaic Telecom Inc	\N	t	t	\N	\N	\N
3112	311-510	0001-01-01	9999-12-31	us	Light Squared LP	\N	t	t	\N	\N	\N
3113	311-520	0001-01-01	9999-12-31	us	Light Squared LP	\N	t	t	\N	\N	\N
3114	311-530	0001-01-01	9999-12-31	us	Newcore Wireless LLC	NewCore Wireless	t	t	\N	\N	\N
3115	311-540	0001-01-01	9999-12-31	us	Poximiti Mobility Inc	\N	t	t	\N	\N	\N
3116	311-550	0001-01-01	9999-12-31	us	Commnet Midwest LLC	\N	t	t	\N	\N	\N
3117	311-560	0001-01-01	9999-12-31	us	OTZ Communications Inc	\N	t	t	\N	\N	\N
3118	311-570	0001-01-01	9999-12-31	us	Bend Cable Communications LLC	\N	t	t	\N	\N	\N
3119	311-580	0001-01-01	9999-12-31	us	United States Cellular	\N	t	t	\N	\N	\N
3120	311-590	0001-01-01	9999-12-31	us	California RSA No3 Ltd Partnership dba Golden State Cellular	\N	t	t	\N	\N	\N
3121	311-600	0001-01-01	9999-12-31	us	Cox TMI Wireless LLC	\N	t	t	\N	\N	\N
3122	311-610	0001-01-01	9999-12-31	us	North Dakota Network Co.	\N	t	t	\N	\N	\N
3123	311-620	0001-01-01	9999-12-31	us	Terrestar Networks Inc	\N	t	t	\N	\N	\N
3124	311-630	0001-01-01	9999-12-31	us	Corr Wireless Communications	\N	t	t	\N	\N	\N
3125	311-640	0001-01-01	9999-12-31	us	Standing Rock Telecommunications	\N	t	t	\N	\N	\N
3126	311-650	0001-01-01	9999-12-31	us	United Wireless Inc	\N	t	t	\N	\N	\N
3127	311-660	0001-01-01	9999-12-31	us	Metro PCS Wireless Inc	\N	t	t	\N	\N	\N
3128	311-670	0001-01-01	9999-12-31	us	Pine Belt Cellular Inc dba Pine Belt Wireless	\N	t	t	\N	\N	\N
3129	311-680	0001-01-01	9999-12-31	us	GreenFly LLC	\N	t	t	\N	\N	\N
3130	311-690	0001-01-01	9999-12-31	us	TeleBeeper of New Mexico Inc	\N	t	t	\N	\N	\N
3131	311-700	0001-01-01	9999-12-31	us	TotalSolutions Telecom LLC	\N	t	t	\N	\N	\N
3132	311-710	0001-01-01	9999-12-31	us	Northeast Wireless Networks LLC	\N	t	t	\N	\N	\N
3133	311-720	0001-01-01	9999-12-31	us	Maine PCS LLC	\N	t	t	\N	\N	\N
3134	311-730	0001-01-01	9999-12-31	us	Proximiti Mobility Inc	\N	t	t	\N	\N	\N
3135	311-740	0001-01-01	9999-12-31	us	Telalaska Cellular	\N	t	t	\N	\N	\N
3136	311-750	0001-01-01	9999-12-31	us	NetAmerica Alliance LLC	\N	t	t	\N	\N	\N
3137	311-760	0001-01-01	9999-12-31	us	Edigen Inc	\N	t	t	\N	\N	\N
3138	311-770	0001-01-01	9999-12-31	us	Radio Mobile Access Inc	\N	t	t	\N	\N	\N
3139	311-800	0001-01-01	9999-12-31	us	Bluegrass Cellular LLC	\N	t	t	\N	\N	\N
3140	311-810	0001-01-01	9999-12-31	us	Blegrass Cellular LLC	\N	t	t	\N	\N	\N
3141	311-820	0001-01-01	9999-12-31	us	Kineto Wireless Inc	\N	t	t	\N	\N	\N
3142	311-830	0001-01-01	9999-12-31	us	Thumb Cellular LLC	\N	t	t	\N	\N	\N
3143	311-840	0001-01-01	9999-12-31	us	Nsight Spectrum LLC	\N	t	t	\N	\N	\N
3144	311-850	0001-01-01	9999-12-31	us	Nsight Spectrum LLC	\N	t	t	\N	\N	\N
3145	311-860	0001-01-01	9999-12-31	us	Uintah Basin Electronic Telecommunications	\N	t	t	\N	\N	\N
3146	311-870	0001-01-01	9999-12-31	us	Sprintcom Inc	\N	t	t	\N	\N	\N
3147	311-880	0001-01-01	9999-12-31	us	Sprintcom Inc	\N	t	t	\N	\N	\N
3148	311-890	0001-01-01	9999-12-31	us	Globecom Network Services Corporation	\N	t	t	\N	\N	\N
3149	311-900	0001-01-01	9999-12-31	us	Gigsky inc	\N	t	t	\N	\N	\N
3150	311-910	0001-01-01	9999-12-31	us	SI Wireless LLC	\N	t	t	\N	\N	\N
3151	311-920	0001-01-01	9999-12-31	us	Missouri RSA No 5 Partnership dba Charlton Valley Wireless Services	\N	t	t	\N	\N	\N
3152	311-940	0001-01-01	9999-12-31	us	Clearwire Corporation	\N	t	t	\N	\N	\N
3153	311-950	0001-01-01	9999-12-31	us	Sunman Telecommunications corp.	\N	t	t	\N	\N	\N
3154	311-960	0001-01-01	9999-12-31	us	Lycamobile USA Inc	\N	t	t	\N	\N	\N
3155	311-970	0001-01-01	9999-12-31	us	Big River Broadband LLC	\N	t	t	\N	\N	\N
3156	311-980	0001-01-01	9999-12-31	us	LigTel Communications	\N	t	t	\N	\N	\N
3157	311-990	0001-01-01	9999-12-31	us	VTel Wireless	\N	t	t	\N	\N	\N
3158	312-010	0001-01-01	9999-12-31	us	Charlton Valley Communication Corporation Inc	\N	t	t	\N	\N	\N
3159	312-020	0001-01-01	9999-12-31	us	Infrastructure Networks LLC	\N	t	t	\N	\N	\N
3160	312-030	0001-01-01	9999-12-31	us	Cross Wireless	\N	t	t	\N	\N	\N
3161	312-040	0001-01-01	9999-12-31	us	Custer Telephone Cooperative Inc	\N	t	t	\N	\N	\N
3162	312-050	0001-01-01	9999-12-31	us	Fuego Wireless LLC	\N	t	t	\N	\N	\N
3163	312-060	0001-01-01	9999-12-31	us	CoverageCo	\N	t	t	\N	\N	\N
3164	312-070	0001-01-01	9999-12-31	us	Adams Networks Inc	\N	t	t	\N	\N	\N
3165	312-080	0001-01-01	9999-12-31	us	South Georgia Regional Information Technology Authority	\N	t	t	\N	\N	\N
3166	312-090	0001-01-01	9999-12-31	us	Allied Wireless Communixcations Corporation	\N	t	t	\N	\N	\N
3167	312-100	0001-01-01	9999-12-31	us	ClearSky Technologies Inc	\N	t	t	\N	\N	\N
3168	312-110	0001-01-01	9999-12-31	us	Texas Energy Network LLC	\N	t	t	\N	\N	\N
3169	312-120	0001-01-01	9999-12-31	us	East Kentucky Network LLC dba Appalachian Wireless	\N	t	t	\N	\N	\N
3170	312-130	0001-01-01	9999-12-31	us	East Kentucky Network LLC dba Appalachian Wireless	\N	t	t	\N	\N	\N
3171	312-140	0001-01-01	9999-12-31	us	Cleveland Unlimited Inc	\N	t	t	\N	\N	\N
3172	312-150	0001-01-01	9999-12-31	us	Northwest Cell	\N	t	t	\N	\N	\N
3173	312-160	0001-01-01	9999-12-31	us	RSA1 Limited Partnership dba Chat Mobility	\N	t	t	\N	\N	\N
3174	312-170	0001-01-01	9999-12-31	us	Iowa RSA No 2 Limited Partnership	\N	t	t	\N	\N	\N
3175	312-180	0001-01-01	9999-12-31	us	Keystone Wireless LLC	\N	t	t	\N	\N	\N
3176	312-190	0001-01-01	9999-12-31	us	Sprint-Nextel Communications Inc	\N	t	t	\N	\N	\N
3177	312-200	0001-01-01	9999-12-31	us	Voyager Mobility LLC	\N	t	t	\N	\N	\N
3178	313-100	0001-01-01	9999-12-31	us	Assigned to Public Safety	\N	t	t	\N	\N	\N
3179	316-010	0001-01-01	9999-12-31	us	Sprint-Nextel Communications Inc	\N	t	t	\N	\N	\N
3180	316-011	0001-01-01	9999-12-31	us	Southern Communications Services Inc.	\N	t	t	\N	\N	\N
3181	748-00	0001-01-01	9999-12-31	uy	Ancel - TDMA	Ancel	t	t	\N	\N	\N
3182	748-01	0001-01-01	9999-12-31	uy	Ancel - GSM	Ancel	t	t	\N	\N	\N
3183	748-03	0001-01-01	9999-12-31	uy	Ancel	Ancel	t	t	\N	\N	\N
3184	748-07	0001-01-01	9999-12-31	uy	Movistar	Movistar	t	t	\N	\N	\N
3185	748-10	0001-01-01	9999-12-31	uy	CTI Móvil	Claro	t	t	\N	\N	\N
3186	434-01	0001-01-01	9999-12-31	uz	Buztel	Buztel	t	t	\N	\N	\N
3187	434-02	0001-01-01	9999-12-31	uz	Uzmacom	Uzmacom	t	t	\N	\N	\N
3188	434-04	0001-01-01	9999-12-31	uz	Daewoo Unitel	Beeline	t	t	\N	\N	\N
3189	434-05	0001-01-01	9999-12-31	uz	Coscom	Ucell	t	t	\N	\N	\N
3190	434-07	0001-01-01	9999-12-31	uz	Uzdunrobita	MTS	t	t	\N	\N	\N
3191	541-01	0001-01-01	9999-12-31	vu	SMILE	Smile	t	t	\N	\N	\N
3192	541-05	0001-01-01	9999-12-31	vu	Digicel Vanuatu	Digicel	t	t	\N	\N	\N
3193	734-01	0001-01-01	9999-12-31	ve	Infonet	Digitel	t	t	\N	\N	\N
3194	734-02	0001-01-01	9999-12-31	ve	Corporación Digitel	Digitel	t	t	\N	\N	\N
3195	734-03	0001-01-01	9999-12-31	ve	Digicel	Digitel	t	t	\N	\N	\N
3196	734-04	0001-01-01	9999-12-31	ve	Telcel, C.A.	Movistar	t	t	\N	\N	\N
3197	734-06	0001-01-01	9999-12-31	ve	Telecomunicaciones Movilnet, C.A.	Movilnet	t	t	\N	\N	\N
3198	452-01	0001-01-01	9999-12-31	vn	Mobifone	MobilFone	t	t	\N	\N	\N
3199	452-02	0001-01-01	9999-12-31	vn	Vinaphone	Vinaphone	t	t	\N	\N	\N
3200	452-03	0001-01-01	9999-12-31	vn	S Telecom (CDMA)	S-Fone	t	t	\N	\N	\N
3201	452-04	0001-01-01	9999-12-31	vn	Viettel	Viettel	t	t	\N	\N	\N
3202	452-06	0001-01-01	9999-12-31	vn	EVN Telecom	E-Mobile	t	t	\N	\N	\N
3203	452-07	0001-01-01	9999-12-31	vn	Beeline VN/GTEL Mobile JSC	Beeline VN	t	t	\N	\N	\N
3204	452-08	0001-01-01	9999-12-31	vn	EVN Telecom	\N	t	t	\N	\N	\N
3205	421-01	0001-01-01	9999-12-31	ye	Yemen Mobile Phone Company	SabaFon	t	t	\N	\N	\N
3206	421-02	0001-01-01	9999-12-31	ye	Spacetel Yemen	Spacetel Yemen	t	t	\N	\N	\N
3207	645-01	0001-01-01	9999-12-31	zm	Celtel Zambia Ltd.	Zain	t	t	\N	\N	\N
3208	645-02	0001-01-01	9999-12-31	zm	Telecel Zambia Ltd.	MTN	t	t	\N	\N	\N
3209	645-03	0001-01-01	9999-12-31	zm	Zamtel	Zamtel	t	t	\N	\N	\N
3210	648-01	0001-01-01	9999-12-31	zw	Net One	Net One	t	t	\N	\N	\N
3211	648-03	0001-01-01	9999-12-31	zw	Telecel	Telecel	t	t	\N	\N	\N
1856	219-01	0001-01-01	9999-12-31	hr	T-Mobile Hrvatska d.o.o./T-Mobile Croatia LLC	T-Mobile hr	t	t	\N	\N	\N
3239	232-05	2013-01-03	9999-12-31	at	Yesss! (A1 TA)	Yesss! (A1 TA)	t	t	232-01	Yesss nach Merger mit Orange-Sim	\N
3212	648-04	0001-01-01	9999-12-31	zw	Econet	Econet	t	t	\N	\N	\N
1867	230-01	0001-01-01	9999-12-31	cz	T-Mobile Czech Republic a.s.	T-Mobile cz	t	t	\N	\N	\N
3213	232-01	0001-01-01	9999-12-31	at	A1 Telekom Austria AG - Mobilnetz	A1 TA Mobil	t	t	\N	Primäre Kennung von A1 TA	\N
3231	232-09	0001-01-01	9999-12-31	at	dummy	\N	t	f	\N	ehem. Kennung von Tele2-Mobil AT	3234
1985	262-01	0001-01-01	9999-12-31	de	T-Mobile Deutschland GmbH	T-Mobile de	t	t	\N	\N	\N
2062	216-30	0001-01-01	9999-12-31	hu	Magyar Telecom Plc	T-Mobile hu	t	t	\N	\N	\N
3217	232-03	0001-01-01	9999-12-31	at	T-Mobile Austria GmbH	T-Mobile AT	t	t	\N	Primäre Kennung von T-Mobile	\N
2511	297-02	0001-01-01	9999-12-31	me	Crnogorski Telekom	T-Mobile me	t	t	\N	\N	\N
2604	260-02	0001-01-01	9999-12-31	pl	T-Mobile / PTC S.A.	T-Mobile pl	t	t	\N	\N	\N
2706	231-02	0001-01-01	9999-12-31	sk	Eurotel, GSM & NMT	T-Mobile sk	t	t	\N	\N	\N
2707	231-04	0001-01-01	9999-12-31	sk	Eurotel, UMTS	T-Mobile sk	t	t	\N	\N	\N
2839	294-01	0001-01-01	9999-12-31	mk	T-Mobile	T-Mobile mk	t	t	\N	\N	\N
2952	310-160	0001-01-01	9999-12-31	us	T-Mobile USA	T-Mobile us	t	t	\N	\N	\N
2956	310-200	0001-01-01	9999-12-31	us	T-Mobile USA	T-Mobile us	t	t	\N	\N	\N
2957	310-210	0001-01-01	9999-12-31	us	T-Mobile USA	T-Mobile us	t	t	\N	\N	\N
2958	310-220	0001-01-01	9999-12-31	us	T-Mobile USA	T-Mobile us	t	t	\N	\N	\N
2959	310-230	0001-01-01	9999-12-31	us	T-Mobile USA	T-Mobile us	t	t	\N	\N	\N
2960	310-240	0001-01-01	9999-12-31	us	T-Mobile USA	T-Mobile us	t	t	\N	\N	\N
2961	310-250	0001-01-01	9999-12-31	us	T-Mobile USA	T-Mobile us	t	t	\N	\N	\N
2962	310-260	0001-01-01	9999-12-31	us	T-Mobile USA	T-Mobile us	t	t	\N	\N	\N
2963	310-270	0001-01-01	9999-12-31	us	T-Mobile USA	T-Mobile us	t	t	\N	\N	\N
3245	232-05	0001-01-01	2013-07-01	at	Orange AT	Orange AT	t	t	\N	Orange vor Sidestream-Merger	\N
3227	232-07	0001-01-01	9999-12-31	at	T-Mobile Austria GmbH (tele.ring)	tele.ring (T-Mobile AT)	t	f	\N	ehem. ID tele.ring, nunmehr T-Mobile/Reseller	\N
2967	310-310	0001-01-01	9999-12-31	us	T-Mobile USA	T-Mobile us	t	t	\N	\N	\N
3223	232-04	0001-01-01	9999-12-31	at	dummy	\N	t	t	\N	Test-ID T-Mobile AT	3217
2985	310-490	0001-01-01	9999-12-31	us	T-Mobile USA	T-Mobile us	t	t	\N	\N	\N
3015	310-800	0001-01-01	9999-12-31	us	T-Mobile USA	T-Mobile us	t	t	\N	\N	\N
1620	276-02	0001-01-01	9999-12-31	al	Vodafone Albania	Vodafone al	t	t	\N	\N	\N
3237	232-91	0001-01-01	9999-12-31	at	OeBB Infrastruktur Bau AG	OeBB GSM-R AT	t	t	\N	kein GSM/UMTS-Netz, nur GSM-R	\N
3235	232-12	2013-01-03	9999-12-31	at	Yesss! (A1 TA)	Yesss! (A1 TA)	t	f	\N	Yesss nach Merger/Weiterverkauf	\N
3234	232-11	0001-01-01	9999-12-31	at	Bob (A1 TA)	Bob (A1 TA)	t	f	\N	\N	\N
3226	232-06	0001-01-01	9999-12-31	at	dummy	\N	t	t	\N	Test-ID Hutchison	3224
3215	232-02	0001-01-01	9999-12-31	at	dummy	\N	t	t	\N	Test-ID A1-TA	3213
3236	232-15	0001-01-01	9999-12-31	at	Barablue Mobile Austria Ltd	Barablue	t	f	\N	MVNO	\N
3224	232-05	2013-07-02	9999-12-31	at	Hutchison Drei Austria GmbH	Drei AT	t	t	\N	Primäre Kennung von Drei	\N
3240	232-12	0001-01-01	2013-01-02	at	Yesss! (Orange)	Yesss! (Orange)	t	f	\N	Yesss vor Merger, eigene ID	\N
1643	505-03	0001-01-01	9999-12-31	au	Vodafone Network Pty. Ltd.	Vodafone au	t	t	\N	\N	\N
1904	602-02	0001-01-01	9999-12-31	eg	Vodafone	Vodafone eg	t	t	\N	\N	\N
1921	288-02	0001-01-01	9999-12-31	fo	Kall GSM	Vodafone fo	t	t	\N	\N	\N
1923	542-01	0001-01-01	9999-12-31	fj	Vodafone (Fiji) Ltd	Vodafone fj	t	t	\N	\N	\N
1993	262-09	0001-01-01	9999-12-31	de	Vodafone D2 GmbH	Vodafone de	t	t	\N	\N	\N
2003	620-02	0001-01-01	9999-12-31	gh	Ghana Telecom Mobile	Vodafone gh	t	t	\N	\N	\N
2014	202-05	0001-01-01	9999-12-31	gr	Vodafone - Panafon	Vodafone gr	t	t	\N	\N	\N
2065	274-02	0001-01-01	9999-12-31	is	Og fjarskipti hf (Vodafone Iceland)	Vodafone is	t	t	\N	\N	\N
2066	274-03	0001-01-01	9999-12-31	is	Og fjarskipti hf (Vodafone Iceland)	Vodafone is	t	t	\N	\N	\N
2070	404-01	0001-01-01	9999-12-31	in	Aircell Digilink India Ltd., Haryana	Vodafone in	t	t	\N	\N	\N
2074	404-05	0001-01-01	9999-12-31	in	Fascel Ltd., Gujarat	Vodafone in	t	t	\N	\N	\N
2079	404-11	0001-01-01	9999-12-31	in	Hutchison Essar Mobile Services Ltd, Delhi	Vodafone in	t	t	\N	\N	\N
2081	404-13	0001-01-01	9999-12-31	in	Hutchison Essar South Ltd., Andhra Pradesh	Vodafone in	t	t	\N	\N	\N
2083	404-15	0001-01-01	9999-12-31	in	Aircell Digilink India Ltd., UP (East)	Vodafone in	t	t	\N	\N	\N
2088	404-20	0001-01-01	9999-12-31	in	Hutchison Essar Ltd, Mumbai	Vodafone in	t	t	\N	\N	\N
2094	404-27	0001-01-01	9999-12-31	in	Hutchison Essar Cellular Ltd., Maharashtra	Vodafone in	t	t	\N	\N	\N
2096	404-30	0001-01-01	9999-12-31	in	Hutchison Telecom East Ltd, Kolkata	Vodafone in	t	t	\N	\N	\N
2107	404-43	0001-01-01	9999-12-31	in	Hutchison Essar Cellular Ltd., Tamil Nadu	Vodafone in	t	t	\N	\N	\N
2109	404-46	0001-01-01	9999-12-31	in	Hutchison Essar Cellular Ltd., Kerala	Vodafone in	t	t	\N	\N	\N
2122	404-60	0001-01-01	9999-12-31	in	Aircell Digilink India Ltd., Rajasthan	Vodafone in	t	t	\N	\N	\N
2146	404-84	0001-01-01	9999-12-31	in	Hutchison Essar South Ltd., Chennai	Vodafone in	t	t	\N	\N	\N
2148	404-86	0001-01-01	9999-12-31	in	Hutchison Essar South Ltd., Karnataka	Vodafone in	t	t	\N	\N	\N
2150	404-88	0001-01-01	9999-12-31	in	Hutchison Essar South Ltd, Punjab	Vodafone in	t	t	\N	\N	\N
2208	405-66	0001-01-01	9999-12-31	in	Hutchison Essar South Ltd, UP (West)	Vodafone in	t	t	\N	\N	\N
2209	405-67	0001-01-01	9999-12-31	in	Hutchison Essar South Ltd, Orissa	Vodafone in	t	t	\N	\N	\N
2216	405-750	0001-01-01	9999-12-31	in	Vodafone Essar Spacetel Ltd, J&K	Vodafone in	t	t	\N	\N	\N
2217	405-751	0001-01-01	9999-12-31	in	Vodafone Essar Spacetel Ltd, Assam	Vodafone in	t	t	\N	\N	\N
2218	405-752	0001-01-01	9999-12-31	in	Vodafone Essar Spacetel Ltd, Bihar	Vodafone in	t	t	\N	\N	\N
2219	405-753	0001-01-01	9999-12-31	in	Vodafone Essar Spacetel Ltd, Orissa	Vodafone in	t	t	\N	\N	\N
2220	405-754	0001-01-01	9999-12-31	in	Vodafone Essar Spacetel Ltd, Himachal Pradesh	Vodafone in	t	t	\N	\N	\N
2221	405-755	0001-01-01	9999-12-31	in	Vodafone Essar Spacetel Ltd, North East	Vodafone in	t	t	\N	\N	\N
2273	272-01	0001-01-01	9999-12-31	ie	Vodafone Ireland Plc	Vodafone ie	t	t	\N	\N	\N
2296	222-10	0001-01-01	9999-12-31	it	Omnitel Pronto Italia (OPI)	Vodafone it	t	t	\N	\N	\N
2491	278-01	0001-01-01	9999-12-31	mt	Vodafone Malta	Vodafone mt	t	t	\N	\N	\N
2542	530-01	0001-01-01	9999-12-31	nz	Vodafone New Zealand GSM Network	Vodafone nz	t	t	\N	\N	\N
2638	268-01	0001-01-01	9999-12-31	pt	Vodafone Telecel - Comunicaçôes Pessoais, S.A.	Vodafone pt	t	t	\N	\N	\N
2901	234-15	0001-01-01	9999-12-31	gb	Vodafone Ltd	Vodafone gb	t	t	\N	\N	\N
1713	652-02	0001-01-01	9999-12-31	bw	Orange Botswana (Pty) Ltd.	Orange bw	t	t	\N	\N	\N
1765	624-02	0001-01-01	9999-12-31	cm	Orange Cameroun	Orange cm	t	t	\N	\N	\N
2459	295-02	0001-01-01	9999-12-31	li	Orange (Liechtenstein) AG	Orange li	t	t	\N	\N	\N
2467	270-99	0001-01-01	9999-12-31	lu	Voxmobile S.A.	Orange lu	t	t	\N	\N	\N
2476	646-02	0001-01-01	9999-12-31	mg	Orange Madagascar, GSM	Orange mg	t	t	\N	\N	\N
2708	231-05	0001-01-01	9999-12-31	sk	Orange, UMTS	Orange sk	t	t	\N	\N	\N
2743	214-03	0001-01-01	9999-12-31	es	France Telecom España, SA	Orange es	t	t	\N	\N	\N
2749	214-09	0001-01-01	9999-12-31	es	France Telecom España, SA	Orange es	t	t	\N	\N	\N
2818	228-03	0001-01-01	9999-12-31	ch	Orange Communications SA	Orange ch	t	t	\N	\N	\N
2919	234-33	0001-01-01	9999-12-31	gb	Orange	Orange gb	t	t	\N	\N	\N
2920	234-34	0001-01-01	9999-12-31	gb	Orange	Orange gb	t	t	\N	\N	\N
3232	232-10	0001-01-01	9999-12-31	at	dummy	\N	t	t	\N	Hutchison nach Sidestream-Merger	3224
3251	232-16	0001-01-01	9999-12-31	at	dummy	\N	t	t	\N	Test-ID Hutchison	3224
3254	232-17	0001-01-01	9999-12-31	at	Massresponse Service GmbH	Massresponse AT	t	f	\N	MVNO	\N
2471	455-03	0001-01-01	9999-12-31	mo	Hutchison - Telefone(Macau) Limitada	3 mo	t	t	\N	\N	\N
2473	455-05	0001-01-01	9999-12-31	mo	Hutchison - Telefone(Macau) Limitada	3 mo	t	t	\N	\N	\N
2906	234-20	0001-01-01	9999-12-31	gb	Hutchison 3G UK Ltd.	3 UK	t	t	\N	\N	\N
2781	240-02	0001-01-01	9999-12-31	se	H3G Access AB	3 SE	t	t	\N	\N	\N
1885	238-06	0001-01-01	9999-12-31	dk	Hi3G	3 DK	t	t	\N	\N	\N
3256	250-25	0001-01-01	9999-12-31	ru	Motiv RU	Motiv RU	t	t	\N	\N	\N
3257	272-05	0001-01-01	9999-12-31	ie	Hutchison Three IE	3 IE	t	t	\N	\N	\N
3258	274-11	0001-01-01	9999-12-31	is	Nova IS	Nova IS	t	t	\N	\N	\N
3259	310-004	0001-01-01	9999-12-31	us	Verizon US	Verizon US	t	t	\N	\N	\N
3260	310-026	0001-01-01	9999-12-31	us	T-Mobile US	T-Mobile US	t	t	\N	\N	\N
3261	310-29	0001-01-01	9999-12-31	us	T-Mobile US	T-Mobile US	t	t	\N	incorrect, should be 310-290	\N
3262	310-99	0001-01-01	9999-12-31	us	AT&T US	AT&T US	t	t	\N	incorrect, should be 310-990	\N
3263	310-110	0001-01-01	9999-12-31	us	PTI Pacifica US	PTI Pacifica US	t	t	\N	\N	\N
3265	401-77	0001-01-01	9999-12-31	kz	Tele2 KZ	Tle2 KZ	t	t	\N	\N	\N
3266	405-09	0001-01-01	9999-12-31	in	Reliance Jammu & Kashmir IN	Reliance IN	t	t	\N	\N	\N
3267	405-18	0001-01-01	9999-12-31	in	Reliance Punjab IN	Reliance IN	t	t	\N	\N	\N
3268	405-51	0001-01-01	9999-12-31	in	AirTel West Bengal IN	AirTel IN	t	t	\N	\N	\N
3269	405-848	0001-01-01	9999-12-31	in	IDEA Kolkata IN	IDEA IN	t	t	\N	\N	\N
3270	405-932	0001-01-01	9999-12-31	in	Videocon Punjab IN	Videocon IN	t	t	\N	\N	\N
3271	413-01	0001-01-01	9999-12-31	lk	Mobitel LK	Mobitel LK	t	t	\N	\N	\N
3272	420-04	0001-01-01	9999-12-31	sa	Zain SA	Zain SA	t	t	\N	\N	\N
3273	424-03	0001-01-01	9999-12-31	ae	Emirates Integrated - du	du AE	t	t	\N	\N	\N
3274	450-05	0001-01-01	9999-12-31	kr	SKTelecom KR	SKTelecom KR	t	t	\N	\N	\N
3275	450-08	0001-01-01	9999-12-31	kr	KT olleh KR	KT olleh KR	t	t	\N	\N	\N
3276	454-13	0001-01-01	9999-12-31	hk	China Mobile HK	China Mobile HK	t	t	\N	\N	\N
3277	466-89	0001-01-01	9999-12-31	tw	Vibo Telecom TW	Vibo Telecom TW	t	t	\N	\N	\N
3278	466-92	0001-01-01	9999-12-31	tw	Changhwa Telecom TW	Changhua Telecom TW	t	t	\N	\N	\N
3279	466-97	0001-01-01	9999-12-31	tw	Taiwan Mobile TW	Taiwan Mobile TW	t	t	\N	\N	\N
3280	510-09	0001-01-01	9999-12-31	id	Smartfren ID	Smartfren ID	t	t	\N	\N	\N
3281	510-89	0001-01-01	9999-12-31	id	3 ID	3 ID	t	t	\N	\N	\N
3282	520-03	0001-01-01	9999-12-31	th	AIS TH	AIS TH	t	t	\N	\N	\N
3283	520-04	0001-01-01	9999-12-31	th	truemove H TH	truemove H TH	t	t	\N	\N	\N
3284	520-05	0001-01-01	9999-12-31	th	dtac 3G TH	dtac 3G TH	t	t	\N	\N	\N
3285	520-18	0001-01-01	9999-12-31	th	dtac TH	dtac TH	t	t	\N	\N	\N
3286	520-99	0001-01-01	9999-12-31	th	truemove TH	truemove TH	t	t	\N	\N	\N
3287	630-03	0001-01-01	9999-12-31	dz	Ooredoo DZ	Ooredoo DZ	t	t	\N	\N	\N
3288	634-07	0001-01-01	9999-12-31	sd	Sudani One SD	Sudani One SD	t	t	\N	\N	\N
3289	744-05	0001-01-01	9999-12-31	py	Personal PY	Personal PY	t	t	\N	\N	\N
3293	232-13	0001-01-01	9999-12-31	at	UPC Mobil AT	UPC Mobil AT	t	\N	\N	\N	\N
\.


--
-- Name: mccmnc2name_uid_seq; Type: SEQUENCE SET; Schema: public; Owner: rmbt
--

SELECT pg_catalog.setval('mccmnc2name_uid_seq', 3293, true);


--
-- Data for Name: mccmnc2provider; Type: TABLE DATA; Schema: public; Owner: rmbt
--

COPY mccmnc2provider (uid, mcc_mnc_sim, provider_id, mcc_mnc_network, valid_from, valid_to) FROM stdin;
1	232-01	1	\N	\N	\N
2	232-02	1	\N	\N	\N
3	232-03	2	\N	\N	\N
6	232-07	2	\N	\N	\N
7	232-09	1	\N	\N	\N
9	232-11	1	\N	\N	\N
32	232-21	50	\N	\N	\N
14	232-05	1	232-01	\N	\N
10	232-12	1	\N	2013-01-03	\N
15	232-12	3	\N	\N	2013-01-02
5	232-06	3	\N	\N	2013-07-01
19	232-14	36	\N	2013-07-02	\N
20	232-16	36	\N	2013-07-02	\N
11	232-14	4	\N	\N	2013-07-01
13	232-16	4	\N	\N	2013-07-01
21	232-05	36	\N	2013-07-02	\N
22	232-10	36	\N	2013-07-02	\N
4	232-05	3	\N	\N	2013-07-01
8	232-10	4	\N	\N	2013-07-01
16	232-04	2	\N	\N	\N
24	232-08	1	\N	\N	\N
23	232-13	36	\N	\N	\N
25	232-17	36	\N	\N	\N
26	232-18	36	\N	\N	\N
27	232-19	2	\N	\N	\N
28	232-20	1	\N	\N	\N
29	232-91	48	\N	\N	\N
30	232-92	49	\N	\N	\N
12	232-15	1	232-01	\N	\N
31	232-15	2	232-03	\N	\N
\.


--
-- Name: mccmnc2provider_uid_seq; Type: SEQUENCE SET; Schema: public; Owner: rmbt
--

SELECT pg_catalog.setval('mccmnc2provider_uid_seq', 32, true);


--
-- Name: provider_uid_seq; Type: SEQUENCE SET; Schema: public; Owner: rmbt
--

SELECT pg_catalog.setval('provider_uid_seq', 116, true);


--
-- PostgreSQL database dump complete
--

