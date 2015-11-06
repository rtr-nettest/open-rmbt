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
package at.alladin.rmbt.client.v2.task;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Locale;

import at.alladin.rmbt.client.AbstractRMBTTest;
import at.alladin.rmbt.client.QualityOfServiceTest;
import at.alladin.rmbt.client.v2.task.result.QoSTestResult;
import at.alladin.rmbt.client.v2.task.result.QoSTestResultEnum;
import at.alladin.rmbt.client.v2.task.service.TestProgressListener.TestProgressEvent;

/**
 * Superclass of all QoS tasks<br>
 * When creating a new task place a call to {@link AbstractQoSTask#onStart(QoSTestResult)} at the beginning of the test and a call to {@link AbstractQoSTask#onEnd(QoSTestResult)} at the absolute end of the test.<br>
 * @author lb
 *
 */
public abstract class AbstractQoSTask extends AbstractRMBTTest implements QoSTask {
	/**
	 * timeout to establish a control connection for a test
	 */
	public final static int CONTROL_CONNECTION_TIMEOUT = 10000;
	
	public final static String QOS_SERVER_PROTOCOL_VERSION = "QoSSP0.1";
	
	public final static String PARAM_QOS_TEST_OBJECTIVE_ID = "qos_test_uid";
	
	public final static String PARAM_QOS_TEST_OBJECTIVE_PORT = "server_port";
	
	public final static String PARAM_QOS_TEST_OBJECTIVE_ADDRESS = "server_addr";
	
	public final static String PARAM_QOS_CONCURRENCY_GROUP = "concurrency_group";
	
	public final static String PARAM_QOS_RESULT_START_TIME = "start_time_ns";
	
	public final static String PARAM_QOS_RESULT_END_TIME = "end_time_ns";
	
	public final static String PARAM_QOS_RESULT_DURATION_NS = "duration_ns";
		
	/**
	 * 
	 */
	private final int priority;
	
	private final int serverPort;
	
	private final int concurrencyGroup;
	
	private final String serverAddress;
	
	private final long qoSTestObjectiveUid;
	
	private long testStartTimestampNs;
	
	private long testEndTimestampNs;
	
	private boolean hasFinished = false;
	
	private boolean hasStarted = false;
	
	/**
	 * 
	 */
	protected final TaskDesc taskDesc;
	
	protected final QualityOfServiceTest qoSTest;
	
	protected final int id;
	
	protected QoSControlConnection controlConnection;
	
	/**
	 * this constructor set the priority to max 
	 * @param taskDesc
	 */
	public AbstractQoSTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId, int id) {
		this(nnTest, taskDesc, threadId, id, Integer.MAX_VALUE);
	}
	
	/**
	 * 
	 * @param taskDesc
	 * @param priority the higher the value, the higher the priority
	 */
	public AbstractQoSTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId, int id, int priority) {
		super(nnTest.getRMBTClient(), taskDesc, threadId);
		this.qoSTest = nnTest;
		this.taskDesc = taskDesc;
		this.priority = priority;
		this.id = id;
		
		String value = (String) taskDesc.getParams().get(PARAM_QOS_TEST_OBJECTIVE_ID);
		this.qoSTestObjectiveUid = value != null ? Long.valueOf(value) : null;

		value = (String) taskDesc.getParams().get(PARAM_QOS_TEST_OBJECTIVE_PORT);
		this.serverPort = value != null ? Integer.valueOf(value) : null;

		value = (String) taskDesc.getParams().get(PARAM_QOS_CONCURRENCY_GROUP);
		this.concurrencyGroup = value != null ? Integer.valueOf(value) : 0;

		value = (String) taskDesc.getParams().get(PARAM_QOS_TEST_OBJECTIVE_ADDRESS);
		this.serverAddress = value;

	}

	/**
	 * 
	 */
	public abstract void initTask();

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#getPriority()
	 */
	public int getPriority() {
		return priority;
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#getTestServerPort()
	 */
	public int getTestServerPort() {
		return serverPort;
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#getTestServerAddr()
	 */
	public String getTestServerAddr() {
		return serverAddress;
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#getQoSObjectiveTestId()
	 */
	public long getQoSObjectiveTestId() {
		return qoSTestObjectiveUid;
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#getConcurrencyGroup()
	 */
	public int getConcurrencyGroup() {
		return concurrencyGroup;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.QoSTask#getTaskDesc()
	 */
	public TaskDesc getTaskDesc() {
		return taskDesc;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(QoSTask o) {
		return (Integer.valueOf(priority).compareTo(Integer.valueOf(o.getPriority())));
	}
	
	/**
	 * 
	 * @param socket
	 * @param message
	 * @throws IOException 
	 */
	public void sendMessage(Socket socket, String message) throws IOException {
		FilterOutputStream fos = new FilterOutputStream(socket.getOutputStream());

		String send;
        send = String.format(Locale.US, message);        	

		fos.write(send.getBytes("US-ASCII"));
        fos.flush();
	}
	
	/**
	 * 
	 * @param socket
	 * @return
	 * @throws IOException
	 */
	public String readLine(Socket socket) throws IOException {
		FilterInputStream fis = new BufferedInputStream(socket.getInputStream());
        BufferedReader r = new BufferedReader(new InputStreamReader(fis, "US-ASCII"), 4096);
        return r.readLine();
	}
	
	/**
	 * 
	 * @param testType
	 * @return
	 */
	public QoSTestResult initQoSTestResult(QoSTestResultEnum testType) {
		QoSTestResult nnResult = new QoSTestResult(testType, this);
		nnResult.getResultMap().put(PARAM_QOS_TEST_OBJECTIVE_ID, qoSTestObjectiveUid);
		return nnResult;
	}
	
	/**
	 * 
	 * @return
	 */
	public QualityOfServiceTest getQoSTest() {
		return qoSTest;
	}
	
	/**
	 * 
	 */
	public void onStart(QoSTestResult result) {
		this.testStartTimestampNs = (System.nanoTime() - getQoSTest().getTestSettings().getStartTimeNs());
		this.hasStarted = true;
		result.getResultMap().put(PARAM_QOS_RESULT_START_TIME, this.testStartTimestampNs);
		getQoSTest().getTestSettings().dispatchTestProgressEvent(TestProgressEvent.ON_START, this);
	}
	
	/**
	 * 
	 */
	public void onEnd(QoSTestResult result) {
		this.testEndTimestampNs = (System.nanoTime() - getQoSTest().getTestSettings().getStartTimeNs());
		//result.getResultMap().put(PARAM_QOS_RESULT_END_TIME, this.testEndTimestampNs);
		result.getResultMap().put(PARAM_QOS_RESULT_DURATION_NS, (this.testEndTimestampNs - this.testStartTimestampNs));
		this.hasFinished = true;
		getQoSTest().getTestSettings().dispatchTestProgressEvent(TestProgressEvent.ON_END, this);
	}
	
	/**
	 * 
	 * @return
	 */
	public long getTestStartTimestampNs() {
		return this.testStartTimestampNs;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getTestEndTimestampNs() {
		return this.testEndTimestampNs;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean hasFinished() {
		return this.hasFinished;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean hasStarted() {
		return this.hasStarted; 
	}
	
	/**
	 * 
	 * @param timeStampNs
	 * @return
	 */
	public long getRelativeDurationNs(long timeStampNs) {
		return ((timeStampNs - getQoSTest().getTestSettings().getStartTimeNs()) - this.testStartTimestampNs);
	}

	/**
	 * 
	 * @return
	 */
	public QoSControlConnection getControlConnection() {
		return controlConnection;
	}

	/**
	 * 
	 * @param controlConnection
	 */
	public void setControlConnection(QoSControlConnection controlConnection) {
		this.controlConnection = controlConnection;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean hasConnectionError() {
		if (needsQoSControlConnection()) {
			return (getControlConnection() == null || getControlConnection().couldNotConnect.get());
		}
		
		return false;
	}

	/**
	 * 
	 * @param command
	 * @param listener
	 * @throws IOException 
	 */
	public void sendCommand(String command, ControlConnectionResponseCallback callback) throws IOException {
		controlConnection.sendTaskCommand(this, command, callback);
	}
}
