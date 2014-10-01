/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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
package at.alladin.rmbt.client.helper;

import java.io.IOException;
import java.net.InetAddress;

import org.xbill.DNS.DClass;
import org.xbill.DNS.ExtendedFlags;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.Type;

public class Dig {
	static Name name = null;
	static int type = Type.A, dclass = DClass.IN;

	static void	usage() {
		System.out.println("Usage: dig [@server] name [<type>] [<class>] " +
				   "[options]");
		System.exit(0);
	}

	static void	doQuery(Message response, long ms) throws IOException {
		System.out.println("; java dig 0.0");
		System.out.println(response);
		System.out.println(";; Query time: " + ms + " ms");
	}

	static void	doAXFR(Message response) throws IOException {
		System.out.println("; java dig 0.0 <> " + name + " axfr");
		if (response.isSigned()) {
			System.out.print(";; TSIG ");
			if (response.isVerified())
				System.out.println("ok");
			else
				System.out.println("failed");
		}

		if (response.getRcode() != Rcode.NOERROR) {
			System.out.println(response);
			return;
		}

		Record [] records = response.getSectionArray(Section.ANSWER);
		for (int i = 0; i < records.length; i++)
			System.out.println(records[i]);

		System.out.print(";; done (");
		System.out.print(response.getHeader().getCount(Section.ANSWER));
		System.out.print(" records, ");
		System.out.print(response.getHeader().getCount(Section.ADDITIONAL));
		System.out.println(" additional)");
	}

	public static void run(String argv[]) throws IOException {
		String server = null;
		int arg;
		Message query, response;
		Record rec;
		SimpleResolver res = null;
		boolean printQuery = false;
		long startTime, endTime;

		if (argv.length < 1) {
			usage();
		}

		try {
			arg = 0;
			if (argv[arg].startsWith("@"))
				server = argv[arg++].substring(1);

			if (server != null)
				res = new SimpleResolver(server);
			else
				res = new SimpleResolver();

			String nameString = argv[arg++];
			if (nameString.equals("-x")) {
				name = ReverseMap.fromAddress(argv[arg++]);
				type = Type.PTR;
				dclass = DClass.IN;
			}
			else {
				name = Name.fromString(nameString, Name.root);
				type = Type.value(argv[arg]);
				if (type < 0)
					type = Type.A;
				else
					arg++;

				dclass = DClass.value(argv[arg]);
				if (dclass < 0)
					dclass = DClass.IN;
				else
					arg++;
			}

			while (argv[arg].startsWith("-") && argv[arg].length() > 1) {
				switch (argv[arg].charAt(1)) {
				case 'p':
					String portStr;
					int port;
					if (argv[arg].length() > 2)
						portStr = argv[arg].substring(2);
					else
						portStr = argv[++arg];
					port = Integer.parseInt(portStr);
					if (port < 0 || port > 65536) {
						System.out.println("Invalid port");
						return;
					}
					res.setPort(port);
					break;

				case 'b':
					String addrStr;
					if (argv[arg].length() > 2)
						addrStr = argv[arg].substring(2);
					else
						addrStr = argv[++arg];
					InetAddress addr;
					try {
						addr = InetAddress.getByName(addrStr);
					}
					catch (Exception e) {
						System.out.println("Invalid address");
						return;
					}
					res.setLocalAddress(addr);
					break;

				case 'k':
					String key;
					if (argv[arg].length() > 2)
						key = argv[arg].substring(2);
					else
						key = argv[++arg];
					res.setTSIGKey(TSIG.fromString(key));
					break;

				case 't':
					res.setTCP(true);
					break;

				case 'i':
					res.setIgnoreTruncation(true);
					break;

				case 'e':
					String ednsStr;
					int edns;
					if (argv[arg].length() > 2)
						ednsStr = argv[arg].substring(2);
					else
						ednsStr = argv[++arg];
					edns = Integer.parseInt(ednsStr);
					if (edns < 0 || edns > 1) {
						System.out.println("Unsupported " +
								   "EDNS level: " +
								   edns);
						return;
					}
					res.setEDNS(edns);
					break;

				case 'd':
					res.setEDNS(0, 0, ExtendedFlags.DO, null);
					break;

				case 'q':
				    	printQuery = true;
					break;

				default:
					System.out.print("Invalid option: ");
					System.out.println(argv[arg]);
				}
				arg++;
			}

		}
		catch (ArrayIndexOutOfBoundsException e) {
			if (name == null)
				usage();
		}
		if (res == null)
			res = new SimpleResolver();

		rec = Record.newRecord(name, type, dclass);
		query = Message.newQuery(rec);
		if (printQuery)
			System.out.println(query);
		startTime = System.currentTimeMillis();
		response = res.send(query);
		endTime = System.currentTimeMillis();

		if (type == Type.AXFR)
			doAXFR(response);
		else
			doQuery(response, endTime - startTime);
	}
	
	public static DnsRequest doRequest(String domain, String record, int timeout) throws Exception {
		return doRequest(domain, record, null, timeout);
	}
	
	public static DnsRequest doRequest(String domain, String record, String resolver, int timeout) throws Exception {
		Resolver res;
		if (resolver != null) {
			res = new SimpleResolver(resolver);
		}
		else {
			res = new SimpleResolver();
		}
		
		Record rec = Record.newRecord(Name.fromString(domain, Name.root), Type.value(record), DClass.IN);
		Message query = Message.newQuery(rec);

		long startTime = System.currentTimeMillis();
		res.setTimeout(0, timeout);
		Message response = res.send(query);
		long endTime = System.currentTimeMillis();

		return new DnsRequest(response, query, res, endTime - startTime);
	}

	public static class DnsRequest {
		private final Message response;
		private final Message request;
		private final Resolver resolver;
		private final long duration;
		
		public DnsRequest(Message resp, Message req, Resolver res, long duration) {
			this.response = resp;
			this.request = req;
			this.resolver = res;
			this.duration = duration;
		}

		public Message getResponse() {
			return response;
		}

		public Message getRequest() {
			return request;
		}

		public Resolver getResolver() {
			return resolver;
		}

		public long getDuration() {
			return duration;
		}
	}
}
