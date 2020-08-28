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

package at.rtr.rmbt.shared.qos;

import at.rtr.rmbt.shared.qos.util.SipTaskHelper;
import com.fasterxml.jackson.annotation.JsonProperty;



/**
 * @author lb@alladin.at
 */
public class SipResult extends AbstractResult {

	@JsonProperty(SipTaskHelper.PARAM_OBJECTIVE_PORT)
	private Integer port;

	@JsonProperty(SipTaskHelper.PARAM_OBJECTIVE_TIMEOUT)
	private Long timeout;

	@JsonProperty(SipTaskHelper.PARAM_OBJECTIVE_COUNT)
	private Long count;

	@JsonProperty(SipTaskHelper.PARAM_OBJECTIVE_CALL_DURATION)
	private Long callDuration;

	@JsonProperty(SipTaskHelper.PARAM_OBJECTIVE_TO)
	private String objectiveTo;
	
	@JsonProperty(SipTaskHelper.PARAM_OBJECTIVE_FROM)
	private String objectivefrom;

	@JsonProperty(SipTaskHelper.PARAM_OBJECTIVE_VIA)
	private String objectiveVia;
	
	@JsonProperty(SipTaskHelper.PARAM_RESULT_TO)
	private String resultTo;
	
	@JsonProperty(SipTaskHelper.PARAM_RESULT_FROM)
	private String resultFrom;

	@JsonProperty(SipTaskHelper.PARAM_RESULT_VIA)
	private String resultVia;

	@JsonProperty(SipTaskHelper.PARAM_RESULT)
	private String result;
	
	@JsonProperty(SipTaskHelper.PARAM_RESULT_CCSR)	
	private Object callCompletionSuccessRate;

	@JsonProperty(SipTaskHelper.PARAM_RESULT_CSSR)	
	private Object callSetupSuccessRate;

	@JsonProperty(SipTaskHelper.PARAM_RESULT_DCR)	
	private Object callDroppedCallRate;

	/**
	 * 
	 */
	public SipResult() {
		// TODO Auto-generated constructor stub
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Object getCallCompletionSuccessRate() {
		return callCompletionSuccessRate;
	}

	public void setCallCompletionSuccessRate(Double callCompletionSuccessRate) {
		this.callCompletionSuccessRate = callCompletionSuccessRate;
	}

	public Object getCallSetupSuccessRate() {
		return callSetupSuccessRate;
	}

	public void setCallSetupSuccessRate(Double callSetupSuccessRate) {
		this.callSetupSuccessRate = callSetupSuccessRate;
	}

	public Object getCallDroppedCallRate() {
		return callDroppedCallRate;
	}

	public void setCallDroppedCallRate(Double callDroppedCallRate) {
		this.callDroppedCallRate = callDroppedCallRate;
	}
	
	public String getObjectiveTo() {
		return objectiveTo;
	}

	public void setObjectiveTo(String objectiveTo) {
		this.objectiveTo = objectiveTo;
	}

	public String getObjectivefrom() {
		return objectivefrom;
	}

	public void setObjectivefrom(String objectivefrom) {
		this.objectivefrom = objectivefrom;
	}

	public String getResultTo() {
		return resultTo;
	}

	public void setResultTo(String resultTo) {
		this.resultTo = resultTo;
	}

	public String getResultFrom() {
		return resultFrom;
	}

	public void setResultFrom(String resultFrom) {
		this.resultFrom = resultFrom;
	}

	public Long getCallDuration() {
		return callDuration;
	}

	public void setCallDuration(Long callDuration) {
		this.callDuration = callDuration;
	}

	public String getObjectiveVia() {
		return objectiveVia;
	}

	public void setObjectiveVia(String objectiveVia) {
		this.objectiveVia = objectiveVia;
	}

	public String getResultVia() {
		return resultVia;
	}

	public void setResultVia(String resultVia) {
		this.resultVia = resultVia;
	}

	@Override
	public String toString() {
		return "SipResult{" +
				"port=" + port +
				", timeout=" + timeout +
				", count=" + count +
				", callDuration=" + callDuration +
				", objectiveTo='" + objectiveTo + '\'' +
				", objectivefrom='" + objectivefrom + '\'' +
				", objectiveVia='" + objectiveVia + '\'' +
				", resultTo='" + resultTo + '\'' +
				", resultFrom='" + resultFrom + '\'' +
				", resultVia='" + resultVia + '\'' +
				", result='" + result + '\'' +
				", callCompletionSuccessRate=" + callCompletionSuccessRate +
				", callSetupSuccessRate=" + callSetupSuccessRate +
				", callDroppedCallRate=" + callDroppedCallRate +
				"} " + super.toString();
	}
}
