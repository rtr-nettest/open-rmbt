/*******************************************************************************
 * Copyright 2016 Specure GmbH
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
package at.alladin.rmbt.android.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import at.alladin.rmbt.util.tools.TracerouteService;

/**
 * 
 * @author lb
 *
 */
public class TracerouteAndroidImpl implements TracerouteService {
	
	public static final class PingException extends IOException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PingException(String msg) {
			super(msg);
		}
	}
	
	public final static class PingDetailImpl implements HopDetail {
		private final int transmitted;
		private final int received;
		private final int errors;
		private final int packetLoss;
		private long time;
		private final String fromIp;
		
		public final static Pattern PATTERN_PING_PACKET =  Pattern.compile("([\\d]*) packets transmitted, ([\\d]*) received, ([+-]?([\\d]*) errors, )?([\\d]*)% packet loss, time ([\\d]*)ms");
		//public final static Pattern PATTERN_FROM_IP =  Pattern.compile("[fF]rom ([\\.:-_\\d\\w\\s\\(\\)]*):(.*time=([\\d\\.]*))?");
		public final static Pattern PATTERN_FROM_IP =  Pattern.compile("[fF]rom ([\\.\\-_\\d\\w\\s\\(\\)]*)(:|icmp)+(.*time=([\\d\\.]*))?");
		
		public PingDetailImpl(String pingResult, final long durationNs) {
			System.out.println(pingResult);

			time = durationNs;;
			final Matcher pingPacket = PATTERN_PING_PACKET.matcher(pingResult);
			
			if (pingPacket.find()) {
				transmitted = Integer.parseInt(pingPacket.group(1));
				received = Integer.parseInt(pingPacket.group(2));
				String errors = pingPacket.group(4);
				if (errors != null) {
					this.errors = Integer.parseInt(errors);
				}
				else {
					this.errors = 0;
				}
				packetLoss = Integer.parseInt(pingPacket.group(5));
			}
			else {
				transmitted = 0;
				received = 0;
				packetLoss = 0;
				errors = 0;
			}
			
			final Matcher fromIpMatcher = PATTERN_FROM_IP.matcher(pingResult);
			if (fromIpMatcher.find()) {
				fromIp = fromIpMatcher.group(1);
				String time = fromIpMatcher.group(4);
				if (time != null) {
					this.time = TimeUnit.NANOSECONDS.convert((int)(Float.parseFloat(time) * 1000f), TimeUnit.MICROSECONDS);
				}
			}
			else {
				fromIp = "*";
			}
			
		}
		
		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}

		public int getTransmitted() {
			return transmitted;
		}

		public int getReceived() {
			return received;
		}

		public int getErrors() {
			return errors;
		}

		public int getPacketLoss() {
			return packetLoss;
		}

		public String getFromIp() {
			return fromIp;
		}

		@Override
		public String toString() {
			return "PingDetail [transmitted=" + transmitted + ", received="
					+ received + ", errors=" + errors + ", packetLoss="
					+ packetLoss + ", time=" + (time / 1000000) + "ms, fromIp=" + fromIp
					+ "]";
		}
		
		public JSONObject toJson() {
			JSONObject json = new JSONObject();
			try {
				json.put("host", fromIp);
				json.put("time", time);
				return json;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	private String host;
	private int maxHops;
	private AtomicBoolean isRunning = new AtomicBoolean(false);
	private boolean hasMaxHopsExceeded = true;
	private List<HopDetail> resultList;
	
	public TracerouteAndroidImpl() {
		
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getMaxHops() {
		return maxHops;
	}

	public void setMaxHops(int maxHops) {
		this.maxHops = maxHops;
	}

	public List<HopDetail> call() throws Exception {
		isRunning.set(true);
		if (resultList == null) {
			resultList = new ArrayList<TracerouteService.HopDetail>();
		}

        final Runtime runtime = Runtime.getRuntime();
    	
    	for (int i = 1; i <= maxHops; i++) {
    		if (Thread.interrupted() || !isRunning.get()) {
    			throw new InterruptedException();
    		}
        	final long ts = System.nanoTime();
            final Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 -t " + i + " -W2 " + host);
            final String proc = readFromProcess(mIpAddrProcess);
            final PingDetailImpl pingDetail = new PingDetailImpl(proc, System.nanoTime() - ts);
            resultList.add(pingDetail);
            if (pingDetail.getReceived() > 0) {
            	hasMaxHopsExceeded = false;
            	break;
            }
    	}
    	    	
    	return resultList;
	}
	

	/**
	 * stop the ping tool task
	 * @return true if task has been stopped or false if it was not running
	 */
	public boolean stop() {
		return isRunning.getAndSet(false);
	}
	
	public static String readFromProcess(Process proc) throws PingException {
		BufferedReader brErr = null;
		BufferedReader br = null;
		StringBuilder sbErr = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		
		try {
			brErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        	String currInputLine = null;
	        
        	while((currInputLine = brErr.readLine()) != null) {
        		sbErr.append(currInputLine);
        		sbErr.append("\n");
        	}
			
        	if (sbErr.length() > 0) {
        		throw new PingException(sbErr.toString());
        	}

			br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        	currInputLine = null;
	        
        	while((currInputLine = br.readLine()) != null) {
        		sb.append(currInputLine);
        		sb.append("\n");
        	}
		}
		catch (PingException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) { }
			try {
				if (brErr != null) {
					brErr.close();
				}
			} catch (IOException e) { }
		}
		
		return sb.toString();
	}

	@Override
	public boolean hasMaxHopsExceeded() {
		return hasMaxHopsExceeded;
	}

	@Override
	public void setResultListObject(List<HopDetail> resultList) {
		this.resultList = resultList;
	}
}
