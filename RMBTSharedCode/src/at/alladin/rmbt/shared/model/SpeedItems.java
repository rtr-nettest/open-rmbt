/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
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
 *******************************************************************************/
package at.alladin.rmbt.shared.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.alladin.rmbt.shared.smoothing.Smoothable;

import com.google.gson.annotations.SerializedName;

public class SpeedItems
{
	
    public static class SpeedItem implements Comparable<SpeedItem>, Smoothable
    {
        @SerializedName("t")
        protected long time;
        @SerializedName("b")
        protected long bytes;
        
        public SpeedItem()
        {
        }
        
        public SpeedItem(long time, long bytes)
        {
            this.time = time;
            this.bytes = bytes;
        }
        
        public long getTime()
        {
            return time;
        }
        public void setTime(long time)
        {
            this.time = time;
        }
        public long getBytes()
        {
            return bytes;
        }
        public void setBytes(long bytes)
        {
            this.bytes = bytes;
        }

		@Override
		public double getXValue() {
			return getTime();
		}
		
		@Override
		public double getYValue() {
			return getBytes();
		}

        @Override
        public int compareTo(SpeedItem o)
        {
            if (o == null)
                return -1;
            return Long.compare(time, o.time);
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("SpeedItem [time=").append(time).append(", bytes=").append(bytes).append("]");
            return builder.toString();
        }
    }
    
    protected Map<Integer, List<SpeedItem>> download;
    protected Map<Integer, List<SpeedItem>> upload;
    
    protected static Map<Integer, List<SpeedItem>> addSpeedItem(SpeedItem si, int thread, Map<Integer, List<SpeedItem>> target)
    {
        if (target == null)
            target = new HashMap<>();
        List<SpeedItem> speedThread = target.get(thread);
        if (speedThread == null)
        {
            speedThread = new ArrayList<>();
            target.put(thread, speedThread);
        }
        speedThread.add(si);
        return target;
    }
    
    public void addSpeedItemDownload(SpeedItem si, int thread)
    {
        download = addSpeedItem(si, thread, download);
    }
    
    public void addSpeedItemUpload(SpeedItem si, int thread)
    {
        upload = addSpeedItem(si, thread, upload);
    }

    public Map<Integer, List<SpeedItem>> getDownload()
    {
        return download;
    }

    public Map<Integer, List<SpeedItem>> getUpload()
    {
        return upload;
    }
    
    protected static void sortItems(Map<Integer, List<SpeedItem>> items)
    {
        if (items == null)
            return;
        for (List<SpeedItem> list : items.values())
            Collections.sort(list);
    }
    
    public void sortItems()
    {
        sortItems(download);
        sortItems(upload);
    }
    
    public List<SpeedItem> getAccumulatedSpeedItemsDownload()
    {
        return getAccumulatedSpeedItems(download);
    }
    
    public List<SpeedItem> getAccumulatedSpeedItemsUpload()
    {
        return getAccumulatedSpeedItems(upload);
    }
    
    public Map<String, Map<Integer, List<SpeedItem>>> getRawJSONData() {
        HashMap<String, Map<Integer, List<SpeedItem>>> ret = new HashMap<>();
        sortItems();
        
        //download
        ret.put("download", download);
        
        //upload        
        ret.put("upload", upload);
        
        return ret;
    }
    

    
    protected static List<SpeedItem> getAccumulatedSpeedItems(Map<Integer, List<SpeedItem>> items)
    {
        sortItems(items);
        
        if (items == null) {
        	return new ArrayList<>();
        }
        
        int numItems = 0;
        for (List<SpeedItem> speedItems : items.values())
            numItems += speedItems.size();
        
        final long times[] = new long[numItems];
        
        int i = 0;
        for (List<SpeedItem> speedItems : items.values())
        {
            for (SpeedItem item : speedItems)
                times[i++] = item.time;
            if (i == times.length)
                break;
        }
        
        numItems = i;
        Arrays.sort(times);
        
        final long bytes[] = new long[times.length];
        for (Map.Entry<Integer, List<SpeedItem>> entry : items.entrySet())
        {
            i = 0;
            long lastTime = 0;
            long lastBytes = 0;
            for (SpeedItem si : entry.getValue())
            {
                while (si.time > times[i]) // average times we don't have
                {
                    bytes[i] += Math.round((double)((times[i] - lastTime) * si.bytes + (si.time - times[i]) * lastBytes) / (si.time - lastTime));
                    i++;
                }
                if (si.time == times[i])
                    bytes[i++] += si.bytes;
                lastTime = si.time;
                lastBytes = si.bytes;
            }
            while (i < numItems)
                bytes[i++] += lastBytes; // assume no transfer after last entry; might not be the case, but assuming otherwise could be worse 
        }
        
        final List<SpeedItem> result = new ArrayList<>();
        for (int j = 0; j < numItems; j++)
            result.add(new SpeedItem(times[j], bytes[j]));
        
        return result;
    }
}
