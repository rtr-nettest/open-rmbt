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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ComputedNetworkPointInTimeInfoDto extends NetworkPointInTimeInfoDto {

	/**
	 * @see NatTypeInfo
	 */
	@JsonPropertyDescription("Contains network address translation related information.")
	@Expose
	@SerializedName("nat_type_info")
	@JsonProperty("nat_type_info")
	NatTypeInfoDto natTypeInfo;
	
	/**
	 * The computed mobile frequency band of the signal array.
	 */
	@JsonPropertyDescription("The computed mobile frequency band of the signal array.")
	@Expose
	@SerializedName("mobile_frequency")
	@JsonProperty("mobile_frequency")
	Integer mobileFrequency;
	
	public Integer getMobileFrequency() {
		return mobileFrequency;
	}

	public void setMobileFrequency(Integer mobileFrequency) {
		this.mobileFrequency = mobileFrequency;
	}

	public NatTypeInfoDto getNatTypeInfo() {
		return natTypeInfo;
	}

	public void setNatTypeInfo(NatTypeInfoDto natTypeInfo) {
		this.natTypeInfo = natTypeInfo;
	}
	
}
