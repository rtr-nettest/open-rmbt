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

public abstract class NumberFieldAdapter<T extends Number> extends FieldAdapter<T>
{
    private static final long serialVersionUID = 1L;
    
    public NumberFieldAdapter(final String dbKey, final String jsonKey, final boolean readOnly)
    {
        super(dbKey, jsonKey, readOnly);
    }
    
    @Override
    public int intValue()
    {
        if (value == null)
            return 0;
        return value.intValue();
    }
    
    @Override
    public long longValue()
    {
        if (value == null)
            return 0;
        return value.longValue();
    }
    
    @Override
    public double doubleValue()
    {
        if (value == null)
            return 0;
        return value.doubleValue();
    }
    
    @Override
    public float floatValue()
    {
        if (value == null)
            return 0;
        return value.floatValue();
    }
    
    @Override
    public byte byteValue()
    {
        if (value == null)
            return 0;
        return value.byteValue();
    }
    
    @Override
    public short shortValue()
    {
        if (value == null)
            return 0;
        return value.shortValue();
    }
}
