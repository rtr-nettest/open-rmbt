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

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 
 * @author lb
 *
 */
public class DaoUtil {
	
	/**
	 * 
	 * @param length
	 * @return
	 */
	public static String preparePlaceHolders(int length) {
	    StringBuilder builder = new StringBuilder();
	    for (int i = 0; i < length;) {
	        builder.append("?");
	        if (++i < length) {
	            builder.append(",");
	        }
	    }
	    return builder.toString();
	}

	/**
	 * 
	 * @param preparedStatement
	 * @param values
	 * @throws SQLException
	 */
	public static void setStrings(PreparedStatement preparedStatement, String... values) throws SQLException {
		setStrings(preparedStatement, 1, values);
	}

	/**
	 * 
	 * @param preparedStatement
	 * @param start
	 * @param values
	 * @throws SQLException
	 */
	public static void setStrings(PreparedStatement preparedStatement, int start, String... values) throws SQLException {
	    for (int i = 0; i < values.length; i++) {
	        preparedStatement.setString(start + i, values[i]);
	    }
	}
}
