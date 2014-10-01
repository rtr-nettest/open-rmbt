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
package at.alladin.rmbt.android.map;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author bp
 * 
 */
public class MapListSection implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    /**
	 * 
	 */
    private String title;
    
    /**
	 * 
	 */
    private List<MapListEntry> mapListEntryList;
    
    /**
	 * 
	 */
    public MapListSection()
    {
        
    }
    
    /**
     * 
     * @param title
     * @param mapListEntryList
     */
    public MapListSection(final String title, final List<MapListEntry> mapListEntryList)
    {
        setTitle(title);
        setMapListEntryList(mapListEntryList);
    }
    
    /**
     * 
     * @return
     */
    public String getTitle()
    {
        return title;
    }
    
    /**
     * 
     * @param title
     */
    public void setTitle(final String title)
    {
        this.title = title;
    }
    
    /**
     * 
     * @return
     */
    public List<MapListEntry> getMapListEntryList()
    {
        return mapListEntryList;
    }
    
    /**
     * 
     * @param mapListEntryList
     */
    public void setMapListEntryList(final List<MapListEntry> mapListEntryList)
    {
        this.mapListEntryList = mapListEntryList;
        
        for (final MapListEntry entry : mapListEntryList)
            entry.setSection(this);
    }
    
    /**
     * 
     * @return
     */
    public MapListEntry getCheckedMapListEntry()
    {
        
        for (final MapListEntry entry : mapListEntryList)
            if (entry.isChecked())
                return entry;
        
        return null;
    }
}
