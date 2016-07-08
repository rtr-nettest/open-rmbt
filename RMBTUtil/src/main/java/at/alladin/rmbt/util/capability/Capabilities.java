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

/**
 * 
 * @author lb
 *
 */
public class Capabilities {
	
	@SerializedName("classification")
	protected ClassificationCapability classification = new ClassificationCapability();
	
	@SerializedName("qos")
	protected QualityOfServiceCapability qos = new QualityOfServiceCapability();

	/**
	 * classification capability
	 * @return
	 */
	public ClassificationCapability getClassificationCapability() {
		return classification;
	}

	public void setClassification(ClassificationCapability classification) {
		this.classification = classification;
	}

	/**
	 * qos capabilities
	 * @return
	 */
	public QualityOfServiceCapability getQosCapability() {
		return qos;
	}

	public void setQos(QualityOfServiceCapability qos) {
		this.qos = qos;
	}

	@Override
	public String toString() {
		return "Capabilities [classification=" + classification + ", qos="
				+ qos + "]";
	}
}
