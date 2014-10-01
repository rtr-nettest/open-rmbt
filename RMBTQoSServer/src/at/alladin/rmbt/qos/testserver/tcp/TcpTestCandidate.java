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
package at.alladin.rmbt.qos.testserver.tcp;

public class TcpTestCandidate {
	public static int TTL = 30000;
	private int testCounter = 0;
	private long ttl = 0;
	
	/**
	 * 
	 */
	public int increaseTestCounter(boolean resetTtl) {
		if (resetTtl) {
			resetTtl();
		}
		return ++testCounter;
	}

	/**
	 * 
	 * @return
	 */
	public int decreaseTestCounter(boolean resetTtl) {
		if (resetTtl) {
			resetTtl();
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

	/**
	 * 
	 * @return
	 */
	public long getTtl() {
		return ttl;
	}

	/**
	 * 
	 * @param ttl
	 */
	public void setTtl(long ttl) {
		this.ttl = ttl;
	}

	/**
	 * 
	 */
	public void resetTtl() {
		setTtl(System.currentTimeMillis() + TTL);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TcpTestCandidate [testCounter=" + testCounter + ", ttl=" + ttl
				+ "]";
	}
}
