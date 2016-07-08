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
package at.alladin.rmbt.shared;

public final class Classification
{
    public static final int[] THRESHOLD_UPLOAD = { 10000, 1000, 500 }; // 10Mbit/s, 1Mbit/s, 500kbit/s
    public static final String[] THRESHOLD_UPLOAD_CAPTIONS = { "1", "0.5" };
    
    public static final int[] THRESHOLD_DOWNLOAD = { 30000, 2000, 1000 }; // 30Mbit/s, 2Mbit/s, 1Mbit/s
    public static final String[] THRESHOLD_DOWNLOAD_CAPTIONS = { "2", "1" };
    
    public static final int[] THRESHOLD_PING = { 10000000, 25000000, 75000000 }; // 10ms, 25ms, 75ms
    public static final String[] THRESHOLD_PING_CAPTIONS = { "25", "75" };
    
    // RSSI limits used for 2G,3G (and 4G when RSSI is used)
    // only odd values are reported by 2G/3G 
    public static final int[] THRESHOLD_SIGNAL_MOBILE = { -75, -85, -101 }; // -75 is still ultra-green, -85 is still green, -101 is still yellow
    public static final String[] THRESHOLD_SIGNAL_MOBILE_CAPTIONS = { "-85", "-101" };
    
    // RSRP limit used for 4G
    public static final int[] THRESHOLD_SIGNAL_RSRP = { -85, -95, -111 };
    public static final String[] THRESHOLD_SIGNAL_RSRP_CAPTIONS = { "-95", "-111" };

    // RSSI limits used for Wifi
    public static final int[] THRESHOLD_SIGNAL_WIFI = { -51, -61, -76 };
    public static final String[] THRESHOLD_SIGNAL_WIFI_CAPTIONS = { "-61", "-76" };
    

    public static int classify(final int[] threshold, final long value, int classificationItems)
    {
    	int init = threshold.length-(classificationItems-1);
        final boolean inverse = threshold[0] < threshold[1];
        
        if (!inverse)
        {
        	/*
        	 * 1 = RED
        	 * classificationItems = ULTRA GREEN
        	 */
        	
        	int c = 0;
        	for (int i = init; i < threshold.length; i++, c++) {
        		if (value >= threshold[i]) {
        			return classificationItems-c;
        		}
        	}
        	
        	return 1;
        }
        

        int c = 0;
    	for (int i = init; i < threshold.length; i++, c++) {
    		if (value <= threshold[i]) {
    			return classificationItems-c;
    		}
    	}        	

    	return 1;
    }
}
