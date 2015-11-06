/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
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
package at.alladin.rmbt.client.v2.task;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.xbill.DNS.A6Record;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.Section;

import at.alladin.rmbt.client.QualityOfServiceTest;
import at.alladin.rmbt.client.helper.Dig;
import at.alladin.rmbt.client.helper.Dig.DnsRequest;
import at.alladin.rmbt.client.v2.task.result.QoSTestResult;
import at.alladin.rmbt.client.v2.task.result.QoSTestResultEnum;

public class DnsTask extends AbstractQoSTask {

	public final static long DEFAULT_TIMEOUT = 5000000000L;
	
	private final String record;
	
	private final String host;
	
	private final String resolver;
	
	private final long timeout;
	
	public final static String PARAM_DNS_HOST = "host";
	
	public final static String PARAM_DNS_RESOLVER = "resolver";
	
	public final static String PARAM_DNS_RECORD = "record";
	
	public final static String PARAM_DNS_TIMEOUT = "timeout";

	public final static String RESULT_STATUS = "dns_result_status";
	
	public final static String RESULT_ENTRY = "dns_result_entries";
	
	public final static String RESULT_TTL = "dns_result_ttl";
	
	public final static String RESULT_ADDRESS = "dns_result_address";
	
	public final static String RESULT_PRIORITY = "dns_result_priority";
	
	public final static String RESULT_DURATION = "dns_result_duration";
	
	public final static String RESULT_QUERY = "dns_result_info";
	
	public final static String RESULT_RESOLVER = "dns_objective_resolver";
	
	public final static String RESULT_DNS_HOST = "dns_objective_host";
	
	public final static String RESULT_DNS_RECORD = "dns_objective_dns_record";
	
	public final static String RESULT_DNS_TIMEOUT = "dns_objective_timeout";
	
	public final static String RESULT_DNS_ENTRIES_FOUND = "dns_result_entries_found";
	
	/**
	 * 
	 * @param taskDesc
	 */
	public DnsTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId) {
		super(nnTest, taskDesc, threadId, threadId);
		this.record = (String)taskDesc.getParams().get(PARAM_DNS_RECORD);
		this.host = (String)taskDesc.getParams().get(PARAM_DNS_HOST);
		this.resolver = (String)taskDesc.getParams().get(PARAM_DNS_RESOLVER);
		
		String value = (String) taskDesc.getParams().get(PARAM_DNS_TIMEOUT);
		this.timeout = value != null ? Long.valueOf(value) : DEFAULT_TIMEOUT;
	}

	/**
	 * 
	 */
	public QoSTestResult call() throws Exception {
  		final QoSTestResult testResult = initQoSTestResult(QoSTestResultEnum.DNS);
  		
		try {
			onStart(testResult);
			
	  		long start = System.nanoTime();
	  		List<JSONObject> dnsResult = lookupDns(host, record, resolver, (int)(timeout / 1000000), testResult);
	  		testResult.getResultMap().put(RESULT_ENTRY, dnsResult);
	  		long duration = System.nanoTime() - start;
	  		//testResult.getResultMap().put(RESULT_DURATION, (duration / 1000000));
	  		testResult.getResultMap().put(RESULT_DURATION, duration);
	  		testResult.getResultMap().put(RESULT_RESOLVER, resolver != null ? resolver : "Standard");
	  		testResult.getResultMap().put(RESULT_DNS_RECORD, record);
	  		testResult.getResultMap().put(RESULT_DNS_HOST, host);
	  		testResult.getResultMap().put(RESULT_DNS_TIMEOUT, timeout);
	  		if (dnsResult == null || dnsResult.size() <= 0) {
		  		testResult.getResultMap().put(RESULT_DNS_ENTRIES_FOUND, "0");	
	  		}
	  		else {
		  		testResult.getResultMap().put(RESULT_DNS_ENTRIES_FOUND, dnsResult.size());
	  		}
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			onEnd(testResult);
		}
		
        return testResult;
	}

	/**
	 * 
	 * @param domainName
	 * @param record
	 * @param resolver
	 * @return
	 */
	public static List<JSONObject> lookupDns(String domainName, String record, String resolver, int timeout, QoSTestResult testResult) {
		//List<String> result = new ArrayList<String>();
		List<JSONObject> result = new ArrayList<JSONObject>();
		
		//Lookup dnsLookup = null;
		try {
			System.out.println("dns lookup: record = " + record + " for host: " + domainName + ", using resolver:" + resolver);
			
			ResolverConfig.refresh(); // refresh dns server
			
			DnsRequest req = Dig.doRequest(domainName, record, resolver, timeout);

			testResult.getResultMap().put(RESULT_QUERY, "OK");
			//dnsLookup = new Lookup(domainName, Type.value(record.toUpperCase()));
			//dnsLookup.setResolver(new SimpleResolver(resolver));			
			//Record[] records = dnsLookup.run();
			testResult.getResultMap().put(RESULT_STATUS, Rcode.string(req.getResponse().getRcode()));
			if (req.getRequest().getRcode() == Rcode.NOERROR) {
				Record [] records = req.getResponse().getSectionArray(Section.ANSWER);
				
				if (records != null && records.length > 0) {
					for (int i = 0; i < records.length; i++) {
						JSONObject dnsEntry = new JSONObject();
						if (records[i] instanceof MXRecord) {
							dnsEntry.put(RESULT_PRIORITY, String.valueOf(((MXRecord) records[i]).getPriority()));
							dnsEntry.put(RESULT_ADDRESS, ((MXRecord) records[i]).getTarget().toString());
						}
						else if (records[i] instanceof CNAMERecord) {
							dnsEntry.put(RESULT_ADDRESS, ((CNAMERecord) records[i]).getAlias());
						}
						else if (records[i] instanceof ARecord) {
							dnsEntry.put(RESULT_ADDRESS, ((ARecord) records[i]).getAddress().getHostAddress());	
						}
						else if (records[i] instanceof AAAARecord) {
							dnsEntry.put(RESULT_ADDRESS, ((AAAARecord) records[i]).getAddress().getHostAddress());
						}
						else if (records[i] instanceof A6Record) {
							dnsEntry.put(RESULT_ADDRESS, ((A6Record) records[i]).getSuffix().toString());
						}
						else {
							dnsEntry.put(RESULT_ADDRESS, records[i].getName());
						}
						
						dnsEntry.put(RESULT_TTL, String.valueOf(records[i].getTTL()));
						
						//result.add(records[i].toString());
						result.add(dnsEntry);
						System.out.println("record " + i + " toString: " + records[i].toString());
					}
				}
				else {
					return null;
				}
			}
			else {
				return null;
			}
		} catch (SocketTimeoutException e) {
			testResult.getResultMap().put(RESULT_QUERY, "TIMEOUT");
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			testResult.getResultMap().put(RESULT_QUERY, "ERROR");
			e.printStackTrace();
			return null;
		}
		
		return result;
    }

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.AbstractRmbtTask#initTask()
	 */
	@Override
	public void initTask() {
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#getTestType()
	 */
	public QoSTestResultEnum getTestType() {
		return QoSTestResultEnum.DNS;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#needsQoSControlConnection()
	 */
	public boolean needsQoSControlConnection() {
		return false;
	}
}
