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
package at.alladin.rmbt.db;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.alladin.rmbt.qos.AbstractResult;
import at.alladin.rmbt.qos.DnsResult;
import at.alladin.rmbt.qos.HttpProxyResult;
import at.alladin.rmbt.qos.NonTransparentProxyResult;
import at.alladin.rmbt.qos.QoSUtil.TestUuid.UuidType;
import at.alladin.rmbt.qos.TcpResult;
import at.alladin.rmbt.qos.TracerouteResult;
import at.alladin.rmbt.qos.UdpResult;
import at.alladin.rmbt.qos.VoipResult;
import at.alladin.rmbt.qos.WebsiteResult;

/**
 * 
 * @author lb
 *
 */
public class QoSTestResult implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static enum TestType {
		HTTP_PROXY(HttpProxyResult.class), 
		DNS(DnsResult.class), 
		TCP(TcpResult.class), 
		UDP(UdpResult.class), 
		WEBSITE(WebsiteResult.class), 
		NON_TRANSPARENT_PROXY(NonTransparentProxyResult.class),
		TRACEROUTE(TracerouteResult.class),
		VOIP(VoipResult.class);
		
		protected Class<? extends AbstractResult<?>> clazz; 
		
		TestType(Class<? extends AbstractResult<?>> clazz) {
			this.clazz = clazz;
		}
		
		public Class<? extends AbstractResult<?>> getClazz() {
			return this.clazz;
		}
	}
	
    private Long uid;
    private Long testUid;
    private Long qosTestObjectiveId;
    private String testType;
    private String results;
    private String testDescription;
    private String testSummary;
    private JSONArray expectedResults;
    private AbstractResult<?> result;
    private Map<String, String> resultKeyMap = new HashMap<>();
    
	private int successCounter = 0;
    private int failureCounter = 0;

	/**
     * 
     */
    public QoSTestResult() {
    	
    }

	public Long getUid() {
		return uid;
	}

	public void setUid(Long uid) {
		this.uid = uid;
	}

	public Long getTestUid() {
		return testUid;
	}

	public void setTestUid(Long testUid) {
		this.testUid = testUid;
	}

	public String getTestType() {
		return testType;
	}

	public void setTestType(String testType) {
		this.testType = testType;
	}

	public String getResults() {
		return results;
	}

	public void setResults(String results) {
		this.results = results;
	}

	public Long getQoSTestObjectiveId() {
		return qosTestObjectiveId;
	}

	public void setQoSTestObjectiveId(Long qosTestObjectiveId) {
		this.qosTestObjectiveId = qosTestObjectiveId;
	}

	public JSONArray getExpectedResults() {
		return expectedResults;
	}

	public void setExpectedResults(JSONArray expectedResults) {
		this.expectedResults = expectedResults;
	}
	
    public int getSuccessCounter() {
		return successCounter;
	}

	public void setSuccessCounter(int successCounter) {
		this.successCounter = successCounter;
	}

	public int getFailureCounter() {
		return failureCounter;
	}

	public void setFailureCounter(int failureCounter) {
		this.failureCounter = failureCounter;
	}

	public String getTestDescription() {
		return testDescription;
	}

	public void setTestDescription(String testDescription) {
		this.testDescription = testDescription;
	}

	public AbstractResult<?> getResult() {
		return result;
	}

	public void setResult(AbstractResult<?> result) {
		this.result = result;
	}

	public String getTestSummary() {
		return testSummary;
	}

	public void setTestSummary(String testSummary) {
		this.testSummary = testSummary;
	}
	
    public Map<String, String> getResultKeyMap() {
		return resultKeyMap;
	}

	public void setResultKeyMap(Map<String, String> resultKeyMap) {
		this.resultKeyMap = resultKeyMap;
	}
	
	/**
	 * 
	 * @return
	 * @throws JSONException 
	 */
	public JSONObject toJson(UuidType exportType) throws JSONException {
		try {
			JSONObject json = new JSONObject();
			json.put("uid", getUid());
			json.put("test_type", getTestType());
			json.put("result", new JSONObject(getResults()));
			json.put("test_desc", getTestDescription());
			json.put("success_count", getSuccessCounter());
			json.put("failure_count", getFailureCounter());
			json.put("test_summary", String.valueOf(getTestSummary()));
			
			if (resultKeyMap != null && resultKeyMap.size() > 0) {
				json.put("test_result_keys", new JSONArray(resultKeyMap.keySet()));
				json.put("test_result_key_map", new JSONObject(resultKeyMap));
			}
	
			if (exportType.equals(UuidType.TEST_UUID)) {
				json.put("nn_test_uid", getQoSTestObjectiveId());  // TODO: remove backwards compatibility (<= 2.0.28/20028)
				json.put("qos_test_uid", getQoSTestObjectiveId());
				json.put("test_uid", getTestUid());
			}
			return json;
		}
		catch (final Exception e) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return "QoSTestResult [uid=" + uid + ", testUid=" + testUid
				+ ", qosTestObjectiveId="
				+ qosTestObjectiveId + ", testType=" + testType
				+ ", results=" + results + ", testDescription="
				+ testDescription + ", testSummary=" + testSummary
				+ ", expectedResults=" + expectedResults.toString()
				+ ", result=" + result + ", successCounter=" + successCounter
				+ ", failureCounter=" + failureCounter + "]";
	}
}
