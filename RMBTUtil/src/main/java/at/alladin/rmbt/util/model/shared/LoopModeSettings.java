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
package at.alladin.rmbt.util.model.shared;

import com.google.gson.annotations.SerializedName;

public class LoopModeSettings {

	protected Long uid;
	
	@SerializedName("test_uuid")
	protected String testUuid;
	
	@SerializedName("client_uuid")
	protected String clientUuid;
	
	@SerializedName("max_delay")
	protected int maxDelay;
	
	@SerializedName("max_movement")
	protected int maxMovement;
	
	@SerializedName("max_tests")
	protected int maxTests;
	
	@SerializedName("text_counter")
	protected int testCounter;

	public LoopModeSettings() { }
	
	public LoopModeSettings(final int maxDelay, final int maxMovement, final int maxTests, final int testCounter) {
		setMaxDelay(maxDelay);
		setMaxMovement(maxMovement);
		setMaxTests(maxTests);
		setTestCounter(testCounter);
	}
	
	public int getMaxDelay() {
		return maxDelay;
	}

	public void setMaxDelay(int maxDelay) {
		this.maxDelay = maxDelay;
	}

	public int getMaxMovement() {
		return maxMovement;
	}

	public void setMaxMovement(int maxMovement) {
		this.maxMovement = maxMovement;
	}

	public int getMaxTests() {
		return maxTests;
	}

	public void setMaxTests(int maxTests) {
		this.maxTests = maxTests;
	}

	public int getTestCounter() {
		return testCounter;
	}

	public void setTestCounter(int testCounter) {
		this.testCounter = testCounter;
	}

	public String getTestUuid() {
		return testUuid;
	}

	public void setTestUuid(String testUuid) {
		this.testUuid = testUuid;
	}

	public String getClientUuid() {
		return clientUuid;
	}

	public void setClientUuid(String clientUuid) {
		this.clientUuid = clientUuid;
	}

	public Long getUid() {
		return uid;
	}

	public void setUid(Long uid) {
		this.uid = uid;
	}

	@Override
	public String toString() {
		return "LoopModeSettings [uid=" + uid + ", testUuid=" + testUuid + ", clientUuid=" + clientUuid + ", maxDelay="
				+ maxDelay + ", maxMovement=" + maxMovement + ", maxTests=" + maxTests + ", testCounter=" + testCounter
				+ "]";
	}
}
