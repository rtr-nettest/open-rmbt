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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public abstract class TestResult
{
    public InetAddress ip_local;
    public InetAddress ip_server;
    public int port_remote;
    public int num_threads;
    public String encryption = "NONE";
    
    public long ping_shortest;
    public long ping_median;
    public String client_version;
    
    public final List<Ping> pings = new ArrayList<Ping>();
    
    public final List<SpeedItem> speedItems = new ArrayList<SpeedItem>();
    
    public static long getSpeedBitPerSec(final long bytes, final long nsec)
    {
        return Math.round((double) bytes / (double) nsec * 1e9 * 8.0);
    }
    
}
