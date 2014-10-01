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
package at.alladin.rmbt.qos.testserver.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.TestServer;
import at.alladin.rmbt.qos.testserver.tcp.TcpServer;
import at.alladin.rmbt.qos.testserver.udp.UdpMultiClientServer;


public class TestServerConsole extends PrintStream {
	public final static String HINT_SHOW = "SHOW what? Available options: [tcp] [udp] [info]";
	
	public final static String COMMAND_EXIT_COMMAND_PROMPT = "exit";
	
	public final static String COMMAND_SHOW = "show";
	
	public final static String COMMAND_HELP = "help";
	
	public final static String COMMAND_SETTINGS = "set";
	
	public final static String COMMAND_SHUTDOWN = "shutdown";
	
	public final static String SUBCOMMAND_SETTINGS_SET_VERBOSE = "verbose";
	
	public final static String SUBCOMMAND_SHOW_INFO = "info";
	
	public final static String SUBCOMMAND_SHOW_OPENED_TCP_PORTS = "tcp";
	
	public final static String SUBCOMMAND_SHOW_OPENED_UDP_PORTS = "udp";
	
	public final static String SUBCOMMAND_FORCE = "force";
	
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

											printlnCommand("\nFound " + TestServer.tcpServerMap.size() + " active TCP sockets.");
											if (showTcp) {
												printlnCommand("\n\nList of all TCP sockets:\n");
												for (Entry<Integer, List<TcpServer>> e : TestServer.tcpServerMap.entrySet()) {
													for (TcpServer i : e.getValue()) {
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
													for (List<UdpMultiClientServer> udpServerList : TestServer.udpServerMap.values()) {
														for (UdpMultiClientServer udpServer : udpServerList) {
															if (!udpServer.getIncomingMap().isEmpty()) {
																printlnCommand("\nUDP Server Info: " + udpServer.toString());
															}
														}
													}
												}
												else if ("nodata".equals(commands[2])) {
													printlnCommand("\nMultiClient UDP Server wihtout ClientData:");
													for (List<UdpMultiClientServer> udpServerList : TestServer.udpServerMap.values()) {
														for (UdpMultiClientServer udpServer : udpServerList) {
															if (udpServer.getIncomingMap().isEmpty()) {
																printlnCommand("\nUDP Server Info: " + udpServer.toString());
															}
														}
													}													
												}
												else {
													try {
														List<UdpMultiClientServer> udpServerList = TestServer.udpServerMap.get(Integer.valueOf(commands[2]));
														for (UdpMultiClientServer udpServer : udpServerList) {
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
				+ "\nset \t\t\t\t- displays current test server settings"
				+ "\nset verbose [0] [1] [2] \t- set verbose level to 0, 1 or 2"
				+ "\nshutdown \t\t\t- testserver shutdown (alternative: CTRL+C)"
				+ "\nexit \t\t\t\t- returns to debug mode");
	}
	
	public void start() {
		if (TestServer.serverPreferences.isCommandConsoleEnabled()) {
			log("Command Console enabled. Starting KeyListener", 1, TestServerServiceEnum.TEST_SERVER);
			keyListener.start();
		}
		else {
			log("Command Console disabled. KeyListener not started", 1, TestServerServiceEnum.TEST_SERVER);
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
	 * @param msg
	 * @param verboseLevelNeeded
	 */
	public static void log(String msg, int verboseLevelNeeded) {
		if (TestServer.serverPreferences != null && TestServer.serverPreferences.getVerboseLevel() >= verboseLevelNeeded
				&& (TestServer.serverPreferences.isConsoleLog() || verboseLevelNeeded < 0)) {
			System.out.println(msg);
		}
	}
		
	/**
	 * 
	 * @param msg
	 * @param verboseLevelNeeded
	 * @param service
	 */
	public static void log(String msg, int verboseLevelNeeded, TestServerServiceEnum service) {
		String logFileName = TestServer.serverPreferences.getLogFileMap().get(service);
		if (logFileName == null) {
			logFileName = TestServer.serverPreferences.getLogFileMap().get(TestServerServiceEnum.TEST_SERVER);
		}
		
		log(msg, verboseLevelNeeded, logFileName);
	}
	
	/**
	 * 
	 * @param msg
	 * @param verboseLvelNeeded
	 * @param logFileName
	 */
	public static void log(String msg, int verboseLevelNeeded, String logFileName) {
		log(msg, verboseLevelNeeded);
		if (logFileName != null && (verboseLevelNeeded <= 0 
				|| (TestServer.serverPreferences != null && TestServer.serverPreferences.getVerboseLevel() >= verboseLevelNeeded))) {
			appendToLogFile(msg, logFileName + getLogFileSuffix());
		}
	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	public static String getFormattedDate(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(date);
	}
	
	/**
	 * 
	 * @param msg
	 * @param fileName
	 */
	public synchronized static void appendToLogFile(String msg, String fileName) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
		    out.println(TestServerConsole.getFormattedDate(new Date()) + ": " + msg);
		}catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private static String getLogFileSuffix() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		return "_" + df.format(new Date()) + ".log"; 
	}
}
