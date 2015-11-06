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

import org.json.JSONException;
import org.json.JSONObject;

import at.alladin.rmbt.db.QoSTestResult.TestType;

public class QoSTestTypeDesc implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long uid;
	private TestType testType;
	private String description;
	private String name;
	
	public QoSTestTypeDesc() {
		
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public TestType getTestType() {
		return testType;
	}

	public void setTestType(TestType testType) {
		this.testType = testType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return
	 * @throws JSONException 
	 */
	public JSONObject toJson() {
		final JSONObject json = new JSONObject();
	
		try {
			if (getTestType() == null) {
				throw new IllegalArgumentException("test_type not found: " + getName());
			}
			json.put("test_type", getTestType().name());
			json.put("desc", getDescription());
			json.put("name", getName());
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return json;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "QoSTestTypeDesc [uid=" + uid + ", testType=" + testType
				+ ", description=" + description + ", name=" + name + "]";
	}
}
