/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
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
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import at.alladin.rmbt.db.Client;
import at.alladin.rmbt.db.QoSTestTypeDesc;
import at.alladin.rmbt.db.dao.QoSTestTypeDescDao;
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.RevisionHelper;

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
    {   long startTime = System.currentTimeMillis();
        addAllowOrigin();
        
        JSONObject request = null;
        
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;
        
        final String clientIpRaw = getIP();
        System.out.println(MessageFormat.format(labels.getString("NEW_SETTINGS_REQUEST"), clientIpRaw));
        
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

                    	UUID uuid = null;
                    	long clientUid = 0;
                    	boolean existingClient = false;

                    	final int tcAcceptedVersion = request.optInt("terms_and_conditions_accepted_version", 0);
                    	boolean tcAccepted = tcAcceptedVersion > 0;
                    	if (! tcAccepted) // for clients backwards compatibility
                    		tcAccepted = request.optBoolean("terms_and_conditions_accepted", false);

                    	final String uuidString = request.optString("uuid", "");
                    	try
                    	{
                    		if (! Strings.isNullOrEmpty(uuidString))
                    			uuid = UUID.fromString(uuidString);
                    	}
                    	catch (IllegalArgumentException e) // uuid invalid
                    	{
                    	}

                    	if (uuid != null && errorList.getLength() == 0)
                    	{
                    		clientUid = client.getClientByUuid(uuid);
                    		if (!client.hasError() && clientUid > 0) {
                    			existingClient = true;

                    			if (client.getTcAcceptedVersion()< tcAcceptedVersion) {
                    				client.updateTcAcceptedVersion(uuid,tcAcceptedVersion);
                    			}
                    		}
                    	}

                    	if (tcAccepted && !existingClient)
                    	{
                    		final Timestamp tstamp = java.sql.Timestamp.valueOf(new Timestamp(System
                    				.currentTimeMillis()).toString());

                    		final Calendar timeWithZone = Helperfunctions.getTimeWithTimeZone(Helperfunctions
                    				.getTimezoneId());

                    		client.setTimeZone(timeWithZone);
                    		client.setTime(tstamp);
                    		client.setClient_type_id(typeId);
                    		client.setTcAccepted(tcAccepted);
                    		client.setTcAcceptedVersion(tcAcceptedVersion);
                    		uuid = client.storeClient(uuid);

                    		if (client.hasError())
                    			errorList.addError(client.getError());
                    		else {
                    			jsonItem.put("uuid", uuid.toString());
                    		}
                    	}

                            if (client.getUid() > 0)
                            {
                            	client.updateLastSeen(uuid);
                            	
                            	JSONObject mapServer = new JSONObject();
                            	//for clients backwards compatibility, shall be obsoleted by url_map_server
                            	mapServer.put("host", getSetting("host_map_server", lang));
                            	try
                            	{
                            		mapServer.put("port", Integer.parseInt(getSetting("port_map_server", lang)));
                            	}
                            	catch (NumberFormatException e)
                            	{
                            		mapServer.put("port",443);
                            	}
                            	mapServer.put("ssl", Boolean.parseBoolean(getSetting("ssl_map_server", lang)));
                            	jsonItem.put("map_server", mapServer);

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
                        
                        
                        //also put there: basis-urls for all services
                        final JSONObject jsonItemURLs = new JSONObject();
                        jsonItemURLs.put("open_data_prefix", getSetting("url_open_data_prefix", lang));
                        jsonItemURLs.put("statistics", getSetting("url_statistics", lang));
                        jsonItemURLs.put("control_ipv4_only", getSetting("control_ipv4_only", lang));
                        jsonItemURLs.put("control_ipv6_only", getSetting("control_ipv6_only", lang));
                        jsonItemURLs.put("url_ipv4_check", getSetting("url_ipv4_check", lang));
                        jsonItemURLs.put("url_ipv6_check", getSetting("url_ipv6_check", lang));
                        jsonItemURLs.put("url_map_server", getSetting("url_map_server", lang));
                    
                        jsonItem.put("urls",jsonItemURLs);
                        
                        final JSONObject jsonControlServerVersion = new JSONObject();
                        jsonControlServerVersion.put("control_server_version",  RevisionHelper.getVerboseRevision());
                        jsonItem.put("versions", jsonControlServerVersion);
                        
                        
                        /// servers
                        
                        final Boolean userServerSelection = request.optBoolean("user_server_selection"); 
                        // It returns false if there is no such key, or if the value is not Boolean.TRUE or the String "true". 
                        if (userServerSelection)
                        {
                        	jsonItem.put("servers", getServers());
                        	jsonItem.put("servers_ws", getServersWs());
                        	jsonItem.put("servers_qos", getServersQos());
                        	
                        }
                        
                        /// qos
                        
                        try {
                            final Locale locale = new Locale(lang);
                            final QoSTestTypeDescDao testTypeDao = new QoSTestTypeDescDao(conn, locale);
                            final JSONArray testTypeDescArray = new JSONArray();
                            for (QoSTestTypeDesc desc : testTypeDao.getAll()) {
                            	if (desc.getTestType()!=null) {
                            		//in case a qos module is not included but the entry hasn't beed removed from the db
	                            	JSONObject json = new JSONObject();
	                        		json.put("test_type", desc.getTestType().name());
	                        		json.put("name", desc.getName());
	                            	testTypeDescArray.put(json);
                            	}
                            }
                            jsonItem.put("qostesttype_desc", testTypeDescArray);
                        } catch (SQLException e) {
                        	errorList.addError("ERROR_DB_CONNECTION");
						}

                        /// android permissions
                        final JSONArray androidPermissionStatus = request.optJSONArray("android_permission_status");
                        if (androidPermissionStatus != null)
                        {
                            final JSONArray requestAndroidPermissions = new JSONArray();
                            
                            // for now request all permissions every time. see #1248 / comment:6
                            addRequestPermission(requestAndroidPermissions, "android.permission.ACCESS_FINE_LOCATION", true);
                            addRequestPermission(requestAndroidPermissions, "android.permission.ACCESS_COARSE_LOCATION", true);
                            addRequestPermission(requestAndroidPermissions, "android.permission.READ_PHONE_STATE", true);
                            
                            jsonItem.put("request_android_permissions", requestAndroidPermissions);
                        }
                        
                        
                        settingsList.put(jsonItem);
                        
                        answer.put("settings", settingsList);
                        
                        // debug: print settings request (JSON)
                        // System.out.println(request);
                        
                        // debug: print settings response (JSON)
                        // System.out.println(settingsList.toString(3));
                        
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
        
        
        answerString = answer.toString();
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println(MessageFormat.format(labels.getString("NEW_SETTINGS_REQUEST_SUCCESS"), clientIpRaw, Long.toString(elapsedTime)));
        

        
        
        return answerString;
    }
    
 // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private static void addRequestPermission(JSONArray requestAndroidPermissions, String permission, boolean request) throws JSONException
    {
        final JSONObject requestObj = new JSONObject();
        requestObj.put("permission", permission);
        requestObj.put("request", request);
        requestAndroidPermissions.put(requestObj);
    }

    private JSONArray getServers() throws JSONException
    {
        final String sql = "SELECT * FROM test_server WHERE active AND selectable AND server_type = 'RMBT'";
        final JSONArray result = new JSONArray();
        try (PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                final JSONObject obj = new JSONObject();
                
                obj.put("name", rs.getString("name"));
                obj.put("uuid", rs.getString("uuid"));
                
                result.put(obj);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return result;
    }
    
// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private JSONArray getServersWs() throws JSONException
    {
        final String sql = "SELECT * FROM test_server WHERE selectable AND server_type = 'RMBTws'";
        final JSONArray result = new JSONArray();
        try (PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                final JSONObject obj = new JSONObject();
                
                obj.put("name", rs.getString("name"));
                obj.put("uuid", rs.getString("uuid"));
                
                result.put(obj);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return result;
    }
// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private JSONArray getServersQos() throws JSONException
    {
        final String sql = "SELECT * FROM test_server WHERE selectable AND server_type = 'QoS'";
        final JSONArray result = new JSONArray();
        try (PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                final JSONObject obj = new JSONObject();
                
                obj.put("name", rs.getString("name"));
                obj.put("uuid", rs.getString("uuid"));
                
                result.put(obj);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return result;
    }
    
 // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
                        + " WHERE (t.client_id IN (SELECT ? UNION SELECT uid FROM client WHERE sync_group_id = ? )) AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED' ORDER BY model ASC");
        
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
