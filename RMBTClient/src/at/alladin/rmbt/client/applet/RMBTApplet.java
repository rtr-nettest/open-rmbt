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
package at.alladin.rmbt.client.applet;

import java.applet.Applet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import net.measurementlab.ndt.NdtTests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.alladin.rmbt.client.QualityOfServiceTest;
import at.alladin.rmbt.client.RMBTClient;
import at.alladin.rmbt.client.TestResult;
import at.alladin.rmbt.client.helper.Config;
import at.alladin.rmbt.client.helper.ControlServerConnection;
import at.alladin.rmbt.client.helper.IntermediateResult;
import at.alladin.rmbt.client.helper.NdtStatus;
import at.alladin.rmbt.client.helper.RevisionHelper;
import at.alladin.rmbt.client.helper.TestStatus;
import at.alladin.rmbt.client.ndt.NDTRunner;
import at.alladin.rmbt.client.v2.task.QoSTestEnum;
import at.alladin.rmbt.client.v2.task.result.QoSResultCollector;
import at.alladin.rmbt.client.v2.task.service.TestSettings;

public class RMBTApplet extends Applet
{
	public final static String PARAM_RUN_QOS = "runQos";
	public final static String PARAM_RUN_NDT = "runNdt";
	public final static String PARAM_QOS_SSL = "qosSsl";
	
    private static final long serialVersionUID = 1L;

    private RMBTClient client;
    
    private volatile boolean start = false;
    private volatile boolean runRMBT = false;
    private volatile boolean ndtActivated = false;
    
    private volatile String uuid;
    
    private volatile ArrayList<String> geoInfo = null;
    
    private final AtomicReference<NDTRunner> ndtRunnerHolder = new AtomicReference<NDTRunner>();
    
    private volatile Integer zip;
    private volatile String product;
    
    @Override
    public void init()
    {
        System.out.println(String.format("initializing applet %s", RevisionHelper.getVerboseRevision()));
        System.out.println("supported HTML named parameters: ");
        System.out.println("runNdt \n\toptions: true, false \t default: false \t functionality: run NDT test");
        System.out.println("runQos \n\toptions: true, false \t default: false \t functionality: run QoS test");
        System.out.println("qosSsl \n\toptions: true, false \t default: true  \t functionality: run QoS test with SSL");
        
        String param = getParameter(PARAM_RUN_QOS);
        final boolean runQoS= Boolean.parseBoolean(param == null ? "false" : param.trim());
        
        param = getParameter(PARAM_QOS_SSL);
        final boolean useSslForQoS = Boolean.parseBoolean(param == null ? "true" : param.trim());
        
        param = getParameter(PARAM_RUN_NDT);
        final boolean runNdt = Boolean.parseBoolean(param == null ? "false" : param.trim());

        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    synchronized (RMBTApplet.this)
                    {
                        while (!start)
                            RMBTApplet.this.wait(); // wait for start signal
                    }
                    
                    System.out.println("got start signal");
                                                            
                    final int port = 443;
                    final boolean encryption = true;
                    
                    // Leere GeoInfoS
                    // ArrayList<String> geoInfo = null;
                    
                    // uuid fuer tests
                    
                    if (runRMBT)
                    {
                        final JSONObject additionalValues = new JSONObject();
                        try
                        {
                            additionalValues.put("ndt", ndtActivated);
                            additionalValues.put("plattform", "Applet");
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                        
                        client = RMBTClient.getInstance(getParameter("host"), getParameter("path"),
                                port, encryption, geoInfo, uuid, "DESKTOP", Config.RMBT_CLIENT_NAME,
                                Config.RMBT_VERSION_NUMBER, null, additionalValues);
                                                
                        final TestResult result = client.runTest();
                        if (result != null)
                        {
                            final JSONObject jsonResult = new JSONObject();
                            
                            try
                            {
                                if (geoInfo != null)
                                {
                                    final JSONArray itemList = new JSONArray();
                                    
                                    final JSONObject jsonItem = new JSONObject();
                                    
                                    jsonItem.put("tstamp", geoInfo.get(0));
                                    jsonItem.put("geo_lat", geoInfo.get(1));
                                    jsonItem.put("geo_long", geoInfo.get(2));
                                    jsonItem.put("accuracy", geoInfo.get(3));
                                    jsonItem.put("altitude", geoInfo.get(4));
                                    jsonItem.put("bearing", geoInfo.get(5));
                                    jsonItem.put("speed", geoInfo.get(6));
                                    jsonItem.put("provider", geoInfo.get(7));
                                    
                                    itemList.put(jsonItem);
                                    
                                    jsonResult.put("geoLocations", itemList);
                                }
                                
                                if (zip != null)
                                    jsonResult.put("zip_code", zip);
                                
                                if (product != null)
                                {
                                    jsonResult.put("model", product);
                                    jsonResult.put("product", product);
                                }
                                
                                jsonResult.put("plattform", "Applet");
                                
                                jsonResult.put("ndt", ndtActivated);
                                
                                jsonResult.put("network_type", 98);
                                
                            }
                            catch (final JSONException e)
                            {
                                e.printStackTrace();
                            }
                            
                            client.sendResult(jsonResult);
                        }
                        
                        System.out.println("Status: " + client.getStatus());
                        System.out.println("Checking QoS...");
                        if (runQoS && client.getStatus() == TestStatus.SPEEDTEST_END)
                        {
                        	System.out.println("On. Starting QoS...");
                        	try {
                        		client.setStatus(TestStatus.QOS_TEST_RUNNING);
                            	final TestSettings qosTestSettings = new TestSettings(client.getControlConnection().getStartTimeNs());
                            	System.out.println(qosTestSettings);
                            	qosTestSettings.setUseSsl(useSslForQoS);
                            	final QualityOfServiceTest qosTest = new QualityOfServiceTest(client, qosTestSettings);
								QoSResultCollector qosResult = qosTest.call();
								if (qosResult != null && qosTest.getStatus().equals(QoSTestEnum.QOS_FINISHED)) {
	                                System.out.println("Sending QoS results...");
									client.sendQoSResult(qosResult);
								}
								System.out.println("QoS finished.");
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
		                        client.setStatus(TestStatus.QOS_END);
							}
                        }
                        else {
                        	System.out.println("Off. Not running QoS.");
                            client.setStatus(TestStatus.QOS_END);
                        }
                        
                        client.shutdown();
                    }                    
                    
                    System.out.println("Status: " + client.getStatus());
                    System.out.println("Checking NDT...");
                    if (runNdt && (client.getStatus() == TestStatus.QOS_END || client.getStatus() == TestStatus.SPEEDTEST_END))
                    {
                    	System.out.println("On. Starting NDT...");
                        final NDTRunner ndtRunner = new NDTRunner();
                        ndtRunnerHolder.set(ndtRunner);
                        ndtRunner.runNDT(NdtTests.NETWORK_WIRED, ndtRunner.new UiServices()
                        {
                            @Override
                            public void sendResults()
                            {
                                System.out.println("Sending NDT results...");
                                final ControlServerConnection csc = new ControlServerConnection();
                                csc.sendNDTResult(getParameter("host"), getParameter("path"),
                                        port, encryption, uuid, this, client.getTestUuid());
                                System.out.println("NDT finished.");
                            }
                        });
                    }
                    else {
                    	System.out.println("Off. Not running NDT.");
                    }
                    
                    //set status to end:
                    if (client.getStatus() != TestStatus.ERROR ) {
                    	client.setStatus(TestStatus.END);
                    	System.out.println("TEST FINISHED. Status: " + client.getStatus());
                    }
                    else {
                    	System.out.println("ERROR: " + client.getErrorMsg());
                    }
                }
                catch (final InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
        };
        new Thread(runnable).start();
    }
    
    synchronized public void startTest(final String uuid)
    {
        System.out.println(String.format("starting test. uuid: %s", uuid));
        
        this.uuid = uuid;
        
        start = true;
        runRMBT = true;
        notify();
    }
    
    public IntermediateResult getIntermediateResult()
    {
        if (client == null)
            return null;
        return client.getIntermediateResult(null);
    }
    
    public NdtStatus getNdtStatus()
    {
        final NDTRunner ndtRunner = ndtRunnerHolder.get();
        if (ndtRunner == null)
            return null;
        return ndtRunner.getNdtStatus();
    }
    
    public float getNdtProgress()
    {
        final NDTRunner ndtRunner = ndtRunnerHolder.get();
        if (ndtRunner == null)
            return 0;
        return ndtRunner.getNdtProgress();
    }
    
    public String getPublicIP()
    {
        if (client == null)
            return null;
        return client.getPublicIP();
    }
    
    public String getServerName()
    {
        if (client == null)
            return null;
        return client.getServerName();
    }
    
    public String getProvider()
    {
        if (client == null)
            return null;
        return client.getProvider();
    }
    
    public String getTestUuid()
    {
        if (client == null)
            return null;
        return client.getTestUuid();
    }
    
    public void setLocation(final Number posTimestamp, final Number posLat, final Number posLong,
            final Number posAccuracy, final Number posAltitude, final Number posHeading, final Number posSpeed)
    {
        
        try
        {
            geoInfo = new ArrayList<String>(Arrays.asList(String.valueOf(posTimestamp), String.valueOf(posLat),
                    String.valueOf(posLong), String.valueOf(posAccuracy), String.valueOf(posAltitude),
                    String.valueOf(posHeading), String.valueOf(posSpeed), "Browser"));
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void setZip(final Integer zip)
    {
        this.zip = zip;
    }
    
    public void setBrowserInfo(final String product)
    {
        if (product != null && product.length() > 0)
            this.product = product;
    }
    
    public void setNDTActivated(final boolean ndtActivated)
    {
        this.ndtActivated = ndtActivated;
    }
    
}
