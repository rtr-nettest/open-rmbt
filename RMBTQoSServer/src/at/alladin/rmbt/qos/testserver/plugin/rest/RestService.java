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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.util.Series;

import at.alladin.rmbt.qos.testserver.ServerPreferences;
import at.alladin.rmbt.qos.testserver.ServerPreferences.ServiceSetting;
import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.TestServer;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole;

/**
 * 
 * @author lb
 *
 */
public class RestService extends ServiceSetting {
	public static final String PARAM_REST = "server.service.rest";
	public static final String PARAM_REST_PORT = "server.service.rest.port";
	public static final String PARAM_REST_SSL = "server.service.rest.ssl";
	public final static String QOS_KEY_FILE_ABSOLUTE = "src/at/alladin/rmbt/qos/testserver/" + TestServer.QOS_KEY_FILE;
	
	AtomicBoolean isRunning = new AtomicBoolean(false);
	
	int port = 0;
	
	boolean isSsl = false;
	
	final ServerPreferences serverPreferences;
	
	public RestService(Properties prop, ServerPreferences serverPreferences) {
		this(false, serverPreferences);
		setParam(prop);
	}
	
	public RestService(boolean isEnabled, ServerPreferences serverPreferences) {
		super("REST SERVICE", isEnabled);
		this.serverPreferences = serverPreferences;
	}

	@Override
	public void start() throws UnknownHostException {
		if (isEnabled) {
			final boolean isRunning = this.isRunning.getAndSet(true);
			if (!isRunning) {
				if (isEnabled && port <= 0) {
					this.isEnabled = false;
					TestServerConsole.log("Could not start RestService. Parameter missing: 'server.service.rest.port'", 1, TestServerServiceEnum.TEST_SERVER);
				}
				 
			    Component component = new Component();  

			    Server s = component.getServers().add(isSsl ? Protocol.HTTPS : Protocol.HTTP, InetAddress.getLocalHost().getHostAddress(), port);
			    
			    if (isSsl) {
				    Series<Parameter> parameters = s.getContext().getParameters();				    
				    parameters.add("keystorePath", QOS_KEY_FILE_ABSOLUTE);
				    parameters.add("keystorePassword", TestServer.QOS_KEY_PASSWORD);
				    parameters.add("keyPassword", TestServer.QOS_KEY_PASSWORD);
				    parameters.add("keystoreType", TestServer.QOS_KEY_TYPE);
			    }

			    component.getDefaultHost().attach("", new RestletApplication());
			    
			    try {
					component.start();
					TestServerConsole.log("[" + getName() + "] started: " + toString(), 1, TestServerServiceEnum.TEST_SERVER);
				} catch (Exception e) {
					TestServerConsole.error(getName(), e, 0, TestServerServiceEnum.TEST_SERVER);
				}  
			}
		}
	}

	@Override
	public void stop() {
		if (isEnabled && isRunning.get()) {
			
			isRunning.set(false);
		}
	}

	@Override
	public void setParam(Properties properties) {
   		String param = properties.getProperty(PARAM_REST);
   		if (param!=null) {
   			setEnabled(Boolean.parseBoolean(param.trim()));
   		}

   		param = properties.getProperty(PARAM_REST_SSL);
   		if (param!=null) {
   			isSsl = Boolean.parseBoolean(param.trim());
   		}

   		param = properties.getProperty(PARAM_REST_PORT);
   		if (param!=null) {
   			port = Integer.parseInt(param);
   		}
	}

	@Override
	public String toString() {
		return "RestService [isRunning=" + isRunning + ", port=" + port
				+ ", isSsl=" + isSsl + "]";
	}
	
	/**
	 * 
	 * @param json
	 * @param error
	 * @return
	 * @throws JSONException 
	 */
	public static JSONObject addError(JSONObject json, String error) throws JSONException {
		JSONArray errorArray = json.optJSONArray("errors");
		if (errorArray == null) {
			errorArray = new JSONArray();
		}
		errorArray.put(error);
		json.put("errors", errorArray);
		return json;
	}
}
