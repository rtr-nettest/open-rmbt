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
package at.alladin.rmbt.mapServer.v2;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import at.alladin.rmbt.mapServer.MapServerOptions;
import at.alladin.rmbt.mapServer.MapServerOptions.MapOption;
import at.alladin.rmbt.mapServer.ServerResource;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.util.model.option.OptionFunction;
import at.alladin.rmbt.util.model.option.ServerOption;
import at.alladin.rmbt.util.model.shared.MapOptions;

public class InfoResource extends ServerResource
{
    private List<ServerOption> getMapFilterList() throws JSONException, SQLException
    {
    	final double[] statisticalMethodArray = { 0.8, 0.5, 0.2 };
    	final boolean[] defaultArray = {false, true, false};
    	
        final ServerOption statisticalMethods = new ServerOption(labels.getString("MAP_FILTER_STATISTICAL_METHOD"));
        statisticalMethods.setOptionList(new ArrayList<ServerOption>());
        for (int stat = 1; stat <= statisticalMethodArray.length; stat++) {
            final ServerOption o = new ServerOption(labels.getString("MAP_FILTER_STATISTICAL_METHOD_" + stat + "_TITLE"),
            		labels.getString("MAP_FILTER_STATISTICAL_METHOD_" + stat + "_SUMMARY"));
            o.addParameter("statistical_method", statisticalMethodArray[stat - 1]);
            o.setDefault(defaultArray[stat - 1]);
            statisticalMethods.getOptionList().add(o);
        }

        final List<ServerOption> mapOptions = new ArrayList<ServerOption>();
        mapOptions.add(getMapTypeList());
        mapOptions.add(getTechnology());
        mapOptions.add(getOperators(true));
        mapOptions.add(getOperators(false));
        mapOptions.add(getAppearanceTypeList());
        mapOptions.add(getOverlayTypeList());
        mapOptions.add(getTimes());
        mapOptions.add(statisticalMethods);

        return mapOptions;
    }
    
    ///////////////////////////////////////
    // MAP APPEARANCE TYPES
    ///////////////////////////////////////
    
    public enum AppearanceType {
    	NORMAL,
    	SAT;
    	
    	public OptionFunction getFunctionDef() {
    		final OptionFunction f = new OptionFunction("change_appearance");
    		f.addParameter("type", name().toLowerCase(Locale.US));
    		return f;
    	}
    }
    
    public ServerOption getAppearanceTypeList() {
       	final List<ServerOption> optionList = new ArrayList<ServerOption>();

    	for (AppearanceType overlay : AppearanceType.values()) {        	
    		final ServerOption o = new ServerOption(labels.getString("MAP_APPEARANCE_" + overlay.name()), 
    			labels.getString("MAP_APPEARANCE_" + overlay.name() + "_SUMMARY"));
    	    		
    		o.addFunction(overlay.getFunctionDef());
    		
	    	if (AppearanceType.NORMAL.equals(overlay)) {
	    		o.setDefault(true);
	    	}
	    		
	    	optionList.add(o);
    	}

    	final ServerOption option = new ServerOption(labels.getString("MAP_APPEARANCE"));
    	option.setOptionList(optionList);
    	
        return option;    	
    }
    
    ///////////////////////////////////////
    // OVERLAY TYPES
    ///////////////////////////////////////
    
    public enum OverlayType {
    	AUTO,
    	HEATMAP("/RMBTMapServer/tiles/heatmap", "heatmap", 100000000, 256),
    	POINTS("/RMBTMapServer/tiles/points", "points", 100000000, 256),
    	SHAPES("/RMBTMapServer/tiles/shapes", "shapes", 200000000, 512);
    	
    	final String path;
    	final String type;
    	final int zIndex;
    	final int tileSize;
    	
    	OverlayType() {
    		this(null, "automatic", 0, 0);
		}
    	
    	OverlayType(final String path, final String type, final int zIndex, final int tileSize) {
    		this.path = path;
    		this.zIndex = zIndex;
    		this.type = type;
    		this.tileSize = tileSize;
    	}

		public String getPath() {
			return path;
		}

		public String getType() {
			return type;
		}
		
		public int getzIndex() {
			return zIndex;
		}

		public int getTileSize() {
			return tileSize;
		}
		
		public OptionFunction getFunctionDef(final String functionName) {
			final OptionFunction f = new OptionFunction(functionName);
    		f.addParameter("path", getPath());
    		f.addParameter("z_index", getzIndex());
    		f.addParameter("tile_size", getTileSize());
    		f.addParameter("type", getType());
    		return f;
		}
    }
    
    private ServerOption getOverlayTypeList() throws JSONException
    {
    	final List<ServerOption> optionList = new ArrayList<ServerOption>();

    	for (OverlayType overlay : OverlayType.values()) {        	
    		final ServerOption o = new ServerOption(labels.getString("OVERLAY_" + overlay.name()), 
    			labels.getString("OVERLAY_" + overlay.name() + "_SUMMARY"));
    	    		
    		o.addFunction(overlay.getFunctionDef("set_overlay"));
	    		
	    	if (OverlayType.AUTO.equals(overlay)) {
	    		o.addFunction(OverlayType.HEATMAP.getFunctionDef("add_alt_overlay"));
	    		o.addFunction(OverlayType.POINTS.getFunctionDef("add_alt_overlay"));
	    		o.setDefault(true);
	    	}
	    		
	    	optionList.add(o);
    	}

    	final ServerOption option = new ServerOption(labels.getString("OVERLAY_TYPE"));
    	option.setOptionList(optionList);
    	
        return option;
    }
    
    ///////////////////////////////////////
    // MAP TYPES
    ///////////////////////////////////////
        
    private ServerOption getMapTypeList() throws JSONException
    {
        String lastType = null;
        
        final ServerOption mapTypeOption = new ServerOption(labels.getString(String.format("MAP_TYPE")));
        final OptionFunction dropFunction = new OptionFunction("drop_param");
        dropFunction.addParameter("key", "map_type_is_mobile");
        mapTypeOption.addFunction(dropFunction);
        
        ServerOption subOption = new ServerOption();
        
        final Map<String, MapOption> mapOptionMap = MapServerOptions.getMapOptionMap();
        for (final Map.Entry<String, MapOption> entry : mapOptionMap.entrySet())
        {
            final String key = entry.getKey();
            final MapOption mapOption = entry.getValue();
            final String[] split = key.split("/");       
        	
            if (lastType == null || !lastType.equals(split[0]))
            {
                lastType = split[0];
            	subOption = new ServerOption(labels.getString(String.format("MAP_%s", lastType.toUpperCase())));
               	subOption.addParameter("map_type_is_mobile", lastType.toUpperCase().equals("MOBILE"));
            	mapTypeOption.addOption(subOption);
            }
            
            final String type = split[1].toUpperCase();
            final ServerOption subOptionItem = new ServerOption(labels.getString(String.format("RESULT_%s", type)), labels.getString(String.format("MAP_%s_SUMMARY", type)));
            subOptionItem.addParameter("map_options", key);
            if ("mobile/download".equals(key)) {
            	subOptionItem.setDefault(true);
            }
            subOptionItem.addParameter("overlay_type", mapOption.overlayType);
            subOption.addOption(subOptionItem);
        }
        
        return mapTypeOption;
    }
	
    
    ///////////////////////////////////////
    // TIME FILTER
    ///////////////////////////////////////
    
    private final static int[] OPTION_TIMES_VALUE = new int[] { 7, 30, 90, 180, 365, 730, 1460 };
    
    private ServerOption getTimes() throws JSONException
    {
    	final List<ServerOption> optionList = new ArrayList<ServerOption>();
    	// expects resources in the format MAP_FILTER_PERIOD_<n>_DAYS and MAP_FILTER_PERIOD_<n>_DAYS_SUMMARY
    	for (int i = 0; i < OPTION_TIMES_VALUE.length; i++) {
    		String title ="MAP_FILTER_PERIOD_"+Integer.toString(OPTION_TIMES_VALUE[i])+"_DAYS";
        	final ServerOption o = new ServerOption(labels.getString(title), 
        			labels.getString(title+"_SUMMARY"));
        	o.addParameter("period", OPTION_TIMES_VALUE[i]);
        	if (OPTION_TIMES_VALUE[i] == 180) {
        		o.setDefault(true);
        	}
        	optionList.add(o);
    		
    	}
    	
    	final ServerOption option = new ServerOption(labels.getString("MAP_FILTER_PERIOD"));
    	option.setOptionList(optionList);
    	
        return option;
    }
    
    
    ///////////////////////////////////////
    // TECHNOLOGY FILTER
    ///////////////////////////////////////
    
    private final static String[] OPTION_TECHNOLOGY_TITLE = new String[] {
    	"ANY", "3G_4G", "2G", "3G", "4G"};

    private final static String[] OPTION_TECHNOLOGY_VALUE = new String[] {
    	 "", "34", "2", "3", "4"};
    
    private ServerOption getTechnology() throws JSONException
    {
    	final List<ServerOption> optionList = new ArrayList<ServerOption>();

    	for (int i = 0; i < OPTION_TECHNOLOGY_TITLE.length; i++) {
    		// expects resources in the format MAP_FILTER_TECHNOLOGY_<title> and MAP_FILTER_TECHNOLOGY_<title>_SUMMARY
    		String title ="MAP_FILTER_TECHNOLOGY_"+OPTION_TECHNOLOGY_TITLE[i];
        	final ServerOption o = new ServerOption(labels.getString(title), labels.getString(title+"_SUMMARY"));
        	o.addParameter("technology", OPTION_TECHNOLOGY_VALUE[i]);
        	if ("".equals(OPTION_TECHNOLOGY_VALUE[i])) {
        		o.setDefault(true);
        	}
        	optionList.add(o);
    		
    	}
    	
    	final ServerOption option = new ServerOption(labels.getString("MAP_FILTER_TECHNOLOGY"));
    	option.setOptionList(optionList);
    	option.addDependsOn("map_type_is_mobile", true);
        return option;
    }
    
    
    ///////////////////////////////////////
    // OPERATORS FILTER
    ///////////////////////////////////////    
    
    private ServerOption getOperators(final boolean mobile) throws JSONException, SQLException
    {
    	final ServerOption optionAll = new ServerOption(labels.getString("MAP_FILTER_ALL_OPERATORS"));
   		optionAll.addParameter(mobile ? "operator" : "provider", "");
   		optionAll.setDefault(true);
   		
    	final ServerOption option = new ServerOption(labels.getString("MAP_FILTER_CARRIER"));
    	option.setOptionList(new ArrayList<ServerOption>());
   		option.getOptionList().add(optionAll);
   		option.addDependsOn("map_type_is_mobile", mobile);
   		        
        final String sql = "SELECT uid,name,mcc_mnc,shortname FROM provider p WHERE p.map_filter=true"
                + (mobile ? " AND p.mcc_mnc IS NOT NULL" : " ") + " ORDER BY shortname";  // allow mobile networks for wifi/browser
        
        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            try (final ResultSet rs = ps.executeQuery()) {
	            if (rs == null) {
	                return null;
	            }
	            
	            while (rs.next()) {
	            	final ServerOption o = new ServerOption();
	                o.setTitle(rs.getString("shortname"));
	                o.setSummary(rs.getString("name"));
	                o.addParameter(mobile ? "operator" : "provider", rs.getLong("uid"));
	                option.getOptionList().add(o);
	            }
            }
        }
        
        
        return option;
    }
    
    @Post("json")
    @Get("json")
    public String request(final String entity)
    {
        addAllowOrigin();
        
        JSONObject request = null;
        
        try
        {
            
            String lang = settings.getString("RMBT_DEFAULT_LANGUAGE");
            
            if (entity != null)
            {
                request = new JSONObject(entity);
                
                lang = request.optString("language");
                
                final List<String> langs = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));
                
                if (langs.contains(lang))
                    labels = ResourceManager.getSysMsgBundle(new Locale(lang));
            }
            
            MapOptions filter = new MapOptions();
            filter.setMapFilterList(getMapFilterList());
            
            return ServerOption.getGson().toJson(filter);
        }
        catch (final JSONException e)
        {
            System.out.println("Error parsing JSDON Data " + e.toString());
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
