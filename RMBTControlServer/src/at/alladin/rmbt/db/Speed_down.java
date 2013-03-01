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
import java.sql.Timestamp;
import java.util.Calendar;

import at.alladin.rmbt.shared.Helperfunctions;

public class Speed_down
{
    
    private long uid;
    private long test_id;
    private Timestamp tstamp;
    private long sumAllTrans;
    private long sumDiffTrans;
    private long maxAllTime;
    private long maxDiffTime;
    
    private Calendar timeZone;
    
    private Connection conn = null;
    
    private String errorLabel = "";
    
    private boolean error = false;
    
    public Speed_down(final Connection conn)
    {
        reset();
        this.conn = conn;
    }
    
    public Speed_down(final Connection conn, final long uid, final long test_id, final Timestamp tstamp,
            final long sumAllTrans, final long sumDiffTrans, final long maxAllTime, final long maxDiffTime,
            final Calendar timeZone)
    {
        reset();
        this.conn = conn;
        
        this.uid = uid;
        this.test_id = test_id;
        this.tstamp = tstamp;
        this.sumAllTrans = sumAllTrans;
        this.sumDiffTrans = sumDiffTrans;
        this.maxAllTime = maxAllTime;
        this.maxDiffTime = maxDiffTime;
        
        this.timeZone = timeZone;
    }
    
    public void reset()
    {
        
        uid = 0;
        test_id = 0;
        tstamp = null;
        sumAllTrans = 0;
        sumDiffTrans = 0;
        maxAllTime = 0;
        maxDiffTime = 0;
        
        timeZone = null;
        
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
    
    public boolean hasError()
    {
        return error;
    }
    
    public String getError()
    {
        return errorLabel;
    }
    
    public void storeSpeed_down()
    {
        resetError();
        
        try
        {
            PreparedStatement st;
            
            st = conn.prepareStatement(
                    "INSERT INTO speed_down(test_id, time, sumAllTrans, sumDiffTrans, maxAllTime, maxDiffTime)"
                            + "VALUES( ? , ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            
            st.setLong(1, test_id);
            
            if (timeZone == null)
                st.setTimestamp(2, tstamp);
            else
                st.setTimestamp(2, tstamp, timeZone);
            
            st.setLong(3, sumAllTrans);
            st.setLong(4, sumDiffTrans);
            st.setLong(5, maxAllTime);
            st.setLong(6, maxDiffTime);
            
            // System.out.println(st.toString());
            
            final int affectedRows = st.executeUpdate();
            if (affectedRows == 0)
            {
                reset();
                setError("ERROR_DB_STORE_SPEED_DOWN");
                // errorList.addError(labels.getString("ERROR_DB_STORE_CLIENT"));
            }
            else
            {
                final ResultSet rs = st.getGeneratedKeys();
                if (rs.next())
                    // Retrieve the auto generated key(s).
                    uid = rs.getLong(1);
            }
            st.close();
        }
        catch (final SQLException e)
        {
            setError("ERROR_DB_STORE_SPEED_DOWN_SQL");
            // e.printStackTrace();
        }
    }
    
    public long getUid()
    {
        return uid;
    }
    
    public long getTest_id()
    {
        return test_id;
    }
    
    public Timestamp getTstamp()
    {
        return tstamp;
    }
    
    public long getSumAllTrans()
    {
        return sumAllTrans;
    }
    
    public long getSumDiffTrans()
    {
        return sumDiffTrans;
    }
    
    public long getMaxAllTime()
    {
        return maxAllTime;
    }
    
    public long getMaxDiffTime()
    {
        return maxDiffTime;
    }
    
    public void setUid(final long uid)
    {
        this.uid = uid;
    }
    
    public void setTest_id(final long test_id)
    {
        this.test_id = test_id;
    }
    
    public void setTstamp(final Timestamp tstamp, final String timeZoneId)
    {
        this.tstamp = tstamp;
        timeZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);
    }
    
    public void setSumAllTrans(final long sumAllTrans)
    {
        this.sumAllTrans = sumAllTrans;
    }
    
    public void setSumDiffTrans(final long sumDiffTrans)
    {
        this.sumDiffTrans = sumDiffTrans;
    }
    
    public void setMaxAllTime(final long maxAllTime)
    {
        this.maxAllTime = maxAllTime;
    }
    
    public void setMaxDiffTime(final long maxDiffTime)
    {
        this.maxDiffTime = maxDiffTime;
    }
    
}
