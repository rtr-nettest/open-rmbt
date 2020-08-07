package at.rtr.rmbt.controlServer;

import at.rtr.rmbt.db.Client;
import at.rtr.rmbt.shared.GeoIPHelper;
import at.rtr.rmbt.shared.Helperfunctions;
import com.google.common.net.InetAddresses;
import org.json.JSONObject;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.Post;

import java.net.InetAddress;
import java.sql.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.logging.Logger;

public class SignalRegistrationResource extends ServerResource {

    @Post("json")
    public String request(final String entity) {
        //accept, return stub
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        JSONObject request = null;

        final String clientIpRaw = getIP();
        final InetAddress clientAddress = InetAddresses.forString(clientIpRaw);
        final String clientIpString = InetAddresses.toAddrString(clientAddress);

        //code duplication from RegistrationResource
        final String geoIpCountry = GeoIPHelper.lookupCountry(clientAddress);
        // public_ip_asn
        Long asn;
        // public_ip_as_name
        // country_asn (2 digit country code of AS, eg. AT or EU)
        String asName;
        String asCountry;

        //try AS resolution service 1
        try {
            final Helperfunctions.ASInformation asInformation = Helperfunctions.getASInformation(clientAddress);
            if (asInformation != null) {
                asn = asInformation.getNumber();
                asName = asInformation.getName();
                asCountry = asInformation.getCountry();
            } else {
                //if this fails, try AS resolution service 2
                Logger.getGlobal().info("AS resolution failed with service A");
                asn = Helperfunctions.getASN(clientAddress);
                if (asn == null) {
                    asName = null;
                    asCountry = null;
                } else {
                    asName = Helperfunctions.getASName(asn);
                    asCountry = Helperfunctions.getAScountry(asn);
                }
            }
        } catch (RuntimeException e) {
            Logger.getGlobal().info("As resolution threw an error");
            e.printStackTrace();
            asn = null;
            asName = null;
            asCountry = null;
        }

        if (entity != null && !entity.isEmpty()) {
            request = new JSONObject(entity);

            //check if client is registered
            UUID uuid = null;
            final String uuidString = request.optString("uuid", "");
            if (!uuidString.isEmpty()) {
                if (uuidString.startsWith("U") && uuidString.length() > 1) {
                    uuid = UUID.fromString(uuidString.substring(1));
                } else {
                    uuid = UUID.fromString(uuidString);
                }
            }
            long clientUid = 0;
            final Client clientDb = new Client(conn);
            if (errorList.getLength() == 0 && uuid != null) {
                clientUid = clientDb.getClientByUuid(uuid);
                if (clientDb.hasError())
                    errorList.addError(clientDb.getError());
            }

            if (clientUid > 0) {
                //generate uuid and open_test_uuid
                UUID testUuid = UUID.randomUUID();
                UUID openTestUuid = UUID.randomUUID();
                answer.put("test_uuid", testUuid.toString());

                //Insert basic info
                String timeZoneId = request.getString("timezone");
                Calendar timeWithZone = null;

                if (timeZoneId.isEmpty()) {
                    timeZoneId = Helperfunctions.getTimezoneId();
                    timeWithZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);
                } else {
                    timeWithZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);
                }
                final long clientTime = request.getLong("time");
                final Timestamp clientTstamp = java.sql.Timestamp.valueOf(new Timestamp(clientTime).toString());

                try {
                    PreparedStatement st = conn
                            .prepareStatement(
                                    "INSERT INTO test(time, uuid, open_test_uuid, client_id, client_public_ip, client_public_ip_anonymized, timezone, client_time, public_ip_asn, public_ip_as_name, country_asn, public_ip_rdns, status, last_sequence_number)"
                                            + "VALUES(NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, -1)",
                                    Statement.RETURN_GENERATED_KEYS);
                    int i = 1;
                    // uuid
                    st.setObject(i++, testUuid);
                    // open_test_uuid
                    st.setObject(i++, openTestUuid);
                    // client_id
                    st.setLong(i++, clientUid);
                    // client_public_ip
                    st.setString(i++, clientIpString);
                    // client_public_ip_anonymized
                    st.setString(i++, Helperfunctions.anonymizeIp(clientAddress));
                    // timezone (of client)
                    st.setString(i++, timeZoneId);
                    // client_time (local time of client)
                    st.setTimestamp(i++, clientTstamp, timeWithZone);
                    // AS name
                    if (asn == null)
                        st.setNull(i++, Types.BIGINT);
                    else
                        st.setLong(i++, asn);
                    if (asName == null)
                        st.setNull(i++, Types.VARCHAR);
                    else
                        st.setString(i++, asName);
                    // AS country
                    if (asCountry == null)
                        st.setNull(i++, Types.VARCHAR);
                    else
                        st.setString(i++, asCountry);
                    //public_ip_rdns
                    String reverseDNS = Helperfunctions.reverseDNSLookup(clientAddress);
                    if (reverseDNS == null || reverseDNS.isEmpty())
                        st.setNull(i++, Types.VARCHAR);
                    else
                    {
                        reverseDNS = reverseDNS.replaceFirst("\\.$", "");
                        st.setString(i++, reverseDNS); // cut off last dot (#332)
                    }
                    //status
                    st.setString(i++, SignalResultResource.STATUS_SIGNAL);
                    final int affectedRows = st.executeUpdate();
                    if (affectedRows == 0) {
                        errorList.addError("ERROR_DB_STORE_TEST");
                    } else {
                        System.out.println("saved new uuid " + testUuid + " into database");

                        long key = 0;
                        final ResultSet rs = st.getGeneratedKeys();
                        if (rs.next())
                            // Retrieve the auto generated
                            // key(s).
                            key = rs.getLong(1);
                        rs.close();

                        //Get provider
                        final PreparedStatement getProviderSt = conn
                                .prepareStatement("SELECT rmbt_set_provider_from_as(?)");
                        getProviderSt.setLong(1, key);
                        String provider = null;
                        if (getProviderSt.execute()) {
                            final ResultSet rs2 = getProviderSt.getResultSet();
                            if (rs2.next())
                                provider = rs2.getString(1);
                        }
                        getProviderSt.close();

                        if (provider != null)
                            answer.put("provider", provider);
                        else
                            answer.put("provider", JSONObject.NULL);
                    }
                } catch (SQLException e) {
                    errorList.addError("ERROR_DB_STORE_GENERAL");
                    e.printStackTrace();
                }


                //IP
                answer.put("client_remote_ip", clientIpString);

                final String resultUrl = new Reference(getURL(), settings.getString("RMBT_RESULT_PATH"))
                        .getTargetRef().toString().replace("/result", "/signalResult");

                answer.put("result_url", resultUrl);
            } else {
                errorList.addError("ERROR_CLIENT_UUID");
            }

        } else {
            errorList.addErrorString("Expected request is missing.");
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }

        answer.putOpt("error", errorList.getList());

        return answer.toString();
    }
}
