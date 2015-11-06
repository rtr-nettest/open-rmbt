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
package at.alladin.rmbt.qos.testserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLContext;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.util.TestServerConsole;

/**
 * 
 * @author lb
 *
 */
public class QoSService implements Runnable {
	
	private final static String TAG = QoSService.class.getCanonicalName();
	
	protected final ExecutorService executor;
	
	protected final ServerSocket socket;
	
	protected final SSLContext sslContext;
	
	private final String name;
	
	/**
	 * 
	 * @param executor
	 * @param socket
	 */
	public QoSService(ExecutorService executor, ServerSocket socket, SSLContext sslContext) {
		this.executor = executor;
		this.socket = socket;
		this.sslContext = sslContext;
		this.name = "[QoSService " +  socket.getInetAddress() + ":" + socket.getLocalPort() +"]: ";
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		TestServerConsole.log("QoSService started on: " + socket + ". Awaiting connections...", -1, TestServerServiceEnum.TEST_SERVER);
		try {
			while (true) {
				try {
					if (Thread.interrupted() || socket.isClosed()) {
						throw new InterruptedException();
					}
	
					Socket client = socket.accept();					
					executor.execute(new ClientHandler(socket, client));
				}
				catch (Exception e) {
					if (e instanceof InterruptedException) {
						throw e;
					}
					TestServerConsole.error(name + "Exception. Trying to continue service:", e, 0, TestServerServiceEnum.TEST_SERVER);
				}
			}
		}
		catch (InterruptedException e) {
			TestServerConsole.log(name +"Interrupted! Shutting down!", 0, TestServerServiceEnum.TEST_SERVER);
		}
		catch (Exception e) {
			TestServerConsole.error(name +"Exception. Shutting down.", e, 0, TestServerServiceEnum.TEST_SERVER);
		}
		finally {
			if (socket != null && !socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					TestServerConsole.error(TAG, e, 0, TestServerServiceEnum.TEST_SERVER);
					e.printStackTrace();
				}
			}
		}
	}
}
