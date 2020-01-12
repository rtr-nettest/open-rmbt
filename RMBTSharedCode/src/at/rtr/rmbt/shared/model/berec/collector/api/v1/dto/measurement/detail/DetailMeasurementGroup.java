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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.detail;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Measurement detail group object which contains a translated title, an optional description, and icon and the items.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Measurement detail group object which contains a translated title, an optional description, and icon and the items.")
@JsonClassDescription("Measurement detail group object which contains a translated title, an optional description, and icon and the items.")
public class DetailMeasurementGroup {
	
	/**
	 * The already translated title of the given group.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The already translated title of the given group.")
	@JsonPropertyDescription("The already translated title of the given group.")
	@Expose
	@SerializedName("title")
	@JsonProperty(required = true, value = "title")
	private String title;
	
	/**
	 * The already translated (optional) description of the given group.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The already translated (optional) description of the given group.")
	@JsonPropertyDescription("The already translated (optional) description of the given group.")
	@Expose
	@SerializedName("description")
	@JsonProperty(required = true, value = "description")
	private String description;
	
	/**
	 * The icon to be used for the given group (as a single char in the corresponding icon font).
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "The icon to be used for the given group (as a single char in the corresponding icon font).")
	@JsonPropertyDescription("The icon to be used for the given group (as a single char in the corresponding icon font).")
	@Expose
	@SerializedName("icon_character")
	@JsonProperty(required = true, value = "icon_character")
	private String iconCharacter;
	
	/**
	 * Contains all the entries of the given group.
	 */
	@io.swagger.annotations.ApiModelProperty(required = true, value = "Contains all the entries of the given group.")
	@JsonPropertyDescription("Contains all the entries of the given group.")
	@Expose
	@SerializedName("items")
	@JsonProperty(required = true, value = "items")
	private List<DetailMeasurementGroupItem> items;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIconCharacter() {
		return iconCharacter;
	}

	public void setIconCharacter(String iconCharacter) {
		this.iconCharacter = iconCharacter;
	}

	public List<DetailMeasurementGroupItem> getItems() {
		return items;
	}

	public void setItems(List<DetailMeasurementGroupItem> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "DetailMeasurementGroup [title=" + title + ", description=" + description + ", iconCharacter="
				+ iconCharacter + ", items=" + items + "]";
	}
	
}
