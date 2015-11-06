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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import at.alladin.rmbt.client.helper.TestStatus;

public class RMBTTest extends AbstractRMBTTest implements Callable<ThreadTestResult>
{
    private static final long nsecsL = 1000000000L;
//    private static final double nsecs = 1e9;
    
    private static final long UPLOAD_MAX_DISCARD_TIME = 1 * nsecsL;
    private static final long UPLOAD_MAX_WAIT_SECS = 3;
    
    private final CyclicBarrier barrier;
    private final AtomicBoolean fallbackToOneThread;
    
    private final boolean doDownload = true;
    private final boolean doUpload = true;
    
    private final AtomicLong curTransfer = new AtomicLong();
    private final AtomicLong curTime = new AtomicLong();
        
    private final long minDiffTime;
    private final int maxCoarseResults;
    private final int maxFineResults;
    
    private class SingleResult
    {
        private final Results fine;
        private final Results coarse;
        
        private int fineResults = 0;
        private int coarseResults = 0;
        
        SingleResult()
        {
            fine = new Results(maxFineResults);
            coarse = new Results(maxCoarseResults);
        }
        
        @Override
		public String toString() {
			return "SingleResult [fine=" + fine + ", coarse=" + coarse
					+ ", fineResults=" + fineResults + ", coarseResults="
					+ coarseResults + "]";
		}



		public void addResult(final long newBytes, final long newNsec)
        {
            
            boolean addToCoarse = coarseResults == 0;
            if (! addToCoarse)
            {
                final long diffTime = newNsec - coarse.nsec[(coarseResults - 1) % coarse.nsec.length];
                if (diffTime > minDiffTime)
                    addToCoarse = true;
            }
            
            if (coarse.bytes.length > 0) {
                if (addToCoarse)
                {
                    int coarsePos = coarseResults++ % coarse.bytes.length;
                    coarse.bytes[coarsePos] = newBytes;
                    coarse.nsec[coarsePos] = newNsec;
                }
                
                int finePos = fineResults++ % fine.bytes.length;
                fine.bytes[finePos] = newBytes;
                fine.nsec[finePos] = newNsec;
            }
        }
        
//        @SuppressWarnings("unused")
//        void logResult(final String type)
//        {
//            log(String.format(Locale.US, "thread %d - Time Diff %d", threadId, nsec));
//            log(String.format(Locale.US, "thread %d: %.0f kBit/s %s (%.2f kbytes / %.3f secs)", threadId, getSpeed() / 1e3, type,
//                    getBytes() / 1e3, getNsec() / nsecs));
//        }
        
//        // bit/s
//        double getSpeed()
//        {
//            return (double) getBytes() / (double) getNsec() * nsecs * 8.0;
//        }
        
        public long getBytes()
        {
            if (fineResults == 0)
                return 0;
            else
                return fine.bytes[(fineResults - 1) % fine.bytes.length];
        }
        
        public long getNsec()
        {
            if (fineResults == 0)
                return 0;
            else
                return fine.nsec[(fineResults - 1) % fine.nsec.length];
        }
        
        public Results getAllResults()
        {
            final int numResultsCoarse = Math.min(coarseResults, maxCoarseResults);
            final int numResultsFine = Math.min(fineResults, maxFineResults);
            final int numResults = numResultsCoarse + numResultsFine;
            
            long[] resultBytes = new long[numResults];
            long[] resultNsec = new long[numResults];
            
            int results = 0;
            int posCoarse = coarseResults - numResultsCoarse;
            int posFine = fineResults - numResultsFine;
            
            while (results < numResults && (posCoarse < coarseResults || posFine < fineResults))
            {
                final boolean coarseAvail = posCoarse < coarseResults;
                final boolean fineAvail = posFine < fineResults;
                final long thisCoarse = coarseAvail ? coarse.nsec[posCoarse % coarse.nsec.length] : -1;
                final long thisFine = fineAvail ? fine.nsec[posFine % fine.nsec.length] : -1;
                
                if ((thisFine <= thisCoarse || thisCoarse == -1) && fineAvail)
                {
                    resultNsec[results] = thisFine;
                    resultBytes[results++] = fine.bytes[posFine++ % fine.bytes.length];
                    
                    if (thisFine == thisCoarse && coarseAvail)
                        posCoarse++;
                }
                else if ((thisCoarse < thisFine || thisFine == -1) && coarseAvail)
                {
                    resultNsec[results] = thisCoarse;
                    resultBytes[results++] = coarse.bytes[posCoarse++ % coarse.bytes.length];
                }
                else // shoudn't happen; avoid endless loop
                    break;
            }
            
            if (results < numResults)
            {
//                resultBytes = Arrays.copyOf(resultBytes, results); // copyOf not avail in android sdk < 9
//                resultNsec = Arrays.copyOf(resultNsec, results);
                
                long[] newResultBytes = new long[results];
                long[] newResultNsec = new long[results];
                System.arraycopy(resultBytes, 0, newResultBytes, 0, results);
                System.arraycopy(resultNsec, 0, newResultNsec, 0, results);
                resultBytes = newResultBytes;
                resultNsec = newResultNsec;
            }
            final Results result = new Results(resultBytes, resultNsec);
            return result;
        }
        
        public void addCoarseSpeedItems(List<SpeedItem> list, boolean upload, int thread)
        {
            long lastNsec = 0;
            final int numResultsCoarse = Math.min(coarseResults, maxCoarseResults);
            for (int i = 0; i < numResultsCoarse; i++)
            {
                final long nsec = coarse.nsec[i % coarse.nsec.length];
                final long bytes = coarse.bytes[i % coarse.bytes.length];
                final SpeedItem item = new SpeedItem(upload, thread, nsec, bytes);
                list.add(item);
                lastNsec = nsec;
            }
            
            final long nsec = getNsec();
            if (nsec > lastNsec)
            {
                final long bytes = getBytes();
                final SpeedItem item = new SpeedItem(upload, thread, nsec, bytes);
                list.add(item);
            }
        }
    }
    
    public RMBTTest(final RMBTClient client, final RMBTTestParameter params, final int threadId,
            final CyclicBarrier barrier, final int storeResults, final long minDiffTime,
            final AtomicBoolean fallbackToOneThread)
    {
    	super (client, params, threadId);
        this.barrier = barrier;
        this.maxCoarseResults = storeResults;
        this.maxFineResults = storeResults;
        this.minDiffTime = minDiffTime;
        this.fallbackToOneThread = fallbackToOneThread;
    }
        
    static class CurrentSpeed
    {
        long trans;
        long time;

        @Override
		public String toString() {
			return "CurrentSpeed [trans=" + trans + ", time=" + time + "]";
		}
    }
    
    public CurrentSpeed getCurrentSpeed(CurrentSpeed result)
    {
        if (result == null)
            result = new CurrentSpeed();
        result.trans = curTransfer.get();
        result.time = curTime.get();
        return result;
    }
    
    protected Socket connect(final TestResult testResult) throws IOException
    {
        log(String.format(Locale.US, "thread %d: connecting...", threadId));
        
        final InetAddress inetAddress = InetAddress.getByName(params.getHost());
        
        System.out.println("connecting to: " + inetAddress.getHostName() + ":" + params.getPort());
        final Socket s = getSocket(inetAddress.getHostAddress(), params.getPort(), true, 20000);
        
        testResult.ip_local = s.getLocalAddress();
        testResult.ip_server = s.getInetAddress();
        
        testResult.port_remote = s.getPort();
        
        if (s instanceof SSLSocket)
        {
            final SSLSocket sslSocket = (SSLSocket) s;
            final SSLSession session = sslSocket.getSession();
            testResult.encryption = String.format(Locale.US, "%s (%s)", session.getProtocol(), session.getCipherSuite());
        }
        
        log(String.format(Locale.US, "thread %d: ReceiveBufferSize: '%s'.", threadId, s.getReceiveBufferSize()));
        log(String.format(Locale.US, "thread %d: SendBufferSize: '%s'.", threadId, s.getSendBufferSize()));
        
        if (in != null)
            totalDown += in.getCount();
        if (out != null)
            totalUp += out.getCount();
        
        in = new InputStreamCounter(s.getInputStream());
        reader = new BufferedReader(new InputStreamReader(in, "US-ASCII"), 4096);
        out = new OutputStreamCounter(s.getOutputStream());
        
        String line = reader.readLine();
        if (!line.equals(EXPECT_GREETING))
        {
            log(String.format(Locale.US, "thread %d: got '%s' expected '%s'", threadId, line, EXPECT_GREETING));
            return null;
        }
        
        line = reader.readLine();
        if (!line.startsWith("ACCEPT "))
        {
            log(String.format(Locale.US, "thread %d: got '%s' expected 'ACCEPT'", threadId, line));
            return null;
        }
        
        final String send = String.format(Locale.US, "TOKEN %s\n", params.getToken());
        
        out.write(send.getBytes("US-ASCII"));
        
        line = reader.readLine();
        
        if (line == null)
        {
            log(String.format(Locale.US, "thread %d: got no answer expected 'OK'", threadId, line));
            return null;
        }
        else if (!line.equals("OK"))
        {
            log(String.format(Locale.US, "thread %d: got '%s' expected 'OK'", threadId, line));
            return null;
        }
        
        line = reader.readLine();
        final Scanner scanner = new Scanner(line);
        try
        {
            if (!"CHUNKSIZE".equals(scanner.next()))
            {
                log(String.format(Locale.US, "thread %d: got '%s' expected 'CHUNKSIZE'", threadId, line));
                return null;
            }
            try
            {
                chunksize = scanner.nextInt();
                log(String.format(Locale.US, "thread %d: CHUNKSIZE is %d", threadId, chunksize));
            }
            catch (final Exception e)
            {
                log(String.format(Locale.US, "thread %d: invalid CHUNKSIZE: '%s'", threadId, line));
                return null;
            }
            if (buf == null || buf != null && buf.length != chunksize)
                buf = new byte[chunksize];
            return s;
        }
        finally
        {
            scanner.close();
        }
    }
    
    public ThreadTestResult call()
    {
        log(String.format(Locale.US, "thread %d: started.", threadId));
        final ThreadTestResult testResult = new ThreadTestResult();
        Socket s = null;
        try
        {
            
            s = connect(testResult);
            if (s == null)
                throw new Exception("error during connect to test server");
            
            log(String.format(Locale.US, "thread %d: connected, waiting for rest...", threadId));
            barrier.await();
            
            /***** short download *****/
            {
                final long targetTimeEnd = System.nanoTime() + params.getPretestDuration() * nsecsL;
                int chunks = 1;
                do
                {
                    downloadChunks(chunks);
                    chunks *= 2;
                }
                while (System.nanoTime() < targetTimeEnd);
                
                if (chunks <= 4)
                    // connection is quite slow, we'll only use 1 thread
                    fallbackToOneThread.set(true);
            }
            /*********************/
            
            boolean _fallbackToOneThread;
            setStatus(TestStatus.PING);
            /***** ping *****/
            {
                barrier.await();
                
                startTrafficService(TestStatus.PING);
                
                _fallbackToOneThread = fallbackToOneThread.get();
                
                if (_fallbackToOneThread && threadId != 0)
                    return null;
                
                final int NUMPINGS = params.getNumPings();
                long shortestPing = Long.MAX_VALUE;
                long medianPing = Long.MAX_VALUE;
                long[] pings = new long[NUMPINGS];
                final long timeStart = System.nanoTime();
                if (threadId == 0) // only one thread pings!
                {
                	for (int i = 0; i < NUMPINGS; i++)
                	{
                		final Ping ping = ping();
                		if (ping != null)
                		{
                		    client.updatePingStatus(timeStart, i+1, System.nanoTime());
                		    
                		    pings[i] = ping.server;
                			if (ping.client < shortestPing)
                				shortestPing = ping.client;

                			testResult.pings.add(ping);
                		}
                	}
                	
                	// median
                	Arrays.sort(pings);
                	int middle = ((pings.length) / 2);
                	if(pings.length % 2 == 0){
                		long medianA = pings[middle];
                		long medianB = pings[middle-1];
                		medianPing = (medianA + medianB) / 2;
                	} else{
                		medianPing = pings[middle + 1];
                	}
                	// display median ping
                	client.setPing(medianPing);
                }
                testResult.ping_shortest = shortestPing;
                testResult.ping_median = medianPing;

            }
            /*********************/
            
                        
            if (doDownload)
            {
                final int duration = params.getDuration();
            	//final int duration = 1;
                
                setStatus(TestStatus.DOWN);
                /***** download *****/
                
                if (!_fallbackToOneThread)
                    barrier.await();
                
                stopTrafficService(TestStatus.PING);
                startTrafficService(TestStatus.DOWN);
                
                curTransfer.set(0);
                curTime.set(0);
                
                final SingleResult result = new SingleResult();
                final boolean reinitSocket = download(duration, 0, result);
                if (reinitSocket)
                {
                    s.close();
                    s = connect(testResult);
                    log(String.format(Locale.US, "thread %d: reconnected", threadId));
                    if (s == null)
                        throw new Exception("error during connect to test server");
                }
                
                testResult.down = result.getAllResults();
                result.addCoarseSpeedItems(testResult.speedItems, false, threadId);
                
//                if (threadId == 0) {
//                	System.out.println("download speed items: " + testResult.speedItems);
//                	System.out.println("download raw results: " + result);
//                }
                
                curTransfer.set(result.getBytes());
                curTime.set(result.getNsec());
             
                
                /*********************/
                
            }
            
            if (doUpload)
            {
                final int duration = params.getDuration();
            	//final int duration = 1;
                
                setStatus(TestStatus.INIT_UP);
                /***** short upload *****/
                {
                    if (!_fallbackToOneThread)
                        barrier.await();
                    
                    stopTrafficService(TestStatus.DOWN);
                    
                    curTransfer.set(0);
                    curTime.set(0);
                    
                    final long targetTimeEnd = System.nanoTime() + params.getPretestDuration() * nsecsL;
                    int chunks = 1;
                    do
                    {
                        uploadChunks(chunks);
                        chunks *= 2;
                    }
                    while (System.nanoTime() < targetTimeEnd);
                }
                /*********************/
                
                /***** upload *****/
                
                setStatus(TestStatus.UP);
                
                startTrafficService(TestStatus.UP);
                
                curTransfer.set(0);
                curTime.set(0);
                
                if (!_fallbackToOneThread)
                    barrier.await();
                
                final SingleResult result = new SingleResult();
                
                upload(duration, result);
                
                testResult.up = result.getAllResults();
                result.addCoarseSpeedItems(testResult.speedItems, true, threadId);
                
                if (in != null)
                    totalDown += in.getCount();
                if (out != null)
                    totalUp += out.getCount();
                
                testResult.totalDownBytes = totalDown;
                testResult.totalUpBytes = totalUp;
                
                curTransfer.set(result.getBytes());
                curTime.set(result.getNsec());
                
                stopTrafficService(TestStatus.UP);
                
                /*********************/
            }
            
        }
        catch (final BrokenBarrierException e)
        {
            client.log("interrupted (BBE)");
            Thread.currentThread().interrupt();
        }
        catch (final InterruptedException e)
        {
            client.log("interrupted");
            Thread.currentThread().interrupt();
        }
        catch (final Exception e)
        {
            client.log(e);
            client.abortTest(true);
        }
        finally
        {
            if (s != null)
                try
                {
                    s.close();
                }
                catch (final IOException e)
                {
                    client.log(e);
                }
        }
        return testResult;
    }
    
    private void downloadChunks(final int chunks) throws InterruptedException, IOException
    {
        if (Thread.interrupted())
            throw new InterruptedException();
        
        if (chunks < 1)
            throw new IllegalArgumentException();
        
        log(String.format(Locale.US, "thread %d: getting %d chunk(s)", threadId, chunks));
        
        String line = reader.readLine();
        if (line == null)
            throw new IllegalStateException("connection lost");
        if (!line.startsWith("ACCEPT "))
        {
            log(String.format(Locale.US, "thread %d: got '%s' expected 'ACCEPT'", threadId, line));
            throw new IllegalStateException();
        }
        
        String send;
        send = String.format(Locale.US, "GETCHUNKS %d\n", chunks);
        out.write(send.getBytes("US-ASCII"));
        out.flush();
        
        // long expectBytes = chunksize * chunks;
        long totalRead = 0;
        long read;
        byte lastByte = (byte) 0;
        do
        {
            if (Thread.interrupted())
                throw new InterruptedException();
            read = in.read(buf);
            if (read > 0)
            {
                final int posLast = chunksize - 1 - (int) (totalRead % chunksize);
                if (read > posLast)
                    lastByte = buf[posLast];
                totalRead += read;
            }
        }
        while (read > 0 && lastByte != (byte) 0xff);
        
        send = "OK\n";
        out.write(send.getBytes("US-ASCII"));
        out.flush();
        
        line = reader.readLine(); // read TIME line
    }
    
    /**
     * perform single donwload test
     * 
     * @param seconds
     *            requested duration of the test
     * @param result
     *            SingleResult object to store the results in
     * @return true if the socket needs to be reinitialized, false if can be
     *         reused
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws InterruptedException
     * @throws IllegalStateException
     */
    private boolean download(final int seconds, final int additionalWait, final SingleResult result)
            throws IOException, UnsupportedEncodingException, InterruptedException, IllegalStateException
    {
        if (Thread.interrupted())
            throw new InterruptedException();
        
        if (seconds < 1)
            throw new IllegalArgumentException();
        
        log(String.format(Locale.US, "thread %d: download test %d seconds", threadId, seconds));
        
        String line = reader.readLine();
        if (line == null)
            throw new IllegalStateException("connection lost");
        if (!line.startsWith("ACCEPT "))
        {
            log(String.format(Locale.US, "thread %d: got '%s' expected 'ACCEPT'", threadId, line));
            throw new IllegalStateException();
        }
        
        final long timeStart = System.nanoTime();
        final long timeLatestEnd = timeStart + (seconds + additionalWait) * nsecsL;
        
        String send;
        send = String.format(Locale.US, "GETTIME %d\n", seconds);
        out.write(send.getBytes("US-ASCII"));
        out.flush();
        
        long totalRead = 0;
        long read;
        byte lastByte = (byte) 0;
        
        do
        {
            if (Thread.interrupted())
                throw new InterruptedException();
            read = in.read(buf);
            if (read > 0)
            {
                final int posLast = chunksize - 1 - (int) (totalRead % chunksize);
                if (read > posLast)
                    lastByte = buf[posLast];
                totalRead += read;
                
                final long nsec = System.nanoTime() - timeStart;
                
                result.addResult(totalRead, nsec);
                curTransfer.set(totalRead);
                curTime.set(nsec);
            }
        }
        while (read > 0 && lastByte != (byte) 0xff && System.nanoTime() <= timeLatestEnd);
        
        final long timeEnd = System.nanoTime();
        
        if (read <= 0)
        {
            log(String.format(Locale.US, "thread %d: error while receiving data", threadId));
            throw new IllegalStateException();
        }
        
        final long nsec = timeEnd - timeStart;
        result.addResult(totalRead, nsec);
        curTransfer.set(totalRead);
        curTime.set(nsec);
        
        if (lastByte != (byte) 0xff)
            return true;
        
        send = "OK\n";
        out.write(send.getBytes("US-ASCII"));
        out.flush();
        
        line = reader.readLine();
        if (line == null)
            throw new IllegalStateException("connection lost");
        final Scanner s = new Scanner(line);
        s.findInLine("TIME (\\d+)");
        s.close();
        // result.nsecServer = Long.parseLong(s.match().group(1));
        return false;
        
    }
    
    private void uploadChunks(final int chunks) throws InterruptedException, IOException
    {
        if (Thread.interrupted())
            throw new InterruptedException();
        
        if (chunks < 1)
            throw new IllegalArgumentException();
        
        log(String.format(Locale.US, "thread %d: putting %d chunk(s)", threadId, chunks));
        
        String line = reader.readLine();
        if (line == null)
            throw new IllegalStateException("connection lost");
        if (!line.startsWith("ACCEPT "))
        {
            log(String.format(Locale.US, "thread %d: got '%s' expected 'ACCEPT'", threadId, line));
            throw new IllegalStateException();
        }
        
        out.write("PUTNORESULT\n".getBytes("US-ASCII"));
        out.flush();
        
        line = reader.readLine();
        if (line == null)
            throw new IllegalStateException("connection lost");
        if (!line.equals("OK"))
            throw new IllegalStateException();
        
        buf[chunksize - 1] = (byte) 0; // set last byte to continue value
        
        for (int i = 0; i < chunks; i++)
        {
            if (i == chunks - 1)
                buf[chunksize - 1] = (byte) 0xff; // set last byte to
                                                  // termination value
            out.write(buf, 0, chunksize);
        }
        
        line = reader.readLine(); // TIME line
    }
    
    /**
     * @param seconds
     *            requested duration of the test
     * @param result
     *            SingleResult object to store the results in
     * @return true if the socket needs to be reinitialized, false if can be
     *         reused
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws InterruptedException
     * @throws IllegalStateException
     */
    private boolean upload(final int seconds, final SingleResult result) throws IOException,
            UnsupportedEncodingException, InterruptedException, IllegalStateException
    {
        if (Thread.interrupted())
            throw new InterruptedException();
        
        if (seconds < 1 && !params.isEncryption())
            throw new IllegalArgumentException();
        
        log(String.format(Locale.US, "thread %d: upload test %d seconds", threadId, seconds));
        
        long _enoughTime = (seconds - UPLOAD_MAX_DISCARD_TIME) * nsecsL;
        if (_enoughTime < 0)
            _enoughTime = 0;
        final long enoughTime = _enoughTime;
        
        String line = reader.readLine();
        if (line == null)
            throw new IllegalStateException("connection lost");
        if (!line.startsWith("ACCEPT "))
        {
            log(String.format(Locale.US, "thread %d: got '%s' expected 'ACCEPT'", threadId, line));
            throw new IllegalStateException();
        }
        
        out.write("PUT\n".getBytes("US-ASCII"));
        out.flush();
        
        line = reader.readLine();
        if (line == null)
            throw new IllegalStateException("connection lost");
        if (!line.equals("OK"))
            throw new IllegalStateException();
        
        final AtomicBoolean terminateRxIfEnough = new AtomicBoolean(false);
        final AtomicBoolean terminateRxAtAllEvents = new AtomicBoolean(false);
        
        final Future<Boolean> futureRx = RMBTClient.getCommonThreadPool().submit(new Callable<Boolean>()
        {
            public Boolean call() throws Exception
            {
                
                final Pattern patternFull = Pattern.compile("TIME (\\d+) BYTES (\\d+)");
                final Pattern patternTime = Pattern.compile("TIME (\\d+)");
                
                final Scanner s = new Scanner(reader);
                try
                {
                    s.useDelimiter("\n");
                    boolean terminate = false;
                    do
                    {
                        String next = null;
                        try
                        {
                            next = s.next(patternFull);
                        }
                        catch (final InputMismatchException e)
                        {
                        }
                        
                        if (next == null)
                        {
                            next = s.next(patternTime);
                            if (next == null)
                            {
                                System.out.println(s.nextLine());
                                throw new IllegalStateException();
                            }
                            return false;
                        }
                        
                        final MatchResult match = s.match();
                        if (match.groupCount() == 2)
                        {
                            final long nsec = Long.parseLong(match.group(1));
                            final long bytes = Long.parseLong(match.group(2));
                            result.addResult(bytes, nsec);
                            curTransfer.set(bytes);
                            curTime.set(nsec);
                        }
                        
                        if (terminateRxAtAllEvents.get())
                            terminate = true;
                        if (terminateRxIfEnough.get() && curTime.get() > enoughTime)
                            terminate = true;
                    }
                    while (! terminate);
                    return true;
                }
                finally
                {
                    s.close();
                }
            }
        });
        
        final long maxnsecs = seconds * 1000000000L;
        buf[chunksize - 1] = (byte) 0x00; // set last byte to continue value
        
        final byte[] bufTx = buf.clone();
        final AtomicBoolean terminateTx = new AtomicBoolean(false);
        final Future<Void> futureTx = RMBTClient.getCommonThreadPool().submit(new Callable<Void>()
        {
            public Void call() throws Exception
            {
                for (;;)
                {
                    if (Thread.interrupted())
                        throw new InterruptedException();
                    if (terminateTx.get())
                    {
                        // last package
                        bufTx[chunksize - 1] = (byte) 0xff; // set last byte to termination value
                        out.write(bufTx, 0, chunksize);
                        // forces buffered bytes to be written out.
                        out.flush();
                        return null;
                    }
                    else
                        out.write(bufTx, 0, chunksize);
                }
            }
        });

        Boolean returnValue = null;
        try
        {
            try
            {
                futureTx.get(maxnsecs, TimeUnit.NANOSECONDS);
//                System.out.println("futureTx regular");
            }
            catch (final TimeoutException e)
            {
                try
                {
                    terminateTx.set(true);
                    futureTx.get(250, TimeUnit.MILLISECONDS);
//                    System.out.println("futureTx after 250");
                }
                catch (final TimeoutException e2)
                {
                    futureTx.cancel(true);
//                    System.out.println("futureTx cancel");
                }
            }
        
            Thread.sleep(100);
            
            terminateRxIfEnough.set(true);
            
            try
            {
                returnValue = futureRx.get(UPLOAD_MAX_WAIT_SECS, TimeUnit.SECONDS);
//                System.out.println("futureRx regular");
            }
            catch (final TimeoutException e)
            {
                try
                {
                    terminateRxAtAllEvents.set(true);
                    returnValue = futureRx.get(250, TimeUnit.MILLISECONDS);
//                    System.out.println("futureRx after 250");
                }
                catch (final TimeoutException e2)
                {
                    futureRx.cancel(true);
//                    System.out.println("futureRx cancel");
                }
            }
        }
        catch (final ExecutionException e)
        {
            if (e.getCause() instanceof IOException)
                throw (IOException) e.getCause();
            else
                e.printStackTrace();
        }
        
        if (returnValue == null)
            returnValue = true;
        return returnValue;
    }
    
    private Ping ping() throws IOException
    {
        log(String.format(Locale.US, "thread %d: ping test", threadId));
        
        final long pingTimeNs = System.nanoTime();
        
        String line = reader.readLine();
        if (!line.startsWith("ACCEPT "))
        {
            log(String.format(Locale.US, "thread %d: got '%s' expected 'ACCEPT'", threadId, line));
            return null;
        }
        
        final byte[] data = "PING\n".getBytes("US-ASCII");
        final long timeStart = System.nanoTime();
        out.write(data);
        out.flush();
        line = reader.readLine();
        final long timeEnd = System.nanoTime();
        out.write("OK\n".getBytes("US-ASCII"));
        out.flush();
        if (!line.equals("PONG"))
            return null;
        
        line = reader.readLine();
        final Scanner s = new Scanner(line);
        s.findInLine("TIME (\\d+)");
        s.close();
        
        final long diffClient = timeEnd - timeStart;
        final long diffServer = Long.parseLong(s.match().group(1));
        
        final double pingClient = diffClient / 1e6;
        final double pingServer = diffServer / 1e6;
        
        log(String.format(Locale.US, "thread %d - client: %.3f ms ping", threadId, pingClient));
        log(String.format(Locale.US, "thread %d - server: %.3f ms ping", threadId, pingServer));
        return new Ping(diffClient, diffServer, pingTimeNs);
    }
        
    private void setStatus(final TestStatus status)
    {
        if (threadId == 0)
            client.setStatus(status);
    }
    
    private void startTrafficService(final TestStatus status) {
    	client.startTrafficService(threadId, status);
    }
    
    private void stopTrafficService(final TestStatus status) {
    	client.stopTrafficMeasurement(threadId, status);
    }
    
}
