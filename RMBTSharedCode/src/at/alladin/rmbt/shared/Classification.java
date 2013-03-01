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
package at.alladin.rmbt.shared;

public final class Classification
{
    public static final int[] THRESHOLD_UPLOAD = { 1000, 500 };
    public static final String[] THRESHOLD_UPLOAD_CAPTIONS = { "1", "0.5" };
    
    public static final int[] THRESHOLD_DOWNLOAD = { 2000, 1000 };
    public static final String[] THRESHOLD_DOWNLOAD_CAPTIONS = { "2", "1" };
    
    public static final int[] THRESHOLD_PING = { 25000000, 75000000 };
    public static final String[] THRESHOLD_PING_CAPTIONS = { "25", "75" };
    
    public static final int[] THRESHOLD_SIGNAL_MOBILE = { -86, -101 };
    public static final String[] THRESHOLD_SIGNAL_MOBILE_CAPTIONS = { "-86", "-101" };
    
    public static final int[] THRESHOLD_SIGNAL_WIFI = { -61, -76 };
    public static final String[] THRESHOLD_SIGNAL_WIFI_CAPTIONS = { "-61", "-76" };
    
    public static int classify(final int[] threshold, final long value)
    {
        final boolean inverse = threshold[0] < threshold[1];
        
        if (!inverse)
        {
            if (value >= threshold[0])
                return 3;
            else if (value >= threshold[1])
                return 2;
            else
                return 1;
        }
        else if (value <= threshold[0])
            return 3;
        else if (value <= threshold[1])
            return 2;
        else
            return 1;
    }
}
