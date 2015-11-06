/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
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
package at.alladin.rmbt.android.about;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.widget.SimpleAdapter;

/**
 * 
 * @author
 * 
 */
public class RMBTAboutAdapter extends SimpleAdapter
{
    
    /**
     * 
     * @param context
     * @param data
     * @param resource
     * @param from
     * @param to
     */
    public RMBTAboutAdapter(final Context context, final List<? extends Map<String, ?>> data, final int resource,
            final String[] from, final int[] to)
    {
        super(context, data, resource, from, to);
    }
    
    /**
	 * 
	 */
    @Override
    public boolean areAllItemsEnabled()
    {
        return false;
    }
    
    /**
	 * 
	 */
    @Override
    public boolean isEnabled(final int position)
    {
        // return false if position == position you want to disable
        switch (position)
        {
        
        case 0:
            return false;
            
        default:
            return true;
        }
    }
}
