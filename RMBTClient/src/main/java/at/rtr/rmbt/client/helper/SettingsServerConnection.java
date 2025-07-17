/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
 * Copyright 2013-2016 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
package at.rtr.rmbt.client.helper;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SettingsServerConnection
{

    private URL hostUrl;

    private static URL getUrl(final boolean encryption, final String host, final String pathPrefix, final int port,
            final String path)
    {
        try
        {
            final String protocol = encryption ? "https" : "http";
            final int defaultPort = encryption ? 443 : 80;
            final String totalPath = (pathPrefix != null ? pathPrefix : "") + Config.RMBT_CONTROL_PATH + path;
            
            if (defaultPort == port)
                return new URL(protocol, host, totalPath);
            else
                return new URL(protocol, host, port, totalPath);
        }
        catch (final MalformedURLException e)
        {
            return null;
        }
    }

    /* 
    public String requestQoSTestParameters(final String host, final String pathPrefix, final int port,
            final boolean encryption, final ArrayList<String> geoInfo, final String uuid, final String clientType,
            final String clientName, final String clientVersion, final JSONObject additionalValues)
    */

    public String SettingsConnection(final String host, final boolean encryption, final String uuid, final int port, final String pathPrefix)
    {
        
        hostUrl = getUrl(encryption, host, pathPrefix, port, Config.RMBT_TEST_SETTINGS_REQUEST);
        
        //System.out.println("Connection Settings: " + hostUrl);
        
        final JSONObject regData = new JSONObject();
        
        try
        {
            regData.put("uuid", uuid);
            regData.put("terms_and_conditions_accepted", true);
            // regData.put("type","CLI");
            regData.put("name", "RTR-Netztest");
            // regData.put("version_name","0.1");
            // regData.put("version_code", "1");
            regData.put("terms_and_conditions_accepted_version", 6);
        }
        catch (final JSONException e1)
        {
            System.out.println("JSONException JC1 " + e1);
        }

        // JC Settings
        try {
            hostUrl = getUrl(encryption, host, pathPrefix, port, Config.RMBT_SETTINGS_HOST_URL);
            final JSONObject response = JSONParser.sendJSONToUrl(hostUrl, regData);
            //System.out.println(response);        
            //System.out.println(response.getJSONArray("settings").getJSONObject(0).getString("uuid"));  
            String uuidFromServer = response.getJSONArray("settings").getJSONObject(0).getString("uuid");

            System.out.println(String.format("Client UUID generated on server side. Client UUID: %s", uuidFromServer));

            return uuidFromServer;
        } catch (Exception e) {
 
        }
     
        return "";
    }

    

}
