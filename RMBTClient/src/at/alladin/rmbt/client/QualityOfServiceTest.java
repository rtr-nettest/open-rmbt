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
package at.alladin.rmbt.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import at.alladin.rmbt.client.v2.task.AbstractQoSTask;
import at.alladin.rmbt.client.v2.task.DnsTask;
import at.alladin.rmbt.client.v2.task.HttpProxyTask;
import at.alladin.rmbt.client.v2.task.NonTransparentProxyTask;
import at.alladin.rmbt.client.v2.task.QoSControlConnection;
import at.alladin.rmbt.client.v2.task.QoSTestEnum;
import at.alladin.rmbt.client.v2.task.QoSTestErrorEnum;
import at.alladin.rmbt.client.v2.task.TaskDesc;
import at.alladin.rmbt.client.v2.task.TcpTask;
import at.alladin.rmbt.client.v2.task.TracerouteTask;
import at.alladin.rmbt.client.v2.task.UdpTask;
import at.alladin.rmbt.client.v2.task.VoipTask;
import at.alladin.rmbt.client.v2.task.WebsiteTask;
import at.alladin.rmbt.client.v2.task.result.QoSResultCollector;
import at.alladin.rmbt.client.v2.task.result.QoSTestResult;
import at.alladin.rmbt.client.v2.task.result.QoSTestResultEnum;
import at.alladin.rmbt.client.v2.task.service.TestProgressListener.TestProgressEvent;
import at.alladin.rmbt.client.v2.task.service.TestSettings;
import at.alladin.rmbt.client.v2.task.service.TrafficService;

/**
 * 
 * @author lb
 *
 */
public class QualityOfServiceTest implements Callable<QoSResultCollector> {

    private final RMBTClient client;
    
    private final AtomicInteger progress = new AtomicInteger(); 
    private final AtomicInteger testCount = new AtomicInteger();
    private final AtomicInteger concurrentGroupCount = new AtomicInteger();
    private final AtomicReference<QoSTestEnum> status = new AtomicReference<QoSTestEnum>();
    private final AtomicReference<QoSTestErrorEnum> errorStatus = new AtomicReference<QoSTestErrorEnum>(QoSTestErrorEnum.NONE);

    private final ExecutorService executor;
    private final ExecutorCompletionService<QoSTestResult> executorService;
    
    private final TestSettings qoSTestSettings;
    
    final TreeMap<Integer, List<AbstractQoSTask>> concurrentTasks = new TreeMap<Integer, List<AbstractQoSTask>>();
    final TreeMap<QoSTestResultEnum, List<AbstractQoSTask>> testMap = new TreeMap<QoSTestResultEnum, List<AbstractQoSTask>>();
    final TreeMap<String, QoSControlConnection> controlConnectionMap = new TreeMap<String, QoSControlConnection>();
    		
    private TreeMap<QoSTestResultEnum, Counter> testGroupCounterMap = new TreeMap<QoSTestResultEnum, Counter>();

    /**
     * 
     * @param client
     * @param websiteTestImpl
     * @param trafficService
     */
    public QualityOfServiceTest(RMBTClient client, TestSettings nnTestSettings) {
    	System.out.println("\n\n---- Initializing QoS Tests ----\n");
    	
    	this.client = client;
    	executor = Executors.newFixedThreadPool(client.getTaskDescList().size());
    	executorService = new ExecutorCompletionService<QoSTestResult>(executor);
    	status.set(QoSTestEnum.START);
    	testCount.set(client.getTaskDescList().size());
    	this.qoSTestSettings = nnTestSettings;
    	
		int threadCounter = 0;
		
		for (TaskDesc taskDesc : client.getTaskDescList()) {
			String taskId = (String) taskDesc.getParams().get(TaskDesc.QOS_TEST_IDENTIFIER_KEY);
			AbstractQoSTask test = null;
			
			if (RMBTClient.TASK_HTTP.equals(taskId)) {
	        	test = new HttpProxyTask(this, taskDesc, threadCounter++);
			}
			else if (RMBTClient.TASK_NON_TRANSPARENT_PROXY.equals(taskId)) {
	        	test = new NonTransparentProxyTask(this, taskDesc, threadCounter++);
			}
			else if (RMBTClient.TASK_DNS.equals(taskId)) {
	        	test = new DnsTask(this, taskDesc, threadCounter++);
			}
			else if (RMBTClient.TASK_TCP.equals(taskId)) {
	        	test = new TcpTask(this, taskDesc, threadCounter++);
			}
			else if (RMBTClient.TASK_UDP.equals(taskId)) {
	        	test = new UdpTask(this, taskDesc, threadCounter++);
			}
			else if (RMBTClient.TASK_VOIP.equals(taskId)) {
				test = new VoipTask(this, taskDesc, threadCounter++);
			}
			else if (RMBTClient.TASK_TRACEROUTE.equals(taskId)) {
				if (nnTestSettings != null && nnTestSettings.getTracerouteServiceClazz() != null) {
					test = new TracerouteTask(this, taskDesc, threadCounter++);	
				}
				else {
					System.out.println("No TracerouteService implementation: Skipping TracerouteTask: " + taskDesc);
				}
			}
			else if (RMBTClient.TASK_WEBSITE.equals(taskId)) {
				if (nnTestSettings != null && nnTestSettings.getWebsiteTestService() != null) {
					test = new WebsiteTask(this, taskDesc, threadCounter++);	
				}
				else {
					System.out.println("No WebsiteTestService implementation: Skipping WebsiteTask: " + taskDesc);
				}
			}
			
			if (test != null) {
				//manage taskMap:
				List<AbstractQoSTask> testList = null;
				testList = testMap.get(test.getTestType());
				if (testList == null) {
					testList = new ArrayList<AbstractQoSTask>();
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
				
				if (!controlConnectionMap.containsKey(test.getTestServerAddr())) {
					RMBTTestParameter params = new RMBTTestParameter(test.getTestServerAddr(), test.getTestServerPort(), 
									nnTestSettings.isUseSsl(), test.getTaskDesc().getToken(), 
									test.getTaskDesc().getDuration(), test.getTaskDesc().getNumThreads(),
									test.getTaskDesc().getNumPings(), test.getTaskDesc().getStartTime());
					controlConnectionMap.put(test.getTestServerAddr(), new QoSControlConnection(getRMBTClient(), params));
				}
				
				//check if qos test need test server
				if (test.needsQoSControlConnection()) {
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
		status.set(QoSTestEnum.QOS_RUNNING);
		QoSResultCollector result = new QoSResultCollector();
		
		final int testSize = testCount.get();
		
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
	        		}
	        		
				}
	    		catch (InterruptedException e) {
	    			executor.shutdownNow();
					e.printStackTrace();
					status.set(QoSTestEnum.ERROR);
					break;
	    		}
	    		catch (Exception e) {
					e.printStackTrace();
				}
	    		finally {
					progress.incrementAndGet();    			
	    		}
			}

			closeControlConnections(groupId);
		}
		
		if (status.get().equals(QoSTestEnum.ERROR)) {
			progress.set(testCount.get());
		}
		
    	if (trafficServiceStatus != TrafficService.SERVICE_NOT_SUPPORTED) {
    		qoSTestSettings.getTrafficService().stop();
    		System.out.println("TRAFFIC SERVICE: Tx Bytes = " + qoSTestSettings.getTrafficService().getTxBytes() 
    				+ ", Rx Bytes = " + qoSTestSettings.getTrafficService().getRxBytes());
    	}
    	
    	if (status.get() != QoSTestEnum.ERROR) {
    		status.set(QoSTestEnum.QOS_FINISHED);    	
    	}

    	if (executor != null)
            executor.shutdownNow();
    	
		return result;
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
    	this.status.set(newStatus);
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
    public Map<QoSTestResultEnum, Counter> getTestGroupCounterMap() {
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
    public TreeMap<QoSTestResultEnum, List<AbstractQoSTask>> getTestMap() {
    	return testMap;
    }
    
    /**
     * @return 
     * 
     */
    public synchronized void interrupt()
    {
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
    	public QoSTestResultEnum testType;
    	public int value;
    	public int target;
    	public int firstTest;
    	public int lastTest;

		public Counter(QoSTestResultEnum testType, int target, int concurrencyGroup) {
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