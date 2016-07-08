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
package at.alladin.rmbt.controlServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import at.alladin.rmbt.db.Cell_location;
import at.alladin.rmbt.db.GeoLocation;
import at.alladin.rmbt.db.Signal;
import at.alladin.rmbt.db.Test;
import at.alladin.rmbt.db.fields.IntField;
import at.alladin.rmbt.db.fields.LongField;
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.model.SpeedItems;
import at.alladin.rmbt.shared.model.SpeedItems.SpeedItem;

import com.google.common.net.InetAddresses;

public class ResultResource extends ServerResource
{
    final static int UNKNOWN = Integer.MIN_VALUE;
    final static Pattern MCC_MNC_PATTERN = Pattern.compile("\\d{3}-\\d+");
    
    @Post("json")
    public String request(final String entity) 
    {
        addAllowOrigin();
        
        JSONObject request = null;
        
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        
        System.out.println(MessageFormat.format(labels.getString("NEW_RESULT"), getIP()));
        
        if (entity != null && !entity.isEmpty())
            // try parse the string to a JSON object
            try
            {
                request = new JSONObject(entity);
                //System.out.println(request);
                               
                final String lang = request.optString("client_language");
                
                // Load Language Files for Client
                
                final List<String> langs = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));
                
                if (langs.contains(lang))
                {
                    errorList.setLanguage(lang);
                    labels = ResourceManager.getSysMsgBundle(new Locale(lang));
                }
                
//                System.out.println(request.toString(4));
                
                if (conn != null)
                {
                    boolean oldAutoCommitState = conn.getAutoCommit();
                    conn.setAutoCommit(false);
                    
                    final Test test = new Test(conn);
                    
                    if (request.optString("test_token").length() > 0)
                    {
                        
                        final String[] token = request.getString("test_token").split("_");
                        
                        try
                        {
                            
                            final UUID testUuid = UUID.fromString(token[0]);
                            UUID openTestUuid=new java.util.UUID( 0L, 0L ); 
                            
                            // get open_test_uuid from test table
                            
                            final PreparedStatement psOpenUuid = conn.prepareStatement("SELECT open_test_uuid FROM test WHERE uuid = ?");
                            psOpenUuid.setObject(1, testUuid);
                            ResultSet rsOpenUuid = psOpenUuid.executeQuery();
                            if (rsOpenUuid.next())
                            	openTestUuid = (java.util.UUID) rsOpenUuid.getObject("open_test_uuid");
                            System.out.println("open_test_uuid: " + openTestUuid.toString());
                            
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
                                        
                                        final String networkOperator = request.optString("telephony_network_operator");
                                        if (MCC_MNC_PATTERN.matcher(networkOperator).matches())
                                            test.getField("network_operator").setString(networkOperator);
                                        else
                                            test.getField("network_operator").setString(null);
                                        
                                        final String networkSimOperator = request.optString("telephony_network_sim_operator");
                                        if (MCC_MNC_PATTERN.matcher(networkSimOperator).matches())
                                            test.getField("network_sim_operator").setString(networkSimOperator);
                                        else
                                            test.getField("network_sim_operator").setString(null);
                                        
                                        
                                        // RMBTClient Info
                                        
                                        final String ipLocalRaw = request.optString("test_ip_local", null);
                                        if (ipLocalRaw != null)
                                        {
                                            final InetAddress ipLocalAddress = InetAddresses.forString(ipLocalRaw);
                                            // original address (not filtered)
                                            test.getField("client_ip_local").setString(
                                            		InetAddresses.toAddrString(ipLocalAddress));
                                            // anonymized local address
                                            final String ipLocalAnonymized = Helperfunctions.anonymizeIp(ipLocalAddress);
                                            test.getField("client_ip_local_anonymized").setString(ipLocalAnonymized);
                                            // type of local ip
                                            test.getField("client_ip_local_type").setString(
                                                    Helperfunctions.IpType(ipLocalAddress));
                                            // public ip
                                            final InetAddress ipPublicAddress = InetAddresses.forString(test.getField("client_public_ip").toString());
                                            test.getField("nat_type")
                                                    .setString(Helperfunctions.getNatType(ipLocalAddress, ipPublicAddress));
                                        }
                                        
                                        final String ipServer = request.optString("test_ip_server", null);
                                        if (ipServer != null)
                                        {
                                            final InetAddress testServerInetAddress = InetAddresses.forString(ipServer);
                                            test.getField("server_ip").setString(
                                                    InetAddresses.toAddrString(testServerInetAddress));
                                        }
                                        
                                        //log IP address
                                        final String ipSource = getIP();
                                        test.getField("source_ip").setString(ipSource);
                                        
                                        //log anonymized address
                                        try{
                                        	final InetAddress ipSourceIP = InetAddress.getByName(ipSource);
                                            final String ipSourceAnonymized = Helperfunctions.anonymizeIp(ipSourceIP);
                                            test.getField("source_ip_anonymized").setString(ipSourceAnonymized);
                                        } catch(UnknownHostException e){
                                            System.out.println("Exception thrown:" + e);
                                        }
                                        

                                        //avoid null value on user_server_selection
                                        if (test.getField("user_server_selection").toString()!="true") 
                                           test.getField("user_server_selection").setString("false");
                                        
                                        
                                        // Additional Info
                                        
                                        JSONArray speedData = request.optJSONArray("speed_detail");
                                        
                                        if (speedData != null && !test.hasError())
                                        {
                                            
                                            // old implementation - extra table, JSON converted into SQL columns
                                            
                                            /*
                                                final PreparedStatement psSpeed = conn.prepareStatement("INSERT INTO test_speed (test_id, upload, thread, time, bytes) VALUES (?,?,?,?,?)");
                                                psSpeed.setLong(1, test.getUid());
                                                for (int i = 0; i < speedData.length(); i++)
                                                {
                                                    final JSONObject item = speedData.getJSONObject(i);
                                                    
                                                    final String direction = item.optString("direction");
                                                    if (direction != null && (direction.equals("download") || direction.equals("upload")))
                                                    {
                                                        psSpeed.setBoolean(2, direction.equals("upload"));
                                                        psSpeed.setInt(3, item.optInt("thread"));
                                                        psSpeed.setLong(4, item.optLong("time"));
                                                        psSpeed.setLong(5, item.optLong("bytes"));
                                                        
                                                        psSpeed.executeUpdate();
                                                    }
                                                }
                                            */
                                            
                                            
                                            // next implementation - JSON result as JSON string within the test table
                                            
                                            final SpeedItems speedItems = new SpeedItems();
                                            for (int i = 0; i < speedData.length(); i++)
                                            {
                                                final JSONObject item = speedData.getJSONObject(i);
                                                final String direction = item.optString("direction");
                                                if (direction != null && (direction.equals("download") || direction.equals("upload")))
                                                {
                                                    final boolean upload = direction.equals("upload");
                                                    final int thread = item.optInt("thread");
                                                    final SpeedItem speedItem = new SpeedItem(item.optLong("time"), item.optLong("bytes"));
                                                    
                                                    if (upload)
                                                        speedItems.addSpeedItemUpload(speedItem, thread);
                                                    else
                                                        speedItems.addSpeedItemDownload(speedItem, thread);
                                                }
                                            }
                                            final String speedItemsJson = getGson(false).toJson(speedItems);
                                            
                                            //to be deleted when migration is finished (no speed items in test table any more)
                                            test.getField("speed_items").setString(speedItemsJson);
                                            
                                            // current implementation - JSON result as binary JSONB in extra table 
                                            
                                            // reuses speedItemsJson prepared above

                                            final PreparedStatement psSpeed = conn.prepareStatement("INSERT INTO speed (open_test_uuid,items) VALUES (?,?::JSONB)");
                                            psSpeed.setObject(1,openTestUuid);
                                            psSpeed.setString(2,speedItemsJson);
                                            psSpeed.executeUpdate();
  
                                        }
                                        
                                        final JSONArray pingData = request.optJSONArray("pings");
                                        
                                        if (pingData != null && !test.hasError())
                                        {
                                            final PreparedStatement psPing = conn.prepareStatement("INSERT INTO ping (open_test_uuid,test_id, value, value_server, time_ns) " + "VALUES(?,?,?,?,?)");
                                            psPing.setObject(1,openTestUuid);
                                            psPing.setLong(2, test.getUid());
                                            
                                            for (int i = 0; i < pingData.length(); i++)
                                            {
                                                
                                                final JSONObject pingDataItem = pingData.getJSONObject(i);
                                                
                                                long valueClient = pingDataItem.optLong("value", -1);
                                                if (valueClient >= 0)
                                                    psPing.setLong(3, valueClient);
                                                else
                                                    psPing.setNull(3, Types.BIGINT);
                                                
                                                long valueServer = pingDataItem.optLong("value_server", -1);
                                                if (valueServer >= 0)
                                                    psPing.setLong(4, valueServer);
                                                else
                                                    psPing.setNull(4, Types.BIGINT);
                                                
                                                long timeNs = pingDataItem.optLong("time_ns", -1);
                                                if (timeNs >= 0)
                                                    psPing.setLong(5, timeNs);
                                                else
                                                    psPing.setNull(5, Types.BIGINT);

                                                
                                                psPing.executeUpdate();
                                            }
                                        }
                                        
                                        final JSONArray geoData = request.optJSONArray("geoLocations");
                                        
                                        if (geoData != null && !test.hasError())
                                            for (int i = 0; i < geoData.length(); i++)
                                            {
                                                
                                                final JSONObject geoDataItem = geoData.getJSONObject(i);
                                                
                                                if (geoDataItem.optLong("tstamp", 0) != 0 && geoDataItem.optDouble("geo_lat", 0) != 0 && geoDataItem.optDouble("geo_long", 0) != 0) {
                                                
                                                final GeoLocation geoloc = new GeoLocation(conn);
                                                
                                                geoloc.setOpenTestUuid(openTestUuid);
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
                                                geoloc.setTime_ns(geoDataItem.optLong("time_ns", 0));
                                                
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
                                                
                                                cellloc.setOpenTestUuid(openTestUuid);
                                                cellloc.setTest_id(test.getUid());
                                                
                                                final long clientTime = cellDataItem.optLong("time");
                                                final Timestamp tstamp = java.sql.Timestamp.valueOf(new Timestamp(
                                                        clientTime).toString());
                                                
                                                cellloc.setTime(tstamp, test.getField("timezone").toString());
                                                
                                                cellloc.setTime_ns(cellDataItem.optLong("time_ns", 0));
                                                
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
                                        
                                        int signalStrength = Integer.MAX_VALUE; //measured as RSSI (GSM,UMTS,Wifi)
                                        int lteRsrp = Integer.MAX_VALUE; // signal strength measured as RSRP
                                        int lteRsrq = Integer.MAX_VALUE; // signal quality of LTE measured as RSRQ
                                        int linkSpeed = UNKNOWN;
                                        final int networkType = test.getField("network_type").intValue();
                                        
                                        final JSONArray signalData = request.optJSONArray("signals");
                                        
                                        if (signalData != null && !test.hasError())
                                        {
                                            
                                            for (int i = 0; i < signalData.length(); i++)
                                            {
                                                
                                                final JSONObject signalDataItem = signalData.getJSONObject(i);
                                                
                                                final Signal signal = new Signal(conn);
                                                
                                                signal.setOpenTestUuid(openTestUuid);
                                                signal.setTest_id(test.getUid());
                                                
                                                final long clientTime = signalDataItem.optLong("time");
                                                final Timestamp tstamp = java.sql.Timestamp.valueOf(new Timestamp(
                                                        clientTime).toString());
                                                
                                                signal.setTime(tstamp, test.getField("timezone").toString());
                                                
                                                final int thisNetworkType = signalDataItem.optInt("network_type_id", 0);
                                                signal.setNetwork_type_id(thisNetworkType);
                                                
                                                final int thisSignalStrength = signalDataItem.optInt("signal_strength",
                                                        UNKNOWN);
                                                if (thisSignalStrength != UNKNOWN)
                                                    signal.setSignal_strength(thisSignalStrength);
                                                signal.setGsm_bit_error_rate(signalDataItem.optInt(
                                                        "gsm_bit_error_rate", 0));
                                                final int thisLinkSpeed = signalDataItem.optInt("wifi_link_speed", 0);
                                                signal.setWifi_link_speed(thisLinkSpeed);
                                                final int rssi = signalDataItem.optInt("wifi_rssi", UNKNOWN);
                                                if (rssi != UNKNOWN)
                                                    signal.setWifi_rssi(rssi);
                                                
                                                lteRsrp = signalDataItem.optInt("lte_rsrp", UNKNOWN);
                                                lteRsrq = signalDataItem.optInt("lte_rsrq", UNKNOWN);
                                                final int lteRssnr = signalDataItem.optInt("lte_rssnr", UNKNOWN);
                                                final int lteCqi = signalDataItem.optInt("lte_cqi", UNKNOWN);
                                                final long timeNs = signalDataItem.optLong("time_ns", UNKNOWN);
                                                signal.setLte_rsrp(lteRsrp);
                                                signal.setLte_rsrq(lteRsrq);
                                                signal.setLte_rssnr(lteRssnr);
                                                signal.setLte_cqi(lteCqi);
                                                signal.setTime_ns(timeNs);
                                                
                                                signal.storeSignal();
                                                
                                                if (networkType == 99) // wlan
                                                {
                                                    if (rssi < signalStrength && rssi != UNKNOWN)
                                                        signalStrength = rssi;
                                                }
                                                else if (thisSignalStrength < signalStrength && thisSignalStrength != UNKNOWN)
                                                    signalStrength = thisSignalStrength;
                                                
                                                if (thisLinkSpeed != 0 && (linkSpeed == UNKNOWN || thisLinkSpeed < linkSpeed))
                                                    linkSpeed = thisLinkSpeed;
                                                
                                                if (signal.hasError())
                                                {
                                                    errorList.addError(signal.getError());
                                                    break;
                                                }
                                                
                                            }
                                            // set rssi value (typically GSM,UMTS, but also old LTE-phones)
                                            if (signalStrength != Integer.MAX_VALUE
                                                    && signalStrength != UNKNOWN
                                                    && signalStrength != 0) // 0 dBm is out of range
                                                ((IntField) test.getField("signal_strength")).setValue(signalStrength);
                                            // set rsrp value (typically LTE)
                                            if (lteRsrp != Integer.MAX_VALUE
                                                    && lteRsrp != UNKNOWN
                                                    && lteRsrp != 0) // 0 dBm is out of range
                                                ((IntField) test.getField("lte_rsrp")).setValue(lteRsrp);
                                            // set rsrq value (LTE)
                                            if (lteRsrq != Integer.MAX_VALUE
                                                    && lteRsrq != UNKNOWN)
                                                ((IntField) test.getField("lte_rsrq")).setValue(lteRsrq);
                                            
                                            if (linkSpeed != Integer.MAX_VALUE && linkSpeed != UNKNOWN)
                                                ((IntField) test.getField("wifi_link_speed")).setValue(linkSpeed);
                                        }
                                        
                                        // use max network type
                                        
                                        final String sqlMaxNetworkType = "SELECT nt.uid"
                                                + " FROM signal s"
                                                + " JOIN network_type nt"
                                                + " ON s.network_type_id=nt.uid"
                                                + " WHERE test_id=?"
                                                + " ORDER BY nt.technology_order DESC"
                                                + " LIMIT 1";

                                        final PreparedStatement psMaxNetworkType = conn.prepareStatement(sqlMaxNetworkType);
                                        psMaxNetworkType.setLong(1, test.getUid());
                                        if (psMaxNetworkType.execute())
                                        {
                                            final ResultSet rs = psMaxNetworkType.getResultSet();
                                            if (rs.next())
                                            {
                                                final int maxNetworkType = rs.getInt("uid");
                                                if (maxNetworkType != 0)
                                                    ((IntField) test.getField("network_type")).setValue(maxNetworkType);
                                            }
                                        }
                                        
                                        /*
                                         * check for different types (e.g.
                                         * 2G/3G)
                                         */
                                        final String sqlAggSignal = "WITH agg AS"
                                                + " (SELECT array_agg(DISTINCT nt.group_name ORDER BY nt.group_name) agg"
                                                + " FROM signal s"
                                                + " JOIN network_type nt ON s.network_type_id=nt.uid WHERE test_id=?)"
                                                + " SELECT uid FROM agg JOIN network_type nt ON nt.aggregate=agg";
                                        
                                        final PreparedStatement psAgg = conn.prepareStatement(sqlAggSignal);
                                        psAgg.setLong(1, test.getUid());
                                        if (psAgg.execute())
                                        {
                                            final ResultSet rs = psAgg.getResultSet();
                                            if (rs.next())
                                            {
                                                final int newNetworkType = rs.getInt("uid");
                                                if (newNetworkType != 0)
                                                    ((IntField) test.getField("network_type")).setValue(newNetworkType);
                                            }
                                        }
                                        
                                        ///////// android_permissions
                                        final JSONArray androidPermissionStatus = request.optJSONArray("android_permission_status");
                                        String androidPermissionStatusString = null;
                                        if (androidPermissionStatus != null)
                                        {
                                            androidPermissionStatusString = androidPermissionStatus.toString();
                                            if (androidPermissionStatusString.length() > 1000) // sanity check
                                                androidPermissionStatusString = null;
                                        }
                                        
                                        test.getField("android_permissions").setString(androidPermissionStatusString);
                                        ///////////
                                        
                                        
                                        if (test.getField("network_type").intValue() <= 0)
                                            errorList.addError("ERROR_NETWORK_TYPE");
                                        
                                        final IntField downloadField = (IntField) test.getField("speed_download");
                                        if (downloadField.isNull() || downloadField.intValue() <= 0 || downloadField.intValue() > 10000000) // 10 gbit/s limit
                                            errorList.addError("ERROR_DOWNLOAD_INSANE");
                                        
                                        final IntField upField = (IntField) test.getField("speed_upload");
                                        if (upField.isNull() || upField.intValue() <= 0 || upField.intValue() > 10000000) // 10 gbit/s limit
                                            errorList.addError("ERROR_UPLOAD_INSANE");
                                        
                                        //clients still report eg: "test_ping_shortest":9195040 (note the 'test_' prefix there!)
                                        final LongField pingField = (LongField) test.getField("ping_shortest");
                                        if (pingField.isNull() || pingField.longValue() <= 0 || pingField.longValue() > 60000000000L) // 1 min limit
                                            errorList.addError("ERROR_PING_INSANE");
                                        
                                        
                                        if (errorList.isEmpty())
                                            test.getField("status").setString("FINISHED");
                                        else
                                            test.getField("status").setString("ERROR");
                                        
                                        test.storeTestResults(false);
                                        
                                        if (test.hasError())
                                            errorList.addError(test.getError());
                                        
                                    }
                                    else
                                        errorList.addError("ERROR_CLIENT_VERSION");
                            }
                        }
                        catch (final IllegalArgumentException e)
                        {
                            e.printStackTrace();
                            errorList.addError("ERROR_TEST_TOKEN_MALFORMED");
                        }
                        
                    }
                    else
                        errorList.addError("ERROR_TEST_TOKEN_MISSING");
                                        
                    conn.commit();
                    conn.setAutoCommit(oldAutoCommitState); // be nice and restore old state TODO: do it in finally
                }
                else
                    errorList.addError("ERROR_DB_CONNECTION");
                
            }
            catch (final JSONException e)
            {
                errorList.addError("ERROR_REQUEST_JSON");
                System.out.println("Error parsing JSDON Data " + e.toString());
                e.printStackTrace();
            }
            catch (final SQLException e)
            {
                System.out.println("Error while storing data " + e.toString());
                e.printStackTrace();
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
