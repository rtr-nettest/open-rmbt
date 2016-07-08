/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.naming.NamingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import at.alladin.rmbt.db.DbConnection;
import at.alladin.rmbt.shared.Classification;
import at.alladin.rmbt.shared.cache.CacheHelper;
import at.alladin.rmbt.shared.cache.CacheHelper.ObjectWithTimestamp;

public class StatisticsResource extends ServerResource
{
    private static final int CACHE_STALE = 3600;
    private static final int CACHE_EXPIRE = 7200;
    private static final boolean ONLY_PINNED = true;
    
    private final CacheHelper cache = CacheHelper.getInstance();

    @Get
    @Post("json")
    public String request(final String entity)
    {
        addAllowOrigin();
        
        final StatisticParameters params = new StatisticParameters(settings.getString("RMBT_DEFAULT_LANGUAGE"), entity);
        
        final String cacheKey = CacheHelper.getHash(params);
        final ObjectWithTimestamp cacheObject = cache.getWithTimestamp(cacheKey, CACHE_STALE);
        if (cacheObject != null)
        {
            final String result = (String)cacheObject.o;
            System.out.println("cache hit");
            if (cacheObject.stale)
            {
                final Runnable refreshCacheRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        System.out.println("adding in background: " + cacheKey);
                        final String result = generateStatistics(params, cacheKey);
                        if (result != null)
                            cache.set(cacheKey, CACHE_EXPIRE, result, true);
                    }
                };
                cache.getExecutor().execute(refreshCacheRunnable);
            }
            return result; // cache hit
        }
        System.out.println("not in cache");
        
        final String result = generateStatistics(params, cacheKey);
        if (result != null)
            cache.set(cacheKey, CACHE_EXPIRE, result, true);
        return result;
    }

    private static String generateStatistics(final StatisticParameters params, final String cacheKey)
    {
        String result;
        final String lang = params.getLang();
        final float quantile = params.getQuantile();
        final int durationDays = params.getDuration();
        final int maxDevices = params.getMaxDevices();
        final String type = params.getType();
        final String networkTypeGroup = params.getNetworkTypeGroup();
        final double accuracy = params.getAccuracy();
        final String country = params.getCountry();
        final java.sql.Timestamp endDate = params.getEndDate();
        final int province = params.getProvince();
        
        final boolean userServerSelection = params.getUserServerSelection();
        
        boolean useMobileProvider = false;
        
        final boolean signalMobile;
        final String where;
        String signalColumn = null;
        if (type.equals("mobile"))
        {
            signalMobile = true;
            useMobileProvider = true;
            
            if (networkTypeGroup == null)
                where = "nt.type = 'MOBILE'";
            else
            {
                if ("2G".equalsIgnoreCase(networkTypeGroup))
                {
                    where = "nt.group_name = '2G'";
                    signalColumn = "signal_strength";
                }
                else if ("3G".equalsIgnoreCase(networkTypeGroup))
                {
                    where = "nt.group_name = '3G'";
                    signalColumn = "signal_strength";
                }
                else if ("4G".equalsIgnoreCase(networkTypeGroup))
                {
                    where = "nt.group_name = '4G'";
                    signalColumn = "lte_rsrp";
                }
                else if ("mixed".equalsIgnoreCase(networkTypeGroup))
                    where = "nt.group_name IN ('2G/3G','2G/4G','3G/4G','2G/3G/4G')";
                else
                    where = "1=0";
            }
        }
        else if (type.equals("wifi"))
        {
            where = "nt.type='WLAN'";
            signalMobile = false;
            signalColumn = "signal_strength";
        }
        else if (type.equals("browser"))
        {
            where = "nt.type = 'LAN'";
            signalMobile = false;
        }
        else
        {   // invalid request
            where = "1=0";
            signalMobile = false;
        }
        
        final JSONObject answer = new JSONObject();
        
        try (Connection conn = DbConnection.getConnection())
        {
            final JSONArray providers = new JSONArray();
            answer.put("providers", providers);
            final JSONArray devices = new JSONArray();
            answer.put("devices", devices);
            answer.put("quantile", quantile);
            answer.put("duration", durationDays);
            answer.put("type", type);
            
            try (PreparedStatement ps = selectProviders(conn, true, quantile, durationDays, accuracy, country,
            		useMobileProvider, where, signalMobile, userServerSelection,endDate,province,signalColumn);
                ResultSet rs = ps.executeQuery())
            {
                fillJSON(lang, rs, providers);
            }
            
            try (PreparedStatement ps = selectProviders(conn, false, quantile, durationDays, accuracy, country,
            		useMobileProvider, where, signalMobile, userServerSelection,endDate,province,signalColumn);
                ResultSet rs = ps.executeQuery())
            {
                final JSONArray providersSumsArray = new JSONArray();
                fillJSON(lang, rs, providersSumsArray);
                if (providersSumsArray.length() == 1)
                    answer.put("providers_sums", providersSumsArray.get(0));
            }
            
            try (PreparedStatement ps = selectDevices(conn, true, quantile, durationDays, accuracy, country,
            		useMobileProvider, where, maxDevices, userServerSelection,endDate,province);
                ResultSet rs = ps.executeQuery())
            {
                fillJSON(lang, rs, devices);
            }
            
            try (PreparedStatement ps = selectDevices(conn, false, quantile, durationDays, accuracy, country,
            		useMobileProvider, where, maxDevices, userServerSelection,endDate,province);
                ResultSet rs = ps.executeQuery())
            {
                final JSONArray devicesSumsArray = new JSONArray();
                fillJSON(lang, rs, devicesSumsArray);
                if (devicesSumsArray.length() == 1)
                    answer.put("devices_sums", devicesSumsArray.get(0));
            }
            
            final JSONArray countries = new JSONArray(getCountries(conn));
            answer.put("countries", countries);
            
            result = answer.toString();
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
        catch (final NamingException e1)
        {
            e1.printStackTrace();
        }
        return null;
    }
    
    private static Set<String> getCountries(Connection conn) throws SQLException {
    	Set<String> countries = new TreeSet<>();
		String sql = "WITH RECURSIVE t(n) AS ( "
				+ "SELECT MIN(mobile_network_id) FROM test"
				+ " UNION"
				+ " SELECT (SELECT mobile_network_id FROM test WHERE mobile_network_id > n"
				+ " ORDER BY mobile_network_id LIMIT 1)"
				+ " FROM t WHERE n IS NOT NULL"
				+ " )"
				+ "SELECT upper(mccmnc2name.country) FROM t LEFT JOIN mccmnc2name ON n=mccmnc2name.uid WHERE NOT mccmnc2name.country IS NULL GROUP BY mccmnc2name.country;";
    	
		try (PreparedStatement ps = conn.prepareStatement(sql);
		    ResultSet rs = ps.executeQuery())
	    {
    		while(rs.next())
    			countries.add(rs.getString(1));
    		return countries;
	    }
    }
    
    private static PreparedStatement selectProviders(final Connection conn, final boolean group, final float quantile, final int durationDays, final double accuracy,
            final String country, final boolean useMobileProvider, final String where, final boolean signalMobile, final boolean userServerSelection,
            final java.sql.Timestamp endDate, final int province, final String signalColumn) throws SQLException
    {
        PreparedStatement ps;
        String sql = String
                .format("SELECT" +
                        (group ? " p.name, p.shortname, " : "") +
                        " count(t.uid) count," +
                        " quantile(speed_download::bigint, ?::double precision) quantile_down," +
                        " quantile(speed_upload::bigint, ?::double precision) quantile_up," +
                        " quantile(%1$s::bigint, ?::double precision) quantile_signal," +
                        " quantile(ping_shortest::bigint, ?::double precision) quantile_ping," +
                        
                        " sum((speed_download >= ?)::int)::double precision / count(speed_download) down_green," +
                        " sum((speed_download < ? and speed_download >= ?)::int)::double precision / count(speed_download) down_yellow," +
                        " sum((speed_download < ?)::int)::double precision / count(speed_download) down_red," +
                                                
                        " sum((speed_upload >= ?)::int)::double precision / count(speed_upload) up_green," +
                        " sum((speed_upload < ? and speed_upload >= ?)::int)::double precision / count(speed_upload) up_yellow," +
                        " sum((speed_upload < ?)::int)::double precision / count(speed_upload) up_red," +
                        
                        " sum((%1$s >= ?)::int)::double precision / count(%1$s) signal_green," +
                        " sum((%1$s < ? and %1$s >= ?)::int)::double precision / count(%1$s) signal_yellow," +
                        " sum((%1$s < ?)::int)::double precision / count(%1$s) signal_red," + 
                        
                        " sum((ping_shortest <= ?)::int)::double precision / count(ping_shortest) ping_green," +
                        " sum((ping_shortest > ? and ping_shortest <= ?)::int)::double precision / count(ping_shortest) ping_yellow," +
                        " sum((ping_shortest > ?)::int)::double precision / count(ping_shortest) ping_red" +
                        
                        " FROM test t" +
                        " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                        " JOIN provider p ON" + 
                        (useMobileProvider ? " t.mobile_provider_id = p.uid" : " t.provider_id = p.uid") +
                        " WHERE %2$s" +
                        ((country != null && useMobileProvider)? " AND t.network_sim_country = ?" : "") +
                        " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'"+
                        " AND \"time\" > " +
                        ((endDate != null) ? (" ?::TIMESTAMP WITH TIME ZONE  ") : "NOW()") +
                        " - ?::INTERVAL " +
                        ((endDate != null) ? (" AND \"time\" <=  ?::TIMESTAMP WITH TIME ZONE ") : "") +
                        //" AND user_server_selection = ? " +
                        ((province != -1) ? (" AND gkz/10000 = ? ") : "") +
                        ((accuracy > 0) ? " AND t.geo_accuracy < ?" : "") + 
                        ((ONLY_PINNED)?" AND t.pinned = true":"") +
                        (group? " GROUP BY p.uid" : "") +
                        " ORDER BY count DESC",
                        signalColumn,
                        where);
        
        if (country != null) {
        	sql = String
                    .format("SELECT" +
                            ((group && useMobileProvider) ? " p.name AS name, p.shortname AS shortname,  p.mccmnc AS sim_mcc_mnc, " : "") +
                            ((group && !useMobileProvider) ? " public_ip_as_name AS name, public_ip_as_name AS shortname, t.public_ip_asn AS asn,  " : "") +
                            " count(t.uid) count," +
                            " quantile(speed_download::bigint, ?::double precision) quantile_down," +
                            " quantile(speed_upload::bigint, ?::double precision) quantile_up," +
                            " quantile(%1$s::bigint, ?::double precision) quantile_signal," +
                            " quantile(ping_shortest::bigint, ?::double precision) quantile_ping," +
                            
                            " sum((speed_download >= ?)::int)::double precision / count(speed_download) down_green," +
                            " sum((speed_download < ? and speed_download >= ?)::int)::double precision / count(speed_download) down_yellow," +
                            " sum((speed_download < ?)::int)::double precision / count(speed_download) down_red," +
                                                    
                            " sum((speed_upload >= ?)::int)::double precision / count(speed_upload) up_green," +
                            " sum((speed_upload < ? and speed_upload >= ?)::int)::double precision / count(speed_upload) up_yellow," +
                            " sum((speed_upload < ?)::int)::double precision / count(speed_upload) up_red," +
                            
                            " sum((%1$s >= ?)::int)::double precision / count(%1$s) signal_green," +
                            " sum((%1$s < ? and %1$s >= ?)::int)::double precision / count(%1$s) signal_yellow," +
                            " sum((%1$s < ?)::int)::double precision / count(%1$s) signal_red," + 
                            
                            " sum((ping_shortest <= ?)::int)::double precision / count(ping_shortest) ping_green," +
                            " sum((ping_shortest > ? and ping_shortest <= ?)::int)::double precision / count(ping_shortest) ping_yellow," +
                            " sum((ping_shortest > ?)::int)::double precision / count(ping_shortest) ping_red" +
                            
                            " FROM test t" +
                            " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                            (useMobileProvider ? " LEFT JOIN mccmnc2name p ON p.uid = t.mobile_sim_id" : "") + 
                            " WHERE %2$s" +
                            " AND " + (useMobileProvider?"p.country = ? AND ((t.country_location IS NULL OR t.country_location = ?)  AND (NOT t.roaming_type = 2))":"t.country_geoip = ? ") +
                            " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'"+
                            " AND \"time\" > " +
                            ((endDate != null) ? (" ?::TIMESTAMP WITH TIME ZONE  ") : "NOW()") +
                            " - ?::INTERVAL " +
                            ((endDate != null) ? (" AND \"time\" <=  ?::TIMESTAMP WITH TIME ZONE ") : "") +
                            //" AND user_server_selection = ? " +
                            ((province != -1) ? (" AND gkz/10000 = ? ") : "") +
                            ((accuracy > 0) ? " AND t.geo_accuracy < ?" : "") + 
                            ((ONLY_PINNED)?" AND t.pinned = true":"") +
                            ((group && (useMobileProvider))? " GROUP BY p.uid, p.mccmnc" : "") +
                            ((group && (!useMobileProvider))? " GROUP BY t.public_ip_as_name, t.public_ip_asn" : "") +
                            " ORDER BY count DESC",
                            signalColumn,
                            where);
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
        
        if (endDate != null )
        {	
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            ps.setTimestamp(i++, endDate, cal);
        }
        
        ps.setString(i++, String.format("%d days", durationDays));
        
        if (endDate != null )
        {	
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            ps.setTimestamp(i++, endDate, cal);
        }
        

        //ps.setBoolean(i++, userServerSelection);
        
        if (province != -1) {
        	ps.setInt(i++, province);
        }
        
        if (accuracy>0) {
        	ps.setDouble(i++, accuracy);
        }

        System.out.println(ps);
        
        return ps;
    }
    
    private static PreparedStatement selectDevices(final Connection conn, final boolean group, final float quantile, final int durationDays, final double accuracy,
            final String country, final boolean useMobileProvider, final String where, final int maxDevices, final boolean userServerSelection,
            final java.sql.Timestamp endDate, final int province) throws SQLException
    {
        PreparedStatement ps;
        String sql = String.format("SELECT" +
                (group ? " COALESCE(adm.fullname, t.model) model," : "") +
                " count(t.uid) count," + " quantile(speed_download::bigint, ?::double precision) quantile_down," +
                " quantile(speed_upload::bigint, ?::double precision) quantile_up," +
                " quantile(ping_shortest::bigint, ?::double precision) quantile_ping" +
                " FROM test t" +
                " LEFT JOIN device_map adm ON adm.codename=t.model" +
                " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                " WHERE %s" +
                " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
                " AND \"time\" > " +
                ((endDate != null) ? (" ?::TIMESTAMP WITH TIME ZONE ") : "NOW()") +
                " - ?::INTERVAL " +
                ((endDate != null) ? (" AND \"time\" <=  ?::TIMESTAMP WITH TIME ZONE ") : "") +
                //" AND user_server_selection = ? " +
                ((province != -1) ? (" AND gkz/10000 = ? ") : "") +
                (useMobileProvider ? " AND t.mobile_provider_id IS NOT NULL" : "") +
                ((accuracy > 0) ? " AND t.geo_accuracy < ?" : "") + 
                ((ONLY_PINNED)?" AND t.pinned = true":"") +
                (group ? " GROUP BY COALESCE(adm.fullname, t.model) HAVING count(t.uid) > 10" : "") +
                " ORDER BY count DESC" +
                " LIMIT %d", where, maxDevices);
        if (country != null) {
        	sql = String.format("SELECT" +
                    (group ? " COALESCE(adm.fullname, t.model) model," : "") +
                    " count(t.uid) count," + " quantile(speed_download::bigint, ?::double precision) quantile_down," +
                    " quantile(speed_upload::bigint, ?::double precision) quantile_up," +
                    " quantile(ping_shortest::bigint, ?::double precision) quantile_ping" +
                    " FROM test t" +
                    " LEFT JOIN device_map adm ON adm.codename=t.model" +
                    " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                    (useMobileProvider ? " LEFT JOIN mccmnc2name p ON p.uid = t.mobile_sim_id" : "") +
                    " WHERE %s" +
                    " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
                    " AND \"time\" > " +
                    ((endDate != null) ? (" ?::TIMESTAMP WITH TIME ZONE ") : "NOW()") +
                    " - ?::INTERVAL" +
                    ((endDate != null) ? (" AND \"time\" <=  ?::TIMESTAMP WITH TIME ZONE ") : "") +
                    //" AND user_server_selection = ? " +
                    ((province != -1) ? (" AND gkz/10000 = ? ") : "") +
                    " AND " + (useMobileProvider?"p.country = ? AND ((t.country_location IS NULL OR t.country_location = ?)  AND (NOT t.roaming_type = 2))":"t.country_geoip = ? ") +
                    ((accuracy > 0) ? " AND t.geo_accuracy < ?" : "") + 
                    ((ONLY_PINNED)?" AND t.pinned = true":"") +
                    (group ? " GROUP BY COALESCE(adm.fullname, t.model) HAVING count(t.uid) > 10" : "") +
                    " ORDER BY count DESC" +
                    " LIMIT %d", where, maxDevices);
        }
        
        ps = conn.prepareStatement(sql);
        
        int i = 1;
        for (int j = 0; j < 2; j++)
            ps.setFloat(i++, quantile);
        ps.setFloat(i++, 1 - quantile); // inverse for ping
        
        
        if (endDate != null )
        {	
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            ps.setTimestamp(i++, endDate, cal);
        }
       
        ps.setString(i++, String.format("%d days", durationDays));
        
        if (endDate != null )
        {	
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            ps.setTimestamp(i++, endDate, cal);
        }
       
        //ps.setBoolean(i++, userServerSelection);
        	
        if (province != -1) {
        	ps.setInt(i++, province);
        }
        
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
        
        System.out.println(ps);
        return ps;
    }
    
    private static void fillJSON(final String lang, final ResultSet rs, final JSONArray providers)
            throws SQLException, JSONException
    {
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
    }
}
