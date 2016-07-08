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
import java.util.UUID;

import at.alladin.rmbt.db.fields.BooleanField;
import at.alladin.rmbt.db.fields.DoubleField;
import at.alladin.rmbt.db.fields.Field;
import at.alladin.rmbt.db.fields.IntField;
import at.alladin.rmbt.db.fields.JsonField;
import at.alladin.rmbt.db.fields.LongField;
import at.alladin.rmbt.db.fields.StringField;
import at.alladin.rmbt.db.fields.TimestampField;
import at.alladin.rmbt.db.fields.UUIDField;

public class Test extends Table
{
    
    // Interface for Table Test
    
    private final static String SELECT = "SELECT" +
            " t.*," +
            " pMob.shortname mobile_provider_name," +
            " pSim.shortname network_sim_operator_mcc_mnc_text," +
            " pPro.shortname provider_id_name," +
            " k.gemeinde community," +
            " k.bezirk district," +
            " k.land province," +
            " k.anhang cov800cat," +
            " COALESCE(adm.fullname, t.model) model_fullname," +
            " pServ.name server_name" +
            " FROM test t" +
            " LEFT JOIN provider pSim" +
            " ON t.network_sim_operator=pSim.mcc_mnc" +
            " LEFT JOIN provider pPro" +
            " ON t.provider_id=pPro.uid" +
            " LEFT JOIN provider pMob" +
            " ON t.mobile_provider_id=pMob.uid" +
            " LEFT JOIN device_map adm ON adm.codename=t.model" +
            " LEFT JOIN test_server pServ ON t.server_id=pServ.uid" +
            " LEFT JOIN kategorisierte_gemeinden k ON t.gkz=k.gemeinde_i";
    
    private final static ThreadLocal<Field[]> PER_THREAD_FIELDS = new ThreadLocal<Field[]>() {
        protected Field[] initialValue() {
            return new Field[] {
            //   <sql-column> <json-field-name> [<not in test-table]
            // 
            // when receiving results from client:
            //<a> null => field <a> is expected in results from client, stored as <a> in database
            //<a> <b>  => field <b> is expected in results from client, stored as <a> in database
            //<a> <b> true => nothing is expected/stored
            //the fields are selected by the list, there is no additional mechanism to select fields
            // when sending results to client:
            //<a> is read from database, sent as <a> to client (<b> is ignored)
            //the fields are selected in TestResult(Detail)Resource, this is just a list of available fields	
            new UUIDField("uuid", null),
            new LongField("client_id", null),
            new StringField("client_version", "client_version"),
            new StringField("client_name", "client_name"),
            new StringField("client_language", "client_language"),
            new StringField("client_ip_local",null),
            new StringField("client_ip_local_anonymized",null),
            new StringField("client_ip_local_type","client_local_ip"),
            new StringField("token", null),
            new IntField("server_id", null),
            new IntField("port", null), // "test_port_remote"
            new BooleanField("use_ssl", null),
            new TimestampField("time", null),
            new TimestampField("client_time", null),
            new IntField("speed_upload", "test_speed_upload"), // note the '_test' prefix!
            new IntField("speed_download", "test_speed_download"), // note the '_test' prefix!
            new LongField("ping_shortest", "test_ping_shortest"), // note the '_test' prefix!
            new LongField("ping_median", null),
            new StringField("encryption", "test_encryption"), // note the '_test' prefix!
            new StringField("client_public_ip", null),
            new StringField("client_public_ip_anonymized", null),
            new StringField("plattform", "plattform"),
            new StringField("server_name", null, true),            
            new StringField("os_version", "os_version"),
            new StringField("api_level", "api_level"),
            new StringField("device", "device"),
            new StringField("model", "model"),
            new StringField("model_fullname", null, true),
            new StringField("product", "product"),
            new IntField("phone_type", "telephony_phone_type"),
            new IntField("data_state", "telephony_data_state"),
            new StringField("network_country", "telephony_network_country"),
            new StringField("network_operator", null),
            new StringField("mobile_provider_name", null, true),
            new StringField("network_operator_name", "telephony_network_operator_name"),
            new StringField("network_sim_country", "telephony_network_sim_country"),
            new StringField("network_sim_operator", null),
            new StringField("network_sim_operator_mcc_mnc_text", null, true),
            new StringField("network_sim_operator_name", "telephony_network_sim_operator_name"),
            new IntField("roaming_type", null),
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
            new StringField("source_ip", null),
            new StringField("source_ip_anonymized",null),
            new StringField("client_software_version", "client_software_version"),
            new DoubleField("geo_lat", "geo_lat"), 
            new DoubleField("geo_long", "geo_long"),
            new IntField("network_type", "network_type"), 
            new IntField("signal_strength", null), // signal strength as RSSI value
            new IntField("lte_rsrp", null),        // signal strength as RSRP value
            new IntField("lte_rsrq", null),        // signal quality as RSRQ value
            new StringField("software_revision", null), 
            new LongField("client_test_counter", null),
            new StringField("nat_type", null), 
            new StringField("client_previous_test_status", null),
            new LongField("public_ip_asn", null), 
            new StringField("public_ip_rdns", null),
            new StringField("public_ip_as_name", null),
            new StringField("country_geoip", null),
            new StringField("country_location", null),
            new StringField("country_asn", null),
            new LongField("total_bytes_download", "test_total_bytes_download"),
            new LongField("total_bytes_upload", "test_total_bytes_upload"), 
            new IntField("wifi_link_speed", null),
            new BooleanField("network_is_roaming", "telephony_network_is_roaming"),
            new IntField("zip_code", "zip_code"),
            new IntField("zip_code_geo", null), 
            new IntField("gkz",null,true),
            new StringField("community",null,true),
            new StringField("district",null,true),
            new StringField("province",null,true),
            new StringField("cov800cat",null,true),
            new StringField("provider_id_name", null, true),
            new StringField("geo_provider", "provider"),
            new DoubleField("geo_accuracy", "accuracy"),
            new UUIDField("open_uuid", null),
            new UUIDField("open_test_uuid",null),
            new LongField("test_if_bytes_download", "test_if_bytes_download"),
            new LongField("test_if_bytes_upload", "test_if_bytes_upload"),
            new LongField("testdl_if_bytes_download", "testdl_if_bytes_download"),
            new LongField("testdl_if_bytes_upload", "testdl_if_bytes_upload"),
            new LongField("testul_if_bytes_download", "testul_if_bytes_download"),
            new LongField("testul_if_bytes_upload", "testul_if_bytes_upload"),
            new LongField("time_dl_ns", "time_dl_ns"),
            new LongField("time_ul_ns", "time_ul_ns"), 
            new IntField("num_threads_ul", "num_threads_ul"),
            new StringField("tag", "tag"),
            new StringField("hidden_code", "hidden_code"),
            new BooleanField("user_server_selection", "user_server_selection"),
            new JsonField("speed_items", null),
            new BooleanField("dual_sim", "dual_sim"),
            new JsonField("android_permissions", null),
            new StringField("dual_sim_detection_method", "dual_sim_detection_method"),
            };
        };
    };
    
    public Test(final Connection conn)
    {
        super(PER_THREAD_FIELDS.get(), conn);
    }
    
    public void storeTestResults(boolean update)
    {
        
        try
        {
            
            final StringBuilder sqlBuilder = new StringBuilder();
            for (final Field field : fields)
                if (!field.isReadOnly())
                    field.appendDbKeyValue(sqlBuilder);
            
            final String updateString;

            if (update) // allow to update previous test results
            	updateString = ""; // update allowed
            else
            	updateString =" AND status = 'STARTED' "; //results are only stored when status was "STARTED"
            
            PreparedStatement st;
            // allow updates only when previous status was 'started' and max 2min after test was started 
            st = conn.prepareStatement("UPDATE test " + "SET " + sqlBuilder
                    + ", location = ST_TRANSFORM(ST_SetSRID(ST_Point(?, ?), 4326), 900913) WHERE uid = ? " + 
            		updateString + " AND (now() - time  < interval '2' minute)");
            
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
            final PreparedStatement st = conn.prepareStatement(SELECT + " WHERE t.deleted = false AND t.implausible = false AND t.uuid = ?");
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
    
    public long getTestByOpenTestUuid(final UUID openTestUuid) {
        resetError();
        try
        {
            final PreparedStatement st = conn.prepareStatement(SELECT + " WHERE t.deleted = false AND t.implausible = false AND t.open_test_uuid = ?");
            st.setObject(1, openTestUuid);
            
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
            
            final PreparedStatement st = conn.prepareStatement(SELECT + " WHERE t.deleted = false AND  t.implausible = false AND t.uid = ?");
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
