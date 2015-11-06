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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import at.alladin.rmbt.client.QualityOfServiceTest;
import at.alladin.rmbt.client.v2.task.result.QoSTestResult;
import at.alladin.rmbt.client.v2.task.result.QoSTestResultEnum;
import at.alladin.rmbt.client.v2.task.service.WebsiteTestService;
import at.alladin.rmbt.client.v2.task.service.WebsiteTestService.RenderingListener;

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
	
	public final static String PARAM_TIMEOUT = "timeout";
	
	public final static String RESULT_URL = "website_objective_url";
	
	public final static String RESULT_TIMEOUT = "website_objective_timeout";
	
	public final static String RESULT_DURATION = "website_result_duration";
	
	public final static String RESULT_STATUS = "website_result_status";
	
	public final static String RESULT_INFO = "website_result_info";
	
	public final static String RESULT_RX_BYTES = "website_result_rx_bytes";
	
	public final static String RESULT_TX_BYTES = "website_result_tx_bytes";

	
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
		final QoSTestResult result = initQoSTestResult(QoSTestResultEnum.WEBSITE);
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
	public QoSTestResultEnum getTestType() {
		return QoSTestResultEnum.WEBSITE;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#needsQoSControlConnection()
	 */
	public boolean needsQoSControlConnection() {
		return false;
	}
}
