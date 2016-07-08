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
package at.alladin.rmbt.qos.testserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.alladin.rmbt.qos.testserver.plugin.rest.RestService;

/**
 * 
 * @author lb
 *
 */
public class ServerPreferences {
	public static enum TestServerServiceEnum {
		TEST_SERVER("TEST SERVER"),
		TCP_SERVICE("TCP MONITOR"),
		UDP_SERVICE("UDP MONITOR"),
		RUNTIME_GUARD_SERVICE("RUNTIME GUARD SERVICE");
		
		protected String name;
		private TestServerServiceEnum(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	
	public class UdpPort  {
		final boolean isNio;
		final int port;
		
		public UdpPort(final boolean isNio, final int port) {
			this.isNio = isNio;
			this.port = port;
		}

		@Override
		public String toString() {
			return "UdpPort [isNio=" + isNio + ", port=" + port + "]";
		}	
	}
	
	public final static int TEST_SERVER_ID = 0;
	public final static int TCP_SERVICE_ID = 1;
	public final static int UDP_SERVICE_ID = 2;
	
	public static final String ARG_SERVER_PORT = "-P";
	public static final String ARG_UDP_PORT_RANGE = "-U";
	public static final String ARG_MAX_THREADS = "-T";
	public static final String ARG_HELP = "-H";
	public static final String ARG_IP_CHECK = "-IC";
	public static final String ARG_IP = "-IP";
	public static final String ARG_CONFIG_FILE = "-F";
	public static final String ARG_SECRET_KEY = "-K";
	public static final String ARG_USE_SSL = "-S";
	public static final String ARG_VERBOSE = "-V";
	public static final String ARG_VERBOSE2 = "-VV";
	public static final String ARG_LOG = "-L";
	
	public static final String PARAM_SERVER_PORT = "server.port";
	public static final String PARAM_SERVER_THREADS = "server.threads";
	public static final String PARAM_SERVER_SECRET_KEY = "server.secret";
	public static final String PARAM_SERVER_USE_SSL = "server.ssl";
	public static final String PARAM_SERVER_VERBOSE = "server.verbose";
	public static final String PARAM_SERVER_UDP_MIN_PORT = "server.udp.minport";
	public static final String PARAM_SERVER_UDP_MAX_PORT = "server.udp.maxport";
	public static final String PARAM_SERVER_UDP_PORT_LIST = "server.udp.ports";
	public static final String PARAM_SERVER_UDP_NIO_PORT_LIST = "server.udp.nio.ports";
	public static final String PARAM_SERVER_LOGGING = "server.logging";
	public static final String PARAM_SERVER_LOGGING_PATTERN = "server.log.pattern";
	public static final String PARAM_SERVER_SYSLOG = "server.syslog";
	public static final String PARAM_SERVER_SYSLOG_HOST = "server.syslog.host";
	public static final String PARAM_SERVER_SYSLOG_PATTERN = "server.syslog.pattern";
	public static final String PARAM_SERVER_LOG_CONSOLE = "server.log.console";
	public static final String PARAM_SERVER_COMMAND_CONSOLE = "server.console";
	public static final String PARAM_SERVER_LOG_FILE = "server.log";
	public static final String PARAM_SERVER_UDP_SERVICE_LOG_FILE = "server.log.udp";
	public static final String PARAM_SERVER_TCP_SERVICE_LOG_FILE = "server.log.tcp";
	public static final String PARAM_SERVER_TCP_IP_CHECK = "server.ip.check";
	public static final String PARAM_SERVER_IP = "server.ip";
	
	public static final String REGEX_PORT_LIST = "([0-9]+)[,]?";
	public static final String REGEX_IP_LIST = "([a-fA-F0-9.:]+)[,]?";
	
	private int serverPort = 5234;
	private int udpPortMin = 0;
	private int udpPortMax = 0;
	private final Set<UdpPort> udpPortSet = new TreeSet<>(new Comparator<UdpPort>() {
		@Override
		public int compare(UdpPort o1, UdpPort o2) {
			return Integer.compare(o1.port, o2.port);
		}
		
	});
	
	private int maxThreads = 100;
	private boolean useSsl = false;
	private int verboseLevel = 0;
	private String secretKey = null;
	private boolean isIpCheck = false;
	private boolean isLoggingEnabled = true;
	private String loggingPattern = "%p %d{ISO8601} - %m%n";
	private boolean isSyslogEnabled = true;
	private String syslogHost = "localhost";
	private String syslogPattern = "%p %d{ISO8601} %c - %m%n";
	private boolean isConsoleLog = false;
	private boolean isCommandConsoleEnabled = false;
	private final TreeMap<TestServerServiceEnum, String> logFileMap = new TreeMap<>();
	private final Set<InetAddress> inetAddrBindToSet = new HashSet<>();
	private final long startTimestamp = System.currentTimeMillis();

	///////////////////////////
	// Plugins/Services:
	///////////////////////////
	public final static String PLUGIN_REST_SERVICE = "REST";
	private final Map<String, ServiceSetting> pluginMap = new HashMap<>();
	
	/**
	 * 
	 * @throws TestServerException
	 * @throws UnknownHostException 
	 */
	public ServerPreferences() throws TestServerException {
		loadFromConfigFile("config.properties");
		setUdpPortSet();
	    checkConstraints();
	}
	
	/**
	 * 
	 * @param args
	 * @throws TestServerException
	 * @throws UnknownHostException 
	 */
	public ServerPreferences(String[] args) throws TestServerException {
		loadFromConfigFile("config.properties");
		try {
		    for (int i = 0; i < args.length; i++) {
		    	String arg = args[i].toUpperCase();
		    	
		    	if (arg.equals(ARG_SERVER_PORT)) {
		    		serverPort = Integer.parseInt(args[++i]);
		    	}
		    	else if (arg.equals(ARG_IP)) {
		    		inetAddrBindToSet.add(InetAddress.getByName(args[++i]));
		    	}
		    	else if (arg.equals(ARG_IP_CHECK)) {
		    		isIpCheck = true;
		    	}
		    	else if (arg.equals(ARG_UDP_PORT_RANGE)) {
		    		udpPortMin = Integer.parseInt(args[++i]);
		    		udpPortMax = Integer.parseInt(args[++i]);
		    	}
		    	else if (arg.equals(ARG_MAX_THREADS)) {
		    		maxThreads = Integer.parseInt(args[++i]);
		    		if (maxThreads <= 10) {
		    			writeWarningNumThreadsTooLowString(maxThreads);
		    		}
		    	}
		    	else if (arg.equals(ARG_CONFIG_FILE)) {
		    		loadFromConfigFile(args[++i]);
		    	}
		    	else if (arg.equals(ARG_SECRET_KEY)) {
		    		secretKey = args[++i];
		    	}
		    	else if (arg.equals(ARG_USE_SSL)) {
		    		useSsl = true;
		    	}
		    	else if (arg.equals(ARG_VERBOSE)) {
		    		verboseLevel = 1;
		    	}
		    	else if (arg.equals(ARG_VERBOSE2)) {
		    		verboseLevel = 2;
		    	}
		    	else if (arg.equals(ARG_HELP)) {
		    		writeErrorString();
		    	}
		    	else if (arg.equals(ARG_LOG)) {
	    			logFileMap.put(TestServerServiceEnum.TEST_SERVER, args[++i]);
		    	}
		    	else {
		    		throw new TestServerException("UNKNOWN PARAMETER: " + arg, null);
		    	}
		    }
		    
		    setUdpPortSet();
		}
		catch (Exception e) {
			throw new TestServerException("TEST SERVER EXCEPTION", e);
		}
		
	    checkConstraints();
	}
	
	/**
	 * 
	 */
	private void setUdpPortSet() {
		if (udpPortMin > 0) {
			for (int port = udpPortMin; port <= udpPortMax; port++) {
				udpPortSet.add(new UdpPort(false, port));
			}
		}
	}
	
	/**
	 * 
	 * @param fileName
	 * @throws TestServerException
	 */
	private void loadFromConfigFile(String fileName) throws TestServerException {
		Properties prop = new Properties();
	   	try {
	   		prop.load(new FileInputStream(fileName));
	   		
	   		String param = prop.getProperty(PARAM_SERVER_PORT);
	   		if (param!=null) {
		   		serverPort = Integer.parseInt(param.trim());	   			
	   		}

	   		param = prop.getProperty(PARAM_SERVER_IP);
	   		if (param!=null) {
	   			Pattern p = Pattern.compile(REGEX_IP_LIST);
	   			Matcher m = p.matcher(param);
   				while (m.find()) {
   		   			inetAddrBindToSet.add(InetAddress.getByName(m.group(1)));
   				}
	   		}
	   		
	   		param = prop.getProperty(PARAM_SERVER_TCP_IP_CHECK);
	   		if (param!=null) {
		   		isIpCheck = Boolean.parseBoolean(param.trim());
	   		}

	   		param = prop.getProperty(PARAM_SERVER_THREADS);
	   		if (param!=null) {
		   		maxThreads = Integer.parseInt(param.trim());	   			
	   		}
	   		
	   		param = prop.getProperty(PARAM_SERVER_UDP_MIN_PORT);
	   		if (param!=null) {
		   		udpPortMin = Integer.parseInt(param.trim()); 			
	   		}
	   		
	   		param = prop.getProperty(PARAM_SERVER_UDP_MAX_PORT);
	   		if (param!=null) {
		   		udpPortMax = Integer.parseInt(param.trim());
	   		}
	   		
	   		param = prop.getProperty(PARAM_SERVER_USE_SSL);
	   		if (param!=null) {
		   		useSsl = Boolean.parseBoolean(param.trim());
	   		}

	   		param = prop.getProperty(PARAM_SERVER_VERBOSE);
	   		if (param!=null) {
		   		verboseLevel = Integer.parseInt(param.trim());
	   		}
	   		
	   		param = prop.getProperty(PARAM_SERVER_SECRET_KEY);
	   		if (param!=null) {
		   		secretKey = param.trim();
	   		}

	   		param = prop.getProperty(PARAM_SERVER_COMMAND_CONSOLE);
	   		if (param!=null) {
	   			isCommandConsoleEnabled = Boolean.parseBoolean(param.trim());
	   		}

	   		param = prop.getProperty(PARAM_SERVER_SYSLOG);
	   		if (param!=null) {
	   			isSyslogEnabled = Boolean.parseBoolean(param.trim());
	   		}

	   		param = prop.getProperty(PARAM_SERVER_SYSLOG_HOST);
	   		if (param!=null) {
	   			syslogHost = param.trim();
	   		}

	   		param = prop.getProperty(PARAM_SERVER_SYSLOG_PATTERN);
	   		if (param!=null) {
	   			syslogPattern = param.trim();
	   		}

	   		param = prop.getProperty(PARAM_SERVER_LOGGING);
	   		if (param!=null) {
	   			isLoggingEnabled = Boolean.parseBoolean(param.trim());
	   		}

	   		param = prop.getProperty(PARAM_SERVER_LOGGING_PATTERN);
	   		if (param!=null) {
	   			loggingPattern = param.trim();
	   		}

	   		param = prop.getProperty(PARAM_SERVER_LOG_CONSOLE);
	   		if (param!=null) {
	   			isConsoleLog = Boolean.parseBoolean(param.trim());
	   		}

	   		param = prop.getProperty(PARAM_SERVER_LOG_FILE);
	   		if (param!=null) {
	   			logFileMap.put(TestServerServiceEnum.TEST_SERVER, param.trim());
	   		}

	   		param = prop.getProperty(PARAM_SERVER_UDP_SERVICE_LOG_FILE);
	   		if (param!=null) {
	   			logFileMap.put(TestServerServiceEnum.UDP_SERVICE, param.trim());
	   		}

	   		param = prop.getProperty(PARAM_SERVER_TCP_SERVICE_LOG_FILE);
	   		if (param!=null) {
	   			logFileMap.put(TestServerServiceEnum.TCP_SERVICE, param.trim());
	   		}
	   		
	   		param = prop.getProperty(PARAM_SERVER_UDP_PORT_LIST);
	   		if (param!=null) {
	   			Pattern p = Pattern.compile(REGEX_PORT_LIST);
	   			Matcher m = p.matcher(param);
   				while (m.find()) {
   					udpPortSet.add(new UdpPort(false, Integer.valueOf(m.group(1))));
   				}
	   		}

	   		param = prop.getProperty(PARAM_SERVER_UDP_NIO_PORT_LIST);
	   		if (param!=null) {
	   			Pattern p = Pattern.compile(REGEX_PORT_LIST);
	   			Matcher m = p.matcher(param);
   				while (m.find()) {
   					final UdpPort udpPort = new UdpPort(true, Integer.valueOf(m.group(1)));
   					if (udpPortSet.contains(udpPort)) {
   						throw new TestServerException("Cannot create non blocking datagram channel. UDP port: " + udpPort.port + " already defined as blocking.", null);
   					}
   					else {
   						udpPortSet.add(udpPort);
   					}
   				}
	   		}

	   		/////////////////////////////////
	   		//	Services/Plugins:
	   		/////////////////////////////////
	   		
	   		pluginMap.put("REST", new RestService(prop, this));

	   	} catch (IOException ex) {
	   		ex.printStackTrace();
			throw new TestServerException("TEST SERVER EXCEPTION", ex);
	   	}
	}
	
	/**
	 * 
	 * @throws TestServerException
	 * @throws UnknownHostException 
	 */
	private void checkConstraints() throws TestServerException {
		if (udpPortMin > udpPortMax) {
			throw new TestServerException("UDP MIN PORT (=" + udpPortMin + ") MUST BE LOWER THAN UDP MAX PORT (=" + udpPortMax + ")", null);
		}
		if (maxThreads < 5) {
			throw new TestServerException("NUMBER OF THREADS TOO LOW (" + maxThreads + ")", null);
		}

		//create log paths:
		for (String fileName : logFileMap.values()) {
			File f = new File(fileName);
			File dir = new File(f.getParent());
			if (!dir.exists()) {
				dir.mkdir();
			}
		}
		
		if (inetAddrBindToSet.isEmpty()) {
			try {
				inetAddrBindToSet.add(InetAddress.getByName("0.0.0.0"));
			}
			catch (Exception e) {
				throw new TestServerException(e.getLocalizedMessage(), e);
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public int getServerPort() {
		return serverPort;
	}
	
	/**
	 * 
	 * @param serverPort
	 */
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getUdpPortMin() {
		return udpPortMin;
	}
	
	/**
	 * 
	 * @param udpPortMin
	 */
	public void setUdpPortMin(int udpPortMin) {
		this.udpPortMin = udpPortMin;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getMaxThreads() {
		return maxThreads;
	}

	/**
	 * 
	 * @param maxThreads
	 */
	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	/**
	 * 
	 * @return
	 */
	public int getUdpPortMax() {
		return udpPortMax;
	}
	
	/**
	 * 
	 * @param udpPortMax
	 */
	public void setUdpPortMax(int udpPortMax) {
		this.udpPortMax = udpPortMax;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getSecretKey() {
		return secretKey;
	}
	
	/**
	 * 
	 * @param secretKey
	 */
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean useSsl() {
		return useSsl;
	}
	
	/**
	 * 
	 * @param useSsl
	 */
	public void setUseSsl(boolean useSsl) {
		this.useSsl = useSsl;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getVerboseLevel() {
		return verboseLevel;
	}
	
	/**
	 * 
	 * @param verboseLevel
	 */
	public void setVerboseLevel(int verboseLevel) {
		this.verboseLevel = verboseLevel;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isLoggingEnabled() {
		return isLoggingEnabled;
	}

	/**
	 * 
	 * @param isLoggingEnabled
	 */
	public void setLoggingEnabled(boolean isLoggingEnabled) {
		this.isLoggingEnabled = isLoggingEnabled;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, ServiceSetting> getPluginMap() {
		return pluginMap;
	}

	/**
	 * 
	 */
	public static void writeErrorString() {
		System.out.println("---------------------------------------------");
		System.out.println("Error initializing QoS TestServer\n");
		System.out.println("Supported parameters:");
		System.out.println("-h => shows this help.");
		System.out.println("-s => ssl connection.");
		System.out.println("-ip => the ip, the test server will be bound to.");
		System.out.println("-ic => register all TCP test candidates and make an ip check before responding.");
		System.out.println("-v => verbose level 1, more debug output.");
		System.out.println("-vv => verbose level 2, even more debug output.");
		System.out.println("-f [file_name] => initilizes the server by using a config file (default: config.properties).");
		System.out.println("-p [port_number] => initilizes the server on this port number (default: 5233).");
		System.out.println("-u [min_port_number] [max_port_number] => supported udp port range by this server: from min_port_number (inclusive) to max_port_number (exclusive).");
		System.out.println("-t [max_threads] => The max number of threads (clients) this server can hanle at the same time (default: 100).");
		System.out.println("-k [secret_key] => The secret key used for client-server communication.");
		System.out.println("\nExample: -p 5233 -u 10000 20000 => makes the server listen on port 5233 for incoming test requests and let it accept udp test connections on ports 10000 to 20000.");
		System.out.println("\nIf no parameter is set then the default config file config.properties is beeing used. If this file isn't found, the default values are beeing used.");
		System.out.println("---------------------------------------------");
	}

	/**
	 * 
	 * @param threads
	 */
	public void writeWarningNumThreadsTooLowString(int threads) {
		System.out.println("---------------------------------------------");
		System.out.println("Warning: number of threads is low (" + threads + ")\nThe TestServer can handle only this amount of clients at the same time. Waiting times may occure.");
		System.out.println("---------------------------------------------\n");
	}

	/**
	 * 
	 * @return
	 */
	public Set<UdpPort> getUdpPortSet() {
		return udpPortSet;
	}

	/**
	 * 
	 * @return
	 */
	public TreeMap<TestServerServiceEnum, String> getLogFileMap() {
		return logFileMap;
	}

	/**
	 * 
	 * @return
	 */
	public Set<InetAddress> getInetAddrBindToSet() {
		return inetAddrBindToSet;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isIpCheck() {
		return isIpCheck;
	}

	/**
	 * 
	 * @param isIpCheck
	 */
	public void setIpCheck(boolean isIpCheck) {
		this.isIpCheck = isIpCheck;
	}

	public boolean isConsoleLog() {
		return isConsoleLog;
	}

	public void setConsoleLog(boolean isConsoleLog) {
		this.isConsoleLog = isConsoleLog;
	}

	public boolean isCommandConsoleEnabled() {
		return isCommandConsoleEnabled;
	}

	public void setCommandConsoleEnabled(boolean isCommandConsoleEnabled) {
		this.isCommandConsoleEnabled = isCommandConsoleEnabled;
	}
	
	public long getStartTimestamp() {
		return startTimestamp;
	}
	
	public boolean isSyslogEnabled() {
		return isSyslogEnabled;
	}

	public void setSyslogEnabled(boolean isSyslogEnabled) {
		this.isSyslogEnabled = isSyslogEnabled;
	}

	public String getSyslogHost() {
		return syslogHost;
	}

	public void setSyslogHost(String syslogHost) {
		this.syslogHost = syslogHost;
	}
	
	public String getSyslogPattern() {
		return syslogPattern;
	}

	public void setSyslogPattern(String syslogPattern) {
		this.syslogPattern = syslogPattern;
	}
	
	public String getLoggingPattern() {
		return loggingPattern;
	}

	public void setLoggingPattern(String loggingPattern) {
		this.loggingPattern = loggingPattern;
	}

	@Override
	public String toString() {
		return "ServerPreferences [serverPort=" + serverPort + ", udpPortMin="
				+ udpPortMin + ", udpPortMax=" + udpPortMax + ", udpPortSet="
				+ udpPortSet + ", maxThreads=" + maxThreads + ", useSsl="
				+ useSsl + ", verboseLevel=" + verboseLevel + ", secretKey="
				+ secretKey + ", isIpCheck=" + isIpCheck
				+ ", isLoggingEnabled=" + isLoggingEnabled
				+ ", loggingPattern=" + loggingPattern + ", isSyslogEnabled="
				+ isSyslogEnabled + ", syslogHost=" + syslogHost
				+ ", syslogPattern=" + syslogPattern + ", isConsoleLog="
				+ isConsoleLog + ", isCommandConsoleEnabled="
				+ isCommandConsoleEnabled + ", logFileMap=" + logFileMap
				+ ", inetAddrBindToSet=" + inetAddrBindToSet
				+ ", startTimestamp=" + startTimestamp + ", pluginMap="
				+ pluginMap + "]"
				+ "\n\tOnline since: " + (new java.util.Date(startTimestamp)).toString();
	}
	
	
	
	/////////////////////////////////////////////////////////
	//
	//		Server Services
	//
	/////////////////////////////////////////////////////////
	
	public static abstract class ServiceSetting {
		protected boolean isEnabled;
		protected final String name;
		
		public ServiceSetting(String name, boolean isEnabled) {
			this.isEnabled = isEnabled;
			this.name = name;
		}
		
		public boolean isEnabled() {
			return isEnabled;
		}

		public void setEnabled(boolean isEnabled) {
			this.isEnabled = isEnabled;
		}
		
		public String getName() {
			return name;
		}

		public abstract void start() throws UnknownHostException;
		public abstract void stop();
		public abstract void setParam(Properties properties);
	}	
}
