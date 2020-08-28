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
package at.rtr.rmbt.controlServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import at.rtr.rmbt.db.DbConnection;
import at.rtr.rmbt.shared.GeoIPHelper;
import at.rtr.rmbt.shared.Helperfunctions;
import at.rtr.rmbt.shared.RevisionHelper;

import com.google.common.net.InetAddresses;

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
        System.out.println("RMBTControlServer - " + RevisionHelper.getVerboseRevision());
        
        scheduler.scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    System.out.println("Removing old IPs");
                    final Connection conn = DbConnection.getConnection();
                    //purge test table
                    PreparedStatement ps = conn.prepareStatement(
                    "UPDATE test SET client_public_ip = NULL, public_ip_rdns = NULL, source_ip = NULL, client_ip_local = NULL, wifi_bssid = NULL, wifi_ssid = NULL "
                    + "WHERE time < NOW() - CAST('4 months' AS INTERVAL) "
                    + "AND (client_public_ip IS NOT NULL OR public_ip_rdns IS NOT NULL OR source_ip IS NOT NULL OR client_ip_local IS NOT NULL OR wifi_bssid IS NOT NULL OR wifi_ssid IS NOT NULL)");
                    ps.executeUpdate();
                    ps.close();
                    //purge ndt table
                    System.out.println("Removing old NDT data");
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
                    System.out.println("Removing old data completed");
                    conn.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }, 1, 24, TimeUnit.HOURS);

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("Looking up missing ASN infos");
                //add AS to all tests of the last two days where lookup failed previously
                try
                {
                    Connection conn = DbConnection.getConnection();
                    final PreparedStatement ps1 = conn.prepareStatement("SELECT uid, client_public_ip from test where public_ip_asn is null and client_public_ip is not null and time > current_date - interval '7 days' order by uid desc LIMIT 10000;");
                    final PreparedStatement ps2 = conn.prepareStatement("UPDATE test SET public_ip_asn = ?, country_asn = ?, public_ip_as_name = ? WHERE uid = ?;");
                    final PreparedStatement ps3 = conn.prepareStatement("SELECT rmbt_set_provider_from_as(uid) from test where  uid = ?;");


                    final ResultSet rs1 = ps1.executeQuery();

                    while (rs1.next())
                    {
                        final long uid = rs1.getLong(1);
                        final String ip = rs1.getString(2);

                        System.out.println("Setting AS for: " + uid);

                        try {
                            InetAddress ipp = InetAddress.getByName(ip);
                            Helperfunctions.ASInformation asn;
                            asn = Helperfunctions.getASInformation(ipp);
                            if (asn != null) {
                                final String asName = asn.getName();
                                final String asCountry = asn.getCountry();
                                ps2.setLong(1, asn.getNumber());
                                ps2.setString(2, asCountry);
                                ps2.setString(3, asName);
                                ps2.setLong(4, uid);

                                ps3.setLong(1, uid);

                                ps2.executeUpdate();
                                ps3.execute();

                                System.out.println("Setting AS: uid: " + uid + " asn:" + asn + " name:" + asName + " country:" + asCountry);
                            }
                        } catch (SQLException e)
                        {
                            e.printStackTrace();
                        }
                        finally {
                            ps2.close();
                            ps3.close();
                        }
                        Thread.sleep(10);
                    }

                    ps1.close();
                    System.out.println("Looking up missing ASN infos completed");
                }
                catch (SQLException | InterruptedException | UnknownHostException | NamingException e)
                {
                    e.printStackTrace();
                }
            }
        }, 2, 24, TimeUnit.HOURS);
    }
}
