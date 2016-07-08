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
package at.alladin.rmbt.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DbConnection
{
    
    private static final DataSource ds;
    
    static
    {
        DataSource _ds = null;
        try
        {
            _ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/rmbt");
        }
        catch (final NamingException e)
        {
        }
        ds = _ds;
    }
    
    public static Connection getConnection() throws NamingException, SQLException
    {
        final Connection connection = ds.getConnection();
        connection.setAutoCommit(true);
        return connection;
    }
}
