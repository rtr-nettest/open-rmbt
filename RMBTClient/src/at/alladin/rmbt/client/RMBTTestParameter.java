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

public class RMBTTestParameter
{
    
    // immutable! (accessed by multiple threads!)
    
    private final String host;
    private final int port;
    private final boolean encryption;
    private final String token;
    private final int pretestDuration = 2;
    private final int duration;
    private final int numThreads;
    private final int numPings;
    private final long startTime;
    
    public RMBTTestParameter(final String host, final int port, final boolean encryption, final String token,
            final int duration, final int numThreads, final int numPings, final long startTime)
    {
        super();
        this.host = host;
        this.port = port;
        this.encryption = encryption;
        this.token = token;
        this.duration = duration;
        this.numThreads = numThreads;
        this.numPings = numPings;
        this.startTime = startTime;
    }
    
    public RMBTTestParameter(final String host, final int port, final boolean encryption,
            final int duration, final int numThreads, final int numPings)
    {
        super();
        this.host = host;
        this.port = port;
        this.encryption = encryption;
        this.duration = duration;
        this.numThreads = numThreads;
        this.numPings = numPings;
        this.token = null;
        this.startTime = 0;
    }
    
    public String getHost()
    {
        return host;
    }
    
    public int getPort()
    {
        return port;
    }
    
    public boolean isEncryption()
    {
        return encryption;
    }
    
    public String getToken()
    {
        return token;
    }
    
    public String getUUID()
    {
        if (token == null)
            return null;
        final String[] parts = token.split("_");
        if (parts == null || parts.length <= 0)
            return null;
        return parts[0];
    }
    
    public int getDuration()
    {
        return duration;
    }
    
    public int getPretestDuration()
    {
        return pretestDuration;
    }
    
    public int getNumThreads()
    {
        return numThreads;
    }
    
    public int getNumPings()
    {
        return numPings;
    }
    
    public long getStartTime()
    {
        return startTime;
    }

    public void check() throws IllegalArgumentException
    {
        if (host == null || host.length() == 0)
            throw new IllegalArgumentException("no host");
        if (port <= 0)
            throw new IllegalArgumentException("no port");
        if (getUUID() == null)
            throw new IllegalArgumentException("no uuid");
        if (numThreads <= 0)
            throw new IllegalArgumentException("num threads <= 0");
//        if (pretestDuration < 0)
//            throw new IllegalArgumentException("pretestDuration < 0");
    }
}
