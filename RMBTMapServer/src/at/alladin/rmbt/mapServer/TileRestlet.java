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
package at.alladin.rmbt.mapServer;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Parameter;

import at.alladin.rmbt.mapServer.MapServerOptions.MapFilter;
import at.alladin.rmbt.mapServer.MapServerOptions.MapOption;
import at.alladin.rmbt.mapServer.MapServerOptions.SQLFilter;

public abstract class TileRestlet extends Restlet
{
    protected static final int[] TILE_SIZES = new int[] { 256, 512 };
    protected static final Pattern PATH_PATTERN = Pattern.compile("(\\d+)/(\\d+)/(\\d+)");
    protected static final byte[][] EMPTY_IMAGES = new byte[TILE_SIZES.length][];
    protected static final int MAX_ZOOM = 21;
    
    static
    {
        for (int i = 0; i < TILE_SIZES.length; i++)
        {
            final BufferedImage img = new BufferedImage(TILE_SIZES[i], TILE_SIZES[i], BufferedImage.TYPE_INT_ARGB);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try
            {
                ImageIO.write(img, "png", baos);
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
            EMPTY_IMAGES[i] = baos.toByteArray();
        }
    }
    
    protected static class Image
    {
        protected BufferedImage bi;
        protected Graphics2D g;
        protected int width;
        protected int height;
    }
    
    static class DPoint
    {
        double x;
        double y;
    }
    
    static class DBox
    {
        double x1;
        double y1;
        double x2;
        double y2;
        double res;
    }
    
    @SuppressWarnings("unchecked")
    protected final ThreadLocal<Image>[] images = new ThreadLocal[TILE_SIZES.length];
    
    public TileRestlet()
    {
        for (int i = 0; i < TILE_SIZES.length; i++)
        {
            final int tileSize = TILE_SIZES[i];
            images[i] = new ThreadLocal<Image>()
            {
                @Override
                protected Image initialValue()
                {
                    final Image image = new Image();
                    image.bi = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
                    image.g = image.bi.createGraphics();
                    image.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    image.g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
                    image.g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    image.g.setStroke(new BasicStroke(1f));
                    image.width = image.bi.getWidth();
                    image.height = image.bi.getHeight();
                    return image;
                };
            };
        }
    }
    
    protected static int valueToColor(final int[] colors, final double[] intervals, final double value)
    {
        int idx = -1;
        for (int i = 0; i < intervals.length; i++)
            if (value < intervals[i])
            {
                idx = i;
                break;
            }
        if (idx == 0)
            return colors[0];
        if (idx == -1)
            return colors[colors.length - 1];
        
        final double factor = (value - intervals[idx - 1]) / (intervals[idx] - intervals[idx - 1]);
        
        final int c0 = colors[idx - 1];
        final int c1 = colors[idx];
        
        final int c0r = c0 >> 16;
        final int c0g = c0 >> 8 & 0xff;
        final int c0b = c0 & 0xff;
        
        final int r = (int) (c0r + ((c1 >> 16) - c0r) * factor);
        final int g = (int) (c0g + ((c1 >> 8 & 0xff) - c0g) * factor);
        final int b = (int) (c0b + ((c1 & 0xff) - c0b) * factor);
        return r << 16 | g << 8 | b;
    }
    
    public static AtomicInteger maxCount = new AtomicInteger();
    
    @Override
    public void handle(final Request req, final Response res)
    {
//        if (maxCount.get() > 40) // fast hack so that not the complete server is taken down with many requests
//            return;
        try
        {
            maxCount.incrementAndGet();
        
            final Form params = req.getResourceRef().getQueryAsForm();
            
            final String zoomStr = (String) req.getAttributes().get("zoom");
            final String xStr = (String) req.getAttributes().get("x");
            final String yStr = (String) req.getAttributes().get("y");
            
            final int zoom, x, y;
            if (zoomStr != null && xStr != null && yStr != null)
            {
                zoom = Integer.valueOf(zoomStr);
                x = Integer.valueOf(xStr);
                y = Integer.valueOf(yStr);
            }
            else
            {
                final String path = params.getFirstValue("path", true);
                if (path == null)
                    return;
                final Matcher m = PATH_PATTERN.matcher(path);
                if (!m.matches())
                    return;
                zoom = Integer.valueOf(m.group(1));
                x = Integer.valueOf(m.group(2));
                y = Integer.valueOf(m.group(3));
            }
            
            if (zoom < 0 || zoom > MAX_ZOOM)
                return;
            
            if (x < 0 || y < 0)
                return;
            
            int pow = 1 << zoom;
            if (x >= pow || y >= pow)
                return;
            
            int tileSizeIdx = 0;
            
            final String sizeStr = params.getFirstValue("size");
            if (sizeStr != null)
            {
                int size = Integer.valueOf(sizeStr);
                for (int i = 0; i < TILE_SIZES.length; i++)
                {
                    if (size == TILE_SIZES[i])
                    {
                        tileSizeIdx = i;
                        break;
                    }
                }
            }
            
            String mapOptionStr = params.getFirstValue("map_options", true);
            if (mapOptionStr == null) // set default
                mapOptionStr = "mobile/download";
            final MapOption mo = MapServerOptions.getMapOptionMap().get(mapOptionStr);
            if (mo == null)
                return;
            
            final List<SQLFilter> filters = new ArrayList<SQLFilter>(MapServerOptions.getDefaultMapFilters());
            
            // filters from params
            for (final Parameter param : params)
            {
                final MapFilter mapFilter = MapServerOptions.getMapFilterMap().get(param.getName());
                if (mapFilter != null)
                    filters.add(mapFilter.getFilter(param.getValue()));
            }
            
            final DBox box = GeoCalc.xyToMeters(TILE_SIZES[tileSizeIdx], x, y, zoom);
            
            handle(req, res, params, tileSizeIdx, zoom, box, mo, filters);
        }
        finally
        {
            maxCount.decrementAndGet();
        }
    }
    
    protected abstract void handle(Request req, Response res, Form params, int tileSizeIdx, int zoom, DBox box, MapOption mo,
            List<SQLFilter> filters);
}
