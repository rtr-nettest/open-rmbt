/*******************************************************************************
 * Copyright 2015, 2016 Thomas Schreiber
 * Copyright 2015 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
package at.rtr.rmbt.statisticServer.opendata;

import at.rtr.rmbt.shared.cache.CacheHelper;
import at.rtr.rmbt.statisticServer.ServerResource;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.resource.Get;

/**
 *
 * @author Thomas
 */
public class HistogramResource extends ServerResource{
    private static final int CACHE_EXP = 3600;
    private final CacheHelper cache = CacheHelper.getInstance();
    
    private final HistogramInfo histogramInfo = new HistogramInfo();
    
    private final int HISTOGRAMCLASSESLOG = 12;
    private final int HISTOGRAMCLASSES = 10;
    private final int FINEMULTIPLIER = 10;
    private final int HISTOGRAMDOWNLOADDEFAULTMAX = 100000;
    private final int HISTOGRAMDOWNLOADDEFAULTMIN = 0;
    private final int HISTOGRAMUPLOADDEFAULTMAX = 100000;
    private final int HISTOGRAMUPLOADDEFAULTMIN = 0;
    private final int HISTOGRAMPINGDEFAULTMAX = 300; //milliseconds
    private final int HISTOGRAMPINGDEFAULTMIN = 0;
    
    
    @Get("json")
    public String request(final String entity) {
        addAllowOrigin();
        final Form getParameters = getRequest().getResourceRef().getQueryAsForm();
        final QueryParser qp = new QueryParser();
        
        //set transformator for time to allow for broader caching
        qp.registerSingleParameterTransformator("time", new QueryParser.SingleParameterTransformator() {
            private final static int ONE_HOUR = 60*60*1000;
            
            @Override
            public void transform(QueryParser.SingleParameter param) {
                //round to 1h
                long timestamp = Long.parseLong(param.getValue());
                timestamp = timestamp - (timestamp % ONE_HOUR);
                param.setValue(Long.toString(timestamp));
            }
        });
        
        //also allow doing histogram just for single fields
        List<String> measurements = new LinkedList<>();
        qp.getAllowedFields().put("measurement", QueryParser.FieldType.IGNORE);
        qp.getAllowedFields().put("measurement[]", QueryParser.FieldType.IGNORE);
        if (getParameters.getNames().contains("measurement") || 
                getParameters.getNames().contains("measurement[]")) { 
            String[] measurementArray = getParameters.getValuesArray("measurement", true, null);
            if (measurementArray.length == 0) {
                measurementArray = getParameters.getValuesArray("measurement[]", true, null);
            }
            
            for (String singleMeasurement : measurementArray) {
                if (singleMeasurement.matches("download|upload|ping")) {
                    measurements.add(singleMeasurement);
                }
            }
        } else {
            measurements.addAll(Arrays.asList(new String[] {"download","upload","ping"}));
        }

        
        qp.parseQuery(getParameters);
        
        //try cache first
        String cacheString = (String) cache.get("opentest-histogram-" + Objects.hash(measurements) + "-" + qp.hashCode());
        if (cacheString != null) {
            //System.out.println("cache hit for histogram");
            return cacheString;
        }
        //System.out.println("No hit for: " + "opentest-histogram-" + qp.hashCode());
        
        
        this.adjustHistogramInfo(qp);
        
        String json = getHistogram(qp, measurements);
        
        //put in cache
        cache.set("opentest-histogram-" + Objects.hash(measurements) + "-" + qp.hashCode(), CACHE_EXP, json);
        
        return json;
    }
    
    private void adjustHistogramInfo(QueryParser qp) {
        //adjust HistogramInfo based on given parameters

        //download
        if (qp.getWhereParams().containsKey("download_kbit")) {
            for (QueryParser.SingleParameter param : qp.getWhereParams().get("download_kbit")) {
                switch (param.getComperator()) {
                    case ">=":
                        this.histogramInfo.min_download = Long.parseLong(param.getValue());
                        break;
                    case "<=":
                        this.histogramInfo.max_download = Long.parseLong(param.getValue());
                        break;
                }
            }
        }

        //upload
        if (qp.getWhereParams().containsKey("upload_kbit")) {
            for (QueryParser.SingleParameter param : qp.getWhereParams().get("upload_kbit")) {
                switch (param.getComperator()) {
                    case ">=":
                        this.histogramInfo.min_upload = Long.parseLong(param.getValue());
                        break;
                    case "<=":
                        this.histogramInfo.max_upload = Long.parseLong(param.getValue());
                        break;
                }
            }
        }

        //ping
        if (qp.getWhereParams().containsKey("ping_ms")) {
            for (QueryParser.SingleParameter param : qp.getWhereParams().get("ping_ms")) {
                switch (param.getComperator()) {
                    case ">=":
                        this.histogramInfo.min_ping = Double.parseDouble(param.getValue());
                        break;
                    case "<=":
                        this.histogramInfo.max_ping = Double.parseDouble(param.getValue());
                        break;
                }
            }
        }
    }
    
    /**
	 * Gets the JSON-Response for the histograms
	 * @param qp QueryParser for the current query
     * @param measurements The fields for which to get the histogram data
	 * @return Json as String
	 */
    private String getHistogram(QueryParser qp, List<String> measurements) {
        //String whereClause = qp.getWhereClause();
        
    	JSONObject ret = new JSONObject();
    	try {
    		/*if (searchValues.isEmpty()) {
    			//try getting from cache
    			String cacheString = (String) cache.get("opentest-histogram");
        		if (cacheString != null) {
        			System.out.println("cache hit for histogram");
        			return cacheString;
        		}
    		}*/
    		boolean logarithmic;
            double min, max;
            
	    	//Download
            if (measurements.contains("download")) {
                // logarithmic if without filters
                logarithmic = false;
                if (histogramInfo.max_download == Long.MIN_VALUE
                        && histogramInfo.min_download == Long.MIN_VALUE) {

                    histogramInfo.max_download = 1;
                    histogramInfo.min_download = 0;
                    logarithmic = true;
                }
                if (!logarithmic && histogramInfo.max_download == Long.MIN_VALUE) {
                    histogramInfo.max_download = HISTOGRAMDOWNLOADDEFAULTMAX;
                }
                if (!logarithmic && histogramInfo.min_download == Long.MIN_VALUE) {
                    histogramInfo.min_download = HISTOGRAMDOWNLOADDEFAULTMIN;
                }
                min = this.histogramInfo.min_download;
                max = this.histogramInfo.max_download;
                List<Bucket> downArrayfine = getJSONForHistogram(min, max,
                        (logarithmic) ? "speed_download_log" : "speed_download",
                        logarithmic, qp);
                List<Bucket> downArrayLowRes = getLowResBucketList(downArrayfine);

                ret.put("download_kbit", bucketListToJSONArray(downArrayLowRes));
                ret.put("download_kbit_fine", bucketListToJSONArray(downArrayfine));
            }

			// Upload
            if (measurements.contains("upload")) {
                logarithmic = false;
                if (histogramInfo.max_upload == Long.MIN_VALUE
                        && histogramInfo.min_upload == Long.MIN_VALUE) {
                    histogramInfo.max_upload = 1;
                    histogramInfo.min_upload = 0;
                    logarithmic = true;
                }
                if (!logarithmic && histogramInfo.max_upload == Long.MIN_VALUE) {
                    histogramInfo.max_upload = HISTOGRAMUPLOADDEFAULTMAX;
                }
                if (!logarithmic && histogramInfo.min_upload == Long.MIN_VALUE) {
                    histogramInfo.min_upload = HISTOGRAMUPLOADDEFAULTMIN;
                }
                min = this.histogramInfo.min_upload;
                max = this.histogramInfo.max_upload;
                List<Bucket> uploadBucketsfine = getJSONForHistogram(min, max,
                        (logarithmic) ? "speed_upload_log" : "speed_upload",
                        logarithmic, qp);

                List<Bucket> uploadBucketsLowRes = getLowResBucketList(uploadBucketsfine);

                ret.put("upload_kbit", bucketListToJSONArray(uploadBucketsLowRes));
                ret.put("upload_kbit_fine", bucketListToJSONArray(uploadBucketsfine));
            }
			
			//Ping
            if (measurements.contains("ping")) {
                if (histogramInfo.max_ping == Long.MIN_VALUE) {
                    histogramInfo.max_ping = HISTOGRAMPINGDEFAULTMAX;
                }
                if (histogramInfo.min_ping == Long.MIN_VALUE) {
                    histogramInfo.min_ping = HISTOGRAMPINGDEFAULTMIN;
                }
                min = this.histogramInfo.min_ping;
                max = this.histogramInfo.max_ping;
                List<Bucket> pingArrayH = getJSONForHistogram(min, max, "(t.ping_median::float / 1000000)", false, qp);
                List<Bucket> pingArrayL = getLowResBucketList(pingArrayH);

                ret.put("ping_ms", bucketListToJSONArray(pingArrayL));
                ret.put("ping_ms_fine", bucketListToJSONArray(pingArrayH));
            }
		 	
			//if (searchValues.isEmpty()) {
				//if it was the default -> save it to the cache for later
			//	cache.set("opentest-histogram", CACHE_EXP, ret.toString());
			//}
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    	return ret.toString();
    }

    /**
     * Return a second set of buckets, reduced by FINEMULTIPLIER
     * @param fineList
     * @return
     */
    private List<Bucket> getLowResBucketList(List<Bucket> fineList) {
        List<Bucket> ret = new ArrayList<>();
        Bucket currentBucket = new Bucket();
        for (int i=0;i<fineList.size();i++) {
            if (i % FINEMULTIPLIER == 0) {
                currentBucket = new Bucket();
                ret.add(currentBucket);
                currentBucket.lowerBound = fineList.get(i).lowerBound;
            }
            currentBucket.results += fineList.get(i).results;
            if (i % FINEMULTIPLIER == (FINEMULTIPLIER - 1)) {
                currentBucket.upperBound = fineList.get(i).upperBound;
                currentBucket = null;
            }
        }

        return ret;
    }

    private JSONArray bucketListToJSONArray(List<Bucket> list) {
        JSONArray ret = new JSONArray();
        for (Bucket b : list) {
            ret.put(b.toJson());
        }
        return ret;
    }
    
    /**
     * Gets the JSON Array for a specific histogram
     * @param min lower bound of first class
     * @param max upper bound of last class
     * @param field numeric database-field that the histogram is based on 
     * @param isLogarithmic
     * @param qp QueryParser object for the current selection
     * @return
     * @throws JSONException
     */
    private List<Bucket> getJSONForHistogram(double min, double max, String field, boolean isLogarithmic, QueryParser qp) throws JSONException {

        int histogramClasses = (isLogarithmic) ? HISTOGRAMCLASSESLOG : HISTOGRAMCLASSES;
        histogramClasses *= FINEMULTIPLIER;

    	//Get min and max steps
    	double difference = max - min;
    	int digits = (int) Math.floor(Math.log10(difference));
        int roundTo = Math.max(0,(int) -Math.floor(Math.log10(difference/histogramClasses)));
    	
    	//get histogram classes
        //round everything to make for nicer bucket-widths with 10 buckets
        //e.g. 1,2 to 24 --> diff = 22,8 = 2 digits -> 0 to 30; each resulting bucket 3
    	long upperBound = new BigDecimal(max).setScale(-digits, BigDecimal.ROUND_CEILING).longValue();
    	long lowerBound = new BigDecimal(min).setScale(-digits, BigDecimal.ROUND_FLOOR).longValue();
    	double step = ((double) (upperBound-lowerBound))/((double)histogramClasses);
        
        
    	System.out.println("lower: " + lowerBound + ", upper: " + upperBound + ", digits: " + digits + ", diff: " + difference + ", step: " + step);
    	
    	//psql width_bucket: gets the histogram class in which a value belongs
		final String sql = 
				"select "
				+ " width_bucket(" + field + "," + lowerBound + "," + upperBound + "," + histogramClasses + ") bucket, "
				+ " count(*) cnt "
				+ " from test t "
				+ qp.getJoins()
				+ " where " + field + " > 0 " 
				+ " AND t.deleted = false"
				+ " AND status = 'FINISHED' " + qp.getWhereClause("AND") 
				+ " group by bucket " + "order by bucket asc;";
    	
    	

        List<Bucket> buckets = new ArrayList<>();
		try {
			PreparedStatement stmt = conn.prepareStatement(sql);
			qp.fillInWhereClause(stmt, 1);
			ResultSet rs = stmt.executeQuery();

            Bucket bucketObj;
			long prevCnt = 0;
			int prevBucket = 0;
			while(rs.next()) {				
				int bucket = rs.getInt("bucket");
				long cnt = rs.getLong("cnt");	
				
				double current_lower_bound = lowerBound + step * (bucket - 1);
				//logarithmic -> times 10 for kbit
				if (isLogarithmic)
					current_lower_bound = Math.pow(10, current_lower_bound*4)*10;
				double current_upper_bound = lowerBound + (step * bucket);
				if (isLogarithmic)
					current_upper_bound = Math.pow(10, current_upper_bound*4)*10;
				
				if (bucket-prevBucket > 1) {
					//problem: bucket without values
					//solution: respond with classes with "0" elements in them
					int diff = bucket-prevBucket;
					for (int i=1;i<diff;i++) {
						prevBucket++;
						//jBucket = new JSONObject();
                        bucketObj = new Bucket();
						double tLowerBound = lowerBound + step * (prevBucket - 1);
						if (isLogarithmic)
							tLowerBound = Math.pow(10, tLowerBound*4)*10;
						double tUpperBound = lowerBound + (step * prevBucket);
						if (isLogarithmic)
							tUpperBound = Math.pow(10, tUpperBound*4)*10;

                        bucketObj.lowerBound = BigDecimal.valueOf(tLowerBound).setScale(roundTo, BigDecimal.ROUND_HALF_UP).doubleValue();;;
                        bucketObj.upperBound = BigDecimal.valueOf(tUpperBound).setScale(roundTo, BigDecimal.ROUND_HALF_UP).doubleValue();;
                        bucketObj.results = 0;
                        buckets.add(bucketObj);
					}
				}
				prevBucket = bucket;
				prevCnt = cnt;

                bucketObj = new Bucket();
				if (bucket == 0) {
                    bucketObj.lowerBound = null;
				} else {
					//2 digits accuracy for small differences
                    bucketObj.lowerBound = BigDecimal.valueOf(current_lower_bound).setScale(roundTo, BigDecimal.ROUND_HALF_UP).doubleValue();;
				}

				if (bucket == histogramClasses + 1) {
					//jBucket.put("upperBound", JSONObject.NULL);
                    bucketObj.upperBound = null;
                } else {
                    bucketObj.upperBound = BigDecimal.valueOf(current_upper_bound).setScale(roundTo, BigDecimal.ROUND_HALF_UP).doubleValue();
				}
				//jBucket.put("results", cnt);
                bucketObj.results = cnt;
				
				//jArray.put(jBucket);
                buckets.add(bucketObj);
			}
			
			//problem: not enough buckets
			//solution: respond with classes with "0" elements
			if (buckets.size() < histogramClasses) {
				int diff = histogramClasses - buckets.size();
				int bucket = buckets.size();
				for (int i=0;i<diff;i++) {
					bucketObj = new Bucket();
					bucket++;
					double tLowerBound = lowerBound + step * (bucket - 1);
					if (isLogarithmic)
						tLowerBound = Math.pow(10, tLowerBound*4)*10;
					double tUpperBound = lowerBound + (step * bucket);
					if (isLogarithmic)
						tUpperBound = Math.pow(10, tUpperBound*4)*10;
                    bucketObj.lowerBound = tLowerBound;
                    bucketObj.upperBound = tUpperBound;
                    bucketObj.results = 0;

					buckets.add(bucketObj);
				}
			}
			
			rs.close();
			stmt.close();
		
		} catch (SQLException e) {
            e.printStackTrace();
        }
    	
		
    	return buckets;
    }

    public class Bucket {
        private Double lowerBound;
        private Double upperBound;
        private long results = 0;


        public Double getLowerBound() {
            return lowerBound;
        }

        public Double getUpperBound() {
            return upperBound;
        }

        public long getResults() {
            return results;
        }

        public JSONObject toJson() {
            try {
                JSONObject ret = new JSONObject();
                if (lowerBound == null) {
                    ret.put("lower_bound", JSONObject.NULL);
                } else {
                    ret.put("lower_bound", lowerBound);
                }
                if (upperBound == null) {
                    ret.put("upper_bound", JSONObject.NULL);
                } else {
                    ret.put("upper_bound", upperBound);
                }
                ret.put("results", results);
                return ret;
            }
            catch (JSONException ex) {
                return null;
            }
        }
    }
    
    private class HistogramInfo {
		long max_download = Long.MIN_VALUE;
		long min_download = Long.MIN_VALUE;
		long max_upload = Long.MIN_VALUE;
		long min_upload = Long.MIN_VALUE;
		double max_ping = Long.MIN_VALUE;
		double min_ping = Long.MIN_VALUE;
	}
}
