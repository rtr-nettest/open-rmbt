/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
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

import java.util.Map;

import com.google.android.gms.maps.model.LatLng;

import at.alladin.rmbt.util.model.option.OptionFunctionCallback;

/**
 * 
 * @author alladin-IT GmbH
 * 
 */
public interface MapProperties
{
    
    /**
	 * 
	 */
    public static final int TILE_SIZE = 256;
    
    /**
	 * 
	 */
    public static final LatLng DEFAULT_MAP_CENTER = new LatLng(48.20855, 16.37312);
    
    public static final float DEFAULT_MAP_ZOOM = 10.5f;
    
    public static final float DEFAULT_MAP_ZOOM_LOCATION = 12f;
    
    public static final float POINT_MAP_ZOOM = 14f;
    
    /**
	 * 
	 */
    /* north, east, south, west */
    // 49.5, 17.5, 46.25, 9.25 -> 49.05, 17.25, 46.35, 9.4
    // http://www.openstreetmap.org/?minlon=9.45&minlat=46.355&maxlon=17.20&maxlat=49.00&box=yes
    // public static final BoundingBoxE6 BOUNDING_BOX = new BoundingBoxE6(49,
    // 17.2, 46.355, 9.45);
    
    /**
	 * 
	 */
    public static final String HEATMAP_PATH = "/RMBTMapServer/tiles/heatmap";
    
    /**
     * 
     */
    public static final String SHAPES_PATH = "/RMBTMapServer/tiles/shapes";
    
    /**
	 * 
	 */
    public static final String POINTS_PATH = "/RMBTMapServer/tiles/points";
    
    /**
     * 
     */
    public static final String MARKER_PATH = "/RMBTMapServer/tiles/markers";
    
    /**
     * 
     */
    public static final String MAP_OPTIONS_PATH = "/RMBTMapServer/tiles/info";
    
    /**
     * 
     */
    public static final String MAP_OPTIONS_PATH_V2 = "/RMBTMapServer/v2/tiles/info";
    
    /**
	 * 
	 */
    public static final String MAP_SAT_KEY = "_SAT";
    
    /**
	 * 
	 */
    public static final String MAP_SAT_VALUE = "SAT";
    
    /**
	 * 
	 */
    public static final String MAP_NOSAT_VALUE = "NOSAT";
    
    /**
	 * 
	 */
    public static final String MAP_OVERLAY_KEY = "_OVERLAY";
    
    /**
     * 
     */
    public static final int MAP_AUTO_SWITCH_VALUE = 12;
    
    /**
     * 
     */
    public static final String MAP_AUTO_VALUE = "AUTO";
    
    /**
	 * 
	 */
    public static final String MAP_HEATMAP_VALUE = "HEATMAP";
    
    /**
	 * 
	 */
    public static final String MAP_POINTS_VALUE = "POINTS";
    
    /**
	 * 
	 */
    public static final String MAP_SHAPES_VALUE = "SHAPES";
    
    
    public static final int MAP_OVERLAY_TYPE_AUTO = 1;
    public static final int MAP_OVERLAY_TYPE_HEATMAP = 2;
    public static final int MAP_OVERLAY_TYPE_POINTS = 3;
    public static final int MAP_OVERLAY_TYPE_SHAPES = 4;
    
    /**
     * 
     */
    public static final int POINT_DIAMETER = 8;
    
    /**
     * 
     */
    public static final double TAB_DIAMETER_FACTOR = 2;

    public Map<String, String> getCurrentMapOptions(final OptionFunctionCallback callback);
}
