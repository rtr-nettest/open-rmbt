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
package at.alladin.rmbt.android.util;

import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

public class SectionListAdapter extends BaseAdapter
{
    
    private final ArrayAdapter<String> sectionAdapter;
    private final Map<String, Adapter> sectionMap;
    private final boolean hasSectionHeader;
    
    public final static int TYPE_SECTION_HEADER = 0;
    
    public SectionListAdapter(final Context context, final int sectionLayoutId, final boolean hasSectionHeader)
    {
        sectionAdapter = new ArrayAdapter<String>(context, sectionLayoutId, android.R.id.title);
        sectionMap = new LinkedHashMap<String, Adapter>();
        this.hasSectionHeader = hasSectionHeader;
    }
    
    public void addSection(final String sectionName, final Adapter sectionContentAdapter)
    {
        sectionAdapter.add(sectionName);
        
        sectionMap.put(sectionName, sectionContentAdapter);
    }
    
    @Override
    public boolean areAllItemsEnabled()
    {
        return false;
    }
    
    @Override
    public boolean isEnabled(final int position)
    {
        return getItemViewType(position) != TYPE_SECTION_HEADER;
    }
    
    @Override
    public int getViewTypeCount()
    {
        int total = 1;
        
        for (final Adapter adapter : sectionMap.values())
            total += adapter.getViewTypeCount();
        
        return total;
    }
    
    @Override
    public int getItemViewType(int position)
    {
        int type = 1;
        
        for (final Object section : sectionMap.keySet())
        {
            
            final Adapter adapter = sectionMap.get(section);
            final int size = adapter.getCount() + (hasSectionHeader ? 1 : 0);
            
            if (position == 0 && hasSectionHeader)
                return TYPE_SECTION_HEADER;
            
            if (position < size)
                return type + adapter.getItemViewType(position - 1);
            
            position -= size;
            type += adapter.getViewTypeCount();
        }
        
        return -1;
    }
    
    @Override
    public int getCount()
    {
        
        int count = 0;
        
        for (final Adapter adapter : sectionMap.values())
            count += adapter.getCount() + (hasSectionHeader ? 1 : 0);
        
        return count;
    }
    
    @Override
    public Object getItem(int position)
    {
        
        for (final Map.Entry<String,Adapter> entry : sectionMap.entrySet())
        {
            final Adapter adapter = entry.getValue();
            
            final int size = adapter.getCount() + (hasSectionHeader ? 1 : 0);
            
            if (position == 0 && hasSectionHeader)
                return entry.getKey();
            
            if (position < size)
                return adapter.getItem(position - 1);
            
            position -= size;
        }
        
        return null;
    }
    
    public Adapter getAdapter(int position)
    {
        for (final Adapter adapter : sectionMap.values())
        {
            final int size = adapter.getCount() + (hasSectionHeader ? 1 : 0);
            
            if (position == 0 && hasSectionHeader)
                return null;
            
            if (position < size)
                return adapter;
            
            position -= size;
        }
        
        return null;
    }
    
    public int indexOf(Object object)
    {
        int idx = 0;
        for (final Map.Entry<String,Adapter> entry : sectionMap.entrySet())
        {
            final Adapter adapter = entry.getValue();
            
            if (object.equals(entry.getKey()))
                return idx;
            
            for (int i = 0; i < adapter.getCount(); i++)
            {
                if (object.equals(adapter.getItem(i)))
                    return idx + (hasSectionHeader ? 1 : 0) + i;
            }
            
            idx += adapter.getCount() + (hasSectionHeader ? 1 : 0);
        }
        return -1;
    }
    
    @Override
    public long getItemId(final int position)
    {
        return position;
    }
    
    @Override
    public View getView(int position, final View convertView, final ViewGroup parent)
    {
        
        int sectionNum = 0;
        
        for (final String sectionName : sectionMap.keySet())
        {
            final Adapter adapter = sectionMap.get(sectionName);
            
            final int size = adapter.getCount() + (hasSectionHeader ? 1 : 0);
            
            if (position == 0 && hasSectionHeader)
                return sectionAdapter.getView(sectionNum, convertView, parent);
            
            if (position < size)
                return adapter.getView(position - 1, convertView, parent);
            
            position -= size;
            sectionNum++;
        }
        
        return null;
    }
    
}
