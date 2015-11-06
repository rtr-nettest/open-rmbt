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

import java.util.UUID;

import org.restlet.data.Form;

import com.google.common.base.Strings;
import com.google.common.hash.PrimitiveSink;

public class PointTileParameters extends TileParameters
{
    protected final double pointDiameter;
    protected final boolean noFill;
    protected final boolean noColor;
    protected final UUID highlight;
    protected final PointTileParameters genericParameters; // same without highlight for caching
    
    public PointTileParameters(Path path, Form params)
    {
        this(path, params, false);
    }
    
    protected PointTileParameters(Path path, Form params, boolean generic)
    {
        super(path, params, 0.6);
        
        final String diameterString = params.getFirstValue("point_diameter");
        double _diameter = 8.0;
        if (diameterString != null)
            try
            {
                _diameter = Double.parseDouble(diameterString);
            }
            catch (final NumberFormatException e)
            {
            }
        pointDiameter = _diameter;
        
        final String noFillString = params.getFirstValue("no_fill");
        boolean _noFill = false;
        if (noFillString != null)
            _noFill = Boolean.parseBoolean(noFillString);
        noFill = _noFill;
        
        final String noColorString = params.getFirstValue("no_color");
        boolean _noColor = false;
        if (noColorString != null)
            _noColor = Boolean.parseBoolean(noColorString);
        noColor = _noColor;
        
        if (generic)
            highlight = null;
        else
        {
            String _highlight = params.getFirstValue("highlight");
            if (Strings.isNullOrEmpty(_highlight) || "undefined".equals(_highlight))
                _highlight = null;
            UUID hightlightUUID = null;
            if (_highlight != null)
                try
                {
                    hightlightUUID = UUID.fromString(_highlight);
                }
                catch (final Exception e)
                {
                }
            highlight = hightlightUUID;
        }
        
        if (highlight == null)
            genericParameters = null;
        else
            genericParameters = new PointTileParameters(path, params, true);
    }
    
    public double getPointDiameter()
    {
        return pointDiameter;
    }

    public boolean isNoFill()
    {
        return noFill;
    }

    public boolean isNoColor()
    {
        return noColor;
    }

    public UUID getHighlight()
    {
        return highlight;
    }
    
    @Override
    public boolean isNoCache()
    {
        return highlight != null;
    }
    
    public PointTileParameters getGenericParameters()
    {
        return genericParameters;
    }

    @Override
    public void funnel(TileParameters o, PrimitiveSink into)
    {
        super.funnel(o, into);
        if (o instanceof PointTileParameters)
        {
            final PointTileParameters _o = (PointTileParameters) o;
            into
                .putDouble(_o.pointDiameter)
                .putBoolean(_o.noFill)
                .putBoolean(_o.noColor);
            if (highlight != null)
                into.putUnencodedChars(highlight.toString());
        }
    }
}
