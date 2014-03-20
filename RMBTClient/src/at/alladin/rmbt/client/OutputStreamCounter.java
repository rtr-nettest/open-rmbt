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
package at.alladin.rmbt.client;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamCounter extends FilterOutputStream
{
    private long count;
    
    public OutputStreamCounter(final OutputStream out)
    {
        super(out);
    }
    
    public long getCount()
    {
        return count;
    }
    
    @Override
    public void write(final byte[] buffer, final int offset, final int length) throws IOException
    {
        out.write(buffer, offset, length);
        count += length;
    }
    
    @Override
    public void write(final int oneByte) throws IOException
    {
        out.write(oneByte);
        count++;
    }
}
