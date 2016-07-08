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
package at.alladin.rmbt.qos.testserver.plugin.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import at.alladin.rmbt.qos.testserver.TestServer;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole.ErrorReport;

/**
 * 
 * @author lb
 *
 */
public class StatusResource extends ServerResource {

	@Get
	public String request() throws JSONException {
		JSONObject json = new JSONObject();
		
		json.put("starttime", TestServer.serverPreferences.getStartTimestamp());
		json.put("version", TestServer.TEST_SERVER_VERSION_MAJOR + "." + TestServer.TEST_SERVER_VERSION_MINOR + "." + TestServer.TEST_SERVER_VERSION_PATCH);
		
		if (TestServerConsole.errorReportMap.size() > 0) {
			setStatus(Status.SERVER_ERROR_INTERNAL);
			
			JSONArray errors = new JSONArray();
			for (ErrorReport er : TestServerConsole.errorReportMap.values()) {
				errors.put(er.toJson());
			}
			
			json.put("errors", errors);
		}
		
		return json.toString();
	}
}
