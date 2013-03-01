/*******************************************************************************
 * Copyright 2013 alladin-IT OG
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Ping
{
    private long uid;
    private long test_id;
    private long value;
    
    private Connection conn = null;
    private String errorLabel = "";
    private boolean error = false;
    
    public Ping(final Connection conn)
    {
        reset();
        this.conn = conn;
    }
    
    public Ping(final Connection conn, final long uid, final long test_id, final long value)
    {
        
        reset();
        
        this.conn = conn;
        
        this.uid = uid;
        this.test_id = test_id;
        this.value = value;
        
    }
    
    public void reset()
    {
        
        uid = 0;
        test_id = 0;
        value = 0;
        
        resetError();
    }
    
    private void resetError()
    {
        error = false;
        errorLabel = "";
    }
    
    private void setError(final String errorLabel)
    {
        error = true;
        this.errorLabel = errorLabel;
    }
    
    public void storePing()
    {
        PreparedStatement st;
        try
        {
            st = conn.prepareStatement("INSERT INTO ping(test_id, value) " + "VALUES( ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            
            st.setLong(1, test_id);
            st.setLong(2, value);
            
            // System.out.println(st2.toString());
            
            final int affectedRows2 = st.executeUpdate();
            if (affectedRows2 == 0)
                setError("ERROR_DB_STORE_PING");
            else
            {
                final ResultSet rs = st.getGeneratedKeys();
                if (rs.next())
                    // Retrieve the auto generated key(s).
                    uid = rs.getInt(1);
            }
            st.close();
        }
        catch (final SQLException e)
        {
            setError("ERROR_DB_STORE_PING_SQL");
            e.printStackTrace();
        }
    }
    
    public boolean hasError()
    {
        return error;
    }
    
    public String getError()
    {
        return errorLabel;
    }
    
    public long getUid()
    {
        return uid;
    }
    
    public long getTest_id()
    {
        return test_id;
    }
    
    public long getValue()
    {
        return value;
    }
    
    public void setUid(final long uid)
    {
        this.uid = uid;
    }
    
    public void setTest_id(final long test_id)
    {
        this.test_id = test_id;
    }
    
    public void setValue(final long value)
    {
        this.value = value;
    }
    
}
