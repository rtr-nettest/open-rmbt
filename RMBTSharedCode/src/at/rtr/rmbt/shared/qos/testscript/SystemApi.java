/*******************************************************************************
 * Copyright 2013-2019 alladin-IT GmbH
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

package at.rtr.rmbt.shared.qos.testscript;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.rtr.rmbt.shared.qos.TracerouteResult;

/**
 * 
 * @author alladin-IT GmbH (?@alladin.at)
 *
 */
public class SystemApi {

	/**
	 * 
	 */
	private static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("##0.00");
	
	/**
	 * 
	 */
	private static final DecimalFormat DEFAULT_PERCENT_FORMAT = new DecimalFormat("0.#");
	
	/**
	 * 
	 */
	static {
		DEFAULT_DECIMAL_FORMAT.setMaximumFractionDigits(2);
	}
	
	/**
	 * 
	 * @param array
	 * @return
	 */
	public int getCount(Object array) {
		if (array != null && array.getClass().isArray()) {
			return Array.getLength(array);
		}

		return 0;
	}

	/**
	 * 
	 * @param array
	 * @return
	 */
	public boolean isEmpty(Object array) {
		return getCount(array) == 0;
	}
	
	/**
	 * 
	 * @param o
	 * @return
	 */
	public boolean isNull(Object o) {
		return o == null;
	}

	/**
	 * 
	 * @param o
	 * @param alternative
	 * @return
	 */
	public Object coalesce(Object o, Object alternative) {
		return o == null ? alternative : o;
	}
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	public String getPayloadType(int value) {
		return null;// RealtimeTransportProtocol.PayloadType.getByCodecValue(value).name();
	}
	
	public String prettifyJson(Object json) throws JSONException {
		JSONObject jsonObj = null;
		if (json instanceof JSONObject) {
			jsonObj = (JSONObject) json;
		} else {
			jsonObj = new JSONObject((String)json);
		}
		return jsonObj.toString(4);
	}
	
	/**
	 *  returns the XYZ p Resolution
	 * @param medianBitrate in kbps
	 * @return
	 */
	public String getAchievableStreamingResolution(int medianBitrate) {
		medianBitrate /= 1000;
		String ret = null;
		if (medianBitrate < 1) {
			ret = "240";
		} else if (medianBitrate < 2.5) {
			ret = "360";
		} else if (medianBitrate < 5) {
			ret = "480";
		} else if (medianBitrate < 8) {
			ret = "720";
		} else if (medianBitrate < 16) {
			ret = "1080";
		} else if (medianBitrate < 35) {
			ret = "1440";
		} else {
			ret = "2160";
		}
		
		return ret + " p";
	}

	/**
	 * 
	 * @param path
	 * @return
	 * @throws JSONException
	 */
	public String parseTraceroute(String path) throws JSONException { // TODO: is this one used?
		final JSONArray traceRoute = new JSONArray(path);
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < traceRoute.length(); i++) {
			final JSONObject e = traceRoute.getJSONObject(i);
			sb.append(anonymizeIp(e.getString("host")));
			sb.append("  time=");
			try {
				sb.append(DEFAULT_DECIMAL_FORMAT.format((float)e.getLong("time") / 1000000f));
				sb.append("ms\n");
			}
			catch (Exception ex) {
				sb.append(e.getLong("time"));
				sb.append("ns\n");
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	public String parseTraceroute(ArrayList<TracerouteResult.PathElement> path) { // TODO: is this one used?
		StringBuilder sb = new StringBuilder();
		for (Object ele : path) {
			if (ele instanceof Map) {
				Map<String, Object> e = (Map<String, Object>) ele;
				sb.append(anonymizeIp(String.valueOf(e.get("host"))));
				sb.append("  time=");
				try {
					sb.append(DEFAULT_DECIMAL_FORMAT.format(Float.valueOf(String.valueOf(e.get("time"))) / 1000000f));
					sb.append("ms\n");
				}
				catch (Exception ex) {
					sb.append(e.get("time"));
					sb.append("ns\n");
				}				
			}
			else if (ele instanceof TracerouteResult.PathElement) {
				TracerouteResult.PathElement e = (TracerouteResult.PathElement) ele;
				sb.append(anonymizeIp(e.getHost()));
				sb.append("  time=");
				try {
					sb.append(DEFAULT_DECIMAL_FORMAT.format((float)e.getTime() / 1000000f));
					sb.append("ms\n");
				}
				catch (Exception ex) {
					sb.append(e.getTime());
					sb.append("ns\n");
				}
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * 
	 * @param ip
	 * @return
	 */
	public String anonymizeIp(String ip) { // TODO: can this also be a rdns name?
		if (ip == null || ip.length() == 0) {
			return "*";
		}
		
		if ("*".equals(ip) || ip.contains("x")) { // If IP address contains 'x' is has already been anonymized on the agent.
			return ip;
		}
		
		try {
			final InetAddress addr = InetAddress.getByName(ip);
			
			if (addr instanceof Inet4Address) {
				return ip.substring(0, ip.lastIndexOf('.')) + ".x";
			} else if (addr instanceof Inet6Address) {
				if (ip.endsWith("::")) {
					return ip + "x";
				} else {
					final String[] splitv6 = ip.split(":");
					return splitv6[0] + ":" + splitv6[1] + ":" + splitv6[2] + ":" + splitv6[3] + "::x";
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		return "*";
	}
	
	/**
	 * 
	 * @param prefix
	 * @param suffix
	 * @param length
	 * @return
	 */
	public static String getRandomUrl(String prefix, String suffix, int length) {
		final Random rnd = new Random();
		char[] digits = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        
        final StringBuilder randomUrl = new StringBuilder();
        randomUrl.append(prefix);
        
        for (int i = 0; i < length; i++) {
        	randomUrl.append(digits[rnd.nextInt(16)]);
        }
        
        randomUrl.append(suffix);

        return randomUrl.toString();
	}
	
	/**
	 * 
	 * @param ratio
	 * @return
	 */
	public String formatRatio(Object ratio) {
		try {
			BigDecimal number = new BigDecimal(String.valueOf(ratio));
			return DEFAULT_PERCENT_FORMAT.format(number.multiply(new BigDecimal(100)).doubleValue());
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
		
		return String.valueOf(ratio);
	}
	
	/*public static void main(String[] args) {
		System.out.println(anonymizeIp("10.10.10.10"));
		System.out.println(anonymizeIp("10.10.10.0"));
		System.out.println(anonymizeIp("192.168.5.1"));
		
		System.out.println(anonymizeIp("2a01:4f8:10a:2315:88:99:181:238"));
		System.out.println(anonymizeIp("2a01:4f8:10a:2315::"));
		System.out.println(anonymizeIp("2a01:4f8:10a:2315:1111:2222:3333:4444"));
		
		System.out.println(anonymizeIp("qwdqdqw"));
		System.out.println(anonymizeIp("10.10.10.10.10"));
	}*/
}
