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
package at.alladin.rmbt.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import at.alladin.rmbt.db.QoSTestDesc;
import at.alladin.rmbt.qos.ResultDesc;

public class QoSTestDescDao implements PrimaryKeyDao<QoSTestDesc, Long> {

	/**
	 * 
	 */
	private final Connection conn;
	
	/**
	 * 
	 */
	private Locale locale;
	
	/**
	 * 
	 * @param conn
	 */
	public QoSTestDescDao(final Connection conn, Locale locale) {
		this.conn = conn;
		this.locale = locale;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.db.dao.PrimaryKeyDao#getById(java.lang.Object)
	 */
	@Override
	public QoSTestDesc getById(Long id) throws SQLException {
		try (PreparedStatement psGetById = conn.prepareStatement("SELECT uid, desc_key AS key, value, lang FROM qos_test_desc WHERE uid = ?"))
		{
    		psGetById.setLong(1, id);
    		
    		if (psGetById.execute()) {
    			try (ResultSet rs = psGetById.getResultSet())
    			{
        			if (rs.next()) {
        				QoSTestDesc nntd = instantiateItem(rs);
        				return nntd;
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
	public List<QoSTestDesc> getAll() throws SQLException {
		List<QoSTestDesc> resultList = new ArrayList<>();

		String[] locales = getWhereInLocales(locale);	
		try (PreparedStatement psGetAll = conn.prepareStatement("SELECT  DISTINCT " +
		        " COALESCE(b.uid, a.uid) uid, " +
		        " COALESCE(b.desc_key, a.desc_key) \"key\", " +
		        " COALESCE(b.value, a.value) \"value\", " +
		        " COALESCE(b.lang, a.lang) lang " + 
		        " FROM qos_test_desc a " +
		        " LEFT JOIN (" +
				" 	SELECT uid, desc_key, \"value\", lang " +
				" 	FROM qos_test_desc " +
				" 	WHERE lang = ? " +
				" ) b ON a.uid <> b.uid AND a.desc_key = b.desc_key " + 
				" WHERE a.lang IN (" + DaoUtil.preparePlaceHolders(locales.length) + ")"))
		{
    		psGetAll.setString(1, locale.getLanguage());
    		DaoUtil.setStrings(psGetAll, 2, locales);
    		
    		if (psGetAll.execute()) {
    			try (ResultSet rs = psGetAll.getResultSet())
    			{
        			while (rs.next()) {
        				resultList.add(instantiateItem(rs));
        			}
        			return resultList;
    			}
    		}
    		else {
    			throw new SQLException("no result set");
    		}
		}
	}
	
	/**
	 * 
	 * @param resultSet
	 * @return
	 * @throws SQLException 
	 */
	public void loadToTestDesc(Collection<ResultDesc> resultCollection) throws SQLException {
		String[] keys = new String[resultCollection.size()];
		Iterator<ResultDesc> iterator = resultCollection.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			ResultDesc desc = iterator.next();
			if (desc.getKey() != null) {
				keys[i++] = desc.getKey();				
			}
			else {
				iterator.remove();
			}
		}
		
		Map<String, String> resultMap = getAllByKeyToMap(keys);
		
		if (resultMap != null && resultMap.size() > 0) {
			iterator = resultCollection.iterator();
			i = 0;
			while (iterator.hasNext()) {
				ResultDesc desc = iterator.next();
				desc.setValue(resultMap.get(desc.getKey()));
			}					
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public String[] getWhereInLocales(Locale locale) {
		String[] locales = null;
		if (new Locale("en").getLanguage().equals(locale.getLanguage())) {
			locales = new String[] {"en"};
		}
		else {
			locales = new String[] {"en", locale.getLanguage()};
		}
		return locales;
	}
	
	/**
	 * 
	 * @param keys
	 * @return
	 * @throws SQLException 
	 */
	public List<QoSTestDesc> getAllByKey(String... keys) throws SQLException {
		List<QoSTestDesc> resultList = new ArrayList<>();
		
		String[] locales = getWhereInLocales(locale);	
		try (PreparedStatement psGetAll = conn.prepareStatement("SELECT  DISTINCT " +
		        " COALESCE(b.uid, a.uid) uid, " +
		        " COALESCE(b.desc_key, a.desc_key) \"key\", " +
		        " COALESCE(b.value, a.value) \"value\", " +
		        " COALESCE(b.lang, a.lang) lang " + 
		        " FROM qos_test_desc a " +
		        " LEFT JOIN (" +
				" 	SELECT uid, desc_key, \"value\", lang " +
				" 	FROM qos_test_desc " +
				" 	WHERE lang = ? " +
				" ) b ON a.uid <> b.uid AND a.desc_key = b.desc_key " + 
				" WHERE a.lang IN (" + DaoUtil.preparePlaceHolders(locales.length) + ") AND a.desc_key IN (" + DaoUtil.preparePlaceHolders(keys.length) + ") "))
		{
    		psGetAll.setString(1, locale.getLanguage());
    		DaoUtil.setStrings(psGetAll, 2, locales);
    		DaoUtil.setStrings(psGetAll, 2 + locales.length, keys);
    		
    		if (psGetAll.execute()) {
    			try (ResultSet rs = psGetAll.getResultSet())
    			{
        			while (rs.next()) {
        				resultList.add(instantiateItem(rs));				
        			}
        			
        			return resultList;
    			}
    		}
    		else {
    			throw new SQLException("no result set");
    		}
		}
	}
	
	/**
	 * 
	 * @param keys
	 * @return
	 * @throws SQLException 
	 */
	public List<QoSTestDesc> getAllByKey(Collection<String> keys) throws SQLException {
		String[] keysArray = new String[keys.size()];
		return getAllByKey(keys.toArray(keysArray));
	} 
	
	/**
	 * 
	 * @param keys
	 * @return
	 * @throws SQLException 
	 */
	public HashMap<String, String> getAllByKeyToMap(String... keys) throws SQLException {
		HashMap<String, String> resultMap = new HashMap<>();
		
		if (keys != null && keys.length > 0) {
			String[] locales = getWhereInLocales(locale);
			String sql = "SELECT  DISTINCT " +
			        " COALESCE(b.uid, a.uid) uid, " +
			        " COALESCE(b.desc_key, a.desc_key) \"key\", " +
			        " COALESCE(b.value, a.value) \"value\", " +
			        " COALESCE(b.lang, a.lang) lang " + 
			        " FROM qos_test_desc a " +
			        " LEFT JOIN (" +
					" 	SELECT uid, desc_key, \"value\", lang " +
					" 	FROM qos_test_desc " +
					" 	WHERE lang = ? " +
					" ) b ON a.uid <> b.uid AND a.desc_key = b.desc_key " + 
					" WHERE a.lang IN (" + DaoUtil.preparePlaceHolders(locales.length) + ") AND a.desc_key IN (" + DaoUtil.preparePlaceHolders(keys.length) + ") ";
			
			try (PreparedStatement psGetAll = conn.prepareStatement(sql))
			{
    			psGetAll.setString(1, locale.getLanguage());
    			DaoUtil.setStrings(psGetAll, 2, locales);
    			DaoUtil.setStrings(psGetAll, 2 + locales.length, keys);
    			
    			if (psGetAll.execute()) {
    				try (ResultSet rs = psGetAll.getResultSet())
    				{
        				while (rs.next()) {
        					QoSTestDesc item = instantiateItem(rs);
        					resultMap.put(item.getKey(), item.getValue());				
        				}
    				}
    				return resultMap;
    			}
    			else {
    				throw new SQLException("no result set");
    			}
			}
		}
		
		return resultMap;
	}
	
	/**
	 * 
	 * @param keys
	 * @return
	 * @throws SQLException
	 */
	public HashMap<String, String> getAllByKeyToMap(Collection<String> keys) throws SQLException {
		String[] keysArray = new String[keys.size()];
		return getAllByKeyToMap(keys.toArray(keysArray));
	}
	
	public HashMap<String, List<QoSTestDesc>> getAllToMapIgnoreLang() throws SQLException {
		HashMap<String, List<QoSTestDesc>> resultMap = new HashMap<>();

		String sql = "SELECT uid, desc_key as \"key\", \"value\", lang FROM qos_test_desc ORDER BY desc_key, lang";		
		try (PreparedStatement psGetAll = conn.prepareStatement(sql))
		{
    		if (psGetAll.execute()) {
    			try (ResultSet rs = psGetAll.getResultSet())
    			{
        			while (rs.next()) {
        				QoSTestDesc item = instantiateItem(rs);
        				List<QoSTestDesc> list = null;
        				if (resultMap.containsKey(item.getKey())) {
        					list = resultMap.get(item.getKey());
        				}
        				else {
        					list = new ArrayList<>();
        					resultMap.put(item.getKey(), list);
        				}
        				list.add(item);
        			}
    			}
    			return resultMap;
    		}
    		else {
    			throw new SQLException("no result set");
    		}
		}
	}
	
	/**
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException 
	 */
	private static QoSTestDesc instantiateItem(ResultSet rs) throws SQLException {
		QoSTestDesc result = new QoSTestDesc();
		
		result.setUid(rs.getLong("uid"));
		result.setValue(rs.getString("value"));
		result.setKey(rs.getString("key"));
		result.setLang(rs.getString("lang"));
		return result;
	}

}
