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

public class ClassificationCapability {

	public final static int DEFAULT_CLASSIFICATON_COUNT = 3;
	
	@SerializedName("count")
	protected int count = DEFAULT_CLASSIFICATON_COUNT;

	/**
	 * amount of classification items supported by client
	 * @return
	 */
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "ClassificationCapability [count=" + count + "]";
	}
}
