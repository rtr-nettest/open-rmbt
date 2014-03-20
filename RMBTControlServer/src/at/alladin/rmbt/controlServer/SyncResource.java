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
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import at.alladin.rmbt.shared.ResourceManager;

public class SyncResource extends ServerResource
{
    @Post("json")
    public String request(final String entity)
    {
        addAllowOrigin();
        
        JSONObject request = null;
        
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;
        
        System.out.println(MessageFormat.format(labels.getString("NEW_SYNC_REQUEST"), getIP()));
        
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
                
//                System.out.println(request.toString(4));
                
                if (conn != null)
                {
                    
                    final JSONArray syncList = new JSONArray();
                    
                    UUID uuid = null;
                    if (request.optString("uuid").length() > 0)
                        uuid = UUID.fromString(request.getString("uuid"));
                    
                    if (uuid != null && request.optString("sync_code").length() == 0)
                    {
                        
                        String syncCode = "";
                        
                        try
                        {
                            
                            final PreparedStatement st = conn
                                    .prepareStatement("SELECT rmbt_get_sync_code(CAST (? AS UUID)) AS code");
                            st.setString(1, uuid.toString());
                            
                            final ResultSet rs = st.executeQuery();
                            
                            if (rs.next())
                                syncCode = rs.getString("code");
                            else
                                errorList.addError("ERROR_DB_GET_SYNC_SQL");
                            // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENT"),
                            // new Object[] {uuid}));
                            
                            rs.close();
                            st.close();
                        }
                        catch (final SQLException e)
                        {
                            e.printStackTrace();
                            errorList.addError("ERROR_DB_GET_SYNC_SQL");
                            // errorList.addError("ERROR_DB_GET_CLIENT_SQL");
                        }
                        
                        if (errorList.getLength() == 0)
                        {
                            final JSONObject jsonItem = new JSONObject();
                            //lower case code is easier to enter on mobile devices
                            jsonItem.put("sync_code", syncCode.toLowerCase(Locale.US));
                            
                            syncList.put(jsonItem);
                            
                        }
                    }
                    else if (uuid != null && request.optString("sync_code").length() > 0)
                    {
                        
                        final String syncCode = request.getString("sync_code").toUpperCase(Locale.US);
                        int syncGroup1 = 0;
                        int uid1 = 0;
                        int syncGroup2 = 0;
                        int uid2 = 0;
                        
                        String msgTitle = labels.getString("SYNC_SUCCESS_TITLE");
                        String msgText = labels.getString("SYNC_SUCCESS_TEXT");
                        
                        boolean error = false;
                        
                        try
                        {
                            
                            PreparedStatement st = conn.prepareStatement("SELECT * FROM client WHERE sync_code = ? AND sync_code_timestamp + INTERVAL '1 month' > NOW()");
                            st.setString(1, syncCode);
                            
                            ResultSet rs = st.executeQuery();
                            
                            if (rs.next())
                            {
                                syncGroup1 = rs.getInt("sync_group_id");
                                uid1 = rs.getInt("uid");
                            }
                            else
                            {
                                msgTitle = labels.getString("SYNC_CODE_TITLE");
                                msgText = labels.getString("SYNC_CODE_TEXT");
                                error = true;
                                // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENT"),
                                // new Object[] {uuid}));
                            }
                            rs.close();
                            st.close();
                            
                            st = conn.prepareStatement("SELECT * FROM client WHERE uuid = CAST(? AS UUID)");
                            st.setString(1, uuid.toString());
                            
                            rs = st.executeQuery();
                            
                            if (rs.next())
                            {
                                syncGroup2 = rs.getInt("sync_group_id");
                                uid2 = rs.getInt("uid");
                            }
                            else
                            {
                                msgTitle = labels.getString("SYNC_UUID_TITLE");
                                msgText = labels.getString("SYNC_UUID_TEXT");
                                error = true;
                                // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENT"),
                                // new Object[] {uuid}));
                            }
                            rs.close();
                            st.close();
                            
                            if (syncGroup1 > 0 && syncGroup1 == syncGroup2)
                            {
                                msgTitle = labels.getString("SYNC_GROUP_TITLE");
                                msgText = labels.getString("SYNC_GROUP_TEXT");
                                error = true;
                            }
                            
                            if (uid1 > 0 && uid1 == uid2)
                            {
                                msgTitle = labels.getString("SYNC_CLIENT_TITLE");
                                msgText = labels.getString("SYNC_CLIENT_TEXT");
                                error = true;
                            }
                            
                            if (!error)
                                if (syncGroup1 == 0 && syncGroup2 == 0)
                                {
                                    
                                    int key = 0;
                                    
                                    // create new group
                                    st = conn.prepareStatement("INSERT INTO sync_group(tstamp) " + "VALUES(now())",
                                            Statement.RETURN_GENERATED_KEYS);
                                    
                                    int affectedRows = st.executeUpdate();
                                    if (affectedRows == 0)
                                        errorList.addError("ERROR_DB_STORE_SYNC_GROUP");
                                    else
                                    {
                                        
                                        rs = st.getGeneratedKeys();
                                        if (rs.next())
                                            // Retrieve the auto generated
                                            // key(s).
                                            key = rs.getInt(1);
                                        rs.close();
                                    }
                                    st.close();
                                    
                                    if (key > 0)
                                    {
                                        st = conn
                                                .prepareStatement("UPDATE client SET sync_group_id = ? WHERE uid = ? OR uid = ?");
                                        st.setInt(1, key);
                                        st.setInt(2, uid1);
                                        st.setInt(3, uid2);
                                        
                                        affectedRows = st.executeUpdate();
                                        
                                        if (affectedRows == 0)
                                            errorList.addError("ERROR_DB_UPDATE_SYNC_GROUP");
                                    }
                                    
                                }
                                else if (syncGroup1 == 0 && syncGroup2 > 0)
                                {
                                    
                                    // add 1 to 2
                                    
                                    st = conn.prepareStatement("UPDATE client SET sync_group_id = ? WHERE uid = ?");
                                    st.setInt(1, syncGroup2);
                                    st.setInt(2, uid1);
                                    
                                    final int affectedRows = st.executeUpdate();
                                    
                                    if (affectedRows == 0)
                                        errorList.addError("ERROR_DB_UPDATE_SYNC_GROUP");
                                    
                                }
                                else if (syncGroup1 > 0 && syncGroup2 == 0)
                                {
                                    
                                    // add 2 to 1
                                    
                                    st = conn.prepareStatement("UPDATE client SET sync_group_id = ? WHERE uid = ? ");
                                    st.setInt(1, syncGroup1);
                                    st.setInt(2, uid2);
                                    
                                    final int affectedRows = st.executeUpdate();
                                    
                                    if (affectedRows == 0)
                                        errorList.addError("ERROR_DB_UPDATE_SYNC_GROUP");
                                    
                                }
                                else if (syncGroup1 > 0 && syncGroup2 > 0)
                                {
                                    
                                    // add all of 2 to 1
                                    
                                    st = conn
                                            .prepareStatement("UPDATE client SET sync_group_id = ? WHERE sync_group_id = ?");
                                    st.setInt(1, syncGroup1);
                                    st.setInt(2, syncGroup2);
                                    
                                    int affectedRows = st.executeUpdate();
                                    
                                    if (affectedRows == 0)
                                        errorList.addError("ERROR_DB_UPDATE_SYNC_GROUP");
                                    else
                                    {
                                        
                                        // Delete empty group
                                        st = conn.prepareStatement("DELETE FROM sync_group WHERE uid = ?");
                                        st.setInt(1, syncGroup2);
                                        
                                        affectedRows = st.executeUpdate();
                                        
                                        if (affectedRows == 0)
                                            errorList.addError("ERROR_DB_DELETE_SYNC_GROUP");
                                    }
                                    
                                }
                            
                        }
                        catch (final SQLException e)
                        {
                            e.printStackTrace();
                            errorList.addError("ERROR_DB_GET_SYNC_SQL");
                            // errorList.addError("ERROR_DB_GET_CLIENT_SQL");
                        }
                        
                        if (errorList.getLength() == 0)
                        {
                            
                            final JSONObject jsonItem = new JSONObject();
                            
                            jsonItem.put("msg_title", msgTitle);
                            jsonItem.put("msg_text", msgText);
                            jsonItem.put("success", !error);
                            syncList.put(jsonItem);
                            
                        }
                    }
                    
                    answer.put("sync", syncList);
                    
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
    
        return answerString;
    }
    
    @Get("json")
    public String retrieve(final String entity)
    {
        return request(entity);
    }
    
}
