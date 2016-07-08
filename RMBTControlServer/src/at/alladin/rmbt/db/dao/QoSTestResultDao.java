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
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import at.alladin.rmbt.db.QoSTestResult;

/**
 * 
 * @author lb
 *
 */
public class QoSTestResultDao implements CrudPrimaryKeyDao<QoSTestResult, Long> {

	private final Connection conn;
	
	/**
	 * 
	 * @param conn
	 */
	public QoSTestResultDao(Connection conn) {
		this.conn = conn;
	}
	
	/**
	 * 
	 * @param testUuid
	 * @return
	 * @throws SQLException
	 */
	public List<QoSTestResult> getByTestUid(Long testUid) throws SQLException {
		List<QoSTestResult> resultList = new ArrayList<>();
		
		try (PreparedStatement psGetAll = conn.prepareStatement("SELECT nntr.uid, test_uid, success_count, failure_count, nnto.test, result AS result, "
				+ " nnto.results as results, qos_test_uid, nnto.test_desc, nnto.test_summary FROM qos_test_result nntr "
				+ " JOIN qos_test_objective nnto ON nntr.qos_test_uid = nnto.uid WHERE test_uid = ? AND nntr.deleted = 'FALSE' and nntr.implausible = 'FALSE'"))
		{
    		psGetAll.setLong(1, testUid);
    		
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
    		
    		return resultList;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.db.dao.PrimaryKeyDao#getById(java.lang.Object)
	 */
	@Override
	public QoSTestResult getById(Long id) throws SQLException {
		try (PreparedStatement psGetById = conn.prepareStatement("SELECT nntr.uid, test_uid, nnto.test, success_count, failure_count, result AS result, "
				+ " nnto.results as results, qos_test_uid, nnto.test_desc, nnto.test_summary FROM qos_test_result nntr "
				+ " JOIN qos_test_objective nnto ON nntr.qos_test_uid = nnto.uid WHERE nntr.uid = ? AND nntr.deleted = 'FALSE' and nntr.implausible = 'FALSE'"))
		{
    		psGetById.setLong(1, id);
    		
    		if (psGetById.execute()) {
    			try (ResultSet rs = psGetById.getResultSet())
    			{
        			if (rs.next()) {
        				QoSTestResult nntr = instantiateItem(rs);
        				return nntr;
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
	public List<QoSTestResult> getAll() throws SQLException {
		List<QoSTestResult> resultList = new ArrayList<>();
		
		try (PreparedStatement psGetAll = conn.prepareStatement("SELECT nntr.uid, test_uid, nnto.test, success_count, failure_count, result AS result, "
				+ " nnto.results as results, qos_test_uid, nnto.test_desc, nnto.test_summary FROM qos_test_result nntr "
				+ " JOIN qos_test_objective nnto ON nntr.qos_test_uid = nnto.uid"))
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
    		
    		return resultList;
		}
	}
	

	@Override
	public int update(QoSTestResult entity) throws SQLException {
		return save(entity);
	}
	
	/**
	 * 
	 * @param result
	 * @throws SQLException
	 */
	public int save(QoSTestResult result) throws SQLException {
		String sql;
		
		PreparedStatement ps = null;
		
		if (result.getUid() == null) {
			sql = "INSERT INTO qos_test_result (test_uid, result, qos_test_uid, success_count, failure_count) VALUES (?,?::json,?,?,?)";
			ps = conn.prepareStatement(sql);
			ps.setLong(1, result.getTestUid());
			ps.setObject(2, result.getResults());
			ps.setLong(3, result.getQoSTestObjectiveId());
			ps.setInt(4, result.getSuccessCounter());
			ps.setInt(5, result.getFailureCounter());
		}
		else {
			sql = "UPDATE qos_test_result SET test_uid = ?, result = ?::json, qos_test_uid = ?, success_count = ?, failure_count = ? WHERE uid = ?";
			ps = conn.prepareStatement(sql);
			ps.setLong(1, result.getTestUid());
			ps.setObject(2, result.getResults());
			ps.setLong(3, result.getQoSTestObjectiveId());
			ps.setInt(4, result.getSuccessCounter());
			ps.setInt(5, result.getFailureCounter());
			ps.setLong(6, result.getUid());
		}

		return ps.executeUpdate();
	}
	
	/**
	 * 
	 * @param resultCollection
	 * @throws SQLException
	 */
	public void saveAll(Collection<QoSTestResult> resultCollection) throws SQLException {
		for (QoSTestResult result : resultCollection) {
			save(result);
		}
	}

	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	public PreparedStatement getUpdateCounterPreparedStatement() throws SQLException {
		String sql = "UPDATE qos_test_result SET success_count = ?, failure_count = ? WHERE uid = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		return ps;
	}
	
	/**
	 * 
	 * @param result
	 * @return
	 * @throws SQLException
	 */
	public int updateCounter(QoSTestResult result) throws SQLException {
		String sql = "UPDATE qos_test_result SET success_count = ?, failure_count = ? WHERE uid = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		return updateCounter(result, ps);
	}
	
	/**
	 * 
	 * @param result
	 * @param columnNames
	 * @throws SQLException
	 */
	public int updateCounter(QoSTestResult result, PreparedStatement ps) throws SQLException {
		ps.setInt(1, result.getSuccessCounter());
		ps.setInt(2, result.getFailureCounter());
		ps.setLong(3, result.getUid());
		return ps.executeUpdate();
	}
	
	/**
	 * 
	 * @param resultCollection
	 * @param columnNames
	 * @throws SQLException
	 */
	public void updateCounterAll(Collection<QoSTestResult> resultCollection) throws SQLException {
		String sql = "UPDATE qos_test_result SET success_count = ?, failure_count = ? WHERE uid = ?";
		PreparedStatement ps = conn.prepareStatement(sql);

		for (QoSTestResult result : resultCollection) {
			updateCounter(result, ps);
		}
	}

	/**
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	private static QoSTestResult instantiateItem(ResultSet rs) throws SQLException {
		QoSTestResult result = new QoSTestResult();
		
		result.setUid(rs.getLong("uid"));
		result.setTestType(rs.getString("test"));
		result.setResults(rs.getString("result"));
		result.setTestUid(rs.getLong("test_uid"));
		result.setQoSTestObjectiveId(rs.getLong("qos_test_uid"));
		result.setTestDescription(rs.getString("test_desc"));
		result.setTestSummary(rs.getString("test_summary"));
		result.setSuccessCounter(rs.getInt("success_count"));
		result.setFailureCounter(rs.getInt("failure_count"));
		
		final String results = rs.getString("results");
		try {
			result.setExpectedResults(results != null ? new JSONArray(results) : null);
		} catch (JSONException e) {
			result.setExpectedResults(null);
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public int delete(QoSTestResult entity) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}
}
