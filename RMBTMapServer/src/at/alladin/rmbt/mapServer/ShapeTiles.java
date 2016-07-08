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
package at.alladin.rmbt.mapServer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.postgis.Geometry;
import org.postgis.MultiPolygon;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;
import org.restlet.data.Form;

import at.alladin.rmbt.mapServer.MapServerOptions.MapOption;
import at.alladin.rmbt.mapServer.MapServerOptions.SQLFilter;
import at.alladin.rmbt.mapServer.parameters.ShapeTileParameters;
import at.alladin.rmbt.mapServer.parameters.TileParameters.Path;

import com.google.common.base.Strings;

public class ShapeTiles extends TileRestlet<ShapeTileParameters>
{
    
    private static class GeometryColor
    {
        final Geometry geometry;
        final Color color;
        
        public GeometryColor(final Geometry geometry, final Color color)
        {
            this.geometry = geometry;
            this.color = color;
        }
    }
    
    @Override
    protected ShapeTileParameters getTileParameters(Path path, Form params)
    {
        return new ShapeTileParameters(path, params);
    }
    
    @Override
    protected byte[] generateTile(final ShapeTileParameters params, final int tileSizeIdx, final int zoom, final DBox box,
            final MapOption mo, final List<SQLFilter> filters, final float quantile)
    {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        double _transparency = params.getTransparency();
        
        try
        {
            con = DbConnection.getConnection();
            
            final StringBuilder whereSQL = new StringBuilder(mo.sqlFilter);
            for (final SQLFilter sf : filters)
                whereSQL.append(" AND ").append(sf.where);
            
            /*
             * old zip code
             * 
            final String sql = String.format(
                    "WITH box AS" 
                    + " (SELECT ST_SetSRID(ST_MakeBox2D(ST_Point(?,?),"
                    + " ST_Point(?,?)), 900913) AS box)" 
                    + " SELECT"
                    + " ST_SnapToGrid(ST_intersection(p.the_geom, box.box), ?,?,?,?) AS geom," 
                    + " count(\"%1$s\") count,"
                    + " quantile(\"%1$s\",?) val" 
                    + " FROM box, plz2001 p" 
                    + " JOIN v_test2 t ON t.zip_code=p.plz_4"
                    + " WHERE" + " %2$s" 
                    + " AND p.the_geom && box.box" 
                    + " AND ST_intersects(p.the_geom, box.box)"
                    + " GROUP BY p.the_geom, box.box", mo.valueColumnLog, whereSQL);
            */
            
            final String sql = String.format(
                    "WITH box AS" 
                    + " (SELECT ST_SetSRID(ST_MakeBox2D(ST_Point(?,?),"
                    + " ST_Point(?,?)), 900913) AS box)" 
                    + " SELECT"
                    + " ST_SnapToGrid(ST_intersection(p.the_geom, box.box), ?,?,?,?) AS geom," 
                    + " count(\"%1$s\") count,"
                    + " quantile(\"%1$s\",?) val" 
                    + " FROM box, kategorisierte_gemeinden p" 
                    + " JOIN v_test2 t ON t.gkz=p.gemeinde_i"
                    + " WHERE" + " %2$s" 
                    + " AND p.the_geom && box.box" 
                    + " AND ST_intersects(p.the_geom, box.box)"
                    + " GROUP BY p.the_geom, box.box", mo.valueColumnLog, whereSQL);
            
            
            ps = con.prepareStatement(sql);
            
            int idx = 1;
            
            /* makeBox2D */
            final double margin = box.res * 1;
            ps.setDouble(idx++, box.x1 - margin);
            ps.setDouble(idx++, box.y1 - margin);
            ps.setDouble(idx++, box.x2 + margin);
            ps.setDouble(idx++, box.y2 + margin);
            
            /* snapToGrid */
            ps.setDouble(idx++, box.x1);
            ps.setDouble(idx++, box.y1);
            ps.setDouble(idx++, box.res);
            ps.setDouble(idx++, box.res);
            
            ps.setFloat(idx++, quantile);
            
            for (final SQLFilter sf : filters)
                idx = sf.fillParams(idx, ps);
            
            rs = ps.executeQuery();
            if (rs == null)
                throw new IllegalArgumentException();
            
            final List<GeometryColor> geoms = new ArrayList<>();
            while (rs.next())
            {
                final String geomStr = rs.getString("geom");
                if (! Strings.isNullOrEmpty(geomStr))
                {
                    final Geometry geom = PGgeometry.geomFromString(geomStr);
                    
                    final long count = rs.getLong("count");
                    final double val = rs.getDouble("val");
                    final int colorInt = valueToColor(mo.colorsSorted, mo.intervalsSorted, val);
                    double transparency = ((double)count / 20d) * _transparency;
                    if (transparency > _transparency)
                        transparency = _transparency;
                    final int alpha = (int) Math.round(transparency * 255) << 24;
                    final Color color = new Color(colorInt | alpha, true);
                    
                    geoms.add(new GeometryColor(geom, color));
                }
            }
            
            if (geoms.isEmpty())
                return null;
            
            final Image img = images[tileSizeIdx].get();
            final Graphics2D g = img.g;
            
            g.setBackground(new Color(0, 0, 0, 0));
            g.clearRect(0, 0, img.width, img.height);
//                    g.setComposite(AlphaComposite.Src);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            final Path2D.Double path = new Path2D.Double();
            
            for (final GeometryColor geomColor : geoms)
            {
                final Geometry geom = geomColor.geometry;
                
                final Polygon[] polys;
                if (geom instanceof MultiPolygon)
                    polys = ((MultiPolygon) geom).getPolygons();
                else if (geom instanceof Polygon)
                    polys = new Polygon[] { (Polygon) geom };
                else
                    polys = new Polygon[] {};
                
                for (final Polygon poly : polys)
                    for (int i = 0; i < poly.numRings(); i++)
                    {
                        final Point[] points = poly.getRing(i).getPoints();
                        
                        path.reset();
                        boolean initial = true;
                        for (final Point point : points)
                        {
                            final double relX = (point.x - box.x1) / box.res;
                            final double relY = TILE_SIZES[tileSizeIdx] - (point.y - box.y1) / box.res;
                            if (initial)
                            {
                                initial = false;
                                path.moveTo(relX, relY);
                            }
                            path.lineTo(relX, relY);
                        }
                        g.setPaint(geomColor.color);
                        g.fill(path);
                    }
            }
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img.bi, "png", baos);
            final byte[] data = baos.toByteArray();
            return data;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        finally
        {
            try
            {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (con != null)
                    con.close();
            }
            catch (final SQLException e)
            {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }
    }
    
}
