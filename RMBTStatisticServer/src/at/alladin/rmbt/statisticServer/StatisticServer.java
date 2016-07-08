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
package at.alladin.rmbt.statisticServer;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

import at.alladin.rmbt.statisticServer.export.ExportResource;
import at.alladin.rmbt.statisticServer.export.ImageExport;
import at.alladin.rmbt.statisticServer.opendata.ChoicesResource;
import at.alladin.rmbt.statisticServer.opendata.HistogramResource;
import at.alladin.rmbt.statisticServer.opendata.OpenTestStatisticsResource;

public class StatisticServer extends Application
{
    
    /**
     * Public Constructor to create an instance of DemoApplication.
     * 
     * @param parentContext
     *            - the org.restlet.Context instance
     */
    public StatisticServer(final Context parentContext)
    {
        super(parentContext);
    }
    
    /**
     * The Restlet instance that will call the correct resource depending up on
     * URL mapped to it.
     * 
     * @return -- The resource Restlet mapped to the URL.
     */
    @Override
    public Restlet createInboundRoot()
    {
        
        final Router router = new Router(getContext());
        
        router.attach("/version", VersionResource.class);
        
        router.attach("/statistics", StatisticsResource.class);

        router.attach("/export/netztest-opendata-{year}-{month}.", ExportResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export/netztest-opendata_hours-{hours}.", ExportResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export", ExportResource.class, Template.MODE_STARTS_WITH);
        
        router.attach("/{lang}/{open_test_uuid}/{size}.png", ImageExport.class);
        
        // administrative resources (access restrictions might be applied to /admin/ 

        router.attach("/opentests/histogram", HistogramResource.class);
        router.attach("/opentests/statistics", OpenTestStatisticsResource.class);
        //router.attach("/opentests/histogra{histogram}", OpenTestSearchResource.class);
        
        router.attach("/opentests/search", at.alladin.rmbt.statisticServer.opendata.OpenTestSearchResource.class, Template.MODE_STARTS_WITH);
        //router.attach("/opentests/search", OpenTestSearchResource.class, Template.MODE_STARTS_WITH);
        
        router.attach("/opentests/choices", ChoicesResource.class, Template.MODE_STARTS_WITH);
        router.attach("/opentests", at.alladin.rmbt.statisticServer.opendata.OpenTestSearchResource.class);        
        
        router.attach("/opentests/O{open_test_uuid}", OpenTestResource.class);
        
        router.attach("/admin/usage", UsageResource.class);
        router.attach("/admin/usageJSON", UsageJSONResource.class);
                        
        return router;
    }
    
}
