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
package at.alladin.rmbt.util.tools;

import java.util.concurrent.TimeUnit;

import org.json.JSONException;

public interface Collector<T, JSONTYPE> {
	
	public static class CollectorData<T> {
		T value;
		long timeStampNs;
		
		public CollectorData(T value) {
			this(value, System.nanoTime());
		}

		public CollectorData(T value, long timeStampNs) {
			this.value = value;
			this.timeStampNs = timeStampNs;
		}

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}

		public long getTimeStampNs() {
			return timeStampNs;
		}

		public void setTimeStampNs(long timeStampNs) {
			this.timeStampNs = timeStampNs;
		}

		@Override
		public String toString() {
			return "CollectorData [value=" + value + ", timeStampNs="
					+ timeStampNs + "]";
		}
	}
	
	/**
	 * 
	 * @param delta time diff since last call
	 * @return
	 */
	public CollectorData<T> update(float delta, TimeUnit timeUnit);
	
	/**
	 * returns the time in nanoseconds between two updates of this collector
	 * @return
	 */
	public long getNanoPause();
	
	/**
	 * returns a Json object representing the result of the collector
	 * @param clean if set to true the current results are wiped out
	 * @return
	 * @throws JSONException 
	 */
	public JSONTYPE getJsonResult(boolean clean) throws JSONException;
	
	/**
	 * returns a Json object representing the result of the collector.<br>
	 * all times are beeing set to relative times by using the relTimeStamp
	 * @param clean
	 * @param relTimeStamp
	 * @param timeUnit
	 * @return
	 * @throws JSONException
	 */
	public JSONTYPE getJsonResult(boolean clean, long relTimeStamp, TimeUnit timeUnit) throws JSONException;
	
	/**
	 * returns the json key string for this collector
	 * @return
	 */
	public String getJsonKey();
	
}
