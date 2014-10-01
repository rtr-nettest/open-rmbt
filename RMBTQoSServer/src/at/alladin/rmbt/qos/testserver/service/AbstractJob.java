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
package at.alladin.rmbt.qos.testserver.service;

import java.util.concurrent.atomic.AtomicBoolean;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole;

/**
 * 
 * @author lb
 *
 */
public abstract class AbstractJob<R> implements Runnable {
	
	/**
	 * the main execution procedure, a result can be returned
	 * @return
	 * @throws Exception
	 */
	public abstract R execute() throws Exception;
	
	/**
	 * 
	 */
	public final AtomicBoolean isRunning = new AtomicBoolean(true);

	/**
	 * 
	 */
	protected final TestServerServiceEnum service;
	
	/**
	 * 
	 */
	R result = null;
	
	/**
	 * 
	 */
	public AbstractJob(TestServerServiceEnum service) {
		this.service = service;
	}

	/**
	 * 
	 */
	public synchronized void stop() {
		log("STOPPING SERVICE!", 0);
		isRunning.set(false);
	}

	/**
	 * 
	 */
	public void start() {
		isRunning.set(true);
	}
	
	/**
	 * 
	 */
	public void interrupt() {
		Thread.currentThread().interrupt();
	}
	
	/**
	 * 
	 * @return
	 */
	public synchronized R getResult() {
		while (isRunning.get()) {

		}
		
		return result;
	}

	/**
	 * 
	 * @param result
	 */
	public void setResult(R result) {
		this.result = result;
	}

	/**
	 * 
	 * @param msg
	 * @param verboseLevelNeeded
	 */
	public void log(String msg, int verboseLevelNeeded) {
		TestServerConsole.log("[" + getService().getName() + "]: " + msg, verboseLevelNeeded, getService());
	}
	
	/**
	 * 
	 * @return
	 */
	public TestServerServiceEnum getService() {
		return service;
	}

	@Override
	public String toString() {
		return "AbstractJob [isRunning=" + isRunning + ", service=" + service
				+ ", result=" + result + "]";
	}
}
