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
package at.alladin.rmbt.controlServer;


import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;

import at.alladin.rmbt.shared.RevisionHelper;

public class VersionResource extends ServerResource
{
    @Get("json")
    public String request(final String entity)
    {
        try
        {
            final JSONObject answer = new JSONObject();
            answer.put("version", RevisionHelper.getVerboseRevision());
            answer.put("system_UUID", getSetting("system_UUID",""));
            
            return answer.toString();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
