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
package at.alladin.rmbt.controlServer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import at.alladin.rmbt.db.Client;
import at.alladin.rmbt.shared.Classification;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.SignificantFormat;

public class HistoryResource extends ServerResource
{
    
    @Post("json")
    public String request(final String entity)
    {   	
    	long startTime = System.currentTimeMillis();
        addAllowOrigin();
        
        JSONObject request = null;
        
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;
        
        final String clientIpRaw = getIP();
        System.out.println(MessageFormat.format(labels.getString("NEW_HISTORY"), clientIpRaw));
        
        if (entity != null && !entity.isEmpty())
            // try parse the string to a JSON object
            try
            {
                request = new JSONObject(entity);
                readCapabilities(request);
                
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
                
//                System.out.println(request.toString(4));
                
                if (conn != null)
                {
                    final Client client = new Client(conn);
                    
                    if (request.optString("uuid").length() > 0
                            && client.getClientByUuid(UUID.fromString(request.getString("uuid"))) > 0)
                    {
                        
                        final Locale locale = new Locale(lang);
                        final Format format = new SignificantFormat(2, locale);
                        
                        String limitRequest = "";
                        if (request.optInt("result_limit", 0) != 0)
                        {
                            final int limit = request.getInt("result_limit");
                            
                            //get offset string if there is one
                            String offsetString = "";
                            if ((request.optInt("result_offset",0) != 0) && (request.getInt("result_offset") >= 0)) {
                                offsetString = " OFFSET " + request.getInt("result_offset");
                            }
                            
                            limitRequest = " LIMIT " + limit + offsetString;
                        }
                        
                        final ArrayList<String> deviceValues = new ArrayList<>();
                        String deviceRequest = "";
                        if (request.optJSONArray("devices") != null)
                        {
                            final JSONArray devices = request.getJSONArray("devices");
                            
                            boolean checkUnknown = false;
                            final StringBuffer sb = new StringBuffer();
                            for (int i = 0; i < devices.length(); i++)
                            {
                                final String device = devices.getString(i);
                                
                                if (device.equals("Unknown Device"))
                                    checkUnknown = true;
                                else
                                {
                                    if (sb.length() > 0)
                                        sb.append(',');
                                    deviceValues.add(device);
                                    sb.append('?');
                                }
                            }
                            
                            if (sb.length() > 0)
                                deviceRequest = " AND (COALESCE(adm.fullname, t.model) IN (" + sb.toString() + ")" + (checkUnknown ? " OR model IS NULL OR model = ''" : "") + ")";
//                            System.out.println(deviceRequest);
                            
                        }
                        
                        final ArrayList<String> filterValues = new ArrayList<>();
                        String networksRequest = "";
                        
                        if (request.optJSONArray("networks") != null)
                        {
                            final JSONArray tmpArray = request.getJSONArray("networks");
                            final StringBuilder tmpString = new StringBuilder();
                            
                            if (tmpArray.length() >= 1)
                            {
                                tmpString.append("AND nt.group_name IN (");
                                boolean first = true;
                                for (int i = 0; i < tmpArray.length(); i++)
                                {
                                    if (first)
                                        first = false;
                                    else
                                        tmpString.append(',');
                                    tmpString.append('?');
                                    filterValues.add(tmpArray.getString(i));
                                }
                                tmpString.append(')');
                            }
                            networksRequest = tmpString.toString();
                        }
                        
                        final JSONArray historyList = new JSONArray();
                        
                        final PreparedStatement st;
                        
                        try
                        {   
                        	if (client.getSync_group_id() == 0) { 
                        		//use faster request ignoring sync-group as user is not synced (id=0)
                                st = conn
                                    .prepareStatement(String
                                            .format(

                                            		"SELECT DISTINCT"
                                                    + " t.uuid, time, timezone, speed_upload, speed_download, ping_median, network_type, nt.group_name network_type_group_name,"
                                                    + " COALESCE(adm.fullname, t.model) model"
                                                    + " FROM test t"
                                                    + " LEFT JOIN device_map adm ON adm.codename=t.model"
                                                    + " LEFT JOIN network_type nt ON t.network_type=nt.uid"
                                                    + " WHERE t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'"
                                                    + " AND client_id = ?"
                                                    + " %s %s" + " ORDER BY time DESC" + " %s", deviceRequest,
                                                    networksRequest, limitRequest));
                        	}
                        	else { //use slower request including sync-group if client is synced
                                 st = conn
                                        .prepareStatement(String
                                                .format(
                                                		"SELECT DISTINCT"
                                                        + " t.uuid, time, timezone, speed_upload, speed_download, ping_median, network_type, nt.group_name network_type_group_name,"
                                                        + " COALESCE(adm.fullname, t.model) model"
                                                        + " FROM test t"
                                                        + " LEFT JOIN device_map adm ON adm.codename=t.model"
                                                        + " LEFT JOIN network_type nt ON t.network_type=nt.uid"
                                                        + " WHERE t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'"
                                                        + " AND (t.client_id IN (SELECT ? UNION SELECT uid FROM client WHERE sync_group_id = ? ))"
                                                        + " %s %s" + " ORDER BY time DESC" + " %s", deviceRequest,
                                                        networksRequest, limitRequest));
                       		
                        	}
                        
                            int i = 1;
                            st.setLong(i++, client.getUid());
                            if (client.getSync_group_id() != 0) 
                              st.setInt(i++, client.getSync_group_id());
                            
                            for (final String value : deviceValues)
                                st.setString(i++, value);
                            
                            for (final String filterValue : filterValues)
                                st.setString(i++, filterValue);
                            
                            //System.out.println(st.toString());
                            
                            final ResultSet rs = st.executeQuery();
                            
                            while (rs.next())
                            {
                                final JSONObject jsonItem = new JSONObject();
                                
                                jsonItem.put("test_uuid", rs.getString("uuid"));
                                
                                final Date date = rs.getTimestamp("time");
                                final long time = date.getTime();
                                final String tzString = rs.getString("timezone");
                                final TimeZone tz = TimeZone.getTimeZone(tzString);
                                
                                jsonItem.put("time", time);
                                jsonItem.put("timezone", tzString);
                                final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                        DateFormat.MEDIUM, locale);
                                dateFormat.setTimeZone(tz);
                                jsonItem.put("time_string", dateFormat.format(date));
                                
                                jsonItem.put("speed_upload", format.format(rs.getInt("speed_upload") / 1000d));
                                jsonItem.put("speed_download", format.format(rs.getInt("speed_download") / 1000d));
                                
                                final long ping = rs.getLong("ping_median");
                                jsonItem.put("ping", format.format(ping / 1000000d));
                                // backwards compatibility for old clients
                                jsonItem.put("ping_shortest", format.format(ping / 1000000d));
                                jsonItem.put("model", rs.getString("model"));
                                jsonItem.put("network_type", rs.getString("network_type_group_name"));
                                
                                
                                
                                //for appscape-iPhone-Version: also add classification to the response
                                jsonItem.put("speed_upload_classification", Classification.classify(Classification.THRESHOLD_UPLOAD, rs.getInt("speed_upload"), capabilities.getClassificationCapability().getCount()));
                                jsonItem.put("speed_download_classification", Classification.classify(Classification.THRESHOLD_DOWNLOAD, rs.getInt("speed_download"), capabilities.getClassificationCapability().getCount()));
                                jsonItem.put("ping_classification", Classification.classify(Classification.THRESHOLD_PING, rs.getLong("ping_median"), capabilities.getClassificationCapability().getCount()));
                                // backwards compatibility for old clients
                                jsonItem.put("ping_shortest_classification", Classification.classify(Classification.THRESHOLD_PING, rs.getLong("ping_median"), capabilities.getClassificationCapability().getCount()));
                                
                                historyList.put(jsonItem);
                            }
                            
//                            if (historyList.length() == 0)
//                                errorList.addError("ERROR_DB_GET_HISTORY");
                            // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENT"),
                            // new Object[] {uuid}));
                            
                            rs.close();
                            st.close();
                        }
                        catch (final SQLException e)
                        {
                            e.printStackTrace();
                            errorList.addError("ERROR_DB_GET_HISTORY_SQL");
                            // errorList.addError("ERROR_DB_GET_CLIENT_SQL");
                        }
                        
                        answer.put("history", historyList);
                    }
                    else
                        errorList.addError("ERROR_REQUEST_NO_UUID");
                    
                }
                else
                    errorList.addError("ERROR_DB_CONNECTION");
                
            }
            catch (final JSONException e)
            {
                errorList.addError("ERROR_REQUEST_JSON");
                System.out.println("Error parsing JSDON Data " + e.toString());
            }
            catch (final IllegalArgumentException e)
            {
                errorList.addError("ERROR_REQUEST_NO_UUID");
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
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println(MessageFormat.format(labels.getString("NEW_HISTORY_SUCCESS"), clientIpRaw, Long.toString(elapsedTime)));

        
        return answerString;
    }
    
    @Get("json")
    public String retrieve(final String entity)
    {
        return request(entity);
    }
    
}
