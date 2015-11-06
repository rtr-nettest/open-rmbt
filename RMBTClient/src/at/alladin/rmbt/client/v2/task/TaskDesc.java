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

import java.util.HashMap;

import at.alladin.rmbt.client.RMBTTestParameter;

/**
 * 
 * @author lb
 *
 */
public class TaskDesc extends RMBTTestParameter {
	
	public final static String QOS_TEST_IDENTIFIER_KEY = "qostest";

	/**
	 * 
	 */
	private final HashMap<String, Object> params;	
	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param encryption
	 * @param token
	 * @param duration
	 * @param numThreads
	 * @param startTime
	 */
	public TaskDesc(String host, int port, boolean encryption, String token,
			int duration, int numThreads, int numPings, long startTime, HashMap<String, Object> params) {
		super(host, port, encryption, token, duration, numThreads, numPings, startTime);
		this.params = params;
	}
	
	public TaskDesc(String host, int port, boolean encryption, String token,
			int duration, int numThreads, int numPings, long startTime, HashMap<String, Object> params, String qosTestId) {
		this(host, port, encryption, token, duration, numThreads, numPings, startTime, params);
		params.put(QOS_TEST_IDENTIFIER_KEY, qosTestId);
	}

	/**
	 * 
	 * @return
	 */
	public HashMap<String, Object> getParams() {
		return params;
	}

	@Override
	public String toString() {
		return "TaskDesc [params=" + params + "]";
	}
}
