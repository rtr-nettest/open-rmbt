/*******************************************************************************
 * Copyright 2016 Specure GmbH
 * Copyright 2019 alladin-IT GmbH
 * Copyright 2016 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
 *******************************************************************************/
package at.rtr.rmbt.qos.testserver.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.net.SyslogAppender;

import at.rtr.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.rtr.rmbt.qos.testserver.TestServerImpl;

/**
 * Logging  service for the test server
 * @author lb
 *
 */
public class LoggingService {

	/**
	 * all available loggers
	 */
	public final static Map<TestServerServiceEnum, Logger> LOGGER_MAP = new HashMap<>();
	
	/**
	 * tells if any logging [syslog, file, console] has been enabled
	 */
	public static boolean IS_LOGGING_AVAILABLE = false;

	/**
	 * 
	 * @param testServerImpl
	 * @return
	 */
	public static boolean isLoggingAvailable(final TestServerImpl testServerImpl) {
		return (testServerImpl.serverPreferences != null 
				&& (testServerImpl.serverPreferences.isConsoleLog() || testServerImpl.serverPreferences.isLoggingEnabled() || testServerImpl.serverPreferences.isSyslogEnabled()));
	}
	
	/**
	 * 
	 * @param testServerImpl
	 */
	public static synchronized void init(final TestServerImpl testServerImpl) {
		IS_LOGGING_AVAILABLE = isLoggingAvailable(testServerImpl);
		
		LogManager.getRootLogger().removeAllAppenders();
		
		if (testServerImpl.serverPreferences != null 
				&& testServerImpl.serverPreferences.isSyslogEnabled()) {
			LogManager.getRootLogger().addAppender(new SyslogAppender(
					new PatternLayout(testServerImpl.serverPreferences.getSyslogPattern()), 
					testServerImpl.serverPreferences.getSyslogHost(), 
					SyslogAppender.LOG_LOCAL0));
		}
	
		LOGGER_MAP.clear();
		LOGGER_MAP.put(TestServerServiceEnum.RUNTIME_GUARD_SERVICE, Logger.getLogger("QOS.DEBUG"));
		LOGGER_MAP.put(TestServerServiceEnum.TCP_SERVICE, Logger.getLogger("QOS.TCP"));
		LOGGER_MAP.put(TestServerServiceEnum.UDP_SERVICE, Logger.getLogger("QOS.UDP"));
		LOGGER_MAP.put(TestServerServiceEnum.TEST_SERVER, Logger.getLogger("QOS.SERVER"));
		
		//file logging appender:
		if (testServerImpl.serverPreferences != null 
				&& testServerImpl.serverPreferences.isLoggingEnabled()) {
			for (final Entry<TestServerServiceEnum, String> e : testServerImpl.serverPreferences.getLogFileMap().entrySet()) {
				final Logger l = LOGGER_MAP.get(e.getKey());
				try {
					l.addAppender(new DailyRollingFileAppender(
							new PatternLayout(testServerImpl.serverPreferences.getLoggingPattern()), e.getValue(), "_yyyy-MM-dd-a"));
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		//console appender:
		if (testServerImpl.serverPreferences != null 
				&& testServerImpl.serverPreferences.isConsoleLog()) {
			LogManager.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%d{ISO8601} - %m%n")));
		}
	}
	
	/**
	 * fatal level logging
	 * @param t
	 * @param message
	 * @param service
	 */
	public static void fatal(Throwable t, String message, TestServerServiceEnum service) {
		if (IS_LOGGING_AVAILABLE) {
			if (t != null) {
				LOGGER_MAP.get(service).fatal("[" + t.getClass().getCanonicalName() + ": " + t.getLocalizedMessage() + "] " + message, t);
			}
			else {
				LOGGER_MAP.get(service).fatal("[unknown Exception] " + message);
			}
		}
	}

	/**
	 * error level logging
	 * @param t
	 * @param message
	 * @param service
	 */
	public static void error(Throwable t, String message, TestServerServiceEnum service) {
		if (IS_LOGGING_AVAILABLE) {
			if (t != null) {
				LOGGER_MAP.get(service).error("[" + t.getClass().getCanonicalName() + ": " + t.getLocalizedMessage() + "] " + message, t);
			}
			else {
				LOGGER_MAP.get(service).error("[unknown Exception] " + message);
			}
		}
	}
	
	/**
	 * warn level logging
	 * @param message
	 * @param service
	 */
	public static void warn(String message, TestServerServiceEnum service) {
		if (IS_LOGGING_AVAILABLE) {
			LOGGER_MAP.get(service).warn(message);
		}		
	}
		
	/**
	 * info level logging
	 * @param message
	 * @param service
	 */
	public static void info(String message, TestServerServiceEnum service) {
		if (IS_LOGGING_AVAILABLE) {
			LOGGER_MAP.get(service).info(message);
		}
	}
	
	/**
	 * debug level logging
	 * @param message
	 * @param service
	 */
	public static void debug(String message, TestServerServiceEnum service) {
		if (IS_LOGGING_AVAILABLE) {
			LOGGER_MAP.get(service).debug(message);
		}		
	}
}
