package at.rtr.rmbt.controlServer;

import com.google.common.net.InetAddresses;
import org.json.JSONObject;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.Post;

import java.net.InetAddress;
import java.util.UUID;

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

        if (entity != null && !entity.isEmpty()) {
            //TODO: Save in DB

            //IP
            answer.put("client_remote_ip", clientIpString);

            //Provider from asn
                //TODO

            UUID testUuid = UUID.randomUUID();
            answer.put("test_uuid", testUuid.toString());

            final String resultUrl = new Reference(getURL(), settings.getString("RMBT_RESULT_PATH"))
                    .getTargetRef().toString().replace("/result","/signalResult");

            // System.out.println(resultUrl);

            answer.put("result_url", resultUrl);

        } else {
            errorList.addErrorString("Expected request is missing.");
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }

        answer.putOpt("error", errorList.getList());

        return answer.toString();
    }
}
