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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
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
import at.alladin.rmbt.db.TestNdt;
import at.alladin.rmbt.db.Test_Server;
import at.alladin.rmbt.db.fields.Field;
import at.alladin.rmbt.db.fields.TimestampField;
import at.alladin.rmbt.shared.Helperfunctions;
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
        addAllowOrigin();
        
        JSONObject request = null;
        
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;
        
        System.out.println(MessageFormat.format(labels.getString("NEW_TESTRESULT_DETAIL"), getIP()));
        
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
                    TestNdt ndt = new TestNdt(conn);
                    
                    final String testUuid = request.optString("test_uuid");
                    if (testUuid != null && test.getTestByUuid(UUID.fromString(testUuid)) > 0
                            && server.getServerByUid(test.getField("server_id").intValue())
                            && client.getClientByUid(test.getField("client_id").intValue()))
                    {
                        
                        if (!ndt.loadByTestId(test.getUid()))
                            ndt = null;
                        
                        final Locale locale = new Locale(lang);
                        final Format format = new SignificantFormat(2, locale);
                        
                        final JSONArray resultList = new JSONArray();
                        
                        addString(resultList, "uuid", String.format("T%s", test.getField("uuid")));
                        
                        final JSONObject singleItem = addObject(resultList, "time");
                        final Date date = ((TimestampField) test.getField("time")).getDate();
                        final long time = date.getTime();
                        singleItem.put("time", time);
                        final String tzString = test.getField("timezone").toString();
                        final TimeZone tz = TimeZone.getTimeZone(tzString);
                        singleItem.put("timezone", tzString);
                        final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                DateFormat.MEDIUM, locale);
                        dateFormat.setTimeZone(tz);
                        singleItem.put("value", dateFormat.format(date));
                        
                        final Format tzFormat = new DecimalFormat("+0.##;-0.##", new DecimalFormatSymbols(locale));
                        
                        final float offset = tz.getOffset(time) / 1000f / 60f / 60f;
                        addString(resultList, "timezone", String.format("UTC%sh", tzFormat.format(offset)));
                        
                        final String download = format.format(test.getField("speed_download").doubleValue() / 1000d);
                        addString(resultList, "speed_download",
                                String.format("%s %s", download, labels.getString("RESULT_DOWNLOAD_UNIT")));
                        
                        final String upload = format.format(test.getField("speed_upload").doubleValue() / 1000d);
                        addString(resultList, "speed_upload",
                                String.format("%s %s", upload, labels.getString("RESULT_UPLOAD_UNIT")));
                        
                        final String ping = format.format(test.getField("ping_shortest").doubleValue() / 1000000d);
                        addString(resultList, "ping_shortest",
                                String.format("%s %s", ping, labels.getString("RESULT_PING_UNIT")));
                        
                        final Field signalStrengthField = test.getField("signal_strength");
                        if (!signalStrengthField.isNull())
                            addString(
                                    resultList,
                                    "signal_strength",
                                    String.format("%d %s", signalStrengthField.intValue(),
                                            labels.getString("RESULT_SIGNAL_UNIT")));
                        addString(resultList, "network_type",
                                Helperfunctions.getNetworkTypeName(test.getField("network_type").intValue()));
                        addString(resultList, "client_type", client.getClient_type_name());
                        
                        final Field latField = test.getField("geo_lat");
                        final Field longField = test.getField("geo_long");
                        final Field accuracyField = test.getField("geo_accuracy");
                        final Field providerField = test.getField("geo_provider");
                        if (!(latField.isNull() || longField.isNull() || accuracyField.isNull()))
                        {
                            final double accuracy = accuracyField.doubleValue();
                            if (accuracy < Double.parseDouble(settings.getString("RMBT_MIN_GEO_ACCURACY")))
                            {
                                final StringBuilder geoString = new StringBuilder(Helperfunctions.geoToString(latField.doubleValue(),
                                        longField.doubleValue()));
                                
                                if (! providerField.isNull())
                                {
                                    geoString.append(" (");
                                    boolean hadProvider = false;
                                    if (! providerField.isNull())
                                    {
                                        hadProvider = true;
                                        geoString.append(providerField.toString().toUpperCase(Locale.US));
                                    }
                                    if (! accuracyField.isNull())
                                    {
                                        if (hadProvider)
                                            geoString.append(", ");
                                        geoString.append(String.format(Locale.US, "+/- %.0f m", accuracy));
                                    }
                                    geoString.append(")");
                                }
                                addString(resultList, "location", geoString.toString());
                            }
                        }
                        
                        final Field zipCodeField = test.getField("zip_code");
                        if (!zipCodeField.isNull())
                        {
                            String zipCode = zipCodeField.toString();
                            if (zipCode.equals("0"))
                                zipCode = "-";
                            addString(resultList, "zip_code", zipCode);
                        }
                        
                        addString(resultList, "client_public_ip", test.getField("client_public_ip"));
                        addString(resultList, "client_public_ip_as_name", test.getField("public_ip_as_name"));
                        addString(resultList, "client_public_ip_rdns", test.getField("public_ip_rdns"));
                        addString(resultList, "provider", test.getField("provider_id_name"));
                        addString(resultList, "client_local_ip", test.getField("client_local_ip"));
                        addString(resultList, "nat_type", test.getField("nat_type"));
                        
                        addString(resultList, "wifi_ssid", test.getField("wifi_ssid"));
                        addString(resultList, "wifi_bssid", test.getField("wifi_bssid"));
                        
                        final Field linkSpeedField = test.getField("wifi_link_speed");
                        if (!linkSpeedField.isNull())
                            addString(
                                    resultList,
                                    "wifi_link_speed",
                                    String.format("%s %s", linkSpeedField.toString(),
                                            labels.getString("RESULT_WIFI_LINK_SPEED_UNIT")));
                        
                        addString(resultList, "network_operator_name", test.getField("network_operator_name"));
                        
                        final Field networkOperatorField = test.getField("network_operator");
                        final Field mobileProviderNameField = test.getField("mobile_provider_name");
                        if (mobileProviderNameField.isNull())
                            addString(resultList, "network_operator", networkOperatorField);
                        else
                            addString(resultList, "network_operator",
                                    String.format("%s (%s)", mobileProviderNameField, networkOperatorField));
                        
                        addString(resultList, "network_sim_operator_name", test.getField("network_sim_operator_name"));
                        
                        final Field networkSimOperatorField = test.getField("network_sim_operator");
                        final Field networkSimOperatorTextField = test.getField("network_sim_operator_mcc_mnc_text");
                        if (networkSimOperatorTextField.isNull())
                            addString(resultList, "network_sim_operator", networkSimOperatorField);
                        else
                            addString(resultList, "network_sim_operator",
                                    String.format("%s (%s)", networkSimOperatorTextField, networkSimOperatorField));
                        
                        final Field roamingTypeField = test.getField("roaming_type");
                        if (! roamingTypeField.isNull() && roamingTypeField.intValue() > 0)
                            addString(resultList, "roaming", Helperfunctions.getRoamingType(labels, roamingTypeField.intValue()));
                        
                        final long totalDownload = test.getField("total_bytes_download").longValue();
                        final long totalUpload = test.getField("total_bytes_upload").longValue();
                        final long totalBytes = totalDownload + totalUpload;
                        if (totalBytes > 0)
                        {
                            final String totalBytesString = format.format(totalBytes / (1024d * 1024d));
                            addString(
                                    resultList,
                                    "total_bytes",
                                    String.format("%s %s", totalBytesString,
                                            labels.getString("RESULT_TOTAL_BYTES_UNIT")));
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
                        
                        addString(resultList, "plattform", test.getField("plattform"));
                        addString(resultList, "os_version", test.getField("os_version"));
                        addString(resultList, "model", test.getField("model"));
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
                            addString(resultList, "client_version", test.getField("client_version"));
                        }
                        
                        // NDT-Typ
                        // NDT-Version
                        
                        addString(resultList, "server_name", server.getName());
                        addString(
                                resultList,
                                "duration",
                                String.format("%d %s", test.getField("duration").intValue(),
                                        labels.getString("RESULT_DURATION_UNIT")));
                        addInt(resultList, "num_threads", test.getField("num_threads"));
                        
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
        
        return answerString;
    }
    
    @Get("json")
    public String retrieve(final String entity)
    {
        return request(entity);
    }
    
}
