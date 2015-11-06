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
package at.alladin.rmbt.controlServer;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;


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
        
        router.attach("/version", VersionResource.class);
        
        // test request
        router.attach("/", RegistrationResource.class); // old URL, for backwards compatibility
        router.attach("/testRequest", RegistrationResource.class);
        
        // test result is submitted, will be called once only
        router.attach("/result", ResultResource.class);
        
        router.attach("/resultQoS", QualityOfServiceResultResource.class);
        
        // plz is submitted (optional additional resource for browser)
        router.attach("/resultUpdate", ResultUpdateResource.class);
        
        // ndt test results are submitted (optional, after /result)
        router.attach("/ndtResult", NdtResultResource.class);
        
        router.attach("/news", NewsResource.class);
        
        router.attach("/ip", IpResource.class);
        
        router.attach("/status", StatusResource.class);
        
        
        // send history list to client
        router.attach("/history", HistoryResource.class);
        
        // send brief summary of test results to client
        router.attach("/testresult", TestResultResource.class);
        
        // send detailed test results to client
        router.attach("/testresultdetail", TestResultDetailResource.class);
        
        // was just used for migration
//        router.attach("/migrateTestSpeed", MigrateTestSpeed.class);

        router.attach("/sync", SyncResource.class);
        
        router.attach("/settings", SettingsResource.class);
        // collection of UserAgent etc.for IE (via server)  
        router.attach("/requestDataCollector", RequestDataCollector.class);
        
        router.attach("/opentests/O{open_test_uuid}&sender={sender}", OpenTestResource.class);
        router.attach("/opentests/O{open_test_uuid}", OpenTestResource.class);

        router.attach("/v2/opentests/O{open_test_uuid}&sender={sender}", at.alladin.rmbt.controlServer.v2.OpenTestResource.class);
        router.attach("/v2/opentests/O{open_test_uuid}", at.alladin.rmbt.controlServer.v2.OpenTestResource.class);
        
        router.attach("/qos/O{open_test_uuid}", OpenTestQoSResource.class);
        router.attach("/qos/O{open_test_uuid}/{lang}", OpenTestQoSResource.class);

        router.attach("/qosTestRequest", QoSTestRequestResource.class);
        router.attach("/qosTestResult", QoSResultResource.class);
        
        // administrative resources (access restrictions might be applied to /admin/ 
        router.attach("/admin/qosObjectives", QualityOfServiceExportResource.class);
        router.attach("/admin/setImplausible", ImplausibilityHelperResource.class);

        /*
         * 
         * use for request time measurements:

	        TimerFilter filter = new TimerFilter();
	        filter.setNext(router);
	        
	        return filter;
        */
        
        return router;
    }
    
}
