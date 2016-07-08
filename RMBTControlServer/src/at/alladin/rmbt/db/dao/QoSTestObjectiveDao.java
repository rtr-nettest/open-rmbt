/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
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
package at.alladin.rmbt.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.alladin.rmbt.db.QoSTestObjective;
import at.alladin.rmbt.shared.Helperfunctions;

/**
 * 
 * @author lb
 *
 */
public class QoSTestObjectiveDao implements PrimaryKeyDao<QoSTestObjective, Integer> {

	/**
	 * 
	 */
	private final Connection conn;
	
	/**
	 * 
	 * @param conn
	 */
	public QoSTestObjectiveDao(final Connection conn) {
		this.conn = conn;
	}
	
	/**
	 * 
	 * @param testClass
	 * @return
	 * @throws SQLException 
	 */
	public List<QoSTestObjective> getByTestClass(Integer... testClass) throws SQLException {
		List<QoSTestObjective> resultList = new ArrayList<>(); 

		String whereClause = "WHERE test_class = " + Helperfunctions.join(" OR test_class = ", testClass);
		String sql = "SELECT nnto.uid, test_class, test, param AS param, results as results, concurrency_group, test_desc, "
				+ " test_summary, ts.web_address_ipv4 as ipv4, ts.web_address_ipv6 as ipv6, ts.port_ssl as port "
				+ " FROM qos_test_objective nnto LEFT JOIN test_server ts ON ts.uid = nnto.test_server " + whereClause;
		
		//System.out.println(sql);
		
		try (PreparedStatement psGetAll = conn.prepareStatement(sql))
		{
    		if (psGetAll.execute()) {
    			try (ResultSet rs = psGetAll.getResultSet())
    			{
        			while (rs.next()) {
        				resultList.add(instantiateItem(rs));				
        			}
    			}
    		}
    		else {
    			throw new SQLException("empty test result");
    		}
		}
		
		return resultList;
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.db.dao.PrimaryKeyDao#getById(java.lang.Object)
	 */
	@Override
	public QoSTestObjective getById(Integer id) throws SQLException {	
		try (PreparedStatement psGetById = conn.prepareStatement("SELECT nnto.uid, test_class, test, param AS param, "
				+ " hstore2json(results) as results, concurrency_group, test_desc, test_summary,"
				+ " ts.web_address_ipv4 as ipv4, ts.web_address_ipv6 as ipv6, ts.port_ssl as port "
				+ " FROM qos_test_objective nnto LEFT JOIN test_server ts ON ts.uid = nnto.test_server WHERE uid = " + id))
		{
    		if (psGetById.execute()) {
    			try (ResultSet rs = psGetById.getResultSet())
    			{
        			if (rs.next()) {
        				QoSTestObjective nnto = instantiateItem(rs);
        				return nnto;
        			}
        			else {
        				throw new SQLException("empty result set");
        			}
    			}
    		}
    		else {
    			throw new SQLException("no result set");
    		}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.db.dao.PrimaryKeyDao#getAll()
	 */
	@Override
	public List<QoSTestObjective> getAll() throws SQLException {
		List<QoSTestObjective> resultList = new ArrayList<>();
		
//		PreparedStatement psGetAll = conn.prepareStatement("SELECT nnto.uid, test_class, test, hstore2json(param) AS param, "
//				+ " hstore2json(results) as results, concurrency_group, test_desc, ts.web_address_ipv4 as ipv4, ts.web_address_ipv6 as ipv6, ts.port_ssl as port "
//				+ " FROM qos_test_objective nnto LEFT JOIN test_server ts ON ts.uid = nnto.test_server");
		
		String sql = "SELECT nnto.uid, test_class, test, param AS param, results as results, concurrency_group, test_desc, "
				+ " test_summary, ts.web_address_ipv4 as ipv4, ts.web_address_ipv6 as ipv6, ts.port_ssl as port "
				+ " FROM qos_test_objective nnto LEFT JOIN test_server ts ON ts.uid = nnto.test_server";

		try (PreparedStatement psGetAll = conn.prepareStatement(sql))
		{
    		if (psGetAll.execute()) {
    			try (ResultSet rs = psGetAll.getResultSet())
    			{
        			while (rs.next()) {
        				resultList.add(instantiateItem(rs));				
        			}
    			}
    		}
    		else {
    			throw new SQLException("item not found");
    		}
		}
		return resultList;
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public Map<String, List<QoSTestObjective>> getAllToMap() throws SQLException {
		List<QoSTestObjective> list = getAll();
		Map<String, List<QoSTestObjective>> testMap = new HashMap<>();
		
		for (QoSTestObjective test : list) {
			List<QoSTestObjective> testList = null;
			if (testMap.containsKey(test.getTestType())) {
				testList = testMap.get(test.getTestType());
			}
			else {
				testList = new ArrayList<>();
				testMap.put(test.getTestType(), testList);
			}
			testList.add(test);
		}
		
		return testMap;
	}

	/**
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	private static QoSTestObjective instantiateItem(ResultSet rs) throws SQLException {
		QoSTestObjective result = new QoSTestObjective();
		
		result.setUid(rs.getInt("uid"));
		result.setTestClass(rs.getInt("test_class"));
		result.setPort(rs.getInt("port"));
		result.setTestType(rs.getString("test"));
		result.setObjective(rs.getString("param"));
		result.setTestServerIpv4(rs.getString("ipv4"));
		result.setTestServerIpv6(rs.getString("ipv6"));
		result.setConcurrencyGroup(rs.getInt("concurrency_group"));
		result.setTestDescription(rs.getString("test_desc"));
		result.setTestSummary(rs.getString("test_summary"));
		result.setResults(rs.getString("results"));

		return result;
	}
}
