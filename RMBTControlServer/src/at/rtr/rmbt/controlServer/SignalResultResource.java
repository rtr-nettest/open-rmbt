package at.rtr.rmbt.controlServer;

import at.rtr.rmbt.db.*;
import at.rtr.rmbt.db.fields.TimestampField;
import at.rtr.rmbt.shared.Helperfunctions;
import at.rtr.rmbt.util.BandCalculationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Post;

import java.net.InetAddress;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SignalResultResource extends ServerResource {
    public static final String STATUS_SIGNAL = "SIGNAL";

    @Post("json")
    public String request(final String entity) {
        //accept, return stub
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        JSONObject request = null;
        Boolean oldAutoCommitState = null;
        ObjectMapper om = new ObjectMapper();

        if (entity != null && !entity.isEmpty() && conn != null) {
            // try parse the string to a JSON object
            try {
                request = new JSONObject(entity);
                System.out.println(request.toString(1));
                UUID testUuid;

                int sequenceNumber = -1;


                if (request.has("test_uuid") && !Strings.isNullOrEmpty(request.optString("test_uuid"))) {
                    testUuid = UUID.fromString(request.getString("test_uuid"));

                    answer.put("test_uuid", testUuid.toString());
                    if (request.has("sequence_number") && !request.isNull("sequence_number") &&
                            request.getInt("sequence_number") > 0) {
                        sequenceNumber = request.getInt("sequence_number");
                    } else {
                        sequenceNumber = 0;
                    }
                } else {
                    testUuid = UUID.randomUUID();
                    if (request.has("sequence_number") && !request.isNull("sequence_number") &&
                            request.getInt("sequence_number") == 0) {
                        answer.put("test_uuid", testUuid.toString());
                        sequenceNumber = 0;
                    } else {
                        //null or not set or not 0 --> invalid
                        errorList.addError("ERROR_INVALID_SEQUENCE");
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    }
                }

                //code duplication from RegistrationResource
                String timeZoneId = request.getString("timezone");
                // String tmpTimeZoneId = timeZoneId;

                //lookup if timezone is valid
                if (!Arrays.asList(TimeZone.getAvailableIDs()).contains(timeZoneId)) {
                    errorList.addError("ERROR_TIMEZONE");
                }

                //when no errors parsing, continue
                if (errorList.isEmpty() && sequenceNumber >= 0) {


                    //get existing open-uuid, if any
                    final PreparedStatement psOpenUuid = conn.prepareStatement("SELECT uuid, open_test_uuid, last_sequence_number FROM test WHERE uuid = ? AND (status is null or status = ?)");
                    psOpenUuid.setObject(1, testUuid);
                    psOpenUuid.setObject(2, STATUS_SIGNAL);
                    ResultSet rsTokenUuid = psOpenUuid.executeQuery();
                    UUID openTestUuid = null;
                    boolean existingInDb = false;

                    if (rsTokenUuid.next()) {
                        openTestUuid = (java.util.UUID) rsTokenUuid.getObject("open_test_uuid");
                        int lastSequenceNumber = rsTokenUuid.getInt("last_sequence_number");
                        if (sequenceNumber <= lastSequenceNumber) {
                            errorList.addError("ERROR_INVALID_SEQUENCE");
                        }
                        existingInDb = true;
                    } else {
                        openTestUuid = UUID.randomUUID();
                    }

                    long clientUid = 0;
                    /*
                     * if (uuid == null) {
                     * clientDb.setTimeZone(timeWithZone);
                     * clientDb.setTime(tstamp);
                     * clientDb.setClient_type_id(typeId); uuid =
                     * clientDb.storeClient(); if (clientDb.hasError()) {
                     * errorList.addError(clientDb.getError()); } else {
                     * answer.put("uuid", uuid.toString()); } }
                     */

                    final Client clientDb = new Client(conn);
                    clientUid = clientDb.getClientByUuid(UUID.fromString(request.getString("client_uuid")));

                    if (testUuid != null && clientUid > 0 && errorList.isEmpty()) {

                        Calendar timeWithZone = null;

                        if (timeZoneId.isEmpty())
                        {
                            timeZoneId = Helperfunctions.getTimezoneId();
                            timeWithZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);
                        }
                        else
                            timeWithZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);


                        //Insert basic info if registration was not received by server
                        if (sequenceNumber == 0 && !existingInDb){
                            System.out.println("not existing in db");
                            //adjust time using time_ns
                            GregorianCalendar calendar = new GregorianCalendar();
                            //System.out.println(calendar.getTime().toGMTString());
                            try{
                                long timeNs = request.getLong("time_ns");
                                calendar.add(Calendar.MILLISECOND, (int) Math.round(timeNs/1e6));
                            }
                            catch (NumberFormatException e) {}


                            //System.out.println(calendar.getTime().toGMTString());
                            PreparedStatement st;
                            st = conn
                                    .prepareStatement(
                                            "INSERT INTO test(time, uuid, open_test_uuid, client_id, status)"
                                                    + "VALUES(?, ?, ?, ?, ?)",
                                            Statement.RETURN_GENERATED_KEYS);
                            int i = 1;
                            //time
                            st.setDate(i++,new java.sql.Date(calendar.getTime().getTime()));
                            // uuid
                            st.setObject(i++, testUuid);
                            // open_test_uuid
                            st.setObject(i++, openTestUuid);
                            // client_id
                            st.setLong(i++, clientUid);
                            //status
                            st.setString(i++, STATUS_SIGNAL);
                            final int affectedRows = st.executeUpdate();
                            if (affectedRows == 0) {
                                errorList.addError("ERROR_DB_STORE_TEST");
                            }
                            System.out.println("saved new uuid " + testUuid + " into database");
                        }
                        else {
                            System.out.println("did not save to db, as " + sequenceNumber + " / " + existingInDb);
                        }

                        final Test test = new Test(conn);
                        long testLoaded = test.getTestByUuid(testUuid);
                        if (testLoaded == 0) {
                            errorList.addError("ERROR_DB_STORE_TEST");
                        }

                        oldAutoCommitState = conn.getAutoCommit();
                        conn.setAutoCommit(false);

                        System.out.println("loaded test:" + testLoaded);

                        test.getField("timezone").setString(timeZoneId);
                        //TODO: client_time
                        test.setFields(request);
                        System.out.println(test.toString());

                        //code duplication from ResultResource
                        final String ipLocalRaw = request.optString("test_ip_local", null);
                        if (ipLocalRaw != null) {
                            final InetAddress ipLocalAddress = InetAddresses.forString(ipLocalRaw);
                            // original address (not filtered)
                            test.getField("client_ip_local").setString(
                                    InetAddresses.toAddrString(ipLocalAddress));
                            // anonymized local address
                            final String ipLocalAnonymized = Helperfunctions.anonymizeIp(ipLocalAddress);
                            test.getField("client_ip_local_anonymized").setString(ipLocalAnonymized);
                            // type of local ip
                            test.getField("client_ip_local_type").setString(
                                    Helperfunctions.IpType(ipLocalAddress));
                            // public ip
                            final InetAddress ipPublicAddress = InetAddresses.forString(test.getField("client_public_ip").toString());
                            test.getField("nat_type")
                                    .setString(Helperfunctions.getNatType(ipLocalAddress, ipPublicAddress));
                        }

                        //avoid null value on user_server_selection
                        if (test.getField("user_server_selection").toString() != "true") {
                            test.getField("user_server_selection").setString("false");
                        }

                        final JSONArray geoData = request.optJSONArray("geoLocations");
                        // geo_location_uuid to be stored in test table ("reference location")
                        UUID geoRefUuid = null;
                        //System.out.println("geoData " + (geoData == null || geoData.toList().isEmpty()));

                        if (geoData != null && !test.hasError()) {
                            float minAccuracy = Float.MAX_VALUE;
                            final AtomicReference<JSONObject> firstAccuratePosition = new AtomicReference<>();

                            for (int i = 0; i < geoData.length(); i++) {

                                final JSONObject geoDataItem = geoData.getJSONObject(i);

                                if (geoDataItem.optLong("tstamp", 0) != 0 && geoDataItem.optDouble("geo_lat", 0) != 0 && geoDataItem.optDouble("geo_long", 0) != 0) {

                                    final GeoLocation geoloc = new GeoLocation(conn);

                                    geoloc.setOpenTestUuid(openTestUuid);
                                    geoloc.setTest_id(test.getUid());

                                    om.readerForUpdating(geoloc).readValue(geoDataItem.toString());

                                    final long clientTime = geoDataItem.optLong("tstamp");
                                    final Timestamp tstamp = java.sql.Timestamp.valueOf(new Timestamp(
                                            clientTime).toString());

                                    geoloc.setTime(tstamp, test.getField("timezone").toString());
                                    geoloc.setAccuracy((float) geoDataItem.optDouble("accuracy", Float.MAX_VALUE));

                                    final long timeNs = geoDataItem.optLong("time_ns");

                                    // ignore all timestamps older than 20s
                                    if (timeNs > -20000000000L) {

                                        geoloc.storeLocation();

                                        // Find reference location
                                        if (geoloc.getAccuracy() != null && geoloc.getAccuracy() < minAccuracy) {
                                            minAccuracy = geoloc.getAccuracy();
                                            // store geo_location_uuid
                                            geoRefUuid = geoloc.getGeoLocationUuid();
                                            firstAccuratePosition.set(geoDataItem);
                                        }
                                        // Fallback: store last geolocation as reference location
                                        else if (firstAccuratePosition.get() == null &&
                                                i == geoData.length() - 1) {
                                            geoRefUuid = geoloc.getGeoLocationUuid();
                                            firstAccuratePosition.set(geoDataItem);
                                        }

                                        if (geoloc.hasError()) {
                                            errorList.addError(geoloc.getError());
                                            break;
                                        }
                                    }
                                }
                            }

                            // Store reference location in test table
                            if (firstAccuratePosition.get() != null) {
                                // set geo_location_uuid
                                if (geoRefUuid != null) {
                                    test.getField("geo_location_uuid").setString(geoRefUuid.toString());
                                }

                                JSONObject geoDataItem = firstAccuratePosition.get();
                                if (geoDataItem.has("geo_lat"))
                                    test.getField("geo_lat").setField(geoDataItem);

                                if (geoDataItem.has("geo_long"))
                                    test.getField("geo_long").setField(geoDataItem);

                                if (geoDataItem.has("accuracy"))
                                    test.getField("geo_accuracy").setField(geoDataItem);

                                if (geoDataItem.has("provider"))
                                    test.getField("geo_provider").setField(geoDataItem);
                            }
                        }

                        int minSignalStrength = Integer.MAX_VALUE; //measured as RSSI (GSM,UMTS,Wifi)
                        int minLteRsrp = Integer.MAX_VALUE; //signal strength measured as RSRP
                        int minLteRsrq = Integer.MAX_VALUE; //signal quality of LTE measured as RSRQ
                        int minLinkSpeed = Integer.MIN_VALUE;
                        boolean radioBandChanged = false;
                        Integer radioBand = null;
                        boolean channelChanged = false;
                        Integer channelNumber = null;
                        boolean locationIdChanged = false; //location id changed somewhere in the test
                        Integer locationId = null;
                        boolean areaCodeChanged = false;
                        Integer areaCode = null;

                        if (request.has("radioInfo")) {
                            //new radio info code
                            om = new ObjectMapper();
                            QueryRunner qr = new QueryRunner();
                            om.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                            List<RadioCell> radioCells = Arrays.asList(om.readValue(request.getJSONObject("radioInfo").getJSONArray("cells").toString(), RadioCell[].class));
                            List<RadioSignal> radioSignals = Arrays.asList(om.readValue(request.getJSONObject("radioInfo").getJSONArray("signals").toString(), RadioSignal[].class));
                            Map<UUID, RadioCell> radioCellsByUuid = new HashMap<>();
                            //System.out.println(request.getJSONObject("radioInfo").toString(4));

                            //set open test uuid, write to db
                            for (RadioCell cell : radioCells) {
                                radioCellsByUuid.put(cell.getUuid(), cell);
                                //System.out.println(cell);
                                cell.setOpenTestUuid(openTestUuid);
                                String sql = "INSERT INTO radio_cell(uuid, open_test_uuid, mnc, mcc, location_id, area_code, primary_scrambling_code, technology, channel_number, registered, active)" +
                                        "        VALUES(?,?,?,?,?,?,?,?,?,?,?);";

                                //this will return some id
                                MapHandler results = new MapHandler();
                                Map<String, Object> insert = qr.insert(conn, sql, results,
                                        cell.getUuid(),
                                        cell.getOpenTestUuid(),
                                        cell.getMnc(),
                                        cell.getMcc(),
                                        cell.getLocationId(),
                                        cell.getAreaCode(),
                                        cell.getPrimaryScramblingCode(),
                                        cell.getTechnology().toString(),
                                        cell.getChannelNumber(),
                                        cell.isRegistered(),
                                        cell.isActive());

                                if (channelNumber == null && Objects.equals(cell.isActive(), true)) {
                                    channelNumber = cell.getChannelNumber();
                                } else if (channelNumber != null && Objects.equals(cell.isActive(), true) &&
                                        !channelNumber.equals(cell.getChannelNumber())) {
                                    channelChanged = true;
                                }

                                if (Objects.equals(cell.isActive(), true) &&
                                        cell.getTechnology() != RadioCell.Technology.CONNECTION_WLAN) {
                                    if (locationId == null && !locationIdChanged) {
                                        locationId = cell.getLocationId();
                                    } else {
                                        if (!locationIdChanged &&
                                                !locationId.equals(cell.getLocationId())) {
                                            locationIdChanged = true;
                                            locationId = null;
                                        }
                                    }

                                    if (areaCode == null && !areaCodeChanged) {
                                        areaCode = cell.getAreaCode();
                                    } else if (areaCode != null) {
                                        if (!areaCode.equals(cell.getAreaCode())) {
                                            areaCodeChanged = true;
                                            areaCode = null;
                                        }
                                    }

                                    if (cell.getChannelNumber() != null &&
                                            !radioBandChanged) {

                                        BandCalculationUtil.FrequencyInformation fi = null;
                                        switch (cell.getTechnology()) {
                                            case CONNECTION_2G:
                                                fi = BandCalculationUtil.getBandFromArfcn(cell.getChannelNumber());
                                                break;
                                            case CONNECTION_3G:
                                                fi = BandCalculationUtil.getBandFromUarfcn(cell.getChannelNumber());
                                                break;
                                            case CONNECTION_4G:
                                                fi = BandCalculationUtil.getBandFromEarfcn(cell.getChannelNumber());
                                                break;
                                            case CONNECTION_WLAN:
                                                break;
                                        }

                                        if (fi != null) {
                                            if (radioBand == null || radioBand.equals(fi.getBand())) {
                                                radioBand = fi.getBand();
                                            } else {
                                                radioBand = null;
                                                radioBandChanged = true;
                                            }
                                        }
                                    }
                                }
                            }

                            for (RadioSignal signal : radioSignals) {
                                signal.setOpenTestUuid(openTestUuid);

                                //set signal times as seen from server side
                                TimestampField time = (TimestampField) test.getField("time");
                                GregorianCalendar calendar = new GregorianCalendar();
                                calendar.setTime(time.getDate());
                                calendar.add(Calendar.MILLISECOND, (int) (signal.getTimeNs() / 1e6));
                                signal.setTime(calendar.getTime());

                                String sql = "INSERT INTO radio_signal(cell_uuid, open_test_uuid, network_type_id, bit_error_rate, wifi_link_speed, " +
                                        " lte_cqi, lte_rssnr, lte_rsrp, lte_rsrq, signal_strength, timing_advance, time, time_ns, time_ns_last) " +
                                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
                                MapHandler results = new MapHandler();

                                qr.insert(conn, sql, results,
                                        signal.getCellUuid(),
                                        signal.getOpenTestUuid(),
                                        signal.getNetworkTypeId(),
                                        signal.getBitErrorRate(),
                                        signal.getWifiLinkSpeed(),
                                        signal.getLteCqi(),
                                        signal.getLteRssnr(),
                                        signal.getLteRsrp(),
                                        signal.getLteRsrq(),
                                        signal.getSignal(),
                                        signal.getTimingAdvance(),
                                        new Timestamp(signal.getTime().getTime()),
                                        signal.getTimeNs(),
                                        signal.getTimeNsLast());


                                //use signal information, if this was a signal belonging
                                //to a cell that was active during this test
                                if (Objects.equals(radioCellsByUuid.get(signal.getCellUuid()).isActive(), true)) {
                                    //System.out.println("active: " + signal);
                                    if (signal.getSignal() != null && signal.getSignal() < minSignalStrength) {
                                        minSignalStrength = signal.getSignal();
                                    }
                                    if (signal.getLteRsrp() != null && signal.getLteRsrp() < minLteRsrp) {
                                        minLteRsrp = signal.getLteRsrp();
                                    }
                                    if (signal.getLteRsrq() != null && signal.getLteRsrq() < minLteRsrq) {
                                        minLteRsrq = signal.getLteRsrq();
                                    }
                                    if (signal.getWifiLinkSpeed() != null && (signal.getWifiLinkSpeed() < minLinkSpeed || minLinkSpeed == Integer.MIN_VALUE)) {
                                        minLinkSpeed = signal.getWifiLinkSpeed();
                                    }
                                } else {
                                    //System.out.println("not active: " + signal);
                                }

                            }
                        }

                        //store, but only for sequence number 0
                        if (sequenceNumber == 0) {
                            System.out.println("sequence 0, storing test");
                            test.storeTestResults(true);

                            if (test.hasError()) {
                                errorList.addError(test.getError());
                            }
                        } else {
                            //System.out.println("sequence >0, only storing geo / signal, updating sequence");
                            PreparedStatement st;
                            try {
                                st = conn
                                        .prepareStatement(
                                                "UPDATE test SET last_sequence_number = ? where uuid = ?;");
                                int i = 1;
                                //sequence
                                st.setInt(i++, sequenceNumber);
                                // uuid
                                st.setObject(i++, testUuid);
                                final int affectedRows = st.executeUpdate();
                                if (affectedRows == 0) {
                                    errorList.addError("ERROR_DB_STORE_TEST");
                                }
                            } catch (SQLException throwables) {
                                errorList.addError("ERROR_DB_STORE_TEST");
                                throwables.printStackTrace();
                            }
                        }

                        //commit
                        conn.commit();

                    }
                }
            } catch(final JSONException e) {
                e.printStackTrace();
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
            catch (final IllegalArgumentException e) {
                errorList.addError("ERROR_REQUEST_JSON");
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            } catch (final SQLException | NullPointerException e) {
                System.out.println("Error while storing data " + e.toString() + " (" + entity + ")");
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } finally {
                // be nice and restore old state
                if (oldAutoCommitState != null) {
                    try {
                        conn.setAutoCommit(oldAutoCommitState);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        } else {
            errorList.addErrorString("Expected request is missing.");
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }

        answer.putOpt("error", errorList.getList());

        return answer.toString();
    }
}
