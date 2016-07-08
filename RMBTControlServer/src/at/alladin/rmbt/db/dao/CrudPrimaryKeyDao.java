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

import java.sql.SQLException;

/**
 * 
 * @author lb
 *
 * @param <E> entity type
 * @param <K> primary key type
 */
public interface CrudPrimaryKeyDao<E, K> extends PrimaryKeyDao<E, K> {
	
	public int delete(E entity) throws SQLException;
	
	public int save(E entity) throws SQLException;
	
	public int update(E entity) throws SQLException;
}
