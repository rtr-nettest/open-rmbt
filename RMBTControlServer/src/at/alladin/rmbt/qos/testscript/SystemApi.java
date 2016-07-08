/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
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
package at.alladin.rmbt.qos.testscript;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.alladin.rmbt.qos.TracerouteResult;
import at.alladin.rmbt.util.net.rtp.RealtimeTransportProtocol;

public class SystemApi {

	final static DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("##0.00");
	
	static {
		DEFAULT_DECIMAL_FORMAT.setMaximumFractionDigits(2);
	}
	
	public int getCount(Object array) {
		if (array != null && array.getClass().isArray()) {
			return Array.getLength(array);
		}

		return 0;
	}

	public boolean isEmpty(Object array) {
		return getCount(array) == 0;
	}
	
	public boolean isNull(Object o) {
		return o == null;
	}

	public Object coalesce(Object o, Object alternative) {
		return o==null ? alternative : o;
	}
	
	public String getPayloadType(int value) {
		return RealtimeTransportProtocol.PayloadType.getByCodecValue(value).name();
	}
	
	public String parseTraceroute(String path) throws JSONException {
		final JSONArray traceRoute = new JSONArray(path);
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < traceRoute.length(); i++) {
			final JSONObject e = traceRoute.getJSONObject(i);
			sb.append(e.getString("host"));
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
	
	public String parseTraceroute(ArrayList<TracerouteResult.PathElement> path) {
		StringBuilder sb = new StringBuilder();
		for (TracerouteResult.PathElement e : path) {
			sb.append(e.getHost());
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
		
		return sb.toString();
	}
	
	public static String getRandomUrl(String prefix, String suffix, int length) {
        char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };
        
        StringBuilder randomUrl = new StringBuilder();
        randomUrl.append(prefix);
        Random rnd = new Random();
        
        for (int i = 0; i < length; i++) {
        	randomUrl.append(digits[rnd.nextInt(16)]);
        }
        
        randomUrl.append(suffix);
        return randomUrl.toString();
	}
	
	public Object debug(Object toLog) {
		System.out.println("QoSLOG: " + toLog);
		return toLog;
	}
}
