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

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

/**
 * 
 * @author lb
 *
 */
public class RestletApplication extends Application {

	@Override
	public Restlet createInboundRoot() {
		
        Router router = new Router(getContext());
        router.attach("/", StatusResource.class);
        router.attach("/info/{type}", InfoResource.class);
        router.attach("/info/", InfoResource.class);
        router.attachDefault(ErrorHandlerResource.class);
        return router;
	}
	
	public final static class ErrorHandlerResource extends ServerResource {
		
		@Get
		public String request() throws JSONException {
			JSONObject json = new JSONObject();
			RestService.addError(json, "resource not found");
			return json.toString();
		}
	}
}
