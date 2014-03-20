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
package at.alladin.rmbt.client.ndt;

import net.measurementlab.ndt.UiServices;

public class UiServicesAdapter implements UiServices
{
    public Double s2cspd;
    public Double c2sspd;
    public Double avgrtt;
    public final StringBuffer sbMain = new StringBuffer();
    public final StringBuffer sbStat = new StringBuffer();
    public final StringBuffer sbDiag = new StringBuffer();
    
    private long startTimeNs;
    private long stopTimeNs;
    
    public boolean arePrimaryResultsSet()
    {
        return s2cspd != null && c2sspd != null;
    }
    
    public void appendString(final String str, final int viewId)
    {
        if (str == null)
            return;
        switch (viewId)
        {
        case MAIN_VIEW:
            sbMain.append(str);
            break;
        case STAT_VIEW:
            sbStat.append(str);
            break;
        case DIAG_VIEW:
            sbDiag.append(str);
            break;
        }
    }
    
    public void incrementProgress()
    {
    }
    
    public void onBeginTest()
    {
    	this.startTimeNs = System.nanoTime();
    	System.out.println("NDT START:" + this.startTimeNs);
    }
    
    public void onEndTest()
    {
    	this.stopTimeNs = System.nanoTime();
    	System.out.println("NDT END:" + this.stopTimeNs);
    }
    
    public void onFailure(final String errorMessage)
    {
    }
    
    public void onPacketQueuingDetected()
    {
    }
    
    public void onLoginSent()
    {
    }
    
    public void logError(final String str)
    {
    }
    
    public void updateStatus(final String status)
    {
    }
    
    public void updateStatusPanel(final String status)
    {
    }
    
    public boolean wantToStop()
    {
        return false;
    }
    
    public String getClientApp()
    {
        return "Open-RMBT";
    }
    
    public void setVariable(final String name, final int value)
    {
    }
    
    public void setVariable(final String name, final double value)
    {
        if (name == null)
            return;
        if ("pub_avgrtt".equals(name))
            avgrtt = value;
        else if ("pub_c2sspd".equals(name))
            c2sspd = value;
        else if ("pub_s2cspd".equals(name))
            s2cspd = value;
    }
    
    public void setVariable(final String name, final Object value)
    {
    }
    
    public long getStartTimeNs() {
    	return startTimeNs;
    }
    
    public long getStopTimeNs() {
    	return stopTimeNs;
    }
}
