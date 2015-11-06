/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
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
package at.alladin.rmbt.client.v2.task;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.alladin.rmbt.client.AbstractRMBTTest;
import at.alladin.rmbt.client.RMBTClient;
import at.alladin.rmbt.client.RMBTTestParameter;

/**
 * 
 * @author lb
 *
 */
public class QoSControlConnection extends AbstractRMBTTest implements Runnable {
	
	public final static Pattern ID_REGEX_PATTERN = Pattern.compile("\\+ID([\\d]*)");
	
	public final AtomicBoolean isRunning = new AtomicBoolean(true);
	
	public final AtomicBoolean couldNotConnect = new AtomicBoolean(false);
	
	private final ConcurrentHashMap<Integer, ControlConnectionResponseCallbackHolder> requestMap = 
			new ConcurrentHashMap<Integer, ControlConnectionResponseCallbackHolder>();

	private final TreeSet<Integer> concurrencyGroupSet = new TreeSet<Integer>();
	
	protected Socket controlSocket;
	
	/**
	 * 
	 * @param client
	 * @param params
	 * @param threadId
	 */
	public QoSControlConnection(RMBTClient client, RMBTTestParameter params) {
		super(client, params, 1);
	}
	
	/**
	 * 
	 * @param qosTask
	 * @param command
	 * @param callback
	 * @throws IOException
	 */
	public void sendTaskCommand(AbstractQoSTask qosTask, String command, ControlConnectionResponseCallback callback) throws IOException {
		if (callback != null) {
			requestMap.put(qosTask.getId(), new ControlConnectionResponseCallbackHolder(command, callback));
		}
		sendMessage(command + " +ID" + qosTask.getId() + "\n");
	}

	public void run() {
		try {
			while (isRunning.get()) {
				final String response = reader.readLine();
				if (response != null) {
					final Matcher m = ID_REGEX_PATTERN.matcher(response);
					if (m.find()) {
						Integer id = Integer.valueOf(m.group(1));
						final ControlConnectionResponseCallbackHolder holder = requestMap.remove(id);
						if (holder != null && holder.getCallback() != null) {
							Runnable responseRunnable = new Runnable() {
								
								public void run() {
									holder.getCallback().onResponse(response, holder.getReqeust());
								}
							};
							
							RMBTClient.getCommonThreadPool().execute(responseRunnable);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			isRunning.set(false);
			if (controlSocket != null && !controlSocket.isClosed()) {
				try {
					controlSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
		}
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public void connect() throws Exception {
		isRunning.set(true);
		try {
			controlSocket = connect(null, InetAddress.getByName(params.getHost()), params.getPort(), 
				AbstractQoSTask.QOS_SERVER_PROTOCOL_VERSION, "ACCEPT", params.isEncryption(), AbstractQoSTask.CONTROL_CONNECTION_TIMEOUT);
		}
		catch (Exception e) {
			isRunning.set(false);
			couldNotConnect.set(true);
			throw e;
		}
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	public void close() throws IOException {
		sendMessage("QUIT\n");
		isRunning.set(false);
		controlSocket.close();
	}

	/**
	 * 
	 * @return
	 */
	public TreeSet<Integer> getConcurrencyGroupSet() {
		return concurrencyGroupSet;
	}
	
	/**
	 * 
	 * @author lb
	 *
	 */
	protected final class ControlConnectionResponseCallbackHolder {
		ControlConnectionResponseCallback callback;
		String reqeust;
		
		public ControlConnectionResponseCallbackHolder(String request, ControlConnectionResponseCallback callback) {
			this.reqeust = request;
			this.callback = callback;
		}
		
		public ControlConnectionResponseCallback getCallback() {
			return callback;
		}
		public void setCallback(ControlConnectionResponseCallback callback) {
			this.callback = callback;
		}
		public String getReqeust() {
			return reqeust;
		}
		public void setReqeust(String reqeust) {
			this.reqeust = reqeust;
		}
	}
}
