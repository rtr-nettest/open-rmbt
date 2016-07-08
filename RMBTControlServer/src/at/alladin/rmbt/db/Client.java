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

public class Client
{
    
    private long uid;
    private UUID uuid;
    private int client_type_id;
    private String client_type_name;
    private Timestamp time;
    private Calendar time_zone;
    private int sync_group_id;
    private String sync_code;
    
    private Connection conn = null;
    
    private String errorLabel = "";
    
    private boolean error = false;
    private boolean tcAccepted;
    private int tcAcceptedVersion;
    
    public Client(final Connection conn)
    {
        reset();
        this.conn = conn;
    }
    
    public Client(final Connection conn, final long uid, final UUID uuid, final int client_type_id,
            final Timestamp time, final Calendar time_zone, final int sync_group_id, final String sync_code,
            final boolean tcAccepted, final int tcAcceptedVersion)
    {
        reset();
        this.conn = conn;
        
        this.uid = uid;
        this.uuid = uuid;
        this.client_type_id = client_type_id;
        this.time = time;
        this.time_zone = time_zone;
        this.sync_group_id = sync_group_id;
        this.sync_code = sync_code;
        this.tcAccepted = tcAccepted;
        this.tcAcceptedVersion = tcAcceptedVersion;
    }
    
    public void reset()
    {
        uid = 0;
        uuid = null;
        client_type_id = 0;
        client_type_name = "";
        time = null;
        time_zone = null;
        sync_group_id = 0;
        sync_code = "";
        tcAccepted = false;
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
    
    public long getClientByUuid(final UUID uuid)
    {
        resetError();
        
        try
        {
            
            final PreparedStatement st = conn
                    .prepareStatement("SELECT client.*, client_type.name AS client_type_name FROM client , client_type WHERE client.client_type_id = client_type.uid AND client.uuid = CAST ( ? AS uuid)");
            st.setString(1, uuid.toString());
            
            final ResultSet rs = st.executeQuery();
            
            if (rs.next())
            {
                uid = rs.getLong("uid");
                this.uuid = UUID.fromString(rs.getString("uuid"));
                client_type_id = rs.getInt("client_type_id");
                client_type_name = rs.getString("client_type_name");
                time = rs.getTimestamp("time");
                time_zone = Helperfunctions.getTimeWithTimeZone(Helperfunctions.getTimezoneId());
                sync_group_id = rs.getInt("sync_group_id");
                sync_code = rs.getString("sync_code");
                tcAccepted = rs.getBoolean("terms_and_conditions_accepted");
                tcAcceptedVersion = rs.getInt("terms_and_conditions_accepted_version");
            }
            else
            {
                // setError("ERROR_DB_GET_CLIENT");
                // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENT"),
                // new Object[] {uuid}));
            }
            
            rs.close();
            st.close();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
            setError("ERROR_DB_GET_CLIENT_SQL");
            // errorList.addError("ERROR_DB_GET_CLIENT_SQL");
        }
        
        return uid;
    }
    
    public boolean getClientByUid(final long uid)
    {
        resetError();
        
        try
        {
            
            final PreparedStatement st = conn
                    .prepareStatement("SELECT client.*, client_type.name AS client_type_name FROM client , client_type WHERE client.client_type_id = client_type.uid AND client.uid = ?");
            st.setLong(1, uid);
            
            final ResultSet rs = st.executeQuery();
            
            if (rs.next())
            {
                this.uid = rs.getLong("uid");
                uuid = UUID.fromString(rs.getString("uuid"));
                client_type_id = rs.getInt("client_type_id");
                client_type_name = rs.getString("client_type_name");
                time = rs.getTimestamp("time");
                sync_group_id = rs.getInt("sync_group_id");
                sync_code = rs.getString("sync_code");
                tcAccepted = rs.getBoolean("terms_and_conditions_accepted");
                tcAcceptedVersion = rs.getInt("terms_and_conditions_accepted_version");
            }
            else
                setError("ERROR_DB_GET_CLIENT");
            // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENT"),
            // new Object[] {uuid}));
            
            rs.close();
            st.close();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
            setError("ERROR_DB_GET_CLIENT_SQL");
            // errorList.addError("ERROR_DB_GET_CLIENT_SQL");
        }
        
        return !error;
    }
    
    public UUID storeClient(UUID uuid)
    {
        
        resetError();
        
        try
        {
            PreparedStatement st;
            if (uuid == null) 
              uuid = UUID.randomUUID();
            
            st = conn.prepareStatement(
                    "INSERT INTO client(uuid, client_type_id, time, sync_group_id, sync_code, terms_and_conditions_accepted, terms_and_conditions_accepted_version)"
                            + "VALUES( CAST( ? AS UUID), ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            
            st.setString(1, uuid.toString());
            st.setInt(2, client_type_id);
            st.setTimestamp(3, time);
            if (sync_group_id > 0)
                st.setInt(4, sync_group_id);
            else
                st.setObject(4, null);
            if (sync_code.length() > 0)
                st.setString(5, sync_code);
            else
                st.setObject(5, null);
            
            st.setBoolean(6, tcAccepted);
            
            st.setInt(7, tcAcceptedVersion);
            
//            System.out.println(st.toString());
            
            final int affectedRows = st.executeUpdate();
            if (affectedRows == 0)
            {
                uid = 0;
                uuid = null;
                client_type_id = 0;
                client_type_name = "";
                time = null;
                setError("ERROR_DB_STORE_CLIENT");
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
            setError("ERROR_DB_STORE_CLIENT_SQL");
            // errorList.addError(labels.getString("ERROR_DB_STORE_CLIENT_SQL"));
            e.printStackTrace();
        }
        
        return uuid;
    }
    
    public int getTypeId(final String clientType)
    {
        resetError();
        int id = 0;
        
        try
        {
            
            final PreparedStatement st = conn.prepareStatement("SELECT uid FROM client_type WHERE name = ?");
            st.setString(1, clientType.toUpperCase());
            final ResultSet rs = st.executeQuery();
            
            if (rs.next())
                id = rs.getInt(1);
            else
                setError("ERROR_DB_GET_CLIENTTYPE");
            // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENTTYPE"),
            // new Object[] {clientType}));
            rs.close();
            st.close();
        }
        catch (final SQLException e)
        {
            setError("ERROR_DB_GET_CLIENTTYPE_SQL");
            // errorList.addError(labels.getString("ERROR_DB_GET_CLIENTTYPE_SQL"));
            e.printStackTrace();
        }
        
        return id;
    }
    
    public int updateTcAcceptedVersion(final UUID uuid, final int tcAcceptedVersion)
    {
        resetError();
        int id = 0;
        
        try
        {
            
            final PreparedStatement st = conn.prepareStatement("UPDATE client SET terms_and_conditions_accepted_version = ? WHERE uuid = ? ::UUID");
            st.setInt(1, tcAcceptedVersion);
            st.setString(2, uuid.toString());
            st.executeUpdate();

            st.close();
        }
        catch (final SQLException e)
        {
            setError("ERROR_DB_STORE_CLIENT_SQL");
            e.printStackTrace();
        }
        
        return id;
    }
    
    public int updateLastSeen(final UUID uuid)
    {
        resetError();
        int id = 0;
        
        try
        {
            
            final PreparedStatement st = conn.prepareStatement("UPDATE client SET last_seen = NOW() WHERE uuid = ? ::UUID");
            st.setString(1, uuid.toString());
            st.executeUpdate();

            st.close();
        }
        catch (final SQLException e)
        {
            setError("ERROR_DB_STORE_CLIENT_SQL");
            e.printStackTrace();
        }
        
        return id;
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
    
    public UUID getUuid()
    {
        return uuid;
    }
    
    public int getClient_type_id()
    {
        return client_type_id;
    }
    
    public String getClient_type_name()
    {
        return client_type_name;
    }
    
    public Timestamp getTime()
    {
        return time;
    }
    
    public Calendar getTimeZone()
    {
        return time_zone;
    }
    
    public void setUid(final long uid)
    {
        this.uid = uid;
    }
    
    public void setUuid(final UUID uuid)
    {
        this.uuid = uuid;
    }
    
    public int getSync_group_id()
    {
        return sync_group_id;
    }
    
    public String getSync_code()
    {
        return sync_code;
    }
    
    public void setTcAccepted(final boolean tcAccepted)
    {
        this.tcAccepted = tcAccepted;
    }
    
    public void setTcAcceptedVersion(final int tcAcceptedVersion)
    {
        this.tcAcceptedVersion = tcAcceptedVersion;
    }
    
    public boolean isTcAccepted()
    {
        return tcAccepted;
    }
    
    public int getTcAcceptedVersion()
    {
        return tcAcceptedVersion;
    }
    
    public void setClient_type_id(final int client_type_id)
    {
        this.client_type_id = client_type_id;
    }
    
    public void setTime(final Timestamp time)
    {
        this.time = time;
    }
    
    public void setTimeZone(final Calendar time_zone)
    {
        this.time_zone = time_zone;
    }
    
    public void setSync_group_id(final int sync_group_id)
    {
        this.sync_group_id = sync_group_id;
    }
    
    public void setSync_code(final String sync_code)
    {
        this.sync_code = sync_code;
    }
}
