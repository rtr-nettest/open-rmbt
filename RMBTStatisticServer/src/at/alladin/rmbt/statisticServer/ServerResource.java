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
package at.alladin.rmbt.statisticServer;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.naming.NamingException;

import org.joda.time.DateTime;
import org.restlet.data.Reference;
import org.restlet.engine.header.Header;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import at.alladin.rmbt.db.DbConnection;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.util.capability.Capabilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ServerResource extends org.restlet.resource.ServerResource
{
    protected Connection conn;
    protected ResourceBundle labels;
    protected ResourceBundle settings;
    protected Capabilities capabilities = new Capabilities();
    
    public static class MyDateTimeAdapter implements JsonSerializer<DateTime>, JsonDeserializer<DateTime>
    {
        public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context)
        {
            return new JsonPrimitive(src.toString());
        }
        
        public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException
        {
            return new DateTime(json.getAsJsonPrimitive().getAsString());
        }
    }
    
    public static Gson getGson(boolean prettyPrint)
    {
        GsonBuilder gb = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new MyDateTimeAdapter());
        if (prettyPrint)
            gb = gb.setPrettyPrinting();
        return gb.create();
    }
    
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
        try
        {
            if (conn != null)
                conn.close();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void addAllowOrigin()
    {
        Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
        if (responseHeaders == null)
        {
            responseHeaders = new Series<>(Header.class);
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
    
    @SuppressWarnings("unchecked")
    public String getIP()
    {
        final Series<Header> headers = (Series<Header>) getRequest().getAttributes().get("org.restlet.http.headers");
        final String realIp = headers.getFirstValue("X-Real-IP", true);
        if (realIp != null)
            return realIp;
        else
            return getRequest().getClientInfo().getAddress();
    }
    
    @SuppressWarnings("unchecked")
    public Reference getURL()
    {
        final Series<Header> headers = (Series<Header>) getRequest().getAttributes().get("org.restlet.http.headers");
        final String realURL = headers.getFirstValue("X-Real-URL", true);
        if (realURL != null)
            return new Reference(realURL);
        else
            return getRequest().getOriginalRef();
    }
    
    protected String getSetting(String key, String lang)
    {
        if (conn == null)
            return null;
        

        try (final PreparedStatement st = conn.prepareStatement(
                        "SELECT value"
                        + " FROM settings"
                        + " WHERE key=? AND (lang IS NULL OR lang = ?)"
                        + " ORDER BY lang NULLS LAST LIMIT 1");)
        {
                    
            st.setString(1, key);
            st.setString(2, lang);
            
            try (final ResultSet rs = st.executeQuery();)
            {
            
                if (rs != null && rs.next())
                    return rs.getString("value");
            }
            return null;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
