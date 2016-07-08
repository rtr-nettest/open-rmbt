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
package at.alladin.rmbt.mapServer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import at.alladin.rmbt.shared.Classification;
import at.alladin.rmbt.util.capability.Capabilities;

import com.google.common.base.Strings;

final public class MapServerOptions
{
    
    // 10^(a×4)×10
    // log(a÷10)÷4
    
    // 10^(a×3)×1000000
    // log(a÷1000000)÷3
    
	public final static int SUPPORTED_CLASSIFICATION_ITEMS = 4;
	
    protected static final int[] colors_ryg = new int[] { 0x600000, 0xff0000, 0xffff00, 0x00ff00, 0x00cb00, 0x009600, 0x006100 };
    protected static final int[] colors_ryg_short = new int[] { 0x600000, 0xff0000, 0xffff00, 0x00ff00, 0x00b000 };    
    
    protected static final double[] values_download = new double[] { 0.3871137516, 0.4623712505, 0.5376287495, 0.6128862484, 0.6881437473, 0.7634012462, 0.8386587451 };
    protected static final String[] captions_download = new String[] { "0.4", "", "1.4", "", "5.7", "", "22.6" };
    
    protected static final double[] values_upload = new double[] { 0.3118562527, 0.3871137516, 0.4623712505, 0.5376287495, 0.6128862484, 0.6881437473, 0.7634012462 };
    protected static final String[] captions_upload = new String[] { "0.2", "", "0.7", "", "2.8", "", "11.3" };
    
    protected static final double[] values_ping = new double[] { 0.8996566681, 0.7329900014, 0.5663233348, 0.3996566681, 0.2329900014, 0.0663233348, -0.1003433319 };
    protected static final String[] captions_ping = new String[] { "500", "", "50", "", "5", "", "0.5" };
    
    protected static final Map<String, MapOption> mapOptionMap = new LinkedHashMap<String, MapOption>()
    {
        {
            put("mobile/download", 
                    new MapOption("speed_download", 
                        "speed_download_log",
                        "speed_download is not null AND network_type not in (0, 97, 98, 99)", 
                        
//                        new int[] { 0xffff00, 0xff0000 },
//                        new double[] { 0.5, 0.825257499 },
//                        new String[] { "1", "20" },
                        
                        // ampel
//                        new int[] { 0xff0000, 0xffff00, 0x00ff00 },
//                        new double[] { 0.5, 0.6626287495, 0.825257499 },
//                        new String[] { "1", "4.5", "20" },
                        
                        // ampel 2
                        colors_ryg,
                        values_download,
                        captions_download,
                        
                        // LSD
//                        new int[] { 0x33B5E5, 0xAA66CC, 0x99CC00, 0xFFBB33, 0xFF4444, 0x0099CC, 0x9933CC, 0x669900, 0xFF8800, 0xCC0000 },
//                        new double[] { 0.5, 0.5361397221, 0.5722794442, 0.6084191663, 0.6445588884, 0.6806986106, 0.7168383327, 0.7529780548, 0.7891177769, 0.825257499 },
//                        new String[] { "1", "1.4", "1.9", "2.7", "3.8", "5.3", "7.4", "10", "14", "20" },
                        
                        // LSD reverse
//                        new int[] { 0xCC0000, 0xFF8800, 0x669900, 0x9933CC, 0x0099CC, 0xFF4444, 0xFFBB33, 0x99CC00, 0xAA66CC, 0x33B5E5 },
//                        new double[] { 0.5, 0.5361397221, 0.5722794442, 0.6084191663, 0.6445588884, 0.6806986106, 0.7168383327, 0.7529780548, 0.7891177769, 0.825257499 },
//                        new String[] { "1", "1.4", "1.9", "2.7", "3.8", "5.3", "7.4", "10", "14", "20" },
                        
                        // rotblau
//                        new int[] { 0x0000ff, 0xff00ff, 0xff0000 },
//                        new double[] { 0.5, 0.6626287495, 0.825257499 },
//                        new String[] { "1", "4.5", "20" },
                        
                        // blaurot
//                        new int[] { 0xff0000, 0xff00ff, 0x0000ff },
//                        new double[] { 0.5, 0.6626287495, 0.825257499 },
//                        new String[] { "1", "4.5", "20" },
                        
                        // .se
//                        new int[] { 0x9b55fc, 0x344bfc, 0x0ebff7, 0x08fe05, 0xf8fd04, 0xfbbc04, 0xf40204, 0x790204, 0x240204 },
//                        new double[] { 0.5, 0.5406571874, 0.5813143748, 0.6219715621, 0.6626287495, 0.7032859369, 0.7439431243, 0.7846003116, 0.825257499 },
//                        new String[] { "1", "1.5", "2.1", "3.1", "4.5", "6.5", "9.5", "14", "20" },
                        
                     // falschfarben
//                        new int[] { 0x811616, 0x81b16, 0x818116, 0x4b8116, 0x168116, 0x16814b, 0x168181, 0x164b81, 0x161681, 0x4b1681, 0x811681 },
//                        new double[] { 0.5, 0.5325257499, 0.5650514998, 0.5975772497, 0.6301029996, 0.6626287495, 0.6951544994, 0.7276802493, 0.7602059992, 0.7927317491, 0.825257499 },
//                        new String[] { "1", "1.3", "1.8", "2.5", "3.3", "4.5", "6", "8.1", "11", "14.8", "20" },
                        
                        Classification.THRESHOLD_DOWNLOAD,
                        Classification.THRESHOLD_DOWNLOAD_CAPTIONS,
                        "heatmap",
                        false));
            
            put("mobile/upload",
                    new MapOption("speed_upload",
                        "speed_upload_log",
                        "speed_upload is not null AND network_type not in (0, 97, 98, 99)",
                        colors_ryg,
                        values_upload,
                        captions_upload,
                        Classification.THRESHOLD_UPLOAD,
                        Classification.THRESHOLD_UPLOAD_CAPTIONS,
                        "heatmap",
                        false));

            
            put("mobile/ping",
                    new MapOption("ping_median",
                        "ping_median_log",
                        "ping_median is not null AND network_type not in (0, 97, 98, 99)",
                        colors_ryg,
                        values_ping,
                        captions_ping,
                        Classification.THRESHOLD_PING,
                        Classification.THRESHOLD_PING_CAPTIONS,
                        "heatmap",
                        true));
            
            put("mobile/signal",
                    new MapOption("merged_signal",
                    "merged_signal is not null AND network_type not in (0, 97, 98, 99)",
                    colors_ryg_short,
                    new double[] { -123.5, -108.5, -93.5, -78.5, -63.5 },
                    new String[] { "", "-108", "-94", "-78", "" },
                    Classification.THRESHOLD_SIGNAL_MOBILE,
                    Classification.THRESHOLD_SIGNAL_MOBILE_CAPTIONS,
                    "heatmap",
                    false));
                        
            put("wifi/download", new MapOption("speed_download",
                    "speed_download_log",
                    "speed_download is not null AND network_type = 99",
                    colors_ryg,
                    values_download,
                    captions_download,
                    Classification.THRESHOLD_DOWNLOAD,
                    Classification.THRESHOLD_DOWNLOAD_CAPTIONS,
                    "heatmap",
                    false));
            
            put("wifi/upload",
                    new MapOption("speed_upload",
                    "speed_upload_log",
                    "speed_upload is not null AND network_type = 99",
                    colors_ryg,
                    values_upload,
                    captions_upload,
                    Classification.THRESHOLD_UPLOAD,
                    Classification.THRESHOLD_UPLOAD_CAPTIONS,
                    "heatmap",
                    false));
            
            put("wifi/ping",
                    new MapOption("ping_median",
                    "ping_median_log",
                    "ping_median is not null AND network_type = 99",
                    colors_ryg,
                    values_ping,
                    captions_ping,
                    Classification.THRESHOLD_PING,
                    Classification.THRESHOLD_PING_CAPTIONS,
                    "heatmap",
                    true));
            
            put("wifi/signal", new MapOption("signal_strength",
                    "signal_strength is not null AND network_type = 99",
                    colors_ryg_short,
                    new double[] { -98.5, -83.5, -68.5, -53.5, -38.5 },
                    new String[] { "-99", "", "-69", "", "-39" },
                    Classification.THRESHOLD_SIGNAL_WIFI,
                    Classification.THRESHOLD_SIGNAL_WIFI_CAPTIONS,
                    "heatmap",
                    false));
            
            put("browser/download",
                    new MapOption("speed_download", 
                    "speed_download_log",
                    "speed_download is not null AND network_type = 98",
                    colors_ryg,
                    values_download,
                    captions_download,
                    Classification.THRESHOLD_DOWNLOAD,
                    Classification.THRESHOLD_DOWNLOAD_CAPTIONS,
                    "shapes",
                    false));
            
            put("browser/upload",
                    new MapOption("speed_upload",
                    "speed_upload_log",
                    "speed_upload is not null AND network_type = 98",
                    colors_ryg,
                    values_upload,
                    captions_upload,
                    Classification.THRESHOLD_UPLOAD,
                    Classification.THRESHOLD_UPLOAD_CAPTIONS,
                    "shapes",
                    false));
            
            put("browser/ping",
                    new MapOption("ping_median",
                    "ping_median_log",
                    "ping_median is not null AND network_type = 98",
                    colors_ryg,
                    values_ping,
                    captions_ping,
                    Classification.THRESHOLD_PING,
                    Classification.THRESHOLD_PING_CAPTIONS,
                    "shapes",
                    true));

            put("all/download",
                    new MapOption("speed_download", 
                    "speed_download_log",
                    "speed_download is not null",
                    colors_ryg,
                    values_download,
                    captions_download,
                    Classification.THRESHOLD_DOWNLOAD,
                    Classification.THRESHOLD_DOWNLOAD_CAPTIONS,
                    "shapes",
                    false));
            
            put("all/upload",
                    new MapOption("speed_upload",
                    "speed_upload_log",
                    "speed_upload is not null",
                    colors_ryg,
                    values_upload,
                    captions_upload,
                    Classification.THRESHOLD_UPLOAD,
                    Classification.THRESHOLD_UPLOAD_CAPTIONS,
                    "shapes",
                    false));
            
            put("all/ping",
                    new MapOption("ping_median",
                    "ping_median_log",
                    "ping_median is not null",
                    colors_ryg,
                    values_ping,
                    captions_ping,
                    Classification.THRESHOLD_PING,
                    Classification.THRESHOLD_PING_CAPTIONS,
                    "shapes",
                    true));
        }
    };
    
    protected static final List<SQLFilter> defaultMapFilters = Collections.unmodifiableList(new ArrayList<SQLFilter>()
    {
        {
            add(new SQLFilter("t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'"));
        }
    });
    
    protected static final SQLFilter accuracyMapFilter = new SQLFilter("t.geo_accuracy < 2000"); // 2km
    
    protected static final Map<String, MapFilter> mapFilterMap = Collections.unmodifiableMap(new LinkedHashMap<String, MapFilter>()
    {
        {
            put("operator", new MapFilter()
            {
                @Override
                SQLFilter getFilter(final String input)
                {
                    if (Strings.isNullOrEmpty(input))
                        return null;
                    if (input.equals("other"))
                        return new SQLFilter("mobile_provider_id IS NULL");
                    else
                        return new SQLFilter("mobile_provider_id=?")
                        {
                            @Override
                            int fillParams(int i, final PreparedStatement ps) throws SQLException
                            {
                                ps.setInt(i++, Integer.parseInt(input));
                                return i;
                            }
                        };
                }
            });
            
            put("provider", new MapFilter()
            {
                @Override
                SQLFilter getFilter(final String input)
                {
                    if (Strings.isNullOrEmpty(input))
                        return null;
                    return new SQLFilter("provider_id=?")
                    {
                        @Override
                        int fillParams(int i, final PreparedStatement ps) throws SQLException
                        {
                            ps.setInt(i++, Integer.parseInt(input));
                            return i;
                        }
                    };
                }
            });

            put("technology", new MapFilter()
            {
            	@Override
            	SQLFilter getFilter(final String input)
            	{ // do not filter if empty
            		if (Strings.isNullOrEmpty(input))
            			return null;
            		try
            		{
            			final int technology = Integer.parseInt(input);
            			// use old numeric network type (replicate network_type_table here)
            			if (technology == 2)      // 2G
            				return new SQLFilter("network_type in (1,2,4,5,6,7,11,12,14)");
            			else if (technology == 3) // 3G
            				return new SQLFilter("network_type in (8,9,10,15)");
            			else if (technology == 4) // 4G
            				return new SQLFilter("network_type = 13");
            			else if (technology == 34) // 3G or 4G
            				return new SQLFilter("network_type in (8,9,10,13,15)");
            			else
            				return null;
 
            			/* //alternative: use network_group_name
            			return new SQLFilter("network_group_name=?")
            			{
            				@Override
            				int fillParams(int i, final PreparedStatement ps) throws SQLException
            				{ // convert 2 => '2G'
            					ps.setString(i++, String.format("%dG", technology));
            					return i;
            				}
            			};
            			*/
            		}
            		catch (NumberFormatException e)
            		{
            			return null;
            		}
            	}
            });

            put("period", new MapFilter()
            {
                @Override
                SQLFilter getFilter(final String input)
                {
                    if (Strings.isNullOrEmpty(input))
                        return null;
                    try
                    {
                        int _period = Integer.parseInt(input);
                        if (_period <= 0 || _period > 1460)
                            _period = 1;
                        final int period = _period;
                        
                        return new SQLFilter("t.time > NOW() - CAST(? AS INTERVAL)")
                        {
                            @Override
                            int fillParams(int i, final PreparedStatement ps) throws SQLException
                            {
                                ps.setString(i++, String.format("%d days", period));
                                return i;
                            }
                        };
                    }
                    catch (NumberFormatException e)
                    {
                        return null;
                    }
                }
            });
            
            put("age", new MapFilter()
            {
                @Override
                SQLFilter getFilter(final String input)
                {
                    if (Strings.isNullOrEmpty(input))
                        return null;
                    try
                    {
                        int _age = Integer.parseInt(input);
                        if (_age <= 0 || _age > 1460)
                            _age = 0;
                        final int age = _age;
                        
                        return new SQLFilter("t.time < NOW() - CAST(? AS INTERVAL)")
                        {
                            @Override
                            int fillParams(int i, final PreparedStatement ps) throws SQLException
                            {
                                ps.setString(i++, String.format("%d days", age));
                                return i;
                            }
                        };
                    }
                    catch (NumberFormatException e)
                    {
                        return null;
                    }
                }
            });
            
            put("user_server_selection", new MapFilter()
            {
            	@Override
                SQLFilter getFilter(final String input)
                {            	
        			return new SQLFilter("t.user_server_selection = ?") {

        				@Override
        				int fillParams(int i, final PreparedStatement ps) throws SQLException
        				{
        					ps.setBoolean(i++, Boolean.valueOf(input));
        					return i;
        				}
        			};
                }
            });
            
//            put("device", new MapFilter()
//            {
//                @Override
//                SQLFilter getFilter(final String input)
//                {
//                    if (Strings.isNullOrEmpty(input))
//                        return null;
//                    final String[] devices = input.split(";");
//                    final StringBuilder builder = new StringBuilder("model IN (");
//                    for (int i = 0; i < devices.length; i++)
//                    {
//                        if (i > 0)
//                            builder.append(',');
//                        builder.append('?');
//                    }
//                    builder.append(')');
//                    return new SQLFilter(builder.toString())
//                    {
//                        @Override
//                        int fillParams(int i, final PreparedStatement ps) throws SQLException
//                        {
//                            for (String device : devices)
//                                ps.setString(i++, device);
//                            return i;
//                        }
//                    };
//                }
//            });
        }
    });
    
    public static class MapOption
    {
        public MapOption(final String valueColumn, final String sqlFilter, final int[] colors,
                final double[] intervals, final String[] captions, final int[] classification,
                final String[] classificationCaptions, final String overlayType, final boolean reverseScale)
        {
            this(valueColumn, valueColumn, sqlFilter, colors, intervals, captions, classification,
                    classificationCaptions, overlayType, reverseScale);
        }
        
        public MapOption(final String valueColumn, final String valueColumnLog, final String sqlFilter,
                final int[] colors, final double[] intervals, final String[] captions, final int[] classification,
                final String[] classificationCaptions, final String overlayType, final boolean reverseScale)
        {
            super();
            this.valueColumn = valueColumn;
            this.valueColumnLog = valueColumnLog;
            this.sqlFilter = sqlFilter;
            this.intervals = intervals;
            this.captions = captions;
            this.classification = classification;
            this.classificationCaptions = classificationCaptions;
            this.overlayType = overlayType;
            this.reverseScale = reverseScale;
            
            
            if (intervals.length != colors.length || intervals.length != captions.length)
                throw new IllegalArgumentException("illegal array size");
            
            colorsHexStrings = new String[colors.length];
            for (int i = 0; i < colors.length; i++)
            {
                if (colors[i] < 0 || colors[i] > 0xffffff)
                    throw new IllegalArgumentException("illegal color ["+i+"]: "+colors[i]);
                colorsHexStrings[i] = String.format("#%06x", colors[i]);
            }
            
            final SortedMap<Double, Integer> sortedIntervals = new TreeMap<Double, Integer>();
            for (int i = 0; i < intervals.length; i++)
                sortedIntervals.put(intervals[i], colors[i]);
            
            colorsSorted = new int[sortedIntervals.size()];
            intervalsSorted = new double[sortedIntervals.size()];
            int i = 0;
            for (final Map.Entry<Double, Integer> entry : sortedIntervals.entrySet())
            {
                intervalsSorted[i] = entry.getKey();
                colorsSorted[i++] = entry.getValue();
            }
            
        }
        
        public final String valueColumn;
        public final String valueColumnLog;
        public final String sqlFilter;
        public final int[] colorsSorted;
        public final double[] intervalsSorted;
        public final String[] colorsHexStrings;
        public final double[] intervals;
        public final String[] captions;
        public final int[] classification;
        public final String[] classificationCaptions;
        public final String overlayType;
        public final boolean reverseScale;
        
        public int getClassification(final long value)
        {
            return Classification.classify(classification, value, 4);
        }
    }
    
    static abstract class MapFilter
    {
        abstract SQLFilter getFilter(String input);
    }
    
    static class StaticMapFilter extends MapFilter
    {
        private final SQLFilter filter;
        public StaticMapFilter(String where)
        {
            filter = new SQLFilter(where);
        }
        @Override
        SQLFilter getFilter(String input)
        {
            return filter;
        }
    }
    
    static class SQLFilter
    {
        public SQLFilter(final String where)
        {
            this.where = where;
        }
        
        final String where;
        
        int fillParams(final int i, final PreparedStatement ps) throws SQLException
        {
            return i;
        }
    }
    
    public static Map<String, MapOption> getMapOptionMap()
    {
        return mapOptionMap;
    }
    
    public static Map<String, MapFilter> getMapFilterMap()
    {
        return mapFilterMap;
    }
    
    public static boolean isValidFilter(String name)
    {
        return mapFilterMap.containsKey(name);
    }
    
    public static List<SQLFilter> getDefaultMapFilters()
    {
        return defaultMapFilters;
    }
    
    public static SQLFilter getAccuracyMapFilter()
    {
        return accuracyMapFilter;
    }
}
