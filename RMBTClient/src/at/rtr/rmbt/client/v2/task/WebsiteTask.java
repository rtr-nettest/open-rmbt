/*******************************************************************************
 * Copyright 2013-2019 alladin-IT GmbH
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
package at.rtr.rmbt.client.v2.task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import at.rtr.rmbt.shared.qos.QosMeasurementType;
import at.rtr.rmbt.client.QualityOfServiceTest;
import at.rtr.rmbt.client.v2.task.result.QoSTestResult;
import at.rtr.rmbt.client.v2.task.service.WebsiteTestService;
import at.rtr.rmbt.client.v2.task.service.WebsiteTestService.RenderingListener;

/**
 * 
 * @author lb
 *
 */
public class WebsiteTask extends AbstractQoSTask {

	private final WebsiteTestService testImpl;
	
	private final String url;
	
	private final long timeout;
	
	private final static long DEFAULT_TIMEOUT = 10000000000L;
	
	public final static String PARAM_URL = "url";
	
	public final static String PARAM_CLEAR_CACHE = "clear_cache"; // TODO: implement
	
	public final static String PARAM_USER_AGENT = "user_agent"; // TODO: implement
	
	public final static String PARAM_TIMEOUT = "timeout";
	
	public final static String RESULT_URL = "website_objective_url";
	
	public final static String RESULT_TIMEOUT = "website_objective_timeout";
	
	public final static String RESULT_DURATION = "website_result_duration";
	
	public final static String RESULT_STATUS = "website_result_status";

	public final static String RESULT_ERROR_MESSAGE = "website_result_error_message";
	
	public final static String RESULT_INFO = "website_result_info";
	
	public final static String RESULT_RX_BYTES = "website_result_rx_bytes";
	
	public final static String RESULT_TX_BYTES = "website_result_tx_bytes";

	public final static String RESULT_HTTP_RESPONSE_TIME_NS = "website_result_first_http_response_time_ns";

	public final static String RESULT_REQUESTED_RESOURCE_COUNT = "website_result_request_count";

	
	/**
	 * 
	 * @param client
	 * @param taskDesc
	 * @param threadId
	 */
	public WebsiteTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId) {
		super(nnTest, taskDesc, threadId, threadId);
		this.testImpl = nnTest.getTestSettings().getWebsiteTestService().getInstance();

		String value = (String) taskDesc.getParams().get(PARAM_URL);
		this.url = value != null ? value : null;

		value = (String) taskDesc.getParams().get(PARAM_TIMEOUT);
		this.timeout = value != null ? new Long(value) : DEFAULT_TIMEOUT;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	public QoSTestResult call() throws Exception {
		final QoSTestResult result = initQoSTestResult(QosMeasurementType.WEBSITE);
		try {
			onStart(result);
			
			result.getResultMap().put(RESULT_URL, url);
			result.getResultMap().put(RESULT_TIMEOUT, String.valueOf(timeout));

			final CountDownLatch latch = new CountDownLatch(1);
			
			testImpl.setOnRenderingFinishedListener(new RenderingListener() {
					
				public boolean onTimeoutReached(WebsiteTestService test) {
					System.out.println("WEBSITETEST timeout");
					result.getResultMap().put(RESULT_STATUS, test.getStatusCode());
					result.getResultMap().put(RESULT_INFO, "TIMEOUT");
					//result.getResultMap().put(RESULT_DURATION, (test.getDownloadDuration() / 1000000));
					result.getResultMap().put(RESULT_DURATION, test.getDownloadDuration());
					result.getResultMap().put(RESULT_RX_BYTES, test.getRxBytes());
					result.getResultMap().put(RESULT_TX_BYTES, test.getTxBytes());
					if (test.getHttpResponseTime() > 0) {
						result.getResultMap().put(RESULT_HTTP_RESPONSE_TIME_NS, test.getHttpResponseTime());
					}
					result.getResultMap().put(RESULT_REQUESTED_RESOURCE_COUNT, test.getResourceCount());
					if (test.hasError()) {
						result.getResultMap().put(RESULT_ERROR_MESSAGE, test.getErrorMessage());
					}
					latch.countDown();
					return true;
				}
					
				public void onRenderFinished(WebsiteTestService test) {
					System.out.println("WEBSITETEST finished");
					result.getResultMap().put(RESULT_STATUS, test.getStatusCode());
					result.getResultMap().put(RESULT_INFO, "OK");
					//result.getResultMap().put(RESULT_DURATION, (test.getDownloadDuration() / 1000000));
					result.getResultMap().put(RESULT_DURATION, test.getDownloadDuration());
					result.getResultMap().put(RESULT_RX_BYTES, test.getRxBytes());
					result.getResultMap().put(RESULT_TX_BYTES, test.getTxBytes());
					if (test.getHttpResponseTime() > 0) {
						result.getResultMap().put(RESULT_HTTP_RESPONSE_TIME_NS, test.getHttpResponseTime());
					}
					result.getResultMap().put(RESULT_REQUESTED_RESOURCE_COUNT, test.getResourceCount());
					if (test.hasError()) {
						result.getResultMap().put(RESULT_ERROR_MESSAGE, test.getErrorMessage());
					}
					latch.countDown();
				}
					
				public void onDownloadStarted(WebsiteTestService test) {
					//nothing to do?
				}

				public boolean onError(WebsiteTestService test) {
					System.out.println("WEBSITETEST Error");
					result.getResultMap().put(RESULT_STATUS, test.getStatusCode());
					result.getResultMap().put(RESULT_INFO, "ERROR");
					//result.getResultMap().put(RESULT_DURATION, (test.getDownloadDuration() / 1000000));
					result.getResultMap().put(RESULT_DURATION, test.getDownloadDuration());
					result.getResultMap().put(RESULT_RX_BYTES, test.getRxBytes());
					result.getResultMap().put(RESULT_TX_BYTES, test.getTxBytes());
					if (test.getHttpResponseTime() > 0) {
						result.getResultMap().put(RESULT_HTTP_RESPONSE_TIME_NS, test.getHttpResponseTime());
					}
					result.getResultMap().put(RESULT_REQUESTED_RESOURCE_COUNT, test.getResourceCount());
					if (test.hasError()) {
						result.getResultMap().put(RESULT_ERROR_MESSAGE, test.getErrorMessage());
					}
					latch.countDown();
					return true;
				}
			});
			
			System.out.println("Starting WEBSITETASK");
				
			testImpl.run(url, (int)(timeout/1000000));			
			latch.await(timeout, TimeUnit.NANOSECONDS);
			
			System.out.println("Stopping WEBSITETASK");
			
			return result;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			onEnd(result);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.AbstractQoSTask#initTask()
	 */
	@Override
	public void initTask() {
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#getTestType()
	 */
	public QosMeasurementType getTestType() {
		return QosMeasurementType.WEBSITE;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#needsQoSControlConnection()
	 */
	public boolean needsQoSControlConnection() {
		return false;
	}
}
