/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
 * Copyright 2013-2015 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
package at.rtr.rmbt.client;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import javax.net.ssl.SSLContext;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.measurementlab.ndt.NdtTests;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import at.rtr.rmbt.client.helper.Config;
import at.rtr.rmbt.client.helper.Globals;
import at.rtr.rmbt.client.helper.RevisionHelper;
import at.rtr.rmbt.client.helper.TestStatus;
import at.rtr.rmbt.client.helper.SettingsServerConnection;
import at.rtr.rmbt.client.helper.BrowserShow;
import at.rtr.rmbt.client.ndt.NDTRunner;
import at.rtr.rmbt.client.v2.task.QoSTestEnum;
import at.rtr.rmbt.client.v2.task.result.QoSResultCollector;
import at.rtr.rmbt.client.v2.task.service.TestSettings;
import at.rtr.rmbt.client.RMBTClientJFrame;
import at.rtr.rmbt.client.RMBTClientJFrame.ClientMode;

import java.io.PrintStream;
import javax.swing.*;

public class RMBTClientRunner
{
    
    /**
     * @param args
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static void main(final String[] args) throws IOException, InterruptedException, KeyManagementException,
            NoSuchAlgorithmException
    {
        final OptionParser parser = new OptionParser()
        {
            {
                acceptsAll(Arrays.asList("?", "help"), "Show help");
                
                acceptsAll(Arrays.asList("u", "uuid"), "Client-uuid; if this uuid is specified the controlserver is contacted for the token; either uuid or token is needed").withRequiredArg()
                        .ofType(String.class);
                
                acceptsAll(Arrays.asList("token"), "Token to access rmbt server directly; in this case the controlserver is not contacted and uuid is not needed").withRequiredArg()
                        .ofType(String.class);
                
                acceptsAll(Arrays.asList("h", "host"), "RMBT server IP or hostname (required)").withRequiredArg()
                        .ofType(String.class);
                
                acceptsAll(Arrays.asList("p", "port"), "RMBT server port (required)").withRequiredArg().ofType(
                        Integer.class);
                
                //acceptsAll(Arrays.asList("s", "ssl"), "use SSL/TLS");
                
                acceptsAll(Arrays.asList("ssl-no-verify"), "Turn off SSL/TLS certificate validation (default)");
                acceptsAll(Arrays.asList("ssl-verify"), "Turn on SSL/TLS certificate validation");

                acceptsAll(Arrays.asList("o", "open"), "Open test result in browser");
                
                acceptsAll(Arrays.asList("t", "threads"), "Number of threads")
                        .withRequiredArg().ofType(Integer.class);
                
                acceptsAll(Arrays.asList("d", "duration"), "Test duration in seconds")
                        .withRequiredArg().ofType(Integer.class);
                
                acceptsAll(Arrays.asList("n", "ndt"), "Run NDT after RMBT");
                
                acceptsAll(Arrays.asList("ndt-host"), "NDT host to use").withRequiredArg()
                        .ofType(String.class);
                
                acceptsAll(Arrays.asList("q", "qos"), "Run QoS tests");

                acceptsAll(Arrays.asList("log", "log"), "Show detailed log");

                //acceptsAll(Arrays.asList("a", "tc"), "accept terms & conditions");

                acceptsAll(Arrays.asList("v", "verbose"), "Log data for gui");
                     
                acceptsAll(Arrays.asList("tag"), "Tag the test with custom word").withRequiredArg()
                        .ofType(String.class);

                acceptsAll(Arrays.asList("g", "gui"), "Show the test progress in Graphical User Interface");

                acceptsAll(Arrays.asList("l", "loop"), "Enable loop mode with defined count of measurements")
                    .withRequiredArg().ofType(Integer.class);

                acceptsAll(Arrays.asList("i", "interval"), "Interval in seconds between two measurements in loop mode")
                    .withRequiredArg().ofType(Integer.class);    

                acceptsAll(Arrays.asList("server-type"),"Type of test server to use, if a token is supplied (default: RMBT)");

                // --override net_type=WLAN,net_tech=WLAN,platform=DARWIN,device=Desktop_arm64,app_version=1.0.8

                acceptsAll(Arrays.asList("platform", "platform"), "Platform").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("os", "os"), "Operation system").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("model", "model"), "Model").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("osver", "osver"), "Operation system version").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("type", "type"), "Type").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("nettype", "nettype"), "Network type").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("set-version", "set-version"), "Set version").withRequiredArg().ofType(String.class);

                // loop mode
                acceptsAll(Arrays.asList("user-loop-mode", "user-loop-mode"), "User loop mode");
                acceptsAll(Arrays.asList("user-loop-mode-max-delay", "user-loop-mode-max-delay"), "User loop mode max delay").withRequiredArg().ofType(Integer.class);
                acceptsAll(Arrays.asList("user-loop-mode-test-counter", "user-loop-mode-test-counter"), "User loop mode test counter").withRequiredArg().ofType(Integer.class);
                acceptsAll(Arrays.asList("user-loop-mode-uuid", "user-loop-mode-uuid"), "User loop mode local UUID").withRequiredArg().ofType(String.class);
                
                // qMON json
                acceptsAll(Arrays.asList("qmon", "qmon"), "qMON JSON data").withRequiredArg().ofType(String.class);

                // JSON result
                acceptsAll(Arrays.asList("json-result", "json-result"), "Show log JSON result");
            }
        };
        
        System.out.println(String.format("=============== RMBTClient %s ===============",
                RevisionHelper.getVerboseRevision()));
        
        OptionSet options;
        try
        {
            options = parser.parse(args);
        }
        catch (final OptionException e)
        {
            System.out.println(String.format("error while parsing command line options: %s", e.getLocalizedMessage()));
            System.exit(1);
            return;
        }
        

        boolean reqArgMissing = false;

        boolean guiWindow = options.has("g");

        if (guiWindow){
            // show GUI to see test outputs

            RMBTClientJFrame frame = new RMBTClientJFrame(Config.APPLICATION_NAME, RMBTClientJFrame.ClientMode.NORMAL);

            // redirect outputs to gui
            System.setOut(new PrintStream(frame.getOutputStream()));
            System.setErr(new PrintStream(frame.getOutputStream()));

            // show GUI
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    frame.setVisible(true);
                }
            });
        }

        String uuid = "";
        
        // Host
        String host = (String) Config.SERVER_DEFAULT;
        if (options.has("h") ){
            host = (String) options.valueOf("h");
        }

        // Port
        int port = (Integer) Config.PORT_HTTPS;
        if (options.has("p") ){
            port = (Integer) options.valueOf("p");
        }
        System.out.println(String.format("Server: '%s:%d'", host, port));

        final String tag = (String) options.valueOf("tag");

        //final boolean encryption = options.has("s") ? true : false;   
        final boolean encryption = true;   

        // DATA for GUI
        if (options.has("log"))
        { 
            Globals.DEBUG_CLI = true;
        }
        else{
            Globals.DEBUG_CLI = false;
        }

        // DATA for GUI
        if (options.has("v"))
        { 
            System.out.println("Data for GUI will be seen");
            Globals.DEBUG_CLI_GUI = true;
        }
        else{
            Globals.DEBUG_CLI_GUI = false;
        }

        // JSON RESULT
        if (options.has("json-result"))
        { 
            Globals.JSON_RESULT = true;
        }
        else{
            Globals.JSON_RESULT = false;
        }

        // Accept policy and privacy, tmp out
        /* 
        if (! options.has("a") )
        { 
            System.out.println();
            System.out.println("Terms and conditions not accecpted");
            parser.printHelpOn(System.out);
            System.exit(1);
            return;
        }
        */

        final JSONObject jsonInputParms = new JSONObject();

        if (options.has("platform")){
            jsonInputParms.put("platform", (String) options.valueOf("platform")); 
        }
        if (options.has("model")){
            jsonInputParms.put("model", (String) options.valueOf("model")); 
        }
        if (options.has("os")){
            jsonInputParms.put("os", (String) options.valueOf("os")); 
        }
        if (options.has("type")){
            jsonInputParms.put("type", (String) options.valueOf("type")); 
        }
        if (options.has("osver")){
            jsonInputParms.put("osver", (String) options.valueOf("osver")); 
        }
        if (options.has("nettype")){
            jsonInputParms.put("nettype", (String) options.valueOf("nettype")); 
        }
        if (options.has("set-version")){
            jsonInputParms.put("set-version", (String) options.valueOf("set-version")); 
        }

        // Loop mode
        boolean user_loop_mode = false;
        final JSONObject jsonLoopData = new JSONObject();
        if (options.has("user-loop-mode")){
            user_loop_mode = true; 
        }
        if (user_loop_mode){
            if (options.has("user-loop-mode-max-delay")){
                jsonLoopData.put("max_delay", (Integer) options.valueOf("user-loop-mode-max-delay"));
            }
            if (options.has("user-loop-mode-test-counter")){
                jsonLoopData.put("test_counter", (Integer) options.valueOf("user-loop-mode-test-counter"));
            }
            if (options.has("user-loop-mode-uuid")){
                jsonLoopData.put("loop_uuid", (String) options.valueOf("user-loop-mode-uuid"));
            }

        }

        JSONObject jsonQmonData = new JSONObject();
        if (options.has("qmon")){
            // java -jar RMBTClient/build/libs/RMBTClient-all.jar -d 1 --qmon '{"location":{"accuracy":19.385,"age":1651362485,"altitude":91.9000015258789,"bearing":0.0,"geo_lat":45.8869188,"geo_long":13.5924854,"mock_location":false,"provider":"network","satellites":0,"speed":0.0,"tstamp":1740046168204,"time_ns":-1130837923}, "temperature": 30.9}'
            jsonQmonData = new JSONObject((String)options.valueOf("qmon"));
        }
    
        

        //if (true){
        //    System.exit(1);
        //    return;        
        //}
        

        // UUID parm
        if (options.has("u") ){
            uuid = (String) options.valueOf("u");

            System.out.println(String.format("Client UUID entered as a startup parameter. Client UUID: %s", uuid));

        }
        else{
            System.out.print("Client UUID NOT entered as a startup parameter! Try to get it from server side...\n");
        }
        
        // Go to WEB
        if (uuid == "") 
        {
            SettingsServerConnection settings = new SettingsServerConnection();
            uuid = settings.SettingsConnection(host, encryption, uuid, port, null);
        }

        if (uuid == "" && ! options.has("token"))
        {
            reqArgMissing = true;
            System.out.println(String.format("ERROR: either uuid or token is needed"));
        }
        
        if (options.has("?") || reqArgMissing)
        {
            System.out.println();
            parser.printHelpOn(System.out);
            System.exit(1);
            return;
        }

        final String serverType;
        if (options.has("server-type")) {
            serverType = options.valueOf("server-type").toString();
        } else {
            serverType = Config.SERVER_TYPE_RMBT;
        }
        
        final ArrayList<String> geoInfo = null;
        
        final JSONObject additionalValues = new JSONObject();
        try
        {
            additionalValues.put("ndt", options.has("n"));
            additionalValues.put("plattform", "CLI");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        final boolean openResultsInBrowser = options.has("o");

        int numThreads = 0;
        int duration = 0;
        if (options.has("t"))
            numThreads = (Integer) options.valueOf("t");
        if (options.has("d"))
            duration = (Integer) options.valueOf("d");

    
        int numPings = 10;

        int loop = 0;
        if (options.has("l"))
            loop = (Integer) options.valueOf("l");     
  
        int interval = 0;
        if (options.has("i"))
            interval = (Integer) options.valueOf("interval");     

        if (loop != 0)
        { 
            //System.out.println("Loop mode started, number of tests: " + loop + " with interval [s]: " + interval + "");
            System.out.println(String.format("Loop mode started, number of tests: %d, interval [s]: %d", loop, interval));

            int countOfMeasurements = 0;
            do {

                countOfMeasurements++;
                System.out.println("Starting new test in loop: " + countOfMeasurements);

                performTest(uuid, host, port, tag, openResultsInBrowser, options, encryption, geoInfo, duration, numThreads, numPings, loop,  jsonInputParms, jsonLoopData, jsonQmonData);
                try {
                    // sleep for one minute
                    if (loop != countOfMeasurements){
                        System.out.println("Sleeping for " + (interval) + " seconds...");
                        Thread.sleep(interval*1000);
                    }
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }

            } while (loop != countOfMeasurements);

        }
        else{
            performTest(uuid, host, port, tag, openResultsInBrowser, options, encryption, geoInfo, duration, numThreads, numPings, loop, jsonInputParms, jsonLoopData, jsonQmonData);
        }

        RMBTClient.getCommonThreadPool().shutdownNow();
    }

    private static void performTest(String uuid, String host, int port, String tag, boolean openResultsInBrowser, 
        OptionSet options, boolean encryption, ArrayList<String> geoInfo, int duration, int numThreads, int numPings, int loop,  JSONObject jsonInputParms, JSONObject jsonLoopData, JSONObject jsonQmonData) throws IOException, InterruptedException, KeyManagementException,
        NoSuchAlgorithmException{
    	
        final RMBTClient client;

        if (options.has("ssl-verify"))
            SSLContext.setDefault(RMBTClient.getSSLContext("at/rtr/rmbt/crt/ca.pem",
                    "at/rtr/rmbt/crt/controlserver.pem"));
        else
            SSLContext.setDefault(RMBTClient.getSSLContext(null, null));

        System.out.println("STARTING TEST.");

        RMBTTestParameter overrideParams = null;

        if (numThreads > 0 || duration > 0)
            overrideParams = new RMBTTestParameter(null, 0, false, duration, numThreads, numPings);
            
        //System.out.println(String.format("START options %s", options));

        String setVersion = Config.RMBT_VERSION_NUMBER;
        if (jsonInputParms.has("set-version"))
            setVersion = jsonInputParms.getString("set-version");

        client = RMBTClient.getInstance(host, null, port, encryption, geoInfo, uuid,
            "DESKTOP", Config.RMBT_CLIENT_NAME, setVersion, overrideParams, null, jsonLoopData, jsonQmonData);

        String loopUuid = client.getLoopUuid();

        // JC_STATUS
        client.setStatus(TestStatus.NOT_STARTED);

        if (client != null)
        {
            final TestResult result = client.runTest(host);
            
            if (result != null)
            {
                final JSONObject jsonResult = new JSONObject();
                try
                {
                    if (jsonInputParms.has("nettype"))
                        jsonResult.put("network_type", jsonInputParms.getString("nettype"));
                    else
                        jsonResult.put("network_type", "98"); // default value 98=LAN

                    if (jsonInputParms.has("platform"))
                        jsonResult.put("plattform", jsonInputParms.getString("platform"));
                    else
                        jsonResult.put("plattform", "desktop"); // default value =desktop

                    if (jsonInputParms.has("model"))
                        jsonResult.put("model", jsonInputParms.getString("model"));

                    if (jsonInputParms.has("os"))
                        jsonResult.put("operating_system", jsonInputParms.getString("os"));

                    if (jsonInputParms.has("type"))
                        jsonResult.put("type", jsonInputParms.getString("type"));

                    if (jsonInputParms.has("osver"))
                        jsonResult.put("os_version", jsonInputParms.getString("osver"));

                    //System.out.println(jsonResult);

                    if (tag != null) 
                        jsonResult.put("tag", tag);

                    if (loopUuid != null) 
                        jsonResult.put("loop_uuid", loopUuid);

                    // Location
                    if (jsonQmonData.length() != 0 && jsonQmonData.has("location"))           
                    {
                        ArrayList<JSONObject> geoLocList = new ArrayList<JSONObject>();
                        geoLocList.add(jsonQmonData.getJSONObject("location"));
                        jsonResult.put("geoLocations", geoLocList);
                    }

                    // Opererator info
                    if (jsonQmonData.has("operatorinfo")){
                        JSONObject operatorinfo = jsonQmonData.getJSONObject("operatorinfo");
                        if (operatorinfo.has("telephony_network_operator_name"))
                            jsonResult.put("telephony_network_operator_name", operatorinfo.getString("telephony_network_operator_name"));
                        if (operatorinfo.has("telephony_network_sim_operator_name"))
                            jsonResult.put("telephony_network_sim_operator_name", operatorinfo.getString("telephony_network_sim_operator_name"));
                        if (operatorinfo.has("telephony_network_operator"))
                            jsonResult.put("telephony_network_operator", operatorinfo.getString("telephony_network_operator"));
                        if (operatorinfo.has("telephony_network_is_roaming"))    
                            jsonResult.put("telephony_network_is_roaming", operatorinfo.getBoolean("telephony_network_is_roaming"));
                        if (operatorinfo.has("telephony_network_sim_operator"))
                            jsonResult.put("telephony_network_sim_operator", operatorinfo.getString("telephony_network_sim_operator"));
                        if (operatorinfo.has("telephony_network_sim_country"))
                            jsonResult.put("telephony_network_sim_country", operatorinfo.getString("telephony_network_sim_country"));
                    }

                    // Radio info
                    if (jsonQmonData.has("radioinfo")){
                        JSONObject radioinfo = jsonQmonData.getJSONObject("radioinfo");
                        String celluuid = UUID.randomUUID().toString(); 

                        if (radioinfo.has("cells") && radioinfo.getJSONArray("cells").length() != 0){

                            String celltechnology = "";
                            if (radioinfo.getJSONArray("cells").getJSONObject(0).has("technology"))
                                celltechnology = radioinfo.getJSONArray("cells").getJSONObject(0).getString("technology");
                            
                            if(celltechnology.equals("LTE"))
                                celltechnology = "4G";
                            else if(celltechnology.contains("5G"))
                                celltechnology = "5G";
    
                            radioinfo.getJSONArray("cells").getJSONObject(0).put("technology", celltechnology);     
                            radioinfo.getJSONArray("cells").getJSONObject(0).put("uuid", celluuid);  

                        }

                        if (radioinfo.has("signals") && radioinfo.getJSONArray("signals").length() != 0){
                            radioinfo.getJSONArray("signals").getJSONObject(0).put("cell_uuid", celluuid);  
                        }

                        jsonResult.put("radioInfo", radioinfo);
                    }

                        
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                // JC_STATUS
                client.setStatus(TestStatus.SUBMITTING_RESULTS);

                client.sendResult(jsonResult);

                // JC_STATUS
                client.setStatus(TestStatus.SPEEDTEST_END);
            }        
            
            client.shutdown();
            
			try {
			    if (options.has("q") && client.getControlConnection() != null)
			    {
                    if (Globals.DEBUG_CLI)
    				    System.out.print("Starting QoS Test... ");

                    // JC_STATUS
                    client.setStatus(TestStatus.QOS_TEST_RUNNING);

                	TestSettings nnTestSettings = new TestSettings(client.getControlConnection().getStartTimeNs());
    				QualityOfServiceTest nnTest = new QualityOfServiceTest(client, nnTestSettings);
    				QoSResultCollector nnResult = nnTest.call();
                    if (Globals.DEBUG_CLI)
                        System.out.println("Finished.");
    				if (nnResult != null && nnTest.getStatus().equals(QoSTestEnum.QOS_FINISHED)) {
    					if (Globals.DEBUG_CLI)
                            System.out.print("Sending QoS results... ");

                        // JC_STATUS
                        client.setStatus(TestStatus.SUBMITTING_QOS_RESULTS);

    					client.sendQoSResult(nnResult);
                        if (Globals.DEBUG_CLI)
                            System.out.println("Finished QoS.");

                        // JC_STATUS
                        client.setStatus(TestStatus.QOS_END);
    				}
    				else {
                        if (Globals.DEBUG_CLI)
                            System.out.println("Error during QoS test.");
    				}
			    }
            	
			} catch (Exception e) {
				e.printStackTrace();
			}                            	                    	

                       
            if (client.getStatus() != TestStatus.SPEEDTEST_END && client.getStatus() != TestStatus.QOS_END)
            {
                System.out.println("ERROR: " + client.getErrorMsg());
                System.out.println("Status: " + client.getStatus());
            }
            else
            {
                if (options.has("n"))
                {
                    if (Globals.DEBUG_CLI)
                        System.out.println("\n\nStarting NDT...");
                    
                    String ndtHost = null;
                    if (options.has("ndt-host"))
                        ndtHost = (String) options.valueOf("ndt-host");
                    
                    final NDTRunner ndtRunner = new NDTRunner(ndtHost);
                    ndtRunner.runNDT(NdtTests.NETWORK_WIRED, ndtRunner.new UiServices()
                    {
                        @Override
                        public void appendString(String str, int viewId)
                        {
                            super.appendString(str, viewId);
                        }
                        
                        @Override
                        public void sendResults()
                        {
                            if (Globals.DEBUG_CLI)    
                                System.out.println("sending NDT results...");
                            client.getControlConnection().sendNDTResult(this, null);
                        }
                    });
                    if (Globals.DEBUG_CLI)
                        System.out.println("NDT finished.");
                }
            }

            System.out.println("ENDING TEST.");
            
            // JC_STATUS
            client.setStatus(TestStatus.END);

            if (result != null){
                if (openResultsInBrowser) {
                    // open browser
                    System.out.println("Trying to open following URL with test result in default browser: " + result.result_link);
                    BrowserShow.showResults(result.result_link);
                }
                else
                {
                    System.out.println(String.format("To see test result open browser with url: %s", result.result_link));
                }

                // JSON result
                if(Globals.JSON_RESULT){
                    JSONObject json_result = new JSONObject();
                    try
                    {
                        json_result.put("client_uuid", uuid);
                        json_result.put("test_server", result.test_server);
                        json_result.put("registration_server", result.registration_server);
                        json_result.put("test_open_uuid", result.test_uuid);
                        json_result.put("result_link", result.result_link);
                        json_result.put("download_kbps", result.test_download);
                        json_result.put("upload_kbps", result.test_upload);
                        json_result.put("ping_ms", result.test_ping);

                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                    System.out.println(json_result);
                }
                
            }
  
        }    
    }
    
}
