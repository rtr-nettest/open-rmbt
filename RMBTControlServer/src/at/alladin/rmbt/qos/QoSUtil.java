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
package at.alladin.rmbt.qos;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.alladin.rmbt.controlServer.ErrorList;
import at.alladin.rmbt.db.Client;
import at.alladin.rmbt.db.QoSTestResult;
import at.alladin.rmbt.db.QoSTestResult.TestType;
import at.alladin.rmbt.db.QoSTestTypeDesc;
import at.alladin.rmbt.db.Test;
import at.alladin.rmbt.db.dao.QoSTestDescDao;
import at.alladin.rmbt.db.dao.QoSTestResultDao;
import at.alladin.rmbt.db.dao.QoSTestTypeDescDao;
import at.alladin.rmbt.qos.testscript.TestScriptInterpreter;
import at.alladin.rmbt.shared.hstoreparser.Hstore;
import at.alladin.rmbt.shared.hstoreparser.HstoreParseException;
import at.alladin.rmbt.util.capability.QualityOfServiceCapability;

/**
 * 
 * @author lb
 *
 */
public class QoSUtil {
    public static final Hstore HSTORE_PARSER = new Hstore(HttpProxyResult.class, NonTransparentProxyResult.class, 
    		DnsResult.class, TcpResult.class, UdpResult.class, WebsiteResult.class, VoipResult.class, TracerouteResult.class);


	/**
	 * 
	 * @author lb
	 *
	 */
	public static class TestUuid {
		public static enum UuidType {
			TEST_UUID, OPEN_TEST_UUID
		}
		
		protected UuidType type;
		protected String uuid;
		
		public TestUuid(String uuid, UuidType type) {
			this.type = type;
			this.uuid = uuid;
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}

		public UuidType getType() {
			return type;
		}

		public void setType(UuidType type) {
			this.type = type;
		}
	}
	
	/**
	 * 
	 * @param settings
	 * @param conn
	 * @param answer
	 * @param lang
	 * @param errorList
	 * @throws SQLException 
	 * @throws JSONException 
	 * @throws HstoreParseException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static void evaluate(final ResourceBundle settings, final Connection conn, final TestUuid uuid,
			final JSONObject answer, String lang, final ErrorList errorList, final QualityOfServiceCapability qosCapability) throws SQLException, HstoreParseException, JSONException, IllegalArgumentException, IllegalAccessException {
        // Load Language Files for Client
        
        final List<String> langs = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));
        
        if (langs.contains(lang)) {
            errorList.setLanguage(lang);
        }
        else {
            lang = settings.getString("RMBT_DEFAULT_LANGUAGE");
        }
        
        
        if (conn != null)
        {
        	
            final Client client = new Client(conn);
            final Test test = new Test(conn);
            
            boolean necessaryDataAvailable = false;
            
            if (uuid != null && uuid.getType() != null && uuid.getUuid() != null) {
            	switch (uuid.getType()) {
            	case OPEN_TEST_UUID:
            		if (test.getTestByOpenTestUuid(UUID.fromString(uuid.getUuid())) > 0
            				&& client.getClientByUid(test.getField("client_id").intValue())) {
            			necessaryDataAvailable = true;
            		}
            		break;
            	case TEST_UUID:
            		if (test.getTestByUuid(UUID.fromString(uuid.getUuid())) > 0
            				&& client.getClientByUid(test.getField("client_id").intValue())) {
            			necessaryDataAvailable = true;
            		}
            		break;
            	}
            }
            
            final long timeStampFullEval = System.currentTimeMillis();
            
            if (necessaryDataAvailable)
            {
                                      
                final Locale locale = new Locale(lang);
                final ResultOptions resultOptions = new ResultOptions(locale);
                final JSONArray resultList = new JSONArray();
                
                QoSTestResultDao resultDao = new QoSTestResultDao(conn);
                List<QoSTestResult> testResultList = resultDao.getByTestUid(test.getUid());
                if (testResultList == null || testResultList.isEmpty()) {
                	throw new UnsupportedOperationException("test " + test + " has no result list");
                }
                //map that contains all test types and their result descriptions determined by the test result <-> test objectives comparison
            	Map<TestType,TreeSet<ResultDesc>> resultKeys = new HashMap<>();
            	
            	//test description set:
            	Set<String> testDescSet = new TreeSet<>();
            	//test summary set:
            	Set<String> testSummarySet = new TreeSet<>();

            	
            	//Staring timestamp for evaluation time measurement
            	final long timeStampEval = System.currentTimeMillis();
            	
                //iterate through all result entries
                for (final QoSTestResult testResult : testResultList) {
                	
                	//reset test counters
                	testResult.setFailureCounter(0);
                	testResult.setSuccessCounter(0);
                	
                	//get the correct class of the result;
                	TestType testType = null;
                	try {
                		testType = TestType.valueOf(testResult.getTestType().toUpperCase(Locale.US));
                	}
                	catch(IllegalArgumentException e) {
                		final String errorMessage = "WARNING: QoS TestType '" + testResult.getTestType().toUpperCase(Locale.US) + "' not supported by ControlServer. Test with UID: " + testResult.getUid() + " skipped.";
                		System.out.println(errorMessage);
                		errorList.addErrorString(errorMessage);
                		testType = null;
                	}
                	
                	if (testType == null) {
                		continue;
                	}
                	
                	Class<? extends AbstractResult<?>> clazz = testType.getClazz();
                	//parse hstore data
                	if (testResult.getResults() != null) {
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
	
	                	//compare test results
	                	compareTestResults(testResult, result, resultKeys, testType, resultOptions);
                	}

                	//resultList.put(testResult.toJson());
                	
                    //save all test results after the success and failure counters have been set
                	//resultDao.updateCounter(testResult);
                }
                
            	//ending timestamp for evaluation time measurement
            	final long timeStampEvalEnd = System.currentTimeMillis();
                                       
                //-------------------------------------------------------------
                //fetch all result strings from the db
                QoSTestDescDao descDao = new QoSTestDescDao(conn, locale);

                //FIRST: get all test descriptions
                Set<String> testDescToFetchSet = testDescSet;
                testDescToFetchSet.addAll(testSummarySet);

                Map<String, String> testDescMap = descDao.getAllByKeyToMap(testDescToFetchSet);
                
                for (QoSTestResult testResult : testResultList) {
                	
                    //and set the test results + put each one to the result list json array
                	String preParsedDesc = testDescMap.get(testResult.getTestDescription());
                	if (preParsedDesc != null) {
                    	String description = String.valueOf(TestScriptInterpreter.interprete(testDescMap.get(testResult.getTestDescription()), 
                    			QoSUtil.HSTORE_PARSER, testResult.getResult(), true, resultOptions));
                    	testResult.setTestDescription(description);
                	}

                	//do the same for the test summary:
                	String preParsedSummary = testDescMap.get(testResult.getTestSummary());
                	if (preParsedSummary != null) {
                    	String description = String.valueOf(TestScriptInterpreter.interprete(testDescMap.get(testResult.getTestSummary()), 
                    			QoSUtil.HSTORE_PARSER, testResult.getResult(), true, resultOptions));
                    	testResult.setTestSummary(description);
                	}

               		final JSONObject resultJsonObject = testResult.toJson(uuid.getType());
               		if (resultJsonObject != null) {
               			resultList.put(resultJsonObject);
               		}
                }
                
                //finally put results to json
                if (resultList != null && resultList.length() > 0) {
                	answer.put("testresultdetail", resultList);
                }
                
                JSONArray resultDescArray = new JSONArray();
                
                //SECOND: fetch all test result descriptions 
                for (TestType testType : resultKeys.keySet()) {
                	TreeSet<ResultDesc> descSet = resultKeys.get(testType);
                	//fetch results to same object
                    descDao.loadToTestDesc(descSet);

                    //another tree set for duplicate entries:
                    //TODO: there must be a better solution 
                    //(the issue is: compareTo() method returns differnt values depending on the .value attribute (if it's set or not))
                    TreeSet<ResultDesc> descSetNew = new TreeSet<>();
                    //add fetched results to json
                                        
                    for (ResultDesc desc : descSet) {
                    	if (!qosCapability.isSupportsInfo()) {
                    		if (ResultDesc.STATUS_CODE_INFO.equals(desc.getStatusCode())) {
                    			continue;
                    		}
                    	}
                    	
                    	if (!descSetNew.contains(desc)) {
                        	descSetNew.add(desc);
                    	}
                    	else {
                    		for (ResultDesc d : descSetNew) {
                    			if (d.compareTo(desc) == 0) {
                    				d.getTestResultUidList().addAll(desc.getTestResultUidList());
                    			}
                    		}
                    	}
                    }
                    
                    for (ResultDesc desc : descSetNew) {
                    	if (desc.getValue() != null) {
                            resultDescArray.put(desc.toJson());	
                    	}	
                    }
                    
                }
                //System.out.println(resultDescArray);
                //put result descriptions to json
                answer.put("testresultdetail_desc", resultDescArray);

                QoSTestTypeDescDao testTypeDao = new QoSTestTypeDescDao(conn, locale);
                JSONArray testTypeDescArray = new JSONArray();
                for (QoSTestTypeDesc desc : testTypeDao.getAll()) {
                	final JSONObject testTypeDesc = desc.toJson();
                	if (testTypeDesc != null) {
                		testTypeDescArray.put(testTypeDesc);
                	}
                }

                //put result descriptions to json
                answer.put("testresultdetail_testdesc", testTypeDescArray);
                JSONObject evalTimes = new JSONObject();
                evalTimes.put("eval", (timeStampEvalEnd - timeStampEval));
                evalTimes.put("full", (System.currentTimeMillis() - timeStampFullEval));
                answer.put("eval_times", evalTimes);

                //System.out.println(answer);
            }
            else
                errorList.addError("ERROR_REQUEST_TEST_RESULT_DETAIL_NO_UUID");
            
        }
        else
            errorList.addError("ERROR_DB_CONNECTION");
	}
	
	/**
	 * compares test results with expected results and increases success/failure counter 
	 * @param testResult the test result
	 * @param result the parsed test result
	 * @param resultKeys result key map
	 * @param testType test type
	 * @param resultOptions result options
	 * @throws HstoreParseException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void compareTestResults(final QoSTestResult testResult, final AbstractResult<?> result, 
			final Map<TestType,TreeSet<ResultDesc>> resultKeys, final TestType testType, final ResultOptions resultOptions) throws HstoreParseException, IllegalArgumentException, IllegalAccessException {

    	//if expected resuls not null, compare them to the test results
    	if (testResult.getExpectedResults()!=null) {    		
    		final Class<? extends AbstractResult<?>> clazz = testType.getClazz();
    		
    		//create a parsed abstract result set sorted by priority
    		final Set<AbstractResult<?>> expResultSet = new TreeSet<AbstractResult<?>>(new Comparator<AbstractResult<?>>() {
				@Override
				public int compare(final AbstractResult<?> o1, final AbstractResult<?> o2) {
					return o1.priority.compareTo(o2.priority);
				}
			});

    		int priority = Integer.MAX_VALUE;
    		
    		if (testResult.getExpectedResults() != null) {
	    		for (int i = 0; i < testResult.getExpectedResults().length(); i++) {
	    			final JSONObject expectedResults = testResult.getExpectedResults().optJSONObject(i);
	    			if (expectedResults != null) {
	        			//parse hstore string to object
		    			final AbstractResult<?> expResult = QoSUtil.HSTORE_PARSER.fromJSON(expectedResults, clazz);
		    			if (expResult.getPriority() == Integer.MAX_VALUE) {
		    				expResult.setPriority(priority--);
		    			}
		    			expResultSet.add(expResult);  
	    			}
	    		}
    		}
    		
    		for (final AbstractResult<?> expResult : expResultSet) {
    			//compare expected result to test result and save the returned id
    			ResultDesc resultDesc = ResultComparer.compare(result, expResult, QoSUtil.HSTORE_PARSER, resultOptions);
    			if (resultDesc != null) {
        			resultDesc.addTestResultUid(testResult.getUid());
        			resultDesc.setTestType(testType);
        			        			
        			final ResultHolder resultHolder = calculateResultCounter(testResult, expResult, resultDesc);

        			//check if there is a result message
        			if (resultHolder != null) {
            			TreeSet<ResultDesc> resultDescSet;
            			if (resultKeys.containsKey(testType)) {
            				resultDescSet = resultKeys.get(testType);
            			}
            			else {
            				resultDescSet = new TreeSet<>();
            				resultKeys.put(testType, resultDescSet);
            			}

        				resultDescSet.add(resultDesc);
        				
        				testResult.getResultKeyMap().put(resultDesc.getKey(), resultHolder.resultKeyType);
        				
            			if (AbstractResult.BEHAVIOUR_ABORT.equals(resultHolder.event)) {
            				break;
            			}
        			}        			
    			}
    		}
    	}
	}
	
	/**
	 * calculates and set the specific result counter
	 * @param testResult test result
	 * @param expResult expected test result
	 * @param resultDesc result description
	 * @return result type string, can be: 
	 * 		<ul>
	 * 			<li>{@link ResultDesc#STATUS_CODE_SUCCESS}</li>
	 * 			<li>{@link ResultDesc#STATUS_CODE_FAILURE}</li>
	 * 			<li>{@link ResultDesc#STATUS_CODE_INFO}</li>
	 * 		</ul>
	 */
	public static ResultHolder calculateResultCounter(final QoSTestResult testResult, final AbstractResult<?> expResult, final ResultDesc resultDesc) {
		String resultKeyType = null;
		String event = AbstractResult.BEHAVIOUR_NOTHING;
		
		//increase the failure or success counter of this result object
		if (resultDesc.getStatusCode().equals(ResultDesc.STATUS_CODE_SUCCESS)) {
			if (expResult.getOnSuccess() != null) {
				testResult.setSuccessCounter(testResult.getSuccessCounter()+1);
				if (AbstractResult.RESULT_TYPE_DEFAULT.equals(expResult.getSuccessType())) {
					resultKeyType = ResultDesc.STATUS_CODE_SUCCESS;
				}
				else {
					resultKeyType = ResultDesc.STATUS_CODE_INFO;
				}
				
				event = expResult.getOnSuccessBehaivour();
			}
		}
		else if (resultDesc.getStatusCode().equals(ResultDesc.STATUS_CODE_FAILURE)) {
			if (expResult.getOnFailure() != null) {
				testResult.setFailureCounter(testResult.getFailureCounter()+1);
				if (AbstractResult.RESULT_TYPE_DEFAULT.equals(expResult.getFailureType())) {
					resultKeyType = ResultDesc.STATUS_CODE_FAILURE;
				}
				else {
					resultKeyType = ResultDesc.STATUS_CODE_INFO;
				}
				
				event = expResult.getOnFailureBehaivour();
			}
		}
		else {
			resultKeyType = ResultDesc.STATUS_CODE_INFO;
			event = AbstractResult.BEHAVIOUR_NOTHING;
		}
		
		return resultKeyType != null ? new ResultHolder(resultKeyType, event) : null;
	}
	
	/**
	 * 
	 * @author lb
	 *
	 */
	public static class ResultHolder {
		final String resultKeyType;
		final String event;
		
		public ResultHolder(final String resultKeyType, final String event) {
			this.resultKeyType = resultKeyType;
			this.event = event;
		}

		public String getResultKeyType() {
			return resultKeyType;
		}

		public String getEvent() {
			return event;
		}
	}
}
