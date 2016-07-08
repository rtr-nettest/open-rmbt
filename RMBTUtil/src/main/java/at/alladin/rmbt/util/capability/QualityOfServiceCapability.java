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
package at.alladin.rmbt.util.capability;

import com.google.gson.annotations.SerializedName;

public class QualityOfServiceCapability {
	
	public static boolean DEFAULT_QOS_SUPPORTS_INFO = false;

	@SerializedName("supports_info")
	public boolean supportsInfo = DEFAULT_QOS_SUPPORTS_INFO;

	/**
	 * if true the third state (=INFO) is supported
	 * @return
	 */
	public boolean isSupportsInfo() {
		return supportsInfo;
	}

	public void setSupportsInfo(boolean supportsInfo) {
		this.supportsInfo = supportsInfo;
	}

	@Override
	public String toString() {
		return "QualityOfServiceCapability [supportsInfo=" + supportsInfo + "]";
	}
}
