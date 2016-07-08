/*******************************************************************************
 * Copyright 2016 Specure GmbH
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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import org.json.JSONObject;

import at.alladin.rmbt.util.tools.TracerouteService.HopDetail;

public interface TracerouteService extends Callable<List<HopDetail>> {

	public static final class PingException extends IOException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PingException(String msg) {
			super(msg);
		}
	}
	
	public interface HopDetail {
		public JSONObject toJson();
	}
	
	public String getHost();

	public void setHost(String host);
	
	public void setResultListObject(List<HopDetail> resultList);

	public int getMaxHops();

	public void setMaxHops(int maxHops);
	
	public boolean hasMaxHopsExceeded();
}
