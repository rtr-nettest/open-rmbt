/*******************************************************************************
 * Copyright 2019 alladin-IT GmbH
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

package at.rtr.rmbt.qos.testserver.mock;

import java.util.ArrayList;
import java.util.List;

import at.rtr.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.rtr.rmbt.qos.testserver.util.TestServerConsole;
import mockit.Mock;
import mockit.MockUp;

/**
 * 
 * @author Lukasz Budryk (lb@alladin.at)
 *
 */
public class TestServerConsoleMockup extends MockUp<TestServerConsole> {

	public static class ConsoleLog {
		final String msg;
		final int verboseLevel;
		final TestServerServiceEnum service;
		final long timeMs = System.currentTimeMillis();
		final Throwable t;
		
		public ConsoleLog(final String msg, final int verboseLevel, final TestServerServiceEnum service) {
			this(msg, null, verboseLevel, service);
		}
		
		public ConsoleLog(final String msg, final Throwable t, final int verboseLevel, final TestServerServiceEnum service) {
			this.msg = msg;
			this.t = t;
			this.verboseLevel = verboseLevel;
			this.service = service;
		}

		public String getMsg() {
			return msg;
		}

		public int getVerboseLevel() {
			return verboseLevel;
		}

		public TestServerServiceEnum getService() {
			return service;
		}

		public long getTimeMs() {
			return timeMs;
		}

		public Throwable getT() {
			return t;
		}

		@Override
		public String toString() {
			return "LogClass [msg=" + msg + ", verboseLevel=" + verboseLevel + ", service=" + service + ", timeMs="
					+ timeMs + (t != null ? ", t=" + t.getCause() : "") + "]";
		}
	}
	
	public final List<ConsoleLog> logList = new ArrayList<>();
	public final List<ConsoleLog> errorList = new ArrayList<>();
	
	@Mock
	public void log(String msg, int verboseLevelNeeded, TestServerServiceEnum service) {
		logList.add(new ConsoleLog(msg, verboseLevelNeeded, service));
	}
	
	@Mock
	public void error(String info, Throwable t, int verboseLevelNeeded, TestServerServiceEnum service) {
		errorList.add(new ConsoleLog(info, t, verboseLevelNeeded, service));
	}
	
	public List<ConsoleLog> getLogList() {
		return logList;
	}
	
	public List<ConsoleLog> getErrorList() {
		return errorList;
	}
}
