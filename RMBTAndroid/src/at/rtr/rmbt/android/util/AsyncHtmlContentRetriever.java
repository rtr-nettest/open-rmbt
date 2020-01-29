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
package at.rtr.rmbt.android.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * @author lb
 */
public class AsyncHtmlContentRetriever extends AsyncTask<String, Void, AsyncHtmlContentRetriever.HtmlContentPojo> {

    private ContentRetrieverListener listener;

    /**
     * @author lb
     */
    public static interface ContentRetrieverListener {
        public void onContentFinished(String htmlContent, int statusCode, String errorMessage);
    }

    /**
     * @param listener
     */
    public void setContentRetrieverListener(ContentRetrieverListener listener) {
        this.listener = listener;
    }

    @Override
    protected HtmlContentPojo doInBackground(String... params) {
        HtmlContentPojo result = new HtmlContentPojo();
        URL url;
        URLConnection connection;
        try {
            url = new URL(params[0]);
            connection = url.openConnection();
            connection.setConnectTimeout(3000);
            connection.connect();
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatusCode(-1);
            result.setHtmlContent(null);
            result.setErrorMessage(e.getMessage());
            try {
                result.setUrl(params[0]);
            } catch (Exception ex) {
                //ignore
            }
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
            System.out.println("response code for url: " + url + " code: " + statusCode);
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
            result.setStatusCode(-1);
            result.setHtmlContent(null);
            result.setUrl(url.toString());
            result.setErrorMessage(e.getMessage());
            return result;
        }

        result.setStatusCode(statusCode);
        result.setHtmlContent(htmlContent);
        result.setUrl(url.toString());
        //error message remains null

        return result;
    }

    @Override
    protected void onPostExecute(HtmlContentPojo result) {
        super.onPostExecute(result);
        System.out.println("response code for url: " + result.getUrl() + " code: " + result.getStatusCode() + " error: " + result.getErrorMessage());
        if (this.listener != null) {
            listener.onContentFinished(result.getHtmlContent(), result.getStatusCode(), result.getErrorMessage());
        }
    }

    /**
     * @param inputStream
     * @return
     */
    public static String convertToString(InputStream inputStream) {
        StringBuffer string = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String l;
        try {
            while ((l = reader.readLine()) != null) {
                string.append(l + "\n");
            }
        } catch (IOException e) {
        }
        return string.toString();
    }

    protected class HtmlContentPojo {
        private String htmlContent;
        private String url;
        private Integer statusCode;
        private String errorMessage;

        private HtmlContentPojo() {

        }

        private HtmlContentPojo(final String htmlContent, final String url, final Integer statusCode, final String errorMessage) {
            this.htmlContent = htmlContent;
            this.url = url;
            this.statusCode = statusCode;
            this.errorMessage = errorMessage;
        }

        public String getHtmlContent() {
            return htmlContent;
        }

        public void setHtmlContent(String htmlContent) {
            this.htmlContent = htmlContent;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Integer getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(Integer statusCode) {
            this.statusCode = statusCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}