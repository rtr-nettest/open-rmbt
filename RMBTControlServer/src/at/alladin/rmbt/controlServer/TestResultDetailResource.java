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

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import at.alladin.rmbt.db.Client;
import at.alladin.rmbt.db.Test;
import at.alladin.rmbt.db.TestNdt;
import at.alladin.rmbt.db.fields.Field;
import at.alladin.rmbt.db.fields.TimestampField;
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.SignificantFormat;

public class TestResultDetailResource extends ServerResource
{
	
    private JSONObject addObject(final JSONArray array, final String key) throws JSONException
    {
        final JSONObject newObject = new JSONObject();
        newObject.put("title", getKeyTranslation(key));
        array.put(newObject);
        return newObject;
    }
    
    private void addString(final JSONArray array, final String title, final String value) throws JSONException
    {
        if (value != null && !value.isEmpty())
            addObject(array, title).put("value", value);
    }
    
    private void addString(final JSONArray array, final String title, final Field field) throws JSONException
    {
        if (!field.isNull())
            addString(array, title, field.toString());
    }
    
    private void addInt(final JSONArray array, final String title, final Field field) throws JSONException
    {
        if (!field.isNull())
            addObject(array, title).put("value", field.intValue());
    }
    
    private String getKeyTranslation(final String key)
    {
        try
        {
            return labels.getString("key_" + key);
        }
        catch (final MissingResourceException e)
        {
            return key;
        }
    }
    
    @Post("json")
    public String request(final String entity)
    {
    	long startTime = System.currentTimeMillis();
        addAllowOrigin();
        
        JSONObject request = null;
        
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;
        
        final String clientIpRaw = getIP();
        System.out.println(MessageFormat.format(labels.getString("NEW_TESTRESULT_DETAIL"), clientIpRaw));
        
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
                    
                    final Client client = new Client(conn);
                    final Test test = new Test(conn);
                    TestNdt ndt = new TestNdt(conn);
                    
                    final String testUuid = request.optString("test_uuid");
                    if (testUuid != null && test.getTestByUuid(UUID.fromString(testUuid)) > 0
                            && client.getClientByUid(test.getField("client_id").intValue()))
                    {
                        
                        if (!ndt.loadByTestId(test.getUid()))
                            ndt = null;
                        
                        final Locale locale = new Locale(lang);
                        final Format format = new SignificantFormat(2, locale);
                        
                        final JSONArray resultList = new JSONArray();

                        boolean dualSim = false;
                    	final Field dualSimField = test.getField("dual_sim");
                        if (! dualSimField.isNull() && (dualSimField.toString() == "true"))
                        	  dualSim = true;
                        
                        final JSONObject singleItem = addObject(resultList, "time");
                        final Field timeField = test.getField("time");
                        if (!timeField.isNull()) {
                        	final Date date = ((TimestampField) timeField).getDate();
                        	final long time = date.getTime();
                        	singleItem.put("time", time); //csv 3

                        	final Field timezoneField = test.getField("timezone");
                        	if (!timezoneField.isNull()) {
                        		final String tzString = timezoneField.toString();
                        		final TimeZone tz = TimeZone.getTimeZone(timezoneField.toString());
                        		singleItem.put("timezone", tzString);


                        		final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                        				DateFormat.MEDIUM, locale);
                        		dateFormat.setTimeZone(tz);
                        		singleItem.put("value", dateFormat.format(date));

                        		final Format tzFormat = new DecimalFormat("+0.##;-0.##", new DecimalFormatSymbols(locale));

                        		final float offset = tz.getOffset(time) / 1000f / 60f / 60f;
                        		addString(resultList, "timezone", String.format("UTC%sh", tzFormat.format(offset)));
                        	}
                        }

                        // speed download in Mbit/s (converted from kbit/s) - csv 10 (in kbit/s)
                        final Field downloadField = test.getField("speed_download");
                        if (!downloadField.isNull()) {
                        	final String download = format.format(downloadField.doubleValue() / 1000d);
                        	addString(resultList, "speed_download",
                        			String.format("%s %s", download, labels.getString("RESULT_DOWNLOAD_UNIT")));
                        }
                        
                        // speed upload im MBit/s (converted from kbit/s) - csv 11 (in kbit/s)
                        final Field uploadField = test.getField("speed_upload");
                        if (!uploadField.isNull()) {
                        	final String upload = format.format(uploadField.doubleValue() / 1000d);
                        	addString(resultList, "speed_upload",
                        			String.format("%s %s", upload, labels.getString("RESULT_UPLOAD_UNIT")));
                        }
                        
                        // median ping in ms
                        final Field pingMedianField = test.getField("ping_median");
                        if (!pingMedianField.isNull()) {
                        	final String pingMedian = format.format(pingMedianField.doubleValue() / 1000000d);
                        	addString(resultList, "ping_median",
                        			String.format("%s %s", pingMedian, labels.getString("RESULT_PING_UNIT")));
                        }
                        
                        // signal strength RSSI in dBm - csv 13
                        final Field signalStrengthField = test.getField("signal_strength");
                        if (!dualSim && !signalStrengthField.isNull())
                            addString(
                                    resultList,
                                    "signal_strength",
                                    String.format("%d %s", signalStrengthField.intValue(),
                                            labels.getString("RESULT_SIGNAL_UNIT")));

                        //signal strength RSRP in dBm (LTE) - csv 29
                        final Field lteRsrpField = test.getField("lte_rsrp");
                        if (!dualSim && !lteRsrpField.isNull())
                            addString(
                                    resultList,
                                    "signal_rsrp",
                                    String.format("%d %s", lteRsrpField.intValue(),
                                            labels.getString("RESULT_SIGNAL_UNIT")));

                        //signal quality in LTE, RSRQ in dB
                        final Field lteRsrqField = test.getField("lte_rsrq");
                        if (!dualSim && !lteRsrqField.isNull())
                            addString(
                                    resultList,
                                    "signal_rsrq",
                                    String.format("%d %s", lteRsrqField.intValue(),
                                            labels.getString("RESULT_DB_UNIT")));
                      
                        // network, eg. "3G (HSPA+)
                        //TODO fix helper-function
                        final Field networkTypeField = test.getField("network_type");
                        if (!dualSim && !networkTypeField.isNull())
                        	addString(resultList, "network_type",
                        			Helperfunctions.getNetworkTypeName(networkTypeField.intValue()));
                        
                        // geo-location
                        JSONObject locationJson = getGeoLocation(test, settings, conn, labels);
                        
                        if (locationJson != null) {
                        	if (locationJson.has("location")) {
                        		addString(resultList, "location", locationJson.getString("location"));
                        	}
                        	if (locationJson.has("country_location")) {
                        		addString(resultList, "country_location", locationJson.getString("country_location"));
                        	}
                        	if (locationJson.has("motion")) {
                        		addString(resultList, "motion", locationJson.getString("motion"));
                        	}                        	
                        }

                        // country derived from AS registry
                        final Field countryAsnField = test.getField("country_asn");
                        if (!countryAsnField.isNull()) 
                            addString(resultList, "country_asn", countryAsnField.toString());

                        // country derived from geo-IP database
                        final Field countryGeoipField = test.getField("country_geoip");
                        if (!countryGeoipField.isNull())
                            addString(resultList, "country_geoip", countryGeoipField.toString());

                        final Field zipCodeField = test.getField("zip_code");
                        if (!zipCodeField.isNull())
                        {
                            final String zipCode = zipCodeField.toString();
                            final int zipCodeInt = zipCodeField.intValue();
                            if (zipCodeInt > 999 || zipCodeInt <= 9999)  // plausibility of zip code (must be 4 digits in Austria)
                            	addString(resultList, "zip_code", zipCode);
                        }
                        
                        /*
                        final Field gkzField = test.getField("gkz");
                        if (!gkzField.isNull())
                        {
                            	addString(resultList, "gkz", gkzField.toString());
                        }
                        */
                        
                        final Field communityField = test.getField("community");
                        if (!communityField.isNull())
                        {
                            	addString(resultList, "community", communityField.toString());
                        }

                        final Field cov800catField = test.getField("cov800cat");
                        if (!cov800catField.isNull())
                        {
                            	addString(resultList, "cov800cat", cov800catField.toString());
                        }


                        final Field districtField = test.getField("district");
                        if (!districtField.isNull())
                        {
                            	addString(resultList, "district", districtField.toString());
                        }

                        final Field provinceField = test.getField("province");
                        if (!provinceField.isNull())
                        {
                            	addString(resultList, "province", provinceField.toString());
                        }

                        
                        // public client ip (private)
                        addString(resultList, "client_public_ip", test.getField("client_public_ip"));

                        // AS number - csv 24
                        addString(resultList, "client_public_ip_as", test.getField("public_ip_asn"));

                        // name of AS
                        addString(resultList, "client_public_ip_as_name", test.getField("public_ip_as_name"));

                        // reverse hostname (from ip) - (private)
                        addString(resultList, "client_public_ip_rdns", test.getField("public_ip_rdns"));
                        
                        // operator - derived from provider_id (only for pre-defined operators)
                        //TODO replace provider-information by more generic information
                        addString(resultList, "provider", test.getField("provider_id_name"));
                        
                        // type of client local ip (private)
                        addString(resultList, "client_local_ip", test.getField("client_ip_local_type"));
                        
                        // nat-translation of client - csv 23
                        addString(resultList, "nat_type", test.getField("nat_type"));
                        
                        // wifi base station id SSID (numberic) eg 01:2c:3d..
                        addString(resultList, "wifi_ssid", test.getField("wifi_ssid"));
                        // wifi base station id - BSSID (text) eg 'my hotspot'
                        addString(resultList, "wifi_bssid", test.getField("wifi_bssid"));
                        
                        // nominal link speed of wifi connection in MBit/s
                        final Field linkSpeedField = test.getField("wifi_link_speed");
                        if (!linkSpeedField.isNull())
                            addString(
                                    resultList,
                                    "wifi_link_speed",
                                    String.format("%s %s", linkSpeedField.toString(),
                                            labels.getString("RESULT_WIFI_LINK_SPEED_UNIT")));
                        // name of mobile network operator (eg. 'T-Mobile AT')
                        if (!dualSim)
                           addString(resultList, "network_operator_name", test.getField("network_operator_name"));
                        
                        // mobile network name derived from MCC/MNC of network, eg. '232-01'
                        final Field networkOperatorField = test.getField("network_operator");
                        
                        
                        if (!dualSim)
                        {   // mobile provider name, eg. 'Hutchison Drei' (derived from mobile_provider_id)
                        	final Field mobileProviderNameField = test.getField("mobile_provider_name");
                        	if (mobileProviderNameField.isNull()) // eg. '248-02'
                        		addString(resultList, "network_operator", networkOperatorField);
                        	else
                        	{
                        		if (networkOperatorField.isNull())
                        			addString(resultList, "network_operator", mobileProviderNameField);
                        		else // eg. 'Hutchison Drei (232-10)'
                        			addString(resultList, "network_operator",
                        					String.format("%s (%s)", mobileProviderNameField, networkOperatorField));
                        	}

                        	addString(resultList, "network_sim_operator_name", test.getField("network_sim_operator_name"));

                        	final Field networkSimOperatorField = test.getField("network_sim_operator");
                        	final Field networkSimOperatorTextField = test.getField("network_sim_operator_mcc_mnc_text");
                        	if (networkSimOperatorTextField.isNull())
                        		addString(resultList, "network_sim_operator", networkSimOperatorField);
                        	else
                        		addString(resultList, "network_sim_operator",
                        				String.format("%s (%s)", networkSimOperatorTextField, networkSimOperatorField));

                        	final Field roamingTypeField = test.getField("roaming_type");
                        	if (!roamingTypeField.isNull())
                        		addString(resultList, "roaming", Helperfunctions.getRoamingType(labels, roamingTypeField.intValue()));
                        } //dualSim
                        
                        final long totalDownload = test.getField("total_bytes_download").longValue();
                        final long totalUpload = test.getField("total_bytes_upload").longValue();
                        final long totalBytes = totalDownload + totalUpload;
                        if (totalBytes > 0)
                        {
                            final String totalBytesString = format.format(totalBytes / (1000d * 1000d));
                            addString(
                                    resultList,
                                    "total_bytes",
                                    String.format("%s %s", totalBytesString,
                                            labels.getString("RESULT_TOTAL_BYTES_UNIT")));
                        }
                        
                        // interface volumes - total including control-server and pre-tests (and other tests)
                        final long totalIfDownload = test.getField("test_if_bytes_download").longValue();
                        final long totalIfUpload = test.getField("test_if_bytes_upload").longValue();
                        // output only total of down- and upload
                        final long totalIfBytes = totalIfDownload + totalIfUpload;
                        if (totalIfBytes > 0)
                        {
                            final String totalIfBytesString = format.format(totalIfBytes / (1000d * 1000d));
                            addString(
                                    resultList,
                                    "total_if_bytes",
                                    String.format("%s %s", totalIfBytesString,
                                            labels.getString("RESULT_TOTAL_BYTES_UNIT")));
                        }
                        // interface volumes during test
                        // download test - volume in download direction
                        final long testDlIfBytesDownload = test.getField("testdl_if_bytes_download").longValue();
                        if (testDlIfBytesDownload > 0l) {
                        	final String testDlIfBytesDownloadString = format.format(testDlIfBytesDownload / (1000d * 1000d));
                        	addString(
                        			resultList,
                        			"testdl_if_bytes_download",
                        			String.format("%s %s", testDlIfBytesDownloadString,
                        					labels.getString("RESULT_TOTAL_BYTES_UNIT")));
                        }
                        // download test - volume in upload direction
                        final long testDlIfBytesUpload = test.getField("testdl_if_bytes_upload").longValue();
                        if (testDlIfBytesUpload > 0l) {
                        	final String testDlIfBytesUploadString = format.format(testDlIfBytesUpload / (1000d * 1000d));
                        	addString(
                        			resultList,
                        			"testdl_if_bytes_upload",
                        			String.format("%s %s", testDlIfBytesUploadString,
                        					labels.getString("RESULT_TOTAL_BYTES_UNIT")));
                        }
                        // upload test - volume in upload direction
                        final long testUlIfBytesUpload = test.getField("testul_if_bytes_upload").longValue();
                        if (testUlIfBytesUpload > 0l) {
                        	final String testUlIfBytesUploadString = format.format(testUlIfBytesUpload / (1000d * 1000d));
                        	addString(
                        			resultList,
                        			"testul_if_bytes_upload",
                        			String.format("%s %s", testUlIfBytesUploadString,
                        					labels.getString("RESULT_TOTAL_BYTES_UNIT")));
                        }
                        // upload test - volume in download direction
                        final long testUlIfBytesDownload = test.getField("testul_if_bytes_download").longValue();
                        if (testDlIfBytesDownload > 0l) {
                        	final String testUlIfBytesDownloadString = format.format(testUlIfBytesDownload / (1000d * 1000d));
                        	addString(
                        			resultList,
                        			"testul_if_bytes_download",
                        			String.format("%s %s", testUlIfBytesDownloadString,
                        					labels.getString("RESULT_TOTAL_BYTES_UNIT")));
                        }

                        //start time download-test 
                        final Field time_dl_ns = test.getField("time_dl_ns");
                        if (!time_dl_ns.isNull()) {                   
                        addString(resultList,"time_dl",
                                    String.format("%s %s", format.format(time_dl_ns.doubleValue() / 1000000000d),  //convert ns to s
                                            labels.getString("RESULT_DURATION_UNIT")));
                        }
                        
                        //duration download-test 
                        final Field duration_download_ns = test.getField("nsec_download");
                        if (!duration_download_ns.isNull()) {                   
                        addString(resultList,"duration_dl",
                                    String.format("%s %s", format.format(duration_download_ns.doubleValue() / 1000000000d),  //convert ns to s
                                            labels.getString("RESULT_DURATION_UNIT")));
                        }

                        //start time upload-test 
                        final Field time_ul_ns = test.getField("time_ul_ns");
                        if (!time_ul_ns.isNull()) {                   
                        addString(resultList,"time_ul",
                                    String.format("%s %s", format.format(time_ul_ns.doubleValue() / 1000000000d),  //convert ns to s
                                            labels.getString("RESULT_DURATION_UNIT")));
                        }
                        
                        //duration upload-test 
                        final Field duration_upload_ns = test.getField("nsec_upload");
                        if (!duration_upload_ns.isNull()) {                   
                        addString(resultList,"duration_ul",
                                    String.format("%s %s", format.format(duration_upload_ns.doubleValue() / 1000000000d),  //convert ns to s
                                            labels.getString("RESULT_DURATION_UNIT")));
                        }
                   
                        if (ndt != null)
                        {
                            final String downloadNdt = format.format(ndt.getField("s2cspd").doubleValue());
                            addString(resultList, "speed_download_ndt",
                                    String.format("%s %s", downloadNdt, labels.getString("RESULT_DOWNLOAD_UNIT")));
                            
                            final String uploaddNdt = format.format(ndt.getField("c2sspd").doubleValue());
                            addString(resultList, "speed_upload_ndt",
                                    String.format("%s %s", uploaddNdt, labels.getString("RESULT_UPLOAD_UNIT")));
                            
                            // final String pingNdt =
                            // format.format(ndt.getField("avgrtt").doubleValue());
                            // addString(resultList, "ping_ndt",
                            // String.format("%s %s", pingNdt,
                            // labels.getString("RESULT_PING_UNIT")));
                        }
                        
                        addString(resultList, "server_name", test.getField("server_name"));
                        addString(resultList, "plattform", test.getField("plattform"));
                        addString(resultList, "os_version", test.getField("os_version"));
                        addString(resultList, "model", test.getField("model_fullname"));
                        addString(resultList, "client_name", test.getField("client_name"));
                        addString(resultList, "client_software_version", test.getField("client_software_version"));
                        final String encryption = test.getField("encryption").toString();
                        
                        if (encryption != null)
                        {
                            addString(
                                    resultList,
                                    "encryption",
                                    "NONE".equals(encryption) ? labels
                                            .getString("key_encryption_false") : labels.getString("key_encryption_true"));
                        }
                        
                        addString(resultList, "client_version", test.getField("client_version"));
                                               
                        addString(
                                resultList,
                                "duration",
                                String.format("%d %s", test.getField("duration").intValue(),
                                        labels.getString("RESULT_DURATION_UNIT")));
 
                        // number of threads for download-test
                        final Field num_threads = test.getField("num_threads");
                        if (!num_threads.isNull()) {                   
                        	addInt(resultList,"num_threads",num_threads);
                        }
                       
                        //number of threads for upload-test
                        final Field num_threads_ul = test.getField("num_threads_ul");
                        if (!num_threads_ul.isNull()) {                   
                        	addInt(resultList,"num_threads_ul",num_threads_ul);
                        }
                    
                        //dz 2013-11-09 removed UUID from details as users might get confused by two
                        //              ids;
                        //addString(resultList, "uuid", String.format("T%s", test.getField("uuid")));
                        
                        final Field openTestUUIDField = test.getField("open_test_uuid");
                        if (! openTestUUIDField.isNull())
                            addString(resultList, "open_test_uuid", String.format("O%s", openTestUUIDField));

                        final Field openUUIDField = test.getField("open_uuid");
                        if (! openUUIDField.isNull())
                            addString(resultList, "open_uuid", String.format("P%s", openUUIDField));
          
                        //todo: Add "user_server_selection" Flag
                        
                        //number of threads for upload-test
                        final Field tag = test.getField("tag");
                        if (!tag.isNull()) {                   
                        	addString(resultList,"tag",tag);
                        }
                        
                        if (ndt != null)
                        {
                            addString(resultList, "ndt_details_main", ndt.getField("main"));
                            addString(resultList, "ndt_details_stat", ndt.getField("stat"));
                            addString(resultList, "ndt_details_diag", ndt.getField("diag"));
                        }
                        
                        if (resultList.length() == 0)
                            errorList.addError("ERROR_DB_GET_TESTRESULT_DETAIL");
                        
                        answer.put("testresultdetail", resultList);
                    }
                    else
                        errorList.addError("ERROR_REQUEST_TEST_RESULT_DETAIL_NO_UUID");

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
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println(MessageFormat.format(labels.getString("NEW_TESTRESULT_DETAIL_SUCCESS"), clientIpRaw, Long.toString(elapsedTime)));
        
        return answerString;
    }
    
    @Get("json")
    public String retrieve(final String entity)
    {
        return request(entity);
    }

    /**
     * 
     * @param test
     * @param settings
     * @param conn
     * @return
     * @throws JSONException
     */
    public static JSONObject getGeoLocation(Test test, ResourceBundle settings, Connection conn, ResourceBundle labels) throws JSONException {
    	JSONObject json = new JSONObject(); 
        // geo-location
        final Field latField = test.getField("geo_lat"); //csv 6
        final Field longField = test.getField("geo_long"); //csv 7
        final Field accuracyField = test.getField("geo_accuracy"); 
        final Field providerField = test.getField("geo_provider"); //csv 8
        if (!(latField.isNull() || longField.isNull() || accuracyField.isNull()))
        {
            final double accuracy = accuracyField.doubleValue();
            if (accuracy < Double.parseDouble(settings.getString("RMBT_GEO_ACCURACY_DETAIL_LIMIT")))
            {
                final StringBuilder geoString = new StringBuilder(Helperfunctions.geoToString(latField.doubleValue(),
                        longField.doubleValue()));
                
                geoString.append(" (");
                if (! providerField.isNull())
                {
                	String provider = providerField.toString().toUpperCase(Locale.US);
                	
                	switch(provider) {
                		case "NETWORK":
                			provider = labels.getString("key_geo_source_network");
                			break;
                		case "GPS":
                			provider = labels.getString("key_geo_source_gps");
                			break;
                	}
                	
                    geoString.append(provider);
                    geoString.append(", ");
                }
                geoString.append(String.format(Locale.US, "+/- %.0f m", accuracy));
                geoString.append(")");
                json.put("location", geoString.toString());
                
                //also try getting the distance during the test
                final Date clientDate = ((TimestampField) test.getField("client_time")).getDate();
                final long clientTime = clientDate.getTime();
                try {
                	OpenTestResource.LocationGraph locGraph = new OpenTestResource.LocationGraph(test.getUid(), clientTime, conn);
                	if ((locGraph.getTotalDistance() > 0) &&
                	        locGraph.getTotalDistance() <= Double.parseDouble(settings.getString("RMBT_GEO_DISTANCE_DETAIL_LIMIT"))) {
                		json.put("motion", Math.round(locGraph.getTotalDistance()) + " m");
                	}

                } catch (SQLException ex) {
                	//cannot happen since the test uid has to exist in here
                	ex.printStackTrace();
                }
            }
            
            // country derived from location
            final Field countryLocationField = test.getField("country_location"); 
            if (!countryLocationField.isNull()) {
                json.put("country_location", countryLocationField.toString());
            }
        }
        
        return json;
    }
}
