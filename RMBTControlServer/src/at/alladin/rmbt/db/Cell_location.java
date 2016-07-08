/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
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
import java.util.UUID;

import at.alladin.rmbt.shared.Helperfunctions;

public class Cell_location
{
    
    private UUID open_test_uuid;
	private long uid;
    private long test_id;
    private Timestamp time;
    private int location_id;
    private int area_code;
    private int primary_scrambling_code;
    private long time_ns;
    
    private Calendar timeZone = null;
    
    private Connection conn = null;
    private String errorLabel = "";
    private boolean error = false;
    
    public Cell_location(final Connection conn)
    {
        reset();
        this.conn = conn;
    }
    
    public Cell_location(final Connection conn, final UUID open_test_uuid, final long uid, final long test_id, final Timestamp time,
            final int location_id, final int area_code, final int primary_scrambling_code, final String timeZoneId, final long time_ns)
    {
        
        reset();
        
        this.conn = conn;
        
        this.open_test_uuid = open_test_uuid;
        this.uid = uid;
        this.test_id = test_id;
        this.time = time;
        this.time_ns = time_ns;
        this.location_id = location_id;
        this.area_code = area_code;
        this.primary_scrambling_code = primary_scrambling_code;
        
        timeZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);
    }
    
    public void reset()
    {
        
        open_test_uuid = null;
    	uid = 0;
        test_id = 0;
        time = null;
        location_id = 0;
        area_code = 0;
        primary_scrambling_code = 0;
        
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
    
    public void storeLocation()
    {
        PreparedStatement st;
        try
        {
            st = conn.prepareStatement(
                    "INSERT INTO cell_location(open_test_uuid, test_id, time, location_id, area_code, primary_scrambling_code, time_ns) "
                            + "VALUES(?, ?, ?, ?, ?, ?,?)", Statement.RETURN_GENERATED_KEYS);
            
            /*
             * Timestamp geotstamp = java.sql.Timestamp.valueOf(new Timestamp(
             * this.time).toString());
             */
            
            int i=1;
            
            st.setObject(i++,open_test_uuid);
            st.setLong(i++, test_id);
            st.setTimestamp(i++, time, timeZone);
            st.setInt(i++, location_id);
            st.setInt(i++, area_code);
            st.setInt(i++, primary_scrambling_code);
            st.setLong(i++, time_ns);
            
            //System.out.println(st.toString());
            
            final int affectedRows2 = st.executeUpdate();
            if (affectedRows2 == 0)
                setError("ERROR_DB_STORE_CELLLOCATION");
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
            setError("ERROR_DB_STORE_CELLLOCATION_SQL");
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
    
    public UUID getOpenTestUuid()
    {
        return open_test_uuid;
    }
    
    public long getUid()
    {
        return uid;
    }
    
    public long getTest_id()
    {
        return test_id;
    }
    
    public Timestamp getTime()
    {
        return time;
    }

	public long getTime_ns() {
		return time_ns;
	}

	public void setTime_ns(long time_ns) {
		this.time_ns = time_ns;
	}
    
    public int getLocation_id()
    {
        return location_id;
    }
    
    public int getArea_code()
    {
        return area_code;
    }
    
    public int getPrimary_scrambling_code()
    {
        return primary_scrambling_code;
    }
    
    public void setUid(final long uid)
    {
        this.uid = uid;
    }
    
    public void setOpenTestUuid(final UUID open_test_uuid)
    {
        this.open_test_uuid = open_test_uuid;
    }
    
    public void setTest_id(final long test_id)
    {
        this.test_id = test_id;
    }
    
    public void setTime(final Timestamp time, final String timeZoneId)
    {
        this.time = time;
        timeZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);
    }
    
    public void setLocation_id(final int location_id)
    {
        this.location_id = location_id;
    }
    
    public void setArea_code(final int area_code)
    {
        this.area_code = area_code;
    }
    
    public void setPrimary_scrambling_code(final int primary_scrambling_code)
    {
        this.primary_scrambling_code = primary_scrambling_code;
    }
    
}
