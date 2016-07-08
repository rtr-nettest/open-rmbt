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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import at.alladin.rmbt.db.QoSTestResult;
import at.alladin.rmbt.db.QoSTestResult.TestType;
import at.alladin.rmbt.db.Test;
import at.alladin.rmbt.db.dao.QoSTestResultDao;
import at.alladin.rmbt.qos.AbstractResult;
import at.alladin.rmbt.qos.QoSUtil;
import at.alladin.rmbt.qos.ResultDesc;
import at.alladin.rmbt.qos.ResultOptions;
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.hstoreparser.HstoreParseException;

public class QualityOfServiceResultResource extends ServerResource
{
    final static int UNKNOWN = Integer.MIN_VALUE; 
    
    @Post("json")
    public String request(final String entity)
    {
        final String secret = getContext().getParameters().getFirstValue("RMBT_SECRETKEY");
        
        addAllowOrigin();
        
        JSONObject request = null;
        
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        
        System.out.println(MessageFormat.format(labels.getString("NEW_QOS_RESULT"), getIP()));
        
        if (entity != null && !entity.isEmpty())
            // try parse the string to a JSON object
            try
            {
                request = new JSONObject(entity);
                
                final String lang = request.optString("client_language");
                
                // Load Language Files for Client
                
                final List<String> langs = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));
                
                if (langs.contains(lang))
                {
                    errorList.setLanguage(lang);
                    labels = ResourceManager.getSysMsgBundle(new Locale(lang));
                }
                
//                System.out.println(request.toString(4));
                
                if (conn != null)
                {
                    ResultOptions resultOptions = new ResultOptions(new Locale(lang));
                    
                    boolean oldAutoCommitState = conn.getAutoCommit();
                    conn.setAutoCommit(false);
                    
                    final Test test = new Test(conn);
                    
                    if (request.optString("test_token").length() > 0)
                    {
                        
                        final String[] token = request.getString("test_token").split("_");
                        
                        try
                        {
                            // Check if UUID
                            final UUID testUuid = UUID.fromString(token[0]);
                            
                            final String data = token[0] + "_" + token[1];
                            
                            final String hmac = Helperfunctions.calculateHMAC(secret.getBytes(), data);
                            
                            if (hmac.length() == 0)
                                errorList.addError("ERROR_TEST_TOKEN");
                            
                            if (token[2].length() > 0) // && hmac.equals(token[2]))
                            {
                                
                                final List<String> clientNames = Arrays.asList(settings.getString("RMBT_CLIENT_NAME")
                                        .split(",\\s*"));
                                final List<String> clientVersions = Arrays.asList(settings.getString(
                                        "RMBT_VERSION_NUMBER").split(",\\s*"));
                                
                                if (test.getTestByUuid(testUuid) > 0)
                                    if (clientNames.contains(request.optString("client_name"))
                                            && clientVersions.contains(request.optString("client_version")))
                                    {
                                        //save qos test results:
                                        JSONArray qosResult = request.optJSONArray("qos_result");
                                        if (qosResult != null) {
                                        	QoSTestResultDao resultDao = new QoSTestResultDao(conn);
                                        	
                                        	Set<String> excludeTestTypeKeys = new TreeSet<>();
                                        	excludeTestTypeKeys.add("test_type");
                                        	excludeTestTypeKeys.add("qos_test_uid");
                                        	
                                        	for (int i = 0; i < qosResult.length(); i++) {
                                        		JSONObject testObject = qosResult.optJSONObject(i);
                                        		//String hstore = Helperfunctions.json2hstore(testObject, excludeTestTypeKeys);
                                        		JSONObject resultJson = new JSONObject(testObject, JSONObject.getNames(testObject));
                                        		for (String excludeKey : excludeTestTypeKeys) {
                                        			resultJson.remove(excludeKey);
                                        		}
                                        		QoSTestResult testResult = new QoSTestResult();
                                        		//testResult.setResults(hstore);
                                        		testResult.setResults(resultJson.toString());
                                        		testResult.setTestType(testObject.getString("test_type"));
                                        		testResult.setTestUid(test.getUid());
                                        		long qosTestId = testObject.optLong("qos_test_uid", Long.MIN_VALUE);
                                                testResult.setQoSTestObjectiveId(qosTestId);
                                        		resultDao.save(testResult);
                                        	}
                                        }
                                        
                                        QoSTestResultDao resultDao = new QoSTestResultDao(conn);
                                        PreparedStatement updateCounterPs = resultDao.getUpdateCounterPreparedStatement();
                                        List<QoSTestResult> testResultList = resultDao.getByTestUid(test.getUid());
                                        //map that contains all test types and their result descriptions determined by the test result <-> test objectives comparison
                                    	Map<TestType,TreeSet<ResultDesc>> resultKeys = new HashMap<>();
                                    	
                                    	//test description set:
                                    	Set<String> testDescSet = new TreeSet<>();
                                    	//test summary set:
                                    	Set<String> testSummarySet = new TreeSet<>();
                                    	
                                        //iterate through all result entries
                                        for (QoSTestResult testResult : testResultList) {
                                        	
                                        	//reset test counters
                                        	testResult.setFailureCounter(0);
                                        	testResult.setSuccessCounter(0);
                                        	
                                        	//get the correct class of the result;
                                        	TestType testType = TestType.valueOf(testResult.getTestType().toUpperCase());
                                        	Class<? extends AbstractResult<?>> clazz = testType.getClazz();
                                        	//parse hstore data
                                        	final JSONObject resultJson = new JSONObject(testResult.getResults());
                                        	AbstractResult<?> result = QoSUtil.HSTORE_PARSER.fromJSON(resultJson, clazz);
                                        	result.setResultJson(resultJson);
                                        	
                                        	if (result != null) {
                                        		//add each test description key to the testDescSet (to fetch it later from the db)
                                        		if (testResult.getTestDescription() != null) {
                                            		testDescSet.add(testResult.getTestDescription());	
                                        		}
                                        		if (testResult.getTestSummary() != null) {
                                        			testSummarySet.add(testResult.getTestSummary());
                                        		}
                                        		testResult.setResult(result);

                                        	}
                                        	//compare test results with expected results 
                                        	QoSUtil.compareTestResults(testResult, result, resultKeys, testType, resultOptions);
                                        	//resultList.put(testResult.toJson());
                                        	
                                            //update all test results after the success and failure counters have been set
                                        	resultDao.updateCounter(testResult, updateCounterPs);
                                        	//System.out.println("UPDATING: " + testResult.toString());
                                        }
                                    }
                                    else
                                        errorList.addError("ERROR_CLIENT_VERSION");
                            }
                            else
                                errorList.addError("ERROR_TEST_TOKEN_MALFORMED");
                        }
                        catch (final IllegalArgumentException e)
                        {
                            e.printStackTrace();
                            errorList.addError("ERROR_TEST_TOKEN_MALFORMED");
                        } catch (HstoreParseException e) {
                            e.printStackTrace();
                            errorList.addError("ERROR_DB_CONNECTION");
						} catch (IllegalAccessException e) {
							e.printStackTrace();
							errorList.addError("ERROR_TEST_TOKEN_MALFORMED");
						}
                        
                    }
                    else
                        errorList.addError("ERROR_TEST_TOKEN_MISSING");
                                        
                    conn.commit();
                    conn.setAutoCommit(oldAutoCommitState); // be nice and restore old state TODO: do it in finally
                }
                else
                    errorList.addError("ERROR_DB_CONNECTION");
                
            }
            catch (final JSONException e)
            {
                errorList.addError("ERROR_REQUEST_JSON");
                //System.out.println("Error parsing JSDON Data " + e.toString());
                e.printStackTrace();
            }
            catch (final SQLException e)
            {
                //System.out.println("Error while storing data " + e.toString());
                e.printStackTrace();
            }
        else
            errorList.addErrorString("Expected request is missing.");
        
        try
        {
            answer.putOpt("error", errorList.getList());
        }
        catch (final JSONException e)
        {
            System.out.println("Error saving ErrorList: " + e.toString());
        }
        
        return answer.toString();
    }
    
    @Get("json")
    public String retrieve(final String entity)
    {
        return request(entity);
    }
    
}
