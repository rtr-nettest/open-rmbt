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

import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import com.google.common.net.InetAddresses;

public class StatusResource extends ServerResource
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

        System.out.println(MessageFormat.format(labels.getString("NEW_STATUS_REQ"), clientIpRaw));

       	double geolat = 0;
    	double geolong = 0;
    	float geoaccuracy = 0; // in m
    	double geoaltitude = 0;
    	float geospeed = 0; // in m/s
    	String telephonyNetworkSimOperator = "";
    	String telephonyNetworkSimCountry = "";
    	boolean homeCountry = true;
        
        
        if (entity != null && !entity.isEmpty()) {
            // try parse the string to a JSON object
            try
            { 
            	// debug parameters sent
            	request = new JSONObject(entity);
            	System.out.println(request.toString(4));
            	
            	/* sample request
				Status request from 46.206.21.94
				{
    				"accuracy": 65,
    				"altitude": 233.5172119140625,
    				"client": "RMBT",
    				"device": "iPhone",
    				"language": "de",
    				"lat": 48.19530995813387,
    				"long": 16.29190354953834,
    				"model": "iPhone5,2",
    				"name": "RMBT",
    				"network_type": 2,
    				"os_version": "8.1.2",
    				"plattform": "iOS",
    				"softwareRevision": "next-858-8491858",
    				"softwareVersion": "1.3.7",
    				"softwareVersionCode": "1307",
    				"speed": 0,
    				"telephony_network_sim_country": "at",
    				"telephony_network_sim_operator": "232-01",
    				"telephony_network_sim_operator_name": "A1",
    				"time": 1418241092784,
    				"timezone": "Europe/Vienna",
    				"type": "MOBILE",
    				"uuid": "1cf594f6-0d07-4ff6-acd3-3d78ed9c0274",
    				"version": "0.3"
				}
            	*/

            	geolat = request.optDouble("lat", 0);
            	geolong = request.optDouble("long", 0);
            	geoaccuracy = (float) request.optDouble("accuracy", 0);
            	geoaltitude = request.optDouble("altitude", 0);
            	geospeed = (float) request.optDouble("speed", 0);
            	telephonyNetworkSimOperator = request.optString("telephony_network_sim_operator", "");
            	telephonyNetworkSimCountry = request.optString("telephony_network_sim_country", "");

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
/*
 * 
 * wb_a2 = 'AT' (2 digit country code)
 * ne_50m_admin_0_countries (SRID 900913 Mercator)
 * lat/long (SRID 4326 WGS84)
 * 
 * SELECT st_contains(the_geom, ST_transform(ST_GeomFromText('POINT(56.391944 48.218056)',4326),900913)) home_country
 * FROM  ne_50m_admin_0_countries 
 * WHERE wb_a2='AT';
 * 
 *
 * */
        try
        {
        	PreparedStatement st;
        	st = conn
        			.prepareStatement(
        					" SELECT st_contains(the_geom, ST_TRANSFORM(ST_GeomFromText( ? ,4326),900913)) home_country " +
        					" FROM  ne_50m_admin_0_countries " +
        					" WHERE wb_a2 = ? ");
        	int i = 1;   
        	final String point = "POINT(" + String.valueOf(geolong) + " " + String.valueOf(geolat) +  ")";
        	st.setObject(i++, point);    	
        	st.setObject(i++,telephonyNetworkSimCountry.toUpperCase());
    	
        	//debug query
        	System.out.println(st.toString());
            final ResultSet rs = st.executeQuery();
           
            if (rs.next()) // result only available if country (wb_a2) is found in ne_50_admmin_0_countries
            	homeCountry = rs.getBoolean("home_country");

        }
        catch (final SQLException e)
        {
        	errorList.addError("ERROR_DB_GENERAL");
        	e.printStackTrace();
        }

        try
        {
        	
        	answer.put("home_country",homeCountry);
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