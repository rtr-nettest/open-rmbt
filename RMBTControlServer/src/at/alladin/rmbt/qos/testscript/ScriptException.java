/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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
package at.alladin.rmbt.qos.testscript;

/**
 * 
 * @author lb
 *
 */
public class ScriptException extends Exception {

	public final static String ERROR_INVALID_ARGUMENT_COUNT = "TestScript - Invalid argument count: ";
	
	public final static String ERROR_BAD_ARGUMENTS = "TestScript - Bad arguments: ";
	
	public final static String ERROR_UNKNOWN = "TestScript - Unknown error: ";
	
	public final static String ERROR_PARSER_IS_NULL = "TestScript - HstoreParser must not be NULL in ";
	
	public final static String ERROR_RESULT_IS_NULL = "TestScript - AsbtractResult must not be NULL in ";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 * @param errMsg
	 */
	public ScriptException(String errMsg) {
		super(errMsg);
	}
	
}
