/*******************************************************************************
 * Copyright 2019 alladin-IT GmbH
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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.full;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Felix Kendlbacher (alladin-IT GmbH)
 */
@io.swagger.annotations.ApiModel(description = "This DTO class contains all QoS measurement information that is sent to the measurement agent.")
@JsonClassDescription("This DTO class contains all QoS measurement information that is sent to the measurement agent.")
public class QoSTypeDescription {

    /**
     * The translated name of the qos type.
     */
    @io.swagger.annotations.ApiModelProperty(required = true, value = "The translated name of the qos type.")
    @JsonPropertyDescription("The translated name of the qos type.")
    @Expose
    @SerializedName("name")
    @JsonProperty(required = true, value = "name")
    private String name;

    /**
     * The translated description of the qos type.
     */
    @io.swagger.annotations.ApiModelProperty(required = true, value = "The translated description of the qos type.")
    @JsonPropertyDescription("The translated description of the qos type.")
    @Expose
    @SerializedName("description")
    @JsonProperty(required = true, value = "description")
    private String description;

    /**
     * The icon of the qos type.
     */
    @io.swagger.annotations.ApiModelProperty(required = true, value = "The icon of the qos type.")
    @JsonPropertyDescription("The icon of the qos type.")
    @Expose
    @SerializedName("icon")
    @JsonProperty(required = true, value = "icon")
    private String icon;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}
    
}
