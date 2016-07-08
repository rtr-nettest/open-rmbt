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
package at.alladin.rmbt.qos;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import at.alladin.rmbt.qos.annotations.NonComparableField;
import at.alladin.rmbt.qos.testscript.TestScriptInterpreter;
import at.alladin.rmbt.shared.hstoreparser.annotation.HstoreKey;

/**
 * 
 * @author lb
 *
 */
public abstract class AbstractResult<T> {

	public final static String COMPARATOR_EQUALS = "eq";
	
	public final static String COMPARATOR_NOT_EQUALS = "ne";
	
	public final static String COMPARATOR_GREATER_THEN = "gt";
	
	public final static String COMPARATOR_GREATER_OR_EQUALS = "ge";
	
	public final static String COMPARATOR_LOWER_THEN = "lt";
	
	public final static String COMPARATOR_LOWER_OR_EQUALS = "le";
	
	public final static String COMPARATOR_CONTAINS = "contains";
	
	public final static String RESULT_TYPE_INFO = "info";
	
	public final static String RESULT_TYPE_DEFAULT = "default";
	
	public final static String BEHAVIOUR_ABORT = "abort";
	
	public final static String BEHAVIOUR_NOTHING = "nothing";
	
	@NonComparableField
	protected Map<String, Object> resultMap = new HashMap<String, Object>();
	
	@HstoreKey("operator")
	@NonComparableField
	protected String operator;
	
	@HstoreKey("on_failure")
	@NonComparableField
	protected String onFailure;
	
	@HstoreKey("on_success")
	@NonComparableField
	protected String onSuccess;
	
	@HstoreKey("evaluate")
	protected Object evaluate;
	
	@HstoreKey("end_time_ns")
	protected Long endTimeNs;
	
	@HstoreKey("start_time_ns")
	protected Long startTimeNs;
	
	@HstoreKey("duration_ns")
	protected Long testDuration;
	
	///////////////////////////////////////
	//	Advanced implementations:
	///////////////////////////////////////
	
	@HstoreKey("success_condition")
	@NonComparableField
	protected String successCondition = "true";
	
	/**
	 * can hold following values:
	 * <ul>
	 * <li>default: {@link AbstractResult#RESULT_TYPE_DEFAULT}</li>
	 * <li>{@link AbstractResult#RESULT_TYPE_INFO}: Will not count as a success or failure. The status of the result will be "info"</li>
	 * </ul>
	 */
	@HstoreKey("failure_type")
	@NonComparableField
	protected String failureType = RESULT_TYPE_DEFAULT;

	/**
	 * @see AbstractResult#failureType
	 */
	@HstoreKey("success_type")
	@NonComparableField
	protected String successType = RESULT_TYPE_DEFAULT;
	
	/**
	 * the behaviour of the evaluation if the test fails
	 * <ul>
	 * <li>default: {@link AbstractResult#BEHAVIOUR_NOTHING}</li>
	 * <li>{@link AbstractResult#BEHAVIOUR_ABORT}: Will cause the evaluation to abort. All following expected results will be ignored.</li>
	 * </ul>
	 */
	@HstoreKey("on_failure_behaviour")
	@NonComparableField
	protected String onFailureBehaivour = BEHAVIOUR_NOTHING;

	/**
	 * the behaviour of the evaluation if the test succeeds
	 * @see AbstractResult#onFailureBehaivour
	 */
	@HstoreKey("on_success_behaviour")
	@NonComparableField
	protected String onSuccessBehaivour = BEHAVIOUR_NOTHING;
	
	/**
	 * Test evaluation priority. 
	 * The lower the value the higher the priority
	 * <br>
	 * default: {@link Integer#MAX_VALUE} 
	 */
	@HstoreKey("priority")
	@NonComparableField
	protected Integer priority = Integer.MAX_VALUE;
	
	/**
	 * 
	 */
	public AbstractResult() {
		
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getOnFailure() {
		return onFailure;
	}

	public void setOnFailure(String onFailure) {
		this.onFailure = onFailure;
	}

	public String getOnSuccess() {
		return onSuccess;
	}

	public void setOnSuccess(String onSuccess) {
		this.onSuccess = onSuccess;
	}

	public Object getEvaluate() {
		return evaluate;
	}

	public void setEvaluate(Object evaluate) {
		this.evaluate = evaluate;
	}

	public Long getEndTimeNs() {
		return endTimeNs;
	}

	public void setEndTimeNs(Long endTimeNs) {
		this.endTimeNs = endTimeNs;
	}

	public Long getStartTimeNs() {
		return startTimeNs;
	}

	public void setStartTimeNs(Long startTimeNs) {
		this.startTimeNs = startTimeNs;
	}

	public Long getTestDuration() {
		return testDuration;
	}

	public void setTestDuration(Long testDuration) {
		this.testDuration = testDuration;
	}

	public String getSuccessCondition() {
		return successCondition;
	}

	public void setSuccessCondition(String successCondition) {
		this.successCondition = successCondition;
	}
	
	public String getFailureType() {
		return failureType;
	}

	public void setFailureType(String failureType) {
		this.failureType = failureType;
	}

	public String getSuccessType() {
		return successType;
	}

	public void setSuccessType(String successType) {
		this.successType = successType;
	}
	
	public String getOnFailureBehaivour() {
		return onFailureBehaivour;
	}

	public void setOnFailureBehaivour(String onFailureBehaivour) {
		this.onFailureBehaivour = onFailureBehaivour;
	}

	public String getOnSuccessBehaivour() {
		return onSuccessBehaivour;
	}

	public void setOnSuccessBehaivour(String onSuccessBehaivour) {
		this.onSuccessBehaivour = onSuccessBehaivour;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	
	public Map<String, Object> getResultMap() {
		return resultMap;
	}

	public void setResultJson(JSONObject resultJson) {
		this.resultMap = TestScriptInterpreter.jsonToMap(resultJson);
	}

	@Override
	public String toString() {
		return "AbstractResult [operator=" + operator + ", onFailure="
				+ onFailure + ", onSuccess=" + onSuccess + ", evaluate="
				+ evaluate + ", endTimeNs=" + endTimeNs + ", startTimeNs="
				+ startTimeNs + ", testDuration=" + testDuration
				+ ", successCondition=" + successCondition + ", failureType="
				+ failureType + ", successType=" + successType
				+ ", onFailureBehaivour=" + onFailureBehaivour
				+ ", onSuccessBehaivour=" + onSuccessBehaivour + ", priority="
				+ priority + "]";
	}
}
