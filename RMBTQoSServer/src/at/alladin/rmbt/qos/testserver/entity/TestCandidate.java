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
package at.alladin.rmbt.qos.testserver.entity;

/**
 * 
 * @author lb
 *
 */
public class TestCandidate extends AbstractTtlObject {
	public final static int DEFAULT_TTL = 30000; 
	
	private int resetTtl = DEFAULT_TTL;
	private int testCounter = 0;
	
	/**
	 * 
	 * @return
	 */
	public int getResetTtl() {
		return resetTtl;
	}

	/**
	 * 
	 * @param resetTtl
	 */
	public void setResetTtl(int resetTtl) {
		this.resetTtl = resetTtl;
	}

	/**
	 * 
	 */
	public int increaseTestCounter(boolean resetTtl) {
		if (resetTtl) {
			resetTtl(this.resetTtl);
		}
		return ++testCounter;
	}

	/**
	 * 
	 * @return
	 */
	public int decreaseTestCounter(boolean resetTtl) {
		if (resetTtl) {
			resetTtl(this.resetTtl);
		}
		return --testCounter;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getTestCounter() {
		return testCounter;
	}
	
	/**
	 * 
	 * @param counter
	 */
	public void setTestCounter(int counter) {
		testCounter = counter;
	}
}
