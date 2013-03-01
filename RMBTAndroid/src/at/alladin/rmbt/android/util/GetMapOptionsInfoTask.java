/*******************************************************************************
 * Copyright 2013 alladin-IT OG
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.map.MapListEntry;
import at.alladin.rmbt.android.map.MapListSection;
import at.alladin.rmbt.android.map.MapProperties;

public class GetMapOptionsInfoTask extends AsyncTask<Void, Void, JSONObject>
{
    /**
     * 
     */
    private static final String DEBUG_TAG = "CheckSettingsTask";
    
    /**
     * 
     */
    private final RMBTMainActivity activity;
    
    /**
     * 
     */
    private ControlServerConnection serverConn;
    
    /**
     * 
     */
    private EndTaskListener endTaskListener;
    
    /**
     * 
     */
    private boolean hasError = false;
    
    /**
     * 
     * @param activity
     */
    public GetMapOptionsInfoTask(final RMBTMainActivity activity)
    {
        this.activity = activity;
    }
    
    /**
     * 
     */
    @Override
    protected JSONObject doInBackground(final Void... params)
    {
        JSONObject result = null;
        
        serverConn = new ControlServerConnection(activity.getApplicationContext(), true);
        
        result = serverConn.requestMapOptionsInfo();
        
        return result;
    }
    
    /**
     * 
     */
    @Override
    protected void onCancelled()
    {
        if (serverConn != null)
        {
            serverConn.unload();
            serverConn = null;
        }
    }
    
    /**
     * 
     */
    @Override
    protected void onPostExecute(final JSONObject result)
    {
        if (serverConn.hasError())
            hasError = true;
        else if (result != null)
        {
            try
            {
                final JSONObject mapSettingsObject = result.getJSONObject("mapfilter");
                
                // /
                
                // ////////////////////////////////////////////////////
                // MAP / CHOOSE
                
                final JSONArray mapTypeArray = mapSettingsObject.getJSONArray("mapTypes");
                
                Log.d(DEBUG_TAG, mapTypeArray.toString(4));
                
                // /
                
                final ArrayList<MapListSection> mapListSectionList = new ArrayList<MapListSection>();
                
                for (int cnt = 0; cnt < mapTypeArray.length(); cnt++)
                {
                    
                    final JSONObject t = mapTypeArray.getJSONObject(cnt);
                    
                    final String sectionTitle = t.getString("title");
                    
                    final JSONArray objectOptionsArray = t.getJSONArray("options");
                    
                    // /
                    
                    final List<MapListEntry> mapListEntryList = new ArrayList<MapListEntry>();
                    
                    for (int cnt2 = 0; cnt2 < objectOptionsArray.length(); cnt2++)
                    {
                        
                        final JSONObject s = objectOptionsArray.getJSONObject(cnt2);
                        
                        final String entryTitle = s.getString("title");
                        final String entrySummary = s.getString("summary");
                        final String value = s.getString("map_options");
                        final String overlayType = s.getString("overlay_type");
                        
                        final MapListEntry mapListEntry = new MapListEntry(entryTitle, entrySummary);
                        
                        mapListEntry.setKey("map_options");
                        mapListEntry.setValue(value);
                        mapListEntry.setOverlayType(overlayType);
                        
                        mapListEntryList.add(mapListEntry);
                    }
                    
                    final MapListSection mapListSection = new MapListSection(sectionTitle, mapListEntryList);
                    mapListSectionList.add(mapListSection);
                }
                
                // ////////////////////////////////////////////////////
                // MAP / FILTER
                
                final JSONObject mapFiltersObject = mapSettingsObject.getJSONObject("mapFilters");
                
                final HashMap<String,List<MapListSection>> mapFilterListSectionListHash = new HashMap<String,List<MapListSection>>();
                
//                Log.d(DEBUG_TAG, mapFilterArray.toString(4));
                
                for (final String typeKey : new String[]{ "mobile", "wifi", "browser" })
                {
                    final JSONArray mapFilterArray = mapFiltersObject.getJSONArray(typeKey);
                    final List<MapListSection> mapFilterListSectionList = new ArrayList<MapListSection>();
                    mapFilterListSectionListHash.put(typeKey, mapFilterListSectionList);
                    
                    
                 // add map appearance option (satellite, no satellite)
                    final MapListSection appearanceSection = new MapListSection(
                            activity.getString(R.string.map_appearance_header), Arrays.asList(
                                    new MapListEntry(activity.getString(R.string.map_appearance_nosat_title), activity
                                            .getString(R.string.map_appearance_nosat_summary), true,
                                            MapProperties.MAP_SAT_KEY, MapProperties.MAP_NOSAT_VALUE),
                                    new MapListEntry(activity.getString(R.string.map_appearance_sat_title), activity
                                            .getString(R.string.map_appearance_sat_summary), MapProperties.MAP_SAT_KEY,
                                            MapProperties.MAP_SAT_VALUE)));
                    
                    mapFilterListSectionList.add(appearanceSection);
                    
                    // add overlay option (heatmap, points)
                    final MapListSection overlaySection = new MapListSection(
                            activity.getString(R.string.map_overlay_header), Arrays.asList(
                                    new MapListEntry(activity.getString(R.string.map_overlay_auto_title), activity
                                            .getString(R.string.map_overlay_auto_summary), true,
                                            MapProperties.MAP_OVERLAY_KEY, MapProperties.MAP_AUTO_VALUE),
                                    new MapListEntry(activity.getString(R.string.map_overlay_heatmap_title), activity
                                            .getString(R.string.map_overlay_heatmap_summary),
                                            MapProperties.MAP_OVERLAY_KEY, MapProperties.MAP_HEATMAP_VALUE),
                                    new MapListEntry(activity.getString(R.string.map_overlay_points_title), activity
                                            .getString(R.string.map_overlay_points_summary), MapProperties.MAP_OVERLAY_KEY,
                                            MapProperties.MAP_POINTS_VALUE)));
                    
                    mapFilterListSectionList.add(overlaySection);
                    
                    // add other filter options
                    
                    for (int cnt = 0; cnt < mapFilterArray.length(); cnt++)
                    {
                        
                        final JSONObject t = mapFilterArray.getJSONObject(cnt);
                        
                        final String sectionTitle = t.getString("title");
                        
                        final JSONArray objectOptionsArray = t.getJSONArray("options");
                        
                        // /
                        
                        final List<MapListEntry> mapListEntryList = new ArrayList<MapListEntry>();
                        
                        for (int cnt2 = 0; cnt2 < objectOptionsArray.length(); cnt2++)
                        {
                            
                            final JSONObject s = objectOptionsArray.getJSONObject(cnt2);
                            
                            final String entryTitle = s.getString("title");
                            final String entrySummary = s.getString("summary");
                            
                            s.remove("title");
                            s.remove("summary");
                            
                            //
                            
                            final MapListEntry mapListEntry = new MapListEntry(entryTitle, entrySummary);
                            
                            //
                            
                            final JSONArray sArray = s.names();
                            
                            if (sArray != null && sArray.length() > 0)
                            {
                                
                                final String key = sArray.getString(0);
                                
                                mapListEntry.setKey(key);
                                mapListEntry.setValue(s.getString(key));
                            }
                            
                            // always select first element in sections
                            mapListEntry.setChecked(cnt2 == 0);
                            
                            // /
                            
                            mapListEntryList.add(mapListEntry);
                        }
                        
                        final MapListSection mapListSection = new MapListSection(sectionTitle, mapListEntryList);
                        mapFilterListSectionList.add(mapListSection);
                    }
                }
                
                
                
                // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                
                // map type
                final MapListEntry entry = mapListSectionList.get(0).getMapListEntryList().get(0);
                
                activity.setCurrentMapType(entry);
                
                // map filter
                
                
                // unnecessary ?
//                for (final MapListSection section : mapFilterListSectionListHash)
//                {
//                    
//                    final MapListEntry checkedEntry = section.getCheckedMapListEntry();
//                    
//                    if (checkedEntry.getKey() != null && checkedEntry.getValue() != null)
//                        activity.getCurrentMapOptions().put(checkedEntry.getKey(), checkedEntry.getValue());
//                    
//                    activity.getCurrentMapOptionTitles().put(checkedEntry.getKey(),
//                            checkedEntry.getSection().getTitle() + ": " + checkedEntry.getTitle());
//                }
                
//                System.out.println(activity.getCurrentMapOptions());
//                System.out.println(activity.getCurrentMapOptionTitles());
                
                // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                
                activity.setMapTypeListSectionList(mapListSectionList);
                activity.setMapFilterListSectionListMap(mapFilterListSectionListHash);
                
            }
            catch (final JSONException e)
            {
                e.printStackTrace();
            }
            
        }
        else
            Log.i(DEBUG_TAG, "LEERE LISTE");
        
        if (endTaskListener != null)
        {
            final JSONArray array = new JSONArray();
            array.put(result);
            endTaskListener.taskEnded(array);
        }
    }
    
    /**
     * 
     * @param endTaskListener
     */
    public void setEndTaskListener(final EndTaskListener endTaskListener)
    {
        this.endTaskListener = endTaskListener;
    }
    
    /**
     * 
     * @return
     */
    public boolean hasError()
    {
        return hasError;
    }
}
