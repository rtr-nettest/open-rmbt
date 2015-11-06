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
 ******************************************************************************
 */
package at.alladin.rmbt.statisticServer.opendata;

import at.alladin.rmbt.statisticServer.ServerResource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.resource.Get;

/**
 *
 * @author Thomas
 */
public class ChoicesResource extends ServerResource {
    
    //all fields for which the user can get choices
    private static final HashSet<String> openDataFieldsSortable = new HashSet<>(Arrays.asList(
            new String[]{"country_geoip","provider","platform","asn","mobile_provider_name"}));
    
    
    @Get("json")
    public String request(final String entity) throws JSONException
    {
        QueryParser qp = new QueryParser();
        Form parameters = getRequest().getResourceRef().getQueryAsForm();
        qp.parseQuery(parameters);
        
        List<String> fields = Arrays.asList(new String[]{"country_geoip"});
 
        final JSONObject answer = new JSONObject();
        final JSONArray countries = new JSONArray(queryDB("upper(msim.country)", "t.mobile_network_id", "mccmnc2name msim ON msim.uid", qp));
        final JSONArray provider = new JSONArray(queryDB("mprov.name", "t.mobile_provider_id", "provider mprov ON mprov.uid", qp));
        final JSONArray providerN = new JSONArray(queryDB("prov.name", "t.provider_id", "provider prov ON prov.uid", qp));
        
        
        
        answer.put("country_mobile", countries);
        answer.put("provider_mobile", provider);
        answer.put("provider", providerN);
        
        return answer.toString();
    }
    
    private Set<String> queryDB(String dbField, String dbKey, String join, QueryParser qp) {
        Set<String> countries = new TreeSet<>();
        String sql = "WITH RECURSIVE t1(n) AS ( "
                + "SELECT MIN(" + dbKey + ") FROM test t " + qp.getJoins() + qp.getWhereClause("WHERE")
                + " UNION"
                + " SELECT (SELECT " + dbKey + " FROM test t "
                + qp.getJoins() + "WHERE " + dbKey + " > n" + qp.getWhereClause("AND")
                + " ORDER BY " + dbKey + " LIMIT 1)"
                + " FROM t1 "
                + " )"
                + "SELECT " + dbField + " FROM t1 LEFT JOIN " + join + "=n WHERE NOT " + dbField + " IS NULL GROUP BY " + dbField + ";";
        System.out.println(sql);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            
            //fill in
            int newIndex;
            newIndex = qp.fillInWhereClause(ps, 1);
            qp.fillInWhereClause(ps, newIndex);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                countries.add(rs.getString(1));
            }
            return countries;
        } catch (SQLException ex) {
            Logger.getLogger(ChoicesResource.class.getName()).log(Level.SEVERE, null, ex);
            return countries;
        }
    }
}
