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

import at.rtr.rmbt.client.helper.Globals;
import at.rtr.rmbt.client.Ping;
import at.rtr.rmbt.client.RMBTTestParameter;
import at.rtr.rmbt.client.SpeedItem;
import at.rtr.rmbt.client.TotalTestResult;
import at.rtr.rmbt.client.ndt.UiServicesAdapter;
import at.rtr.rmbt.client.v2.task.TaskDesc;
import at.rtr.rmbt.client.v2.task.service.TestMeasurement;
import at.rtr.rmbt.client.v2.task.service.TestMeasurement.TrafficDirection;
import at.rtr.rmbt.util.capability.Capabilities;
import at.rtr.rmbt.util.model.shared.exception.ErrorStatus;

public class ControlServerConnection
{
    
    // url to make request
    private URL hostUrl;
    
    private boolean testEncryption;
    
    private String testToken = "";
    //private String testId = "";
    private String testUuid = "";
    private String loopUuid = "";
    
    private long testTime = 0;
    
    private String testHost = "";
    private String serverType;
    private int testPort = 0;
    private String remoteIp = "";
    private String serverName;
    private String provider;
    
    private int testDuration = 0;
    private int testNumThreads = 0;
    private int testNumPings = 0;
    
    private String clientUUID = "";
    
    private URL resultURL;
	private URL resultQoSURI;
    
    private String errorMsg = null;
    
    private boolean hasError = false;
    
    private Set<ErrorStatus> lastErrorList;
    
    public TaskDesc udpTaskDesc;
    public TaskDesc dnsTaskDesc;
    public TaskDesc ntpTaskDesc;
    public TaskDesc httpTaskDesc;
    public TaskDesc tcpTaskDesc;
    
    private JSONObject lastTestResult;
    
    public List<TaskDesc> v2TaskDesc;
    
    private long startTimeMillis = 0;
    private long startTimeNs = 0;
    
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

    public ControlServerConnection() {
        Capabilities capabilities = new Capabilities();
        capabilities.setRmbtHttp(true);
        JSONParser.setCapabilities(capabilities);
    }

    /**
     * requests the parameters for the v2 tests
     * @param host
     * @param pathPrefix
     * @param port
     * @param encryption
     * @param geoInfo
     * @param uuid
     * @param clientType
     * @param clientName
     * @param clientVersion
     * @param additionalValues
     * @return
     */
    public String requestQoSTestParameters(final String host, final String pathPrefix, final int port,
            final boolean encryption, final ArrayList<String> geoInfo, final String uuid, final String clientType,
            final String clientName, final String clientVersion, final JSONObject additionalValues)
    {
    	resetErrors();
        clientUUID = uuid;
        
        hostUrl = getUrl(encryption, host, pathPrefix, port, Config.RMBT_QOS_TEST_REQUEST);
        
        if(Globals.DEBUG_CLI) 
            System.out.println("Connection to: " + hostUrl);
        
        final JSONObject regData = new JSONObject();
        
        try
        {
            regData.put("uuid", uuid);
            regData.put("client", clientName);
            regData.put("version", clientVersion);
            regData.put("type", clientType);
            regData.put("softwareVersion", clientVersion);
            regData.put("softwareRevision", RevisionHelper.getVerboseRevision());
            regData.put("language", Locale.getDefault().getLanguage());
            regData.put("timezone", TimeZone.getDefault().getID());
            regData.put("time", System.currentTimeMillis());
            
            if (geoInfo != null)
            {
                final JSONObject locData = new JSONObject();
                locData.put("time", geoInfo.get(0));
                locData.put("lat", geoInfo.get(1));
                locData.put("long", geoInfo.get(2));
                locData.put("accuracy", geoInfo.get(3));
                locData.put("altitude", geoInfo.get(4));
                locData.put("bearing", geoInfo.get(5));
                locData.put("speed", geoInfo.get(6));
                locData.put("provider", geoInfo.get(7));
                
                regData.accumulate("location", locData);
            }
            
            addToJSONObject(regData, additionalValues);
            
        }
        catch (final JSONException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }
        
        // getting JSON string from URL
        final JSONObject response = JSONParser.sendJSONToUrl(hostUrl, regData);
        
        if (response != null)
            try
            {
                final JSONArray errorList = response.getJSONArray("error");
                
                checkHasErrors(response);
                
                // System.out.println(response.toString(4));
                
                if (errorList.length() == 0)
                {
                	
                	int testPort = 5233;
                	
                	Map<String, Object> testParams = null;
                	testParams = JSONParser.toMap(response.getJSONObject("objectives"));
                	
                	v2TaskDesc = new ArrayList<TaskDesc>();
                	
                	for (Entry<String, Object> e : testParams.entrySet()) {
                		List<HashMap<String, Object>> paramList = (List<HashMap<String, Object>>) e.getValue();
                		for (HashMap<String, Object> params : paramList) {
                			TaskDesc taskDesc = new TaskDesc(testHost, testPort, encryption, testToken, 0, 1, 0, System.nanoTime(), params, e.getKey());
            				v2TaskDesc.add(taskDesc);
                		}
                	}                    
                }
                else
                {
                    hasError = true;
                    for (int i = 0; i < errorList.length(); i++)
                    {
                        if (i > 0)
                            errorMsg += "\n";
                        errorMsg += errorList.getString(i);
                    }
                }
                
                // }
            }
            catch (final JSONException e)
            {
                hasError = true;
                errorMsg = "Error parsing server response";
                e.printStackTrace();
            }
        else
        {
            hasError = true;
            errorMsg = "No response";
        }
        
        return errorMsg;
    }
    
    private void resetErrors() {
    	lastErrorList = null;
    }
    
    private boolean checkHasErrors(JSONObject response) throws JSONException {
    	final JSONArray errorList = response.getJSONArray("error");
    	final JSONArray errorFlags = response.optJSONArray("error_flags");
    	if (errorFlags != null && errorFlags.length() > 0) {
    		lastErrorList = new HashSet<ErrorStatus>();
    		for (int i = 0; i < errorFlags.length(); i++) {
    			lastErrorList.add(ErrorStatus.valueOf(errorFlags.getString(i)));
    		}
    	}
    	return errorList.length() > 0;
    }
    
    public String requestNewTestConnection(final String host, final String pathPrefix, final int port,
            final boolean encryption, final ArrayList<String> geoInfo, final String uuid, final String clientType,
            final String clientName, final String clientVersion, final JSONObject additionalValues, final JSONObject jsonLoopData, final JSONObject jsonQmonData)
    {
    	resetErrors();
        String errorMsg = null;
        // url to make request to
        
        clientUUID = uuid;
        
        hostUrl = getUrl(encryption, host, pathPrefix, port, Config.RMBT_TEST_SETTINGS_REQUEST);
        
        //System.out.println("Connection: " + hostUrl);
        
        final JSONObject regData = new JSONObject();
        
        try
        {
            regData.put("uuid", uuid);
            regData.put("client", clientName);
            regData.put("version", clientVersion);
            regData.put("type", clientType);
            regData.put("softwareVersion", clientVersion);
            regData.put("softwareRevision", RevisionHelper.getVerboseRevision());
            regData.put("language", Locale.getDefault().getLanguage());
            regData.put("timezone", TimeZone.getDefault().getID());
            startTimeMillis = System.currentTimeMillis();
            regData.put("time", startTimeMillis);
            startTimeNs = System.nanoTime();

            if (jsonLoopData.length() != 0){
                regData.put("user_loop_mode", true);
                regData.put("loopmode_info", jsonLoopData);
            }

            if (jsonQmonData.length() != 0 && jsonQmonData.has("temperature"))  
                regData.put("temperature", jsonQmonData.getFloat("temperature"));

            System.out.println(regData);

            if (geoInfo != null)
            {
                final JSONObject locData = new JSONObject();
                locData.put("time", geoInfo.get(0));
                locData.put("lat", geoInfo.get(1));
                locData.put("long", geoInfo.get(2));
                locData.put("accuracy", geoInfo.get(3));
                locData.put("altitude", geoInfo.get(4));
                locData.put("bearing", geoInfo.get(5));
                locData.put("speed", geoInfo.get(6));
                locData.put("provider", geoInfo.get(7));
                
                regData.accumulate("location", locData);
            }
            
            addToJSONObject(regData, additionalValues);
            
        }
        catch (final JSONException e1)
        {
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }

        //System.out.println(regData);  
        
        // getting JSON string from URL
        final JSONObject response = JSONParser.sendJSONToUrl(hostUrl, regData);  
        
        if (response != null)
            try
            {
                final JSONArray errorList = response.getJSONArray("error");
                
                //System.out.println(hostUrl);
                // System.out.println(response.toString(4));
                checkHasErrors(response);
                
                if (errorList.length() == 0)
                {
                    //System.out.println(response);

                    clientUUID = response.optString("uuid", clientUUID);
                    
                    testToken = response.getString("test_token");
                    loopUuid = response.optString("loop_uuid");

                    // JC US?
                    testUuid = response.getString("test_uuid");
                    
                    testTime = System.currentTimeMillis() + 1000 * response.getLong("test_wait");
                    
                    testHost = response.getString("test_server_address");
                    testPort = response.getInt("test_server_port");
                    serverType = response.optString("test_server_type", Config.SERVER_TYPE_RMBT);
                    testEncryption = response.getBoolean("test_server_encryption");
                    serverName = response.optString("test_server_name", null);
                    provider = response.optString("provider", null);
                    
                    testDuration = response.getInt("test_duration");
                    testNumThreads = response.getInt("test_numthreads");
                    // JC Num PING!
                    testNumPings = response.optInt("test_numpings", 10); // pings default to 10
                    testNumPings = 40;
                    
                    remoteIp = response.getString("client_remote_ip");
                                        
                    resultURL = new URL(response.getString("result_url"));
                    resultQoSURI = new URL(response.getString("result_qos_url"));

                    JSONObject json = new JSONObject();
                    json.put("type", DebugStates.UUID_INFO);
                    json.put("testUuid", testUuid);
                    json.put("openTestUuid", response.getString("open_test_uuid"));
                    json.put("testToken", testToken);
                    
                    //System.out.println(json);
                    if (Globals.DEBUG_CLI_GUI)
                        System.out.println(String.format(Locale.US, "%s", json));
                }
                else
                {
                    errorMsg = "";
                    for (int i = 0; i < errorList.length(); i++)
                    {
                        if (i > 0)
                            errorMsg += "\n";
                        errorMsg += errorList.getString(i);
                    }
                }
                
                // }
            }
            catch (final JSONException e)
            {
                errorMsg = "Error parsing server response";
                e.printStackTrace();
            }
            catch (final MalformedURLException e)
            {
                errorMsg = "Error parsing server response";
                e.printStackTrace();
            }
        else
            errorMsg = "No response";

        return errorMsg;
    }

    /**
     * Best effort: Try to set a test as "aborted"
     *   This may or may not work, depending on the user behaviour
     */
    public void abortStartedTest() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", clientUUID);
        jsonObject.put("test_uuid", testUuid);
        jsonObject.put("aborted", true);

        try {

            JSONObject ret = JSONParser.sendJSONToUrl(hostUrl.toURI().resolve(Config.RMBT_CONTROL_PATH + Config.RMBT_UPDATE_RESULT_URL).toURL(), jsonObject);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    
    public String sendTestResult(TotalTestResult result, JSONObject additionalValues)
    {
        String errorMsg = null;
        lastTestResult = null;
        
        if (resultURL != null)
        {

            final JSONObject testData = new JSONObject();

            try
            {
                testData.put("client_uuid", clientUUID);
                testData.put("client_name", Config.RMBT_CLIENT_NAME);
                testData.put("client_version", result.client_version);
                testData.put("client_language", Locale.getDefault().getLanguage());

                testData.put("time", System.currentTimeMillis());

                testData.put("test_token", testToken);

                testData.put("test_port_remote", result.port_remote);
                testData.put("test_bytes_download", result.bytes_download);
                testData.put("test_bytes_upload", result.bytes_upload);
                testData.put("test_total_bytes_download", result.totalDownBytes);
                testData.put("test_total_bytes_upload", result.totalUpBytes);
                testData.put("test_encryption", result.encryption);
                testData.put("test_ip_local", result.ip_local.getHostAddress());
                testData.put("test_ip_server", result.ip_server.getHostAddress());
                testData.put("test_nsec_download", result.nsec_download);
                testData.put("test_nsec_upload", result.nsec_upload);
                testData.put("test_num_threads", result.num_threads);
                testData.put("test_speed_download", (long) Math.floor(result.speed_download + 0.5d));
                testData.put("test_speed_upload", (long) Math.floor(result.speed_upload + 0.5d));
                testData.put("test_ping_shortest", result.ping_shortest);

                //dz todo - add interface values

                // total bytes on interface
                testData.put("test_if_bytes_download", result.getTotalTrafficMeasurement(TrafficDirection.RX));
                testData.put("test_if_bytes_upload", result.getTotalTrafficMeasurement(TrafficDirection.TX));
                // bytes during download test
                testData.put("testdl_if_bytes_download", result.getTrafficByTestPart(TestStatus.DOWN, TrafficDirection.RX));
                testData.put("testdl_if_bytes_upload", result.getTrafficByTestPart(TestStatus.DOWN, TrafficDirection.TX));
                // bytes during upload test
                testData.put("testul_if_bytes_download", result.getTrafficByTestPart(TestStatus.UP, TrafficDirection.RX));
                testData.put("testul_if_bytes_upload", result.getTrafficByTestPart(TestStatus.UP, TrafficDirection.TX));

                //relative timestamps:
                TestMeasurement dlMeasurement = result.getTestMeasurementByTestPart(TestStatus.DOWN);
                if (dlMeasurement != null) {
                    testData.put("time_dl_ns", dlMeasurement.getTimeStampStart() - startTimeNs);
                }
                TestMeasurement ulMeasurement = result.getTestMeasurementByTestPart(TestStatus.UP);
                if (ulMeasurement != null) {
                    testData.put("time_ul_ns", ulMeasurement.getTimeStampStart() - startTimeNs);
                }


                final JSONArray pingData = new JSONArray();

                if (result.pings != null && !result.pings.isEmpty())
                {
                    for (final Ping ping : result.pings)
                    {
                        final JSONObject pingItem = new JSONObject();
                        pingItem.put("value", ping.client);
                        pingItem.put("value_server", ping.server);
                        pingItem.put("time_ns", ping.timeNs - startTimeNs);
                        // JC ping print, not OK
                        //System.out.println(pingItem);
                        pingData.put(pingItem);
                    }
                }

                testData.put("pings", pingData);

                JSONArray speedDetail = new JSONArray();

                if (result.speedItems != null)
                {
                    for (SpeedItem item : result.speedItems) {
                        speedDetail.put(item.toJSON());
                    }
                }

                testData.put("speed_detail", speedDetail);

                addToJSONObject(testData, additionalValues);

                // System.out.println(testData.toString(4));
            }
            catch (final JSONException e1)
            {
                errorMsg = "Error gernerating request";
                e1.printStackTrace();
            }

            // getting JSON string from URL
            JSONObject response = JSONParser.sendJSONToUrl(resultURL, testData);
            // JC results upload
            //System.out.println(testData);
            System.out.println(response);

            for (int i = 0; response == null && i < 4; i++) {
                //try again
                int connectTimeout = JSONParser.CONNECT_TIMEOUT + (int) Math.round(Math.pow(3,i)*1000);
                System.out.println("Submitting the results failed, trying again with " + connectTimeout + " ms timeout");
                response = JSONParser.sendJSONToUrl(resultURL, testData, connectTimeout);
            }

            if (response != null) {
                try {
                    final JSONArray errorList = response.getJSONArray("error");

                    // System.out.println(response.toString(4));

                    if (errorList.length() == 0) {
                        lastTestResult = testData;
                        // System.out.println("All is fine");

                    } else {
                        for (int i = 0; i < errorList.length(); i++) {
                            if (i > 0)
                                errorMsg += "\n";
                            errorMsg += errorList.getString(i);
                        }
                    }

                    // }
                } catch (final JSONException e) {
                    errorMsg = "Error parsing server response";
                    e.printStackTrace();
                }
            }
            else {
                errorMsg = "Result submission failed";
            }
        }
        else
            errorMsg = "No URL to send the Data to.";
        
        return errorMsg;
    }
    
    /**
     * 
     * @param result
     * @param qosTestResult
     * @return
     */
    public String sendQoSResult(final TotalTestResult result, final JSONArray qosTestResult)
    {
        String errorMsg = null;
        if (Globals.DEBUG_CLI)
            System.out.println("Sending qos results to: " + resultQoSURI);
        if (resultQoSURI != null)
        {
            
            final JSONObject testData = new JSONObject();
            
            try
            {
                testData.put("client_uuid", clientUUID);
                testData.put("client_name", Config.RMBT_CLIENT_NAME);
                testData.put("client_version", Config.RMBT_VERSION_NUMBER);
                testData.put("client_language", Locale.getDefault().getLanguage());
                
                testData.put("time", System.currentTimeMillis());
                
                testData.put("test_token", testToken);
                
               	testData.put("qos_result", qosTestResult);               
            }
            catch (final JSONException e1)
            {
                errorMsg = "Error gernerating request";
                e1.printStackTrace();
            }
            
            // getting JSON string from URL
            final JSONObject response = JSONParser.sendJSONToUrl(resultQoSURI, testData);
            // JCqos
            //System.out.println(testData);
            
            if (response != null)
                try
                {
                    final JSONArray errorList = response.getJSONArray("error");
                    
                    // System.out.println(response.toString(4));
                    
                    if (errorList.length() == 0)
                    {
                        
                        // System.out.println("All is fine");
                        
                    }
                    else
                    {
                        for (int i = 0; i < errorList.length(); i++)
                        {
                            if (i > 0)
                                errorMsg += "\n";
                            errorMsg += errorList.getString(i);
                        }
                    }
                    
                    // }
                }
                catch (final JSONException e)
                {
                    errorMsg = "Error parsing server response";
                    e.printStackTrace();
                }
        }
        else
            errorMsg = "No URL to send the Data to.";
        
        return errorMsg;
    }
    
    public void sendNDTResult(final String host, final String pathPrefix, final int port, final boolean encryption,
            final String clientUUID, final UiServicesAdapter data, final String testUuid)
    {
        hostUrl = getUrl(encryption, host, pathPrefix, port, Config.RMBT_TEST_SETTINGS_REQUEST);
        this.clientUUID = clientUUID;
        sendNDTResult(data, testUuid);
    }
    
    public void sendNDTResult(final UiServicesAdapter data, final String testUuid)
    {
        final JSONObject testData = new JSONObject();
        
        try
        {
            testData.put("client_uuid", clientUUID);
            testData.put("client_language", Locale.getDefault().getLanguage());
            if (testUuid != null)
                testData.put("test_uuid", testUuid);
            else
                testData.put("test_uuid", this.testUuid);
            testData.put("s2cspd", data.s2cspd);
            testData.put("c2sspd", data.c2sspd);
            testData.put("avgrtt", data.avgrtt);
            testData.put("main", data.sbMain.toString());
            testData.put("stat", data.sbStat.toString());
            testData.put("diag", data.sbDiag.toString());
            testData.put("time_ns", data.getStartTimeNs() - startTimeNs);
            testData.put("time_end_ns", data.getStopTimeNs() - startTimeNs);
            
            JSONParser.sendJSONToUrl(hostUrl.toURI().resolve(Config.RMBT_CONTROL_NDT_RESULT_URL).toURL(), testData);
            
            System.out.println(testData);
        }
        catch (final JSONException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (URISyntaxException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void addToJSONObject(final JSONObject data, final JSONObject additionalValues) throws JSONException
    {
        if (additionalValues != null && additionalValues.length() > 0)
        {
            final JSONArray attr = additionalValues.names();
            for (int i = 0; i < attr.length(); i++)
                data.put(attr.getString(i), additionalValues.get(attr.getString(i)));
        }
    }
    
    public String getRemoteIp()
    {
        return remoteIp;
    }
    
    public String getClientUUID()
    {
        return clientUUID;
    }
    
    public String getServerName()
    {
        return serverName;
    }

    public String getLoopUuid()
    {
        if (loopUuid != null && !loopUuid.isEmpty()) {
            return loopUuid;
        }
        return null;
    }
    
    public String getProvider()
    {
        return provider;
    }
    
    public long getTestTime()
    {
        return testTime;
    }
    
    /**
     * this time stamp is only a relative timestamp (see: {@link System#nanoTime()})
     * @return
     */
    public long getStartTimeNs() {
    	return startTimeNs;
    }
    
    /**
     * this is the starting (= timestamp of the test request) UNIX timestamp (see: {@link System#currentTimeMillis()})
     * @return
     */
    public long getStartTimeMillis() {
    	return startTimeMillis;
    }
    
    public String getTestUuid()
    {
        return testUuid;
    }
    
    public JSONObject getLastTestResult() {
    	return lastTestResult;
    }
    
    public RMBTTestParameter getTestParameter(RMBTTestParameter overrideParams)
    {
        String host = testHost;
        int port = testPort;
        boolean encryption = testEncryption;
        int duration = testDuration;
        int numThreads = testNumThreads;
        int numPings = testNumPings;
        
        if (overrideParams != null)
        {
            if (overrideParams.getHost() != null && overrideParams.getPort() > 0)
            {
                host = overrideParams.getHost();
                encryption = overrideParams.isEncryption();
                port = overrideParams.getPort();
            }
            if (overrideParams.getDuration() > 0)
                duration = overrideParams.getDuration();
            if (overrideParams.getNumThreads() > 0)
                numThreads = overrideParams.getNumThreads();
        }

        return new RMBTTestParameter(host, port, encryption, testToken, duration, numThreads, numPings, testTime, serverType);
    }

	public Set<ErrorStatus> getLastErrorList() {
		return lastErrorList;
	}
}
