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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ServerOptionContainer implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String FUNCTION_DROP_PARAM = "drop_param";
	
	public Map<ServerOption, ServerOption> selectedMap = new HashMap<>();
	
	public List<ServerOption> rootOptionList;
	
	private OptionFunctionCallback functionCallback;
	
	public ServerOptionContainer(final List<ServerOption> serverOptionList) {
		this.rootOptionList = new ArrayList<>();
		this.rootOptionList.addAll(serverOptionList);		
		
		for (final ServerOption o : rootOptionList) {
			this.selectedMap.put(o, null);
		}
	}
	
	public void registerFunctionCallback(final OptionFunctionCallback functionCallback) {
		this.functionCallback = functionCallback;
	}

	public void unregisterFunctionCallback() {
		this.functionCallback = null;
	}
	
	public List<ServerOption> getRootOptions() {
		checkDependencies();
		return rootOptionList;
	}
	
	public ServerOption getDefault(final ServerOption option) {
		return null;
	}
	
	public void setDefault() {
		for (final ServerOption o : rootOptionList) {
			setDefault(o);
		}
	}
	
	private boolean setDefault(final ServerOption option) {
		if (option.getOptionList() != null && !option.isDefault()) {
			for (final ServerOption subOption : option.getOptionList()) {
				if (setDefault(subOption)) {
					return true;
				}
			}
		}
		else if (option.isDefault()) {
			select(option);
			ServerOption p = option.getParent();
			while (p != null) {
				p.setChecked(true);
				p = p.getParent();
			}
			return true;
		}
		
		return false;
	}
	
	private ServerOption setSelected(final ServerOption option, final ServerOption parent) {
		if (parent.getParent() != null) {
			return setSelected(option, parent.getParent());
		}
		else {
			selectedMap.put(parent, option);
			return parent;
		}
	}
	
	/**
	 * unselects given option and returns main parent
	 * @param option
	 * @return
	 */
	public ServerOption unselect(final ServerOption option) {
		option.setChecked(false);
		final ServerOption parent = setSelected(null, option);
		checkDependencies();
		return parent;
	}
	
	/**
	 * selects given option and returns a list of suboptions or null if selected option was last in hierarchy
	 * @param option
	 * @return
	 */
	public List<ServerOption> select(final ServerOption option) {
		if (option.isEnabled()) {
			if (!option.isChecked()) {
				//if selected option not checked yet, unselect all other parent's options
				if (option.getParent() != null && option.getParent().getOptionList() != null) {
					for (final ServerOption o : option.getParent().getOptionList()) {
						o.setChecked(false);
					}
				}
				setSelected(option, option);
				option.setChecked(true);
			}
			checkDependencies();

			if (option.optionList != null && option.optionList.size() > 0) {
				return option.getOptionList();
			}
		}
		
		return null;
	}
		
	/**
	 * 
	 * @return
	 */
	public Map<String, Object> getSelectedParams() {
		return getSelectedParams(true);
	}
	
	/**
	 * returns all parameters from the current selected options
	 * @return
	 */
	private Map<String, Object> getSelectedParams(boolean isUserRequest) {
		final Map<String, Object> paramMap = new HashMap<>();
		for (Entry<ServerOption, ServerOption> e : selectedMap.entrySet()) {
			if (e.getValue() != null && e.getKey().isEnabled()) {
				generateSelectedParams(paramMap, e.getValue(), isUserRequest);
			}
		}
		
		return paramMap;
	}
	
	private void generateSelectedParams(final Map<String, Object> paramMap, final ServerOption option, boolean isUserRequest) {
		if (option.isEnabled()) {
			fillSelectedParamsFromOption(paramMap, option);
			runFunctionsAfterParamSet(option, paramMap, isUserRequest);
			
			if (option.getParent() != null) {
				generateSelectedParams(paramMap, option.getParent(), isUserRequest);
			}
		}
	}
	
	/**
	 * 
	 * @param paramMap
	 * @param option
	 * @return
	 */
	private void fillSelectedParamsFromOption(final Map<String, Object> paramMap, final ServerOption option) {
		if (option.getParameterMap() != null) {
			for (final Entry<String, Object> e : option.getParameterMap().entrySet()) {
				if (!paramMap.containsKey(e.getKey())) {
					paramMap.put(e.getKey(), e.getValue());
				}
			}
		}
	}
	
	/**
	 * 
	 * @param option
	 * @param paramMap
	 */
	private void runFunctionsAfterParamSet(final ServerOption option, final Map<String, Object> paramMap, final boolean isUserRequest) {
		if (option.getFunctionList() != null) {
			for (final OptionFunction func : option.getFunctionList()) {
				if (func.getName().equals(FUNCTION_DROP_PARAM) && isUserRequest) {
					OptionFunctionUtil.funcDropParam(paramMap, func);
				}
				else if (isUserRequest) {
					if (functionCallback != null) {
						functionCallback.onCall(option, func);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 */
	private void checkDependencies() {
		final Map<String, Object> paramMap = getSelectedParams(false);
		for (final ServerOption o : selectedMap.keySet()) {
			checkDependenciesRecursive(o, paramMap);
		}
	}
	
	/**
	 * 
	 * @param option
	 * @param paramMap
	 */
	private void checkDependenciesRecursive(final ServerOption option, final Map<String, Object> paramMap) {
		option.setEnabled(checkOptionDependencies(option, paramMap));
			
		if (option.isEnabled() && option.getOptionList() != null) {
			for (final ServerOption subOption : option.getOptionList()) {
				checkDependenciesRecursive(subOption, paramMap);
			}
		}
	}
	
	/**
	 * 
	 * @param option
	 * @param paramMap
	 * @return
	 */
	private boolean checkOptionDependencies(final ServerOption option, final Map<String, Object> paramMap) {
		if (option.getDependsOnMap() != null) {
			for (Entry<String, Object> e : option.getDependsOnMap().entrySet()) {
				if (!paramMap.containsKey(e.getKey()) || !paramMap.get(e.getKey()).equals(e.getValue())) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param option
	 * @return
	 */
	public boolean isAnyChildSelected(final ServerOption option) {
		if (option.getOptionList() != null) {
			for (final ServerOption o : option.getOptionList()) {
				if (o.isChecked()) {
					return true;
				}
			}
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param option
	 * @return
	 */
	public List<ServerOption> getSelectedSubOptions(final ServerOption option) {
		return getSelectedSubOptionsRecursive(option, option);
	}
	
	/**
	 * 
	 * @param parent
	 * @param option
	 * @return
	 */
	private List<ServerOption> getSelectedSubOptionsRecursive(final ServerOption parent, final ServerOption option) {
		if (parent.getParent() != null) {
			return getSelectedSubOptionsRecursive(parent.getParent(), option);
		}
		else {
			final List<ServerOption> subList = new ArrayList<>();
			ServerOption selectedOption = selectedMap.get(parent);
			if (selectedOption != null) {
				while (selectedOption.getParent() != null && !selectedOption.equals(option)) {
					subList.add(selectedOption);
					selectedOption = selectedOption.getParent();
				}
			}
			
			return subList;
		}
	}
}
