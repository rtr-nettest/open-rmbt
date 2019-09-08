/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
 * Copyright 2013-2016 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import at.rtr.rmbt.db.fields.BooleanField;
import at.rtr.rmbt.db.fields.DoubleField;
import at.rtr.rmbt.db.fields.Field;
import at.rtr.rmbt.db.fields.IntField;
import at.rtr.rmbt.db.fields.JsonField;
import at.rtr.rmbt.db.fields.LongField;
import at.rtr.rmbt.db.fields.StringField;
import at.rtr.rmbt.db.fields.TimestampField;
import at.rtr.rmbt.db.fields.UUIDField;

public class Test extends Table
{
    
    // Interface for Table Test

    private final static String SELECT = "SELECT" +
            " t.uid, t.uuid, t.client_id, t.client_version, t.client_name, t.client_language, t.client_ip_local," +
            " t.client_ip_local_anonymized, t.client_ip_local_type, t.token, t.server_id, t.port, t.use_ssl, t.time," +
            " t.client_time, t.speed_upload, t.speed_download, t.ping_shortest, t.ping_median, t.client_public_ip," +
            " t.client_public_ip_anonymized, t.plattform, t.os_version, t.api_level, t.device, t.model, t.product," +
            " t.phone_type, t.data_state, t.network_country, t.network_operator, t.network_operator_name," +
            " t.network_sim_country, t.network_sim_operator, t.network_sim_operator_name, t.roaming_type," +
            " t.wifi_ssid, t.wifi_bssid, t.wifi_network_id, t.duration, t.num_threads, t.status, t.timezone," +
            " t.bytes_download, t.bytes_upload, t.nsec_download, t.nsec_upload, t.server_ip, t.source_ip,"+
            " t.source_ip_anonymized, t.client_software_version, t.network_type," +
            " t.signal_strength, t.lte_rsrp, t.lte_rsrq, t.software_revision, t.client_test_counter, t.nat_type," +
            " t.client_previous_test_status, t.public_ip_asn, t.public_ip_rdns, t.public_ip_as_name, t.country_geoip," +
            " t.country_asn, t.total_bytes_download, t.total_bytes_upload, t.wifi_link_speed," +
            " t.network_is_roaming, t.provider_id, t.open_uuid, t.open_test_uuid, t.geo_location_uuid, " +
             "t.test_if_bytes_download, t.test_if_bytes_upload, t.testdl_if_bytes_download,"+
            " t.testdl_if_bytes_upload, t.testul_if_bytes_download, t.testul_if_bytes_upload , t.time_dl_ns," +
            " t.time_ul_ns, t.num_threads_ul  , t.tag, t.hidden_code, t.user_server_selection, t.dual_sim," +
            " t.android_permissions, t.dual_sim_detection_method, t.radio_band, t.cell_location_id," +
            " t.cell_area_code, t.channel_number, t.sim_count," +
            " pMob.shortname mobile_provider_name," +
            " pSim.shortname network_sim_operator_mcc_mnc_text," +
            " pPro.shortname provider_id_name," +
            " tl.geo_lat geo_lat," +
            " tl.country_location," +
            " tl.geo_long geo_long," +
            " tl.geo_provider geo_provider," +
            " tl.geo_accuracy," +
            " tl.land_cover land_cover," +
            " tl.kg_nr_bev kg_nr_bev," +
            " tl.gkz_bev gkz_bev," +
            " tl.gkz_sa gkz_sa," +
            " tl.settlement_type settlement_type," +
            " tl.link_id link_id," +
            " tl.link_name link_name," +
            " tl.link_distance link_distance," +
            " tl.edge_id edge_id," +
            " tl.frc link_frc," +
            " tl.frc link_frc," +
            " tl.dtm_level," +
            " gl.altitude geo_altitude," +
            " gl.speed geo_speed," +
            " ln.name1 link_name1," +
            " ln.name2 link_name2," +
            " k.kg locality," +
            " k.pg community," +
            " k.pb district," +
            " k.bl province," +
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
            " LEFT JOIN test_location tl ON t.open_test_uuid = tl.open_test_uuid" +
            " LEFT JOIN bev_vgd k ON tl.kg_nr_bev = k.kg_nr_int" +
            " LEFT JOIN linknet ln ON tl.link_id = ln.link_id" +
            " LEFT JOIN geo_location gl ON tl.geo_location_uuid = gl.geo_location_uuid";
    
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
            new StringField("client_ip_local",null, 50),
            new StringField("client_ip_local_anonymized",null, 50),
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
            new StringField("source_ip", null, 50),
            new StringField("source_ip_anonymized",null, 50),
            new StringField("client_software_version", "client_software_version", 50),
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
            new IntField("gkz_bev",null,true),
            new IntField("gkz_sa",null,true),
            new IntField("kg_nr_bev",null,true),
            new IntField("land_cover",null,true),
            new StringField("locality",null,true),
            new StringField("community",null,true),
            new StringField("district",null,true),
            new StringField("province",null,true),
            new IntField("settlement_type",null,true),
            new IntField("link_id",null,true),
            new StringField("link_name",null,true),
            new IntField("link_distance",null,true),
            new LongField("edge_id",null,true),
            new IntField("link_frc",null,true),
            new StringField("link_name1",null,true),
            new StringField("link_name2",null,true),
            new IntField("dtm_level",null,true),
            new DoubleField("geo_altitude",null,true),
            new DoubleField("geo_speed",null,true),
            new StringField("provider_id_name", null, true),
            new StringField("geo_provider", "provider"),
            new DoubleField("geo_accuracy", "accuracy"),
            new UUIDField("open_uuid", null),
            new UUIDField("open_test_uuid",null),
            new UUIDField("geo_location_uuid",null),
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
            new BooleanField("dual_sim", "dual_sim"),
            new JsonField("android_permissions", null),
            //new StringField("dual_sim_detection_method", "dual_sim_detection_method"),
            new IntField("radio_band", null),
            new IntField("cell_location_id", null),
            new IntField("cell_area_code", null),
            new IntField("channel_number", null),
            new IntField("sim_count","telephony_sim_count")
            };
        }
    };
    
    public Test(final Connection conn)
    {
        super(PER_THREAD_FIELDS.get(), conn);
    }
    
    public void storeTestResults(boolean update)
    {
        PreparedStatement st = null;
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

            // allow updates only when previous status was 'started' and max 5min after test was started
            st = conn.prepareStatement("UPDATE test " + "SET " + sqlBuilder
                    + ", location = ST_TRANSFORM(ST_SetSRID(ST_Point(?, ?), 4326), 900913) WHERE uid = ? " + 
            		updateString + " AND (now() - time  < interval '5' minute)");
            
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
            if (st != null) {
                Logger.getLogger(Test.class.getName()).log(Level.WARNING,"Failed: " + st.toString());
            }
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
            else {
                setError("ERROR_DB_GET_TEST");
                Logger.getLogger(Test.class.getName()).log(Level.WARNING, "Load test failed: " + rs.toString());
            }
            rs.close();
            st.close();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
            setError("ERROR_DB_GET_TEST_SQL");
        }
    }

    public long getFinishedTestByUuid(final UUID uuid)
    {
        resetError();
        try
        {
            final PreparedStatement st = conn.prepareStatement(SELECT + " WHERE t.deleted = false AND t.implausible = false AND t.uuid = ? AND t.status = 'FINISHED'");
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

    public long getTestByOpenTestUuidAndClientUuid(final UUID openTestUuid, final UUID clientUuid) {
        resetError();
        try
        {
            final PreparedStatement st = conn.prepareStatement(SELECT + " JOIN client c ON c.uid = t.client_id WHERE t.deleted = false AND t.implausible = false AND t.open_test_uuid = ? AND c.uuid = ?");
            st.setObject(1, openTestUuid);
            st.setObject(1, clientUuid);

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
