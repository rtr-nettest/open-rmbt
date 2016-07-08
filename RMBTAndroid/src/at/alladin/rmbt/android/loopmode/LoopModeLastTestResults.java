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
package at.alladin.rmbt.android.loopmode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.alladin.rmbt.client.v2.task.result.QoSServerResultCollection;

public class LoopModeLastTestResults {

	public static enum RMBTLoopLastTestStatus {
		OK,
		NOT_FINISHED,
		ERROR,
		TEST_RESULTS_MISSING_UUID,
		REJECTED
	}
	
	public static enum RMBTLoopFetchingResultsStatus {
		FETCHING,
		OK,
		ERROR
	}

	private long localStartTimeStamp = System.currentTimeMillis();
	
    private RMBTLoopLastTestStatus status = LoopModeLastTestResults.RMBTLoopLastTestStatus.NOT_FINISHED;

    private QoSServerResultCollection qosResult;
    
    private JSONObject testResults;
    
    private String testUuid;
    
    private String openTestUuid;
    
    private RMBTLoopFetchingResultsStatus qosResultStatus = RMBTLoopFetchingResultsStatus.FETCHING;
    
    private RMBTLoopFetchingResultsStatus testResultStatus = RMBTLoopFetchingResultsStatus.FETCHING;
    
	public RMBTLoopLastTestStatus getStatus() {
		return status;
	}

	public void setStatus(RMBTLoopLastTestStatus status) {
		this.status = status;
	}
	
	public QoSServerResultCollection getQosResult() {
		return qosResult;
	}

	public void setQosResult(QoSServerResultCollection qosResult) {
		this.qosResult = qosResult;
	}

	public RMBTLoopFetchingResultsStatus getQosResultStatus() {
		return qosResultStatus;
	}

	public void setQosResultStatus(RMBTLoopFetchingResultsStatus qosResultStatus) {
		this.qosResultStatus = qosResultStatus;
	}

	public RMBTLoopFetchingResultsStatus getTestResultStatus() {
		return testResultStatus;
	}

	public void setTestResultStatus(RMBTLoopFetchingResultsStatus testResultStatus) {
		this.testResultStatus = testResultStatus;
	}

	public void setQoSResult(final JSONArray serverResponse) {
		try {
			if (this.testResults != null) {
				this.qosResult = new QoSServerResultCollection(serverResponse);
				this.qosResultStatus = RMBTLoopFetchingResultsStatus.OK;				
			}
			else {
				this.qosResultStatus = RMBTLoopFetchingResultsStatus.ERROR;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			this.qosResultStatus = RMBTLoopFetchingResultsStatus.ERROR;
		}
	}

	public JSONObject getTestResults() {
		return testResults;
	}

	public void setTestResults(JSONArray testResults) {
		try {
			this.testResults = testResults.getJSONObject(0);
			if (this.testResults != null) {
				this.testResultStatus = RMBTLoopFetchingResultsStatus.OK;
			}
			else {
				this.testResultStatus = RMBTLoopFetchingResultsStatus.ERROR;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			this.testResultStatus = RMBTLoopFetchingResultsStatus.ERROR;
		}
	}

	public String getTestUuid() {
		return testUuid;
	}

	public void setTestUuid(String testUuid) {
		this.testUuid = testUuid;
	}

	public String getOpenTestUuid() {
		return openTestUuid;
	}

	public void setOpenTestUuid(String openTestUuid) {
		this.openTestUuid = openTestUuid;
	}

	public long getLocalStartTimeStamp() {
		return localStartTimeStamp;
	}

	public void setLocalStartTimeStamp(long localStartTimeStamp) {
		this.localStartTimeStamp = localStartTimeStamp;
	}
}
