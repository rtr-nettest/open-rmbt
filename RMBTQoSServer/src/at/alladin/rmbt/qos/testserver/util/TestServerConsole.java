/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
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
package at.alladin.rmbt.qos.testserver.util;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONException;
import org.json.JSONObject;

import at.alladin.rmbt.qos.testserver.ClientHandler;
import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.servers.AbstractUdpServer;
import at.alladin.rmbt.qos.testserver.TestServer;
import at.alladin.rmbt.qos.testserver.service.ServiceManager.FutureService;
import at.alladin.rmbt.qos.testserver.tcp.TcpMultiClientServer;
import at.alladin.rmbt.qos.testserver.udp.UdpTestCandidate;


public class TestServerConsole extends PrintStream {
	
	public final static class ErrorReport {
		Date date;
		Date lastDate;
		int counter = 0;
		String error;
		
		public ErrorReport(String error, Date date) {
			this.date = date;
			this.error = error;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}
		
		public void increaseCounter() {
			this.counter++;
			this.lastDate = new Date();
		}
		
		public JSONObject toJson() throws JSONException {
			JSONObject json = new JSONObject();
			json.put("first_date", date);
			json.put("counter", counter);
			json.put("last_date", lastDate);
			json.put("error", error);
			return json;
		}
	}
	
	public final static ConcurrentMap<String, ErrorReport> errorReportMap = new ConcurrentHashMap<String, TestServerConsole.ErrorReport>();
	
	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public final static String HINT_SHOW = "SHOW what? Available options: [tcp] [udp] [info]";
	
	public final static String COMMAND_EXIT_COMMAND_PROMPT = "exit";
	
	public final static String COMMAND_SHOW = "show";
	
	public final static String COMMAND_HELP = "help";
	
	public final static String COMMAND_SETTINGS = "set";
	
	public final static String COMMAND_SHUTDOWN = "shutdown";
	
	public final static String SUBCOMMAND_SETTINGS_SET_VERBOSE = "verbose";
	
	public final static String SUBCOMMAND_SHOW_INFO = "info";
	
	public final static String SUBCOMMAND_SHOW_CLIENTS = "clients";
	
	public final static String SUBCOMMAND_SHOW_OPENED_TCP_PORTS = "tcp";
	
	public final static String SUBCOMMAND_SHOW_OPENED_UDP_PORTS = "udp";
	
	public final static String SUBCOMMAND_FORCE = "force";
	
	public final static String DEBUG_COMMAND_LIST_SERVICES = "dls";
	
	public final static String DEBUG_COMMAND_FORCE_SERVICE_EVENT = "dfse";
	
	public final static String PROMPT = "\n\nEnter command:";
	
	final Thread keyListener;
	
	public boolean isCommandLine;

	public TestServerConsole() {
		super(System.out, true);
	    keyListener = new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (System.console() != null) {
					while (true) {
						try {
							String command = System.console().readLine();
							if (!isCommandLine) {
								isCommandLine = true;
								printLine();
								printCommand("\n\nEntered command mode!\n\nTo switch to debug output type 'exit' and press enter."
										+ "\nType 'help' for some information on command mode.");
							}
							else {
								String[] commands = command.split("[ ]");
								
								switch (commands[0]) {
								case COMMAND_EXIT_COMMAND_PROMPT:
									printlnCommand("\nStopped command mode.\n\n");
									isCommandLine = false;
									break;
									
								case COMMAND_SHOW:
									if (commands.length > 1) {
										switch (commands[1]) {
										case SUBCOMMAND_SHOW_CLIENTS:
											printLine();
											printlnCommand("\n");
											printlnCommand("\nActive clients: " + TestServer.clientHandlerSet.size() + "\n");
											for (ClientHandler client : TestServer.clientHandlerSet) {
												printlnCommand(" - " + client.getName());
												printlnCommand("\t UdpIncomingResults: ");
												for (Entry<Integer, UdpTestCandidate> udpIn : client.getClientUdpInDataMap().entrySet()) {
													printlnCommand("\t\t " + udpIn.toString());
												}
												printlnCommand("\t UdpOutgoingResults: ");
												for (Entry<Integer, UdpTestCandidate> udpOut : client.getClientUdpOutDataMap().entrySet()) {
													printlnCommand("\t\t " + udpOut.toString());
												}
											}
											printLine();
										break;
										case SUBCOMMAND_SHOW_INFO:
											printLine();
											printlnCommand("\n" + TestServer.TEST_SERVER_VERSION_NAME);
											printlnCommand("\nCurrent server settings: " + TestServer.serverPreferences.toString());
											printlnCommand("\nQoSTestServer is listening on the following addresses:");
											for (ServerSocket ss : TestServer.serverSocketList) {
												printlnCommand("\t- " + ss.toString());
											}
											printLine();
										break;
										case SUBCOMMAND_SHOW_OPENED_TCP_PORTS:
											boolean showTcp = true;
											if (TestServer.tcpServerMap.size() > 500) {
												showTcp = (commands.length > 2 && commands[2].equals(SUBCOMMAND_FORCE));
											}

											printlnCommand("\nFound " + TestServer.tcpServerMap.values().size() + " active TCP sockets.");
											
											if (showTcp) {
												printlnCommand("\n\nList of all TCP sockets:\n");
												for (Entry<Integer, List<TcpMultiClientServer>> e : TestServer.tcpServerMap.entrySet()) {
													for (TcpMultiClientServer i : e.getValue()) {
														printlnCommand("Port " + e.getKey() + " -> " + i.toString());	
													}
												}
											}
											else {
												printlnCommand("\n\nAre you sure you want to display them all? To ignore this warning type: 'show tcp force'.");
											}
											break;
										case SUBCOMMAND_SHOW_OPENED_UDP_PORTS:
											printlnCommand("Available udp sub options: "
													+ "\nshow udp [#####] \t- where ##### is a port number between 0 and 65535"
													+ "\nshow udp data \t\t- shows UDP servers with active ClientData awaiting incoming packets"
													+ "\nshow udp nodata \t- shows UDP servers without active ClientData\n\n");
											if (commands.length > 2) {
												if ("data".equals(commands[2])) {
													printlnCommand("\nMultiClient UDP Server containing ClientData:");
													for (List<AbstractUdpServer<?>> udpServerList : TestServer.udpServerMap.values()) {
														for (AbstractUdpServer<?> udpServer : udpServerList) {
															if (!udpServer.getIncomingMap().isEmpty()) {
																printlnCommand("\nUDP Server Info: " + udpServer.toString());
															}
														}
													}
												}
												else if ("nodata".equals(commands[2])) {
													printlnCommand("\nMultiClient UDP Server wihtout ClientData:");
													for (List<AbstractUdpServer<?>> udpServerList : TestServer.udpServerMap.values()) {
														for (AbstractUdpServer<?> udpServer : udpServerList) {
															if (udpServer.getIncomingMap().isEmpty()) {
																printlnCommand("\nUDP Server Info: " + udpServer.toString());
															}
														}
													}													
												}
												else {
													try {
														List<AbstractUdpServer<?>> udpServerList = TestServer.udpServerMap.get(Integer.valueOf(commands[2]));
														for (AbstractUdpServer<?> udpServer : udpServerList) {
															printlnCommand("\nMultiClient UDP Server Info:\n" + udpServer.toString());
														}
													}
													catch (Exception e) {
														e.printStackTrace();
													}
												}
											}
											else {
												printlnCommand("\nActive UDP ports: " + TestServer.serverPreferences.getUdpPortSet());	
											}
											break;
										default:
											printlnCommand("\n" + HINT_SHOW);
											break;
										}
									}
									else {
										printlnCommand("\n" + HINT_SHOW);
									}
									break;
									
								case COMMAND_SETTINGS:
									if (commands.length > 1) {
										switch (commands[1]) {
										
										case SUBCOMMAND_SETTINGS_SET_VERBOSE:
											if (commands.length > 2) {
												int verboseLevel = Integer.parseInt(commands[2]);
												verboseLevel = verboseLevel < 0 ? 0 : (verboseLevel > 2 ? 2 : verboseLevel);
												
												printlnCommand("\nSetting verbose level to: " + verboseLevel);
												TestServer.serverPreferences.setVerboseLevel(verboseLevel);
											}
											else {
												printlnCommand("\nVerbose level missing!");
											}
											break;
											
										default:
											printCommand("\nSET what? Available options: [verbose].");	
										}
									}
									printlnCommand("\nCurrent server settings: " + TestServer.serverPreferences.toString());
									break;
									
								case COMMAND_SHUTDOWN:
									isCommandLine = false;
									printLine();
									TestServer.shutdown();
									System.exit(0);
									break;
								case COMMAND_HELP:
									printHelp();
									break;
									
								case DEBUG_COMMAND_LIST_SERVICES:
									synchronized (TestServer.serviceManager.getServiceMap()) {
										Iterator<Entry<String, FutureService>> entries = TestServer.serviceManager.getServiceMap().entrySet().iterator();
										printlnCommand("\n" + TestServer.serviceManager.getServiceMap().size() + " services found:\n");
										int i = 0;
										while (entries.hasNext()) {
											Entry<String, FutureService> e = entries.next();
											printlnCommand("\t" + (++i) + ") " + e.getKey() + ":\n\t\t" + e.getValue().getService().toString());
										}										
									}
									break;
																		
								default:
									printCommand("\nUnknown command. Enter 'help' for more information.");
									printHelp();
								}
							}
							
							if (isCommandLine) {
								printCommand(PROMPT);
							}
						}
						catch (Exception e) {
							e.printStackTrace();
							break; //exit loop
						}
					}
				}
			}
		});
	}
	
	/**
	 * 
	 */
	public void printLine() {
		printlnCommand("\n---------------------------------------");
	}
	
	/**
	 * 
	 */
	public void printHelp() {
		printCommand("\nAvailable commands in this mode:"
				+ "\nhelp \t\t\t\t- shows this help"
				+ "\nshow tcp \t\t\t- shows tcp info"
				+ "\nshow udp \t\t\t- shows udp info"
				+ "\nshow info \t\t\t- shows qos test server info"
				+ "\nshow clients \t\t\t- shows all active/opened client handlers"
				+ "\nset \t\t\t\t- displays current test server settings"
				+ "\nset verbose [0] [1] [2] \t- set verbose level to 0, 1 or 2"
				+ "\nshutdown \t\t\t- testserver shutdown (alternative: CTRL+C)"
				+ "\nexit \t\t\t\t- returns to debug mode");
	}
	
	public void start() {
		if (TestServer.serverPreferences.isCommandConsoleEnabled()) {
			log("Command Console enabled. Starting KeyListener\n", 1, TestServerServiceEnum.TEST_SERVER);
			keyListener.start();
		}
		else {
			log("Command Console disabled. KeyListener not started\n", 1, TestServerServiceEnum.TEST_SERVER);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.io.PrintWriter#println(java.lang.String)
	 */
	@Override
	public void println(String s) {
		if (!isCommandLine) {
			print(s + "\n");
		}
	}
	
	/**
	 * 
	 * @param s
	 */
	public void printlnCommand(String s) {
		print(s + "\n");
	}
	
	/**
	 * 
	 * @param s
	 */
	public void printCommand(String s) {
		print(s);
	}
	
	/**
	 * 
	 * @param t
	 * @param verboseLevelNeeded
	 * @param service
	 */
	public static void error(String info, Throwable t, int verboseLevelNeeded, TestServerServiceEnum service) {
		LoggingService.error(t, info, service);
	}
	
	/**
	 * 
	 * @param info
	 * @param t
	 * @param verboseLevelNeeded
	 * @param service
	 */
	public static void errorReport(String errorReportKey, String info, Throwable t, int verboseLevelNeeded, TestServerServiceEnum service) {
		StringWriter stackTrace = new StringWriter();			
		t.printStackTrace(new PrintWriter(stackTrace));
		if (!errorReportMap.containsKey(errorReportKey)) {
			errorReportMap.putIfAbsent(errorReportKey, new ErrorReport(info + ": [" + t.getClass().getCanonicalName() + " - " + t.getMessage() +"]", new Date()));	
		}

		ErrorReport er = errorReportMap.get(errorReportKey);
		er.increaseCounter();
		
		LoggingService.fatal(t, info, service);
	}
	
	/**
	 * 
	 * @param msg
	 * @param verboseLevelNeeded
	 * @param service
	 */
	public static void log(String msg, int verboseLevelNeeded, TestServerServiceEnum service) {
		LoggingService.info(msg, service);
	}
		
	/**
	 * 
	 * @param date
	 * @return
	 */
	public static String getFormattedDate(Date date) {
		return DATE_FORMAT.format(date);
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getPrefix() {
		return TestServerConsole.getFormattedDate(new Date()) + " [T-" + Thread.currentThread().getId() +"]: ";
	}	
}
