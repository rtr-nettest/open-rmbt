/*******************************************************************************
 * Copyright 2019 alladin-IT GmbH
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

package at.rtr.rmbt.shared.qos.util;

import java.util.Map;
import java.util.Random;

import com.google.common.base.Strings;

public class SipTaskHelper {

	//default timeout = 5s
	public final static long DEFAULT_TIMEOUT = 5000000000L;

	//default count = 3s
	public final static long DEFAULT_COUNT = 3;

	//default call duration = 2s
	public final static long DEFAULT_CALL_DURATION = 2000000000L;
	
	public final static String PARAM_PORT = "port";
	
	public final static String PARAM_TIMEOUT = "timeout";

	public final static String PARAM_COUNT = "count";

	public final static String PARAM_CALL_DURATION = "call_duration";
	
	public final static String PARAM_TO = "to";
	
	public final static String PARAM_FROM = "from";
	
	public final static String PARAM_VIA = "via";
	
	public final static String PARAM_PREFIX_RESULT = "sip_result";

	public final static String PARAM_PREFIX_OBJECTIVE = "sip_objective";

	//results and objectives:

	public final static String PARAM_OBJECTIVE_PORT = PARAM_PREFIX_OBJECTIVE + "_" + PARAM_PORT;

	public final static String PARAM_OBJECTIVE_COUNT = PARAM_PREFIX_OBJECTIVE + "_" + PARAM_COUNT;

	public final static String PARAM_OBJECTIVE_TIMEOUT = PARAM_PREFIX_OBJECTIVE + "_" + PARAM_TIMEOUT;

	public final static String PARAM_OBJECTIVE_CALL_DURATION = PARAM_PREFIX_OBJECTIVE + "_" + PARAM_CALL_DURATION;

	public final static String PARAM_OBJECTIVE_TO = PARAM_PREFIX_OBJECTIVE + "_" + PARAM_TO;

	public final static String PARAM_OBJECTIVE_FROM = PARAM_PREFIX_OBJECTIVE + "_" + PARAM_FROM;

	public final static String PARAM_OBJECTIVE_VIA = PARAM_PREFIX_OBJECTIVE + "_" + PARAM_VIA;

	public final static String PARAM_RESULT_TO = PARAM_PREFIX_RESULT + "_" + PARAM_TO;

	public final static String PARAM_RESULT_FROM = PARAM_PREFIX_RESULT + "_" + PARAM_FROM;

	public final static String PARAM_RESULT_VIA = PARAM_PREFIX_RESULT + "_" + PARAM_VIA;

	public final static String PARAM_RESULT = PARAM_PREFIX_RESULT;

	public final static String PARAM_RESULT_DURATION = PARAM_PREFIX_RESULT + "_duration";

	public final static String PARAM_RESULT_CCSR = PARAM_PREFIX_RESULT + "_ccsr";
	
	public final static String PARAM_RESULT_CSSR = PARAM_PREFIX_RESULT + "_cssr";
	
	public final static String PARAM_RESULT_DCR = PARAM_PREFIX_RESULT + "_dcr";
	
	public static void preProcess(final Map<String, Object> params) {
		if (params == null) {
			return;
		}

		generateRandomSipAddrIfNullOrEmpty(params, PARAM_TO);
		generateRandomSipAddrIfNullOrEmpty(params, PARAM_FROM);
		generateRandomViaAddrIfNullOrEmpty(params, PARAM_VIA);
	}
	
	public static void generateRandomSipAddrIfNullOrEmpty(final Map<String, Object> params, final String key) {
		final String param = (String)params.get(key);
		if (Strings.isNullOrEmpty(param)) {
		    final String generatedString = generateRandomString(5, 8);
			params.put(key, generatedString + " <sip:" + generatedString + "@home>");
		}
	}
	
	public static void generateRandomViaAddrIfNullOrEmpty(final Map<String, Object> params, final String key) {
		if (Strings.isNullOrEmpty((String)params.get(key))) {
			final String generatedString = generateRandomString(5, 8);
			params.put(key, "SIP/2.0/TCP random." + generatedString + ".domain:5060");
		}
	}
	
	public static String generateRandomString(final int minLength, final int maxLength) {
		final Random rnd = new Random();
		final byte[] buffer = new byte[rnd.nextInt(maxLength-minLength+1) + minLength];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = (byte) ((rnd.nextFloat() * (122-97)) + 97);
		}
		return new String(buffer);
	}
}
