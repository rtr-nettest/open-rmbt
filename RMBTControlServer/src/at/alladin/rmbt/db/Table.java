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
package at.alladin.rmbt.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import at.alladin.rmbt.db.fields.Field;

public abstract class Table
{
    protected long uid;
    protected Connection conn = null;
    protected String errorLabel = "";
    protected boolean error = false;
    
    protected final Field[] fields;
    private final Map<String, Field> keyMap = new HashMap<>();
    
    public Table(final Field[] fields, final Connection conn)
    {
        this.fields = fields;
        this.conn = conn;
        reset();
        generateKeyMap();
    }
    
    public Table(final Field[] fields, final Connection conn, final ResultSet rs) throws SQLException
    {
        this(fields, conn);
        setValuesFromResult(rs);
    }
    
    protected void generateKeyMap()
    {
        keyMap.clear();
        for (final Field field : fields)
            keyMap.put(field.getDbKey(), field);
    }
    
    public void reset()
    {
        for (final Field field : fields)
            field.clear();
        
        uid = 0;
        
        resetError();
    }
    
    protected void resetError()
    {
        error = false;
        errorLabel = "";
    }
    
    protected void setError(final String errorLabel)
    {
        error = true;
        this.errorLabel = errorLabel;
    }
    
    protected void setValuesFromResult(final ResultSet rs) throws SQLException
    {
        uid = rs.getLong("uid");
        
        for (final Field field : fields)
            field.setField(rs);
    }
    
    public boolean hasError()
    {
        return error;
    }
    
    public String getError()
    {
        return errorLabel;
    }
    
    public long getUid()
    {
        return uid;
    }
    
    /***********
     * 
     * SETTERS
     * 
     ***********/
    
    public void setFields(final JSONObject object)
    {
        for (final Field field : fields)
            field.setField(object);
    }
    
    public Field getField(final String key)
    {
        return keyMap.get(key);
    }
}
