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
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.alladin.rmbt.shared.Helperfunctions;


/**
 * 
 * POJO for the nn_test_objective table
 * 
 * @author lb
 *
 */
public class QoSTestObjective implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
    private String testType;
    private int testClass;
    private String objective;
    private String results;
    private String testDescription;
    private String testSummary;
    private String testServerIpv4;
    private String testServerIpv6;
    private int port;
    private int concurrencyGroup;
        
    public QoSTestObjective()
    {
    	
    }

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getTestType() {
		return testType;
	}

	public void setTestType(String testType) {
		this.testType = testType;
	}

	public int getTestClass() {
		return testClass;
	}

	public void setTestClass(int testClass) {
		this.testClass = testClass;
	}

	public String getObjective() {
		return objective;
	}

	public void setObjective(String objective) {
		this.objective = objective;
	}

	public String getResults() {
		return results;
	}

	public void setResults(String results) {
		this.results = results;
	}

	public String getTestDescription() {
		return testDescription;
	}

	public void setTestDescription(String testDescription) {
		this.testDescription = testDescription;
	}

	public String getTestServerIpv4() {
		return testServerIpv4;
	}

	public void setTestServerIpv4(String testServerIpv4) {
		this.testServerIpv4 = testServerIpv4;
	}

	public String getTestServerIpv6() {
		return testServerIpv6;
	}

	public void setTestServerIpv6(String testServerIpv6) {
		this.testServerIpv6 = testServerIpv6;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	

	public int getConcurrencyGroup() {
		return concurrencyGroup;
	}

	public void setConcurrencyGroup(int concurrencyGroup) {
		this.concurrencyGroup = concurrencyGroup;
	}

	public String getTestSummary() {
		return testSummary;
	}

	public void setTestSummary(String testSummary) {
		this.testSummary = testSummary;
	}
	
	@Override
	public String toString() {
		return "QoSTestObjective [uid=" + uid + ", testType="
				+ testType + ", testClass=" + testClass + ", objective="
				+ objective + ", results=" + results
				+ ", testDescription=" + testDescription + ", testServerIpv4="
				+ testServerIpv4 + ", testServerIpv6=" + testServerIpv6
				+ ", port=" + port + ", concurrencyGroup=" + concurrencyGroup
				+ ", testSummary=" + testSummary + "]";
	}
	
	@SuppressWarnings("unchecked")
	public String toHtml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<h3>QoS-Test (uid: " + getUid() + ", test_class: " + getTestClass() + ")</h3>");
		if (getObjective() != null) {
			try {
				JSONObject objectives = new JSONObject(getObjective());
				Iterator<String> keys = objectives.keys();
				sb.append("<b>Test objectives (as plain text):</b> <ul>");
				while (keys.hasNext()) {
					String key = keys.next();
					sb.append("<li><i>" + key + "</i>: " + objectives.optString(key) + "</li>");
				}
				sb.append("</ul>");
				sb.append("<b>Test objectives (as hstore representation):</b> " + Helperfunctions.json2hstore(objectives, null) + "<br><br>");
				if (testSummary != null) { 
					sb.append("<b>Test summary (test_summary):</b> <a href=\"#" + testSummary.replaceAll("[\\-\\+\\.\\^:,]", "_") + "\">" + testSummary + "</a><br><br>");
				}
				else {
					sb.append("<b>Test summary (test_summary):</b> <i>NULL</i><br><br>");
				}
				if (testDescription != null) {
					sb.append("<b>Test description (test_desc):</b> <a href=\"#" + testDescription.replaceAll("[\\-\\+\\.\\^:,]", "_") + "\">" + testDescription + "</a><br><br>");	
				}
				else {
					sb.append("<b>Test description (test_desc):</b> <i>NULL</i><br><br>");
				}
			} catch (JSONException e) {
				sb.append("<b><i>incorrect test objectives format:</i></b><ul><li>" + getObjective() + "</li></ul>");
				e.printStackTrace();
			}	
		}
		else {
			sb.append("<b><i>no objectives set for this test</i></b>");
		}
		
		sb.append("<b>Expected test results (as hstore representation):</b><ul>");
		if (getResults() != null) {
			JSONArray resultsJson;
			try {
				resultsJson = new JSONArray(getResults());
				for (int i = 0; i < resultsJson.length(); i++) {
					try {
						final 
						JSONObject expected = resultsJson.getJSONObject(i);
						sb.append("<li>" + Helperfunctions.json2htmlWithLinks(expected) + "</li>");
					} catch (Exception e) {
						e.printStackTrace();
						sb.append("<li>incorrect expected test result format</li>");
					}
				}		
			} catch (JSONException e1) {
				sb.append("<li>incorrect expected test result format</li>");
			}
		}
		else {
			sb.append("<li><i>No expected results set for this test</i></li>");
		}
		sb.append("</ul>");
		return sb.toString();
	}
}
