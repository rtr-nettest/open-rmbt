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
import java.sql.Timestamp;
import java.util.Calendar;

import at.alladin.rmbt.shared.Helperfunctions;

public class News
{
    
    private int uid;
    private Timestamp tstamp;
    private String title_en;
    private String title_de;
    private String text_en;
    private String text_de;
    private boolean active;
    
    private Calendar timeZone;
    
    private Connection conn = null;
    private String errorLabel = "";
    private boolean error = false;
    
    public News(final Connection conn)
    {
        reset();
        this.conn = conn;
    }
    
    public News(final Connection conn, final int uid, final Timestamp tstamp, final String title_en,
            final String title_de, final String text_en, final String text_de, final boolean active)
    {
        
        reset();
        
        this.conn = conn;
        
        this.uid = uid;
        this.tstamp = tstamp;
        this.title_en = title_en;
        this.title_de = title_de;
        this.text_en = text_en;
        this.text_de = text_de;
        this.active = active;
        
    }
    
    public void reset()
    {
        
        uid = 0;
        tstamp = null;
        title_en = "";
        title_de = "";
        text_en = "";
        text_de = "";
        active = false;
        
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
    
    public void storeNews()
    {
        PreparedStatement st;
        try
        {
            st = conn.prepareStatement("INSERT INTO news(tstamp, title_en, title_de, text_en, text_de, active) "
                    + "VALUES( ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            
            if (timeZone == null)
                st.setTimestamp(1, tstamp);
            else
                st.setTimestamp(1, tstamp, timeZone);
            
            st.setString(2, title_en);
            st.setString(3, title_de);
            st.setString(4, text_en);
            st.setString(5, text_de);
            st.setBoolean(6, active);
            
            // System.out.println(st2.toString());
            
            final int affectedRows1 = st.executeUpdate();
            if (affectedRows1 == 0)
                setError("ERROR_DB_STORE_NEWS");
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
            setError("ERROR_DB_STORE_NEWS_SQL");
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
    
    public int getUid()
    {
        return uid;
    }
    
    public Timestamp getTstamp()
    {
        return tstamp;
    }
    
    public String getTitle_en()
    {
        return title_en;
    }
    
    public String getTitle_de()
    {
        return title_de;
    }
    
    public String getText_en()
    {
        return text_en;
    }
    
    public String getText_de()
    {
        return text_de;
    }
    
    public boolean isActive()
    {
        return active;
    }
    
    public void setUid(final int uid)
    {
        this.uid = uid;
    }
    
    public void setTstamp(final Timestamp tstamp, final String timeZoneId)
    {
        this.tstamp = tstamp;
        timeZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);
    }
    
    public void setTitle_en(final String title_en)
    {
        this.title_en = title_en;
    }
    
    public void setTitle_de(final String title_de)
    {
        this.title_de = title_de;
    }
    
    public void setText_en(final String text_en)
    {
        this.text_en = text_en;
    }
    
    public void setText_de(final String text_de)
    {
        this.text_de = text_de;
    }
    
    public void setActive(final boolean active)
    {
        this.active = active;
    }
    
}
