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

package at.rtr.rmbt.qos.testserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TestTcp {

	public static void main(String[] args) throws IOException {
		try(final ServerSocket serverSocket = new ServerSocket(3000)) {
			System.out.println("Listening on TCP 3000...");
			while (true) {
				final Socket s = serverSocket.accept();
				//new TcpHandler(s).runDis();
				new TcpHandler(s).runBr();
			}
		}
		catch (final Exception e) {
			e.printStackTrace();
		}		
	}
	
	public static class TcpHandler {
		
		final Socket socket;
		
		public TcpHandler(final Socket socket) {
			this.socket = socket;
		}
		
		public void runBr() {
			try (final BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
				long nulls = 0;
				while (true) {
					final String line = br.readLine();
					if (line == null) {
						nulls++;
					}
					else if (line != null) {
						System.out.println("TS: " + System.currentTimeMillis() + ", LINE: " + line + " (NULLS: " + nulls + ")");
					}
				}
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
		}
		
		public void runDis() {
			try (final DataInputStream dis = new DataInputStream(socket.getInputStream())) {
				long nulls = 0;
				while (true) {
					final byte[] buffer = new byte[1024];
					if (dis.read(buffer) > -1) {
						System.out.println("TS: " + System.currentTimeMillis() + ", LINE: " + new String(buffer) + " (NULLS: " + nulls + ")");						
					}
				}
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
		}

	}
}
