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
package at.alladin.rmbt.android.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import at.alladin.rmbt.android.main.RMBTMainActivity;

/**
 * 
 * @author
 * 
 */
public class CheckSettingsTask extends AsyncTask<Void, Void, JSONArray>
{
    
    /**
	 * 
	 */
    private static final String DEBUG_TAG = "CheckSettingsTask";
    
    /**
	 * 
	 */
    private final RMBTMainActivity activity;
    
    /**
	 * 
	 */
    private ControlServerConnection serverConn;
    
    /**
	 * 
	 */
    private EndTaskListener endTaskListener;
    
    /**
	 * 
	 */
    private boolean hasError = false;
    
    /**
     * 
     * @param activity
     */
    public CheckSettingsTask(final RMBTMainActivity activity)
    {
        this.activity = activity;
        
    }
    
    /**
	 * 
	 */
    @Override
    protected JSONArray doInBackground(final Void... params)
    {
        JSONArray resultList = null;
        
        serverConn = new ControlServerConnection(activity.getApplicationContext());
        
        resultList = serverConn.requestSettings();
        
        return resultList;
    }
    
    /**
	 * 
	 */
    @Override
    protected void onPostExecute(final JSONArray resultList)
    {
        System.err.println("\n\n\n" + resultList + "\n\n\n");
        
        try
        {
        if (serverConn.hasError())
            hasError = true;
        else if (resultList != null && resultList.length() > 0)
        {
            
                JSONObject resultListItem;
                
                try
                {
                    resultListItem = resultList.getJSONObject(0);
                    
                    /* UUID */
                    
                    final String uuid = resultListItem.optString("uuid", "");
                    if (uuid != null && uuid.length() != 0)
                        ConfigHelper.setUUID(activity.getApplicationContext(), uuid);
                    
                    /* urls */
                    
                    final ConcurrentMap<String, String> volatileSettings = ConfigHelper.getVolatileSettings();
                    
                    final JSONObject urls = resultListItem.optJSONObject("urls");
                    if (urls != null)
                    {
                        final Iterator<String> keys = urls.keys();
                        
                        while (keys.hasNext())
                        {
                            final String key = keys.next();
                            final String value = urls.optString(key, null);
                            if (value != null) {
                                volatileSettings.put("url_" + key, value);
                                if ("statistics".equals(key)) {
                                	ConfigHelper.setCachedStatisticsUrl(value, activity);
                                }
                                else if ("control_ipv4_only".equals(key)) {
                                	ConfigHelper.setCachedControlServerNameIpv4(value, activity);
                                }
                                else if ("control_ipv6_only".equals(key)) {
                                	ConfigHelper.setCachedControlServerNameIpv6(value, activity);
                                }
                                else if ("url_ipv4_check".equals(key)) {
                                	ConfigHelper.setCachedIpv4CheckUrl(value, activity);
                                }
                                else if ("url_ipv6_check".equals(key)) {
                                	ConfigHelper.setCachedIpv6CheckUrl(value, activity);
                                }
                            }
                        }
                    }
                    
                    /* qos names */
                    final JSONArray qosNames = resultListItem.optJSONArray("qostesttype_desc");
                    if (qosNames != null) {
                    	final Map<String, String> qosNamesMap = new HashMap<String, String>();
                    	for (int i = 0; i < qosNames.length(); i++) {
                    		JSONObject json = qosNames.getJSONObject(i);
                    		qosNamesMap.put(json.optString("test_type"), json.optString("name"));
                    	}
                    	ConfigHelper.setCachedQoSNames(qosNamesMap, activity);
                    }
                    
                    /* map server */
                    
                    final JSONObject mapServer = resultListItem.optJSONObject("map_server");
                    if (mapServer != null)
                    {
                        final String host = mapServer.optString("host");
                        final int port = mapServer.optInt("port");
                        final boolean ssl = mapServer.optBoolean("ssl");
                        if (host != null && port > 0)
                            ConfigHelper.setMapServer(activity, host, port, ssl);
                    }
                    
                    /* control server version */
                    final JSONObject versions = resultListItem.optJSONObject("versions");
                    if (versions != null)
                    {
                    	if (versions.has("control_server_version")) {
                    		ConfigHelper.setControlServerVersion(activity, versions.optString("control_server_version"));
                    	}
                    }
                    
                    // ///////////////////////////////////////////////////////
                    // HISTORY / FILTER
                    
                    final JSONObject historyObject = resultListItem.getJSONObject("history");
                    
                    final JSONArray deviceArray = historyObject.getJSONArray("devices");
                    final JSONArray networkArray = historyObject.getJSONArray("networks");
                    
                    final String historyDevices[] = new String[deviceArray.length()];
                    
                    for (int i = 0; i < deviceArray.length(); i++)
                        historyDevices[i] = deviceArray.getString(i);
                    
                    final String historyNetworks[] = new String[networkArray.length()];
                    
                    for (int i = 0; i < networkArray.length(); i++)
                        historyNetworks[i] = networkArray.getString(i);
                    
                    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    
                    activity.setSettings(historyDevices, historyNetworks);
                    
                    activity.setHistoryDirty(true);
                    
                    // /////
                    
                    
                    ///////// servers
                    
                    final Set<String> serverSet;
                    final JSONArray servers = resultListItem.optJSONArray("servers");
                    if (servers == null)
                        serverSet = null;
                    else
                    {
                        serverSet = new TreeSet<String>();
                        for (int i = 0; i < servers.length(); i++)
                        {
                            final JSONObject serverObj = (JSONObject) servers.get(i);
                            final String serverName = serverObj.getString("name");
                            final String serverUuid = serverObj.getString("uuid");
                            
                            final Server server = new Server(serverName, serverUuid);
                            
                            serverSet.add(server.encode());
                            
                            System.out.println("server: " + serverName + " " + serverUuid);
                        }
                    }
                    ConfigHelper.setServers(activity, serverSet);
                    
                    
                    
                    ///////// permissions
                    
                    final JSONArray permissions = resultListItem.optJSONArray("request_android_permissions");
                    if (permissions != null)
                        PermissionHelper.setRequestPermissions(permissions);
                    
                }
                catch (final JSONException e)
                {
                    e.printStackTrace();
                }
                
            }
            else
                Log.i(DEBUG_TAG, "LEERE LISTE");
        }
        finally
        {
            if (endTaskListener != null)
                endTaskListener.taskEnded(resultList);
        }
    }
    
    /**
     * 
     * @param endTaskListener
     */
    public void setEndTaskListener(final EndTaskListener endTaskListener)
    {
        this.endTaskListener = endTaskListener;
    }
    
    /**
     * 
     * @return
     */
    public boolean hasError()
    {
        return hasError;
    }
}
