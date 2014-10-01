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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamCounter extends FilterInputStream
{
    private long count;
    
    public InputStreamCounter(final InputStream in)
    {
        super(in);
    }
    
    public long getCount()
    {
        return count;
    }
    
    @Override
    public int read() throws IOException
    {
        final int read = in.read();
        if (read != -1)
            count++;
        return read;
    }
    
    @Override
    public int read(final byte[] buffer, final int offset, final int count) throws IOException
    {
        final int read = in.read(buffer, offset, count);
        if (read != -1)
            this.count += read;
        return read;
    }
    
    @Override
    public long skip(final long byteCount) throws IOException
    {
        final long skip = in.skip(byteCount);
        count += skip;
        return skip;
    }
    
    @Override
    public boolean markSupported()
    {
        return false;
    }
    
    @Override
    public synchronized void mark(final int readlimit)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public synchronized void reset() throws IOException
    {
        throw new UnsupportedOperationException();
    }
}
