/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
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

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author lb
 *
 */
public class Hstore {

	public Map<Class<?>, HstoreParser<?>> parserMap = new HashMap<>();
	
	/**
	 * 
	 * @param clazz
	 * @throws HstoreParseException 
	 */
	public Hstore(Class<?>...clazzes) {
		for (Class<?> clazz : clazzes) {
			try {
				parserMap.put(clazz, new HstoreParser<>(clazz, this));
			} catch (HstoreParseException e) {
				e.printStackTrace();
			}	
		}
	}
	
	/**
	 * 
	 * @param clazz
	 * @throws HstoreParseException
	 */
	public void addClass(Class<?> clazz) throws HstoreParseException {
		parserMap.put(clazz, new HstoreParser<>(clazz, this));
	}

	/**
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> HstoreParser<T> getParser(Class<T> clazz) {
		return (HstoreParser<T>) parserMap.get(clazz);
	}

	/**
	 * 
	 * @return
	 */
	public Map<Class<?>, HstoreParser<?>> getParserMap() {
		return parserMap;
	}

	/**
	 * 
	 * @param object
	 * @param clazz
	 * @return
	 * @throws HstoreParseException 
	 * @throws JSONException 
	 */
	public <T> T fromString(String object, Class<T> clazz) throws HstoreParseException {
		Object o = toJson(object, clazz);
		
		try {
			if (o != null) {
				if (o instanceof JSONObject) {
					return fromJSON((JSONObject) o, clazz);
				}
				else {
					return fromJSON(((JSONArray) o).getJSONObject(0), clazz);
				}
			}
			else {
				throw new HstoreParseException(HstoreParseException.HSTORE_FORMAT_UNSUPPORTED + object);
			}
		}
		catch (JSONException e) {
			throw new HstoreParseException(HstoreParseException.HSTORE_FORMAT_UNSUPPORTED + object, e);
		}
	}
	
	/**
	 * 
	 * @param json
	 * @param clazz
	 * @return
	 * @throws HstoreParseException
	 */
	@SuppressWarnings("unchecked")
	public <T> T fromJSON(JSONObject json, Class<?> clazz) throws HstoreParseException {
		HstoreParser<?> parser = parserMap.get(clazz);
		if (parser != null) {
			return (T) parser.fromJson(json);
		}
		return null;
	}
	
	/**
	 * 
	 * @param json
	 * @param clazz
	 * @return
	 * @throws HstoreParseException
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] fromJSONArray(JSONArray json, Class<T> clazz) throws HstoreParseException {
		HstoreParser<?> parser = parserMap.get(clazz);
		T[] array = (T[]) Array.newInstance(clazz, json.length());
		try {
			if (parser != null) {
				for (int i = 0; i < json.length(); i++) {
					array[i] = ((T) parser.fromJson(json.getJSONObject(i)));
				}
				return array;
			}
		}
		catch (JSONException e) {
			throw new HstoreParseException(HstoreParseException.HSTORE_FORMAT_UNSUPPORTED + json, e);
		}
		
		return null;		
	}
	
	/**
	 * 
	 * @param object
	 * @param clazz
	 * @return either a json object or a json array
	 * @throws HstoreParseException 
	 */
	public <T> Object toJson(String object, Class<T> clazz) throws HstoreParseException {
		try {
			if (object == null || "null".equals(object)) {
				return null;
			}
			
			if (!object.startsWith("{") && !object.startsWith("[")) {
				return new JSONObject("{" + object.replace("=>", ":") + "}");	
			}
			else if (object.startsWith("{")) {
				return new JSONObject(object.replace("=>", ":"));
			}
			else if (object.startsWith("[")) {
				return new JSONArray(object.replace("=>", ":"));
			}
			else {
				return null;
			}			
		}
		catch (JSONException e) {
			throw new HstoreParseException(HstoreParseException.HSTORE_FORMAT_UNSUPPORTED + object, e);
		}
	}
}
