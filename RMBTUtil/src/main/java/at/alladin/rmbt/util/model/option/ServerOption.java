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
package at.alladin.rmbt.util.model.option;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author lb
 *
 */
public class ServerOption implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static class ServerOptionParentDeserializer implements JsonDeserializer<ServerOption> {

		@Override
		public ServerOption deserialize(JsonElement json, Type arg1,
				JsonDeserializationContext arg2) throws JsonParseException {
			Gson gson = new Gson();
			final ServerOption item = gson.fromJson(json, ServerOption.class);
			setParentRecursive(item, null);			
			return item;
		}
		
		private void setParentRecursive(final ServerOption option, final ServerOption parent) {
			option.setParent(parent);
			if (option.getOptionList() != null) {
				for (final ServerOption child : option.getOptionList()) {
					setParentRecursive(child, option);
				}			
			}
		}
		
	}
	
	public static Gson getGson() {
		Gson gson = new GsonBuilder().
				registerTypeAdapter(ServerOption.class, new ServerOptionParentDeserializer()).
				excludeFieldsWithoutExposeAnnotation().
				create();
		return gson;
	}

	@Expose
	@SerializedName("title")
	protected String title;
	
	@Expose
	@SerializedName("summary")
	protected String summary;
	
	@Expose
	@SerializedName("options")
	protected List<ServerOption> optionList;
	
	@Expose
	@SerializedName("params")
	protected Map<String, Object> parameterMap;
	
	@Expose
	@SerializedName("functions")
	protected List<OptionFunction> functionList;
	
	@Expose
	@SerializedName("depends_on")
	protected Map<String, Object> dependsOnMap;
	
	@Expose
	@SerializedName("default")
	protected boolean isDefault = false;
	
	protected ServerOption parent;
	
	protected boolean isEnabled = true;
	
	protected boolean isChecked = false;

	public ServerOption() {
		
	}

	public ServerOption(final String title) {
		this(title, null);
	}

	public ServerOption(final String title, final String summary) {
		this.title = title;
		this.summary = summary;
	}
	
	public ServerOption getParent() {
		return parent;
	}

	public void setParent(ServerOption parent) {
		this.parent = parent;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public List<ServerOption> getEnabledOptionList() {
		if (optionList != null) {
			final List<ServerOption> availableOptionList = new ArrayList<>();
			for (final ServerOption o : optionList) {
				if (o.isEnabled()) {
					availableOptionList.add(o);
				}
			}
			
			return availableOptionList;
		}
		
		return null;
	}
	
	public List<ServerOption> getOptionList() {
		return optionList;
	}

	public void setOptionList(List<ServerOption> optionList) {
		this.optionList = optionList;
	}
	
	public void addOption(final ServerOption option) {
		if (this.optionList == null) {
			this.optionList = new ArrayList<>();
		}
		
		this.optionList.add(option);
	}

	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}
	
	public void addParameter(final String key, final Object value) {
		if (this.parameterMap == null) {
			this.parameterMap = new HashMap<>();
		}
		
		this.parameterMap.put(key, value);
	}

	public List<OptionFunction> getFunctionList() {
		return functionList;
	}

	public void setFunctionMap(List<OptionFunction> functionList) {
		this.functionList = functionList;
	}

	public void addFunction(final OptionFunction value) {
		if (this.functionList == null) {
			this.functionList = new ArrayList<>();
		}
		
		this.functionList.add(value);
	}
	
	public Map<String, Object> getDependsOnMap() {
		return dependsOnMap;
	}

	public void setDependsOnMap(Map<String, Object> dependsOnMap) {
		this.dependsOnMap = dependsOnMap;
	}
	
	public void addDependsOn(final String key, final Object value) {
		if (this.dependsOnMap == null) {
			this.dependsOnMap = new HashMap<>();
		}
		
		this.dependsOnMap.put(key, value);
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {	
		if (optionList != null) {
			for (final ServerOption o : optionList) {
				o.setEnabled(isEnabled);
			}
		}
		
		this.isEnabled = isEnabled;
	}
	
	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		if (!isChecked && optionList != null) {
			for (final ServerOption o : optionList) {
				o.setChecked(false);
			}
		}
		
		this.isChecked = isChecked;
	}
	
	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * 
	 * @param option
	 * @return
	 */
	public boolean isInHierarchyBelow(ServerOption option) {
		if (getParent() != null) {
			ServerOption o = getParent();
			while (o.getParent() != null) {
				if (option.equals(o)) {
					return true;
				}
				o = o.getParent();
			}
		}
		
		return false;
	}

	@Override
	public String toString() {
		return "ServerOption [title=" + title + ", summary=" + summary
				+ ", optionList=" + optionList + ", parameterMap="
				+ parameterMap + ", functionList=" + functionList
				+ ", dependsOnMap=" + dependsOnMap + ", isDefault=" + isDefault
				+ ", isEnabled=" + isEnabled + ", isChecked=" + isChecked + "]";
	}
}
