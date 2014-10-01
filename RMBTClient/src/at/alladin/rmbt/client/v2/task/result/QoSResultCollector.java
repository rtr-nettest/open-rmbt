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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * collects all test results
 * @author lb
 *
 */
public class QoSResultCollector {
	private List<QoSTestResult> results;
	
	public QoSResultCollector() {
		results = new ArrayList<QoSTestResult>();
	}

	/**
	 * 
	 * @return
	 */
	public List<QoSTestResult> getResults() {
		return results;
	}

	/**
	 * 
	 * @param results
	 */
	public void setResults(List<QoSTestResult> results) {
		this.results = results;
	}
	
	/**
	 * 
	 * @return
	 */
	public JSONArray toJson() {
		JSONArray json = null;
		json = new JSONArray();
		for (QoSTestResult result : results) {
			json.put(new JSONObject(result.getResultMap()));
		}
		return json;
	}
}
