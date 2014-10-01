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

import at.alladin.rmbt.client.QualityOfServiceTest;
import at.alladin.rmbt.client.v2.task.AbstractQoSTask;

/**
 * 
 * @author lb
 *
 */
public interface TestProgressListener {
	public enum TestProgressEvent {
		ON_CREATED, ON_START, ON_END;
	}
	
	/**
	 * 
	 * @param qosTest
	 */
	public void onQoSCreated(QualityOfServiceTest qosTest); 
	
	/**
	 * @return 
	 * 
	 */
    public void onQoSTestStart(AbstractQoSTask test);
    
    /**
     * 
     * @param test
     */
    public void onQoSTestEnd(AbstractQoSTask test);
}
