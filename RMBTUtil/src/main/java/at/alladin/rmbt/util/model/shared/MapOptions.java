/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
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

import java.io.Serializable;
import java.util.List;

import at.alladin.rmbt.util.model.option.ServerOption;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MapOptions implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Expose
	@SerializedName("map_filters")
	public List<ServerOption> mapFilterList;

	public List<ServerOption> getMapFilterList() {
		return mapFilterList;
	}

	public void setMapFilterList(List<ServerOption> mapFilterList) {
		this.mapFilterList = mapFilterList;
	}

	@Override
	public String toString() {
		return "MapOptions [mapFilterList=" + mapFilterList + "]";
	}
}
