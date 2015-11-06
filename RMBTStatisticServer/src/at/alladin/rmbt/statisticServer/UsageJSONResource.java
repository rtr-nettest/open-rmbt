/*******************************************************************************
 * Copyright 2013-2015 Thomas Schreiber
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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.resource.Get;

//Statistics for internal purpose

public class UsageJSONResource extends ServerResource
{
    @Get("json")
    public String request(final String entity)
    {
    	addAllowOrigin();
    	
    	JSONObject result = new JSONObject();
    	int month = -1;
    	int year = -1;
        try
        {
        	//parameters
        	final Form getParameters = getRequest().getResourceRef().getQueryAsForm();
        	try {
        		
        	
        	
	        	if (getParameters.getNames().contains("month")) {
	        		month = Integer.parseInt(getParameters.getFirstValue("month"));
	        		if (month > 11 || month < 0) {
	        			throw new NumberFormatException();
	        		}
	        	}
	        	if (getParameters.getNames().contains("year")) {
	        		year = Integer.parseInt(getParameters.getFirstValue("year"));
	        		if (year < 0) {
	        			throw new NumberFormatException();
	        		}
	        	}
        	}
        	catch(NumberFormatException e) {
        		return "invalid parameters";
        	}
        	
        	
        	Calendar now = new GregorianCalendar();
        	Calendar monthBegin = new GregorianCalendar((year>0)?year:now.get(Calendar.YEAR), (month>=0)?month:now.get(Calendar.MONTH), 1);
        	Calendar monthEnd = new GregorianCalendar((year>0)?year:now.get(Calendar.YEAR), (month>=0)?month:now.get(Calendar.MONTH), monthBegin.getActualMaximum(Calendar.DAY_OF_MONTH));
        	//if now -> do not use the last day
        	if (month == now.get(Calendar.MONTH) && year == now.get(Calendar.YEAR)){
        		monthEnd = now;
        		monthEnd.add(Calendar.DATE,-1);
        	}
        	
        	JSONObject platforms = getPlatforms(new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
        	JSONObject usage = getClassicUsage(new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
        	JSONObject versionsIOS = getVersions("iOS", new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
        	JSONObject versionsAndroid = getVersions("Android", new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
        	JSONObject versionsApplet = getVersions("Applet", new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
        	JSONObject networkGroupNames = getNetworkGroupName(new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
        	JSONObject networkGroupTypes = getNetworkGroupType(new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
        	result.put("platforms", platforms);
        	result.put("usage", usage);
        	result.put("versions_ios", versionsIOS);
        	result.put("versions_android", versionsAndroid);
        	result.put("versions_applet", versionsApplet);
        	result.put("network_group_names", networkGroupNames);
        	result.put("network_group_types", networkGroupTypes);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        } catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        
        return result.toString();
    }
    
    

    
    private JSONObject getClassicUsage(Timestamp begin, Timestamp end) throws SQLException, JSONException {
    	JSONObject returnObj = new JSONObject();
    	JSONArray sums = new JSONArray();
    	JSONArray values = new JSONArray();
    	returnObj.put("sums", sums);
    	returnObj.put("values", values);
    	
    	HashMap<String,Long> fieldSums = new HashMap<>();
    	fieldSums.put("tests", new Long(0));
    	fieldSums.put("clients", new Long(0));
    	fieldSums.put("ips", new Long(0));
    	
    	PreparedStatement ps;
        ResultSet rs;
        
        
    	final String select = "date_trunc('day', time) _day, count(uid) count_tests, count(DISTINCT client_id) count_clients, count(DISTINCT client_public_ip) count_ips";
        final String where = "status='FINISHED' AND deleted=false";
        final String sql = "SELECT " + select + " FROM test WHERE " + where + " AND time >= ? AND time < ? GROUP BY _day ORDER BY _day ASC";
        ps = conn.prepareStatement(sql);
        ps.setTimestamp(1, begin);
        ps.setTimestamp(2, end);
    	rs = ps.executeQuery();
    	
    	while(rs.next()) {
    		JSONObject entry = new JSONObject();
    		entry.put("day", rs.getDate("_day").getTime());
    		
    		JSONArray currentEntryValues = new JSONArray();
    		entry.put("values",currentEntryValues);
    		
    		JSONObject jTests = new JSONObject();
    		jTests.put("field", "tests");
    		jTests.put("value", rs.getLong("count_tests"));
    		currentEntryValues.put(jTests);
    		
    		JSONObject jClients = new JSONObject();
    		jClients.put("field", "clients");
    		jClients.put("value", rs.getLong("count_clients"));
    		currentEntryValues.put(jClients);
    		
    		JSONObject jIPs = new JSONObject();
    		jIPs.put("field", "ips");
    		jIPs.put("value", rs.getLong("count_ips"));
    		currentEntryValues.put(jIPs);
    		
    		fieldSums.put("tests", fieldSums.get("tests") + rs.getLong("count_tests"));
    		fieldSums.put("clients", fieldSums.get("clients") + rs.getLong("count_clients"));
    		fieldSums.put("ips", fieldSums.get("ips") + rs.getLong("count_ips"));    		
    		
    		//get some structure in there
    		
    		
    		values.put(entry);
    	}
    	
    	rs.close();
    	ps.close();
    	
    	//add field sums
    	for (String field : fieldSums.keySet()) {
    		JSONObject obj = new JSONObject();
    		obj.put("field", field);
    		obj.put("sum", fieldSums.get(field));
    		sums.put(obj);
    	}
        
    	return returnObj;
    }
    
    /**
     * Returns the statistics for used platforms for a specific timespan [begin, end)
     * @param begin select all tests with time >= begin
     * @param end select all tests with time < end
     * @return the structurized JSON object
     * @throws SQLException
     * @throws JSONException
     */
    private JSONObject getPlatforms(Timestamp begin, Timestamp end) throws SQLException, JSONException {
    	JSONObject returnObj = new JSONObject();
    	JSONArray sums = new JSONArray();
    	JSONArray values = new JSONArray();
    	returnObj.put("sums", sums);
    	returnObj.put("values", values);
    	
    	HashMap<String,Long> fieldSums = new HashMap<>();
    	
    	PreparedStatement ps;
        ResultSet rs;
        
        final String sql = "SELECT date_trunc('day', time) _day, COALESCE(plattform,'null') platform, count(plattform) count_platform" +
        		" FROM test" +
        		" WHERE status='FINISHED' AND deleted=false AND time >= ? AND time < ? " +
        		" GROUP BY _day, plattform" + 
        		" HAVING count(plattform) > 0" + 
        		"ORDER BY _day ASC";
        
    	ps = conn.prepareStatement(sql);
        ps.setTimestamp(1, begin);
        ps.setTimestamp(2, end);
    	rs = ps.executeQuery();
    	
    	//one array-item for each day
    	long currentTime = -1;
    	JSONObject currentEntry = null;
    	JSONArray currentEntryValues = null;
    	while(rs.next()) {
    		
    		//new item, of a new day is reached
    		long newTime = rs.getDate("_day").getTime();
    		if (currentTime != newTime) {
    			currentTime = newTime;
    			currentEntry = new JSONObject();
    			currentEntryValues = new JSONArray();
    			currentEntry.put("day", rs.getDate("_day").getTime());
    			currentEntry.put("values", currentEntryValues);
    			values.put(currentEntry);
    		}
    		
    		
    		//disable null-values
    		String platform = rs.getString("platform");
    		long count = rs.getLong("count_platform");
    		if (platform.isEmpty()) {
    			platform = "empty";
    		}
    		
    		//add value to sum
    		if (!fieldSums.containsKey(platform)) {
    			fieldSums.put(platform, new Long(0));
    		}
    		fieldSums.put(platform, fieldSums.get(platform) + count);
    		
    		JSONObject current = new JSONObject();
    		current.put("field", platform);
    		current.put("value", count);
    		currentEntryValues.put(current);
    	}
    	
    	rs.close();
    	ps.close();
        
    	//add field sums
    	for (String field : fieldSums.keySet()) {
    		JSONObject obj = new JSONObject();
    		obj.put("field", field);
    		obj.put("sum", fieldSums.get(field));
    		sums.put(obj);
    	}
    	
    	return returnObj;
    }
    
    /**
     * Returns the statistics for used versions for a specific timespan [begin, end)
     * @param begin select all tests with time >= begin
     * @param end select all tests with time < end
     * @return the structurized JSON object
     * @throws SQLException
     * @throws JSONException
     */
    private JSONObject getVersions(String platform, Timestamp begin, Timestamp end) throws SQLException, JSONException {
    	JSONObject returnObj = new JSONObject();
    	JSONArray sums = new JSONArray();
    	JSONArray values = new JSONArray();
    	returnObj.put("sums", sums);
    	returnObj.put("values", values);
    	
    	HashMap<String,Long> fieldSums = new HashMap<>();
    	
    	PreparedStatement ps;
        ResultSet rs;
        
        final String sql = "SELECT date_trunc('day', time) _day, COALESCE(client_software_version,'null') \"version\", count(client_software_version) count_version" +
        		" FROM test" +
        		" WHERE status='FINISHED' AND deleted=false AND time >= ? AND time < ? AND plattform = ?" +
        		" GROUP BY _day, client_software_version " + 
        		" HAVING count(client_software_version) > 0 " + 
        		" ORDER BY _day ASC";
        
    	ps = conn.prepareStatement(sql);
        ps.setTimestamp(1, begin);
        ps.setTimestamp(2, end);
        ps.setString(3, platform);
    	rs = ps.executeQuery();
    	
    	//one array-item for each day
    	long currentTime = -1;
    	JSONObject currentEntry = null;
    	JSONArray currentEntryValues = null;
    	while(rs.next()) {
    		
    		//new item, of a new day is reached
    		long newTime = rs.getDate("_day").getTime();
    		if (currentTime != newTime) {
    			currentTime = newTime;
    			currentEntry = new JSONObject();
    			currentEntryValues = new JSONArray();
    			currentEntry.put("day", rs.getDate("_day").getTime());
    			currentEntry.put("values", currentEntryValues);
    			values.put(currentEntry);
    		}
    		
    		
    		//disable null-values
    		String version = rs.getString("version");
    		long count = rs.getLong("count_version");
    		if (version.isEmpty()) {
    			version = "empty";
    		}
    		
    		//add value to sum
    		if (!fieldSums.containsKey(version)) {
    			fieldSums.put(version, new Long(0));
    		}
    		fieldSums.put(version, fieldSums.get(version) + count);
    		
    		JSONObject current = new JSONObject();
    		current.put("field", version);
    		current.put("value", count);
    		currentEntryValues.put(current);
    	}
    	
    	rs.close();
    	ps.close();
        
    	//add field sums
    	for (String field : fieldSums.keySet()) {
    		JSONObject obj = new JSONObject();
    		obj.put("field", field);
    		obj.put("sum", fieldSums.get(field));
    		sums.put(obj);
    	}
    	
    	return returnObj;
    }
    
    /**
     * Returns the statistics for used network name for a specific timespan [begin, end)
     * @param begin select all tests with time >= begin
     * @param end select all tests with time < end
     * @return the structurized JSON object
     * @throws SQLException
     * @throws JSONException
     */
    private JSONObject getNetworkGroupName(Timestamp begin, Timestamp end) throws SQLException, JSONException {
    	JSONObject returnObj = new JSONObject();
    	JSONArray sums = new JSONArray();
    	JSONArray values = new JSONArray();
    	returnObj.put("sums", sums);
    	returnObj.put("values", values);
    	
    	HashMap<String,Long> fieldSums = new HashMap<>();
    	
    	PreparedStatement ps;
        ResultSet rs;
        
        final String sql = "SELECT date_trunc('day', time) _day, COALESCE(network_group_name,'null') \"version\", count(network_group_name) count_group_name" +
        		" FROM test" +
        		" WHERE status='FINISHED' AND deleted=false AND time >= ? AND time < ?" +
        		" GROUP BY _day, network_group_name " + 
        		" HAVING count(network_group_name) > 0 " + 
        		" ORDER BY _day ASC";
        
    	ps = conn.prepareStatement(sql);
        ps.setTimestamp(1, begin);
        ps.setTimestamp(2, end);
        
    	rs = ps.executeQuery();
    	
    	//one array-item for each day
    	long currentTime = -1;
    	JSONObject currentEntry = null;
    	JSONArray currentEntryValues = null;
    	while(rs.next()) {
    		
    		//new item, of a new day is reached
    		long newTime = rs.getDate("_day").getTime();
    		if (currentTime != newTime) {
    			currentTime = newTime;
    			currentEntry = new JSONObject();
    			currentEntryValues = new JSONArray();
    			currentEntry.put("day", rs.getDate("_day").getTime());
    			currentEntry.put("values", currentEntryValues);
    			values.put(currentEntry);
    		}
    		
    		
    		//disable null-values
    		String version = rs.getString("version");
    		long count = rs.getLong("count_group_name");
    		if (version.isEmpty()) {
    			version = "empty";
    		}
    		
    		//add value to sum
    		if (!fieldSums.containsKey(version)) {
    			fieldSums.put(version, new Long(0));
    		}
    		fieldSums.put(version, fieldSums.get(version) + count);
    		
    		JSONObject current = new JSONObject();
    		current.put("field", version);
    		current.put("value", count);
    		currentEntryValues.put(current);
    	}
    	
    	rs.close();
    	ps.close();
        
    	//add field sums
    	for (String field : fieldSums.keySet()) {
    		JSONObject obj = new JSONObject();
    		obj.put("field", field);
    		obj.put("sum", fieldSums.get(field));
    		sums.put(obj);
    	}
    	
    	return returnObj;
    }
    
    /**
     * Returns the statistics for used network type for a specific timespan [begin, end)
     * @param begin select all tests with time >= begin
     * @param end select all tests with time < end
     * @return the structurized JSON object
     * @throws SQLException
     * @throws JSONException
     */
    private JSONObject getNetworkGroupType(Timestamp begin, Timestamp end) throws SQLException, JSONException {
    	JSONObject returnObj = new JSONObject();
    	JSONArray sums = new JSONArray();
    	JSONArray values = new JSONArray();
    	returnObj.put("sums", sums);
    	returnObj.put("values", values);
    	
    	HashMap<String,Long> fieldSums = new HashMap<>();
    	
    	PreparedStatement ps;
        ResultSet rs;
        
        final String sql = "SELECT date_trunc('day', time) _day, COALESCE(network_group_type,'null') \"version\", count(network_group_type) count_group_type" +
        		" FROM test" +
        		" WHERE status='FINISHED' AND deleted=false AND time >= ? AND time < ?" +
        		" GROUP BY _day, network_group_type " + 
        		" HAVING count(network_group_type) > 0 " + 
        		" ORDER BY _day ASC";
        
    	ps = conn.prepareStatement(sql);
        ps.setTimestamp(1, begin);
        ps.setTimestamp(2, end);
        
    	rs = ps.executeQuery();
    	
    	//one array-item for each day
    	long currentTime = -1;
    	JSONObject currentEntry = null;
    	JSONArray currentEntryValues = null;
    	while(rs.next()) {
    		
    		//new item, of a new day is reached
    		long newTime = rs.getDate("_day").getTime();
    		if (currentTime != newTime) {
    			currentTime = newTime;
    			currentEntry = new JSONObject();
    			currentEntryValues = new JSONArray();
    			currentEntry.put("day", rs.getDate("_day").getTime());
    			currentEntry.put("values", currentEntryValues);
    			values.put(currentEntry);
    		}
    		
    		
    		//disable null-values
    		String version = rs.getString("version");
    		long count = rs.getLong("count_group_type");
    		if (version.isEmpty()) {
    			version = "empty";
    		}
    		
    		//add value to sum
    		if (!fieldSums.containsKey(version)) {
    			fieldSums.put(version, new Long(0));
    		}
    		fieldSums.put(version, fieldSums.get(version) + count);
    		
    		JSONObject current = new JSONObject();
    		current.put("field", version);
    		current.put("value", count);
    		currentEntryValues.put(current);
    	}
    	
    	rs.close();
    	ps.close();
        
    	//add field sums
    	for (String field : fieldSums.keySet()) {
    		JSONObject obj = new JSONObject();
    		obj.put("field", field);
    		obj.put("sum", fieldSums.get(field));
    		sums.put(obj);
    	}
    	
    	return returnObj;
    }
}
