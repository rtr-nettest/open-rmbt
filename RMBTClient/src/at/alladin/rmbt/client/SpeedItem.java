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
package at.alladin.rmbt.client;

public class SpeedItem
{
    
    public final long sumAllTrans;
    public final long sumDiffTrans;
    public final long maxAllTime;
    public final long maxDiffTime;
    public final long tstamp;
    
    public SpeedItem(final long sumAllTrans, final long sumDiffTrans, final long maxAllTime, final long maxDiffTime,
            final long tstamp)
    {
        
        this.sumAllTrans = sumAllTrans;
        this.sumDiffTrans = sumDiffTrans;
        this.maxAllTime = maxAllTime;
        this.maxDiffTime = maxDiffTime;
        this.tstamp = tstamp;
        
    }
}
