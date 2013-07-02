/*******************************************************************************
 * Copyright 2013 alladin-IT OG
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import com.google.common.base.Strings;

import at.alladin.rmbt.db.Client;
import at.alladin.rmbt.shared.Helperfunctions;

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
                    labels = (PropertyResourceBundle) ResourceBundle.getBundle("at.alladin.rmbt.res.SystemMessages",
                            new Locale(lang));
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
                        
                        final boolean tcAccepted = request.optBoolean("terms_and_conditions_accepted", false);
                        
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
                                    // "SELECT DISTINCT network_type FROM test WHERE (client_id = ? OR client_id IN (SELECT uid FROM client WHERE sync_group_id = ?)) AND status = 'FINISHED' ORDER BY network_type ASC"
                                            "SELECT DISTINCT group_name"
                                                    + " FROM test t"
                                                    + " JOIN network_type nt ON t.network_type=nt.uid"
                                                    + " WHERE t.deleted = false AND t.status = 'FINISHED'"
                                                    + " AND (t.client_id = ? OR t.client_id IN (SELECT uid FROM client WHERE sync_group_id = ?))"
                                                    + " AND group_name IS NOT NULL" + " ORDER BY group_name");
                                    
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
                                
                                // //////////////////////////////////////////////////////////////////////////////////////////////
                                // add map filter options to json response
                            }
                            else
                                errorList.addError("ERROR_CLIENT_UUID");
                            
                            jsonItem.put("mapfilter", getMapFilterObject(errorList));
                            
                        }
                        
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
                .prepareStatement("SELECT DISTINCT model FROM test WHERE (client_id = ? OR client_id IN (SELECT uid FROM client WHERE sync_group_id = ?)) AND deleted = false AND status = 'FINISHED' ORDER BY model ASC");
        
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
    
    private Map<String, String> getDeviceList(final ErrorList errorList) throws SQLException
    {
        
        final Map<String, String> result = new LinkedHashMap<String, String>();
        
        final PreparedStatement st = conn
                .prepareStatement("SELECT t.model model_key, COALESCE(adm.fullname, t.model) model_value FROM test t"
                        + " LEFT JOIN android_device_map adm ON adm.codename=t.model"
                        + " WHERE t.deleted = false AND t.status = 'FINISHED' AND t.model IS NOT NULL GROUP BY model_key, model_value ORDER BY model_value ASC");
        
        final ResultSet rs = st.executeQuery();
        if (rs != null)
            while (rs.next())
            {
                final String key = rs.getString("model_key");
                final String value = rs.getString("model_value");
                if (key != null && !key.isEmpty() && value != null && !value.isEmpty())
                    result.put(key, value);
            }
        else
            errorList.addError("ERROR_DB_GET_SETTING_HISTORY_DEVICES_SQL");
        
        rs.close();
        st.close();
        
        return result;
    }
    
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // MAP CHOOSE / FILTER
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * 
     * @return
     * @throws JSONException
     */
    private JSONObject getMapFilterObject(final ErrorList errorList) throws JSONException
    {
        
        final JSONObject mapFilterObject = new JSONObject();
        
        mapFilterObject.put("mapTypes", getMapTypeList());
        mapFilterObject.put("mapFilters", getMapFilterList(errorList));
        
        return mapFilterObject;
    }
    
    /**
     * 
     * @return
     * @throws JSONException
     */
    private JSONArray getMapTypeList() throws JSONException
    {
        
        final JSONArray mapTypeList = new JSONArray();
        
        final JSONArray mapType1List = new JSONArray();
        
        final JSONObject mapType1Property1List = new JSONObject();
        
        // Map<String, MapOption> mapOptions = MapOptions.getMapOptions();
        
        mapType1Property1List.put("title", labels.getString("RESULT_DOWNLOAD"));
        mapType1Property1List.put("summary", labels.getString("MAP_DOWNLOAD_SUMMARY"));
        mapType1Property1List.put("map_options", "mobile/download");
        /*
         * mapType1Property1List.put("map_values", "mobile/download");
         * mapType1Property1List.put("map_colors", "mobile/download");
         * mapOptions.get("mobile/download").colors;
         */
        
        mapType1List.put(mapType1Property1List);
        
        final JSONObject mapType1Property2List = new JSONObject();
        
        mapType1Property2List.put("title", labels.getString("RESULT_UPLOAD"));
        mapType1Property2List.put("summary", labels.getString("MAP_UPLOAD_SUMMARY"));
        mapType1Property2List.put("map_options", "mobile/upload");
        
        mapType1List.put(mapType1Property2List);
        
        final JSONObject mapType1Property3List = new JSONObject();
        
        mapType1Property3List.put("title", labels.getString("RESULT_PING"));
        mapType1Property3List.put("summary", labels.getString("MAP_PING_SUMMARY"));
        mapType1Property3List.put("map_options", "mobile/ping");
        
        mapType1List.put(mapType1Property3List);
        
        final JSONObject mapType1Property4List = new JSONObject();
        
        mapType1Property4List.put("title", labels.getString("RESULT_SIGNAL"));
        mapType1Property4List.put("summary", labels.getString("MAP_SIGNAL_SUMMARY"));
        mapType1Property4List.put("map_options", "mobile/signal");
        
        mapType1List.put(mapType1Property4List);
        
        // /
        
        final JSONArray mapType2List = new JSONArray();
        
        final JSONObject mapType2Property1List = new JSONObject();
        
        mapType2Property1List.put("title", labels.getString("RESULT_DOWNLOAD"));
        mapType2Property1List.put("summary", labels.getString("MAP_DOWNLOAD_SUMMARY"));
        mapType2Property1List.put("map_options", "wifi/download");
        
        mapType2List.put(mapType2Property1List);
        
        final JSONObject mapType2Property2List = new JSONObject();
        
        mapType2Property2List.put("title", labels.getString("RESULT_UPLOAD"));
        mapType2Property2List.put("summary", labels.getString("MAP_UPLOAD_SUMMARY"));
        mapType2Property2List.put("map_options", "wifi/upload");
        
        mapType2List.put(mapType2Property2List);
        
        final JSONObject mapType2Property3List = new JSONObject();
        
        mapType2Property3List.put("title", labels.getString("RESULT_PING"));
        mapType2Property3List.put("summary", labels.getString("MAP_PING_SUMMARY"));
        mapType2Property3List.put("map_options", "wifi/ping");
        
        mapType2List.put(mapType2Property3List);
        
        final JSONObject mapType2Property4List = new JSONObject();
        
        mapType2Property4List.put("title", labels.getString("RESULT_SIGNAL"));
        mapType2Property4List.put("summary", labels.getString("MAP_SIGNAL_SUMMARY"));
        mapType2Property4List.put("map_options", "wifi/signal");
        
        mapType2List.put(mapType2Property4List);
        
        // /
        
        final JSONArray mapType3List = new JSONArray();
        
        final JSONObject mapType3Property1List = new JSONObject();
        
        mapType3Property1List.put("title", labels.getString("RESULT_DOWNLOAD"));
        mapType3Property1List.put("summary", labels.getString("MAP_DOWNLOAD_SUMMARY"));
        mapType3Property1List.put("map_options", "browser/download");
        
        mapType3List.put(mapType3Property1List);
        
        final JSONObject mapType3Property2List = new JSONObject();
        
        mapType3Property2List.put("title", labels.getString("RESULT_UPLOAD"));
        mapType3Property2List.put("summary", labels.getString("MAP_UPLOAD_SUMMARY"));
        mapType3Property2List.put("map_options", "browser/upload");
        
        mapType3List.put(mapType3Property2List);
        
        final JSONObject mapType3Property3List = new JSONObject();
        
        mapType3Property3List.put("title", labels.getString("RESULT_PING"));
        mapType3Property3List.put("summary", labels.getString("MAP_PING_SUMMARY"));
        mapType3Property3List.put("map_options", "browser/ping");
        
        mapType3List.put(mapType3Property3List);
        
        // /
        
        final JSONObject mobileObj = new JSONObject();
        
        mobileObj.put("title", labels.getString("MAP_MOBILE"));
        mobileObj.put("options", mapType1List);
        
        mapTypeList.put(mobileObj);
        
        // /
        
        final JSONObject wifiObj = new JSONObject();
        
        wifiObj.put("title", labels.getString("MAP_WIFI"));
        wifiObj.put("options", mapType2List);
        
        mapTypeList.put(wifiObj);
        
        // /
        
        final JSONObject browserObj = new JSONObject();
        
        browserObj.put("title", labels.getString("MAP_BROWSER_TEST"));
        browserObj.put("options", mapType3List);
        
        mapTypeList.put(browserObj);
        
        return mapTypeList;
    }
    
    /**
     * 
     * @return
     * @throws JSONException
     */
    private JSONArray getMapFilterList(final ErrorList errorList) throws JSONException
    {
        final JSONArray mapFilterCarrierList = new JSONArray();
        
        for (int i = 0; labels.containsKey(String.format("MAP_FILTER_CARRIER_%d_TITLE", i)); i++)
        {
            final JSONObject mapFilterAllCarrierObject = new JSONObject();
            mapFilterAllCarrierObject.put("title", labels.getString(String.format("MAP_FILTER_CARRIER_%d_TITLE", i)));
            mapFilterAllCarrierObject.put("summary",
                    labels.getString(String.format("MAP_FILTER_CARRIER_%d_SUMMARY", i)));
            mapFilterAllCarrierObject.put("operator",
                    labels.getString(String.format("MAP_FILTER_CARRIER_%d_FILTER", i)));
            mapFilterCarrierList.put(mapFilterAllCarrierObject);
        }
        
        // /
        
        final double[] statisticalMethodArray = { 0.8, 0.5, 0.2 };
        
        final JSONArray mapFilterStatisticalMethodList = new JSONArray();
        
        for (int stat = 1; stat <= statisticalMethodArray.length; stat++)
        {
            
            final JSONObject mapFilterDayTimeObject = new JSONObject();
            
            mapFilterDayTimeObject.put("title", labels.getString("MAP_FILTER_STATISTICAL_METHOD_" + stat + "_TITLE"));
            mapFilterDayTimeObject.put("summary",
                    labels.getString("MAP_FILTER_STATISTICAL_METHOD_" + stat + "_SUMMARY"));
            mapFilterDayTimeObject.put("statistical_method", statisticalMethodArray[stat - 1]);
            
            mapFilterStatisticalMethodList.put(mapFilterDayTimeObject);
        }
        
        // /
        
        final JSONArray mapFilterDeviceList = new JSONArray();
        
        // all-element
        
        final JSONObject mapFilterAllDeviceObject = new JSONObject();
        
        mapFilterAllDeviceObject.put("title", labels.getString("MAP_FILTER_DEVICE_0_TITLE"));
        mapFilterAllDeviceObject.put("summary", labels.getString("MAP_FILTER_DEVICE_0_SUMMARY"));
        mapFilterAllDeviceObject.put("device", "");
        
        mapFilterDeviceList.put(mapFilterAllDeviceObject);
        
        // /
        
        try
        {
            
            final Map<String, String> deviceList = getDeviceList(errorList);
            
            if (deviceList != null)
                for (final Map.Entry<String, String> entry : deviceList.entrySet())
                {
                    final JSONObject mapFilterDeviceObject = new JSONObject();
                    
                    mapFilterDeviceObject.put("title", entry.getValue());
                    mapFilterDeviceObject.put("summary",
                            labels.getString("MAP_FILTER_DEVICE_SUMMARY") + " " + entry.getValue());
                    mapFilterDeviceObject.put("device", entry.getKey());
                    
                    mapFilterDeviceList.put(mapFilterDeviceObject);
                }
        }
        catch (final SQLException ex)
        {
            ex.printStackTrace();
            errorList.addError("ERROR_DB_GET_SETTING_HISTORY_SQL");
        }
        
        // /////////////////////////
        
        final JSONObject carrierObj = new JSONObject();
        
        carrierObj.put("title", labels.getString("MAP_FILTER_CARRIER"));
        carrierObj.put("options", mapFilterCarrierList);
        
        // /
        
        final JSONObject statisticalMethodObj = new JSONObject();
        
        statisticalMethodObj.put("title", labels.getString("MAP_FILTER_STATISTICAL_METHOD"));
        statisticalMethodObj.put("options", mapFilterStatisticalMethodList);
        
        // /
        
        final JSONObject deviceObj = new JSONObject();
        
        deviceObj.put("title", labels.getString("MAP_FILTER_DEVICE"));
        deviceObj.put("options", mapFilterDeviceList);
        
        // /
        
        final JSONArray mapFilterList = new JSONArray();
        
        mapFilterList.put(carrierObj);
        mapFilterList.put(statisticalMethodObj);
        mapFilterList.put(deviceObj);
        
        return mapFilterList;
    }
}
