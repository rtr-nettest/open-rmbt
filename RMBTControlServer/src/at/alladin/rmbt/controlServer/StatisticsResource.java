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
        
        String lang = settings.getString("RMBT_DEFAULT_LANGUAGE");
        
        float quantile = 0.8f;
        int months = 2;
        int maxDevices = 50;
        String type = "mobile";
        String networkTypeGroup = null;
        
        if (entity != null && !entity.isEmpty())
            // try parse the string to a JSON object
            try
            {
                final JSONObject request = new JSONObject(entity);
                lang = request.optString("language");
                
                final double _quantile = request.optDouble("quantile", Double.NaN);
                if (_quantile >= 0 && _quantile <= 1)
                    quantile = (float) _quantile;
                
                final int _months = request.optInt("months", 0);
                if (_months > 0)
                    months = _months;
                
                final int _maxDevices = request.optInt("max_devices", 0);
                if (_maxDevices > 0)
                    maxDevices = _maxDevices;
                
                final String _type = request.optString("type", null);
                if (_type != null)
                    type = _type;
                
                final String _networkTypeGroup = request.optString("network_type_group", null);
                if (_networkTypeGroup != null && ! _networkTypeGroup.equalsIgnoreCase("all"))
                    networkTypeGroup = _networkTypeGroup;
            }
            catch (final JSONException e)
            {
            }
        
        boolean useMccMnc = false;
        
        final boolean signalMobile;
        boolean needNetworkTypeJoin = false;
        final String where;
        if (type.equals("mobile"))
        {
            signalMobile = true;
            useMccMnc = true;
            
            if (networkTypeGroup == null)
                where = "t.network_type not in (0, 98, 99)";
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
                    where = "nt.group_name NOT IN ('2G','3G','4G') AND t.network_type not in (0, 98, 99)";
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
            
            ps = selectProviders(true, quantile, months, useMccMnc, where, signalMobile, needNetworkTypeJoin);
            if (!ps.execute())
                return null;
            rs = fillJSON(lang, ps, providers);
            
            ps = selectProviders(false, quantile, months, useMccMnc, where, signalMobile, needNetworkTypeJoin);
            if (!ps.execute())
                return null;
            final JSONArray providersSumsArray = new JSONArray();
            rs = fillJSON(lang, ps, providersSumsArray);
            if (providersSumsArray.length() == 1)
                answer.put("providers_sums", providersSumsArray.get(0));
            
            ps = selectDevices(true, quantile, months, where, maxDevices, needNetworkTypeJoin);
            if (!ps.execute())
                return null;
            rs = fillJSON(lang, ps, devices);
            
            ps = selectDevices(false, quantile, months, where, maxDevices, needNetworkTypeJoin);
            if (!ps.execute())
                return null;
            final JSONArray devicesSumsArray = new JSONArray();
            rs = fillJSON(lang, ps, devicesSumsArray);
            if (devicesSumsArray.length() == 1)
                answer.put("devices_sums", devicesSumsArray.get(0));
            
            return answer.toString(4);
        }
        catch (final JSONException e)
        {
            e.printStackTrace();
        }
        catch (final SQLException e)
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
            final boolean useMccMnc, final String where, final boolean signalMobile, final boolean needNetworkTypeJoin) throws SQLException
    {
        PreparedStatement ps;
        final String sql = String
                .format("SELECT" +
                        (group ? " p.name," : "") +
                        " count(t.uid) count," +
                        " quantile(speed_download, ?) quantile_down," +
                        " quantile(speed_upload, ?) quantile_up," +
                        " quantile(signal_strength, ?) quantile_signal," +
                        " quantile(ping_shortest, ?) quantile_ping," +
                        
                        " sum((speed_download >= ?)::int)::double precision / count(t.uid) down_green," +
                        " sum((speed_download < ? and speed_download >= ?)::int)::double precision / count(t.uid) down_yellow," +
                        " sum((speed_download < ?)::int)::double precision / count(t.uid) down_red," +
                                                
                        " sum((speed_upload >= ?)::int)::double precision / count(t.uid) up_green," +
                        " sum((speed_upload < ? and speed_upload >= ?)::int)::double precision / count(t.uid) up_yellow," +
                        " sum((speed_upload < ?)::int)::double precision / count(t.uid) up_red," +
                        
                        " sum((signal_strength >= ?)::int)::double precision / count(t.uid) signal_green," +
                        " sum((signal_strength < ? and signal_strength >= ?)::int)::double precision / count(t.uid) signal_yellow," +
                        " sum((signal_strength < ?)::int)::double precision / count(t.uid) signal_red," + 
                        
                        " sum((ping_shortest <= ?)::int)::double precision / count(t.uid) ping_green," +
                        " sum((ping_shortest > ? and ping_shortest <= ?)::int)::double precision / count(t.uid) ping_yellow," +
                        " sum((ping_shortest > ?)::int)::double precision / count(t.uid) ping_red" +
                        
                        " FROM test t" +
                        " LEFT JOIN provider p ON" + 
                        (useMccMnc ? " t.network_operator=p.mcc_mnc" : " t.provider_id = p.uid") +
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
            final String where, final int maxDevices, final boolean needNetworkTypeJoin) throws SQLException
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
                (group ? " GROUP BY COALESCE(adm.fullname, t.model)" : "") +
                " ORDER BY count DESC" +
                " LIMIT %d", where, maxDevices);
        // System.out.println(sql);
        ps = conn.prepareStatement(sql);
        
        int i = 1;
        for (int j = 0; j < 2; j++)
            ps.setFloat(i++, quantile);
        ps.setFloat(i++, 1 - quantile); // inverse for ping
        
        ps.setString(i++, String.format("%d months", months));
        
        return ps;
    }
    
    private ResultSet fillJSON(final String lang, final PreparedStatement ps, final JSONArray providers)
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
                obj.put(colName, data);
            }
            providers.put(obj);
        }
        return rs;
    }
}
