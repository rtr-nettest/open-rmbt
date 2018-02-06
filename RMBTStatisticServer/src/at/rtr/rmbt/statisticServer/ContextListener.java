/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
 * Copyright 2013-2015 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
package at.rtr.rmbt.statisticServer;

import com.google.common.collect.TreeMultimap;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import at.rtr.rmbt.shared.RevisionHelper;

public class ContextListener implements ServletContextListener
{
    
    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        scheduler.shutdownNow();
    }
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        System.out.println("RMBTStatisticServer - " + RevisionHelper.getVerboseRevision());

        //re-generate the most widely used statistics
        scheduler.scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                Logger.getLogger(ContextListener.class.getName()).info("renewing /statistics cache");
                Queue<String> lastRequests = StatisticsResource.getLastRequests();

                Collection<String> asList = new ArrayList<>(lastRequests);

                HashMap<String, AtomicInteger> asOccuranceMap = new HashMap<>();
                for (String entry : asList) {
                    if (asOccuranceMap.containsKey(entry)) {
                        asOccuranceMap.get(entry).incrementAndGet();
                    }
                    else {
                        asOccuranceMap.put(entry,new AtomicInteger());
                    }
                }

                Logger.getLogger(ContextListener.class.getName()).info(asOccuranceMap.keySet().size() + " unique urls in cache");

                //sort
                TreeMultimap<Integer, String> asSortedMap = TreeMultimap.create(Collections.<Integer>reverseOrder(), Collections.<String>reverseOrder());
                for (Map.Entry<String, AtomicInteger> entry : asOccuranceMap.entrySet()) {
                    asSortedMap.put(entry.getValue().get(),entry.getKey());
                }

                asList = asSortedMap.values();

                //print
                int i=0;
                for (String entry : asList) {
                    Logger.getLogger(ContextListener.class.getName()).info("#" + i + " (" + (asOccuranceMap.get(entry).get() + 1) + " entries) -> " + entry);
                    if (i <= 50) {
                        StatisticsResource.generateStatistics(new JSONObject(entry), true);
                        i++;
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }, 1, 60, TimeUnit.MINUTES);

    }
}
