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

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class MapServer extends Application
{
    @Override
    public Restlet createInboundRoot()
    {
        final Router router = new Router(getContext());
        
        final PointTiles pointTiles = new PointTiles();
        router.attach("/points/{zoom}/{x}/{y}.png", pointTiles);
        router.attach("/points", pointTiles);
        
        final HeatmapTiles heatmapTiles = new HeatmapTiles();
        router.attach("/heatmap/{zoom}/{x}/{y}.png", heatmapTiles);
        router.attach("/heatmap", heatmapTiles);
        
        final ShapeTiles shapeTiles = new ShapeTiles();
        router.attach("/shapes/{zoom}/{x}/{y}.png", shapeTiles);
        router.attach("/shapes", shapeTiles);
        
        router.attach("/markers", MarkerResource.class);
        
        router.attach("/info", InfoResource.class);
        
        return router;
    }
}
