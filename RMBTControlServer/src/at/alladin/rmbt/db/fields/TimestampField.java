/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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
package at.alladin.rmbt.db.fields;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import org.json.JSONObject;

public class TimestampField extends FieldAdapter<Timestamp>
{
    private static final long serialVersionUID = 1L;
    
    public TimestampField(final String dbKey, final String jsonKey)
    {
        super(dbKey, jsonKey, false);
    }
    
    public TimestampField(final String dbKey, final String jsonKey, final boolean readOnly)
    {
        super(dbKey, jsonKey, readOnly);
    }
    
    @Override
    public void setField(final ResultSet rs) throws SQLException
    {
        value = rs.getTimestamp(dbKey);
    }
    
    @Override
    public void getField(final PreparedStatement ps, final int idx) throws SQLException
    {
        if (value == null)
            ps.setNull(idx, Types.TIMESTAMP);
        else
            ps.setTimestamp(idx, value);
    }
    
    @Override
    public void setField(final JSONObject obj)
    {
        if (jsonKey != null && obj.has(jsonKey))
            value = Timestamp.valueOf(obj.optString(jsonKey, null));
    }
    
    @Override
    public void setString(final String string)
    {
        value = Timestamp.valueOf(string);
    }
    
    public Date getDate()
    {
        return value;
    }
    
}
