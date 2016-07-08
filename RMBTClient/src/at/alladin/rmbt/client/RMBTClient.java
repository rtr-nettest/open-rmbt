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
package at.alladin.rmbt.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.json.JSONObject;

import at.alladin.rmbt.client.RMBTTest.CurrentSpeed;
import at.alladin.rmbt.client.helper.Config;
import at.alladin.rmbt.client.helper.ControlServerConnection;
import at.alladin.rmbt.client.helper.IntermediateResult;
import at.alladin.rmbt.client.helper.RMBTOutputCallback;
import at.alladin.rmbt.client.helper.TestStatus;
import at.alladin.rmbt.client.v2.task.TaskDesc;
import at.alladin.rmbt.client.v2.task.result.QoSResultCollector;
import at.alladin.rmbt.client.v2.task.service.TestMeasurement;
import at.alladin.rmbt.client.v2.task.service.TrafficService;
import at.alladin.rmbt.util.model.shared.exception.ErrorStatus;

public class RMBTClient
{
    private static final ExecutorService COMMON_THREAD_POOL = Executors.newCachedThreadPool();
    
    private final RMBTTestParameter params;
    
    private final long durationInitNano = 2500000000L; // TODO
    private final long durationUpNano;
    private final long durationDownNano;
    
    private final AtomicLong pingNano = new AtomicLong(-1);
    private final AtomicLong downBitPerSec = new AtomicLong(-1);
    private final AtomicLong upBitPerSec = new AtomicLong(-1);
    
    /* ping status */
    private final AtomicLong pingTsStart = new AtomicLong(-1);
    private final AtomicInteger pingNumDome = new AtomicInteger(-1);
    private final AtomicLong pingTsLastPing = new AtomicLong(-1);
    
    private final static long MIN_DIFF_TIME = 100000000; // 100 ms
    
    private final static int KEEP_LAST_ENTRIES = 20;
    private int lastCounter;
    private final long[][] lastTransfer;
    private final long[][] lastTime;
    
    private final ExecutorService testThreadPool;
    
    private final RMBTTest[] testTasks;
    
    private TotalTestResult result;
    
    private SSLSocketFactory sslSocketFactory;
    
    private RMBTOutputCallback outputCallback;
    private final boolean outputToStdout = true;
    
    private final ControlServerConnection controlConnection;
    
    private final AtomicBoolean aborted = new AtomicBoolean();
    
    private String errorMsg = "";
    
    
    /*------------------------------------
    	V2 tests
    --------------------------------------*/
    
    public final static String TASK_UDP = "udp";
    public final static String TASK_TCP = "tcp";
    public final static String TASK_DNS = "dns";
    public final static String TASK_VOIP = "voip";
    public final static String TASK_NON_TRANSPARENT_PROXY = "non_transparent_proxy";
    public final static String TASK_HTTP = "http_proxy";
    public final static String TASK_WEBSITE = "website";
    public final static String TASK_TRACEROUTE = "traceroute";
    
    private List<TaskDesc> taskDescList;

    /*------------------------------------*/
    
    private final AtomicReference<TestStatus> testStatus = new AtomicReference<TestStatus>(TestStatus.WAIT);
    private final AtomicReference<TestStatus> statusBeforeError = new AtomicReference<TestStatus>(null);
    private final AtomicLong statusChangeTime = new AtomicLong();
    
    private TrafficService trafficService;
    
    public static ExecutorService getCommonThreadPool()
    {
        return COMMON_THREAD_POOL;
    }
    
    private ConcurrentHashMap<TestStatus, TestMeasurement> measurementMap = new ConcurrentHashMap<TestStatus, TestMeasurement>();

    public static RMBTClient getInstance(final String host, final String pathPrefix, final int port,
            final boolean encryption, final ArrayList<String> geoInfo, final String uuid, final String clientType,
            final String clientName, final String clientVersion, final RMBTTestParameter overrideParams,
            final JSONObject additionalValues) {
    	return getInstance(host, pathPrefix, port, encryption, geoInfo, uuid, clientType, 
    			clientName, clientVersion, overrideParams, additionalValues, null);
    }
    
    public static RMBTClient getInstance(final String host, final String pathPrefix, final int port,
            final boolean encryption, final ArrayList<String> geoInfo, final String uuid, final String clientType,
            final String clientName, final String clientVersion, final RMBTTestParameter overrideParams,
            final JSONObject additionalValues, final Set<ErrorStatus> errorSet)
    {
        final ControlServerConnection controlConnection = new ControlServerConnection();
        
        final String error = controlConnection.requestNewTestConnection(host, pathPrefix, port, encryption, geoInfo,
                uuid, clientType, clientName, clientVersion, additionalValues);
        
        if (controlConnection.getLastErrorList() != null && errorSet != null) {
        	errorSet.addAll(controlConnection.getLastErrorList());
        }
        
        if (error != null)
        {
            System.out.println(error);
            return null;
        }

        //TODO: simple and fast solution; make it better
        final String errorNewTest = controlConnection.requestQoSTestParameters(host, pathPrefix, port, encryption, geoInfo,
                uuid, clientType, clientName, clientVersion, additionalValues);
        
        if (errorNewTest != null)
        {
            System.out.println(errorNewTest);
            return null;
        }
        
        final RMBTTestParameter params = controlConnection.getTestParameter(overrideParams);
        
        return new RMBTClient(params, controlConnection);
    }
    
    public static RMBTClient getInstance(final RMBTTestParameter params)
    {
        return new RMBTClient(params, null);
    }
    
    RMBTClient(final RMBTTestParameter params, final ControlServerConnection controlConnection)
    {
        this.params = params;
        this.controlConnection = controlConnection;
        
        params.check();
        
        if (params.getNumThreads() > 0)
        {
            testThreadPool = Executors.newFixedThreadPool(params.getNumThreads());
            testTasks = new RMBTTest[params.getNumThreads()];
        }
        else
        {
            testThreadPool = null;
            testTasks = null;
        }
        
        durationDownNano = params.getDuration() * 1000000000L;
        durationUpNano = params.getDuration() * 1000000000L;
        
        lastTransfer = new long[params.getNumThreads()][KEEP_LAST_ENTRIES];
        lastTime = new long[params.getNumThreads()][KEEP_LAST_ENTRIES];
        
        if (controlConnection != null)
            this.taskDescList = controlConnection.v2TaskDesc;
        //if (params.isEncryption())
        //    sslSocketFactory = createSSLSocketFactory();

    }

    public void setTrafficService(TrafficService trafficService) {
    	this.trafficService = trafficService;
    }
    
    public TrafficService getTrafficService() {
    	return this.trafficService;
    }
    
    private SSLSocketFactory createSSLSocketFactory()
    {
        log("initSSL...");
        try
        {
            final SSLContext sc = getSSLContext(null, null);
            
            final SSLSocketFactory factory = sc.getSocketFactory();
            
            return factory;
        }
        catch (final Exception e)
        {
            setErrorStatus();
            log(e);
        }
        return null;
    }
    
    public static TrustManager getTrustingManager()
    {
        return new javax.net.ssl.X509TrustManager()
        {
            public X509Certificate[] getAcceptedIssuers()
            {
                return new X509Certificate[] {};
            }
            
            public void checkClientTrusted(final X509Certificate[] certs, final String authType)
                    throws CertificateException
            {
                // System.out.println("[TRUSTING] checkClientTrusted: " +
                // Arrays.toString(certs) + " - " + authType);
            }
            
            public void checkServerTrusted(final X509Certificate[] certs, final String authType)
                    throws CertificateException
            {
                // System.out.println("[TRUSTING] checkServerTrusted: " +
                // Arrays.toString(certs) + " - " + authType);
            }
        };
    }
    
    public static SSLContext getSSLContext(final String caResource, final String certResource)
            throws NoSuchAlgorithmException, KeyManagementException
    {
        X509Certificate _ca = null;
        try
        {
            if (caResource != null)
            {
                final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                _ca = (X509Certificate) cf.generateCertificate(RMBTClient.class.getClassLoader().getResourceAsStream(
                        caResource));
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        
        final X509Certificate ca = _ca;
        
        X509Certificate _cert = null;
        try
        {
            if (certResource != null)
            {
                final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                _cert = (X509Certificate) cf.generateCertificate(RMBTClient.class.getClassLoader().getResourceAsStream(
                        certResource));
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        final X509Certificate cert = _cert;
        
        // TrustManagerFactory tmf = null;
        // try
        // {
        // if (cert != null)
        // {
        // final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        // ks.load(null, null);
        // ks.setCertificateEntry("crt", cert);
        //
        // tmf =
        // TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        // tmf.init(ks);
        // }
        // }
        // catch (Exception e)
        // {
        // e.printStackTrace();
        // }
        
        final TrustManager tm;
        if (cert == null)
            tm = getTrustingManager();
        else
            tm = new javax.net.ssl.X509TrustManager()
            {
                public X509Certificate[] getAcceptedIssuers()
                {
                    // System.out.println("getAcceptedIssuers");
                    if (ca == null)
                        return new X509Certificate[] { cert };
                    else
                        return new X509Certificate[] { ca };
                }
                
                public void checkClientTrusted(final X509Certificate[] certs, final String authType)
                        throws CertificateException
                {
                    // System.out.println("checkClientTrusted: " +
                    // Arrays.toString(certs) + " - " + authType);
                }
                
                public void checkServerTrusted(final X509Certificate[] certs, final String authType)
                        throws CertificateException
                {
                    // System.out.println("checkServerTrusted: " +
                    // Arrays.toString(certs) + " - " + authType);
                    if (certs == null)
                        throw new CertificateException();
                    for (final X509Certificate c : certs)
                        if (cert.equals(c))
                            return;
                    throw new CertificateException();
                }
            };
        
        final TrustManager[] trustManagers = new TrustManager[] { tm };
        
        javax.net.ssl.SSLContext sc;
        sc = javax.net.ssl.SSLContext.getInstance(Config.RMBT_ENCRYPTION_STRING);
        
        sc.init(null, trustManagers, new java.security.SecureRandom());
        return sc;
    }
    
    public TestResult runTest() throws InterruptedException
    {
    	System.out.println("starting test...");
    	
    	long txBytes = 0;
    	long rxBytes = 0;
    	final long timeStampStart = System.nanoTime();
    	
        if (testStatus.get() != TestStatus.ERROR && testThreadPool != null)
        {
            
        	if (trafficService != null) {
        		txBytes = trafficService.getTotalTxBytes();
        		rxBytes = trafficService.getTotalRxBytes();
        	}
        	
            resetSpeed();
            downBitPerSec.set(-1);
            upBitPerSec.set(-1);
            pingNano.set(-1);
            
            final long waitTime = params.getStartTime() - System.currentTimeMillis();
            if (waitTime > 0)
            {
                setStatus(TestStatus.WAIT);
                log(String.format(Locale.US, "we have to wait %d ms...", waitTime));
                Thread.sleep(waitTime);
                log(String.format(Locale.US, "...done.", waitTime));
            }
            else
                log(String.format(Locale.US, "luckily we do not have to wait.", waitTime));
            
            setStatus(TestStatus.INIT);
            statusBeforeError.set(null);
            
            if (testThreadPool.isShutdown())
                throw new IllegalStateException("RMBTClient already shut down");
            log("starting test...");
            
            final int numThreads = params.getNumThreads();
            
            aborted.set(false);
            
            result = new TotalTestResult();
            
            if (params.isEncryption())
                sslSocketFactory = createSSLSocketFactory();
                        
            log(String.format(Locale.US, "Host: %s; Port: %s; Enc: %s", params.getHost(), params.getPort(), params.isEncryption()));
            log(String.format(Locale.US, "starting %d threads...", numThreads));
            
            final CyclicBarrier barrier = new CyclicBarrier(numThreads);
            
            @SuppressWarnings("unchecked")
            final Future<ThreadTestResult>[] results = new Future[numThreads];
            
            final int storeResults = (int) (params.getDuration() * 1000000000L / MIN_DIFF_TIME);
            
            final AtomicBoolean fallbackToOneThread = new AtomicBoolean();
            
            for (int i = 0; i < numThreads; i++)
            {
                testTasks[i] = new RMBTTest(this, params, i, barrier, storeResults, MIN_DIFF_TIME, fallbackToOneThread);
                results[i] = testThreadPool.submit(testTasks[i]);
            }
            
            try
            {
                
                long shortestPing = Long.MAX_VALUE;
                
                // wait for all threads first
                for (int i = 0; i < numThreads; i++)
                    results[i].get();
                
                if (aborted.get())
                    return null;
                
                final long[][] allDownBytes = new long[numThreads][];
                final long[][] allDownNsecs = new long[numThreads][];
                final long[][] allUpBytes = new long[numThreads][];
                final long[][] allUpNsecs = new long[numThreads][];
                
                int realNumThreads = 0;
                log("");
                for (int i = 0; i < numThreads; i++)
                {
                    final ThreadTestResult testResult = results[i].get();
                    
                    if (testResult != null)
                    {
                        realNumThreads++;
                        
                        log(String.format(Locale.US, "Thread %d: Download: bytes: %d time: %.3f s", i,
                                ThreadTestResult.getLastEntry(testResult.down.bytes),
                                ThreadTestResult.getLastEntry(testResult.down.nsec) / 1e9));
                        log(String.format(Locale.US, "Thread %d: Upload:   bytes: %d time: %.3f s", i,
                                ThreadTestResult.getLastEntry(testResult.up.bytes),
                                ThreadTestResult.getLastEntry(testResult.up.nsec) / 1e9));
                        
                        final long ping = testResult.ping_shortest;
                        if (ping < shortestPing)
                            shortestPing = ping;
                        
                        if (!testResult.pings.isEmpty())
                            result.pings.addAll(testResult.pings);
                        
                        allDownBytes[i] = testResult.down.bytes;
                        allDownNsecs[i] = testResult.down.nsec;
                        allUpBytes[i] = testResult.up.bytes;
                        allUpNsecs[i] = testResult.up.nsec;
                        
                        result.totalDownBytes += testResult.totalDownBytes;
                        result.totalUpBytes += testResult.totalUpBytes;
                        
                        // aggregate speedItems
                        result.speedItems.addAll(testResult.speedItems);
                    }
                }
                
                result.calculateDownload(allDownBytes, allDownNsecs);
                result.calculateUpload(allUpBytes, allUpNsecs);
                
                log("");
                log(String.format(Locale.US, "Total calculated bytes down: %d", result.bytes_download));
                log(String.format(Locale.US, "Total calculated time down:  %.3f s", result.nsec_download / 1e9));
                log(String.format(Locale.US, "Total calculated bytes up:   %d", result.bytes_upload));
                log(String.format(Locale.US, "Total calculated time up:    %.3f s", result.nsec_upload / 1e9));
                
                // get Connection Info from thread 1 (one thread must run)
                result.ip_local = results[0].get().ip_local;
                result.ip_server = results[0].get().ip_server;
                result.port_remote = results[0].get().port_remote;
                result.encryption = results[0].get().encryption;
                
                result.num_threads = realNumThreads;
                
                result.ping_shortest = shortestPing;
                
                result.speed_download = result.getDownloadSpeedBitPerSec() / 1e3;
                result.speed_upload = result.getUploadSpeedBitPerSec() / 1e3;
                
                log("");
                log(String.format(Locale.US, "Total Down: %.0f kBit/s", result.getDownloadSpeedBitPerSec() / 1e3));
                log(String.format(Locale.US, "Total UP:   %.0f kBit/s", result.getUploadSpeedBitPerSec() / 1e3));
                log(String.format(Locale.US, "Ping:       %.2f ms", shortestPing / 1e6));
                
                if (controlConnection != null)
                {
                    log("");
                    final String testId = controlConnection.getTestId();
                    final String testUUID = params.getUUID();
                    final long testTime = controlConnection.getTestTime();
                    log(String.format(Locale.US, "uid=%s, time=%s, uuid=%s\n", testId, new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date(testTime)), testUUID));
                }
                
                downBitPerSec.set(Math.round(result.getDownloadSpeedBitPerSec()));
                upBitPerSec.set(Math.round(result.getUploadSpeedBitPerSec()));
                
                log("end.");
                setStatus(TestStatus.SPEEDTEST_END);
                
            	if (trafficService != null) {
            		txBytes = trafficService.getTotalTxBytes() - txBytes;
            		rxBytes = trafficService.getTotalRxBytes() - rxBytes;
            		result.setTotalTrafficMeasurement(new TestMeasurement(rxBytes, txBytes, timeStampStart, System.nanoTime()));
            		result.setMeasurementMap(measurementMap);
            	}

                
                return result;
            }
            catch (final ExecutionException e)
            {
                log(e);
                abortTest(true);
                return null;
            }
            catch (final InterruptedException e)
            {
                log("RMBTClient interrupted!");
                abortTest(false);
                throw e;
            }
        }
        else {
            setStatus(TestStatus.SPEEDTEST_END);
            
        	return null;
        }
    }
    
    public boolean abortTest(final boolean error)
    {
        System.out.println("RMBTClient stopTest");
        
        if (error)
            setErrorStatus();
        else
            setStatus(TestStatus.ABORTED);
        aborted.set(true);
        
        if (testThreadPool != null)
            testThreadPool.shutdownNow();
        
        return true;
    }
    
    public void shutdown()
    {
        System.out.println("Shutting down RMBT thread pool...");
        if (testThreadPool != null)
            testThreadPool.shutdownNow();
        
        System.out.println("Shutdown finished.");
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        if (testThreadPool != null)
            testThreadPool.shutdownNow();
    }
    
    public SSLSocketFactory getSslSocketFactory()
    {
        return sslSocketFactory;
    }
    
    public void setOutputCallback(final RMBTOutputCallback outputCallback)
    {
        this.outputCallback = outputCallback;
    }
    
    private void resetSpeed()
    {
        lastCounter = 0;
    }
    
    private float getTotalSpeed()
    {
        long sumTrans = 0;
        long maxTime = 0;
        
        final CurrentSpeed currentSpeed = new CurrentSpeed();
        
        for (int i = 0; i < params.getNumThreads(); i++)
            if (testTasks[i] != null)
            {
                testTasks[i].getCurrentSpeed(currentSpeed);
                
                if (currentSpeed.time > maxTime)
                    maxTime = currentSpeed.time;
                sumTrans += currentSpeed.trans;
            }
        
        return maxTime == 0f ? 0f : (float) sumTrans / (float) maxTime * 1e9f * 8.0f;
    }
    
    final Map<Integer, List<SpeedItem>> speedMap = new HashMap<Integer, List<SpeedItem>>();
    
    private float getAvgSpeed()
    {
        long sumDiffTrans = 0;
        long maxDiffTime = 0;
        
        final CurrentSpeed currentSpeed = new CurrentSpeed();
        
        final int currentIndex = lastCounter % KEEP_LAST_ENTRIES;
        int diffReferenceIndex = (lastCounter - KEEP_LAST_ENTRIES + 1) % KEEP_LAST_ENTRIES;
        if (diffReferenceIndex < 0)
            diffReferenceIndex = 0;
        
        lastCounter++;
        
        for (int i = 0; i < params.getNumThreads(); i++)
            if (testTasks[i] != null)
            {
                testTasks[i].getCurrentSpeed(currentSpeed);
                
                lastTime[i][currentIndex] = currentSpeed.time;
                lastTransfer[i][currentIndex] = currentSpeed.trans;
                
//                System.out.println("T" + i + ": " + currentSpeed);
                
                List<SpeedItem> speedList = speedMap.get(i);
                if (speedList == null) {
                	speedList = new ArrayList<SpeedItem>();
                	speedMap.put(i, speedList);
                }
                
                speedList.add(new SpeedItem(false, i, currentSpeed.time, currentSpeed.trans));
                
                final long diffTime = currentSpeed.time - lastTime[i][diffReferenceIndex];
                final long diffTrans = currentSpeed.trans - lastTransfer[i][diffReferenceIndex];
                
                if (diffTime > maxDiffTime)
                    maxDiffTime = diffTime;
                sumDiffTrans += diffTrans;
            }
                
        //TotalTestResult totalResult = TotalTestResult.calculateAndGet(lastTransfer, lastTime, false);
        //TotalTestResult totalResult = TotalTestResult.calculateAndGet(speedMap);
        
        final float speedAvg = maxDiffTime == 0f ? 0f : (float) sumDiffTrans / (float) maxDiffTime * 1e9f * 8.0f;
        //final float speedAvg = (float)totalResult.speed_download * 1e3f;
        
//        System.out.println("calculate: bytes=" + totalResult.bytes_download + " speed=" + (totalResult.speed_download * 1e3) 
//        		+ " nsec=" + totalResult.nsec_download + ", simple: diff=" + sumDiffTrans + " avg=" + speedAvg);
        
        return speedAvg;
    }
    
    public IntermediateResult getIntermediateResult(IntermediateResult iResult)
    {
        if (iResult == null)
            iResult = new IntermediateResult();
        iResult.status = testStatus.get();
        iResult.remainingWait = 0;
        final long diffTime = System.nanoTime() - statusChangeTime.get();
        switch (iResult.status)
        {
        case WAIT:
            iResult.progress = 0;
            iResult.remainingWait = params.getStartTime() - System.currentTimeMillis();
            break;
        
        case INIT:
            iResult.progress = (float) diffTime / durationInitNano;
            break;
        
        case PING:
            iResult.progress = getPingProgress();
            break;
        
        case DOWN:
            iResult.progress = (float) diffTime / durationDownNano;
            downBitPerSec.set(Math.round(getAvgSpeed()));
            break;
        
        case INIT_UP:
            iResult.progress = 0;
            break;
        
        case UP:
            iResult.progress = (float) diffTime / durationUpNano;
            upBitPerSec.set(Math.round(getAvgSpeed()));
            break;
        
        case SPEEDTEST_END:
            iResult.progress = 1;
            break;
        
        case ERROR:
        case ABORTED:
            iResult.progress = 0;
            break;
        }
        
        if (iResult.progress > 1)
            iResult.progress = 1;
        
        iResult.pingNano = pingNano.get();
        iResult.downBitPerSec = downBitPerSec.get();
        iResult.upBitPerSec = upBitPerSec.get();
        
        iResult.setLogValues();
        
        return iResult;
    }
    
    public TestStatus getStatus()
    {
        return testStatus.get();
    }
    
    public TestStatus getStatusBeforeError()
    {
        return statusBeforeError.get();
    }
    
    public void setStatus(final TestStatus status)
    {
        testStatus.set(status);
        statusChangeTime.set(System.nanoTime());
        if (status == TestStatus.INIT_UP)
        {
            // DOWN is finished
            downBitPerSec.set(Math.round(getTotalSpeed()));
            resetSpeed();
        }
    }
        
    public void startTrafficService(final int threadId, final TestStatus status) {
    	if (trafficService != null) {
    		//a concurrent map is needed in case multiple threads want to start the traffic service
    		//only the first thread should be able to start the service
    		TestMeasurement tm = new TestMeasurement(status.toString(), trafficService);
        	TestMeasurement previousTm = measurementMap.putIfAbsent(status, tm);
        	if (previousTm == null) {
        		tm.start(threadId);
        	}
    	}
    }
    
    public void stopTrafficMeasurement(final int threadId, final TestStatus status) {
    	final TestMeasurement testMeasurement = measurementMap.get(status);
    	if (testMeasurement != null)
    	    testMeasurement.stop(threadId);
    }
    
    public Map<TestStatus, TestMeasurement> getTrafficMeasurementMap() {
    	return measurementMap;
    }
    
    public String getErrorMsg()
    {
        return errorMsg;
    }
    
    public void sendResult(final JSONObject additionalValues)
    {
    	if (controlConnection != null) {
    		final String errorMsg = controlConnection.sendTestResult(result, additionalValues);
            if (errorMsg != null)
            {
                setErrorStatus();
                log("Error sending Result...");
                log(errorMsg);
            }
    	}
    }
        
    public void sendQoSResult(final QoSResultCollector qosResult) {
    	if (controlConnection != null) {
    		final String errorMsg = controlConnection.sendQoSResult(result, qosResult.toJson());
            if (errorMsg != null)
            {
                setErrorStatus();
                log("Error sending QoS Result...");
                log(errorMsg);
            }
    	}    	
    }

    private void setErrorStatus()
    {
        final TestStatus lastStatus = testStatus.getAndSet(TestStatus.ERROR);
        if (lastStatus != TestStatus.ERROR)
            statusBeforeError.set(lastStatus);
    }
    
    void log(final CharSequence text)
    {
        if (outputToStdout)
            System.out.println(text);
        if (outputCallback != null)
            outputCallback.log(text);
    }
    
    void log(final Exception e)
    {
        if (outputToStdout)
            e.printStackTrace(System.out);
        if (outputCallback != null)
            outputCallback.log(String.format(Locale.US, "Error: %s", e.getMessage()));
    }
    
    void setPing(final long shortestPing)
    {
        pingNano.set(shortestPing);
    }
    
    void updatePingStatus(final long tsStart, int pingsDone, long tsLastPing)
    {
        pingTsStart.set(tsStart);
        pingNumDome.set(pingsDone);
        pingTsLastPing.set(tsLastPing);
    }
    
    private float getPingProgress()
    {
        final long start = pingTsStart.get();
        
        if (start == -1) // not yet started
            return 0;
        
        final int numDone = pingNumDome.get();
        final long lastPing = pingTsLastPing.get();
        final long now = System.nanoTime();
        
        final int numPings = params.getNumPings();
        
        if (numPings <= 0) // nothing to do
            return 1;
        
        final float factorPerPing = (float)1 / (float)numPings;
        final float base = factorPerPing * numDone;
        
        final long approxTimePerPing;
        if (numDone == 0 || lastPing == -1) // during first ping, assume 100ms
            approxTimePerPing = 100000000;
        else
            approxTimePerPing = (lastPing - start) / numDone;
        
        float factorLastPing = (float)(now - lastPing) / (float)approxTimePerPing;
        if (factorLastPing < 0)
            factorLastPing = 0;
        if (factorLastPing > 1)
            factorLastPing = 1;
        
        final float result = base + factorLastPing * factorPerPing;
        if (result < 0)
            return 0;
        if (result > 1)
            return 1;
        
//        System.out.println("atpp: " + approxTimePerPing + "; flp:" + factorLastPing+ "; res:" +result);
        return result;
    }
    
    public String getPublicIP()
    {
        if (controlConnection == null)
            return null;
        return controlConnection.getRemoteIp();
    }
    
    public String getServerName()
    {
        if (controlConnection == null)
            return null;
        return controlConnection.getServerName();
    }
    
    public String getProvider()
    {
        if (controlConnection == null)
            return null;
        return controlConnection.getProvider();
    }
    
    public String getTestUuid()
    {
        if (controlConnection == null)
            return null;
        return controlConnection.getTestUuid();
    }
    
    public long getStartTimeMillis() {
    	return controlConnection != null ? controlConnection.getStartTimeMillis() : 0;
    }
    
    public ControlServerConnection getControlConnection()
    {
        return controlConnection;
    }

    /**
     * 
     * @return
     */
	public List<TaskDesc> getTaskDescList() {
		return taskDescList;
	}
    
}
