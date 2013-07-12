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
package at.alladin.rmbt.controlServer;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

import at.alladin.rmbt.controlServer.export.ExportResource;
import at.alladin.rmbt.controlServer.export.ImageExport;

public class ControlServer extends Application
{
    
    /**
     * Public Constructor to create an instance of DemoApplication.
     * 
     * @param parentContext
     *            - the org.restlet.Context instance
     */
    public ControlServer(final Context parentContext)
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
        
        router.attach("/", RegistrationResource.class);
        
        router.attach("/result", ResultResource.class);
        
        router.attach("/resultUpdate", ResultUpdateResource.class);
        
        router.attach("/ndtResult", NdtResultResource.class);
        
        router.attach("/news", NewsResource.class);
        
        router.attach("/history", HistoryResource.class);
        
        router.attach("/testresult", TestResultResource.class);
        
        router.attach("/testresultdetail", TestResultDetailResource.class);
        
        router.attach("/sync", SyncResource.class);
        
        router.attach("/settings", SettingsResource.class);
        
        router.attach("/requestDataCollector", RequestDataCollector.class);
        
        router.attach("/statistics", StatisticsResource.class);
        
        router.attach("/export", ExportResource.class, Template.MODE_STARTS_WITH);
        
        router.attach("/usage", UsageResource.class);
        
        router.attach("/opentests", OpenTestResource.class);
        
        //router.attach("/opentests/search", OpenTestSearchResource.class, Template.MODE_STARTS_WITH);
        
        router.attach("/opentests/P{open_uuid}", OpenTestResource.class);
        
        router.attach("/opentests/O{open_test_uuid}", OpenTestResource.class);
        
        router.attach("/{lang}/{open_test_uuid}/{size}.png", ImageExport.class);
        
        return router;
    }
    
}
