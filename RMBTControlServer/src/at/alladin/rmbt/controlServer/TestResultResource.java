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

import java.text.DateFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
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
import at.alladin.rmbt.db.Test_Server;
import at.alladin.rmbt.db.fields.Field;
import at.alladin.rmbt.db.fields.TimestampField;
import at.alladin.rmbt.shared.Classification;
import at.alladin.rmbt.shared.Helperfunctions;
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
                    labels = (PropertyResourceBundle) ResourceBundle.getBundle("at.alladin.rmbt.res.SystemMessages",
                            new Locale(lang));
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
                        
                        final Field fieldPing = test.getField("ping_shortest");
                        final double pingValue = test.getField("ping_shortest").doubleValue() / 1000000d;
                        singleItem = new JSONObject();
                        singleItem.put("title", labels.getString("RESULT_PING"));
                        final String pingString = String.format("%s %s", format.format(pingValue),
                                labels.getString("RESULT_PING_UNIT"));
                        singleItem.put("value", pingString);
                        singleItem.put("classification",
                                Classification.classify(Classification.THRESHOLD_PING, fieldPing.longValue()));
                        
                        jsonItemList.put(singleItem);
                        
                        final int networkType = test.getField("network_type").intValue();
                        
                        final Field signalField = test.getField("signal_strength");
                        String signalString = null;
                        if (!signalField.isNull())
                        {
                            final int signalValue = signalField.intValue();
                            final int[] threshold = networkType == 99 || networkType == 0 ? Classification.THRESHOLD_SIGNAL_WIFI
                                    : Classification.THRESHOLD_SIGNAL_MOBILE;
                            singleItem = new JSONObject();
                            singleItem.put("title", labels.getString("RESULT_SIGNAL"));
                            signalString = signalValue + " " + labels.getString("RESULT_SIGNAL_UNIT");
                            singleItem.put("value", signalString);
                            singleItem.put("classification", Classification.classify(threshold, signalValue));
                            jsonItemList.put(singleItem);
                        }
                        
                        jsonItem.put("measurement", jsonItemList);
                        
                        jsonItemList = new JSONArray();
                        
                        singleItem = new JSONObject();
                        singleItem.put("title", labels.getString("RESULT_NETWORK_TYPE"));
                        final String networkTypeString = Helperfunctions.getNetworkTypeName(networkType);
                        singleItem.put("value", networkTypeString);
                        
                        jsonItemList.put(singleItem);
                        
                        String mobileNetworkString = null;
                        if (networkType == 99)
                        {
                            final Field ssid = test.getField("wifi_ssid");
                            if (!ssid.isNull())
                            {
                                singleItem = new JSONObject();
                                singleItem.put("title", labels.getString("RESULT_WIFI_SSID"));
                                
                                singleItem.put("value", ssid.toString());
                                
                                jsonItemList.put(singleItem);
                            }
                            
                        }
                        else
                        {
                            final Field operatorField = test.getField("network_operator");
                            final Field mncMccText = test.getField("network_operator_mnc_mcc_text");
                            
                            final Field operatorNameField = test.getField("network_operator_name");
                            if (!operatorNameField.isNull())
                            {
                                singleItem = new JSONObject();
                                singleItem.put("title", labels.getString("RESULT_OPERATOR_NAME"));
                                
                                String addOperatorInfo = null;
                                final Field simField = test.getField("network_sim_operator");
                                if (!operatorField.isNull() && !simField.isNull())
                                {
                                    final String operator = operatorField.toString();
                                    final String sim = simField.toString();
                                    if (operator != null && sim != null && !operator.equals(sim))
                                    {
                                        if (mncMccText.isNull())
                                            addOperatorInfo = labels.getString("RESULT_OPERATOR_NETWORK_ROAMING");
                                        else
                                            addOperatorInfo = MessageFormat.format(
                                                    labels.getString("RESULT_OPERATOR_NETWORK"), mncMccText.toString());
                                    }
                                }
                                
                                if (addOperatorInfo != null)
                                    singleItem.put("value", String.format("%s %s", operatorNameField, addOperatorInfo));
                                else
                                    singleItem.put("value", operatorNameField.toString());
                                
                                jsonItemList.put(singleItem);
                                
                                if (mncMccText.isNull())
                                    mobileNetworkString = operatorField.toString();
                                else
                                    mobileNetworkString = String.format("%s (%s)", mncMccText, operatorField);
                            }
                        }
                        
                        if (! request.has("softwareVersionCode") ||
                                request.optInt("softwareVersionCode", 0) < Integer.parseInt(settings.getString("RMBT_CURRENT_VERSION_CODE")))
                        {
                            singleItem = new JSONObject();
                            singleItem.put("title", labels.getString("RESULT_UPDATE_INFO_TITLE"));
                            singleItem.put("value", labels.getString("RESULT_UPDATE_INFO_VALUE"));
                            jsonItemList.put(singleItem);
                        }
                        
                        jsonItem.put("net", jsonItemList);
                        
                        final Field geo_lat = test.getField("geo_lat");
                        final Field geo_long = test.getField("geo_long");
                        if (!geo_lat.isNull() && !geo_long.isNull())
                        {
                            jsonItem.put("geo_lat", geo_lat.doubleValue());
                            jsonItem.put("geo_long", geo_long.doubleValue());
                        }
                        
                        String providerString = test.getField("provider_id_name").toString();
                        if (providerString == null)
                            providerString = "";
                        String platformString = test.getField("plattform").toString();
                        if (platformString == null)
                            platformString = "";
                        String modelString = test.getField("model").toString();
                        if (modelString == null)
                            modelString = "";
                        
                        final String shareText = MessageFormat.format(labels.getString("RESULT_SHARE_TEXT"),
                                timeString, downloadString, uploadString, pingString,
                                signalString == null ? "" : MessageFormat.format(labels.getString("RESULT_SHARE_TEXT_SIGNAL_ADD"), signalString),
                                networkTypeString,
                                providerString == null || providerString.isEmpty() ? "" : MessageFormat.format(labels.getString("RESULT_SHARE_TEXT_PROVIDER_ADD"), providerString),
                                mobileNetworkString == null ? "" : MessageFormat.format(labels.getString("RESULT_SHARE_TEXT_MOBILE_ADD"), mobileNetworkString),
                                platformString, modelString);
                        
                        jsonItem.put("share_text", shareText);
                        
                        jsonItem.put("network_type", networkType);
                        
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
