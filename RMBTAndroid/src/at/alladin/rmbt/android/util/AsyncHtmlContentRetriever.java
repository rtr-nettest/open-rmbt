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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

/**
 * 
 * @author lb
 *
 */
public class AsyncHtmlContentRetriever extends AsyncTask<String, Void, List<Object>> {

	private ContentRetrieverListener listener;
	
	/**
	 * 
	 * @author lb
	 *
	 */
	public static interface ContentRetrieverListener {
		public void onContentFinished(String htmlContent, int statusCode);
	}
	
	/**
	 * 
	 * @param listener
	 */
	public void setContentRetrieverListener(ContentRetrieverListener listener) {
		this.listener = listener;
	}
	
	@Override
	protected List<Object> doInBackground(String... params) {
        List<Object> result = new ArrayList<Object>();
        URL url;
        URLConnection connection;
        try {
            url = new URL(params[0]);
            connection = url.openConnection();
            connection.setConnectTimeout(3000);
            connection.connect();
        }
        catch (Exception e) {
        	result.add(-1);
        	result.add(null);
        	return result;
        }

        String htmlContent = "";
        HttpGet httpGet = new HttpGet(params[0]);
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response;
        int statusCode = -1;
        
        try {
            response = httpClient.execute(httpGet);
            statusCode = response.getStatusLine().getStatusCode();
            System.out.println("response code: " + statusCode);
            /*
             * load HTML:
             */
            /*
            HttpEntity entity = response.getEntity();
            if (entity != null) {
            	InputStream inputStream = entity.getContent();
            	htmlContent = convertToString(inputStream);
            }
            */
        } catch (Exception e) {
        	result.add(-1);
        	result.add(null);
        	return result;
        }

        result.add(statusCode);
        result.add(htmlContent);
        
        return result;
	}
	
	@Override
	protected void onPostExecute(List<Object> result) {
		super.onPostExecute(result);
		
		if (this.listener != null) {
			listener.onContentFinished((String) result.get(1), (Integer) result.get(0));
		}
	}
	
	/**
	 * 
	 * @param inputStream
	 * @return
	 */
    public static String convertToString(InputStream inputStream){
        StringBuffer string = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String l;
        try {
            while ((l = reader.readLine()) != null) {
                string.append(l + "\n");
            }
        } catch (IOException e) {}
        return string.toString();
    }
}