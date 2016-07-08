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
package at.alladin.rmbt.controlServer;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import at.alladin.rmbt.db.DbConnection;
import at.alladin.rmbt.shared.GeoIPHelper;
import at.alladin.rmbt.shared.RevisionHelper;

import com.google.common.net.InetAddresses;

public class ContextListener implements ServletContextListener
{
    
    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        scheduler.shutdownNow();
    }
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @SuppressWarnings("unused")
    private void getGeoIPs()
    {
        scheduler.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    System.out.println("getting geoips");
                    final Connection conn = DbConnection.getConnection();
                    
                    final boolean oldAutoCommitState = conn.getAutoCommit();
                    conn.setAutoCommit(false);
                    // allow update only 2min after test was started
                    final PreparedStatement psUpd = conn.prepareStatement("UPDATE test SET country_geoip=? WHERE uid=? and (now() - time  < interval '2' minute)");
                    final PreparedStatement ps = conn.prepareStatement("SELECT uid,client_public_ip FROM test WHERE client_public_ip IS NOT NULL AND country_geoip IS NULL");
                    ps.execute();
                    final ResultSet rs = ps.getResultSet();
                    int count = 0;
                    while (rs.next())
                    {
                        Thread.sleep(5);
                        count++;
                        if ((count % 1000) == 0)
                            System.out.println(count + " geoips updated");
                        final long uid = rs.getLong("uid");
                        final String ip = rs.getString("client_public_ip");
                        final InetAddress ia = InetAddresses.forString(ip);
                        final String country = GeoIPHelper.lookupCountry(ia);
                        if (country != null)
                        {
                            psUpd.setString(1, country);
                            psUpd.setLong(2, uid);
                            psUpd.executeUpdate();
                        }
                    }
                    psUpd.close();
                    ps.close();
                    conn.commit();
                    conn.setAutoCommit(oldAutoCommitState);
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
    
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        System.out.println("RMBTControlServer - " + RevisionHelper.getVerboseRevision());
        
        scheduler.scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    System.out.println("Cleaning IPs");
                    final Connection conn = DbConnection.getConnection();
                    //purge test table
                    PreparedStatement ps = conn.prepareStatement(
                    "UPDATE test SET client_public_ip = NULL, public_ip_rdns = NULL, source_ip = NULL, client_ip_local = NULL "
                    + "WHERE time < NOW() - CAST('4 months' AS INTERVAL) "
                    + "AND (client_public_ip IS NOT NULL OR public_ip_rdns IS NOT NULL OR source_ip IS NOT NULL OR client_ip_local IS NOT NULL)");
                    ps.executeUpdate();
                    ps.close();
                    //purge ndt table
                    ps = conn.prepareStatement("UPDATE test_ndt n SET main = NULL, stat = NULL, diag = NULL FROM test t "
                    		+ "WHERE t.uid = n.test_id AND t.time < NOW() - CAST('4 months' AS INTERVAL) "
                    		+ "AND (n.main IS NOT NULL OR n.stat IS NOT NULL OR n.diag IS NOT NULL)");
                    ps.executeUpdate();
                    ps.close();
                    //purge status table
                    ps = conn.prepareStatement("UPDATE status SET ip = NULL "
                    		+ "WHERE time < NOW() - CAST('4 months' AS INTERVAL) "
                    		+ "AND (ip IS NOT NULL)");
                    ps.executeUpdate();
                    ps.close();
                     
                    conn.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }, 0, 24, TimeUnit.HOURS);
    }
}
