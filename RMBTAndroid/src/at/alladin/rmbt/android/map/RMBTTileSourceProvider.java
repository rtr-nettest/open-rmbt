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
package at.alladin.rmbt.android.map;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import android.util.Log;

import com.google.android.gms.maps.model.UrlTileProvider;

/**
 * 
 * @author bp
 * 
 */
public class RMBTTileSourceProvider extends UrlTileProvider
{
    
    /**
	 * 
	 */
    private String options;
    
    /**
     * display density
     */
    private final float density;
    
    private final int tileSize;
    
    /**
	 * 
	 */
    private String path;
    
    /**
     * 
     * @param aName
     * @param aResourceId
     * @param aZoomMinLevel
     * @param aZoomMaxLevel
     * @param aTileSizePixels
     * @param density
     */
    public RMBTTileSourceProvider(final int tileSize, final float density)
    {
        super(tileSize, tileSize);
        this.density = density;
        this.tileSize = tileSize;
        
        path = MapProperties.HEATMAP_PATH; // heatmap is default
    }
    
    @Override
    public URL getTileUrl(int x, int y, int zoom)
    {
        URI uri = null;
        try
        {
            uri = new URI(MapProperties.OVERLAY_PROTOCOL, null, MapProperties.OVERLAY_HOST,
                    MapProperties.OVERLAY_PORT,
                    /* MapProperties.HEATMAP_PATH */path,
                    String.format(Locale.US, "%spath=%d/%d/%d&point_diameter=%d&size=%d",
                    options, zoom, x, y, (int)(density * MapProperties.POINT_DIAMETER), tileSize),
                    null);
            
//            System.out.println(uri.toASCIIString());
            
            return uri.toURL();
        }
        catch (final URISyntaxException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 
     * @param path
     */
    public void setPath(final String path)
    {
        if (!MapProperties.HEATMAP_PATH.equals(path) && !MapProperties.POINTS_PATH.equals(path) && !MapProperties.SHAPES_PATH.equals(path))
            return;
        
        this.path = path;
    }
    
    /**
     * 
     * @param optionMap
     */
    public void setOptionMap(final Map<String, String> optionMap)
    {
        
        options = "";
        
        for (final String key : optionMap.keySet())
        {
            
            if (MapProperties.MAP_OVERLAY_KEY.equals(key))
                // skip map_overlay_key
                continue;
            
            final String value = optionMap.get(key);
            
            if (value != null && value.length() > 0)
            {
                
                if (options.length() > 0)
                    options += "&";
                
                options += key + "=" + value;
            }
        }
        
        if (options.length() > 0)
            options += "&";
        
        Log.i("options", options);
    }

}
