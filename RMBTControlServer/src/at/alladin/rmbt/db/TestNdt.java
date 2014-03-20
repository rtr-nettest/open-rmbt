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
package at.alladin.rmbt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import at.alladin.rmbt.db.fields.DoubleField;
import at.alladin.rmbt.db.fields.Field;
import at.alladin.rmbt.db.fields.LongField;
import at.alladin.rmbt.db.fields.StringField;

public class TestNdt extends Table
{
    
    private final static String SELECT = "SELECT" + " *" + " FROM test_ndt tn";
    
    private final static ThreadLocal<Field[]> PER_THREAD_FIELDS = new ThreadLocal<Field[]>() {
        protected Field[] initialValue() {
            return new Field[] {
                    new LongField("test_id", null),
                    new DoubleField("s2cspd", "s2cspd"),
                    new DoubleField("c2sspd", "c2sspd"),
                    new DoubleField("avgrtt", "avgrtt"),
                    new StringField("main", "main"),
                    new StringField("stat", "stat"),
                    new StringField("diag", "diag"),
                    new LongField("time_ns", "time_ns"),
                    new LongField("time_end_ns", "time_end_ns")
                    };
        }
    };
    
    public TestNdt(final Connection conn)
    {
        super(PER_THREAD_FIELDS.get(), conn);
    }
    
    public void storeTest()
    {
        try
        {
            final StringBuilder keys = new StringBuilder();
            final StringBuilder values = new StringBuilder();
            for (final Field field : fields)
                if (!field.isReadOnly())
                {
                    field.appendDbKey(keys);
                    field.appendDbValue(values);
                }
            
            PreparedStatement st;
            st = conn.prepareStatement(String.format("INSERT INTO test_ndt " + "(%s) VALUES (%s)", keys, values),
                    Statement.RETURN_GENERATED_KEYS);
            
            int idx = 1;
            for (final Field field : fields)
                if (!field.isReadOnly())
                    field.getField(st, idx++);
            
            final int affectedRows = st.executeUpdate();
            if (affectedRows == 0)
                setError("ERROR_DB_STORE_TEST");
            else
            {
                final ResultSet rs = st.getGeneratedKeys();
                if (rs.next())
                    uid = rs.getLong(1);
                rs.close();
            }
        }
        catch (final SQLException e)
        {
            setError("ERROR_DB_STORE_TEST_SQL");
            e.printStackTrace();
        }
    }
    
    public boolean loadByTestId(final long testId)
    {
        PreparedStatement st = null;
        ResultSet rs = null;
        try
        {
            st = conn.prepareStatement(SELECT + " WHERE tn.test_id = ?");
            st.setLong(1, testId);
            rs = st.executeQuery();
            
            if (rs.next())
                setValuesFromResult(rs);
            else
                return false;
            
            return true;
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (rs != null)
                    rs.close();
                if (st != null)
                    st.close();
            }
            catch (final SQLException e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }
}
