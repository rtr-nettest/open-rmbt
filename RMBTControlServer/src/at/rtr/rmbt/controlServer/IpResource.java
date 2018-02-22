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
package at.rtr.rmbt.controlServer;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import com.google.common.net.InetAddresses;

public class IpResource extends ServerResource
{
    @Post("json")
    public String request(final String entity)
    {
        addAllowOrigin();
        JSONObject request = null;
        
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;


        final String clientIpRaw = getIP();
        final InetAddress clientAddress = InetAddresses.forString(clientIpRaw);

        System.out.println(MessageFormat.format(labels.getString("NEW_IP_REQ"), clientIpRaw));
        
        if (entity != null && !entity.isEmpty()) {
            try
            {
                answer.put("ip", clientIpRaw);
                if (clientAddress instanceof Inet4Address) {
                	answer.put("v", "4");
                }
                else if (clientAddress instanceof Inet6Address) {
                	answer.put("v", "6");
                }                    
                else {
                	answer.put("v", "0");
                }
            }
            catch (final JSONException e)
            {
                errorList.addError("ERROR_REQUEST_JSON");
                System.out.println("Error parsing JSON Data " + e.toString());
            }
        }
        else {
            errorList.addErrorString("Expected request is missing.");
        }
        
        try
        {
            answer.putOpt("error", errorList.getList());
        }
        catch (final JSONException e)
        {
            System.out.println("Error saving ErrorList: " + e.toString());
        }
        
        answerString = answer.toString();
        
        return answerString;
    }
    
    @Get("json")
    public String retrieve(final String entity)
    {
        return request(entity);
    }
    
}