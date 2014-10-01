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

import org.json.JSONObject;

public abstract class Field extends Number
{
    private static final long serialVersionUID = 1L;
    
    protected final String dbKey;
    protected final String jsonKey;
    protected final boolean readOnly;
    
    public Field(final String dbKey, final String jsonKey, final boolean readOnly)
    {
        super();
        this.dbKey = dbKey;
        this.jsonKey = jsonKey;
        this.readOnly = readOnly;
    }
    
    public String getDbKey()
    {
        return dbKey;
    }
    
    public String getJsonKey()
    {
        return jsonKey;
    }
    
    public StringBuilder appendDbKey(final StringBuilder builder)
    {
        if (builder.length() > 0)
            builder.append(',');
        builder.append('"').append(dbKey).append('"');
        return builder;
    }
    
    public StringBuilder appendDbKeyValue(final StringBuilder builder)
    {
        appendDbKey(builder);
        builder.append("=").append(getSQLValue());
        return builder;
    }
    
    public StringBuilder appendDbValue(final StringBuilder builder)
    {
        if (builder.length() > 0)
            builder.append(',');
        builder.append(getSQLValue());
        return builder;
    }
    
    public CharSequence getSQLValue()
    {
        return "?";
    }
    
    public boolean isReadOnly()
    {
        return readOnly;
    }
    
    public abstract void setField(ResultSet rs) throws SQLException;
    
    public abstract void getField(PreparedStatement ps, int idx) throws SQLException;
    
    public abstract void setField(JSONObject obj);
    
    public abstract void clear();
    
    public abstract boolean isNull();
    
    public abstract void setString(String string);
    
}
