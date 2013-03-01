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

public class Signal
{
    
    private long uid;
    private long test_id;
    private Timestamp time;
    private int network_type_id;
    private int signal_strength;
    private int gsm_bit_error_rate;
    private int wifi_link_speed;
    private int wifi_rssi;
    
    private Calendar timeZone = null;
    
    private Connection conn = null;
    private String errorLabel = "";
    private boolean error = false;
    
    public Signal(final Connection conn)
    {
        reset();
        this.conn = conn;
    }
    
    public Signal(final Connection conn, final long uid, final long test_id, final Timestamp time,
            final int network_type_id, final int signal_strength, final int gsm_bit_error_rate,
            final int wifi_link_speed, final int wifi_rssi, final String timeZoneId)
    {
        
        reset();
        
        this.conn = conn;
        
        this.uid = uid;
        this.test_id = test_id;
        this.time = time;
        this.network_type_id = network_type_id;
        this.signal_strength = signal_strength;
        this.gsm_bit_error_rate = gsm_bit_error_rate;
        this.wifi_link_speed = wifi_link_speed;
        this.wifi_rssi = wifi_rssi;
        
        timeZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);
    }
    
    public void reset()
    {
        
        uid = 0;
        test_id = 0;
        time = null;
        network_type_id = 0;
        signal_strength = 0;
        gsm_bit_error_rate = 0;
        wifi_link_speed = 0;
        wifi_rssi = 0;
        
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
    
    public void storeSignal()
    {
        PreparedStatement st;
        try
        {
            st = conn.prepareStatement(
                    "INSERT INTO signal(test_id, time, network_type_id, signal_strength, gsm_bit_error_rate, wifi_link_speed, wifi_rssi) "
                            + "VALUES( ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            
            st.setLong(1, test_id);
            st.setTimestamp(2, time, timeZone);
            st.setInt(3, network_type_id);
            st.setInt(4, signal_strength);
            st.setInt(5, gsm_bit_error_rate);
            st.setInt(6, wifi_link_speed);
            st.setInt(7, wifi_rssi);
            
            // System.out.println(st2.toString());
            
            final int affectedRows2 = st.executeUpdate();
            if (affectedRows2 == 0)
                setError("ERROR_DB_STORE_SIGNAL");
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
            setError("ERROR_DB_STORE_SIGNAL_SQL");
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
    
    public Timestamp getTime()
    {
        return time;
    }
    
    public int getNetwork_type_id()
    {
        return network_type_id;
    }
    
    public int getSignal_strength()
    {
        return signal_strength;
    }
    
    public int getGsm_bit_error_rate()
    {
        return gsm_bit_error_rate;
    }
    
    public int getWifi_link_speed()
    {
        return wifi_link_speed;
    }
    
    public int getWifi_rssi()
    {
        return wifi_rssi;
    }
    
    public void setUid(final long uid)
    {
        this.uid = uid;
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
    
    public void setNetwork_type_id(final int network_type_id)
    {
        this.network_type_id = network_type_id;
    }
    
    public void setSignal_strength(final int signal_strength)
    {
        this.signal_strength = signal_strength;
    }
    
    public void setGsm_bit_error_rate(final int gsm_bit_error_rate)
    {
        this.gsm_bit_error_rate = gsm_bit_error_rate;
    }
    
    public void setWifi_link_speed(final int wifi_link_speed)
    {
        this.wifi_link_speed = wifi_link_speed;
    }
    
    public void setWifi_rssi(final int wifi_rssi)
    {
        this.wifi_rssi = wifi_rssi;
    }
    
}
