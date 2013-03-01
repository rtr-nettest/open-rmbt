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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.data.ClientInfo;
import org.restlet.data.Language;
import org.restlet.data.Preference;
import org.restlet.engine.header.Header;
import org.restlet.resource.Get;
import org.restlet.util.Series;

public class RequestDataCollector extends ServerResource
{
    @Get("json")
    public String request(final String entity) throws JSONException
    {
        addAllowOrigin();
        
        final JSONObject answer = new JSONObject();
        
        final Request request = getRequest();
        final ClientInfo clientInfo = request.getClientInfo();
        final String agent = clientInfo.getAgent();
        answer.put("ip", getIP());
        answer.put("port", clientInfo.getPort());
        String product = clientInfo.getAgentName();
        
        if (product != null && "Version".equals(product))
            product = null;
        
        if (product == null)
        {
            // try to find out on our own...
            if (agent.contains("Opera"))
                product = "Opera";
            else if (agent.contains("Chrome"))
                product = "Chrome";
            else if (agent.contains("Firefox"))
                product = "Firefox";
            else if (agent.contains("Safari"))
                product = "Safari";
        }
        
        answer.put("product", product);
        answer.put("agent", agent);
        answer.put("url", getURL().toString());
        
        final List<Preference<Language>> acceptedLanguages = clientInfo.getAcceptedLanguages();
        final JSONArray languages = new JSONArray();
        answer.put("languages", languages);
        for (int i = 0; i < acceptedLanguages.size(); i++)
        {
            final Preference<Language> preference = acceptedLanguages.get(i);
            final Language language = preference.getMetadata();
            languages.put(language.getName());
        }
        
        final JSONObject headersObj = new JSONObject();
        answer.put("headers", headersObj);
        
        @SuppressWarnings("unchecked")
        final Series<Header> headers = (Series<Header>) request.getAttributes().get("org.restlet.http.headers");
        
        if (headers != null)
            for (final Header h : headers)
            {
                final String name = h.getName();
                if (!name.equalsIgnoreCase("X-Real-IP") && !name.equalsIgnoreCase("X-Real-URL"))
                    headersObj.put(name, h.getValue());
            }
        
        return answer.toString();
    }
}
