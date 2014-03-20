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
package at.alladin.rmbt.controlServer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Parameter;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.util.Series;

import at.alladin.rmbt.db.Client;
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.ResourceManager;

import com.google.common.base.Strings;

public class SettingsResource extends ServerResource
{
    
    /**
     * 
     * @param entity
     * @return
     */
    @Post("json")
    public String request(final String entity)
    {
        addAllowOrigin();
        
        JSONObject request = null;
        
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;
        
        System.out.println(MessageFormat.format(labels.getString("NEW_SETTINGS_REQUEST"), getIP()));
        
        if (entity != null && !entity.isEmpty())
            // try parse the string to a JSON object
            try
            {
                request = new JSONObject(entity);
                
                String lang = request.optString("language");
                
                // Load Language Files for Client
                final List<String> langs = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));
                
                if (langs.contains(lang))
                {
                    errorList.setLanguage(lang);
                    labels = ResourceManager.getSysMsgBundle(new Locale(lang));
                }
                else
                    lang = settings.getString("RMBT_DEFAULT_LANGUAGE");
                
                // System.out.println(request.toString(4));
                
                if (conn != null)
                {
                    
                    final Client client = new Client(conn);
                    int typeId = 0;
                    
                    if (request.optString("type").length() > 0)
                    {
                        typeId = client.getTypeId(request.getString("type"));
                        if (client.hasError())
                            errorList.addError(client.getError());
                    }
                    
                    final List<String> clientNames = Arrays.asList(settings.getString("RMBT_CLIENT_NAME")
                            .split(",\\s*"));
                    
                    final JSONArray settingsList = new JSONArray();
                    final JSONObject jsonItem = new JSONObject();
                    
                    if (clientNames.contains(request.optString("name")) && typeId > 0)
                    {
                        
                        // String clientName = request.getString("name");
                        // String clientVersionCode =
                        // request.getString("version_name");
                        // String clientVersionName =
                        // request.optString("version_code", "");
                        
                        UUID uuid = null;
                        long clientUid = 0;
                        
                        final String uuidString = request.optString("uuid", "");
                        try
                        {
                            if (! Strings.isNullOrEmpty(uuidString))
                                uuid = UUID.fromString(uuidString);
                        }
                        catch (IllegalArgumentException e) // not a valid uuid
                        {
                        }
                        
                        if (uuid != null && errorList.getLength() == 0)
                        {
                            clientUid = client.getClientByUuid(uuid);
                            if (client.hasError())
                                errorList.addError(client.getError());
                        }
                        
                        boolean tcAccepted = request.optInt("terms_and_conditions_accepted_version", 0) > 0; // accept any version for now
                        if (! tcAccepted) // allow old non-version parameter
                            tcAccepted = request.optBoolean("terms_and_conditions_accepted", false);
                        {
                            if (tcAccepted && (uuid == null || clientUid == 0))
                            {
                                
                                final Timestamp tstamp = java.sql.Timestamp.valueOf(new Timestamp(System
                                        .currentTimeMillis()).toString());
                                
                                final Calendar timeWithZone = Helperfunctions.getTimeWithTimeZone(Helperfunctions
                                        .getTimezoneId());
                                
                                client.setTimeZone(timeWithZone);
                                client.setTime(tstamp);
                                client.setClient_type_id(typeId);
                                client.setTcAccepted(tcAccepted);
                                
                                uuid = client.storeClient();
                                
                                if (client.hasError())
                                    errorList.addError(client.getError());
                                else
                                    jsonItem.put("uuid", uuid.toString());
                            }
                            
                            if (client.getUid() > 0)
                            {
                                /* map server */
                                
                                final Series<Parameter> ctxParams = getContext().getParameters();
                                final String host = ctxParams.getFirstValue("RMBT_MAP_HOST");
                                final String sslStr = ctxParams.getFirstValue("RMBT_MAP_SSL");
                                final String portStr = ctxParams.getFirstValue("RMBT_MAP_PORT");
                                if (host != null && sslStr != null && portStr != null)
                                {
                                    JSONObject mapServer = new JSONObject();
                                    mapServer.put("host", host);
                                    mapServer.put("port", Integer.parseInt(portStr));
                                    mapServer.put("ssl", Boolean.parseBoolean(sslStr));
                                    jsonItem.put("map_server", mapServer);
                                }
                                
                                // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                // HISTORY / FILTER
                                
                                final JSONObject subItem = new JSONObject();
                                
                                final JSONArray netList = new JSONArray();
                                
                                try
                                {
                                    
                                    // deviceList:
                                    
                                    subItem.put("devices", getSyncGroupDeviceList(errorList, client));
                                    
                                    // network_type:
                                    
                                    final PreparedStatement st = conn.prepareStatement(
                                    "SELECT DISTINCT group_name" +
                                    " FROM test t" +
                                    " JOIN network_type nt ON t.network_type=nt.uid" +
                                    " WHERE t.deleted = false AND t.status = 'FINISHED' "+
                                    " AND (t.client_id IN (SELECT ? UNION SELECT uid FROM client WHERE sync_group_id = ? ))" +
                                    " AND group_name IS NOT NULL ORDER BY group_name;");

                                    st.setLong(1, client.getUid());
                                    st.setInt(2, client.getSync_group_id());
                                    
                                    final ResultSet rs = st.executeQuery();
                                    
                                    if (rs != null)
                                        while (rs.next())
                                            // netList.put(Helperfunctions.getNetworkTypeName(rs.getInt("network_type")));
                                            netList.put(rs.getString("group_name"));
                                    else
                                        errorList.addError("ERROR_DB_GET_SETTING_HISTORY_NETWORKS_SQL");
                                    
                                    rs.close();
                                    st.close();
                                    
                                    subItem.put("networks", netList);
                                    
                                }
                                catch (final SQLException e)
                                {
                                    e.printStackTrace();
                                    errorList.addError("ERROR_DB_GET_SETTING_HISTORY_SQL");
                                    // errorList.addError("ERROR_DB_GET_CLIENT_SQL");
                                }
                                
                                if (errorList.getLength() == 0)
                                    jsonItem.put("history", subItem);
                                
                            }
                            else
                                errorList.addError("ERROR_CLIENT_UUID");
                        }
                        
                        //also put there: basis-urls for all services
                        final JSONObject jsonItemURLs = new JSONObject();
                        jsonItemURLs.put("open_data_prefix", getSetting("url_open_data_prefix", lang));
                        jsonItemURLs.put("statistics", getSetting("url_statistics", lang));
                        
                        jsonItem.put("urls",jsonItemURLs);
                   
                        
                        
                        settingsList.put(jsonItem);
                        
                        answer.put("settings", settingsList);
                        
                    }
                    else
                        errorList.addError("ERROR_CLIENT_VERSION");
                    
                }
                else
                    errorList.addError("ERROR_DB_CONNECTION");
                
            }
            catch (final JSONException e)
            {
                errorList.addError("ERROR_REQUEST_JSON");
                System.out.println("Error parsing JSDON Data " + e.toString());
            }
        else
            errorList.addErrorString("Expected request is missing.");
        
        try
        {
            answer.putOpt("error", errorList.getList());
        }
        catch (final JSONException e)
        {
            System.out.println("Error saving ErrorList: " + e.toString());
        }
        
        answerString = answer.toString();
        
//        try
//        {
//            System.out.println(answer.toString(4));
//        }
//        catch (final JSONException e)
//        {
//            e.printStackTrace();
//        }
        
        return answerString;
    }
    
    /**
     * 
     * @param entity
     * @return
     */
    @Get("json")
    public String retrieve(final String entity)
    {
        return request(entity);
    }
    
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private JSONArray getSyncGroupDeviceList(final ErrorList errorList, final Client client) throws SQLException
    {
        
        JSONArray ownDeviceList = null;
        
        final PreparedStatement st = conn
                .prepareStatement("SELECT DISTINCT COALESCE(adm.fullname, t.model) model"
                        + " FROM test t"
                        + " LEFT JOIN device_map adm ON adm.codename=t.model"
                        + " WHERE (t.client_id = ? OR t.client_id IN (SELECT uid FROM client WHERE sync_group_id = ?)) AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED' ORDER BY model ASC");
        
        st.setLong(1, client.getUid());
        st.setInt(2, client.getSync_group_id());
        
//        System.out.println(st.toString());
        
        final ResultSet rs = st.executeQuery();
        if (rs != null)
        {
            
            ownDeviceList = new JSONArray();
            
            while (rs.next())
            {
                final String model = rs.getString("model");
                if (model == null || model.isEmpty())
                    ownDeviceList.put("Unknown Device");
                else
                    ownDeviceList.put(model);
            }
        }
        else
            errorList.addError("ERROR_DB_GET_SETTING_HISTORY_DEVICES_SQL");
        
        rs.close();
        st.close();
        
        return ownDeviceList;
    }
    
}
