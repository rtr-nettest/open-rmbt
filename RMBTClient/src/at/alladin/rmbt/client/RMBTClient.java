/*******************************************************************************
 * Copyright 2013 alladin-IT OG
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
import java.util.Locale;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
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

public class RMBTClient
{
    
    private final RMBTTestParameter params;
    
    private final long durationInitNano = 2500000000L; // TODO
    private final long durationPingNano = 500000000L; // TODO
    private final long durationUpNano;
    private final long durationDownNano;
    
    private final AtomicLong pingNano = new AtomicLong(-1);
    private final AtomicLong downBitPerSec = new AtomicLong(-1);
    private final AtomicLong upBitPerSec = new AtomicLong(-1);
    
    private final static long MIN_DIFF_TIME = 100000000; // 100 ms
    
    private final static int KEEP_LAST_ENTRIES = 20;
    private int lastCounter;
    private final long[][] lastTransfer;
    private final long[][] lastTime;
    
    private final ExecutorService testThreadPool;
    private final ExecutorService commonThreadPool = Executors.newCachedThreadPool();
    
    private final RMBTTest[] testTasks;
    
    private TotalTestResult result;
    
    private SSLSocketFactory sslSocketFactory;
    
    private RMBTOutputCallback outputCallback;
    private final boolean outputToStdout = true;
    
    private final ControlServerConnection controlConnection;
    
    private final AtomicBoolean aborted = new AtomicBoolean();
    
    private String errorMsg = "";
    
    private final AtomicReference<TestStatus> testStatus = new AtomicReference<TestStatus>(TestStatus.WAIT);
    private final AtomicReference<TestStatus> statusBeforeError = new AtomicReference<TestStatus>(null);
    private final AtomicLong statusChangeTime = new AtomicLong();
    
    public static RMBTClient getInstance(final String host, final String pathPrefix, final int port,
            final boolean encryption, final ArrayList<String> geoInfo, final String uuid, final String clientType,
            final String clientName, final String clientVersion, final RMBTTestParameter overrideParams,
            final JSONObject additionalValues)
    {
        final ControlServerConnection controlConnection = new ControlServerConnection();
        
        final String error = controlConnection.requestNewTestConnection(host, pathPrefix, port, encryption, geoInfo,
                uuid, clientType, clientName, clientVersion, additionalValues);
        
        if (error != null)
        {
            System.out.println(error);
            return null;
        }
        
        final RMBTTestParameter params = controlConnection.getTestParameter();
        
        return new RMBTClient(params, controlConnection);
    }
    
    public static RMBTClient getInstance(final RMBTTestParameter params)
    {
        return new RMBTClient(params, null);
    }
    
    private RMBTClient(final RMBTTestParameter params, final ControlServerConnection controlConnection)
    {
        this.params = params;
        this.controlConnection = controlConnection;
        
        if (controlConnection != null && controlConnection.hasError())
        {
            setErrorStatus();
            log("Error initializing RMBTTest...");
            errorMsg = controlConnection.getErrorMsg();
            log(errorMsg);
        }
        
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
        if (testStatus.get() != TestStatus.ERROR && testThreadPool != null)
        {
            
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
            
            if (testThreadPool.isShutdown() || commonThreadPool.isShutdown())
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
                setStatus(TestStatus.END);
                
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
        else
        {
            log("Threadpool is empty.");
            return result;
        }
    }
    
    public ExecutorService getCommonThreadPool()
    {
        return commonThreadPool;
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
        if (commonThreadPool != null)
            commonThreadPool.shutdownNow();
        
        return true;
    }
    
    public void shutdown()
    {
        System.out.println("Shutting down RMBT thread pool.");
        if (testThreadPool != null)
            testThreadPool.shutdown();
        if (commonThreadPool != null)
            commonThreadPool.shutdown();
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
                
                final long diffTime = currentSpeed.time - lastTime[i][diffReferenceIndex];
                final long diffTrans = currentSpeed.trans - lastTransfer[i][diffReferenceIndex];
                
                if (diffTime > maxDiffTime)
                    maxDiffTime = diffTime;
                sumDiffTrans += diffTrans;
            }
        return maxDiffTime == 0f ? 0f : (float) sumDiffTrans / (float) maxDiffTime * 1e9f * 8.0f;
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
            iResult.progress = (float) diffTime / durationPingNano;
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
        
        case END:
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
    
    public String getError()
    {
        return errorMsg;
    }
    
    public TestStatus getStatus()
    {
        return testStatus.get();
    }
    
    public TestStatus getStatusBeforeError()
    {
        return statusBeforeError.get();
    }
    
    void setStatus(final TestStatus status)
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
    
    public String getErrorMsg()
    {
        return errorMsg;
    }
    
    public void sendResult(final JSONObject additionalValues)
    {
        if (controlConnection != null)
        {
            controlConnection.sendTestResult(result, additionalValues);
            
            if (controlConnection.hasError())
            {
                setErrorStatus();
                log("Error sending Result...");
                errorMsg = controlConnection.getErrorMsg();
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
    
    public ControlServerConnection getControlConnection()
    {
        return controlConnection;
    }
    
}
