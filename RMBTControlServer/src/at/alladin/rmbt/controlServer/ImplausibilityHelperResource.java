/*******************************************************************************
 * Copyright 2015 Thomas Schreiber
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
package at.alladin.rmbt.controlServer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Post;

/**
 * Allow admins to set some tests implausible 
 * @author Thomas
 *
 */
public class ImplausibilityHelperResource extends ServerResource{
	
	@Post("json")
    public String request(final String entity) throws JSONException
    {   	
        //addAllowOrigin();
		final JSONObject answer = new JSONObject();
		JSONObject request;
        try {
        	 request = new JSONObject(entity);
        }
        catch(Exception ex) {
        	answer.put("error","invalid request format");
        	return answer.toString(4); 
        	
        }
        boolean hasErrors = false;
        
        String comment = request.optString("comment", "");
        boolean setImplausible = request.optBoolean("implausible",true);
        String uuid = request.optString("uuid", "");
        String uuidField = null;
        if (!comment.isEmpty() && !uuid.isEmpty()) {       	
        	//get type of uuid
            switch(uuid.charAt(0)) {
            case 'P':
            	uuidField = "open_uuid";
            	break;
            case 'O':
            	uuidField = "open_test_uuid";
            	break;
            case 'T':
            	uuidField = "t.uuid";
            	break;
            case 'U':
            	uuidField = "c.uuid";
            	break;
			default:
				answer.put("error", "invalid uuid type");
				hasErrors = true;
			}
            
            if (!hasErrors) {
            	//try to parse uuid
                uuid = uuid.substring(1);
                try {
                	UUID realUUID = UUID.fromString(uuid);
                	if (!realUUID.toString().equals(uuid)) {
                		throw new IllegalArgumentException();
                	}
                }
                catch (IllegalArgumentException ex) {
                	answer.put("error", "invalid uuid");
                	hasErrors = true;
                } 
            }
		} else {
			answer.put("error", "required fields missing");
			hasErrors = true;
		}

		if (hasErrors) {
			return answer.toString(4);
		}

		//add identifier
		comment = comment + " [web]";		
		try {
			//update all rows
			PreparedStatement ps = conn.prepareStatement("UPDATE test SET implausible = ?, comment=? WHERE NOT implausible = ? AND deleted=FALSE AND uid IN ("
					+ "SELECT t.uid FROM test t INNER JOIN client c ON t.client_id = c.uid WHERE " + uuidField + " = ?"
					+ ");");
			
			ps.setBoolean(1, setImplausible); //SET
			ps.setString(2, comment); //SET
			ps.setBoolean(3, setImplausible); //WHERE
			ps.setObject(4, uuid, Types.OTHER); //inner SELECT
			int affected = ps.executeUpdate();
			ps.close();
			
			answer.put("status", "OK");
			answer.put("affected_rows", affected);
			return answer.toString(4);
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			answer.put("error", "error while updating");
			return answer.toString(4);
		}
		
	}
}
