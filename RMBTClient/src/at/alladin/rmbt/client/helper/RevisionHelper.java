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
package at.alladin.rmbt.client.helper;

import java.io.InputStream;
import java.util.Properties;

public final class RevisionHelper
{
    public static final String describe;
    public static final String revision;
    public static final String gitId;
    public static final String branch;
    public static final boolean dirty;
    
    static
    {
        String _describe = null;
        String _revision = null;
        String _gitId = null;
        String _branch = null;
        boolean _dirty = false;
        final InputStream svnIS = RevisionHelper.class.getClassLoader().getResourceAsStream("revision.properties");
        final Properties properties = new Properties();
        if (svnIS != null)
            try
            {
                properties.load(svnIS);
                _describe = properties.getProperty("git.describe");
                _gitId = properties.getProperty("git.id");
                _branch = properties.getProperty("git.branch");
                final String dirtyString = properties.getProperty("git.dirty");
                _dirty = dirtyString != null && dirtyString.equals("true");
                _revision = properties.getProperty("git.revision");
                if (_dirty)
                {
                    _revision = _revision + "M";
                    _gitId = _gitId + "M";
                }
            }
            catch (final Exception e)
            { // there isn't much we can do here about it..
            }
        describe = _describe == null ? "?" : _describe;
        revision = _revision == null ? "?" : _revision;
        gitId = _gitId == null ? "?" : _gitId;
        branch = _branch == null ? "?" : _branch;
        dirty = _dirty;
    }
    
    public static String getVerboseRevision()
    {
        return String.format("%s_%s", branch, describe);
    }
    
    public static String getServerVersion()
    {
        return gitId;
    }
}
