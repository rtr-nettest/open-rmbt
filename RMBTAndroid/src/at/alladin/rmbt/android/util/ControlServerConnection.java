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
package at.alladin.rmbt.android.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.map.MapProperties;
import at.alladin.rmbt.client.helper.Config;
import at.alladin.rmbt.client.helper.JSONParser;

public class ControlServerConnection
{
    
    private static final String DEBUG_TAG = "ControlServerConnection";
    
    private static String hostname;
    
    private static int port;
    
    private boolean encryption;
    
    private JSONParser jParser;
    
    private Context context;
    
    @SuppressWarnings("unused")
    private String errorMsg = "";
    
    private boolean hasError = false;
    
    private boolean useMapServerPath = false;
    
    private String getUUID()
    {
        return ConfigHelper.getUUID(context.getApplicationContext());
    }
    
    private URI getUri(final String path)
    {
        try
        {
            String protocol = encryption ? "https" : "http";
            final int defaultPort = encryption ? 443 : 80;
            final String totalPath;
            if (useMapServerPath)
            {
                totalPath = path;
                protocol = MapProperties.OVERLAY_PROTOCOL;
                port = MapProperties.OVERLAY_PORT;
            }
            else
                totalPath = Config.RMBT_CONTROL_PATH + path;
            
            if (defaultPort == port)
                return new URL(protocol, hostname, totalPath).toURI();
            else
                return new URL(protocol, hostname, port, totalPath).toURI();
            
        }
        catch (final MalformedURLException e)
        {
            return null;
        }
        catch (final URISyntaxException e)
        {
            return null;
        }
    }
    
    /**
     * @param args
     */
    public ControlServerConnection(final Context context)
    {
        setupServer(context, false);
    }
    
    /**
     * @param args
     */
    public ControlServerConnection(final Context context, final boolean useMapServer)
    {
        setupServer(context, useMapServer);
    }
    
    private void setupServer(final Context context, final boolean useMapServerPath)
    {
        // Creating JSON Parser instance
        jParser = new JSONParser();
        hasError = false;
        
        this.context = context;
        
        this.useMapServerPath = useMapServerPath;
        
        encryption = ConfigHelper.isControlSeverSSL(context);
        if (useMapServerPath)
        {
            hostname = MapProperties.OVERLAY_HOST;
            port = MapProperties.OVERLAY_PORT;
        }
        else
        {
            hostname = ConfigHelper.getControlServerName(context);
            port = ConfigHelper.getControlServerPort(context);
        }
    }
    
    public boolean unload()
    {
        jParser = null;
        
        return true;
    }
    
    private JSONArray sendRequest(final URI hostUrl, final JSONObject requestData, final String fieldName)
    {
        // getting JSON string from URL
        final JSONObject response = jParser.sendJSONToUrl(hostUrl, requestData);
        
        if (response != null)
            try
            {
                final JSONArray errorList = response.optJSONArray("error");
                
                // System.out.println(requestData.toString(4));
                
                // System.out.println(response.toString(4));
                
                if (errorList == null || errorList.length() == 0)
                {
                    
                    final JSONArray responseArray = response.getJSONArray(fieldName);
                    
                    return responseArray;
                    
                }
                else
                {
                    
                    hasError = true;
                    for (int i = 0; i < errorList.length(); i++)
                    {
                        
                        if (i > 0)
                            errorMsg += "\n";
                        errorMsg += errorList.getString(i);
                    }
                }
                
                // }
            }
            catch (final JSONException e)
            {
                hasError = true;
                errorMsg = "Error parsing server response";
                e.printStackTrace();
            }
        else
        {
            hasError = true;
            errorMsg = "No response";
        }
        
        return null;
        
    }
    
    public JSONArray requestNews(final long lastNewsUid)
    {
        
        hasError = false;
        
        final URI hostUrl = getUri(Config.RMBT_NEWS_HOST_URL);
        
        System.out.println("Newsrequest to " + hostUrl);
        
        final JSONObject requestData = new JSONObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            requestData.put("lastNewsUid", lastNewsUid);
        }
        catch (final JSONException e)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
        }
        
        return sendRequest(hostUrl, requestData, "news");
        
    }
    
    public JSONArray requestHistory(final String uuid, final ArrayList<String> devicesToShow,
            final ArrayList<String> networksToShow, final int resultLimit)
    {
        
        hasError = false;
        
        final URI hostUrl = getUri(Config.RMBT_HISTORY_HOST_URL);
        
        System.out.println("Historyrequest to " + hostUrl);
        
        final JSONObject requestData = new JSONObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            requestData.put("uuid", uuid);
            requestData.put("result_limit", resultLimit);
            
            if (devicesToShow != null && devicesToShow.size() > 0)
            {
                
                final JSONArray filterList = new JSONArray();
                
                for (final String s : devicesToShow)
                    filterList.put(s);
                
                requestData.put("devices", filterList);
            }
            
            if (networksToShow != null && networksToShow.size() > 0)
            {
                
                final JSONArray filterList = new JSONArray();
                
                for (final String s : networksToShow)
                {
                    Log.i(DEBUG_TAG, s);
                    filterList.put(s);
                }
                
                requestData.put("networks", filterList);
            }
            
        }
        catch (final JSONException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }
        
        return sendRequest(hostUrl, requestData, "history");
        
    }
    
    public JSONArray requestTestResult(final String testUuid)
    {
        
        hasError = false;
        
        final URI hostUrl = getUri(Config.RMBT_TESTRESULT_HOST_URL);
        
        System.out.println("RMBTTest Result request to " + hostUrl);
        
        final JSONObject requestData = new JSONObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            requestData.put("test_uuid", testUuid);
            requestData.put("uuid", getUUID());
        }
        catch (final JSONException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }
        
        return sendRequest(hostUrl, requestData, "testresult");
    }
    
    public JSONArray requestTestResultDetail(final String testUuid)
    {
        
        hasError = false;
        
        final URI hostUrl = getUri(Config.RMBT_TESTRESULT_DETAIL_HOST_URL);
        
        System.out.println("RMBTTest Result request to " + hostUrl);
        
        final JSONObject requestData = new JSONObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            requestData.put("test_uuid", testUuid);
            requestData.put("uuid", getUUID());
        }
        catch (final JSONException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }
        
        return sendRequest(hostUrl, requestData, "testresultdetail");
    }
    
    public JSONArray requestSyncCode(final String uuid, final String syncCode)
    {
        
        hasError = false;
        
        final URI hostUrl = getUri(Config.RMBT_SYNC_HOST_URL);
        
        System.out.println("Sync request to " + hostUrl);
        
        final JSONObject requestData = new JSONObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            requestData.put("uuid", uuid);
            
            if (syncCode.length() > 0)
                requestData.put("sync_code", syncCode);
            
        }
        catch (final JSONException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }
        
        return sendRequest(hostUrl, requestData, "sync");
    }
    
    public JSONArray requestSettings()
    {
        
        hasError = false;
        
        final URI hostUrl = getUri(Config.RMBT_SETTINGS_HOST_URL);
        
        PackageInfo pInfo;
        String clientVersionName = "";
        int clientVersionCode = 0;
        String clientName = "";
        try
        {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            clientVersionName = pInfo.versionName;
            clientVersionCode = pInfo.versionCode;
            clientName = context.getResources().getString(R.string.app_name);
        }
        catch (final NameNotFoundException e)
        {
            // e1.printStackTrace();
            Log.e(DEBUG_TAG, "version of the application cannot be found", e);
        }
        
        System.out.println("Settings request to " + hostUrl);
        
        final JSONObject requestData = new JSONObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            requestData.put("uuid", getUUID());
            requestData.put("name", clientName);
            requestData.put("version_name", clientVersionName);
            requestData.put("version_code", clientVersionCode);
            
            if (ConfigHelper.isTCAccepted(context))
                requestData.put("terms_and_conditions_accepted", ConfigHelper.isTCAccepted(context));
            
        }
        catch (final JSONException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }
        
        return sendRequest(hostUrl, requestData, "settings");
    }
    
    public JSONObject requestMapOptionsInfo()
    {
        
        hasError = false;
        
        final URI hostUrl = getUri(MapProperties.MAP_OPTIONS_PATH);
        
        final JSONObject requestData = new JSONObject();
        
        try
        {
            requestData.put("language", Locale.getDefault().getLanguage());
        }
        catch (final JSONException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
        }
        
        final JSONObject response = jParser.sendJSONToUrl(hostUrl, requestData);
        return response;
    }
    
    public JSONArray requestMapMarker(final double lat, final double lon, final int zoom, final int size,
            final Map<String, String> optionMap)
    {
        hasError = false;
        
        final URI hostUrl = getUri(MapProperties.MARKER_PATH);
        
        System.out.println("MapMarker request to " + hostUrl);
        
        final JSONObject requestData = new JSONObject();
        
        try
        {
            requestData.put("language", Locale.getDefault().getLanguage());
            
            final JSONObject coords = new JSONObject();
            coords.put("lat", lat);
            coords.put("lon", lon);
            coords.put("z", zoom);
            coords.put("size", size);
            requestData.put("coords", coords);
            
            final JSONObject filter = new JSONObject();
            final JSONObject options = new JSONObject();
            
            for (final String key : optionMap.keySet())
            {
                
                if (MapProperties.MAP_OVERLAY_KEY.equals(key))
                    // skip map_overlay_key
                    continue;
                
                final String value = optionMap.get(key);
                
                if (value != null && value.length() > 0)
                    if (key.equals("map_options"))
                        options.put(key, value);
                    else
                        filter.put(key, value);
            }
            
            requestData.put("filter", filter);
            requestData.put("options", options);
            
        }
        catch (final JSONException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }
        
        return sendRequest(hostUrl, requestData, "measurements");
    }
    
    public boolean hasError()
    {
        return hasError;
    }
    
}
