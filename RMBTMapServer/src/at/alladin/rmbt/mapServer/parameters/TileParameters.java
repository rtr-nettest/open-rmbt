/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
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
 *******************************************************************************/
package at.alladin.rmbt.mapServer.parameters;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.restlet.data.Form;
import org.restlet.data.Parameter;

import at.alladin.rmbt.mapServer.MapServerOptions;

import com.google.common.base.Strings;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import com.google.common.hash.PrimitiveSink;

public abstract class TileParameters implements Funnel<TileParameters>
{
    protected static final Pattern PATH_PATTERN = Pattern.compile("(\\d+)/(\\d+)/(\\d+)");
    protected static final int MAX_ZOOM = 21;
    
    protected final Path path;
    protected final int size;
    protected final String mapOption;
    protected final float quantile;
    protected final Map<String,String> filterMap;
    protected final double transparency;
    protected final String developerCode;
    
    public final static class Path implements Funnel<Path>
    {
        protected final int zoom, x, y;
        
        public Path(String zoomStr, String xStr, String yStr, String path)
        {
            if (zoomStr != null && xStr != null && yStr != null)
            {
                zoom = Integer.valueOf(zoomStr);
                x = Integer.valueOf(xStr);
                y = Integer.valueOf(yStr);
            }
            else
            {
                if (path == null)
                    throw new IllegalArgumentException();
                final Matcher m = PATH_PATTERN.matcher(path);
                if (!m.matches())
                    throw new IllegalArgumentException();
                zoom = Integer.valueOf(m.group(1));
                x = Integer.valueOf(m.group(2));
                y = Integer.valueOf(m.group(3));
            }
            if (zoom < 0 || zoom > MAX_ZOOM)
                throw new IllegalArgumentException();
            if (x < 0 || y < 0)
                throw new IllegalArgumentException();
            int pow = 1 << zoom;
            if (x >= pow || y >= pow)
                throw new IllegalArgumentException();
        }
        
        public Path(int zoom, int x, int y)
        {
            this.zoom = zoom;
            this.x = x;
            this.y = y;
        }
        
        public int getZoom()
        {
            return zoom;
        }

        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return y;
        }

        @Override
        public void funnel(Path o, PrimitiveSink into)
        {
            into
                .putInt(o.zoom)
                .putInt(o.x)
                .putInt(o.y);
        }
    }
    
    protected TileParameters(Path path, Form params, double defaultTransparency)
    {
        this.path = path;
        
        int _size = 0;
        final String sizeStr = params.getFirstValue("size", true);
        if (sizeStr != null)
        {
            try
            {
                _size = Integer.valueOf(sizeStr);
            }
            catch (NumberFormatException e)
            {
            }
        }
        size = _size;
        
        mapOption = params.getFirstValue("map_options", true, "mobile/download");
        
        float _quantile = 0.5f; //median is default quantile
        final String statisticalMethod = params.getFirstValue("statistical_method", true);
        if (statisticalMethod != null)
        {
            try
            {
                float __quantile = Float.parseFloat(statisticalMethod);
                if (__quantile >= 0f && __quantile <= 1f)
                    _quantile = __quantile;
            }
            catch (NumberFormatException e)
            {
            }
        }
        quantile = _quantile;
        
        //developer code parameter
        developerCode = params.getFirstValue("developerCode", true, null);
        
        final String transparencyString = params.getFirstValue("transparency");
        double _transparency = defaultTransparency;
        if (transparencyString != null)
            try
            {
                _transparency = Double.parseDouble(transparencyString);
            }
            catch (final NumberFormatException e)
            {
            }
        if (_transparency < 0)
            _transparency = 0;
        if (_transparency > 1)
            _transparency = 1;
        transparency = _transparency;
        
        final TreeMap<String, String> _filterMap = new TreeMap<>();
        for (final Parameter param : params)
        {
            if (MapServerOptions.isValidFilter(param.getName()))
                _filterMap.put(param.getName(), param.getValue());
        }
        filterMap = Collections.unmodifiableMap(_filterMap);
    }
    
    public Path getPath()
    {
        return path;
    }

    public int getSize()
    {
        return size;
    }

    public String getMapOption()
    {
        return mapOption;
    }

    public float getQuantile()
    {
        return quantile;
    }

    public Map<String, String> getFilterMap()
    {
        return filterMap;
    }

    public double getTransparency()
    {
        return transparency;
    }
    
    public abstract boolean isNoCache();

    protected static final Funnel<Map.Entry<String, String>> FILTER_MAP_FUNNEL_ENTRY = new Funnel<Map.Entry<String,String>>()
    {
        @Override
        public void funnel(Entry<String, String> o, PrimitiveSink into)
        {
            into
                .putUnencodedChars(o.getKey())
                .putChar(':')
                .putUnencodedChars(Strings.nullToEmpty(o.getValue()));
        }
    };
    protected static final Funnel<Iterable<? extends Entry<String, String>>> FILTER_MAP_FUNNEL
        = Funnels.sequentialFunnel(FILTER_MAP_FUNNEL_ENTRY);
    
    @Override
    public void funnel(TileParameters o, PrimitiveSink into)
    {
        into
            .putUnencodedChars(o.getClass().getCanonicalName())
            .putInt(o.size)
            .putUnencodedChars(o.mapOption)
            .putFloat(o.quantile)
            .putDouble(o.transparency)
            .putUnencodedChars(Strings.nullToEmpty(o.developerCode));
        o.path.funnel(o.path, into);
        FILTER_MAP_FUNNEL.funnel(o.filterMap.entrySet(), into);
    }
}
