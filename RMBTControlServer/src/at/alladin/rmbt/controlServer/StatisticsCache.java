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
package at.alladin.rmbt.controlServer;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

public class StatisticsCache
{
    private final JCS cache;
    private final static StatisticsCache STATISTICS_CACHE;
    
    static 
    {
        StatisticsCache statisticsCache = null;
        try
        {
            statisticsCache = new StatisticsCache();
        }
        catch (CacheException e)
        {
            e.printStackTrace();
        }
        STATISTICS_CACHE = statisticsCache;
    }
    
    public static StatisticsCache getInstance()
    {
        return STATISTICS_CACHE;
    }
    
    public StatisticsCache() throws CacheException
    {
        cache = JCS.getInstance("at.alladin.rmbt.statistic");
    }
    
    public String get(StatisticParameters params)
    {
        return (String) cache.get(params);
    }
    
    public void put(StatisticParameters params, String result) throws CacheException
    {
        cache.put(params, result);
    }
}
