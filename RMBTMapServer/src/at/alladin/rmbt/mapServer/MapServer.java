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
        
        router.attach("/version", VersionResource.class);
        
        final PointTiles pointTiles = new PointTiles();
        router.attach("/tiles/points/{zoom}/{x}/{y}.png", pointTiles);
        router.attach("/tiles/points", pointTiles);
        
        final HeatmapTiles heatmapTiles = new HeatmapTiles();
        router.attach("/tiles/heatmap/{zoom}/{x}/{y}.png", heatmapTiles);
        router.attach("/tiles/heatmap", heatmapTiles);
        
        final ShapeTiles shapeTiles = new ShapeTiles();
        router.attach("/tiles/shapes/{zoom}/{x}/{y}.png", shapeTiles);
        router.attach("/tiles/shapes", shapeTiles);
        
        router.attach("/tiles/markers", MarkerResource.class);
        
        router.attach("/tiles/info", InfoResource.class);
        router.attach("/v2/tiles/info", at.alladin.rmbt.mapServer.v2.InfoResource.class);
        
        return router;
    }
}
