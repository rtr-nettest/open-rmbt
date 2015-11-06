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
package at.alladin.rmbt.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Scanner;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import at.alladin.rmbt.client.helper.Config;

public abstract class AbstractRMBTTest {
    protected static final String EXPECT_GREETING = Config.RMBT_VERSION_STRING;
	
    protected final RMBTClient client;
    protected final RMBTTestParameter params;
    protected final int threadId;

    protected InputStreamCounter in;
    protected BufferedReader reader;
    protected OutputStreamCounter out;
    
    protected long totalDown;
    protected long totalUp;
    protected int chunksize;
    protected byte[] buf;

    
    /**
     * 
     * @param client
     * @param params
     * @param threadId
     */
    public AbstractRMBTTest(RMBTClient client, RMBTTestParameter params, int threadId) {
    	this.threadId = threadId;
    	this.client = client;
    	this.params = params;
    }

    protected Socket getSocket(final String host, final int port, final boolean isSecure, final int timeOut) throws UnknownHostException, IOException
    {
    	InetSocketAddress sockAddr = new InetSocketAddress(host, port);
    	
    	final Socket socket;
    	
        if (client.getSslSocketFactory() != null && isSecure)
        {
            socket = client.getSslSocketFactory().createSocket();
        }
        else {
            socket = new Socket();
        }
        
        if (socket != null) {
        	System.out.println("Connecting to " + sockAddr + " with timout: " + timeOut + "ms " + socket + " [SSL: " + isSecure + "]");
        	socket.connect(sockAddr, timeOut);
        }
        
    	return socket;
    }

    protected Socket connect(final TestResult testResult, final InetAddress host, final int port, final String protocolVersion, final String response, final boolean isSecure, final int connTimeOut) throws IOException {
        log(String.format(Locale.US, "thread %d: connecting...", threadId));
        
        final InetAddress inetAddress = host;
        
        final Socket s = getSocket(inetAddress.getHostAddress(), port, isSecure, connTimeOut);
        s.setSoTimeout(12000);
        
        if (testResult != null) {
        	testResult.ip_local = s.getLocalAddress();
        	testResult.ip_server = s.getInetAddress();
        	testResult.port_remote = s.getPort();
        }
        
        if (s instanceof SSLSocket)
        {
            final SSLSocket sslSocket = (SSLSocket) s;
            final SSLSession session = sslSocket.getSession();
            if (testResult != null) {
            	testResult.encryption = String.format(Locale.US, "%s (%s)", session.getProtocol(), session.getCipherSuite());
            }
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
        if (!line.equals(protocolVersion))
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
        	if (response.equals("CHUNKSIZE")) {
                if (!response.equals(scanner.next()))
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
                
                s.setSoTimeout(0);
                return s;        		
        	}
        	else {
        		log(String.format(Locale.US, "thread %d: got '%s'", threadId, line));
        		s.setSoTimeout(0);
        		return s;
        	}

        }
        finally
        {
            scanner.close();
        }    	
    }
    
    protected Socket connect(final TestResult testResult) throws IOException
    {
    	return connect(testResult, InetAddress.getByName(params.getHost()), params.getPort(), EXPECT_GREETING, "CHUNKSIZE", true, 20000);
    }
    
    /**
     * 
     * @param message
     * @return
     * @throws IOException 
     * @throws UnsupportedEncodingException 
     */
    protected void sendMessage(final String message) throws UnsupportedEncodingException, IOException {
        String send;
        send = String.format(Locale.US, message);        	

        System.out.println("sending command (thread " + Thread.currentThread().getId() + "): " + send);
		out.write(send.getBytes("US-ASCII"));
        out.flush();
    }
    
    protected void log(final CharSequence text)
    {
        client.log(text);
    }

}
