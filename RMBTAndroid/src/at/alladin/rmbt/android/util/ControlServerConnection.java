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
    
	public static enum UriType {
		DEFAULT_HOSTNAME,
		HOSTNAME_IPV4,
		HOSTNAME_IPV6
	}
	
    private static final String DEBUG_TAG = "ControlServerConnection";
    
    private static String hostname;
    
    private static String hostname4;
    
    private static String hostname6;
    
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
    
    private URI getUri(final String path) {
    	return getUri(path, UriType.DEFAULT_HOSTNAME);
    }
    
    private URI getUri(final String path, final UriType uriType)
    {
        try
        {
            String protocol = encryption ? "https" : "http";
            final int defaultPort = encryption ? 443 : 80;
            final String totalPath;
            if (useMapServerPath)
                totalPath = path;
            else
                totalPath = Config.RMBT_CONTROL_PATH + path;
            
            String host = hostname;
            
            switch(uriType) {
            case HOSTNAME_IPV4:
            	host = hostname4;
            	break;
            case HOSTNAME_IPV6:
            	host = hostname6;
            	break;
            case DEFAULT_HOSTNAME:
            default:
            	host = hostname;
            }
            
            if (defaultPort == port)
                return new URL(protocol, host, totalPath).toURI();
            else
                return new URL(protocol, host, port, totalPath).toURI();
            
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
        
        hostname4 = ConfigHelper.getCachedControlServerNameIpv4(context);
        hostname6 = ConfigHelper.getCachedControlServerNameIpv6(context);

        if (useMapServerPath)
        {
            encryption = ConfigHelper.isMapSeverSSL(context);
            hostname = ConfigHelper.getMapServerName(context);
            port = ConfigHelper.getMapServerPort(context);
        }
        else
        {
            encryption = ConfigHelper.isControlSeverSSL(context);
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
        //Log.d(DEBUG_TAG, "request to "+ hostUrl);
        final JSONObject response = jParser.sendJSONToUrl(hostUrl, requestData);
        
        if (response != null)
            try
            {
                final JSONArray errorList = response.optJSONArray("error");
                
                //System.out.println(requestData.toString(4));
                
                //System.out.println(response.toString(4));
                
                if (errorList == null || errorList.length() == 0)
                {
                    
                	
                	if (fieldName != null) {
                        return response.getJSONArray(fieldName);	
                	}
                	else {
                		JSONArray array = new JSONArray();
                		array.put(response);
                		return array;
                	}
                    
                }
                else
                {
                    //hasError = true;
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
        
        Log.i(DEBUG_TAG,"Newsrequest to " + hostUrl);
        
        final JSONObject requestData = new JSONObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            
            requestData.put("uuid", getUUID());
            requestData.put("lastNewsUid", lastNewsUid);
        }
        catch (final JSONException e)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
        }
        
        return sendRequest(hostUrl, requestData, "news");
        
    }
    
    public JSONArray requestIp(boolean isIpv6)
    {
        
        hasError = false;
                
        URI hostUrl = null;
        
        final JSONObject requestData = new JSONObject();
        
        try
        {
            hostUrl = isIpv6 ? new URL(ConfigHelper.getCachedIpv6CheckUrl(context)).toURI() : new URL(ConfigHelper.getCachedIpv4CheckUrl(context)).toURI();
            
            Log.i(DEBUG_TAG,"IP request to " + hostUrl);

            InformationCollector.fillBasicInfo(requestData, context);
            
            requestData.put("uuid", getUUID());
        }
        catch (final Exception e)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
		}
        
        return hostUrl != null ? sendRequest(hostUrl, requestData, null) : null;
        
    }
    
    public JSONArray requestHistory(final String uuid, final ArrayList<String> devicesToShow,
            final ArrayList<String> networksToShow, final int resultLimit)
    {
        
        hasError = false;
        
        final URI hostUrl = getUri(Config.RMBT_HISTORY_HOST_URL);
        
        Log.i(DEBUG_TAG,"Historyrequest to " + hostUrl);
        
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
            //e1.printStackTrace();
        }
        
        return sendRequest(hostUrl, requestData, "history");
        
    }
    
    public JSONArray requestTestResult(final String testUuid)
    {
        
        hasError = false;
        
        final URI hostUrl = getUri(Config.RMBT_TESTRESULT_HOST_URL);
        
        Log.i(DEBUG_TAG,"RMBTTest Result request to " + hostUrl);
        
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
    
    public JSONObject requestOpenDataTestResult(final String testUuid, final String openTestUuid) {
        hasError = false;
        
        final URI hostUrl = getUri(Config.RMBT_TESTRESULT_OPENDATA_HOST_URL + openTestUuid);
        
        Log.i(DEBUG_TAG,"RMBTTest OpenData Result request to " + hostUrl);
        
        final JSONObject requestData = new JSONObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            requestData.put("test_uuid", testUuid);
            requestData.put("open_test_uuid", openTestUuid);
            requestData.put("uuid", getUUID());
        }
        catch (final JSONException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }
        
        return jParser.getURL(hostUrl);
        //return sendRequest(hostUrl, requestData, null);
    }
    
    public JSONArray requestTestResultQoS(final String testUuid)
    {
        
        hasError = false;
        
        final URI hostUrl = getUri(Config.RMBT_TESTRESULT_QOS_HOST_URL);
        
        Log.i(DEBUG_TAG,"RMBTTest QoS Result request to " + hostUrl);
        
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
        
        return sendRequest(hostUrl, requestData, null);
    }
    
    public JSONArray requestTestResultDetail(final String testUuid)
    {
        
        hasError = false;
        
        final URI hostUrl = getUri(Config.RMBT_TESTRESULT_DETAIL_HOST_URL);
        
        Log.i(DEBUG_TAG,"RMBTTest ResultDetail request to " + hostUrl);
        
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
        
        Log.i(DEBUG_TAG,"Sync request to " + hostUrl);
        
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
            clientName = context.getResources().getString(R.string.app_name_api);
        }
        catch (final NameNotFoundException e)
        {
            // e1.printStackTrace();
            Log.e(DEBUG_TAG, "version of the application cannot be found", e);
        }
        
        Log.i(DEBUG_TAG,"Settings request to " + hostUrl);
        
        final JSONObject requestData = new JSONObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            requestData.put("uuid", getUUID());
            requestData.put("name", clientName);
            requestData.put("version_name", clientVersionName);
            requestData.put("version_code", clientVersionCode);
            
            final int tcAcceptedVersion = ConfigHelper.getTCAcceptedVersion(context);
            requestData.put("terms_and_conditions_accepted_version", tcAcceptedVersion);
            if (tcAcceptedVersion > 0) // for server backward compatibility
                requestData.put("terms_and_conditions_accepted", true);
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
        
        Log.i(DEBUG_TAG, "request to " + hostUrl);
        final JSONObject response = jParser.sendJSONToUrl(hostUrl, requestData);
        return response;
    }
    
    public JSONArray requestMapMarker(final double lat, final double lon, final int zoom, final int size,
            final Map<String, String> optionMap)
    {
        hasError = false;
        
        final URI hostUrl = getUri(MapProperties.MARKER_PATH);
        
        Log.i(DEBUG_TAG,"MapMarker request to " + hostUrl);
        
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
