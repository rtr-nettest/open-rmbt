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
import java.util.UUID;

import at.alladin.rmbt.db.fields.BooleanField;
import at.alladin.rmbt.db.fields.DoubleField;
import at.alladin.rmbt.db.fields.Field;
import at.alladin.rmbt.db.fields.IntField;
import at.alladin.rmbt.db.fields.LongField;
import at.alladin.rmbt.db.fields.StringField;
import at.alladin.rmbt.db.fields.TimestampField;
import at.alladin.rmbt.db.fields.UUIDField;

public class Test extends Table
{
    
    // Interface for Table Test
    
    private final static String SELECT = "SELECT" + " t.*," + " pOp.shortname network_operator_mnc_mcc_text,"
            + " pSim.shortname network_sim_operator_mnc_mcc_text," + " pPro.shortname provider_id_name"
            + " FROM test t" + " LEFT JOIN provider pOp" + " ON t.network_operator=pOp.mcc_mnc"
            + " LEFT JOIN provider pSim" + " ON t.network_sim_operator=pSim.mcc_mnc" + " LEFT JOIN provider pPro"
            + " ON t.provider_id=pPro.uid";
    
    private static final Field[] fields = new Field[] {
            new UUIDField("uuid", null, true),
            new LongField("client_id", null),
            new StringField("client_version", "client_version"),
            new StringField("client_name", "client_name"),
            new StringField("client_language", "client_language"),
            new StringField("client_local_ip", "client_local_ip"),
            new StringField("token", null),
            new IntField("server_id", null),
            new IntField("port", null), // "test_port_remote"
            new BooleanField("use_ssl", null),
            new TimestampField("time", null),
            new IntField("speed_upload", "test_speed_upload"),
            new IntField("speed_download", "test_speed_download"),
            new LongField("ping_shortest", "test_ping_shortest"),
            new StringField("encryption", "test_encryption"),
            new StringField("client_public_ip", null),
            new StringField("client_public_ip_anonymized", null),
            new StringField("plattform", "plattform"),
            new StringField("os_version", "os_version"),
            new StringField("api_level", "api_level"),
            new StringField("device", "device"),
            new StringField("model", "model"),
            new StringField("product", "product"),
            new IntField("phone_type", "telephony_phone_type"),
            new IntField("data_state", "telephony_data_state"),
            new StringField("network_country", "telephony_network_country"),
            new StringField("network_operator", "telephony_network_operator"),
            new StringField("network_operator_mnc_mcc_text", null, true),
            new StringField("network_operator_name", "telephony_network_operator_name"),
            new StringField("network_sim_country", "telephony_network_sim_country"),
            new StringField("network_sim_operator", "telephony_network_sim_operator"),
            new StringField("network_sim_operator_mnc_mcc_text", null, true),
            new StringField("network_sim_operator_name", "telephony_network_sim_operator_name"),
            new StringField("wifi_ssid", "wifi_ssid"),
            new StringField("wifi_bssid", "wifi_bssid"),
            new IntField("wifi_network_id", "wifi_network_id"),
            new IntField("duration", null),
            new IntField("num_threads", "test_num_threads"),
            new StringField("status", null),
            new StringField("timezone", null),
            new LongField("bytes_download", "test_bytes_download"),
            new LongField("bytes_upload", "test_bytes_upload"),
            new LongField("nsec_download", "test_nsec_download"),
            new LongField("nsec_upload", "test_nsec_upload"), 
            new StringField("server_ip", null),
            new StringField("client_software_version", "client_software_version"),
            new DoubleField("geo_lat", "geo_lat"), 
            new DoubleField("geo_long", "geo_long"),
            new IntField("network_type", "network_type"), 
            new IntField("signal_strength", null),
            new StringField("software_revision", null), 
            new LongField("client_test_counter", null),
            new StringField("nat_type", null), 
            new StringField("client_previous_test_status", null),
            new LongField("public_ip_asn", null), 
            new StringField("public_ip_rdns", null),
            new StringField("public_ip_as_name", null),
            new LongField("total_bytes_download", "test_total_bytes_download"),
            new LongField("total_bytes_upload", "test_total_bytes_upload"), 
            new IntField("wifi_link_speed", null),
            new BooleanField("network_is_roaming", "telephony_network_is_roaming"),
            new IntField("zip_code", "zip_code"), 
            new StringField("provider_id_name", null, true),
            new StringField("geo_provider", "provider"),
            new DoubleField("geo_accuracy", "accuracy"),
            };
    
    public Test(final Connection conn)
    {
        super(fields, conn);
    }
    
    public void updateTest()
    {
        
        try
        {
            
            final StringBuilder sqlBuilder = new StringBuilder();
            for (final Field field : fields)
                if (!field.isReadOnly())
                    field.appendDbKeyValue(sqlBuilder);
            
            PreparedStatement st;
            st = conn.prepareStatement("UPDATE test " + "SET " + sqlBuilder
                    + ", location = ST_TRANSFORM(ST_SetSRID(ST_Point(?, ?), 4326), 900913)" + "WHERE uid = ?",
                    Statement.RETURN_GENERATED_KEYS);
            
            int idx = 1;
            for (final Field field : fields)
                if (!field.isReadOnly())
                    field.getField(st, idx++);
            
            getField("geo_long").getField(st, idx++);
            getField("geo_lat").getField(st, idx++);
            
            // uid to update
            st.setLong(idx++, uid);
            
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
            
            st.close();
        }
        catch (final SQLException e)
        {
            setError("ERROR_DB_STORE_TEST_SQL");
            e.printStackTrace();
            
        }
        
    }
    
    private void loadTest(final PreparedStatement st)
    {
        resetError();
        
        try
        {
            final ResultSet rs = st.executeQuery();
            
            if (rs.next())
                setValuesFromResult(rs);
            else
                setError("ERROR_DB_GET_TEST");
            
            rs.close();
            st.close();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
            setError("ERROR_DB_GET_TEST_SQL");
        }
    }
    
    public long getTestByUuid(final UUID uuid)
    {
        resetError();
        try
        {
            final PreparedStatement st = conn.prepareStatement(SELECT + " WHERE t.deleted = false AND t.uuid = ?");
            st.setObject(1, uuid);
            
            loadTest(st);
            
            return uid;
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
            setError("ERROR_DB_GET_TEST_SQL");
        }
        return -1;
    }
    
    public boolean getTestByUid(final long uid)
    {
        resetError();
        try
        {
            
            final PreparedStatement st = conn.prepareStatement(SELECT + " WHERE t.deleted = false AND t.uid = ?");
            st.setLong(1, uid);
            
            loadTest(st);
            
            return !error;
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
            setError("ERROR_DB_GET_TEST_SQL");
        }
        return false;
    }
}
