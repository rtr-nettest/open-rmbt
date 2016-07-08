/*******************************************************************************
 * Copyright 2015, 2016 Thomas Schreiber
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
package at.alladin.rmbt.statisticServer.opendata;

import at.alladin.rmbt.shared.cache.CacheHelper;
import at.alladin.rmbt.statisticServer.ServerResource;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    
    private final int HISTOGRAMCLASSES = 12;
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
	 * @param whereClause
	 * @param searchValues
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
                JSONArray downArray = getJSONForHistogram(min, max,
                        (logarithmic) ? "speed_download_log" : "speed_download",
                        logarithmic, qp);

                ret.put("download_kbit", downArray);
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
                JSONArray upArray = getJSONForHistogram(min, max,
                        (logarithmic) ? "speed_upload_log" : "speed_upload",
                        logarithmic, qp);

                ret.put("upload_kbit", upArray);
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
                JSONArray pingArray = getJSONForHistogram(min, max, "(t.ping_median::float / 1000000)", false, qp);

                ret.put("ping_ms", pingArray);
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
     * Gets the JSON Array for a specific histogram
     * @param min lower bound of first class
     * @param max upper bound of last class
     * @param field numeric database-field that the histogram is based on 
     * @param isLogarithmic
     * @param whereClause
     * @param searchValues
     * @return
     * @throws JSONException 
     * @throws CacheException 
     */
    private JSONArray getJSONForHistogram(double min, double max, String field, boolean isLogarithmic, QueryParser qp) throws JSONException {

    	//Get min and max steps
    	double difference = max - min;
    	int digits = (int) Math.floor(Math.log10(difference));
    	
    	//get histogram classes
    	long upperBound = new BigDecimal(max).setScale(-digits, BigDecimal.ROUND_CEILING).longValue();
    	long lowerBound = new BigDecimal(min).setScale(-digits, BigDecimal.ROUND_FLOOR).longValue();
    	double step = ((double) (upperBound-lowerBound))/((double)HISTOGRAMCLASSES);
        
        
    	System.out.println("lower: " + lowerBound + ", upper: " + upperBound + ", digits: " + digits + ", diff: " + difference + ", step: " + step);
    	
    	//psql width_bucket: gets the histogram class in which a value belongs
		final String sql = 
				"select "
				+ " width_bucket(" + field + "," + lowerBound + "," + upperBound + "," + HISTOGRAMCLASSES + ") bucket, "
				+ " count(*) cnt " 
				+ " from test t "
				+ " LEFT JOIN network_type nt ON nt.uid=t.network_type"
				+ " LEFT JOIN device_map adm ON adm.codename=t.model"
				+ " LEFT JOIN test_server ts ON ts.uid=t.server_id"
				+ " LEFT JOIN provider prov ON provider_id = prov.uid "
				+ " LEFT JOIN provider mprov ON mobile_provider_id = mprov.uid"
				+ " where " + field + " > 0 " 
				+ " AND t.deleted = false"
				+ " AND status = 'FINISHED' " + qp.getWhereClause("AND") 
				+ " group by bucket " + "order by bucket asc;";
    	
    	
    	
    	JSONArray jArray = new JSONArray();
		try {
			PreparedStatement stmt = conn.prepareStatement(sql);
			qp.fillInWhereClause(stmt, 1);
			ResultSet rs = stmt.executeQuery();
			
			JSONObject jBucket = null;
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
						jBucket = new JSONObject();
						double tLowerBound = lowerBound + step * (prevBucket - 1);
						if (isLogarithmic)
							tLowerBound = Math.pow(10, tLowerBound*4)*10;
						double tUpperBound = lowerBound + (step * prevBucket);
						if (isLogarithmic)
							tUpperBound = Math.pow(10, tUpperBound*4)*10;
						jBucket.put("lower_bound", tLowerBound);
						jBucket.put("upper_bound", tUpperBound);
						jBucket.put("results", 0);
						jArray.put(jBucket);
					}
				}
				prevBucket = bucket;
				prevCnt = cnt;
				jBucket = new JSONObject();
				if (bucket == 0) {
					jBucket.put("lower_bound", JSONObject.NULL);
				} else {
					//2 digits accuracy for small differences
					if (step < 1 && !isLogarithmic) 
						jBucket.put("lower_bound", ((double) Math.round(current_lower_bound*100))/(double) 100);
					else
						jBucket.put("lower_bound", Math.round(current_lower_bound));
				}

				if (bucket == HISTOGRAMCLASSES + 1) {
					jBucket.put("upper_bound", JSONObject.NULL);
				} else {
					if (step < 1 && !isLogarithmic)
						jBucket.put("upper_bound", ((double) Math.round(current_upper_bound*100))/(double) 100);
					else
						jBucket.put("upper_bound", Math.round(current_upper_bound));
				}
				jBucket.put("results", cnt);
				
				jArray.put(jBucket);
			}
			
			//problem: not enough buckets
			//solution: respond with classes with "0" elements
			if (jArray.length() < HISTOGRAMCLASSES) {
				int diff = HISTOGRAMCLASSES - jArray.length();
				int bucket = jArray.length();
				for (int i=0;i<diff;i++) {
					jBucket = new JSONObject();
					bucket++;
					double tLowerBound = lowerBound + step * (bucket - 1);
					if (isLogarithmic)
						tLowerBound = Math.pow(10, tLowerBound*4)*10;
					double tUpperBound = lowerBound + (step * bucket);
					if (isLogarithmic)
						tUpperBound = Math.pow(10, tUpperBound*4)*10;
					jBucket.put("lower_bound", tLowerBound);
					jBucket.put("upper_bound", tUpperBound);
					jBucket.put("results", 0);
					jArray.put(jBucket);
				}
			}
			
			rs.close();
			stmt.close();
		
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		
    	return jArray;
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
