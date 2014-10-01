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
import java.util.Date;
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
	
	protected final ExecutorService executor;
	
	protected final ServerSocket socket;
	
	protected final SSLContext sslContext;
	
	/**
	 * 
	 * @param executor
	 * @param socket
	 */
	public QoSService(ExecutorService executor, ServerSocket socket, SSLContext sslContext) {
		this.executor = executor;
		this.socket = socket;
		this.sslContext = sslContext;
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
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}

				Socket client = socket.accept();
				
				/*
				 * not necessary anymore:

				if (sslContext != null) {
					SSLSocketFactory sslSf = sslContext.getSocketFactory();
					SSLSocket sslSocket = (SSLSocket) sslSf.createSocket(client, null, client.getPort(), false);
					
					executor.execute(new ClientHandler(socket, sslSocket));
				}
				else {
				*/
				if (TestServer.serverPreferences.getVerboseLevel() >= 2) {
					TestServerConsole.log((new Date()) + " -> " + socket, 2, TestServerServiceEnum.TEST_SERVER);
				}
				executor.execute(new ClientHandler(socket, client));
			}
		}
		catch (InterruptedException e) {
			TestServerConsole.log("QoSService interrupted. Shutting down.", 0, TestServerServiceEnum.TEST_SERVER);
		}
		catch (Exception e) {
			TestServerConsole.log("QoSService Exception. Shutting down.", 0, TestServerServiceEnum.TEST_SERVER);
			e.printStackTrace();
		}
		finally {
			if (!socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					TestServerConsole.log("IOException: Could not close ServerSocket.", 0, TestServerServiceEnum.TEST_SERVER);
					e.printStackTrace();
				}
			}
		}
	}
}
