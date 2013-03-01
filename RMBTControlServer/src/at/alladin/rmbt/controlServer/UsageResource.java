package at.alladin.rmbt.controlServer;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.restlet.resource.Get;

public class UsageResource extends ServerResource
{
    @Get
    public String request(final String entity)
    {
        final StringBuilder result = new StringBuilder();
        
        try
        {
            PreparedStatement ps;
            ResultSet rs;
            String sql;
            
            final String select = "count(uid) count_tests, count(DISTINCT client_id) count_clients, count(DISTINCT client_public_ip) count_ips";
            final String where = "status='FINISHED' AND deleted=false";
            
            sql = String.format("select date_trunc('day', time) _day, %s from test where %s AND time > current_date - interval '30 days' group by _day ORDER by _day DESC", select, where);
            ps = conn.prepareStatement(sql);
            ps.execute();
            
            result.append("Date          #tests  #clients      #ips\n");
            
            rs = ps.getResultSet();
            while (rs.next())
            {
                final Date day = rs.getDate("_day");
                final long countTests = rs.getLong("count_tests");
                final long countClients = rs.getLong("count_clients");
                final long countIPs = rs.getLong("count_ips");
                result.append(String.format("%s: % 8d  % 8d  %8d\n", day, countTests, countClients, countIPs));
            }
            ps.close();
            
            sql = String.format("select %s from test where %s", select, where);
            ps = conn.prepareStatement(sql);
            ps.execute();
            
            result.append("\n");
            
            rs = ps.getResultSet();
            if (rs.next())
            {
                final long countTests = rs.getLong("count_tests");
                final long countClients = rs.getLong("count_clients");
                final long countIPs = rs.getLong("count_ips");
                result.append(String.format("Total:      % 8d  % 8d  %8d\n", countTests, countClients, countIPs));
            }
            ps.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        
        
        
        return result.toString();
    }
}
