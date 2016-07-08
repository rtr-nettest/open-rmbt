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
package at.alladin.rmbt.android.util;

public interface Config
{
    
    /*********************
     * 
     * Settings
     * 
     */
    
    public static final String RMBT_CLIENT_TYPE = "MOBILE";
    public static final String RMBT_CLIENT_NAME = "RMBT";
    public static final String RMBT_SETTINGS_FILENAME = "RMBTClient";
    public static final String RMBT_DEV_UNLOCK_PACKAGE_NAME = "at.alladin.rmbt.android.devunlock";
    // public static final String RMBT_AGB_FILENAME = "RMBTAgbs";
    
    /*********************
     * 
     * Default Values
     * 
     *********************/
    
    public final static int MAX_LINE = 500;
    
    public final static int DISPLAY_UPDATE = 500;
    
    public final static int HISTORY_RESULTLIMIT_DEFAULT = 250;
    
    /*********************
     * 
     * Geo Location Settings
     * 
     *********************/
    
    /**
     * only accept locations not older than GEO_ACCEPT_TIME milliseconds
     * (default 15 min)
     */
    public static final int GEO_ACCEPT_TIME = 1000 * 60 * 15;
    
    public static final int GEO_MIN_TIME = 1000 * 60;
    
}
