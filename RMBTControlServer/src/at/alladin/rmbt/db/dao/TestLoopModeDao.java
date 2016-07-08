/*******************************************************************************
 * Copyright 2016 Specure GmbH
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
 *******************************************************************************/
package at.alladin.rmbt.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import at.alladin.rmbt.util.model.shared.LoopModeSettings;

public class TestLoopModeDao implements CrudPrimaryKeyDao<LoopModeSettings, Long> {

	
	/**
	 * 
	 */
	private final Connection conn;
	
	/**
	 * 
	 */
	private final Gson gson = new Gson();
	
	/**
	 * 
	 * @param conn
	 */
	public TestLoopModeDao(final Connection conn) {
		this.conn = conn;
	}

	@Override
	public LoopModeSettings getById(Long id) throws SQLException {
		try (PreparedStatement psGetById = conn.prepareStatement("SELECT row_to_json(test_loopmode) FROM test_loopmode WHERE uid = ?"))
		{
    		psGetById.setLong(1, id);
    		
    		if (psGetById.execute()) {
    			try (ResultSet rs = psGetById.getResultSet())
    			{
        			if (rs.next()) {
        				return gson.fromJson(rs.getString(0), LoopModeSettings.class);
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

	@Override
	public List<LoopModeSettings> getAll() throws SQLException {
		List<LoopModeSettings> resultList = new ArrayList<>();

		try (PreparedStatement psGetAll = conn.prepareStatement("SELECT row_to_json(test_loopmode) FROM test_loopmode"))
		{
    		if (psGetAll.execute()) {
    			try (ResultSet rs = psGetAll.getResultSet())
    			{
        			while (rs.next()) {
        				resultList.add(gson.fromJson(rs.getString(0), LoopModeSettings.class));
        			}
        			return resultList;
    			}
    		}
    		else {
    			throw new SQLException("no result set");
    		}
		}
	}

	@Override
	public int delete(LoopModeSettings entity) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int save(LoopModeSettings entity) throws SQLException {
		return update(entity);
	}

	@Override
	public int update(LoopModeSettings entity) throws SQLException {
		String sql;
		
		PreparedStatement ps = null;
		
		if (entity.getUid() == null) {
			sql = "INSERT INTO test_loopmode (test_uuid, max_delay, max_movement, max_tests, test_counter, client_uuid) VALUES (?::uuid,?,?,?,?,?::uuid)";
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setObject(1, entity.getTestUuid());
			ps.setInt(2, entity.getMaxDelay());
			ps.setInt(3, entity.getMaxMovement());
			ps.setInt(4, entity.getMaxTests());
			ps.setInt(5, entity.getTestCounter());
			ps.setObject(6, entity.getClientUuid());
		}
		else {
			sql = "UPDATE test_loopmode SET test_uuid = ?::uuid, max_delay = ?, max_movement = ?, max_tests = ?, test_counter = ?, client_uuid = ?::uuid WHERE uid = ?";
			ps = conn.prepareStatement(sql);
			ps.setObject(1, entity.getTestUuid());
			ps.setInt(2, entity.getMaxDelay());
			ps.setInt(3, entity.getMaxMovement());
			ps.setInt(4, entity.getMaxTests());
			ps.setInt(5, entity.getTestCounter());
			ps.setObject(6, entity.getClientUuid());
			ps.setLong(7,  entity.getUid());
		}

		final int updateReturn = ps.executeUpdate();
		if (ps.getGeneratedKeys() != null) {
			final ResultSet keySet = ps.getGeneratedKeys();
			if (keySet.next()) {
				entity.setUid(keySet.getLong(1));
			}
		}
		
		return updateReturn;
	}

}
