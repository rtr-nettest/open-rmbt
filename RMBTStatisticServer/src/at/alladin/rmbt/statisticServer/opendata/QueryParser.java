/*******************************************************************************
 * Copyright 2013-2016 Thomas Schreiber
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
package at.alladin.rmbt.statisticServer.opendata;

import at.alladin.rmbt.shared.ResourceManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Form;

/**
 *
 * @author Thomas
 */
public class QueryParser {
    /**
     * @return the whereParams
     */
    public Map<String,List<SingleParameter>> getWhereParams() {
        return whereParams;
    }

    public String getWhereClause() {
        return getWhereClause("");
    }
    
    /**
     * @param prependWith
     * @return the whereClause
     */
    public String getWhereClause(String prependWith) {
        //trim if necessary
        if (whereClause.trim().startsWith("AND")) {
            whereClause = whereClause.substring(4).trim();
        }
        
        if (!whereClause.trim().isEmpty()) {
            return " " + prependWith + " " + whereClause + " ";
        } 
        return whereClause;
    }

    /**
     * @return the orderClause
     */
    public String getOrderClause() {
        return orderClause;
    }

    public Map<String,FieldType> getAllowedFields() {
        return allowedFields;
    }
    
    
    public enum FieldType {STRING, DATE, LONG, DOUBLE, BOOLEAN, UUID, SORTBY, SORTORDER, IGNORE};
    private final ResourceBundle settings = ResourceManager.getCfgBundle();

    //all fields for which the user can sort the result
    private static final HashSet<String> openDataFieldsSortable = new HashSet<>(Arrays.asList(new String[]{"download_kbit","upload_kbit","time","signal_strength","ping_ms"}));
    
    private final Map<String, List<SingleParameter>> whereParams = new HashMap<>();
    private final Map<String, SingleParameterTransformator> transformators = new HashMap<>();
    private final Map<String,FieldType> allowedFields = new HashMap<>();
    private String whereClause;
    private String orderClause;
    
    //Values for the database
    private final Queue<Map.Entry<String, FieldType>> searchValues = new LinkedList<>();
        
    public QueryParser() {
        allowedFields.put("download_kbit", FieldType.LONG);
        allowedFields.put("download_kbit[]", FieldType.LONG);
        allowedFields.put("upload_kbit", FieldType.LONG);
        allowedFields.put("upload_kbit[]", FieldType.LONG);
        allowedFields.put("ping_ms", FieldType.DOUBLE);        
        allowedFields.put("ping_ms[]", FieldType.DOUBLE);        
        allowedFields.put("time", FieldType.DATE);    
        allowedFields.put("time[]", FieldType.DATE);
        allowedFields.put("zip_code", FieldType.LONG);        
        allowedFields.put("zip_code[]", FieldType.LONG);
        allowedFields.put("gkz", FieldType.LONG);
        allowedFields.put("gkz[]", FieldType.LONG);
        allowedFields.put("cat_technology", FieldType.STRING);        
        allowedFields.put("cat_technology[]", FieldType.STRING);        
        allowedFields.put("client_version", FieldType.STRING);
        allowedFields.put("client_version[]", FieldType.STRING);
        allowedFields.put("model", FieldType.STRING);
        allowedFields.put("model[]", FieldType.STRING);
        allowedFields.put("network_name", FieldType.STRING);
        allowedFields.put("network_name[]", FieldType.STRING);
        allowedFields.put("network_type", FieldType.STRING);
        allowedFields.put("network_type[]", FieldType.STRING);
        allowedFields.put("platform", FieldType.STRING);
        allowedFields.put("platform[]", FieldType.STRING);
        allowedFields.put("signal_strength", FieldType.LONG);
        allowedFields.put("signal_strength[]", FieldType.LONG);
        allowedFields.put("open_uuid",FieldType.UUID);
        allowedFields.put("long",FieldType.DOUBLE);
        allowedFields.put("long[]",FieldType.DOUBLE);
        allowedFields.put("lat",FieldType.DOUBLE);
        allowedFields.put("lat[]",FieldType.DOUBLE);
        allowedFields.put("mobile_provider_name", FieldType.STRING);
        allowedFields.put("mobile_provider_name[]", FieldType.STRING);
        allowedFields.put("provider_name",FieldType.STRING);
        allowedFields.put("provider_name[]",FieldType.STRING);
        allowedFields.put("sim_mcc_mnc",FieldType.STRING);
        allowedFields.put("sim_mcc_mnc[]",FieldType.STRING);
        allowedFields.put("sim_country",FieldType.STRING);
        allowedFields.put("sim_country[]",FieldType.STRING);
        allowedFields.put("asn",FieldType.LONG);
        allowedFields.put("asn[]",FieldType.LONG);
        allowedFields.put("network_country",FieldType.STRING);
        allowedFields.put("network_country[]",FieldType.STRING);
        allowedFields.put("country_geoip",FieldType.STRING);
        allowedFields.put("country_geoip[]",FieldType.STRING);
        allowedFields.put("user_server_selection",FieldType.BOOLEAN);
        allowedFields.put("developer_code",FieldType.STRING); //for backwards compatiblity with old web page
        allowedFields.put("loc_accuracy",FieldType.LONG);
        allowedFields.put("loc_accuracy[]",FieldType.LONG);
        allowedFields.put("public_ip_as_name",FieldType.STRING);
        allowedFields.put("timestamp", FieldType.IGNORE); //for forcing no-cache
        allowedFields.put("_", FieldType.IGNORE); //jQuery no-cache standard
        allowedFields.put("sender", FieldType.IGNORE);
        allowedFields.put("additional_info", FieldType.IGNORE);
        allowedFields.put("additional_info[]", FieldType.IGNORE);
        
        //allowedFields.put("ip_anonym", FieldType.STRING);
        //allowedFields.put("ip_anonym[]", FieldType.STRING);
        allowedFields.put("implausible", FieldType.BOOLEAN);
        allowedFields.put("pinned", FieldType.BOOLEAN);
        
        allowedFields.put("sort_by",FieldType.SORTBY);
        allowedFields.put("sort_order",FieldType.SORTORDER);
        allowedFields.put("cursor", FieldType.LONG);
        allowedFields.put("max_results", FieldType.LONG);
    }
    
    public JSONArray parseQuery(Form getParameters) {
        //Values for the database
        searchValues.clear();

        this.whereClause = "";
        this.orderClause = "";
        final JSONArray invalidElements = new JSONArray();
        final JSONObject response = new JSONObject();
        
        String sortBy="";
        String sortOrder = "";
        for (String attr : getParameters.getNames()) {
            //check if attribute is allowed
            if (!allowedFields.containsKey(attr)) {
                invalidElements.put(attr);
                continue;
            }

            //check if value for the attribute is correct
            //first, check if the attribute is an array
            String[] values = getParameters.getValuesArray(attr);            
            for (String value : values) {
                boolean negate = false;
                if (value.startsWith("!") && value.length()>0) {
                    negate = true;
                    value = value.substring(1);
                }
                
                FieldType type = getAllowedFields().get(attr);
                //do some basic sanity checks for the given parameters
                switch (type) {
                    case STRING:
                        if (value.isEmpty()) {
                            invalidElements.put(attr);
                            continue;
                        }
                        //allow using wildcard '*' instead of sql '%'
                        value = value.replace('*', '%');
                        
                        //allow using wildcard '?' instead of sql '_'
                        value = value.replace('?', '_');

                        whereClause += formatWhereClause(attr, value,negate, type, searchValues);

                        break;
                    case DATE:
                        String comperatorDate = "=";
                        if (value.startsWith(">") || value.startsWith("<")) {
                            comperatorDate = value.substring(0, 1);
                            value = value.substring(1);
                        }
                        if (value.isEmpty() || !isDouble(value)) {
                            //try parsing the date
                            long v = parseDate(value);
                            if (v == -1) {
                                invalidElements.put(attr);
                                continue;
                            }
                            
                            //date can be parsed => assign new value
                            value = Long.toString(v);
                        }
                        
                        long v = Long.parseLong(value);
                        value = Long.toString(v);
                        
                        whereClause += formatWhereClause(attr, value, comperatorDate, negate, type, searchValues);
                        break;
                    case UUID:
                        if (value.isEmpty()) {
                            invalidElements.put(attr);
                            continue;
                        }
                        value = value.substring(1); //cut prefix
                        try {
                            UUID.fromString(value);
                        } catch(IllegalArgumentException e) {
                            invalidElements.put(attr);
                            continue;
                        }
                        whereClause += formatWhereClause(attr, value, "=", negate, type, searchValues);
                        break;
                    case BOOLEAN:
                    	if (value.isEmpty() ||
                			(!value.toLowerCase().equals("false") && !value.toLowerCase().equals("true"))) {
                            invalidElements.put(attr);
                            continue;
                        }
                		whereClause += formatWhereClause(attr, value, "=", negate, type, searchValues);
                    	break;
                    case DOUBLE:
                    case LONG:
                        String comperator = "=";
                        if (value.startsWith(">") || value.startsWith("<")) {
                            comperator = value.substring(0, 1);
                            comperator += "=";
                            value = value.substring(1);
                        }
                        if (value.isEmpty() || (type == FieldType.DOUBLE && !isDouble(value)) || (type == FieldType.LONG && !isLong(value))) {
                            invalidElements.put(attr);
                            continue;
                        }
                        whereClause += formatWhereClause(attr, value, comperator, negate, type, searchValues);
                        break;
                    case IGNORE: 
                    	break; //do nothing
                    case SORTBY:
                        if (value.isEmpty() || !openDataFieldsSortable.contains(value)) {
                            invalidElements.put(attr);
                            continue;
                        }
                        sortBy = value;
                        break;
                    case SORTORDER:
                        //only "ASC", "DESC" are allowed
                        //and the attribute is only allowed, if sort_by is also given
                        if (value.isEmpty() || 
                                (!value.toUpperCase().equals("ASC") && !value.toUpperCase().equals("DESC")) || 
                                !getParameters.getNames().contains("sort_by")) {
                            invalidElements.put(attr);
                            continue;
                        }
                        sortOrder = value;
                        break;
                }
            }
            
        }
        
        //add defaults
        whereClause += formatWhereClauseDefaults();
        
        orderClause = formatOrderClause(sortBy, sortOrder);
        return invalidElements;
    }
    
    /**
     * Formats the sql-clause for ordering the results
     * @param sortBy the field for which the results are ordered, must be contained in openDataFieldsSortable
     * @param sortOrder the order; ASC or DESC
     * @return 
     */
    private static String formatOrderClause(String sortBy, String sortOrder) {
        if (sortBy.isEmpty()) {
            return "";
        }
        //convert to real field names
        if (sortBy.equals("download_kbit")) {
            sortBy = "t.speed_download";
        }
        else if (sortBy.equals("upload_kbit")) {
            sortBy = "t.speed_upload";
        }
        else if (sortBy.equals("ping_ms")) {
            sortBy = "t.ping_median";
        }
        else if (sortBy.equals("time")) {
            sortBy = "t.time";
        }
        else if (sortBy.equals("client_version")) {
            sortBy = "client_software_version";
        }
        else if (sortBy.equals("sim_mcc_mnc")) {
            sortBy = "network_sim_operator";
        } 
        else if (sortBy.equals("sim_country")) {
            sortBy = "network_sim_country";
        }
        else if (sortBy.equals("signal_strength")) {
            sortBy= "t.signal_strength";
        } 
        
        String ret = " ORDER BY " + sortBy + " " + sortOrder;
        return ret;
    }
    
    private String formatWhereClauseDefaults() {
    	String ret = "";
    	if (!this.whereParams.containsKey("implausible")) {
    		ret += formatWhereClause("implausible", "false", "=", false, FieldType.BOOLEAN, this.searchValues);
    	}
    	
    	return ret;
    }
    
    private String formatWhereClause(String attr, String value, boolean negate, FieldType type, Queue<Map.Entry<String, FieldType>> queue) {
        return formatWhereClause(attr, value,"ILIKE",negate,type,queue);
    }
    
    /**
     * Transforms the given parameters in a psql where-clause, starting with "AND"
     * @param attr the attribute name from the get-request - is replaced with the real column name
     * @param value what the column given in 'attr' should have as value
     * @param comperator the comparator, eg. '=', '>=', '<=' 'LIKE'
     * @param negate true, if the results should NOT match the criteria
     * @param type the type of the column (numeric, string, uuid, date)
     * @param queue the queue where the resulting transformed value should be put in
     * @return the formatted AND-Clause for the prepared statement (AND xxx = ?)
     */
    private String formatWhereClause(String attr, String value, String comperator, boolean negate, FieldType type, Queue<Map.Entry<String, FieldType>> queue) {    
        //if it is a array => remove the brackets
        if (attr.endsWith("[]")) {
            attr = attr.substring(0,attr.length()-2);
        }
        
        //create meta object and add to data structure
        SingleParameter param = new SingleParameter(attr, comperator, negate, type, value);
        if (!this.getWhereParams().containsKey(attr)) {
            List<SingleParameter> list = new ArrayList<>();
            this.getWhereParams().put(attr, list);
        }
        this.getWhereParams().get(attr).add(param);
                
        
        //transform the parameter if a transformator is set
        if (this.transformators.containsKey(attr)) {
            //apply the transformator
            this.transformators.get(attr).transform(param);
            
            //set result to variables
            value = param.getValue();
            comperator = param.getComperator();
            negate = param.isNegated();
            type = param.getType();
            attr = param.getField();
        }
        
        //because we use aliases, some modifications have to be made
       if (attr.equals("model")) {
            attr = "(adm.fullname ILIKE ? OR t.model ILIKE ?)";
            queue.add(new AbstractMap.SimpleEntry<>(value, type));
            queue.add(new AbstractMap.SimpleEntry<>(value, type));
            if (!negate) {
                return " AND " + attr;
            } 
            else {
                return " AND NOT " + attr;
            }
        }
        else if (attr.equals("provider_name")) {
            attr = "(mprov.name ILIKE ? OR (mprov.name IS NULL AND  prov.name ILIKE ?))";
            queue.add(new AbstractMap.SimpleEntry<>(value, type));
            queue.add(new AbstractMap.SimpleEntry<>(value, type));
            if (!negate) {
                return " AND " + attr;
            } 
            else {
                return " AND NOT " + attr;
            }
        }
        else if (attr.equals("cursor") || attr.equals("max_results")) {
            return "";
        }
        else if (attr.equals("platform")) {
            attr = "(t.plattform ILIKE ? OR (t.plattform IS NULL AND t.client_name ILIKE ?))";
            queue.add(new AbstractMap.SimpleEntry<>(value, type));
            queue.add(new AbstractMap.SimpleEntry<>(value, type));
            if (!negate) {
                return " AND " + attr;
            } 
            else {
                return " AND NOT " + attr;
            }
        }
        else if (attr.equals("loc_accuracy")) {
        	attr = "t.geo_accuracy"; 
        	
        	//special case: if value > threshold -> ignore and find no results (?)
        	if (comperator.equals(">=") || comperator.equals("=")) {
	        	long val =  Long.parseLong(value);
	        	if (val > Double.parseDouble(settings.getString("RMBT_GEO_ACCURACY_DETAIL_LIMIT"))) {
	        		return " AND 1=0";
	        	}
        	}
        	
        	
        	//special case: if (-1) than NULL values should be found
        	if (value.equals("-1")) {
        		return " AND t.geo_accuracy IS NULL";
        	}
        }
        else if (attr.equals("ping_ms")) {
            attr = "t.ping_median";
            Double v = Double.parseDouble(value)*1000000;
            value = v.toString();
        }
        else {
            List<String> attrs = this.getDbFields(attr);
            if (attrs.size() == 1) {
                attr = attrs.get(0);
            }
        }
        
        //, zip_code are not renamed
        
        queue.add(new AbstractMap.SimpleEntry<>(value, type));
        if (negate) {
            return " AND NOT " + attr + " " + comperator + " ?";
        } else {
            return " AND " + attr + " " + comperator + " ?";
        }
    }
    
    /**
     * General method for matching open data fields to database fields
     * that require no further conversion
     * 
     * @param opendataField
     * @return the matching field in the database
     */
    public List<String> getDbFields(String opendataField) {
        List<String> ret = new LinkedList<>();
        if (opendataField.equals("download_kbit")) {
            ret.add("t.speed_download");
        }
        else if (opendataField.equals("upload_kbit")) {
            ret.add("t.speed_upload");
        }
        else if (opendataField.equals("ping_ms")) {
            ret.add("t.ping_median");
        }
        else if (opendataField.equals("time")) {
            ret.add("t.time");
        }
        else if (opendataField.equals("cat_technology")) {
            ret.add("nt.group_name");
        }
        else if (opendataField.equals("client_version")) {
            ret.add("client_software_version");
        }
        else if (opendataField.equals("model")) {
            ret.add("adm.fullname");
            ret.add("t.model");
        }
        else if (opendataField.equals("provider_name")) {
            ret.add("mprov.name");
            ret.add("prov.name");
        }
        else if (opendataField.equals("mobile_provider_name")) {
        	ret.add("mprov.name");	
        }
        else if (opendataField.equals("network_name")) {
            ret.add("network_operator_name");
        }
        else if (opendataField.equals("network_type")) {
            ret.add("t.network_group_type");
        }
        else if (opendataField.equals("platform")) {
            ret.add("t.plattform");
            ret.add("t.client_name");
        } 
        else if (opendataField.equals("signal_strength")) {
            ret.add("t.signal_strength");
        } 
        else if (opendataField.equals("open_uuid")) {
            ret.add("t.open_uuid");
        }
        else if (opendataField.equals("lat")) {
            ret.add("t.geo_lat");
        }
        else if (opendataField.equals("long")) {
            ret.add("t.geo_long");
        }
        else if (opendataField.equals("sim_mcc_mnc")) {
            ret.add("network_sim_operator");
        } 
        else if (opendataField.equals("sim_country")) {
            ret.add("network_sim_country");
        }
        else if (opendataField.equals("asn")) {
            ret.add("public_ip_asn");
        }
        else if (opendataField.equals("loc_accuracy")) {
        	ret.add("t.geo_accuracy");
        }
        else if (opendataField.equals("ip_anonym")) {
        	ret.add("client_public_ip_anonymized");
        }
        else if (opendataField.equals("implausible")) {
        	ret.add("t.implausible");
        }
        else if (opendataField.equals("pinned")) {
        	ret.add("t.pinned");
        }
         return ret;
    }
    
    /**
     * Fills in the given fields in the queue into the given prepared statement
     * @param ps
     * @param firstField
     * @return
     * @throws SQLException
     */
    public int fillInWhereClause(PreparedStatement ps, int firstField) throws SQLException{
    	//insert all values in the prepared statement in the order
        //in which the values had been put in the queue
        for (Map.Entry<String, FieldType> entry : searchValues){
            switch(entry.getValue()) {
                case STRING:
                    ps.setString(firstField, entry.getKey());
                    break;
                case DATE:
                    ps.setTimestamp(firstField, new Timestamp(Long.parseLong(entry.getKey())));
                    break;
                case LONG:
                    ps.setLong(firstField, Long.parseLong(entry.getKey()));
                    break;
                case DOUBLE:
                    ps.setDouble(firstField, Double.parseDouble(entry.getKey()));
                    break;
                case UUID:
                    ps.setObject(firstField, UUID.fromString(entry.getKey()));
                    break;
                case BOOLEAN:
                	ps.setBoolean(firstField, Boolean.valueOf(entry.getKey()));
                	break;
            }
            firstField++;
        }
        return firstField;
    }
    
    public boolean isDouble( String input )  
    {  
       try  
       {  
          Double v = Double.parseDouble(input);  
          if (v.isNaN() || v.isInfinite()){
              return false;
          }
          return true;  
       }  
       catch(Exception e)  
       {  
          return false;  
       }  
    }  
    
    public boolean isLong( String input )  
    {  
       try  
       {  
          Long v = Long.parseLong(input);  
          return true;  
       }  
       catch(Exception e)  
       {  
          return false;  
       }  
    }  
    
    /**
     * Formats a opendata-time-value to utc time
     * @param textual_date e.g. 2013-07-19 41:35
     * @return the date value OR -1 if the format is invalid
     * dz: add seconds
     */
    private static long parseDate(final String textual_date)
    {
        final SimpleDateFormat date_formatter = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        date_formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return date_formatter.parse(textual_date).getTime();
        } catch (ParseException ex) {
            return -1;
        }
    }
    
    
    public String getJoins() {
        return " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                " LEFT JOIN device_map adm ON adm.codename=t.model" +
                " LEFT JOIN test_server ts ON ts.uid=t.server_id" +
                " LEFT JOIN provider prov ON provider_id = prov.uid " +
                " LEFT JOIN provider mprov ON mobile_provider_id = mprov.uid" +
                " LEFT JOIN mccmnc2name msim ON mobile_sim_id = msim.uid ";
    }
    
    public static class SingleParameter {
        private String field;
        private String comperator;
        private boolean negated;
        private FieldType type;
        private String value;
            
        private SingleParameter (String field, String comperator, boolean negated, FieldType type, String value) {
            this.field = field;
            this.comperator = comperator;
            this.negated = negated;
            this.type = type;
            this.value = value;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getComperator() {
            return comperator;
        }

        public void setComperator(String comperator) {
            this.comperator = comperator;
        }

        public boolean isNegated() {
            return negated;
        }

        public void setNegated(boolean negated) {
            this.negated = negated;
        }

        public FieldType getType() {
            return type;
        }

        public void setType(FieldType type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
        
        
    }
    
    public interface SingleParameterTransformator {
        public void transform(SingleParameter param);
    }
    
    public void registerSingleParameterTransformator(String field, SingleParameterTransformator transformator) {
        if (!allowedFields.containsKey(field)) {
            throw new RuntimeException("invalid field " + field + " for transformator");
        }
        
        this.transformators.put(field, transformator);
    }
    
    @Override
    public int hashCode() {
        //return hashCode based on params
        StringBuilder completeQuery = new StringBuilder("");
        for (List<SingleParameter> list : this.getWhereParams().values()) {
            for (SingleParameter param : list) {
                completeQuery.append(";").append(param.getField()).append(param.getValue()).append(param.isNegated()).append(param.getComperator());
            }
        }
        return completeQuery.toString().hashCode();
    }
}
