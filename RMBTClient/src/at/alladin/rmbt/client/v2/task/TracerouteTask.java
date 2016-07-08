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
package at.alladin.rmbt.client.v2.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONObject;

import at.alladin.rmbt.client.QualityOfServiceTest;
import at.alladin.rmbt.client.RMBTClient;
import at.alladin.rmbt.client.v2.task.result.QoSTestResult;
import at.alladin.rmbt.client.v2.task.result.QoSTestResultEnum;
import at.alladin.rmbt.util.tools.TracerouteService;
import at.alladin.rmbt.util.tools.TracerouteService.HopDetail;

/**
 * 
 * @author lb
 *
 */
public class TracerouteTask extends AbstractQoSTask {

	public final static long DEFAULT_TIMEOUT = 10000000000L;
	
	public final static int DEFAULT_MAX_HOPS = 30;
	
	private final String host;
	
	private final long timeout;
	
	private final int maxHops;
	
	public final static String PARAM_HOST = "host";
	
	public final static String PARAM_TIMEOUT = "timeout";
	
	public final static String PARAM_MAX_HOPS = "max_hops";

	public final static String RESULT_HOST = "traceroute_objective_host";
	
	public final static String RESULT_DETAILS = "traceroute_result_details";
	
	public final static String RESULT_TIMEOUT = "traceroute_objective_timeout";
	
	public final static String RESULT_STATUS = "traceroute_result_status";
	
	public final static String RESULT_MAX_HOPS = "traceroute_objective_max_hops";
	
	public final static String RESULT_HOPS = "traceroute_result_hops";

	/**
	 * 
	 * @param taskDesc
	 */
	public TracerouteTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId) {
		super(nnTest, taskDesc, threadId, threadId);
		this.host = (String)taskDesc.getParams().get(PARAM_HOST);
		
		String value = (String) taskDesc.getParams().get(PARAM_TIMEOUT);
		this.timeout = value != null ? Long.valueOf(value) : DEFAULT_TIMEOUT;
		
		value = (String) taskDesc.getParams().get(PARAM_MAX_HOPS);
		this.maxHops = value != null ? Integer.valueOf(value) : DEFAULT_MAX_HOPS;
	}

	/**
	 * 
	 */
	public QoSTestResult call() throws Exception {
  		final QoSTestResult testResult = initQoSTestResult(QoSTestResultEnum.TRACEROUTE);

  		testResult.getResultMap().put(RESULT_HOST, host);
  		testResult.getResultMap().put(RESULT_TIMEOUT, timeout);
  		testResult.getResultMap().put(RESULT_MAX_HOPS, maxHops);
  		
		try {
			onStart(testResult);
			final TracerouteService pingTool = getQoSTest().getTestSettings().getTracerouteServiceClazz().newInstance();
	  		pingTool.setHost(host);
	  		pingTool.setMaxHops(maxHops);

	  		final List<HopDetail> pingDetailList = new ArrayList<TracerouteService.HopDetail>();
	  		pingTool.setResultListObject(pingDetailList);

	  		final Future<List<HopDetail>> traceFuture = RMBTClient.getCommonThreadPool().submit(pingTool);
	  		
	  		try {
	  			traceFuture.get(timeout, TimeUnit.NANOSECONDS);
	  			if (!pingTool.hasMaxHopsExceeded()) {
	  				testResult.getResultMap().put(RESULT_STATUS, "OK");
		  			testResult.getResultMap().put(RESULT_HOPS, pingDetailList.size());
	  			}
	  			else {
	  				testResult.getResultMap().put(RESULT_STATUS, "MAX_HOPS_EXCEEDED");
		  			testResult.getResultMap().put(RESULT_HOPS, maxHops);
	  			}
	  		}
	  		catch (TimeoutException e) {
	  			testResult.getResultMap().put(RESULT_STATUS, "TIMEOUT");
	  		}
	  		finally {
	  			if (pingDetailList != null) {
		  			JSONArray resultArray = new JSONArray();
		  			for (final HopDetail p : pingDetailList) {
		  				JSONObject json = p.toJson();
		  				if (json != null) {
		  					resultArray.put(json);
		  				}
		  			}
		  			
		  			testResult.getResultMap().put(RESULT_DETAILS, resultArray);
	  			}
	  		}
		}
		catch (Exception e) {
			e.printStackTrace();
			testResult.getResultMap().put(RESULT_STATUS, "ERROR");
		}
		finally {
			onEnd(testResult);
		}
		
        return testResult;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.AbstractRmbtTask#initTask()
	 */
	@Override
	public void initTask() {
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#getTestType()
	 */
	public QoSTestResultEnum getTestType() {
		return QoSTestResultEnum.TRACEROUTE;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#needsQoSControlConnection()
	 */
	public boolean needsQoSControlConnection() {
		return false;
	}
}
