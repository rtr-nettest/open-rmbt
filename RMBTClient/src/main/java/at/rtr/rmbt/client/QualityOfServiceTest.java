/*******************************************************************************
 * Copyright 2013-2019 alladin-IT GmbH
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
// based on: https://raw.githubusercontent.com/alladin-IT/open-rmbt/master/RMBTClient/src/at/alladin/rmbt/client/QualityOfServiceTest.java
package at.rtr.rmbt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


import at.rtr.rmbt.client.qos.QoSMeasurementClientProgressListener;
import at.rtr.rmbt.shared.qos.QosMeasurementType;
import at.rtr.rmbt.client.v2.task.AbstractEchoProtocolTask;
import at.rtr.rmbt.client.v2.task.AbstractQoSTask;
import at.rtr.rmbt.client.v2.task.DnsTask;
import at.rtr.rmbt.client.v2.task.EchoProtocolTcpTask;
import at.rtr.rmbt.client.v2.task.EchoProtocolUdpTask;
import at.rtr.rmbt.client.v2.task.HttpProxyTask;
import at.rtr.rmbt.client.v2.task.NonTransparentProxyTask;
import at.rtr.rmbt.client.v2.task.QoSControlConnection;
import at.rtr.rmbt.client.v2.task.QoSTestEnum;
import at.rtr.rmbt.client.v2.task.QoSTestErrorEnum;
import at.rtr.rmbt.client.v2.task.SipTask;
import at.rtr.rmbt.client.v2.task.TaskDesc;
import at.rtr.rmbt.client.v2.task.TcpTask;
import at.rtr.rmbt.client.v2.task.TracerouteTask;
import at.rtr.rmbt.client.v2.task.UdpTask;
import at.rtr.rmbt.client.v2.task.VoipTask;
import at.rtr.rmbt.client.v2.task.WebsiteTask;
import at.rtr.rmbt.client.v2.task.result.QoSResultCollector;
import at.rtr.rmbt.client.v2.task.result.QoSTestResult;
import at.rtr.rmbt.client.v2.task.service.TestProgressListener.TestProgressEvent;
import at.rtr.rmbt.client.v2.task.service.TestSettings;
import at.rtr.rmbt.client.v2.task.service.TrafficService;

/**
 *
 * @author lb
 *
 */
public class QualityOfServiceTest implements Callable<QoSResultCollector> {

	public final static String TASK_UDP = "udp";
	public final static String TASK_TCP = "tcp";
	public final static String TASK_DNS = "dns";
	public final static String TASK_VOIP = "voip";
	public final static String TASK_NON_TRANSPARENT_PROXY = "non_transparent_proxy";
	public final static String TASK_HTTP = "http_proxy";
	public final static String TASK_WEBSITE = "website";
	public final static String TASK_TRACEROUTE = "traceroute";
	public final static String TASK_ECHO_PROTOCOL = "echo_protocol";
	public final static String TASK_SIP = "sip";
	public final static String TASK_MKIT_WEB_CONNECTIVITY = "mkit_web_connectivity";
	public final static String TASK_MKIT_DASH = "mkit_dash";

	private final RMBTClient client;

	private final AtomicInteger progress = new AtomicInteger();
	private final AtomicInteger testCount = new AtomicInteger();
	private final AtomicInteger concurrentGroupCount = new AtomicInteger();
	private final AtomicReference<QoSTestEnum> status = new AtomicReference<>();
	private final AtomicReference<QoSTestErrorEnum> errorStatus = new AtomicReference<>(QoSTestErrorEnum.NONE);

	private final ExecutorService executor;
	private final ExecutorCompletionService<QoSTestResult> executorService;

	private final TestSettings qoSTestSettings;

	final TreeMap<Integer, List<AbstractQoSTask>> concurrentTasks = new TreeMap<>();
	final TreeMap<QosMeasurementType, List<AbstractQoSTask>> testMap = new TreeMap<>();
	final TreeMap<String, QoSControlConnection> controlConnectionMap = new TreeMap<>();

	private TreeMap<QosMeasurementType, Counter> testGroupCounterMap = new TreeMap<>();

	private final List<QoSMeasurementClientProgressListener> progressListeners = new ArrayList<>();

	private final ConcurrentMap<QosMeasurementType, Integer> qosTypeTaskCountMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<QosMeasurementType, Integer> qosTypeDoneCountMap = new ConcurrentHashMap<>();
	//Contains the progress of the currently executed qosTests [0, 1]
	private final ConcurrentMap<QosMeasurementType, Float> qosTypeTestProgressMap = new ConcurrentHashMap<>();

	//provide a list of the test ids to the listeners out there
	private final List<String> taskIdList = new ArrayList<>();

	public QualityOfServiceTest(RMBTClient client, TestSettings nnTestSettings) {
		this(client, nnTestSettings, new ArrayList<QoSMeasurementClientProgressListener>());
	}

	/**
	 *
	 * @param client
	 */
	public QualityOfServiceTest(RMBTClient client, TestSettings nnTestSettings, List<QoSMeasurementClientProgressListener> listeners) {
		System.out.println("\n\n---- Initializing QoS Tests ----\n");

		if (listeners != null) {
			this.progressListeners.addAll(listeners);
		}

		this.client = client;
		client.createSSLSocketFactory();

		executor = Executors.newFixedThreadPool(client.getTaskDescList().size());
		executorService = new ExecutorCompletionService<>(executor);
		updateQoSStatus(QoSTestEnum.START);
		testCount.set(client.getTaskDescList().size());

		this.qoSTestSettings = nnTestSettings;

		int threadCounter = 0;

		for (TaskDesc taskDesc : client.getTaskDescList()) {
			String taskId = (String) taskDesc.getParams().get(TaskDesc.QOS_TEST_IDENTIFIER_KEY);
			taskIdList.add(taskId);
			AbstractQoSTask test = null;

			//for progress by qosType, count the # of tests in each group
			try {
				final QosMeasurementType t = QosMeasurementType.fromValue(taskId);
				if (!qosTypeTaskCountMap.containsKey(t)) {
					qosTypeTaskCountMap.put(t, 1);
					qosTypeTestProgressMap.put(t, 0f);
				} else {
					qosTypeTaskCountMap.put(t, qosTypeTaskCountMap.get(t) + 1);
				}
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			}

			if (TASK_HTTP.equals(taskId)) {
				test = new HttpProxyTask(this, taskDesc, threadCounter++);
			}
			else if (TASK_NON_TRANSPARENT_PROXY.equals(taskId)) {
				test = new NonTransparentProxyTask(this, taskDesc, threadCounter++);
			}
			else if (TASK_DNS.equals(taskId)) {
			    /*  if there is a provided dnsserveraddress from the settings, use it! (unless a dns resolver was already specified in the taskDescription)
                    Necessary for android devices with SDK > 26, as the usual way of obtaining the DNS server (via the dns library) doesn't work for them
                */
				if (qoSTestSettings.getDnsServerAddressList() != null && qoSTestSettings.getDnsServerAddressList().size() > 0 && !taskDesc.getParams().containsKey(DnsTask.PARAM_DNS_RESOLVER)) {
					taskDesc.getParams().put(DnsTask.PARAM_DNS_RESOLVER, qoSTestSettings.getDnsServerAddressList().get(0).getHostAddress());
				}
				test = new DnsTask(this, taskDesc, threadCounter++);
			}
			else if (TASK_TCP.equals(taskId)) {
				test = new TcpTask(this, taskDesc, threadCounter++);
			}
			else if (TASK_UDP.equals(taskId)) {
				test = new UdpTask(this, taskDesc, threadCounter++);
			}
			else if (TASK_VOIP.equals(taskId)) {
				test = new VoipTask(this, taskDesc, threadCounter++);
			}
			else if (TASK_TRACEROUTE.equals(taskId)) {
				if (nnTestSettings != null && nnTestSettings.getTracerouteServiceClazz() != null) {
					test = new TracerouteTask(this, taskDesc, threadCounter++);
				}
				else {
					System.out.println("No TracerouteService implementation: Skipping TracerouteTask: " + taskDesc);
				}
			}
			if (RMBTClient.TASK_TRACEROUTE_MASKED.equals(taskId)) {
				final boolean TraceRouteMaskedAvailable = true; // enable service
				if (TraceRouteMaskedAvailable && nnTestSettings != null && nnTestSettings.getTracerouteServiceClazz() != null) {
					test = new TracerouteTask(this, taskDesc, threadCounter++,true);
				}
				else {
					System.out.println("No TracerouteMaskedService implementation: Skipping TracerouteMaskedTask: " + taskDesc);
				}
			}
			else if (TASK_WEBSITE.equals(taskId)) {
				if (nnTestSettings != null && nnTestSettings.getWebsiteTestService() != null) {
					test = new WebsiteTask(this, taskDesc, threadCounter++);
				}
				else {
					System.out.println("No WebsiteTestService implementation: Skipping WebsiteTask: " + taskDesc);
				}
			}
			else if (TASK_ECHO_PROTOCOL.equals(taskId)) {
				if (taskDesc.getParams().get(AbstractEchoProtocolTask.PROTOCOL) != null) {
					final String protocol = (String) taskDesc.getParams().get(AbstractEchoProtocolTask.PROTOCOL);
					if (AbstractEchoProtocolTask.PROTOCOL_TCP.equals(protocol)) {
						test = new EchoProtocolTcpTask(this, taskDesc, threadCounter++);
					} else if (AbstractEchoProtocolTask.PROTOCOL_UDP.equals(protocol)) {
						test = new EchoProtocolUdpTask(this, taskDesc, threadCounter++);
					} else {
						System.out.println("Protocol for EchoProtocol unknown. Use either: " + AbstractEchoProtocolTask.PROTOCOL_UDP + " or " + AbstractEchoProtocolTask.PROTOCOL_TCP);
					}
				} else {
					System.out.println("No protocol specified for the EchoProtocol test. Skipping " + taskDesc);
				}
			}
			/*else if (TASK_MKIT_WEB_CONNECTIVITY.equals(taskId)) {
				test = new MkitTask(this, taskId, taskDesc, threadCounter++);
			}
			else if (TASK_MKIT_DASH.equals(taskId)) {
				test = new MkitTask(this, taskId, taskDesc, threadCounter++);
			}*/
			else if (TASK_SIP.equals(taskId)) {
				test = new SipTask(this, taskDesc, threadCounter++);
			}

			if (test != null) {
				//manage taskMap:
				List<AbstractQoSTask> testList = null;
				testList = testMap.get(test.getTestType());
				if (testList == null) {
					testList = new ArrayList<>();
					testMap.put(test.getTestType(), testList);
				}
				testList.add(test);

				Counter testTypeCounter;

				if (testGroupCounterMap.containsKey(test.getTestType())) {
					testTypeCounter = testGroupCounterMap.get(test.getTestType());
					testTypeCounter.increaseCounter(test.getConcurrencyGroup());
				}
				else {
					testTypeCounter = new Counter(test.getTestType(), 1, test.getConcurrencyGroup());
					testGroupCounterMap.put(test.getTestType(), testTypeCounter);
				}

				//manage concurrent test groups
				List<AbstractQoSTask> tasks = null;

				if (concurrentTasks.containsKey(test.getConcurrencyGroup())) {
					tasks = concurrentTasks.get(test.getConcurrencyGroup());
				}
				else {
					tasks = new ArrayList<AbstractQoSTask>();
					concurrentTasks.put(test.getConcurrencyGroup(), tasks);
				}

				if (tasks != null) {
					tasks.add(test);
				}

				//check if qos test needs test server
				if (test.needsQoSControlConnection()) {

					if (!controlConnectionMap.containsKey(test.getTestServerAddr())) {
						RMBTTestParameter params = new RMBTTestParameter(test.getTestServerAddr(), test.getTestServerPort(),
								nnTestSettings.isUseSsl(), test.getTaskDesc().getToken(),
								test.getTaskDesc().getDuration(), test.getTaskDesc().getNumThreads(),
								test.getTaskDesc().getNumPings(), test.getTaskDesc().getStartTime());
						controlConnectionMap.put(test.getTestServerAddr(), new QoSControlConnection(getRMBTClient(), params));
					}

					test.setControlConnection(controlConnectionMap.get(test.getTestServerAddr()));
					controlConnectionMap.get(test.getTestServerAddr()).getConcurrencyGroupSet().add(test.getConcurrencyGroup());

				}
			}
		}

		if (qoSTestSettings != null) {
			qoSTestSettings.dispatchTestProgressEvent(TestProgressEvent.ON_CREATED, null, this);
		}
	}

	/**
	 *
	 */
	public QoSResultCollector call() throws Exception {
		//set the
		qoSTestSettings.setStartTimeNs(System.nanoTime());
		final int testSize = testCount.get();
		for (QoSMeasurementClientProgressListener l : progressListeners) {
			l.onQoSTestsDefined(testSize);
		}

		updateQoSStatus(QoSTestEnum.QOS_RUNNING);
		QoSResultCollector result = new QoSResultCollector();


		int trafficServiceStatus = TrafficService.SERVICE_NOT_SUPPORTED;

		if (qoSTestSettings != null && qoSTestSettings.getTrafficService() != null) {
			trafficServiceStatus = qoSTestSettings.getTrafficService().start();
		}

		Iterator<Integer> groupIterator = concurrentTasks.keySet().iterator();
		while (groupIterator.hasNext() && !status.get().equals(QoSTestEnum.ERROR)) {
			final int groupId = groupIterator.next();
			concurrentGroupCount.set(groupId);

			//check if a qos control server connection needs to be initialized:
			openControlConnections(groupId);

			if (status.get().equals(QoSTestEnum.ERROR)) {
				break;
			}

			List<AbstractQoSTask> tasks = concurrentTasks.get(groupId);
			for (AbstractQoSTask task : tasks) {
				//if the test is the first one of its kind, fire the group started event
				final String typeString = (String) task.getTaskDesc().getParams().get(TaskDesc.QOS_TEST_IDENTIFIER_KEY);
				if (typeString != null) {
					try {
						QosMeasurementType type = QosMeasurementType.fromValue(typeString);
						if (!qosTypeDoneCountMap.containsKey(type)) {
							qosTypeDoneCountMap.put(type, 0);
							for (QoSMeasurementClientProgressListener l : progressListeners) {
								l.onQoSTypeStarted(type);
							}
						}
						//TODO: write config which types should listen themselves
						if (type == QosMeasurementType.MKIT_DASH || type == QosMeasurementType.MKIT_WEB_CONNECTIVITY) {
							task.setQoSTestProgressListener(new AbstractQoSTask.QoSTestProgressListener() {
								@Override
								public void onProgress(float currentTestProgress, QosMeasurementType resultType) {
									final QosMeasurementType type = QosMeasurementType.fromValue(resultType.toString().toLowerCase());
									QualityOfServiceTest.this.qosTypeTestProgressMap.put(type, currentTestProgress);
									final float totalProgress = calculateMeasurementTypeProgress(type) + currentTestProgress / (float) qosTypeTaskCountMap.get(type);
									for (QoSMeasurementClientProgressListener l : progressListeners) {
										l.onQoSTypeProgress(type, totalProgress);
									}
								}
							});
						}
					} catch (IllegalArgumentException ex) {
						ex.printStackTrace();
					}
				}
				//and submit the task
				executorService.submit(task);
			}

			for (int i = 0; i < tasks.size(); i++) {
				try {
					Future<QoSTestResult> testResult = executorService.take();
					if (testResult!=null) {
						QoSTestResult curResult = testResult.get();

						if (curResult.isFatalError()) {
							throw new InterruptedException("interrupted due to test fatal error: " + curResult.toString());
						}

						if (!curResult.getQosTask().hasConnectionError()) {
							result.getResults().add(curResult);
						}
						else {
							System.out.println("test: " + curResult.getTestType().name() + " failed. Could not connect to QoSControlServer.");
						}
						System.out.println("test " + curResult.getTestType().name() + " finished (" + (progress.get() + 1) + " out of " +
								testSize + ", CONCURRENCY GROUP=" + groupId + ")");
						Counter testTypeCounter = testGroupCounterMap.get(curResult.getTestType());
						if (testTypeCounter != null) {
							testTypeCounter.value++;
						}

						//Provide progress info to listeners
						try {
							final QosMeasurementType type = QosMeasurementType.fromValue(curResult.getTestType().toString().toLowerCase());
							qosTypeDoneCountMap.put(type, qosTypeDoneCountMap.get(type) + 1);	//this is safe, as we previously init map w/0 on the first test of each type
							final float prog = calculateMeasurementTypeProgress(type);
							for (QoSMeasurementClientProgressListener l : progressListeners) {
								l.onQoSTypeProgress(type, prog);
							}
							if (prog >= 1) {
								for (QoSMeasurementClientProgressListener l : progressListeners) {
									l.onQoSTypeFinished(type);
								}
							}
						} catch (IllegalArgumentException ex) {
							ex.printStackTrace();
						}
					}

				}
				catch (InterruptedException e) {
					executor.shutdownNow();
					e.printStackTrace();
					updateQoSStatus(QoSTestEnum.ERROR);
					break;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					progress.incrementAndGet();
					for (QoSMeasurementClientProgressListener l : progressListeners) {
						l.onProgress(progress.get() / (float) testCount.get());
					}
				}
			}

			closeControlConnections(groupId);
		}

		if (status.get().equals(QoSTestEnum.ERROR)) {
			progress.set(testCount.get());
		}

		for (QoSMeasurementClientProgressListener l : progressListeners) {
			l.onProgress(progress.get() / (float) testCount.get());
		}

		if (trafficServiceStatus != TrafficService.SERVICE_NOT_SUPPORTED) {
			qoSTestSettings.getTrafficService().stop();
			System.out.println("TRAFFIC SERVICE: Tx Bytes = " + qoSTestSettings.getTrafficService().getTxBytes()
					+ ", Rx Bytes = " + qoSTestSettings.getTrafficService().getRxBytes());
		}

		if (status.get() != QoSTestEnum.ERROR) {
			updateQoSStatus(QoSTestEnum.QOS_FINISHED);
		}

		if (executor != null) {
			executor.shutdownNow();
		}
		return result;
	}

	private void updateQoSStatus(QoSTestEnum status) {
		this.status.set(status);
		for (QoSMeasurementClientProgressListener l : progressListeners) {
			l.onQoSStatusChanged(status);
		}
	}

	public void addQoSProgressListener(QoSMeasurementClientProgressListener... listeners) {
		Collections.addAll(progressListeners, listeners);
	}

	public void removeQoSProgressListener(QoSMeasurementClientProgressListener... listeners) {
		for (int i = 0; i < listeners.length; i++) {
			progressListeners.remove(listeners[i]);
		}
	}

	private float calculateMeasurementTypeProgress(final QosMeasurementType type) {
		return qosTypeDoneCountMap.get(type) / (float) qosTypeTaskCountMap.get(type);
	}

	/**
	 *
	 * @return
	 */
	public int getProgress() {
		final int progress = this.progress.get();
		return progress;
	}

	/**
	 *
	 * @return
	 */
	public int getTestCount() {
		return testCount.get();
	}

	/**
	 *
	 * @return total progress (0..1) of this qos test
	 */
	public float getTotalProgress() {
		return (float) progress.get() / (float) testCount.get();
	}

	/**
	 *
	 * @return
	 */
	public int getTestSize() {
		final int testSize = this.testCount.get();
		return testSize;
	}

	/**
	 *
	 * @return
	 */
	public QoSTestEnum getStatus() {
		final QoSTestEnum status = this.status.get();
		return status;
	}

	/**
	 *
	 * @param newStatus
	 */
	public void setStatus(QoSTestEnum newStatus) {
		updateQoSStatus(newStatus);
	}

	/**
	 *
	 * @return
	 */
	public QoSTestErrorEnum getErrorStatus() {
		final QoSTestErrorEnum status = this.errorStatus.get();
		return status;
	}

	/**
	 *
	 * @param newStatus
	 */
	public void setErrorStatus(QoSTestErrorEnum newStatus) {
		this.errorStatus.set(newStatus);
	}

	/**
	 *
	 * @return
	 */
	public int getCurrentConcurrentGroup() {
		final int currentGroupCount = this.concurrentGroupCount.get();
		return currentGroupCount;
	}

	/**
	 *
	 * @return
	 */
	public Map<QosMeasurementType, Counter> getTestGroupCounterMap() {
		return testGroupCounterMap;
	}

	/**
	 *
	 * @return
	 */
	public TestSettings getTestSettings() {
		return qoSTestSettings;
	}

	/**
	 *
	 * @return
	 */
	public RMBTClient getRMBTClient() {
		return client;
	}

	/**
	 *
	 * @return
	 */
	public Map<QosMeasurementType, Integer> getQosTypeTaskCountMap() {
		return qosTypeTaskCountMap;
	}

	/**
	 *
	 * @return
	 */
	public Map<QosMeasurementType, Integer> getQosTypeDoneCountMap() {
		return qosTypeDoneCountMap;
	}

	public ConcurrentMap<QosMeasurementType, Float> getQosTypeTestProgressMap() {
		return qosTypeTestProgressMap;
	}

	/**
	 *
	 * @return
	 */
	public TreeMap<QosMeasurementType, List<AbstractQoSTask>> getTestMap() {
		return testMap;
	}

	/**
	 * @return
	 *
	 */
	public synchronized void interrupt()
	{
		//let the tests themselves interrupt what they're doing
		for (Integer key : concurrentTasks.keySet()) {
			List<AbstractQoSTask> tasks = concurrentTasks.get(key);
			for (AbstractQoSTask t : tasks) {
				t.interrupt();
			}
		}
		if (executor != null)
			executor.shutdownNow();
	}

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		if (executor != null)
			executor.shutdownNow();
	}

	/**
	 *
	 * @author lb
	 *
	 */
	public final class Counter {
		public QosMeasurementType testType;
		public int value;
		public int target;
		public int firstTest;
		public int lastTest;

		public Counter(QosMeasurementType testType, int target, int concurrencyGroup) {
			this.testType = testType;
			this.value = 0;
			this.target = target;
			this.firstTest = concurrencyGroup;
			this.lastTest = concurrencyGroup;
		}

		public void increaseCounter(int concurrencyGroup) {
			this.target++;
			lastTest = concurrencyGroup > lastTest ? concurrencyGroup : lastTest;
			firstTest = concurrencyGroup < firstTest ? concurrencyGroup : firstTest;
		}

		@Override
		public String toString() {
			return "Counter [testType=" + testType + ", value=" + value
					+ ", target=" + target + ", firstTest=" + firstTest
					+ ", lastTest=" + lastTest + "]";
		}
	}

	private void openControlConnections(int concurrencyGroup) {
		manageControlConnections(concurrencyGroup, true);
	}

	private void closeControlConnections(int concurrencyGroup) {
		manageControlConnections(concurrencyGroup, false);
	}

	private void manageControlConnections(int concurrencyGroup, boolean openAll) {
		Iterator<QoSControlConnection> iterator = controlConnectionMap.values().iterator();
		while (iterator.hasNext()) {
			final QoSControlConnection controlConnection = iterator.next();

			try {
				if (controlConnection.getConcurrencyGroupSet().size() > 0) {
					if (openAll) {
						if (controlConnection.getConcurrencyGroupSet().first() == concurrencyGroup) {
							controlConnection.connect();
							RMBTClient.getCommonThreadPool().execute(controlConnection);
						}
					}
					else {
						if (controlConnection.getConcurrencyGroupSet().last() == concurrencyGroup) {
							controlConnection.close();
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
//    			executor.shutdownNow();
//				status.set(QoSTestEnum.ERROR);
//				break;
			}
		}
	}
}
