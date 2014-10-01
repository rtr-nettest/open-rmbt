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
package at.alladin.rmbt.client.v2.task.result;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.json.JSONObject;

/**
 * contains the result of a test after evaluation
 * @author lb
 *
 */
public class QoSServerResult implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static enum DetailType {
		OK,
		FAIL
	}

	public final static String JSON_KEY_TESTTYPE = "test_type";
	
	public final static String JSON_KEY_SUCCESS_COUNT = "success_count";
	
	public final static String JSON_KEY_FAILURE_COUNT = "failure_count";
	
	public final static String JSON_KEY_RESULT_MAP = "result";
	
	public final static String JSON_KEY_TEST_DESCRIPTION = "test_desc";
	
	public final static String JSON_KEY_TEST_SUMMARY = "test_summary";
	
	public final static String JSON_KEY_UID = "uid";
	
	private QoSTestResultEnum testType;
	
	private int successCount = 0;
	
	private int failureCount = 0;
	
	private HashMap<String, String> resultMap = new HashMap<String, String>();
	
	private String testDescription;
	
	private String testSummary;
	
	private long uid;
	
	private int displayPosition;
	
	/**
	 * 
	 * @param json
	 */
	@SuppressWarnings("unchecked")
	public QoSServerResult(JSONObject json) {
		try {
			testType = QoSTestResultEnum.valueOf(json.optString(JSON_KEY_TESTTYPE).toUpperCase(Locale.US));
			testDescription = json.optString(JSON_KEY_TEST_DESCRIPTION);
			testSummary = json.optString(JSON_KEY_TEST_SUMMARY);
			successCount = Integer.valueOf(json.optString(JSON_KEY_SUCCESS_COUNT));
			failureCount = Integer.valueOf(json.optString(JSON_KEY_FAILURE_COUNT));
			uid = json.optLong(JSON_KEY_UID);
			JSONObject resultObj = json.optJSONObject(JSON_KEY_RESULT_MAP);
		
			if (resultObj != null) {
				Iterator<String> keys = resultObj.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					resultMap.put(key, resultObj.optString(key));
				}
			}
		}
		catch (Throwable t) {
			testType = null;
			resultMap = null;
		}
	}

	public QoSTestResultEnum getTestType() {
		return testType;
	}

	public void setTestType(QoSTestResultEnum testType) {
		this.testType = testType;
	}

	public int getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(int successCount) {
		this.successCount = successCount;
	}

	public int getFailureCount() {
		return failureCount;
	}

	public void setFailureCount(int failureCount) {
		this.failureCount = failureCount;
	}

	public HashMap<String, String> getResultMap() {
		return resultMap;
	}

	public void setResultMap(HashMap<String, String> resultMap) {
		this.resultMap = resultMap;
	}

	public String getTestDescription() {
		return testDescription;
	}

	public void setTestDescription(String testDescription) {
		this.testDescription = testDescription;
	}

	public String getTestSummary() {
		return testSummary;
	}

	public void setTestSummary(String testSummary) {
		this.testSummary = testSummary;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public int getDisplayPosition() {
		return displayPosition;
	}

	public void setDisplayPosition(int displayPosition) {
		this.displayPosition = displayPosition;
	}

	@Override
	public String toString() {
		return "QoSServerResult [testType=" + testType + ", successCount="
				+ successCount + ", failureCount=" + failureCount
				+ ", resultMap=" + resultMap + ", testDescription="
				+ testDescription + ", testSummary=" + testSummary + ", uid="
				+ uid + ", displayPosition=" + displayPosition + "]";
	}
}
