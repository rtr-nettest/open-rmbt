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
package at.alladin.rmbt.mapServer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.naming.NamingException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.engine.header.Header;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import com.google.gson.Gson;

import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.util.capability.Capabilities;

public class ServerResource extends org.restlet.resource.ServerResource
{
    protected Connection conn;
    protected ResourceBundle labels;
    protected ResourceBundle settings;
    protected Capabilities capabilities = new Capabilities();
    
    @Override
    public void doInit() throws ResourceException
    {
        super.doInit();
        
        settings = ResourceManager.getCfgBundle();
        // Set default Language for System
        Locale.setDefault(new Locale(settings.getString("RMBT_DEFAULT_LANGUAGE")));
        labels = ResourceManager.getSysMsgBundle();
        
        try {
	        if (getQuery().getNames().contains("capabilities")) {
	        	capabilities = new Gson().fromJson(getQuery().getValues("capabilities"), Capabilities.class);
	        }
        } catch (final Exception e) {
        	e.printStackTrace();
        }
        
        // Get DB-Connection
        try
        {
            conn = DbConnection.getConnection();
        }
        catch (final NamingException e)
        {
            e.printStackTrace();
        }
        catch (final SQLException e)
        {
            System.out.println(labels.getString("ERROR_DB_CONNECTION_FAILED"));
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doRelease() throws ResourceException
    {
        super.doRelease();
        try
        {
            conn.close();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    public void readCapabilities(final JSONObject request) throws JSONException {
    	if (request != null) {
    		if (request.has("capabilities")) {
        		capabilities = new Gson().fromJson(request.get("capabilities").toString(), Capabilities.class);	
    		}
    	}
    }
    
    @SuppressWarnings("unchecked")
    protected void addAllowOrigin()
    {
        Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
        if (responseHeaders == null)
        {
            responseHeaders = new Series<Header>(Header.class);
            getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
        }
        responseHeaders.add("Access-Control-Allow-Origin", "*");
        responseHeaders.add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        responseHeaders.add("Access-Control-Allow-Headers", "Content-Type");
        responseHeaders.add("Access-Control-Allow-Credentials", "false");
        responseHeaders.add("Access-Control-Max-Age", "60");
    }
    
    @Options
    public void doOptions(final Representation entity)
    {
        addAllowOrigin();
    }
}
