/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
 * Copyright 2013-2015 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
package at.rtr.rmbt.client.helper;

/**
 * The system defaults.
 * 
 * 
 * 
 */
public abstract interface Config
{
    
    /*********************
     * 
     * Default Preferences
     * 
     *********************/
    
    public static final String RMBT_SERVER_NAME = "RMBT";
    public static final String RMBT_CLIENT_NAME = "RMBT";
    public static final String RMBT_VERSION_NUMBER = "0.3";
    public static final String RMBT_VERSION_EXPRESSION = RMBT_CLIENT_NAME + "v([0-9.]+)";
    public static final String RMBT_LATEST_SERVER = "1.2.0";
    
    public static final int RMBT_CONTROL_PORT = 443;
    public static final boolean RMBT_CONTROL_SSL = true;
    public static final boolean RMBT_QOS_SSL = true;
    public static final String RMBT_CONTROL_PATH = "/RMBTControlServer";
    public static final String RMBT_TEST_SETTINGS_REQUEST = "/testRequest";
    public static final String RMBT_QOS_TEST_REQUEST = "/qosTestRequest";
    public static final String RMBT_CONTROL_NDT_RESULT_URL = "ndtResult";
    public static final String RMBT_NEWS_HOST_URL = "/news";
    public static final String RMBT_HISTORY_HOST_URL = "/history";
    public static final String RMBT_TESTRESULT_HOST_URL = "/testresult";
    public static final String RMBT_TESTRESULT_DETAIL_HOST_URL = "/testresultdetail";
    public static final String RMBT_TESTRESULT_QOS_HOST_URL = "/qosTestResult";
    public static final String RMBT_TESTRESULT_OPENDATA_HOST_URL = "/opentests/";
    public static final String RMBT_SYNC_HOST_URL = "/sync";
    public static final String RMBT_SETTINGS_HOST_URL = "/settings";
    public static final String RMBT_UPDATE_RESULT_URL = "/resultUpdate";

    public static final String RMBT_ENCRYPTION_STRING = "TLS";

    public static final String MLAB_NS = "http://mlab-ns.appspot.com/ndt?format=json";
    public static final String NDT_FALLBACK_HOST = "ndt.iupui.donar.measurement-lab.org";

    public static final String SERVER_TYPE_QOS = "QoS";
    public static final String SERVER_TYPE_RMBT = "RMBT";
    public static final String SERVER_TYPE_RMBT_HTTP = "RMBThttp";

}
