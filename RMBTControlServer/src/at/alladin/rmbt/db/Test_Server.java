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

public class Test_Server
{
    // Interface that all CustomerDAOs must support
    
    private int uid;
    private String name;
    private String web_address;
    private String web_address_ipv4;
    private String web_address_ipv6;
    private int port;
    private int port_ssl;
    
    private String city;
    private String country;
    private double geo_lat;
    private double geo_long;
    
    private Connection conn = null;
    
    private String errorLabel = "";
    
    private boolean error = false;
    
    public Test_Server(final Connection conn)
    {
        reset();
        this.conn = conn;
    }
    
    // public Test_Server(Connection conn, int uid, String name, String
    // web_address, int port_ssl, int port, String city, String country, double
    // geo_lat, double geo_long) {
    //
    // reset();
    // this.conn = conn;
    //
    // this.uid = uid;
    // this.name = name;
    // this.web_address = web_address;
    // this.port_ssl = port_ssl;
    // this.port = port;
    // this.city = city;
    // this.country = country;
    // this.geo_lat = geo_lat;
    // this.geo_long = geo_long;
    //
    // }
    
    public void reset()
    {
        uid = 0;
        name = "";
        web_address = "";
        web_address_ipv4 = "";
        web_address_ipv6 = "";
        port = 0;
        port_ssl = 0;
        city = "";
        country = "";
        geo_lat = 0;
        geo_long = 0;
        
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
    
    private void setValuesFromResult(final ResultSet rs) throws SQLException
    {
        
        uid = rs.getInt("uid");
        
        name = rs.getString("name");
        
        web_address = rs.getString("web_address");
        web_address_ipv4 = rs.getString("web_address_ipv4");
        web_address_ipv6 = rs.getString("web_address_ipv6");
        
        port_ssl = rs.getInt("port_ssl");
        
        port = rs.getInt("port");
        
        city = rs.getString("city");
        
        country = rs.getString("country");
        
        geo_lat = rs.getDouble("geo_lat");
        
        geo_long = rs.getDouble("geo_long");
        
    }
    
    public int getServerByName(final String name)
    {
        resetError();
        
        try
        {
            
            final PreparedStatement st = conn.prepareStatement("SELECT * FROM test_server WHERE name = ?");
            
            st.setString(1, name);
            
            final ResultSet rs = st.executeQuery();
            
            if (rs.next())
                setValuesFromResult(rs);
            else
                setError("ERROR_DB_GET_SERVER");
            
            rs.close();
            st.close();
        }
        catch (final SQLException e)
        {
            setError("ERROR_DB_GET_SERVER_SQL");
            e.printStackTrace();
            
        }
        
        return uid;
        
    }
    
    public boolean getServerByUid(final int uid)
    {
        resetError();
        
        try
        {
            
            final PreparedStatement st = conn.prepareStatement("SELECT * FROM test_server WHERE uid = ?");
            
            st.setInt(1, uid);
            
            final ResultSet rs = st.executeQuery();
            
            if (rs.next())
                setValuesFromResult(rs);
            else
                setError("ERROR_DB_GET_SERVER");
            
            rs.close();
            st.close();
        }
        catch (final SQLException e)
        {
            setError("ERROR_DB_GET_SERVER_SQL");
            e.printStackTrace();
            
        }
        
        return !error;
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
    
    public String getName()
    {
        return name;
    }
    
    public String getWeb_address()
    {
        return web_address;
    }
    
    public String getWeb_address_ipv4()
    {
        if (web_address_ipv4 == null || web_address_ipv4.isEmpty())
            return web_address;
        return web_address_ipv4;
    }
    
    public String getWeb_address_ipv6()
    {
        if (web_address_ipv6 == null || web_address_ipv6.isEmpty())
            return web_address;
        return web_address_ipv6;
    }
    
    public int getPort()
    {
        return port;
    }
    
    public int getPort_ssl()
    {
        return port_ssl;
    }
    
    public String getCity()
    {
        return city;
    }
    
    public String getCountry()
    {
        return country;
    }
    
    public double getGeo_lat()
    {
        return geo_lat;
    }
    
    public double getGeo_long()
    {
        return geo_long;
    }
    
    public Connection getConn()
    {
        return conn;
    }
    
    public void setUid(final int uid)
    {
        this.uid = uid;
    }
    
    public void setName(final String name)
    {
        this.name = name;
    }
    
    public void setWeb_address(final String web_address)
    {
        this.web_address = web_address;
    }
    
    public void setWeb_address_ipv4(final String web_address_ipv4)
    {
        this.web_address_ipv4 = web_address_ipv4;
    }
    
    public void setWeb_address_ipv6(final String web_address_ipv6)
    {
        this.web_address_ipv6 = web_address_ipv6;
    }
    
    public void setPort(final int port)
    {
        this.port = port;
    }
    
    public void setPort_ssl(final int port_ssl)
    {
        this.port_ssl = port_ssl;
    }
    
    public void setCity(final String city)
    {
        this.city = city;
    }
    
    public void setCountry(final String country)
    {
        this.country = country;
    }
    
    public void setGeo_lat(final double geo_lat)
    {
        this.geo_lat = geo_lat;
    }
    
    public void setGeo_long(final double geo_long)
    {
        this.geo_long = geo_long;
    }
    
    public void setConn(final Connection conn)
    {
        this.conn = conn;
    }
    
}
