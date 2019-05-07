/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
 * Copyright 2017 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
package at.rtr.rmbt.db;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import at.rtr.rmbt.shared.Helperfunctions;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocation
{
    
    private long uid;
    private UUID open_test_uuid;
    private UUID geo_location_uuid;
    private long test_id;
    private Timestamp time;
    private Float accuracy;
    private Double altitude;
    private Float bearing;
    private Float speed;
    private String provider;
    private Double geo_lat;
    private Double geo_long;
    private long time_ns;
    private Boolean mock_location = null;
    
    private Calendar timeZone = null;
    
    private Connection conn = null;
    private String errorLabel = "";
    private boolean error = false;
    
    public GeoLocation(final Connection conn)
    {
        reset();
        this.conn = conn;

    }

    
    public void reset()
    {
        
        uid = 0;
        open_test_uuid = null;
        test_id = 0;
        time = null;
        accuracy = null;
        altitude = null;
        bearing = null;
        speed = null;
        provider = "";
        geo_lat = null;
        geo_long = null;
        time_ns = 0;
        mock_location = null;
        geo_location_uuid = UUID.randomUUID();
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
        if (geo_location_uuid == null)
            geo_location_uuid = UUID.randomUUID();

        try
        {
            QueryRunner qr = new QueryRunner();
            String sql = "INSERT INTO geo_location(geo_location_uuid,open_test_uuid, test_id, time, accuracy, altitude, bearing, speed, provider, geo_lat, geo_long, location, time_ns, mock_location) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?, ST_TRANSFORM(ST_SetSRID(ST_Point(?, ?), 4326), 900913), ?,?)";

            //this will return some id
            MapListHandler results = new MapListHandler();
            List<Map<String, Object>> insert = qr.insert(conn, sql, results,
                    geo_location_uuid,
                    open_test_uuid,
                    test_id,
                    time,
                    accuracy,
                    altitude,
                    bearing,
                    speed,
                    provider,
                    geo_lat,
                    geo_long,
                    geo_long,
                    geo_lat,
                    time_ns,
                    mock_location
            );

            if (insert.size() == 0)
                setError("ERROR_DB_STORE_GEOLOCATION");
            else
            {
                uid = (Long) (insert.get(0).get("uid"));
            }
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
    
    public Float getAccuracy()
    {
        return accuracy;
    }
    
    public Double getAltitude()
    {
        return altitude;
    }
    
    public Float getBearing()
    {
        return bearing;
    }
    
    public Float getSpeed()
    {
        return speed;
    }
    
    public String getProvider()
    {
        return provider;
    }
    
    public Double getGeo_lat()
    {
        return geo_lat;
    }
    
    public Double getGeo_long()
    {
        return geo_long;
    }

    public UUID getGeoLocationUuid()
    {
        return geo_location_uuid;
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
    
    public void setAccuracy(final Float accuracy)
    {
        this.accuracy = accuracy;
    }
    
    public void setAltitude(final Double altitude)
    {
        this.altitude = altitude;
    }
    
    public void setBearing(final Float bearing)
    {
        this.bearing = bearing;
    }
    
    public void setSpeed(final Float speed)
    {
        this.speed = speed;
    }
    
    public void setProvider(final String provider)
    {
        this.provider = provider;
    }
    
    public void setGeo_lat(final Double geo_lat)
    {
        this.geo_lat = geo_lat;
    }
    
    public void setGeo_long(final Double geo_long)
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

    public Boolean getMock_location() {
        return mock_location;
    }

    public void setMock_location(Boolean mock_location) {
        this.mock_location = mock_location;
    }

    public void setGeoLocationUuid(UUID geo_location_uuid) {
        this.geo_location_uuid = geo_location_uuid;
    }
}
