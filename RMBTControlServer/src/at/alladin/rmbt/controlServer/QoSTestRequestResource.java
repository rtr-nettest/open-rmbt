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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import at.alladin.rmbt.db.QoSTestObjective;
import at.alladin.rmbt.db.dao.QoSTestObjectiveDao;
import at.alladin.rmbt.qos.testscript.TestScriptInterpreter;

import com.google.common.net.InetAddresses;

/**
 * 
 * @author lb
 *
 */
public class QoSTestRequestResource extends ServerResource {

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
        final String clientIpString = InetAddresses.toAddrString(clientAddress);
        
        System.out.println(MessageFormat.format(labels.getString("NEW_QOS_REQUEST"), clientIpRaw));
        
        if (entity != null && !entity.isEmpty()) {
            // try parse the string to a JSON object
            try
            {
                request = new JSONObject(entity);                
                
                List<QoSTestObjective> listTestParams = new ArrayList<>();                
                QoSTestObjectiveDao testObjectiveDao = new QoSTestObjectiveDao(conn);
                listTestParams = testObjectiveDao.getByTestClass(1);
                //listTestParams.add(testObjectiveDao.getById(1));
                Map<String, List<JSONObject>> tests = new HashMap<>();
                
                for (QoSTestObjective o : listTestParams) {
                	List<JSONObject> testList;
                	
                	if (tests.containsKey(o.getTestType())) {
                		testList = tests.get(o.getTestType()); 
                	}
                	else {
                		testList = new ArrayList<>();
                		tests.put(o.getTestType(), testList);
                	}
                	
                	JSONObject params = new JSONObject(o.getObjective());
                	                	
                	Iterator<String> keys = params.keys();
                	boolean testInvalid = false;
                	
                	//iterate through all keys and interprete their values if necessary;
                	while (keys.hasNext()) {
                		String key = keys.next();
                		Object scriptResult = TestScriptInterpreter.interprete(params.getString(key), null);
                		if (scriptResult != null) {
                       		params.put(key, String.valueOf(scriptResult));
                		}
                		else {
                			testInvalid = true;
                			break;
                		}
                	}
                	
                	//add test uid to the params object
                	params.put("qos_test_uid", String.valueOf(o.getUid()));
                	params.put("concurrency_group", String.valueOf(o.getConcurrencyGroup()));
                	if (clientAddress instanceof Inet6Address) {
                    	params.put("server_addr", String.valueOf(o.getTestServerIpv6()));                		
                	}
                	else {
                		params.put("server_addr", String.valueOf(o.getTestServerIpv4()));
                	}
                	params.put("server_port", String.valueOf(o.getPort()));

                	
                	if (!testInvalid) {
                		testList.add(params);
                	}
                }
                
                answer.put("objectives", tests);
                
                //System.out.println(answer);
                                
                answer.put("test_duration", settings.getString("RMBT_DURATION"));
                answer.put("test_numthreads", settings.getString("RMBT_NUM_THREADS"));
                answer.put("test_numpings", settings.getString("RMBT_NUM_PINGS"));
                answer.put("client_remote_ip", clientIpString);
                
            } catch(JSONException | SQLException e) {
            	e.printStackTrace();
            	errorList.addError("ERROR_DB_QOS_GET_OBJECTIVE_NOT_FOUND");
			}
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
    }}
;