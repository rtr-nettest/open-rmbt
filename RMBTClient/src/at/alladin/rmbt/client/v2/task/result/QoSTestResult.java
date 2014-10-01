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

import java.util.HashMap;
import java.util.Locale;

import at.alladin.rmbt.client.TestResult;

/**
 * 
 * @author lb
 *
 */
public class QoSTestResult extends TestResult {

	/**
	 * 
	 */
	private final HashMap<String, Object> resultMap;
	
	/**
	 * 
	 */
	private final QoSTestResultEnum testType; 
	
	/**
	 * 
	 */
	private boolean fatalError = false;
	
	/**
	 * 
	 */
	public QoSTestResult(QoSTestResultEnum testType) {
		this.testType = testType;
		resultMap = new HashMap<String, Object>();
		resultMap.put("test_type", testType.name().toLowerCase(Locale.US));
	}

	/**
	 * 
	 * @return
	 */
	public QoSTestResultEnum getTestType() {
		return testType;
	}

	/**
	 * 
	 * @return
	 */
	public HashMap<String, Object> getResultMap() {
		return resultMap;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isFatalError() {
		return fatalError;
	}

	/**
	 * 
	 * @param fatalError
	 */
	public void setFatalError(boolean fatalError) {
		this.fatalError = fatalError;
	}

	@Override
	public String toString() {
		return "QoSTestResult [resultMap=" + resultMap + ", testType="
				+ testType + ", fatalError=" + fatalError + "]";
	}
}
