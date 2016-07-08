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
package at.alladin.rmbt.client.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import at.alladin.rmbt.util.capability.Capabilities;

public abstract class JSONParser
{
    public static int CONNECT_TIMEOUT = 5000;
    public static int READ_TIMEOUT = 8000;
    
    protected static JSONObject CAPABILITIES = null;
    
    
    public static void setCapabilities(Capabilities capabilities)
    {
        try
        {
            if (capabilities == null)
                CAPABILITIES = null;
            else
                CAPABILITIES = new JSONObject(new Gson().toJson(capabilities).toString());
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static String readUrl(final URL url) throws IOException 
    {
        final URLConnection urlConnection = url.openConnection();
        try {
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setReadTimeout(READ_TIMEOUT);
            
            final StringBuilder stringBuilder = new StringBuilder();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            int read;
            final char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                stringBuilder.append(chars, 0, read);
            return stringBuilder.toString();
        } finally {
            if (urlConnection instanceof HttpURLConnection)
                ((HttpURLConnection)urlConnection).disconnect();
        }
    }
    
    public static String sendToUrl(final URL url, final String data) throws IOException 
    {
        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setReadTimeout(READ_TIMEOUT);
            
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setRequestProperty("Accept", "application/json");
            
            final byte[] bytes = data.getBytes(Charset.forName("UTF-8"));
            urlConnection.setFixedLengthStreamingMode(bytes.length);
            urlConnection.getOutputStream().write(bytes);
            
            final StringBuilder stringBuilder = new StringBuilder();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            int read;
            final char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                stringBuilder.append(chars, 0, read);
            return stringBuilder.toString();
        } finally {
            urlConnection.disconnect();
        }
    }
    
    public static JSONObject getURL(final URL url)
    {
        // try parse the string to a JSON object
        try
        {
            final String data = readUrl(url);
            return new JSONObject(data);
        }
        catch (final Exception e)
        {
            //e.printStackTrace();
            return null;
        }
    }
    
    
    public static JSONObject sendJSONToUrl(final URL url, final JSONObject data)
    {
        try
        {
            if (CAPABILITIES != null)
                data.put("capabilities", CAPABILITIES);
            final String output = sendToUrl(url, data.toString());
            return new JSONObject(output);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 
     * @param object
     * @return
     * @throws JSONException
     */
    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator<?> keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, fromJson(object.get(key)));
        }
        return map;
    }
    
    /**
     * 
     * @param array
     * @return
     * @throws JSONException
     */
    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }
 
    /**
     * 
     * @param json
     * @return
     * @throws JSONException
     */
    private static Object fromJson(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return null;
        } else if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }
}
