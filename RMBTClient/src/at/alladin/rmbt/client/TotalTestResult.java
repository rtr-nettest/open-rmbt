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
package at.alladin.rmbt.client;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import at.alladin.rmbt.client.helper.TestStatus;
import at.alladin.rmbt.client.v2.task.service.TestMeasurement;
import at.alladin.rmbt.client.v2.task.service.TestMeasurement.TrafficDirection;

public class TotalTestResult extends TestResult
{
    public double speed_upload;
    public double speed_download;
    public long bytes_download;
    public long nsec_download;
    public long bytes_upload;
    public long nsec_upload;
    public long totalDownBytes;
    public long totalUpBytes;
    
    private Map<TestStatus, TestMeasurement> measurementMap;
    private TestMeasurement totalTrafficMeasurement;

    public void setMeasurementMap(Map<TestStatus, TestMeasurement> trafficMap) {
    	this.measurementMap = trafficMap;
    }
    
    public Map<TestStatus, TestMeasurement> getMeasurementMap() {
    	return measurementMap;
    }
    
    public void setTotalTrafficMeasurement(TestMeasurement measurement) {
    	this.totalTrafficMeasurement = measurement;
    }
    
    public long getTotalTrafficMeasurement(TrafficDirection dir) {
    	if (totalTrafficMeasurement != null) {
			switch (dir) {
			case RX:
				return totalTrafficMeasurement.getRxBytes();
			case TX:
				return totalTrafficMeasurement.getTxBytes();
			case TOTAL:
				return totalTrafficMeasurement.getTxBytes() + totalTrafficMeasurement.getRxBytes();
			}
    	}
    	
    	return 0;
    }
    
    public long getTrafficByTestPart(TestStatus testStatusPart, TrafficDirection dir) {
    	if (measurementMap != null) {
    		TestMeasurement measurement = measurementMap.get(testStatusPart);
    		if (measurement != null) {
    			switch (dir) {
    			case RX:
    				return measurement.getRxBytes();
    			case TX:
    				return measurement.getTxBytes();
    			case TOTAL:
    				return measurement.getTxBytes() + measurement.getRxBytes();
    			}
    		}
    	}
    	
    	return 0;
    }
    
    public TestMeasurement getTestMeasurementByTestPart(TestStatus testStatusPart) {
    	if (measurementMap != null) {
    		return measurementMap.get(testStatusPart);
    	}
    	
    	return null;
    }
    
    public double getDownloadSpeedBitPerSec()
    {
        return getSpeedBitPerSec(bytes_download, nsec_download);
    }
    
    public double getUploadSpeedBitPerSec()
    {
        return getSpeedBitPerSec(bytes_upload, nsec_upload);
    }
    
    public void calculateDownload(final long[][] bytes, final long[][] nsecs)
    {
        calculate(bytes, nsecs, false);
    }
    
    public void calculateUpload(final long[][] bytes, final long[][] nsecs)
    {
        calculate(bytes, nsecs, true);
    }
    
    public static TotalTestResult calculateAndGet(final Map<Integer, List<SpeedItem>> speedMap) {
    	final int threads = speedMap.keySet().size();
    	
    	long[][] allBytes = null;
    	long[][] allNsecs = null;
    	
    	int threadCounter = 0;
    	for (Entry<Integer, List<SpeedItem>> e : speedMap.entrySet()) {
    		final List<SpeedItem> speedList = e.getValue();
    		
    		if (allBytes == null) {
    			allBytes = new long[threads][speedList.size()];
    			allNsecs = new long[threads][speedList.size()];
    		}
    		
    		for (int i = 0; i < speedList.size(); i++) {
    			allBytes[threadCounter][i] = speedList.get(i).bytes;
    			allNsecs[threadCounter][i] = speedList.get(i).time;
    		}
    		
    		threadCounter++;
    	}
    	
    	return TotalTestResult.calculateAndGet(allBytes, allNsecs, false);
    }
    
    public static TotalTestResult calculateAndGet(final long[][] allBytes, final long[][] allNsecs, final boolean upload) {
    	final TotalTestResult totalResult = new TotalTestResult();
    	totalResult.calculate(allBytes, allNsecs, upload);
    	return totalResult;
    }
    
    private void calculate(final long[][] allBytes, final long[][] allNsecs, final boolean upload)
    {
        if (allBytes.length != allNsecs.length)
            throw new IllegalArgumentException();
        
        final int numThreads = allBytes.length;
        
        long targetTime = Long.MAX_VALUE;
        for (int i = 0; i < numThreads; i++)
        {
            final long[] nsecs = allNsecs[i];
            if (nsecs != null && nsecs.length > 0)
                if (nsecs[nsecs.length - 1] < targetTime)
                    targetTime = nsecs[nsecs.length - 1];
        }
        
        long totalBytes = 0;
        
        for (int i = 0; i < numThreads; i++)
        {
            final long[] bytes = allBytes[i];
            final long[] nsecs = allNsecs[i];
            
            if (bytes != null && nsecs != null && bytes.length > 0)
            {
                if (bytes.length != nsecs.length)
                    throw new IllegalArgumentException();
                
                int targetIdx = bytes.length;
                for (int j = 0; j < bytes.length; j++)
                    if (nsecs[j] > targetTime)
                    {
                        targetIdx = j;
                        break;
                    }
                
                final long calcBytes;
                if (targetIdx == bytes.length)
                    // nsec[max] == targetTime
                    calcBytes = bytes[bytes.length - 1];
                else
                {
                    final long bytes1 = targetIdx == 0 ? 0 : bytes[targetIdx - 1];
                    final long bytes2 = bytes[targetIdx];
                    final long bytesDiff = bytes2 - bytes1;
                    
                    final long nsec1 = targetIdx == 0 ? 0 : nsecs[targetIdx - 1];
                    final long nsec2 = nsecs[targetIdx];
                    final long nsecDiff = nsec2 - nsec1;
                    
                    final long nsecCompensation = targetTime - nsec1;
                    final double factor = (double) nsecCompensation / (double) nsecDiff;
                    
                    long compensation = Math.round(bytesDiff * factor);
                    if (compensation < 0)
                        compensation = 0;
                    calcBytes = bytes1 + compensation;
                }
                totalBytes += calcBytes;
            }
        }
        
        if (upload)
        {
            bytes_upload = totalBytes;
            nsec_upload = targetTime;
            speed_upload = getUploadSpeedBitPerSec() / 1e3;
        }
        else
        {
            bytes_download = totalBytes;
            nsec_download = targetTime;
            speed_download = getDownloadSpeedBitPerSec() / 1e3;
        }
    }
}
