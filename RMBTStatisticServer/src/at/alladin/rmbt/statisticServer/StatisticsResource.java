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
package at.alladin.rmbt.statisticServer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

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
        final int durationDays = params.getDuration();
        final int maxDevices = params.getMaxDevices();
        final String type = params.getType();
        final String networkTypeGroup = params.getNetworkTypeGroup();
        final double accuracy = params.getAccuracy();
        final String country = params.getCountry();
        
        boolean useMobileProvider = false;
        
        final boolean signalMobile;
        final String where;
        if (type.equals("mobile"))
        {
            signalMobile = true;
            useMobileProvider = true;
            
            if (networkTypeGroup == null)
                where = "t.network_group_type = 'MOBILE'";
            else
            {
                if ("2G".equalsIgnoreCase(networkTypeGroup))
                    where = "t.network_group_name = '2G'";
                else if ("3G".equalsIgnoreCase(networkTypeGroup))
                    where = "t.network_group_name = '3G'";
                else if ("4G".equalsIgnoreCase(networkTypeGroup))
                    where = "t.network_group_name = '4G'";
                else if ("mixed".equalsIgnoreCase(networkTypeGroup))
                    where = "t.network_group_name IN ('2G/3G','2G/4G','3G/4G','2G/3G/4G')";
                else
                    where = "1=0";
            }
        }
        else if (type.equals("wifi"))
        {
            where = "t.network_group_type='WLAN'";
            signalMobile = false;
        }
        else if (type.equals("browser"))
        {
            where = "t.network_group_type = 'LAN'";
            signalMobile = false;
        }
        else
        {   // invalid request
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
            answer.put("duration", durationDays);
            answer.put("type", type);
            
            ps = selectProviders(true, quantile, durationDays, accuracy, country, useMobileProvider, where, signalMobile);
            if (!ps.execute())
                return null;
            rs = fillJSON(lang, ps, providers);
            
            ps = selectProviders(false, quantile, durationDays, accuracy, country, useMobileProvider, where, signalMobile);
            if (!ps.execute())
                return null;
            final JSONArray providersSumsArray = new JSONArray();
            rs = fillJSON(lang, ps, providersSumsArray);
            if (providersSumsArray.length() == 1)
                answer.put("providers_sums", providersSumsArray.get(0));
            
            ps = selectDevices(true, quantile, durationDays, accuracy, country, useMobileProvider, where, maxDevices);
            if (!ps.execute())
                return null;
            rs = fillJSON(lang, ps, devices);
            
            ps = selectDevices(false, quantile, durationDays, accuracy, country, useMobileProvider, where, maxDevices);
            if (!ps.execute())
                return null;
            final JSONArray devicesSumsArray = new JSONArray();
            rs = fillJSON(lang, ps, devicesSumsArray);
            if (devicesSumsArray.length() == 1)
                answer.put("devices_sums", devicesSumsArray.get(0));
            
            final JSONArray countries = new JSONArray(getCountries());
            answer.put("countries", countries);
            
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
    
    private Set<String> getCountries() throws SQLException {
    	PreparedStatement ps;
    	Set<String> countries = new TreeSet<>();
		String sql = "WITH RECURSIVE t(n) AS ( "
				+ "SELECT MIN(mobile_network_id) FROM test"
				+ " UNION"
				+ " SELECT (SELECT mobile_network_id FROM test WHERE mobile_network_id > n"
				+ " ORDER BY mobile_network_id LIMIT 1)"
				+ " FROM t WHERE n IS NOT NULL"
				+ " )"
				+ "SELECT upper(mccmnc2name.country) FROM t LEFT JOIN mccmnc2name ON n=mccmnc2name.uid WHERE NOT mccmnc2name.country IS NULL GROUP BY mccmnc2name.country;";
    	
		ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			countries.add(rs.getString(1));
		}
		
    	return countries;
    }
    
    private PreparedStatement selectProviders(final boolean group, final float quantile, final int durationDays, final double accuracy,
            final String country, final boolean useMobileProvider, final String where, final boolean signalMobile) throws SQLException
    {
        PreparedStatement ps;
        String sql = String
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
                        " WHERE %s" +
                        ((country != null && useMobileProvider)? " AND t.network_sim_country = ?" : "") +
                        " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'"+
                        " AND t.time > NOW() - CAST(? AS INTERVAL)" +
                        ((accuracy > 0) ? " AND t.geo_accuracy < ?" : "") + 
                        (group? " GROUP BY p.uid" : "") +
                        " ORDER BY count DESC"
                        , where);
        
        if (country != null) {
        	sql = String
                    .format("SELECT" +
                            ((group && useMobileProvider) ? " p.name AS name, p.shortname AS shortname,  p.mccmnc AS sim_mcc_mnc, " : "") +
                            ((group && !useMobileProvider) ? " public_ip_as_name AS name, public_ip_as_name AS shortname, t.public_ip_asn AS asn,  " : "") +
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
                            (useMobileProvider ? " LEFT JOIN mccmnc2name p ON p.uid = t.mobile_sim_id" : "") + 
                            " WHERE %s" +
                            " AND " + (useMobileProvider?"p.country = ? AND ((t.country_location IS NULL OR t.country_location = ?)  AND (NOT t.roaming_type = 2))":"t.country_geoip = ? ") +
                            " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'"+
                            " AND t.time > NOW() - CAST(? AS INTERVAL)" +
                            ((accuracy > 0) ? " AND t.geo_accuracy < ?" : "") + 
                            ((group && (useMobileProvider))? " GROUP BY p.uid, p.mccmnc" : "") +
                            ((group && (!useMobileProvider))? " GROUP BY t.public_ip_as_name, t.public_ip_asn" : "") +
                            " ORDER BY count DESC"
                            , where);
        }
        
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
        
        if (country != null) {
        	if (useMobileProvider) {
        		ps.setString(i++, country.toLowerCase()); //mccmnc2name.country
        		ps.setString(i++, country.toUpperCase()); //country_location
        	}
        	else {
        		ps.setString(i++, country.toUpperCase());
        	}
        }
        
        ps.setString(i++, String.format("%d days", durationDays));
        
        if (accuracy>0) {
        	ps.setDouble(i++, accuracy);
        }

        System.out.println(ps);
        
        return ps;
    }
    
    private PreparedStatement selectDevices(final boolean group, final float quantile, final int durationDays, final double accuracy,
            final String country, final boolean useMobileProvider, final String where, final int maxDevices) throws SQLException
    {
        PreparedStatement ps;
        String sql = String.format("SELECT" +
                (group ? " COALESCE(adm.fullname, t.model) model," : "") +
                " count(t.uid) count," + " quantile(speed_download, ?) quantile_down," +
                " quantile(speed_upload, ?) quantile_up," +
                " quantile(ping_shortest, ?) quantile_ping" +
                " FROM test t" +
                " LEFT JOIN device_map adm ON adm.codename=t.model" +
                " WHERE %s" +
                " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
                " AND time > NOW() - CAST(? AS INTERVAL)" +
                (useMobileProvider ? " AND t.mobile_provider_id IS NOT NULL" : "") +
                ((accuracy > 0) ? " AND t.geo_accuracy < ?" : "") + 
                (group ? " GROUP BY COALESCE(adm.fullname, t.model) HAVING count(t.uid) > 10" : "") +
                " ORDER BY count DESC" +
                " LIMIT %d", where, maxDevices);
        if (country != null) {
        	sql = String.format("SELECT" +
                    (group ? " COALESCE(adm.fullname, t.model) model," : "") +
                    " count(t.uid) count," + " quantile(speed_download, ?) quantile_down," +
                    " quantile(speed_upload, ?) quantile_up," +
                    " quantile(ping_shortest, ?) quantile_ping" +
                    " FROM test t" +
                    " LEFT JOIN device_map adm ON adm.codename=t.model" +
                    (useMobileProvider ? " LEFT JOIN mccmnc2name p ON p.uid = t.mobile_sim_id" : "") +
                    " WHERE %s" +
                    " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
                    " AND time > NOW() - CAST(? AS INTERVAL)" +
                    " AND " + (useMobileProvider?"p.country = ? AND ((t.country_location IS NULL OR t.country_location = ?)  AND (NOT t.roaming_type = 2))":"t.country_geoip = ? ") +
                    ((accuracy > 0) ? " AND t.geo_accuracy < ?" : "") + 
                    (group ? " GROUP BY COALESCE(adm.fullname, t.model) HAVING count(t.uid) > 10" : "") +
                    " ORDER BY count DESC" +
                    " LIMIT %d", where, maxDevices);
        }
        
        ps = conn.prepareStatement(sql);
        System.out.println(ps);
        
        int i = 1;
        for (int j = 0; j < 2; j++)
            ps.setFloat(i++, quantile);
        ps.setFloat(i++, 1 - quantile); // inverse for ping
        
        ps.setString(i++, String.format("%d days", durationDays));
        
        if (country != null) {       	
        	if (useMobileProvider) {
        		ps.setString(i++, country.toLowerCase()); //mccmnc2name.country
        		ps.setString(i++, country.toUpperCase()); //country_location
        	}
        	else {
        		ps.setString(i++, country.toUpperCase());
        	}
        }
        
        if (accuracy>0) {
        	ps.setDouble(i++, accuracy);
        }
        
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
