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

import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import at.alladin.rmbt.db.Cell_location;
import at.alladin.rmbt.db.GeoLocation;
import at.alladin.rmbt.db.Ping;
import at.alladin.rmbt.db.Signal;
import at.alladin.rmbt.db.Speed_down;
import at.alladin.rmbt.db.Speed_up;
import at.alladin.rmbt.db.Test;
import at.alladin.rmbt.db.fields.IntField;
import at.alladin.rmbt.shared.Helperfunctions;

import com.google.common.net.InetAddresses;

public class ResultResource extends ServerResource
{
    @Post("json")
    public String request(final String entity)
    {
        final String secret = getContext().getParameters().getFirstValue("RMBT_SECRETKEY");
        
        addAllowOrigin();
        
        JSONObject request = null;
        
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        
        final String ip = getIP();
        final InetAddress inetAddress = InetAddresses.forString(ip);
        final String clientIp = InetAddresses.toAddrString(inetAddress);
        
        System.out.println(MessageFormat.format(labels.getString("NEW_RESULT"), ip));
        
        if (entity != null && !entity.isEmpty())
            // try parse the string to a JSON object
            try
            {
                request = new JSONObject(entity);
                
                final String lang = request.optString("client_language");
                
                // Load Language Files for Client
                
                final List<String> langs = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));
                
                if (langs.contains(lang))
                {
                    errorList.setLanguage(lang);
                    labels = (PropertyResourceBundle) ResourceBundle.getBundle("at.alladin.rmbt.res.SystemMessages",
                            new Locale(lang));
                }
                
//                System.out.println(request.toString(4));
                
                if (conn != null)
                {
                    
                    final Test test = new Test(conn);
                    
                    if (request.optString("test_token").length() > 0)
                    {
                        
                        final String[] token = request.getString("test_token").split("_");
                        
                        try
                        {
                            
                            // Check if UUID
                            final UUID testUuid = UUID.fromString(token[0]);
                            
                            final String data = token[0] + "_" + token[1];
                            
                            final String hmac = Helperfunctions.calculateHMAC(secret, data);
                            if (hmac.length() == 0)
                                errorList.addError("ERROR_TEST_TOKEN");
                            
                            if (token[2].length() > 0 && hmac.equals(token[2]))
                            {
                                
                                final List<String> clientNames = Arrays.asList(settings.getString("RMBT_CLIENT_NAME")
                                        .split(",\\s*"));
                                final List<String> clientVersions = Arrays.asList(settings.getString(
                                        "RMBT_VERSION_NUMBER").split(",\\s*"));
                                
                                if (test.getTestByUuid(testUuid) > 0)
                                    if (clientNames.contains(request.optString("client_name"))
                                            && clientVersions.contains(request.optString("client_version")))
                                    {
                                        
                                        test.setFields(request);
                                        
                                        // RMBTClient Info
                                        
                                        final String ipLocal = request.optString("test_ip_local", null);
                                        if (ipLocal != null)
                                        {
                                            test.getField("client_local_ip").setString(
                                                    Helperfunctions.filterIpString(ipLocal));
                                        }
                                        test.getField("client_public_ip").setString(clientIp);
                                        test.getField("client_public_ip_anonymized").setString(
                                                Helperfunctions.anonymizeIpString(clientIp));
                                        
                                        if (ipLocal != null)
                                        {
                                            test.getField("nat_type")
                                                    .setString(Helperfunctions.getNatType(ipLocal, clientIp));
                                        }
                                        
                                        final String ipServer = request.optString("test_ip_server", null);
                                        if (ipServer != null)
                                        {
                                            final InetAddress testServerInetAddress = InetAddresses.forString(ipServer);
                                            test.getField("server_ip").setString(
                                                    InetAddresses.toAddrString(testServerInetAddress));
                                        }
                                        test.getField("status").setString("FINISHED");
                                        
                                        // Additional Info from Android
                                        
                                        JSONArray speedData = request.optJSONArray("speed_down");
                                        
                                        if (speedData != null && !test.hasError())
                                            for (int i = 0; i < speedData.length(); i++)
                                            {
                                                
                                                final JSONObject speedDataItem = speedData.getJSONObject(i);
                                                
                                                final Speed_down downSpeed = new Speed_down(conn);
                                                
                                                downSpeed.setTest_id(test.getUid());
                                                
                                                final long clientTime = speedDataItem.optLong("tstamp");
                                                final Timestamp tstamp = java.sql.Timestamp.valueOf(new Timestamp(
                                                        clientTime).toString());
                                                downSpeed.setTstamp(tstamp, test.getField("timezone").toString());
                                                
                                                downSpeed.setSumAllTrans(speedDataItem.optLong("sumAllTrans", 0));
                                                downSpeed.setSumDiffTrans(speedDataItem.optLong("sumDiffTrans", 0));
                                                downSpeed.setMaxAllTime(speedDataItem.optLong("maxAllTime", 0));
                                                downSpeed.setMaxDiffTime(speedDataItem.optLong("maxDiffTime", 0));
                                                
                                                downSpeed.storeSpeed_down();
                                                
                                                if (downSpeed.hasError())
                                                {
                                                    errorList.addError(downSpeed.getError());
                                                    break;
                                                }
                                                
                                            }
                                        
                                        speedData = request.optJSONArray("speed_up");
                                        
                                        if (speedData != null && !test.hasError())
                                            for (int i = 0; i < speedData.length(); i++)
                                            {
                                                
                                                final JSONObject speedDataItem = speedData.getJSONObject(i);
                                                
                                                final Speed_up upSpeed = new Speed_up(conn);
                                                
                                                upSpeed.setTest_id(test.getUid());
                                                
                                                final long clientTime = speedDataItem.optLong("tstamp");
                                                final Timestamp tstamp = java.sql.Timestamp.valueOf(new Timestamp(
                                                        clientTime).toString());
                                                upSpeed.setTstamp(tstamp, test.getField("timezone").toString());
                                                
                                                upSpeed.setSumAllTrans(speedDataItem.optLong("sumAllTrans", 0));
                                                upSpeed.setSumDiffTrans(speedDataItem.optLong("sumDiffTrans", 0));
                                                upSpeed.setMaxAllTime(speedDataItem.optLong("maxAllTime", 0));
                                                upSpeed.setMaxDiffTime(speedDataItem.optLong("maxDiffTime", 0));
                                                
                                                upSpeed.storeSpeed_up();
                                                
                                                if (upSpeed.hasError())
                                                {
                                                    errorList.addError(upSpeed.getError());
                                                    break;
                                                }
                                            }
                                        
                                        final JSONArray pingData = request.optJSONArray("pings");
                                        
                                        if (pingData != null && !test.hasError())
                                            for (int i = 0; i < pingData.length(); i++)
                                            {
                                                
                                                final JSONObject pingDataItem = pingData.getJSONObject(i);
                                                
                                                final Ping ping = new Ping(conn);
                                                
                                                ping.setTest_id(test.getUid());
                                                
                                                ping.setValue(pingDataItem.optLong("value", 0));
                                                ping.storePing();
                                                
                                                if (ping.hasError())
                                                {
                                                    errorList.addError(ping.getError());
                                                    break;
                                                }
                                                
                                            }
                                        
                                        final JSONArray geoData = request.optJSONArray("geoLocations");
                                        
                                        if (geoData != null && !test.hasError())
                                            for (int i = 0; i < geoData.length(); i++)
                                            {
                                                
                                                final JSONObject geoDataItem = geoData.getJSONObject(i);
                                                
                                                if (geoDataItem.optLong("tstamp", 0) != 0 && geoDataItem.optDouble("geo_lat", 0) != 0 && geoDataItem.optDouble("geo_long", 0) != 0) {
                                                
                                                final GeoLocation geoloc = new GeoLocation(conn);
                                                
                                                geoloc.setTest_id(test.getUid());
                                                
                                                final long clientTime = geoDataItem.optLong("tstamp");
                                                final Timestamp tstamp = java.sql.Timestamp.valueOf(new Timestamp(
                                                        clientTime).toString());
                                                
                                                geoloc.setTime(tstamp, test.getField("timezone").toString());
                                                geoloc.setAccuracy((float) geoDataItem.optDouble("accuracy", 0));
                                                geoloc.setAltitude(geoDataItem.optDouble("altitude", 0));
                                                geoloc.setBearing((float) geoDataItem.optDouble("bearing", 0));
                                                geoloc.setSpeed((float) geoDataItem.optDouble("speed", 0));
                                                geoloc.setProvider(geoDataItem.optString("provider", ""));
                                                geoloc.setGeo_lat(geoDataItem.optDouble("geo_lat", 0));
                                                geoloc.setGeo_long(geoDataItem.optDouble("geo_long", 0));
                                                
                                                geoloc.storeLocation();
                                                
                                                // Store Last Geolocation as
                                                // Testlocation
                                                if (i == geoData.length() - 1)
                                                {
                                                    if (geoDataItem.has("geo_lat"))
                                                        test.getField("geo_lat").setField(geoDataItem);
                                                    
                                                    if (geoDataItem.has("geo_long"))
                                                        test.getField("geo_long").setField(geoDataItem);
                                                    
                                                    if (geoDataItem.has("accuracy"))
                                                        test.getField("geo_accuracy").setField(geoDataItem);
                                                    
                                                    if (geoDataItem.has("provider"))
                                                        test.getField("geo_provider").setField(geoDataItem);
                                                }
                                                
                                                if (geoloc.hasError())
                                                {
                                                    errorList.addError(geoloc.getError());
                                                    break;
                                                }
                                                
                                                }
                                                
                                            }
                                        
                                        final JSONArray cellData = request.optJSONArray("cellLocations");
                                        
                                        if (cellData != null && !test.hasError())
                                            for (int i = 0; i < cellData.length(); i++)
                                            {
                                                
                                                final JSONObject cellDataItem = cellData.getJSONObject(i);
                                                
                                                final Cell_location cellloc = new Cell_location(conn);
                                                
                                                cellloc.setTest_id(test.getUid());
                                                
                                                final long clientTime = cellDataItem.optLong("time");
                                                final Timestamp tstamp = java.sql.Timestamp.valueOf(new Timestamp(
                                                        clientTime).toString());
                                                
                                                cellloc.setTime(tstamp, test.getField("timezone").toString());
                                                cellloc.setLocation_id(cellDataItem.optInt("location_id", 0));
                                                cellloc.setArea_code(cellDataItem.optInt("area_code", 0));
                                                
                                                cellloc.setPrimary_scrambling_code(cellDataItem.optInt(
                                                        "primary_scrambling_code", 0));
                                                
                                                cellloc.storeLocation();
                                                
                                                if (cellloc.hasError())
                                                {
                                                    errorList.addError(cellloc.getError());
                                                    break;
                                                }
                                                
                                            }
                                        
                                        int signalStrength = Integer.MAX_VALUE;
                                        int linkSpeed = Integer.MAX_VALUE;
                                        final int networkType = test.getField("network_type").intValue();
                                        
                                        final JSONArray signalData = request.optJSONArray("signals");
                                        
                                        if (signalData != null && !test.hasError())
                                        {
                                            
                                            for (int i = 0; i < signalData.length(); i++)
                                            {
                                                
                                                final JSONObject signalDataItem = signalData.getJSONObject(i);
                                                
                                                final Signal signal = new Signal(conn);
                                                
                                                signal.setTest_id(test.getUid());
                                                
                                                final long clientTime = signalDataItem.optLong("time");
                                                final Timestamp tstamp = java.sql.Timestamp.valueOf(new Timestamp(
                                                        clientTime).toString());
                                                
                                                signal.setTime(tstamp, test.getField("timezone").toString());
                                                
                                                signal.setNetwork_type_id(signalDataItem.optInt("network_type_id", 0));
                                                final int thisSignalStrength = signalDataItem.optInt("signal_strength",
                                                        Integer.MAX_VALUE);
                                                if (thisSignalStrength != Integer.MAX_VALUE)
                                                    signal.setSignal_strength(thisSignalStrength);
                                                signal.setGsm_bit_error_rate(signalDataItem.optInt(
                                                        "gsm_bit_error_rate", 0));
                                                final int thisLinkSpeed = signalDataItem.optInt("wifi_link_speed", 0);
                                                signal.setWifi_link_speed(thisLinkSpeed);
                                                final int rssi = signalDataItem.optInt("wifi_rssi", Integer.MAX_VALUE);
                                                if (rssi != Integer.MAX_VALUE)
                                                    signal.setWifi_rssi(rssi);
                                                
                                                signal.storeSignal();
                                                
                                                if (networkType == 99)
                                                {
                                                    if (rssi < signalStrength && rssi != Integer.MIN_VALUE)
                                                        signalStrength = rssi;
                                                }
                                                else if (thisSignalStrength < signalStrength && thisSignalStrength != Integer.MIN_VALUE)
                                                    signalStrength = thisSignalStrength;
                                                
                                                if (thisLinkSpeed != 0 && thisLinkSpeed < linkSpeed)
                                                    linkSpeed = thisLinkSpeed;
                                                
                                                if (signal.hasError())
                                                {
                                                    errorList.addError(signal.getError());
                                                    break;
                                                }
                                                
                                            }
                                            
                                            if (signalStrength != Integer.MAX_VALUE
                                                    && signalStrength != Integer.MIN_VALUE
                                                    && signalStrength != 0) // 0 dBm is veeery unlikely - is an error in most cases
                                                ((IntField) test.getField("signal_strength")).setValue(signalStrength);
                                            
                                            if (linkSpeed != Integer.MAX_VALUE)
                                                ((IntField) test.getField("wifi_link_speed")).setValue(linkSpeed);
                                        }
                                        
                                        test.updateTest();
                                        
                                        /*
                                         * check for different types (e.g.
                                         * 2G/3G)
                                         */
                                        final String sqlAggSignal = "WITH agg AS"
                                                + " (SELECT array_agg(DISTINCT nt.group_name ORDER BY nt.group_name) agg"
                                                + " FROM signal s"
                                                + " JOIN network_type nt ON s.network_type_id=nt.uid WHERE test_id=?)"
                                                + " SELECT uid FROM agg JOIN network_type nt ON nt.aggregate=agg";
                                        
                                        final PreparedStatement ps = conn.prepareStatement(sqlAggSignal);
                                        ps.setLong(1, test.getUid());
                                        if (ps.execute())
                                        {
                                            final ResultSet rs = ps.getResultSet();
                                            if (rs.next())
                                            {
                                                final int newNetworkType = rs.getInt("uid");
                                                if (newNetworkType != 0)
                                                {
                                                    ((IntField) test.getField("network_type")).setValue(newNetworkType);
                                                    test.updateTest();
                                                }
                                            }
                                        }
                                        
                                        if (test.hasError())
                                            errorList.addError(test.getError());
                                        
                                    }
                                    else
                                        errorList.addError("ERROR_CLIENT_VERSION");
                            }
                            else
                                errorList.addError("ERROR_TEST_TOKEN_MALFORMED");
                        }
                        catch (final IllegalArgumentException e)
                        {
                            e.printStackTrace();
                            errorList.addError("ERROR_TEST_TOKEN_MALFORMED");
                        }
                        
                    }
                    else
                        errorList.addError("ERROR_TEST_TOKEN_MISSING");
                    
                }
                else
                    errorList.addError("ERROR_DB_CONNECTION");
                
            }
            catch (final JSONException e)
            {
                errorList.addError("ERROR_REQUEST_JSON");
                System.out.println("Error parsing JSDON Data " + e.toString());
            }
            catch (final SQLException e)
            {
                errorList.addError("ERROR_REQUEST_JSON");
                System.out.println("Error while storing data " + e.toString());
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
        
        return answer.toString();
    }
    
    @Get("json")
    public String retrieve(final String entity)
    {
        return request(entity);
    }
    
}
