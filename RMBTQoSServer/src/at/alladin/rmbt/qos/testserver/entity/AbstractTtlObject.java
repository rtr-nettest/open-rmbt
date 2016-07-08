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
 * @param <T>
 */
public class AbstractTtlObject {
	
	/**
	 * 
	 */
	private long ttl = 0;

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
	public void resetTtl(long ttl) {
		setTtl(System.currentTimeMillis() + ttl);
	}
}
