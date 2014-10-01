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
package at.alladin.rmbt.controlServer;

import java.text.DateFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import at.alladin.rmbt.db.Client;
import at.alladin.rmbt.db.Test;
import at.alladin.rmbt.db.Test_Server;
import at.alladin.rmbt.db.fields.Field;
import at.alladin.rmbt.db.fields.TimestampField;
import at.alladin.rmbt.db.fields.UUIDField;
import at.alladin.rmbt.shared.Classification;
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.SignificantFormat;

public class TestResultResource extends ServerResource
{
    @Post("json")
    public String request(final String entity)
    {
        addAllowOrigin();
        
        JSONObject request = null;
        
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;
        
        System.out.println(MessageFormat.format(labels.getString("NEW_TESTRESULT"), getIP()));
        
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
                    final Test_Server server = new Test_Server(conn);
                    final Test test = new Test(conn);
                    
                    final String testUuid = request.optString("test_uuid");
                    if (testUuid != null && test.getTestByUuid(UUID.fromString(testUuid)) > 0
                            && server.getServerByUid(test.getField("server_id").intValue())
                            && client.getClientByUid(test.getField("client_id").intValue())
                            && "FINISHED".equals(test.getField("status").toString()))
                    {
                        
                        final Locale locale = new Locale(lang);
                        final Format format = new SignificantFormat(2, locale);
                        
                        final JSONArray resultList = new JSONArray();
                        
                        final JSONObject jsonItem = new JSONObject();
                        
                        JSONArray jsonItemList = new JSONArray();
                        
                        // RMBTClient Info
                        //also send open-uuid (starts with 'P')
                        final String openUUID = "P" + ((UUIDField) test.getField("open_uuid")).toString();
                        jsonItem.put("open_uuid", openUUID);
                        
                        //and open test-uuid (starts with 'O')
                        final String openTestUUID = "O" + ((UUIDField) test.getField("open_test_uuid")).toString();
                        jsonItem.put("open_test_uuid", openTestUUID);
                        
                        final Date date = ((TimestampField) test.getField("time")).getDate();
                        final long time = date.getTime();
                        final String tzString = test.getField("timezone").toString();
                        final TimeZone tz = TimeZone.getTimeZone(tzString);
                        jsonItem.put("time", time);
                        jsonItem.put("timezone", tzString);
                        final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                DateFormat.MEDIUM, locale);
                        dateFormat.setTimeZone(tz);
                        final String timeString = dateFormat.format(date);
                        jsonItem.put("time_string", timeString);
                        
                        final Field fieldDown = test.getField("speed_download");
                        JSONObject singleItem = new JSONObject();
                        singleItem.put("title", labels.getString("RESULT_DOWNLOAD"));
                        final String downloadString = String.format("%s %s",
                                format.format(fieldDown.doubleValue() / 1000d),
                                labels.getString("RESULT_DOWNLOAD_UNIT"));
                        singleItem.put("value", downloadString);
                        singleItem.put("classification",
                                Classification.classify(Classification.THRESHOLD_DOWNLOAD, fieldDown.intValue()));
                        
                        jsonItemList.put(singleItem);
                        
                        final Field fieldUp = test.getField("speed_upload");
                        singleItem = new JSONObject();
                        singleItem.put("title", labels.getString("RESULT_UPLOAD"));
                        final String uploadString = String.format("%s %s",
                                format.format(fieldUp.doubleValue() / 1000d), labels.getString("RESULT_UPLOAD_UNIT"));
                        singleItem.put("value", uploadString);
                        singleItem.put("classification",
                                Classification.classify(Classification.THRESHOLD_UPLOAD, fieldUp.intValue()));
                        
                        jsonItemList.put(singleItem);
                        
                        final Field fieldPing = test.getField("ping_median");
                        String pingString = "";
                        if (! fieldPing.isNull()) {
                        	final double pingValue = fieldPing.doubleValue() / 1000000d;
                        	singleItem = new JSONObject();
                        	singleItem.put("title", labels.getString("RESULT_PING"));
                        	pingString = String.format("%s %s", format.format(pingValue),
                        			labels.getString("RESULT_PING_UNIT"));
                        	singleItem.put("value", pingString);
                        	singleItem.put("classification",
                        			Classification.classify(Classification.THRESHOLD_PING, fieldPing.longValue()));

                        	jsonItemList.put(singleItem);
                        }

                        final int networkType = test.getField("network_type").intValue();
                        
                        final Field signalField = test.getField("signal_strength"); // signal strength as RSSI (GSM, UMTS, Wifi, sometimes LTE)
                        final Field lteRsrpField = test.getField("lte_rsrp");            // signal strength as RSRP, used in LTE
                        String signalString = null;
                        if (!signalField.isNull() || !lteRsrpField.isNull() )
                        {
                        	if (lteRsrpField.isNull()) {  // only RSSI value, output RSSI to JSON  
                        		final int signalValue = signalField.intValue();
                                final int[] threshold = networkType == 99 || networkType == 0 ? Classification.THRESHOLD_SIGNAL_WIFI
                                       : Classification.THRESHOLD_SIGNAL_MOBILE;
                                singleItem = new JSONObject();
                                singleItem.put("title", labels.getString("RESULT_SIGNAL"));
                                signalString = signalValue + " " + labels.getString("RESULT_SIGNAL_UNIT");
                                singleItem.put("value", signalString);
                                singleItem.put("classification", Classification.classify(threshold, signalValue));
                        	}
                        	else  { // use RSRP value else (RSRP value has priority if both are available (e.g. 3G/4G-test))
                        		final int signalValue = lteRsrpField.intValue();
                                final int[] threshold = Classification.THRESHOLD_SIGNAL_RSRP;
                                singleItem = new JSONObject();
                                singleItem.put("title", labels.getString("RESULT_SIGNAL_RSRP"));
                                signalString = signalValue + " " + labels.getString("RESULT_SIGNAL_UNIT");
                                singleItem.put("value", signalString);
                                singleItem.put("classification", Classification.classify(threshold, signalValue));

                            }	
                            jsonItemList.put(singleItem);
                        }
                        
                        jsonItem.put("measurement", jsonItemList);
                        
                        jsonItemList = new JSONArray();
                        
                        singleItem = new JSONObject();
                        singleItem.put("title", labels.getString("RESULT_NETWORK_TYPE"));
                        final String networkTypeString = Helperfunctions.getNetworkTypeName(networkType);
                        singleItem.put("value", networkTypeString);
                        
                        jsonItemList.put(singleItem);
                        
                        if (networkType == 98 || networkType == 99) // mobile wifi or browser
                        {
                            final Field providerNameField = test.getField("provider_id_name");
                            if (! providerNameField.isNull())
                            {
                                singleItem = new JSONObject();
                                singleItem.put("title", labels.getString("RESULT_OPERATOR_NAME"));
                                singleItem.put("value", providerNameField.toString());
                                jsonItemList.put(singleItem);
                            }
                            if (networkType == 99)  // mobile wifi
                            {
                                final Field ssid = test.getField("wifi_ssid");
                                if (! ssid.isNull())
                                {
                                    singleItem = new JSONObject();
                                    singleItem.put("title", labels.getString("RESULT_WIFI_SSID"));
                                    singleItem.put("value", ssid.toString());
                                    jsonItemList.put(singleItem);
                                }
                                
                            }
                        }
                        else // mobile
                        {
                            final Field operatorNameField = test.getField("network_operator_name");
                            if (! operatorNameField.isNull())
                            {
                                singleItem = new JSONObject();
                                singleItem.put("title", labels.getString("RESULT_OPERATOR_NAME"));
                                singleItem.put("value", operatorNameField.toString());
                                jsonItemList.put(singleItem);
                            }
                            
                            final Field roamingTypeField = test.getField("roaming_type");
                            if (! roamingTypeField.isNull() && roamingTypeField.intValue() > 0)
                            {
                                singleItem = new JSONObject();
                                singleItem.put("title", labels.getString("RESULT_ROAMING"));
                                singleItem.put("value", Helperfunctions.getRoamingType(labels, roamingTypeField.intValue()));
                                jsonItemList.put(singleItem);
                            }
                        }
                        
                        jsonItem.put("net", jsonItemList);
                        
                        
                        final Field latField = test.getField("geo_lat");
                        final Field longField = test.getField("geo_long");
                        final Field accuracyField = test.getField("geo_accuracy");
                        if (!(latField.isNull() || longField.isNull() || accuracyField.isNull()))
                        {
                            final double accuracy = accuracyField.doubleValue();
                            if (accuracy < Double.parseDouble(settings.getString("RMBT_GEO_ACCURACY_BUTTON_LIMIT")))
                            {
                                jsonItem.put("geo_lat", latField.doubleValue());
                                jsonItem.put("geo_long", longField.doubleValue());
                            }
                        }
                        
                        final Field zip_code = test.getField("zip_code");
                        if (! zip_code.isNull())
                            jsonItem.put("zip_code", zip_code.intValue());
                        
                        final Field zip_code_geo = test.getField("zip_code_geo");
                        if (! zip_code_geo.isNull())
                            jsonItem.put("zip_code_geo", zip_code_geo.intValue());
                        
                        String providerString = test.getField("provider_id_name").toString();
                        if (providerString == null)
                            providerString = "";
                        String platformString = test.getField("plattform").toString();
                        if (platformString == null)
                            platformString = "";
                        String modelString = test.getField("model_fullname").toString();
                        if (modelString == null)
                            modelString = "";
                        
                        String mobileNetworkString = null;
                        final Field networkOperatorField = test.getField("network_operator");
                        final Field mobileProviderNameField = test.getField("mobile_provider_name");
                        if (! networkOperatorField.isNull())
                        {
                            if (mobileProviderNameField.isNull())
                                mobileNetworkString = networkOperatorField.toString();
                            else
                                mobileNetworkString = String.format("%s (%s)", mobileProviderNameField, networkOperatorField);
                        }
                        
/*
RESULT_SHARE_TEXT = My Result:\nDate/time: {0}\nDownload: {1}\nUpload: {2}\nPing: {3}\n{4}Network type: {5}\n{6}{7}Platform: {8}\nModel: {9}\n{10}\n\n
RESULT_SHARE_TEXT_SIGNAL_ADD = Signal strength: {0}\n
RESULT_SHARE_TEXT_RSRP_ADD = Signal strength (RSRP): {0}\n
RESULT_SHARE_TEXT_MOBILE_ADD = Mobile network: {0}\n
RESULT_SHARE_TEXT_PROVIDER_ADD = Operator: {0}\n

*/                     
                        String shareTextField4 = "";
                        if (signalString != null)             //info on signal strength, field 4
                        	if (lteRsrpField.isNull()) {      //add RSSI if RSRP is not available
                                shareTextField4 = MessageFormat.format(labels.getString("RESULT_SHARE_TEXT_SIGNAL_ADD"), signalString);
                        	}    
                            else {                            //add RSRP
                                shareTextField4 = MessageFormat.format(labels.getString("RESULT_SHARE_TEXT_RSRP_ADD"), signalString);
                            }		

                        final String shareText = MessageFormat.format(labels.getString("RESULT_SHARE_TEXT"),
                                timeString,                   //field 0
                                downloadString,               //field 1
                                uploadString,                 //field 2
                                pingString,                   //field 3
                                shareTextField4,              //contains field 4
                                networkTypeString,            //field 5
                                providerString.isEmpty() ? "" : MessageFormat.format(labels.getString("RESULT_SHARE_TEXT_PROVIDER_ADD"), 
                                		providerString),      //field 6
                                mobileNetworkString == null ? "" : MessageFormat.format(labels.getString("RESULT_SHARE_TEXT_MOBILE_ADD"), 
                                		mobileNetworkString), //field 7
                                platformString,               //field 8
                                modelString,                  //field 9
                                getSetting("url_open_data_prefix", lang) +
                                openTestUUID);                //field 10
                        jsonItem.put("share_text", shareText);
                        
                        jsonItem.put("network_type", networkType);
                        
                        
                        //geo location
                        JSONObject locationJson = TestResultDetailResource.getGeoLocation(test, settings, conn, labels);
                        if (locationJson != null) {
                        	if (locationJson.has("location")) {
                        		jsonItem.put("location", locationJson.getString("location"));
                        	}
                        	if (locationJson.has("motion")) {
                        		jsonItem.put("motion", locationJson.getString("motion"));
                        	}                        	
                        }
                        
                        resultList.put(jsonItem);
                        
                        if (resultList.length() == 0)
                            errorList.addError("ERROR_DB_GET_TESTRESULT");
                        // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENT"),
                        // new Object[] {uuid}));
                        
                        answer.put("testresult", resultList);
                    }
                    else
                        errorList.addError("ERROR_REQUEST_NO_UUID");
                    
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
        
        return answerString;
    }
    
    @Get("json")
    public String retrieve(final String entity)
    {
        return request(entity);
    }
    
}
