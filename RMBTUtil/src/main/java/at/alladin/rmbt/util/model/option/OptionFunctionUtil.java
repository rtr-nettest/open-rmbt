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

import java.util.Map;

public class OptionFunctionUtil {

	/**
	 * function: "drop_param": removes a specific parameter
	 * @param paramMap
	 * @param function
	 */
	public static void funcDropParam(Map<String, Object> paramMap, OptionFunction function) {
		if (paramMap != null) {
			paramMap.remove(function.getParameterMap().get("key"));
		}
	}
}
