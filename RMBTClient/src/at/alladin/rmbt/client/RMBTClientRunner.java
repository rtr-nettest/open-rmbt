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
package at.alladin.rmbt.client;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.SSLContext;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.measurementlab.ndt.NdtTests;

import org.json.JSONException;
import org.json.JSONObject;

import at.alladin.rmbt.client.helper.Config;
import at.alladin.rmbt.client.helper.RevisionHelper;
import at.alladin.rmbt.client.helper.TestStatus;
import at.alladin.rmbt.client.ndt.NDTRunner;
import at.alladin.rmbt.client.v2.task.QoSTestEnum;
import at.alladin.rmbt.client.v2.task.result.QoSResultCollector;
import at.alladin.rmbt.client.v2.task.service.TestSettings;

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
                acceptsAll(Arrays.asList("?", "help"), "show help");
                
                acceptsAll(Arrays.asList("u", "uuid"), "client-uuid; if this uuid is specified the controlserver is contacted for the token; either uuid or token is needed").withRequiredArg()
                        .ofType(String.class);
                
                acceptsAll(Arrays.asList("token"), "token to access rmbt server directly; in this case the controlserver is not contacted and uuid is not needed").withRequiredArg()
                        .ofType(String.class);
                
                acceptsAll(Arrays.asList("h", "host"), "RMBT server IP or hostname (required)").withRequiredArg()
                        .ofType(String.class);
                
                acceptsAll(Arrays.asList("p", "port"), "RMBT server port (required)").withRequiredArg().ofType(
                        Integer.class);
                
                acceptsAll(Arrays.asList("s", "ssl"), "use SSL/TLS");
                
                acceptsAll(Arrays.asList("ssl-no-verify"), "turn off SSL/TLS certificate validation");
                
                acceptsAll(Arrays.asList("t", "threads"), "number of threads")
                        .withRequiredArg().ofType(Integer.class);
                
                acceptsAll(Arrays.asList("d", "duration"), "test duration in seconds")
                        .withRequiredArg().ofType(Integer.class);
                
                acceptsAll(Arrays.asList("n", "ndt"), "run NDT after RMBT");
                
                acceptsAll(Arrays.asList("ndt-host"), "NDT host to use").withRequiredArg()
                        .ofType(String.class);
                
                acceptsAll(Arrays.asList("q", "qos"), "run qos tests");
                
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
        
        final String[] requiredArgs = { "h", "p" };
        
        if (options.has("ssl-no-verify"))
            SSLContext.setDefault(RMBTClient.getSSLContext(null, null));
        else
            SSLContext.setDefault(RMBTClient.getSSLContext("at/alladin/rmbt/crt/ca.pem",
                    "at/alladin/rmbt/crt/controlserver.pem"));
        
        boolean reqArgMissing = false;
        if (!options.has("?"))
            for (final String arg : requiredArgs)
                if (!options.has(arg))
                {
                    reqArgMissing = true;
                    System.out.println(String.format("ERROR: required argument '%s' is missing", arg));
                }
        
        if (! options.has("u") && ! options.has("token"))
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
        
        final RMBTClient client;
        
        final String host = (String) options.valueOf("h");
        final int port = (Integer) options.valueOf("p");
        final boolean encryption = options.has("s") ? true : false;
        
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
        
        
        int numThreads = 0;
        int duration = 0;
        if (options.has("t"))
            numThreads = (Integer) options.valueOf("t");
        if (options.has("d"))
            duration = (Integer) options.valueOf("d");
        
        int numPings = 10;
        
        RMBTTestParameter overrideParams = null;
        if (numThreads > 0 || duration > 0)
            overrideParams = new RMBTTestParameter(null, 0, false, duration, numThreads, numPings);
            
        
        if (options.has("token")) // direct mode
        {
            // set defaults
            if (numThreads == 0)
                numThreads = 3;
            
            if (duration == 0)
                duration = 7;
            
            final String token = (String) options.valueOf("token");
            final long startTime = System.currentTimeMillis(); // startTime = now
            
            final RMBTTestParameter params = new RMBTTestParameter(host, port, encryption, token, duration, numPings, numPings, startTime);
            
            client = new RMBTClient(params, null);
        }
        else
        {
            final String uuid = (String) options.valueOf("u");
            
            // standard mode with contact to control server
            client = RMBTClient.getInstance(host, null, port, encryption, geoInfo, uuid,
                    "DESKTOP", Config.RMBT_CLIENT_NAME, Config.RMBT_VERSION_NUMBER, overrideParams, null);
        }
        
        if (client != null)
        {
            final TestResult result = client.runTest();
            
            if (result != null)
            {
                final JSONObject jsonResult = new JSONObject();
                try
                {
                    jsonResult.put("network_type", "97");
                    jsonResult.put("plattform", "CLI");
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                client.sendResult(jsonResult);
            }
            
            client.shutdown();
            
			try {
			    if (options.has("q") && client.getControlConnection() != null)
			    {
    				System.out.print("Starting QoS Test... ");
                	TestSettings nnTestSettings = new TestSettings(client.getControlConnection().getStartTimeNs());
    				QualityOfServiceTest nnTest = new QualityOfServiceTest(client, nnTestSettings);
    				QoSResultCollector nnResult = nnTest.call();
                	System.out.println("finished.");
    				if (nnResult != null && nnTest.getStatus().equals(QoSTestEnum.QOS_FINISHED)) {
    					System.out.print("Sending QoS results... ");
    					client.sendQoSResult(nnResult);
    					System.out.println("finished");
    				}
    				else {
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
//                            if (viewId == MAIN_VIEW)
                            System.out.println(str);
                        }
                        
                        @Override
                        public void sendResults()
                        {
                            System.out.println("sending NDT results...");
                            client.getControlConnection().sendNDTResult(this, null);
                        }
                    });
                    
                    System.out.println("NDT finished.");
                }
            }
        }
        
    }
    
}
