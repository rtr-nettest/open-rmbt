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
package at.alladin.rmbt.shared.cache;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.ConnectionObserver;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.BulkFuture;
import net.spy.memcached.internal.GetFuture;

import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

public class CacheHelper implements ConnectionObserver
{
    private static final String PREFIX_TS = "ts:";

    private static final CacheHelper instance = new CacheHelper();

    private static final long TIMEOUT = 500; // 0.5 seconds
    
    private static HashFunction keyHash = Hashing.murmur3_128();
    private static BaseEncoding keyEncoding = BaseEncoding.base64();
    
    private final AtomicReference<MemcachedClient> memcachedClient = new AtomicReference<>();
    private final AtomicBoolean memcachedActive = new AtomicBoolean(false);

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    public static CacheHelper getInstance()
    {
        return instance;
    }
    
    public void initMemcached(String addresses) // addresses: space sep. like 'localhost:11211 10.10.10.10:1234'
    {
        if (memcachedClient.get() != null)
            throw new IllegalStateException("memcached already initialized");
        try
        {
            final MemcachedClient _memcachedClient = new MemcachedClient(
                    new BinaryConnectionFactory(),
                    AddrUtil.getAddresses(addresses));
            _memcachedClient.addObserver(this);
            memcachedClient.set(_memcachedClient);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public ExecutorService getExecutor()
    {
        return executor;
    }
    
    public static class ObjectWithTimestamp
    {
        public final Object o;
        public final Long ts;
        public final boolean stale;
        public ObjectWithTimestamp(Object o, Long ts, boolean stale)
        {
            this.o = o;
            this.ts = ts;
            this.stale = stale;
        }
    }
    
    public boolean isActive()
    {
        return (memcachedClient.get() != null && memcachedActive.get());
    }
    
    public Object get(String key)
    {
        if (! isActive())
            return null;
        
        Object result = null;
        final GetFuture<Object> f = memcachedClient.get().asyncGet(key);
        try
        {
            result = f.get(TIMEOUT, TimeUnit.MILLISECONDS);
        }
        catch (Exception e)
        {
            f.cancel(true);
        }
        return result;
//        return memcachedClient.get(key); // sync version
    }
    
    public ObjectWithTimestamp getWithTimestamp(String key, int staleExp) //staleExp: seconds
    {
        if (! isActive())
            return null;
        
        final String tsKey = PREFIX_TS + key;
        final BulkFuture<Map<String, Object>> f = memcachedClient.get().asyncGetBulk(key, tsKey);
        try
        {
            final Map<String, Object> map = f.get(TIMEOUT, TimeUnit.MILLISECONDS);
            Object result = map.get(key);
            if (result == null)
                return null;
            final Long ts = (Long)map.get(tsKey);
            final boolean stale;
            if (ts == null)
                stale = true; // if there is no ts, we assume stale
            else
            {
                final long now = System.currentTimeMillis();
                stale = (ts + (staleExp * 1000) <= now);
            }
            return new ObjectWithTimestamp(result, ts, stale);
        }
        catch (Exception e)
        {
            f.cancel(true);
            return null;
        }
    }
    
//    public MemcachedClient getMemcachedClient()
//    {
//        return memcachedClient;
//    }
    
    public static <T extends Funnel<T>> String getHash(T object)
    {
        return keyEncoding.encode(keyHash.hashObject(object, object).asBytes());
    }

    public Future<Boolean> set(String key, int exp, Object o)
    {
        return set(key, exp, o, false);
    }
    
    public Future<Boolean> set(String key, int exp, Object o, boolean addTimestamp)
    {
        if (! isActive())
            return null;
        if (addTimestamp)
            memcachedClient.get().set(PREFIX_TS + key, exp, System.currentTimeMillis());
        return memcachedClient.get().set(key, exp, o);
    }

    protected void updateMemcachedActive()
    {
        final int numAvail = memcachedClient.get().getAvailableServers().size();
        memcachedActive.set(numAvail >= 1);
    }
    
    @Override
    public void connectionEstablished(SocketAddress addr, int arg1)
    {
        updateMemcachedActive();
    }

    @Override
    public void connectionLost(SocketAddress addr)
    {
        updateMemcachedActive();
    }
}
