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
package at.alladin.rmbt.android.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import at.alladin.rmbt.android.adapter.result.OnCompleteListener;
import at.alladin.rmbt.android.main.titlepage.IpCheckRunnable;

public class CheckIpTask extends AsyncTask<Void, Void, JSONArray>
{
    private final Activity activity;
    
    private JSONArray newsList;
    
    String lastIp;
    
    InetAddress privateIp; 
    String publicIp;
    
    boolean needsRetry = false;
    
    ControlServerConnection serverConn;
    
    private OnCompleteListener onCompleteListener;
    
    private final IpVersionType ipVersionType; 
    
    public enum IpVersionType {
    	V4, V6
    }
    
    /**
  	 * 
  	 */
    private static final String DEBUG_TAG = "CheckIpTask";
        
    public CheckIpTask(final Activity activity, final IpVersionType ipVersionType)
    {
        this.activity = activity;
        this.ipVersionType = ipVersionType;
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
        serverConn = new ControlServerConnection(activity);
        
        try { 
        	Socket s = new Socket();
        	InetSocketAddress addr = null;
        	switch(ipVersionType) {
        	case V4:
            	addr = new InetSocketAddress(ConfigHelper.getCachedControlServerNameIpv4(activity.getApplicationContext()), 
            			ConfigHelper.getControlServerPort(activity.getApplicationContext()));
            	break;
        	case V6:
            	addr = new InetSocketAddress(ConfigHelper.getCachedControlServerNameIpv6(activity.getApplicationContext()), 
            			ConfigHelper.getControlServerPort(activity.getApplicationContext()));
            	break;
        	}
        	Log.d(DEBUG_TAG, "Connecting to: " + addr);
        	s.connect(addr, 5000);

        	privateIp = s.getLocalAddress();
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
        
        try {
	        if (privateIp != null) {
	        	JSONArray response = serverConn.requestIp(ipVersionType == IpVersionType.V6);
	        	if (response != null && response.length() >= 1) {
	        		newsList.put(response.opt(0));
	        	}
	        }
	        else {
	        	Log.d(DEBUG_TAG, "no private ip found");
	        }
        } 
        catch (Exception e) {
        	e.printStackTrace();
        }
        
        return newsList;
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
								publicIp = newsItem.getString("ip");
                            	
								if (onCompleteListener != null && !needsRetry) {                            	
									onCompleteListener.onComplete(IpCheckRunnable.FLAG_IP_PRIVATE, privateIp);
                                	onCompleteListener.onComplete(IpCheckRunnable.FLAG_IP_PUBLIC, publicIp);
                                	onCompleteListener.onComplete(IpCheckRunnable.FLAG_IP_FINISHED, null);
                                }
                            	else if (onCompleteListener != null) {
                            		onCompleteListener.onComplete(IpCheckRunnable.FLAG_IP_CHECK_ERROR, null);
                            	}

                                
                            }
                            else if (onCompleteListener != null) {
                            	onCompleteListener.onComplete(IpCheckRunnable.FLAG_IP_CHECK_ERROR, null);
                        	}                            
                        }
                        catch (final JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }            	
        	}
            else {
            	ConfigHelper.setLastIp(activity.getApplicationContext(), null);
            	if (onCompleteListener != null) {
            		onCompleteListener.onComplete(IpCheckRunnable.FLAG_IP_CHECK_ERROR, null);
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