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

/**
 * 
 * @author bp
 * 
 */
public class MapListEntry implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    /**
	 * 
	 */
    private String title;
    
    /**
	 * 
	 */
    private String summary;
    
    /**
	 * 
	 */
    private String key;
    
    /**
	 * 
	 */
    private String value;
    
    /**
	 * 
	 */
    private MapListSection section;
    
    /**
	 * 
	 */
    private boolean checked;
    
    private boolean _default;
    
    private String overlayType;
    
    /**
	 * 
	 */
    public MapListEntry()
    {
        
    }
    
    /**
     * 
     * @param title
     * @param summary
     */
    public MapListEntry(final String title, final String summary)
    {
        this(title, summary, false, null, null, false);
    }
    
    /**
     * 
     * @param title
     * @param summary
     * @param key
     * @param value
     */
    public MapListEntry(final String title, final String summary, final String key, final String value)
    {
        this(title, summary, false, key, value, false);
    }
    
    /**
     * 
     * @param title
     * @param summary
     * @param key
     * @param value
     */
    public MapListEntry(final String title, final String summary, final boolean checked, final String key,
            final String value, final boolean _default)
    {
        setTitle(title);
        setSummary(summary);
        setKey(key);
        setValue(value);
        setChecked(checked);
        setDefault(_default);
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
    public String getSummary()
    {
        return summary;
    }
    
    /**
     * 
     * @param summary
     */
    public void setSummary(final String summary)
    {
        this.summary = summary;
    }
    
    /**
     * 
     * @return
     */
    public String getKey()
    {
        return key;
    }
    
    /**
     * 
     * @param key
     */
    public void setKey(final String key)
    {
        if (key == null || key.length() == 0)
            return;
        
        this.key = key;
    }
    
    /**
     * 
     * @return
     */
    public String getValue()
    {
        return value;
    }
    
    /**
     * 
     * @param value
     */
    public void setValue(final String value)
    {
        this.value = value;
    }
    
    /**
     * 
     * @return
     */
    public MapListSection getSection()
    {
        return section;
    }
    
    /**
     * 
     * @param section
     */
    public void setSection(final MapListSection section)
    {
        this.section = section;
    }
    
    /**
     * 
     * @return
     */
    public boolean isChecked()
    {
        return checked;
    }
    
    /**
     * 
     * @param checked
     */
    public void setChecked(final boolean checked)
    {
        this.checked = checked;
    }
    
    public void setDefault(boolean _default)
    {
        this._default = _default;
    }
    
    public boolean isDefault()
    {
        return _default;
    }
    
    /**
	 * 
	 */
    @Override
    public String toString()
    {
        return title + ", " + summary;
    }
    
    public void setOverlayType(String overlayType)
    {
        this.overlayType = overlayType;
    }
    
    public String getOverlayType()
    {
        return overlayType;
    }
    
}
