/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
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
package at.alladin.rmbt.util.model.option;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OptionFunction implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Expose
	@SerializedName("func_name")
	protected String name;
	
	@Expose
	@SerializedName("func_params")
	protected Map<String, Object> parameterMap;

	public OptionFunction() {
		
	}
	
	public OptionFunction(final String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}
	
	public void addParameter(final String name, final Object value) {
		if (this.parameterMap == null) {
			this.parameterMap = new HashMap<>();
		}
		
		this.parameterMap.put(name, value);
	}

	@Override
	public String toString() {
		return "OptionFunction [name=" + name + ", parameterMap="
				+ parameterMap + "]";
	}
}
