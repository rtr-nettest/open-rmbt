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
package at.alladin.rmbt.android.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import at.alladin.rmbt.android.adapter.result.OnCompleteListener;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.util.net.NetworkInfoCollector;

public class CheckIpTask extends AsyncTask<Void, Void, JSONArray>
{
    private final RMBTMainActivity activity;
    
    private JSONArray newsList;
    
    String lastIp;
    
    InetAddress privateIpv6; 
    InetAddress privateIpv4;
    String publicIpv4;
    String publicIpv6;
    
    boolean needsRetry = false;
    
    ControlServerConnection serverConn;
    
    private OnCompleteListener onCompleteListener;
    
    /**
  	 * 
  	 */
    private static final String DEBUG_TAG = "CheckIpTask";
    
    
    public CheckIpTask(final RMBTMainActivity activity)
    {
        this.activity = activity;
        
    }
    
    /**
     * 
     * @param listener
     */
    public void setOnCompleteListener(OnCompleteListener listener) {
    	this.onCompleteListener = listener;
    }
    
    @Override
    protected JSONArray doInBackground(final Void... params)
    {
    	needsRetry = false;
        serverConn = new ControlServerConnection(activity.getApplicationContext());
        

        try {
        	Socket s = new Socket();
        	InetSocketAddress addr = new InetSocketAddress(ConfigHelper.getCachedControlServerNameIpv4(activity.getApplicationContext()), 
        			ConfigHelper.getControlServerPort(activity.getApplicationContext()));
        	s.connect(addr, 5000);
        	
        	privateIpv4 = s.getLocalAddress();
        	s.close();
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        
        try { 
        	Socket s = new Socket();
        	InetSocketAddress addr = new InetSocketAddress(ConfigHelper.getCachedControlServerNameIpv6(activity.getApplicationContext()), 
        			ConfigHelper.getControlServerPort(activity.getApplicationContext()));
        	s.connect(addr, 5000);
        	
        	privateIpv6 = s.getLocalAddress();
        	s.close();        	
        }
        catch (SocketTimeoutException e) {
        	e.printStackTrace();
        	needsRetry = ConfigHelper.isRetryRequiredOnIpv6SocketTimeout(activity);
        }
        catch (Exception e) {
        	needsRetry = false;
        	e.printStackTrace();
        }

        newsList = new JSONArray();
        
        if (privateIpv4 != null) {
        	JSONArray response = serverConn.requestIp(false);
        	if (response != null && response.length() >= 1) {
        		newsList.put(response.opt(0));
        	}
        }
        else {
        	Log.d(DEBUG_TAG, "no private ipv4 found");
        }
        
        if (privateIpv6 != null) {
        	JSONArray response = serverConn.requestIp(true);
        	if (response != null && response.length() >= 1) {
        		newsList.put(response.opt(0));
        	}
        }
        else {
        	Log.d(DEBUG_TAG, "no private ipv6 found");
        }
        
        return newsList;
    }
    
    @Override
    protected void onCancelled()
    {
        if (serverConn != null)
        {
            serverConn.unload();
            serverConn = null;
        }
    }
    
    @Override
    protected void onPostExecute(final JSONArray newsList)
    {
        
    	try {
        	Log.d(DEBUG_TAG, "News: " + newsList);
            int ipv = 4;
            
            if (newsList != null && newsList.length() > 0 && !serverConn.hasError()) {
                for (int i = 0; i < newsList.length(); i++) {
                    if (!isCancelled() && !Thread.interrupted()) {
                        try
                        {
                            
                            final JSONObject newsItem = newsList.getJSONObject(i);
                            
                            if (newsItem.has("v"))
                            {
                            	ipv = newsItem.getInt("v");
                            	
                                if (ipv == 6) {
                                	try {
        								publicIpv6 = newsItem.getString("ip");
        							} catch (Exception e) {
        								e.printStackTrace();
        							}
                                }
                                else {
                                	try {
        								publicIpv4 = newsItem.getString("ip");
        							} catch (Exception e) {
        								e.printStackTrace();
        							}                        	
                                }
                            }
                        }
                        catch (final JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            	
            	if (onCompleteListener != null && !needsRetry) {
           			onCompleteListener.onComplete(NetworkInfoCollector.FLAG_PRIVATE_IPV6, privateIpv6);
            		onCompleteListener.onComplete(NetworkInfoCollector.FLAG_PRIVATE_IPV4, privateIpv4);
            		onCompleteListener.onComplete(NetworkInfoCollector.FLAG_IPV4, publicIpv4);
            		onCompleteListener.onComplete(NetworkInfoCollector.FLAG_IPV6, publicIpv6);
            		onCompleteListener.onComplete(NetworkInfoCollector.FLAG_IP_TASK_COMPLETED, null);
            	}
            	else if (onCompleteListener != null) {
            		onCompleteListener.onComplete(OnCompleteListener.ERROR, NetworkInfoCollector.FLAG_IP_TASK_NEEDS_RETRY);
            	}
            	
        	}
            else {
            	ConfigHelper.setLastIp(activity.getApplicationContext(), null);
            	if (onCompleteListener != null) {
            		onCompleteListener.onComplete(OnCompleteListener.ERROR, null);
            	}
            }
    	}
        catch (Exception e) {
        	e.printStackTrace();
        }
//        finally {
//        	if (onCompleteListener != null) {
//        		onCompleteListener.onComplete(NetworkInfoCollector.FLAG_IP_TASK_COMPLETED, null);
//        	}
//        }
    }
    
}