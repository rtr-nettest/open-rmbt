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
import java.sql.Types;
import java.util.Calendar;
import java.util.UUID;

import at.alladin.rmbt.shared.Helperfunctions;

public class Signal
{
    final static int UNKNOWN = Integer.MIN_VALUE; 
    
    private UUID open_test_uuid;
    private long uid;
    private long test_id;
    private Timestamp time;
    private int network_type_id;
    private int signal_strength;    // RSSI value, used in GSM, UMTS and Wifi (sometimes in LTE)
    private int gsm_bit_error_rate;
    private int wifi_link_speed;
    private int wifi_rssi;
    private int lte_rsrp;           // signal strength value as RSRP, used in LTE
    private int lte_rsrq;           // signal quality RSRQ, used in LTE
    private int lte_rssnr;
    private int lte_cqi;
    private long time_ns;			// relative ts in ns
    
    private Calendar timeZone = null;
    
    private Connection conn = null;
    private String errorLabel = "";
    private boolean error = false;
    
    public Signal(final Connection conn)
    {
        reset();
        this.conn = conn;
    }
    
    public Signal(final Connection conn, final UUID open_test_uuid, final long uid, final long test_id, final Timestamp time,
            final int network_type_id, final int signal_strength, final int gsm_bit_error_rate,
            final int wifi_link_speed, final int wifi_rssi,
            final int lte_rsrp, final int lte_rsrq, final int lte_rssnr, final int lte_cqi,
            final String timeZoneId, final long time_ns)
    {
        
        reset();
        
        this.conn = conn;
        
        this.open_test_uuid = open_test_uuid;
        this.uid = uid;
        this.test_id = test_id;
        this.time = time;
        this.network_type_id = network_type_id;
        this.signal_strength = signal_strength;
        this.gsm_bit_error_rate = gsm_bit_error_rate;
        this.wifi_link_speed = wifi_link_speed;
        this.wifi_rssi = wifi_rssi;
        this.lte_rsrp = lte_rsrp;
        this.lte_rsrq = lte_rsrp;
        this.lte_rssnr = lte_rsrp;
        this.lte_cqi = lte_cqi;
        this.time_ns = time_ns;
        
        timeZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);
    }
    
    public void reset()
    {
        open_test_uuid = null;
        uid = 0;
        test_id = UNKNOWN;
        time = null;
        network_type_id = UNKNOWN;
        signal_strength = UNKNOWN;
        gsm_bit_error_rate = UNKNOWN;
        wifi_link_speed = UNKNOWN;
        wifi_rssi = UNKNOWN;
        lte_rsrp = UNKNOWN;
        lte_rsrq = UNKNOWN;
        lte_rssnr = UNKNOWN;
        lte_cqi = UNKNOWN;
        time_ns = UNKNOWN;
        
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
                    "INSERT INTO signal(" +
                    "open_test_uuid, test_id, time, network_type_id, signal_strength, gsm_bit_error_rate, wifi_link_speed, wifi_rssi, " +
                    "lte_rsrp, lte_rsrq, lte_rssnr, lte_cqi, time_ns) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
          
            int i = 1;
            
            // System.out.println(st2.toString());
            st.setObject(i++,open_test_uuid);
            
            if (test_id == UNKNOWN)
                st.setNull(i++, Types.BIGINT);
            else
                st.setLong(i++, test_id);
            
            if (time == null)
                st.setNull(i++, Types.TIMESTAMP);
            else
                st.setTimestamp(i++, time, timeZone);
            
            if (network_type_id == UNKNOWN)
                st.setNull(i++, Types.INTEGER);
            else
                st.setInt(i++, network_type_id);
            
            if (signal_strength == UNKNOWN)
                st.setNull(i++, Types.INTEGER);
            else
                st.setInt(i++, signal_strength);
            
            if (gsm_bit_error_rate == UNKNOWN)
                st.setNull(i++, Types.INTEGER);
            else
                st.setInt(i++, gsm_bit_error_rate);
            
            if (wifi_link_speed == UNKNOWN)
                st.setNull(i++, Types.INTEGER);
            else
                st.setInt(i++, wifi_link_speed);
            
            if (wifi_rssi == UNKNOWN)
                st.setNull(i++, Types.INTEGER);
            else
                st.setInt(i++, wifi_rssi);
            
            if (lte_rsrp == UNKNOWN)
                st.setNull(i++, Types.INTEGER);
            else
                st.setInt(i++, lte_rsrp);
            
            if (lte_rsrq == UNKNOWN)
                st.setNull(i++, Types.INTEGER);
            else
                st.setInt(i++, lte_rsrq);
            
            if (lte_rssnr == UNKNOWN)
                st.setNull(i++, Types.INTEGER);
            else
                st.setInt(i++, lte_rssnr);
            
            if (lte_cqi == UNKNOWN)
                st.setNull(i++, Types.INTEGER);
            else
                st.setInt(i++, lte_cqi);
            
            if (time_ns == UNKNOWN)
                st.setNull(i++, Types.BIGINT);
            else
                st.setLong(i++, time_ns);            
            
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

    public int getLte_rsrp()
    {
        return lte_rsrp;
    }

    public void setLte_rsrp(int lte_rsrp)
    {
        this.lte_rsrp = lte_rsrp;
    }

    public int getLte_rsrq()
    {
        return lte_rsrq;
    }

    public void setLte_rsrq(int lte_rsrq)
    {
        this.lte_rsrq = lte_rsrq;
    }

    public int getLte_rssnr()
    {
        return lte_rssnr;
    }

    public void setLte_rssnr(int lte_rssnr)
    {
        this.lte_rssnr = lte_rssnr;
    }

    public int getLte_cqi()
    {
        return lte_cqi;
    }

    public void setLte_cqi(int lte_cqi)
    {
        this.lte_cqi = lte_cqi;
    }

	public long getTime_ns() {
		return time_ns;
	}

	public void setTime_ns(long time_ns) {
		this.time_ns = time_ns;
	}
    
}
