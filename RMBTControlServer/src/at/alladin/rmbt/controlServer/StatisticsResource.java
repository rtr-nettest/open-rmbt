/*******************************************************************************
 * Copyright 2013 alladin-IT OG
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.jcs.access.exception.CacheException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import at.alladin.rmbt.shared.Classification;

public class StatisticsResource extends ServerResource
{
    @Get
    @Post("json")
    public String request(final String entity)
    {
        addAllowOrigin();
        
        final StatisticParameters params = new StatisticParameters(settings.getString("RMBT_DEFAULT_LANGUAGE"), entity);
        
        final StatisticsCache cache = StatisticsCache.getInstance();
        String result = cache.get(params);
        if (result != null)
        {
            System.out.println("cache hit");
            return result; // cache hit
        }
        System.out.println("not in cache");
        
        final String lang = params.getLang();
        final float quantile = params.getQuantile();
        final int months = params.getMonths();
        final int maxDevices = params.getMaxDevices();
        final String type = params.getType();
        final String networkTypeGroup = params.getNetworkTypeGroup();
        
        boolean useMobileProvider = false;
        
        final boolean signalMobile;
        boolean needNetworkTypeJoin = false;
        final String where;
        if (type.equals("mobile"))
        {
            signalMobile = true;
            useMobileProvider = true;
            
            if (networkTypeGroup == null)
                where = "t.network_type not in (0, 97, 98, 99)";
            else
            {
                needNetworkTypeJoin = true;
                if ("2G".equalsIgnoreCase(networkTypeGroup))
                    where = "nt.group_name = '2G'";
                else if ("3G".equalsIgnoreCase(networkTypeGroup))
                    where = "nt.group_name = '3G'";
                else if ("4G".equalsIgnoreCase(networkTypeGroup))
                    where = "nt.group_name = '4G'";
                else if ("mixed".equalsIgnoreCase(networkTypeGroup))
                    where = "nt.group_name NOT IN ('2G','3G','4G') AND t.network_type not in (0, 97, 98, 99)";
                else
                    where = "1=0";
            }
        }
        else if (type.equals("wifi"))
        {
            where = "t.network_type = 99";
            signalMobile = false;
        }
        else if (type.equals("browser"))
        {
            where = "t.network_type = 98";
            signalMobile = false;
        }
        else
        {
            where = "1=0";
            signalMobile = false;
        }
        
        final JSONObject answer = new JSONObject();
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            final JSONArray providers = new JSONArray();
            answer.put("providers", providers);
            final JSONArray devices = new JSONArray();
            answer.put("devices", devices);
            answer.put("quantile", quantile);
            answer.put("months", months);
            answer.put("type", type);
            
            ps = selectProviders(true, quantile, months, useMobileProvider, where, signalMobile, needNetworkTypeJoin);
            if (!ps.execute())
                return null;
            rs = fillJSON(lang, ps, providers);
            
            ps = selectProviders(false, quantile, months, useMobileProvider, where, signalMobile, needNetworkTypeJoin);
            if (!ps.execute())
                return null;
            final JSONArray providersSumsArray = new JSONArray();
            rs = fillJSON(lang, ps, providersSumsArray);
            if (providersSumsArray.length() == 1)
                answer.put("providers_sums", providersSumsArray.get(0));
            
            ps = selectDevices(true, quantile, months, useMobileProvider, where, maxDevices, needNetworkTypeJoin);
            if (!ps.execute())
                return null;
            rs = fillJSON(lang, ps, devices);
            
            ps = selectDevices(false, quantile, months, useMobileProvider, where, maxDevices, needNetworkTypeJoin);
            if (!ps.execute())
                return null;
            final JSONArray devicesSumsArray = new JSONArray();
            rs = fillJSON(lang, ps, devicesSumsArray);
            if (devicesSumsArray.length() == 1)
                answer.put("devices_sums", devicesSumsArray.get(0));
            
            
            result = answer.toString();
            cache.put(params, result); // put in cache
            return result;
        }
        catch (final JSONException e)
        {
            e.printStackTrace();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
        catch (CacheException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
            }
            catch (final SQLException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    private PreparedStatement selectProviders(final boolean group, final float quantile, final int months,
            final boolean useMobileProvider, final String where, final boolean signalMobile, final boolean needNetworkTypeJoin) throws SQLException
    {
        PreparedStatement ps;
        final String sql = String
                .format("SELECT" +
                        (group ? " p.name, p.shortname, " : "") +
                        " count(t.uid) count," +
                        " quantile(speed_download, ?) quantile_down," +
                        " quantile(speed_upload, ?) quantile_up," +
                        " quantile(signal_strength, ?) quantile_signal," +
                        " quantile(ping_shortest, ?) quantile_ping," +
                        
                        " sum((speed_download >= ?)::int)::double precision / count(speed_download) down_green," +
                        " sum((speed_download < ? and speed_download >= ?)::int)::double precision / count(speed_download) down_yellow," +
                        " sum((speed_download < ?)::int)::double precision / count(speed_download) down_red," +
                                                
                        " sum((speed_upload >= ?)::int)::double precision / count(speed_upload) up_green," +
                        " sum((speed_upload < ? and speed_upload >= ?)::int)::double precision / count(speed_upload) up_yellow," +
                        " sum((speed_upload < ?)::int)::double precision / count(speed_upload) up_red," +
                        
                        " sum((signal_strength >= ?)::int)::double precision / count(signal_strength) signal_green," +
                        " sum((signal_strength < ? and signal_strength >= ?)::int)::double precision / count(signal_strength) signal_yellow," +
                        " sum((signal_strength < ?)::int)::double precision / count(signal_strength) signal_red," + 
                        
                        " sum((ping_shortest <= ?)::int)::double precision / count(ping_shortest) ping_green," +
                        " sum((ping_shortest > ? and ping_shortest <= ?)::int)::double precision / count(ping_shortest) ping_yellow," +
                        " sum((ping_shortest > ?)::int)::double precision / count(ping_shortest) ping_red" +
                        
                        " FROM test t" +
                        " JOIN provider p ON" + 
                        (useMobileProvider ? " t.mobile_provider_id = p.uid" : " t.provider_id = p.uid") +
                        (needNetworkTypeJoin ? " LEFT JOIN network_type nt ON t.network_type=nt.uid" : "") +
                        
                        " WHERE %s" +
                        " AND t.deleted = false AND t.status = 'FINISHED'" +
                        " AND t.time > NOW() - CAST(? AS INTERVAL)" +
                        " AND t.time > '2012-10-26'" +
                        (group ? " GROUP BY p.uid" : "") +
                        " ORDER BY count DESC"
                        , where);
        
        ps = conn.prepareStatement(sql);
        
        int i = 1;
        for (int j = 0; j < 3; j++)
            ps.setFloat(i++, quantile);
        ps.setFloat(i++, 1 - quantile); // inverse for ping
        
        final int[] td = Classification.THRESHOLD_DOWNLOAD;
        ps.setInt(i++, td[0]);
        ps.setInt(i++, td[0]);
        ps.setInt(i++, td[1]);
        ps.setInt(i++, td[1]);
        
        final int[] tu = Classification.THRESHOLD_UPLOAD;
        ps.setInt(i++, tu[0]);
        ps.setInt(i++, tu[0]);
        ps.setInt(i++, tu[1]);
        ps.setInt(i++, tu[1]);
        
        final int[] ts = signalMobile ? Classification.THRESHOLD_SIGNAL_MOBILE : Classification.THRESHOLD_SIGNAL_WIFI;
        ps.setInt(i++, ts[0]);
        ps.setInt(i++, ts[0]);
        ps.setInt(i++, ts[1]);
        ps.setInt(i++, ts[1]);
        
        final int[] tp = Classification.THRESHOLD_PING;
        ps.setInt(i++, tp[0]);
        ps.setInt(i++, tp[0]);
        ps.setInt(i++, tp[1]);
        ps.setInt(i++, tp[1]);
        
        ps.setString(i++, String.format("%d months", months));

//        System.out.println(ps);
        
        return ps;
    }
    
    private PreparedStatement selectDevices(final boolean group, final float quantile, final int months,
            final boolean useMobileProvider, final String where, final int maxDevices, final boolean needNetworkTypeJoin) throws SQLException
    {
        PreparedStatement ps;
        final String sql = String.format("SELECT" +
                (group ? " COALESCE(adm.fullname, t.model) model," : "") +
                " count(t.uid) count," + " quantile(speed_download, ?) quantile_down," +
                " quantile(speed_upload, ?) quantile_up," +
                " quantile(ping_shortest, ?) quantile_ping" +
                " FROM test t" +
                " LEFT JOIN android_device_map adm ON adm.codename=t.model" +
                (needNetworkTypeJoin ? " LEFT JOIN network_type nt ON t.network_type=nt.uid" : "") +
                " WHERE %s" +
                " AND t.deleted = false AND t.status = 'FINISHED'" +
                " AND time > NOW() - CAST(? AS INTERVAL)" +
                " AND time > '2012-10-26'" +
                (useMobileProvider ? " AND t.mobile_provider_id IS NOT NULL" : "") +
                (group ? " GROUP BY COALESCE(adm.fullname, t.model) HAVING count(t.uid) > 10" : "") +
                " ORDER BY count DESC" +
                " LIMIT %d", where, maxDevices);
        ps = conn.prepareStatement(sql);
        System.out.println(ps);
        
        int i = 1;
        for (int j = 0; j < 2; j++)
            ps.setFloat(i++, quantile);
        ps.setFloat(i++, 1 - quantile); // inverse for ping
        
        ps.setString(i++, String.format("%d months", months));
        
        return ps;
    }
    
    private static ResultSet fillJSON(final String lang, final PreparedStatement ps, final JSONArray providers)
            throws SQLException, JSONException
    {
        ResultSet rs;
        rs = ps.getResultSet();
        final ResultSetMetaData metaData = rs.getMetaData();
        final int columnCount = metaData.getColumnCount();
        while (rs.next())
        {
            final JSONObject obj = new JSONObject();
            for (int j = 1; j <= columnCount; j++)
            {
                final String colName = metaData.getColumnName(j);
                Object data = rs.getObject(j);
                if (colName.equals("name") && data == null)
                    if (lang != null && lang.equals("de"))
                        data = "Andere Betreiber";
                    else
                        data = "Other operators";
                if (colName.equals("shortname") && data == null) {
                    if (lang != null && lang.equals("de"))
                            data = "Andere";
                        else
                            data = "Others";
                }
                obj.put(colName, data);
            }
            providers.put(obj);
        }
        return rs;
    }
}
