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
package at.alladin.rmbt.qos;

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
	
	@HstoreKey("operator")
	protected String operator;
	
	@HstoreKey("on_failure")
	protected String onFailure;
	
	@HstoreKey("on_success")
	protected String onSuccess;
	
	@HstoreKey("evaluate")
	protected String evaluate;
	
	@HstoreKey("end_time_ns")
	protected String endTimeNs;
	
	@HstoreKey("start_time_ns")
	protected String startTimeNs;
	
	@HstoreKey("duration_ns")
	protected String testDuration;
	
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

	public String getEvaluate() {
		return evaluate;
	}

	public void setEvaluate(String evaluate) {
		this.evaluate = evaluate;
	}

	public String getEndTimeNs() {
		return endTimeNs;
	}

	public void setEndTimeNs(String endTimeNs) {
		this.endTimeNs = endTimeNs;
	}

	public String getStartTimeNs() {
		return startTimeNs;
	}

	public void setStartTimeNs(String startTimeNs) {
		this.startTimeNs = startTimeNs;
	}

	public String getTestDuration() {
		return testDuration;
	}

	public void setTestDuration(String testDuration) {
		this.testDuration = testDuration;
	}

	@Override
	public String toString() {
		return "AbstractResult [operator=" + operator + ", onFailure="
				+ onFailure + ", onSuccess=" + onSuccess + ", evaluate="
				+ evaluate + ", endTimeNs=" + endTimeNs + ", startTimeNs="
				+ startTimeNs + ", testDuration=" + testDuration + "]";
	}
}
