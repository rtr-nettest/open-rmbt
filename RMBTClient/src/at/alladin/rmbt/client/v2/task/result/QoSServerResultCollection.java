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
package at.alladin.rmbt.client.v2.task.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.alladin.rmbt.client.v2.task.result.QoSServerResult.DetailType;

/**
 * contains all test results and result descriptions after evaluation
 * @author lb
 *
 */
public class QoSServerResultCollection implements Serializable {

	/**
	 * 
	 * @author lb
	 *
	 */
	public static class QoSResultStats {
		private int successCounter;
		private int failureCounter;
		private int testCounter;
		private int failedTestsCounter; 
		
		private HashMap<QoSTestResultEnum, QoSResultStats> resultCounter = 
				new HashMap<QoSTestResultEnum, QoSServerResultCollection.QoSResultStats>();
		
		public QoSResultStats(int successCounter, int failureCounter) {
			this.successCounter = successCounter;
			this.failureCounter = failureCounter;
		}
		
		public int getSuccessCounter() {
			return getSuccessCounter(null);
		}

		public int getSuccessCounter(QoSTestResultEnum key) {
			if (key != null) {
				QoSResultStats stats = resultCounter.get(key);
				return (stats != null ? stats.getSuccessCounter() : 0);
			}
			else {
				return successCounter;
			}
		}

		public int getFailureCounter() {
			return getFailureCounter(null);
		}
		
		public int getFailureCounter(QoSTestResultEnum key) {
			if (key != null) {
				QoSResultStats stats = resultCounter.get(key);
				return (stats != null ? stats.getFailureCounter() : 0);
			}
			else {
				return failureCounter;
			}
		}

		public int getFailedTestsCounter() {
			return getFailedTestsCounter(null);
		}
		
		public int getFailedTestsCounter(QoSTestResultEnum key) {
			if (key != null) {
				QoSResultStats stats = resultCounter.get(key);
				return (stats != null ? stats.getFailedTestsCounter() : 0);
			}
			else {
				return failedTestsCounter;
			}
		}

		public int getTestCounter() {
			return getTestCounter(null);
		}
		
		public int getTestCounter(QoSTestResultEnum key) {
			if (key != null) {
				QoSResultStats stats = resultCounter.get(key);
				return (stats != null ? stats.getTestCounter() : 0);
			}
			else {
				return testCounter;
			}
		}

		public void incSuccessCounter(int counter) {
			this.successCounter += counter;
		}
		
		public void incFailureCounter(int counter) {
			this.failureCounter += counter;
		}
		
		public void incTestCounter(int counter) {
			this.testCounter += counter;
		}
		
		public void incFailedTestsCounter(int counter) {
			this.failedTestsCounter += counter;
		}
		
		/**
		 * 
		 * @return the amount of all successes and failures (>= tests)
		 */
		public int getTestResultsCounter() {
			return getTestResultsCounter(null);
		}

		/**
		 * 
		 * @param key
		 * @return the amount of all successes and failures for a specific test type (>= tests)
		 */
		public int getTestResultsCounter(QoSTestResultEnum key) {
			if (key != null) {
				QoSResultStats stats = resultCounter.get(key);
				return (stats != null ? stats.getSuccessCounter() + stats.getFailureCounter() : 0);
			}
			else {
				return successCounter + failureCounter;
			}
		}
		
		public int getPercentageForTestResults() {
			return getPercentageForTestResults(null);
		}
		
		public int getPercentageForTestResults(QoSTestResultEnum key) {
			if (key != null) {
				QoSResultStats stats = resultCounter.get(key);
				return (stats != null ? (int) (((float)stats.getSuccessCounter() / (float)stats.getTestResultsCounter()) * 100f) : 0);
			}
			else {
				return (int) (((float)successCounter / (float)getTestResultsCounter()) * 100f);
			}
		}

		public int getPercentageForTests() {
			return getPercentageForTests(null);
		}
		
		public int getPercentageForTests(QoSTestResultEnum key) {
			if (key != null) {
				QoSResultStats stats = resultCounter.get(key);
				return (stats != null ? (int) (((float)(stats.getTestCounter() - stats.getFailedTestsCounter()) / (float)stats.getTestCounter()) * 100f) : 0);
			}
			else {
				return (int) (((float)(testCounter - failedTestsCounter) / (float)getTestCounter()) * 100f);
			}
		}

		public void setCategoryCounter(QoSTestResultEnum key, int successCounter, int failureCounter) {
			this.resultCounter.put(key, new QoSResultStats(successCounter, failureCounter));
		}
		
		public void addCategoryCounter(QoSTestResultEnum key, int successCounter, int failureCounter) {
			QoSResultStats stat = resultCounter.get(key);
			if (stat == null) {
				stat = new QoSResultStats(successCounter, failureCounter);
				this.resultCounter.put(key, stat);
			}
			stat.incSuccessCounter(successCounter);
			stat.incFailureCounter(failureCounter);
			stat.incFailedTestsCounter(failureCounter > 0 ? 1 : 0);
			stat.incTestCounter(1);
			
			this.incSuccessCounter(successCounter);
			this.incFailureCounter(failureCounter);
			this.incFailedTestsCounter(failureCounter > 0 ? 1 : 0);
			this.incTestCounter(1);
		}
		
		public HashMap<QoSTestResultEnum, QoSResultStats> get() {
			return resultCounter;
		}
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static String JSON_KEY_DETAIL = "testresultdetail";
	public final static String JSON_KEY_DESC = "testresultdetail_desc";
	public final static String JSON_KEY_TEST_DESC = "testresultdetail_testdesc";
	
	private JSONArray testResultDetails;
	private JSONArray testResultDesc;
	private JSONArray testResultArray;
	private JSONArray testResultTestDesc;
	
	private QoSResultStats qosStats;
	
	private HashMap<QoSTestResultEnum, List<QoSServerResult>> resultMap = new HashMap<QoSTestResultEnum, List<QoSServerResult>>();
	
	private HashMap<QoSTestResultEnum, List<QoSServerResultDesc>> descMap =	new HashMap<QoSTestResultEnum, List<QoSServerResultDesc>>();
	
	private HashMap<QoSTestResultEnum, QoSServerResultTestDesc> testDescMap = new HashMap<QoSTestResultEnum, QoSServerResultTestDesc>();
	
	public QoSServerResultCollection(JSONArray json) throws JSONException {
		this.testResultArray = json;

		try {
			this.testResultDetails = json.getJSONObject(0).getJSONArray(JSON_KEY_DETAIL);
	        this.testResultDesc = json.getJSONObject(0).getJSONArray(JSON_KEY_DESC);
	        this.testResultTestDesc = json.getJSONObject(0).getJSONArray(JSON_KEY_TEST_DESC);		
		}
		catch (Exception e) {
			//e.printStackTrace();
			throw new JSONException("no data found");
		}
        
        //read test results:
        for (int i = 0; i < testResultDetails.length(); i++) {
        	QoSServerResult result = new QoSServerResult(testResultDetails.getJSONObject(i));
        	
        	if (result.getTestType() != null && result.getResultMap() != null) {
            	List<QoSServerResult> resultList;
            	if (resultMap.containsKey(result.getTestType())) {
            		resultList = resultMap.get(result.getTestType());
            	}
            	else {
            		resultList = new ArrayList<QoSServerResult>();
            		resultMap.put(result.getTestType(), resultList);
            	}
            	resultList.add(result);
        	}        		
        }
        
        //and sort them (failed first)
    	sortResultMap();

        
        //read test descriptions:
        try {
            for (int i = 0; i < testResultTestDesc.length(); i++) {
            	try {
	            	JSONObject desc = testResultTestDesc.getJSONObject(i);
	            	QoSTestResultEnum type = QoSTestResultEnum.valueOf(desc.optString("test_type").toUpperCase(Locale.US));
	            	String testDesc = desc.optString("desc");
	            	String testName = desc.optString("name");
	            	QoSServerResultTestDesc test = new QoSServerResultTestDesc(type, testDesc, testName);
	            	testDescMap.put(type, test);
            	}
	            catch (IllegalArgumentException e) {
	            	//illegal argument: occurs if qos test type is not implemented. in this case continue loop
	            	e.printStackTrace();
            	}
            }        		       	
        }
        catch (Throwable t) {
        	t.printStackTrace();
        }

        
        //read result descriptions:
    	try {
			JSONArray descArray = testResultDesc;
			for (int i = 0; i < descArray.length(); i++) {
				QoSServerResultDesc desc = new QoSServerResultDesc(descArray.getJSONObject(i));
				List<QoSServerResultDesc> descList;
				if (descMap.containsKey(desc.getTestType())) {
					descList = descMap.get(desc.getTestType());
				}
				else {
					descList = new ArrayList<QoSServerResultDesc>();
					descMap.put(desc.getTestType(), descList);
				}
				
				descList.add(desc);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * sorts the result map by specific criteria
	 */
	private void sortResultMap() {
		HashMap<QoSTestResultEnum, List<QoSServerResult>> sortedResultMap = new HashMap<QoSTestResultEnum, List<QoSServerResult>>();
		
		for (Entry<QoSTestResultEnum, List<QoSServerResult>> e : resultMap.entrySet()) {
			List<QoSServerResult> sortedList = new ArrayList<QoSServerResult>();
			Iterator<QoSServerResult> iterator = e.getValue().iterator();
			while (iterator.hasNext()) {
				QoSServerResult i = iterator.next();
				if (i.getFailureCount() > 0) {
					sortedList.add(i);
					iterator.remove();
				}
			}
			
			sortedList.addAll(e.getValue());
			sortedResultMap.put(e.getKey(), sortedList);
		}
		
		resultMap = sortedResultMap;
	}

	public JSONArray getTestResultDetails() {
		return testResultDetails;
	}

	public void setTestResultDetails(JSONArray testResultDetails) {
		this.testResultDetails = testResultDetails;
	}

	public JSONArray getTestResultDesc() {
		return testResultDesc;
	}

	public void setTestResultDesc(JSONArray testResultDesc) {
		this.testResultDesc = testResultDesc;
	}

	public HashMap<QoSTestResultEnum, List<QoSServerResult>> getResultMap() {
		return resultMap;
	}

	public void setResultMap(
			HashMap<QoSTestResultEnum, List<QoSServerResult>> resultMap) {
		this.resultMap = resultMap;
	}

	public HashMap<QoSTestResultEnum, List<QoSServerResultDesc>> getDescMap() {
		return descMap;
	}
	
	/**
	 * 
	 * @param detailType
	 * @return
	 */
	public HashMap<QoSTestResultEnum, List<QoSServerResultDesc>> getDescMap(DetailType detailType) {
		Iterator<QoSTestResultEnum> keys = descMap.keySet().iterator();
		
		HashMap<QoSTestResultEnum, List<QoSServerResultDesc>> map = 
				new HashMap<QoSTestResultEnum, List<QoSServerResultDesc>>();
		
		while (keys.hasNext()) {
			QoSTestResultEnum key = keys.next();
			List<QoSServerResultDesc> descList = new ArrayList<QoSServerResultDesc>();
			
			for (QoSServerResultDesc desc : descMap.get(key)) {
				if (detailType.equals(desc.getStatus())) {
					descList.add(desc);
				}
			}
			
			if (descList.size() > 0) {
				map.put(key, descList);
			}
		}
		
		return map;
	}
	
	/**
	 * 
	 * @param detailType
	 * @return
	 */
	public HashMap<QoSTestResultEnum, List<QoSServerResult>> getResultMap(DetailType detailType) {
		Iterator<QoSTestResultEnum> keys = resultMap.keySet().iterator();
		
		HashMap<QoSTestResultEnum, List<QoSServerResult>> map = 
				new HashMap<QoSTestResultEnum, List<QoSServerResult>>();
		
		while (keys.hasNext()) {
			QoSTestResultEnum key = keys.next();
			List<QoSServerResult> resultList = new ArrayList<QoSServerResult>();
			
			for (QoSServerResult res : resultMap.get(key)) {
				if (detailType.equals(DetailType.FAIL) && res.getFailureCount() > 0) {
					resultList.add(res);
				}
				else {
					resultList.add(res);
				}
			}
			
			if (resultList.size() > 0) {
				map.put(key, resultList);
			}
		}
		
		return map;
	}
	
	/**
	 * 
	 * @param detailType
	 * @return
	 */
	public List<QoSTestResultEnum> getDescTypeList(DetailType detailType) {
		Iterator<QoSTestResultEnum> keys = descMap.keySet().iterator();
		
		List<QoSTestResultEnum> typeList = new ArrayList<QoSTestResultEnum>();
		
		while (keys.hasNext()) {
			QoSTestResultEnum key = keys.next();
			
			for (QoSServerResultDesc desc : descMap.get(key)) {
				if (detailType.equals(desc.getStatus())) {
					typeList.add(key);
					break;
				}
			}			
		}
		
		return typeList;
	}

	public void setDescMap(
			HashMap<QoSTestResultEnum, List<QoSServerResultDesc>> descMap) {
		this.descMap = descMap;
	}

	public JSONArray getTestResultArray() {
		return testResultArray;
	}

	public void setTestResultArray(JSONArray testResultArray) {
		this.testResultArray = testResultArray;
	}

	public JSONArray getTestResultTestDesc() {
		return testResultTestDesc;
	}

	public void setTestResultTestDesc(JSONArray testResultTestDesc) {
		this.testResultTestDesc = testResultTestDesc;
	}

	public HashMap<QoSTestResultEnum, QoSServerResultTestDesc> getTestDescMap() {
		return testDescMap;
	}

	public void setTestDescMap(
			HashMap<QoSTestResultEnum, QoSServerResultTestDesc> testDescMap) {
		this.testDescMap = testDescMap;
	}

	/**
	 * 
	 * @return
	 */
	public QoSResultStats getQoSStatistics() {
		if (this.qosStats == null) {
			this.qosStats = new QoSResultStats(0, 0);
			
			for(Entry<QoSTestResultEnum, List<QoSServerResult>> entry : resultMap.entrySet()) {
				for (QoSServerResult result : entry.getValue()) {
					qosStats.addCategoryCounter(result.getTestType(), result.getSuccessCount(), result.getFailureCount());
				}
			}
			
		}
		
		return this.qosStats;
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "QoSServerResultCollection [testResultDetails="
				+ testResultDetails + ", testResultDesc=" + testResultDesc
				+ ", testResultArray=" + testResultArray + ", resultMap="
				+ resultMap + ", descMap=" + descMap + "]";
	}
}
