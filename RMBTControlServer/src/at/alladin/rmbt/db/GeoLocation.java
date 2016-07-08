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

public class GeoLocation
{
    
    private long uid;
    private UUID open_test_uuid;
    private long test_id;
    private Timestamp time;
    private float accuracy;
    private double altitude;
    private float bearing;
    private float speed;
    private String provider;
    private double geo_lat;
    private double geo_long;
    private long time_ns;
    
    private Calendar timeZone = null;
    
    private Connection conn = null;
    private String errorLabel = "";
    private boolean error = false;
    
    public GeoLocation(final Connection conn)
    {
        reset();
        this.conn = conn;
    }
 
    
    public GeoLocation(final Connection conn,  final long uid, final UUID open_test_uuid, final long test_id, final Timestamp time,
            final long accuracy, final double altitude, final float bearing, final float speed, final String provider,
            final double geo_lat, final double geo_long, final String timeZoneId, final long time_ns)
    {
        reset();
        this.conn = conn;
        
        this.uid = uid;
        this.open_test_uuid = open_test_uuid;
        this.test_id = test_id;
        this.time = time;
        this.accuracy = accuracy;
        this.altitude = altitude;
        this.bearing = bearing;
        this.speed = speed;
        this.provider = provider;
        this.geo_lat = geo_lat;
        this.geo_long = geo_long;
        this.time_ns = time_ns;
        
        timeZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);
    }
    
    
    public void reset()
    {
        
        uid = 0;
        open_test_uuid = null;
        test_id = 0;
        time = null;
        accuracy = 0;
        altitude = 0;
        bearing = 0;
        speed = 0;
        provider = "";
        geo_lat = 0;
        geo_long = 0;
        time_ns = 0;
        
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
                    "INSERT INTO geo_location(open_test_uuid, test_id, time, accuracy, altitude, bearing, speed, provider, geo_lat, geo_long, location, time_ns) "
                            + "VALUES(?,?,?,?,?,?,?,?,?,?, ST_TRANSFORM(ST_SetSRID(ST_Point(?, ?), 4326), 900913), ?)",
                    Statement.RETURN_GENERATED_KEYS);
            
            /*
             * Timestamp geotstamp = java.sql.Timestamp.valueOf(new Timestamp(
             * this.time).toString());
             */
            
            int i=1;
            
            st.setObject(i++, open_test_uuid);
            st.setLong(i++, test_id);
            st.setTimestamp(i++, time, timeZone);
            st.setFloat(i++, accuracy);
            st.setDouble(i++, altitude);
            st.setFloat(i++, bearing);
            st.setFloat(i++, speed);
            st.setString(i++, provider);
            st.setDouble(i++, geo_lat);
            st.setDouble(i++, geo_long);
            st.setDouble(i++, geo_long);
            st.setDouble(i++, geo_lat);
            st.setLong(i++, time_ns);
            
            // System.out.println(st2.toString());
            
            final int affectedRows2 = st.executeUpdate();
            if (affectedRows2 == 0)
                setError("ERROR_DB_STORE_GEOLOCATION");
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
            setError("ERROR_DB_STORE_GEOLOCATION_SQL");
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
    
    public UUID getOpenTestUuid()
    {
        return open_test_uuid;
    }
    
    public long getTest_id()
    {
        return test_id;
    }
    
    public Timestamp getTime()
    {
        return time;
    }
    
    public float getAccuracy()
    {
        return accuracy;
    }
    
    public double getAltitude()
    {
        return altitude;
    }
    
    public float getBearing()
    {
        return bearing;
    }
    
    public float getSpeed()
    {
        return speed;
    }
    
    public String getProvider()
    {
        return provider;
    }
    
    public double getGeo_lat()
    {
        return geo_lat;
    }
    
    public double getGeo_long()
    {
        return geo_long;
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
    
    public void setAccuracy(final float accuracy)
    {
        this.accuracy = accuracy;
    }
    
    public void setAltitude(final double altitude)
    {
        this.altitude = altitude;
    }
    
    public void setBearing(final float bearing)
    {
        this.bearing = bearing;
    }
    
    public void setSpeed(final float speed)
    {
        this.speed = speed;
    }
    
    public void setProvider(final String provider)
    {
        this.provider = provider;
    }
    
    public void setGeo_lat(final double geo_lat)
    {
        this.geo_lat = geo_lat;
    }
    
    public void setGeo_long(final double geo_long)
    {
        this.geo_long = geo_long;
    }
    
    public void setTimeZone(final Calendar timeZone)
    {
        this.timeZone = timeZone;
    }

	public long getTime_ns() {
		return time_ns;
	}

	public void setTime_ns(long time_ns) {
		this.time_ns = time_ns;
	}
    
}
