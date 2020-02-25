package at.rtr.rmbt.controlServer;

import com.google.common.base.Strings;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Post;

import java.util.UUID;
import java.util.regex.Pattern;

public class SignalResultResource extends ServerResource {


    @Post("json")
    public String request(final String entity) {
        //accept, return stub
        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        JSONObject request = null;

        if (entity != null && !entity.isEmpty()) {
            // try parse the string to a JSON object
            try {
                request = new JSONObject(entity);

                if (request.has("test_uuid") && !Strings.isNullOrEmpty(request.optString("test_uuid"))) {
                    UUID testUuid = UUID.fromString(request.getString("test_uuid"));

                    answer.put("test_uuid", testUuid);
                } else {
                    if (request.has("sequence_number") && !request.isNull("sequence_number") &&
                            request.getInt("sequence_number") == 0) {
                        answer.put("test_uuid", UUID.randomUUID().toString());
                    } else {
                        //null or not set or not 0 --> invalid
                        errorList.addError("ERROR_INVALID_SEQUENCE");
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    }
                }
            } catch (final JSONException | IllegalArgumentException e) {
                errorList.addError("ERROR_REQUEST_JSON");
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        } else {
            errorList.addErrorString("Expected request is missing.");
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }

        answer.putOpt("error", errorList.getList());

        return answer.toString();
    }
}
