package at.alladin.rmbt.controlServer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import at.alladin.rmbt.db.DbConnection;

public class ContextListener implements ServletContextListener
{
    
    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        scheduler.shutdownNow();
    }
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);
    
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        scheduler.scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    System.out.println("Cleaning IPs");
                    final Connection conn = DbConnection.getConnection();
                    
                    PreparedStatement ps = conn.prepareStatement("UPDATE test SET client_public_ip = NULL, public_ip_rdns = NULL WHERE time < NOW() - CAST('4 months' AS INTERVAL) AND (client_public_ip IS NOT NULL OR public_ip_rdns IS NOT NULL)");
                    ps.executeUpdate();
                    ps.close();
                    
                    ps = conn.prepareStatement("UPDATE test_ndt n SET main = NULL, stat = NULL, diag = NULL FROM test t WHERE t.uid = n.test_id AND t.time < NOW() - CAST('4 months' AS INTERVAL) AND (n.main IS NOT NULL OR n.stat IS NOT NULL OR n.diag IS NOT NULL)");
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
