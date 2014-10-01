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

public abstract class FieldAdapter<T> extends Field
{
    private static final long serialVersionUID = 1L;
    
    protected T value;
    
    public FieldAdapter(final String dbKey, final String jsonKey, final boolean readOnly)
    {
        super(dbKey, jsonKey, readOnly);
    }
    
    @Override
    public void clear()
    {
        value = null;
    }
    
    @Override
    public String toString()
    {
        if (value == null)
            return null;
        return value.toString();
    }
    
    @Override
    public boolean isNull()
    {
        return value == null;
    }
    
    @Override
    public int intValue()
    {
        throw new IllegalFieldTypeException();
    }
    
    @Override
    public long longValue()
    {
        throw new IllegalFieldTypeException();
    }
    
    @Override
    public byte byteValue()
    {
        throw new IllegalFieldTypeException();
    }
    
    @Override
    public short shortValue()
    {
        throw new IllegalFieldTypeException();
    }
    
    @Override
    public float floatValue()
    {
        throw new IllegalFieldTypeException();
    }
    
    @Override
    public double doubleValue()
    {
        throw new IllegalFieldTypeException();
    }
}
