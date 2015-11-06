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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import com.google.common.net.InetAddresses;

public class IpResource extends ServerResource
{
    @Post("json")
    public String request(final String entity)
    {
        addAllowOrigin();
        JSONObject request = null;
        
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;
        
        
        
        final String clientIpRaw = getIP();
        final InetAddress clientAddress = InetAddresses.forString(clientIpRaw);

        System.out.println(MessageFormat.format(labels.getString("NEW_IP_REQ"), clientIpRaw));
        
        if (entity != null && !entity.isEmpty()) {
            // try parse the string to a JSON object
            try
            {
            	// debug parameters sent
            	request = new JSONObject(entity);
            	System.out.println(request.toString(4));

            	/* sample request data
            	{

                "api_level": "22",
                "device": "hammerhead",
                "language": "de",
                "last_signal_item": {
                   "lte_rsrp": -106,
                   "lte_rsrq": -11,
                   "lte_rssnr": 300,
                   "network_type_id": 13,
                   "time": 1031122064510
                 },
                 "location": {
                   "accuracy": 24,
                   "age": 82517,
                   "altitude": 612,
                   "lat": 47.11288465,
                   "long": 15.1345835,
                   "provider": "gps",
                   "speed": 0
                  },

            	  "model": "Nexus 5",
            	  "os_version": "5.0(1570415)",
            	  "plattform": "Android",
            	  "product": "hammerhead",
            	  "softwareRevision": "master_initial-2413-gf89049d",
            	  "softwareVersionCode": 20046,
            	  "softwareVersionName": "2.0.46",
            	  "timezone": "Europe/Vienna",
            	  "type": "MOBILE",
            	  "uuid": "........(uuid)........"
            	}
            	 */
            	UUID uuid = null;
            	final String uuidString = request.optString("uuid", "");
            	if (uuidString.length() != 0)
            		uuid = UUID.fromString(uuidString);

               	final String clientPlattform = request.getString("plattform");
               	final String clientModel = request.getString("model");
               	final String clientProduct = request.getString("product");
               	final String clientDevice = request.getString("device");
               	final String clientSoftwareVersionCode = request.getString("softwareVersionCode");
               	final String clientApiLevel = request.getString("api_level");

            	final JSONObject location = request.optJSONObject("location");

            	long geoage = 0; // age in ms
            	double geolat = 0;
            	double geolong = 0;
            	float geoaccuracy = 0; // in m
            	double geoaltitude = 0;
            	float geospeed = 0; // in m/s
            	String geoprovider = "";

            	if (!request.isNull("location"))
            	{
            		geoage = location.optLong("age", 0);
            		geolat = location.optDouble("lat", 0);
            		geolong = location.optDouble("long", 0);
            		geoaccuracy = (float) location.optDouble("accuracy", 0);
            		geoaltitude = location.optDouble("altitude", 0);
            		geospeed = (float) location.optDouble("speed", 0);
            		geoprovider = location.optString("provider", "");
            	}            	
            	
            	//final JSONObject lastSignalItem = request.optJSONObject("last_signal_item");
            	//TODO parse & add to status table
            	

               	if (errorList.getLength() == 0)
            		try
            	{
            			PreparedStatement st;
            			st = conn
            					.prepareStatement(
            							"INSERT INTO status(client_uuid,time,plattform,model,product,device,software_version_code,api_level,ip,"
            							+ "age,lat,long,accuracy,altitude,speed,provider)"
            									+ "VALUES(?, NOW(),?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            									Statement.RETURN_GENERATED_KEYS);
            			int i = 1;
            			st.setObject(i++, uuid);
            			st.setObject(i++, clientPlattform);
            			st.setObject(i++, clientModel);
            			st.setObject(i++, clientProduct);
            			st.setObject(i++, clientDevice);
            			st.setObject(i++, clientSoftwareVersionCode);
            			st.setObject(i++, clientApiLevel);
            			st.setObject(i++, clientIpRaw);
            			// location information
            			st.setObject(i++, geoage);
            			st.setObject(i++, geolat);
            			st.setObject(i++, geolong);
            			st.setObject(i++, geoaccuracy);
            			st.setObject(i++, geoaltitude);
            			st.setObject(i++, geospeed);
            			st.setObject(i++, geoprovider);

            			final int affectedRows = st.executeUpdate();
            			if (affectedRows == 0)
            				errorList.addError("ERROR_DB_STORE_STATUS");
            	}
            	catch (final SQLException e)
            	{
            		errorList.addError("ERROR_DB_STORE_GENERAL");
            		e.printStackTrace();
            	}
            	
                answer.put("ip", clientIpRaw);
                if (clientAddress instanceof Inet4Address) {
                	answer.put("v", "4");
                }
                else if (clientAddress instanceof Inet6Address) {
                	answer.put("v", "6");
                }                    
                else {
                	answer.put("v", "0");
                }
            }
            catch (final JSONException e)
            {
                errorList.addError("ERROR_REQUEST_JSON");
                System.out.println("Error parsing JSON Data " + e.toString());
            }
        }
        else {
            errorList.addErrorString("Expected request is missing.");
        }
        
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