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
package at.alladin.rmbt.shared.hstoreparser;

/**
 * 
 * @author lb
 *
 */
public class HstoreParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	public final static String HSTORE_OBJECT_KEY_ALREADY_IN_USE = "Hstore key already in use: ";
	
	/**
	 * 
	 */
	public final static String HSTORE_PARSE_EXCEPTION = "Exception during parse: ";

	/**
	 * 
	 */
	public final static String HSTORE_FORMAT_UNSUPPORTED = "Exception during parse. HStore format unsupported: ";

	/**
	 * 
	 */
	public final static String HSTORE_CONSTRUCTOR_EXCEPTION = "Constructor could not be found/is private: ";
	
	/**
	 * 
	 */
	public final static String HSTORE_COULD_NOT_INSTANTIATE = "Could not instantiate object of type: ";
	
	/**
	 * 
	 */
	public final static String HSTORE_COULD_NOT_GET_VALUE = "Could not get field value of: ";
	
	/**
	 * 
	 */
	public final static String HSTORE_MUST_BE_A_COLLECTION = "Field must be a Collection: ";

	/**
	 * 
	 * @param message
	 */
	public HstoreParseException(String message) {
		this(message, null);
	}
	
	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public HstoreParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
