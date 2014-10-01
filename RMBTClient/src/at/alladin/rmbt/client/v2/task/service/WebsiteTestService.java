/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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
package at.alladin.rmbt.client.v2.task.service;

/**
 * 
 * @author lb
 *
 */
public interface WebsiteTestService {
	
	public static interface RenderingListener {
		public void onRenderFinished(WebsiteTestService test);
		
		public void onDownloadStarted(WebsiteTestService test);
		
		public boolean onTimeoutReached(WebsiteTestService test);
		
		public boolean onError(WebsiteTestService test);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getHash();
	
	/**
	 * 
	 * @return
	 */
	public int getStatusCode();
	
	/**
	 * 
	 * @return
	 */
	public long getDownloadDuration();
	
	/**
	 * 
	 */
	public void run(final String targetUrl, final long timeOut);
	
	/**
	 * 
	 * @return
	 */
	public boolean isRunning();
	
	/**
	 * 
	 * @return
	 */
	public boolean hasFinished();
	
	/**
	 * 
	 * @return
	 */
	public boolean hasError();
	
	/**
	 * 
	 * @param listener
	 */
	public void setOnRenderingFinishedListener(RenderingListener listener);
	
	/**
	 * 
	 * @return
	 */
	public WebsiteTestService getInstance();
	
	/**
	 * 
	 * @return
	 */
	public long getTxBytes();
	
	/**
	 * 
	 * @return
	 */
	public long getRxBytes();
	
	/**
	 * 
	 * @return
	 */
	public long getTotalTrafficBytes();
	
	/**
	 * 
	 * @return
	 */
	public int getResourceCount();
}
