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
