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
package at.alladin.rmbt.android.map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.AppConstants;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.map.overlay.RMBTBalloonOverlayItem;
import at.alladin.rmbt.android.map.overlay.RMBTBalloonOverlayView;
import at.alladin.rmbt.android.util.CheckMarker;
import at.alladin.rmbt.android.util.ConfigHelper;
import at.alladin.rmbt.android.util.EndTaskListener;
import at.alladin.rmbt.android.util.GeoLocation;
import at.alladin.rmbt.android.util.PermissionHelper;
import at.alladin.rmbt.util.model.option.OptionFunction;
import at.alladin.rmbt.util.model.option.OptionFunctionCallback;
import at.alladin.rmbt.util.model.option.ServerOption;

public class RMBTMapFragment extends SupportMapFragment implements OnClickListener, OnCameraChangeListener, OnMapClickListener, InfoWindowAdapter, OnInfoWindowClickListener, OnMyLocationChangeListener, OnMarkerClickListener
{
//    private static final String DEBUG_TAG = "RMBTMapFragment";
	
	protected class TileOverlayHolder {
		final TileOverlay tileOverlay;
		final MapOverlayType overlayType;
		
		public TileOverlayHolder(final TileOverlay tileOverlay, final MapOverlayType overlayType) {
			this.tileOverlay = tileOverlay;
			this.overlayType = overlayType;
		}

		public TileOverlay getTileOverlay() {
			return tileOverlay;
		}

		public MapOverlayType getOverlayType() {
			return overlayType;
		}

		@Override
		public String toString() {
			return "TileOverlayHolder [tileOverlay=" + tileOverlay
					+ ", overlayType=" + overlayType + "]";
		}
	}
	
	protected class MapOverlayType {
		String path;
		String type;
		long zIndex;
		int tileSize;
		
		public MapOverlayType(final OptionFunction function) {
			this.path = (String) function.getParameterMap().get("path");
			this.type = (String) function.getParameterMap().get("type");
			this.zIndex = ((Double) function.getParameterMap().get("z_index")).longValue();
			this.tileSize = ((Double) function.getParameterMap().get("tile_size")).intValue();
		}
		
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public long getzIndex() {
			return zIndex;
		}
		public void setzIndex(long zIndex) {
			this.zIndex = zIndex;
		}
		public int getTileSize() {
			return tileSize;
		}
		public void setTileSize(int tileSize) {
			this.tileSize = tileSize;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return "MapOverlayType [path=" + path + ", type=" + type
					+ ", zIndex=" + zIndex + ", tileSize=" + tileSize + "]";
		}
	}
    
	public final static String OPTION_SHOW_INFO_TOAST = "show_info_toast";
	public final static String OPTION_ENABLE_ALL_GESTURES = "enable_all_gestures";
	public final static String OPTION_ENABLE_CONTROL_BUTTONS = "enable_control_buttons";
	public final static String OPTION_ENABLE_OVERLAY = "enable_overlay";
	
    private GoogleMap gMap;
    
    private List<TileOverlayHolder> tileOverlayList = new ArrayList<TileOverlayHolder>();
    
    private Marker myLocationMarker;
    private MyGeoLocation geoLocation;
    private BitmapDescriptor markerIconBitmapDescriptor;
    
    private boolean firstStart = true;
    private Handler handler = new Handler();
    private Runnable checkSettingsRunnable;

    private RMBTMapFragmentOptions options = new RMBTMapFragmentOptions();
    
    private OnClickListener additionalMapClickListener;
    
    private boolean myLocationEnabled;
    
    public class RMBTMapFragmentOptions {
    	private boolean showInfoToast = true;
    	private boolean enableAllGestures = true;
    	private boolean enableControlButtons = true;
    	private boolean enableOverlay = true;
    	
		public boolean isShowInfoToast() {
			return showInfoToast;
		}
		public void setShowInfoToast(boolean showInfoToast) {
			this.showInfoToast = showInfoToast;
		}
		public boolean isEnableAllGestures() {
			return enableAllGestures;
		}
		public void setEnableAllGestures(boolean enableAllGestures) {
			this.enableAllGestures = enableAllGestures;
		}
		public boolean isEnableControlButtons() {
			return enableControlButtons;
		}
		public void setEnableControlButtons(boolean enableControlButtons) {
			this.enableControlButtons = enableControlButtons;
		}
		public boolean isEnableOverlay() {
			return enableOverlay;
		}
		public void setEnableOverlay(boolean enableOverlay) {
			this.enableOverlay = enableOverlay;
		}
    }
    
    private class MyGeoLocation extends GeoLocation
    {
        public MyGeoLocation(Context context)
        {
            super(context, ConfigHelper.isGPS(context));
        }
        
        @Override
        public void onLocationChanged(Location location)
        {
            if (onLocationChangedListener != null)
                onLocationChangedListener.onLocationChanged(location);
        }
    }
    
    private OnLocationChangedListener onLocationChangedListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        myLocationEnabled = PermissionHelper.checkAnyLocationPermission(getActivity());
        if (myLocationEnabled)
            geoLocation = new MyGeoLocation(getActivity());
        
        final Bundle bundle = getArguments();
        
        if (bundle != null) {                	
            if (bundle.containsKey(OPTION_ENABLE_ALL_GESTURES)) {
            	options.setEnableAllGestures(bundle.getBoolean(OPTION_ENABLE_ALL_GESTURES));
            }
            if (bundle.containsKey(OPTION_SHOW_INFO_TOAST)) {
            	options.setShowInfoToast(bundle.getBoolean(OPTION_SHOW_INFO_TOAST));
            }
            if (bundle.containsKey(OPTION_ENABLE_CONTROL_BUTTONS)) {
            	options.setEnableControlButtons(bundle.getBoolean(OPTION_ENABLE_CONTROL_BUTTONS));
            }
            if (bundle.containsKey(OPTION_ENABLE_OVERLAY)) {
            	options.setEnableOverlay(bundle.getBoolean(OPTION_ENABLE_OVERLAY));
            }
        }
    }
    
    @Override
    public void onStart()
    {
        super.onStart();
        if (geoLocation != null)
            geoLocation.start();
        
        gMap = getMap();
        if (gMap != null)
        {
            checkSettingsRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    final RMBTMainActivity activity = (RMBTMainActivity) getActivity();
                    if (activity.getMapOptions() == null)
                    {
                        activity.fetchMapOptions();
                        handler.postDelayed(checkSettingsRunnable, 500);
                    }
                    else {
                    	setFilter();
                    }
                }
            };
            
            checkSettingsRunnable.run();
        	
            if (firstStart)
            {
                final Bundle bundle = getArguments();
                                
                firstStart = false;
                
                final UiSettings uiSettings = gMap.getUiSettings();
                uiSettings.setZoomControlsEnabled(false); // options.isEnableAllGestures());
                uiSettings.setMyLocationButtonEnabled(false);
                uiSettings.setCompassEnabled(false);
                uiSettings.setRotateGesturesEnabled(false);
                uiSettings.setScrollGesturesEnabled(options.isEnableAllGestures());
                
                gMap.setTrafficEnabled(false);
                gMap.setIndoorEnabled(false);
                
                LatLng latLng = MapProperties.DEFAULT_MAP_CENTER;
                float zoom = MapProperties.DEFAULT_MAP_ZOOM;
                
                
                if (geoLocation != null)
                {
                    final Location lastKnownLocation = geoLocation.getLastKnownLocation();
                    if (lastKnownLocation != null)
                    {
                        latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        zoom = MapProperties.DEFAULT_MAP_ZOOM_LOCATION;
                    }
                }

                if (bundle != null)
                {
                    final LatLng initialCenter = bundle.getParcelable("initialCenter");
                    if (initialCenter != null)
                    {
                        latLng = initialCenter;
                        zoom = MapProperties.POINT_MAP_ZOOM;
                        
                        if (balloonMarker != null)
                            balloonMarker.remove();
                        balloonMarker = gMap.addMarker(
                            new MarkerOptions().
                            position(initialCenter).
                            draggable(false).
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            );
                    }
                }
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
                
                gMap.setLocationSource(new LocationSource()
                {
                    
                    @Override
                    public void deactivate()
                    {
                        onLocationChangedListener = null;
                    }
                    
                    @Override
                    public void activate(OnLocationChangedListener listener)
                    {
                        onLocationChangedListener = listener;
                    }
                });
                
                if (myLocationEnabled)
                    gMap.setMyLocationEnabled(true);
                gMap.setOnMyLocationChangeListener(this);
                gMap.setOnMarkerClickListener(this);
                gMap.setOnMapClickListener(this);
                gMap.setInfoWindowAdapter(this);
                gMap.setOnInfoWindowClickListener(this);
                
                
                if (myLocationEnabled)
                {
                    final Location myLocation = gMap.getMyLocation();
                    if (myLocation != null)
                        onMyLocationChange(myLocation);
                }
            }
        }
    }
    
    private void setFilter() {
        final RMBTMainActivity activity = (RMBTMainActivity) getActivity();
        gMap.setMapType(activity.getMapTypeSatellite() ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
        
        final List<MapOverlayType> overlayList = new ArrayList<RMBTMapFragment.MapOverlayType>();
        final Map<String, String> mapOptions = ((MapProperties) getActivity()).getCurrentMapOptions(new OptionFunctionCallback() {
			
			@Override
			public boolean onCall(final ServerOption callingOption, final OptionFunction function) {
				if ("set_overlay".equals(function.getName().toLowerCase(Locale.US))) {
					final MapOverlayType type = new MapOverlayType(function);
					if ("automatic".equals(type.getType())) {
						overlayList.add(type);
						MapOverlayType altPointsType = null;
						MapOverlayType altHeatmapType = null;
						for (final OptionFunction func : callingOption.getFunctionList()) {
							if ("add_alt_overlay".equals(func.getName())) {
								final MapOverlayType altType = new MapOverlayType(func);
								if ("heatmap".equals(altType.getType())) {
									altHeatmapType = altType;
								}
								else if ("points".equals(altType.getType())) {
									altPointsType = altType;
								}
							}
						}
						
						if (altHeatmapType != null) {
							overlayList.add(altHeatmapType);
						}
						if (altPointsType != null) {
							overlayList.add(altPointsType);
						}
					}
					else {
						overlayList.add(type);
					}
				}
				else if ("change_appearance".equals(function.getName().toLowerCase(Locale.US))) {
					boolean isSatAppearance = "sat".equals(function.getParameterMap().get("type"));
					gMap.setMapType(isSatAppearance ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
				}
				return false;
			}
		});
        
        System.out.println(mapOptions);
        
        final String protocol = ConfigHelper.isMapSeverSSL(getActivity()) ? "https" : "http";
        final String host = ConfigHelper.getMapServerName(getActivity());
        final int port = ConfigHelper.getMapServerPort(getActivity());
        
        if (overlayList.size() > 0) {
            if ("automatic".equals(overlayList.get(0).getType())) {
            	//automatic ?
            	System.out.println(overlayList);
            	gMap.setOnCameraChangeListener(this);
            	if (overlayList.size() > 1) {            		
            		for (MapOverlayType autoOverlay : overlayList) {
                		if (autoOverlay != null) {            		
    		                final RMBTTileSourceProvider heatmapProvider = new RMBTTileSourceProvider(protocol, host, port, autoOverlay.getTileSize());
    		                heatmapProvider.setOptionMap(mapOptions);
    		                heatmapProvider.setPath(autoOverlay.getPath());
    	
    		                tileOverlayList.add(new TileOverlayHolder(
    		                		gMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapProvider).zIndex(autoOverlay.getzIndex())), autoOverlay));
                		}
            		}
            	}
            	
            	onCameraChange(gMap.getCameraPosition());
            }
            else {
                gMap.setOnCameraChangeListener(null);
            	for (MapOverlayType overlay : overlayList) {
	                final RMBTTileSourceProvider tileProvider = new RMBTTileSourceProvider(protocol, host, port, overlay.getTileSize());
	                tileProvider.setOptionMap(mapOptions);
	                tileProvider.setPath(overlay.getPath());
	                tileOverlayList.add(new TileOverlayHolder(
	                		gMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider).zIndex(overlay.getzIndex())), overlay));
	                if ("shapes".equals(overlay.getType())) {
	                	gMap.setOnCameraChangeListener(this);
	                }
            	}
            }
        }
        
        System.out.println(tileOverlayList);
    }
    
    @Override
    public void onMyLocationChange(Location location)
    {
        if (myLocationMarker != null)
            myLocationMarker.remove(); 
        
        if (markerIconBitmapDescriptor == null)
            markerIconBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.bg_trans_light);

        myLocationMarker = gMap.addMarker(new MarkerOptions() 
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .icon(markerIconBitmapDescriptor)); 
    }
    
    @Override
    public boolean onMarkerClick(Marker marker)
    {
        if (myLocationMarker != null && marker.equals(myLocationMarker))
        {
            // redirect to map click
            onMapClick(marker.getPosition());
            return true;
        }
        return false;
    }

    
    @Override
    public void onStop()
    {
        super.onStop();
        cancelCheckMarker();
        if (geoLocation != null)
            geoLocation.stop();
        Iterator<TileOverlayHolder> it = tileOverlayList.iterator();
        
        if (checkSettingsRunnable != null)
            handler.removeCallbacks(checkSettingsRunnable);
        
        while (it.hasNext()) {
        	final TileOverlayHolder overlay = it.next();
        	overlay.getTileOverlay().clearTileCache();
        	overlay.getTileOverlay().remove();
        	it.remove();
        }
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        if (options.showInfoToast) {
            showInfoToast();	
        }
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.map_google, container, false);
        registerListeners(view);
        if (! myLocationEnabled)
            view.findViewById(R.id.mapLocateButton).setVisibility(View.INVISIBLE);
        
        final int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (errorCode != ConnectionResult.SUCCESS)
        {
            final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(), 0);
            errorDialog.show();
            getFragmentManager().popBackStack();
            return view;
        }
        
        View mapView = super.onCreateView(inflater, container, savedInstanceState);
        
        final RelativeLayout mapViewContainer = (RelativeLayout) view.findViewById(R.id.mapViewContainer);
        mapViewContainer.addView(mapView);
        
        
        ProgressBar progessBar = new ProgressBar(getActivity());
        final RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
        progessBar.setLayoutParams(layoutParams);
        progessBar.setVisibility(View.GONE);
        view.addView(progessBar);
        
        
        return view;
    }
    
    private void registerListeners(View view)
    {
        final Button mapChooseButton = (Button) view.findViewById(R.id.mapChooseButton);
        final Button mapLocateButton = (Button) view.findViewById(R.id.mapLocateButton);
        final Button mapHelpButton = (Button) view.findViewById(R.id.mapHelpButton);
        final Button mapZoomInButton = (Button) view.findViewById(R.id.mapZoomInButton);
        final Button mapZoomOutButton = (Button) view.findViewById(R.id.mapZoomOutButton);
        final Button mapLocationSearchButton = (Button) view.findViewById(R.id.mapLocationSearchButton);

        if (options.isEnableControlButtons()) {
            mapChooseButton.setOnClickListener(this);
            mapLocateButton.setOnClickListener(this);
            mapHelpButton.setOnClickListener(this);
            mapZoomInButton.setOnClickListener(this);
            mapZoomOutButton.setOnClickListener(this);
            mapLocationSearchButton.setOnClickListener(this);
        }        
        else {
        	mapChooseButton.setVisibility(View.GONE);
        	mapLocateButton.setVisibility(View.GONE);
        	mapHelpButton.setVisibility(View.GONE);
        	mapZoomInButton.setVisibility(View.GONE);
        	mapZoomOutButton.setVisibility(View.GONE);
        	mapLocationSearchButton.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onCameraChange(CameraPosition cp)
    {
    	for (TileOverlayHolder overlay : tileOverlayList) {
    		if ("points".equals(overlay.getOverlayType().getType())) {
    			final boolean automaticShowPoints = cp.zoom >= MapProperties.MAP_AUTO_SWITCH_VALUE;
    			if (automaticShowPoints && !overlay.getTileOverlay().isVisible()) {
    				overlay.getTileOverlay().setVisible(true);
    			}
    			else if (!automaticShowPoints && overlay.getTileOverlay().isVisible()) {
    				overlay.getTileOverlay().setVisible(false);
    			}
    		}
    	}
    }
    
    @Override
    public void onClick(View v)
    {
        final FragmentManager fm = ((FragmentActivity) getActivity()).getSupportFragmentManager();
        final FragmentTransaction ft;
        
        final GoogleMap map = getMap();

        switch (v.getId())
        {

        case R.id.mapChooseButton:

            ft = fm.beginTransaction();
            final RMBTMapFilterFragment mapFilterFragment = new RMBTMapFilterFragment();
            ft.replace(R.id.fragment_content, mapFilterFragment, "map_filter");
            ft.addToBackStack("map_filter");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
            
            break;

        case R.id.mapLocateButton:

            if (map != null && geoLocation != null)
            {
                final Location location = geoLocation.getLastKnownLocation();
                if (location != null)
                {
                    final LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                    gMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                }
            }
            break;

        case R.id.mapHelpButton:
            ((RMBTMainActivity) getActivity()).showHelp("", false, AppConstants.PAGE_TITLE_HELP); // TODO: put correct
                                                        // help url
            break;

        case R.id.mapZoomInButton:

            if (map != null)
                map.animateCamera(CameraUpdateFactory.zoomIn());

            break;

        case R.id.mapZoomOutButton:

            if (map != null)
                map.animateCamera(CameraUpdateFactory.zoomOut());

            break;

        case R.id.mapLocationSearchButton:
        	
        	if (map != null) {
        		MapLocationSearch.showDialog(this);
        	}
        	
        	break;
            
        default:
            break;
        }

    }

    private void showInfoToast()
    {
        final Map<String, String> currentMapOptionTitles = ((RMBTMainActivity) getActivity())
                .getCurrentMapOptionTitles();
        String infoString = "";
        for (final String s : currentMapOptionTitles.values())
        {
            if (infoString.length() > 0)
                infoString += "\n";

            infoString += s;
        }
        if (infoString.length() > 0)
        {
            final Toast toast = Toast.makeText(getActivity(), infoString, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 0);
            toast.show();
        }
    }
    
    private CheckMarker checkMarker;
    private RMBTBalloonOverlayItem balloon;
    private String openTestUUIDURL;
    private Marker balloonMarker;
    private final EndTaskListener checkMarkerEndTaskListener = new EndTaskListener()
    {
        @Override
        public void taskEnded(JSONArray result)
        {
            if (! isVisible())
                return;
            final GoogleMap gMap = getMap();
            if (gMap == null || result == null)
                return;
            
            try
            {
                if (result.length() == 0)
                    return;
                
                final JSONObject resultListItem = result.getJSONObject(0);
                
                final LatLng latLng = new LatLng(resultListItem.getDouble("lat"), resultListItem.getDouble("lon"));
                
                openTestUUIDURL = null;
                final String openDataPrefix = ConfigHelper.getVolatileSetting("url_open_data_prefix");
                if (openDataPrefix != null && openDataPrefix.length() > 0)
                {
                    final String openUUID = resultListItem.optString("open_test_uuid", null);
                    if (openUUID != null && openUUID.length() > 0)
                        openTestUUIDURL = openDataPrefix + openUUID + "#noMMenu";
                }
                
                balloon = new RMBTBalloonOverlayItem(latLng, getResources().getString(R.string.map_balloon_overlay_header), result);
                
                if (balloonMarker != null)
                    balloonMarker.remove();
                
                balloonMarker = gMap.addMarker(new MarkerOptions().
                        position(latLng).
                        draggable(false).
                        icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                balloonMarker.showInfoWindow();
                
                
                gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                gMap.moveCamera(CameraUpdateFactory.scrollBy(0, getResources().getDisplayMetrics().density * -175));
                
//                gMap.animateCamera(CameraUpdateFactory.newLatLng(latLng), 300, new GoogleMap.CancelableCallback()
//                {
//                    @Override
//                    public void onFinish()
//                    {
//                        gMap.animateCamera(CameraUpdateFactory.scrollBy(0, -400), 300, null);
////                        gMap.moveCamera(CameraUpdateFactory.scrollBy(0, -400));
//                    }
//                    
//                    @Override
//                    public void onCancel()
//                    {
//                    }
//                });
            }
            catch (final JSONException e)
            {
                e.printStackTrace();
            }
        }
    };
    
    private void cancelCheckMarker()
    {
        if (checkMarker != null)
            checkMarker.cancel(true);
    }
    
    @Override
    public void onMapClick(LatLng latLng)
    {
    	if (additionalMapClickListener != null) {
    		additionalMapClickListener.onClick(getView());
    	}
    	
    	if (options.isEnableAllGestures() || options.isEnableControlButtons()) {
    		cancelCheckMarker();
    		checkMarker = new CheckMarker(getActivity(), latLng.latitude, latLng.longitude,
    				(int) gMap.getCameraPosition().zoom, 20); // TODO correct params (zoom, size)
    		checkMarker.setEndTaskListener(checkMarkerEndTaskListener);
    		checkMarker.execute();
    	}
    }

    @Override
    public View getInfoWindow(Marker marker)
    {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker)
    {
        if (balloon == null)
            return null;
        
        if (balloonMarker == null || ! balloonMarker.equals(marker))
            return null;
        
//        final FrameLayout view = new FrameLayout(getActivity());
        final RMBTBalloonOverlayView bv = new RMBTBalloonOverlayView(getActivity());
        final View view = bv.setupView(getActivity(), null);
        bv.setBalloonData(balloon, null);
//        view.addView(bv);
        return view;
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {
        if (balloonMarker == null || ! balloonMarker.equals(marker))
            return;
        
        balloonMarker.hideInfoWindow();
        if (openTestUUIDURL != null)
        {
            Log.d(getTag(), "go to url: " + openTestUUIDURL);
            final RMBTMainActivity activity = (RMBTMainActivity) getActivity();
            activity.showHelp(openTestUUIDURL, false, AppConstants.PAGE_TITLE_MAP);
        }
    }

    /**
     * 
     * @return
     */
	public OnClickListener getAdditionalMapClickListener() {
		return additionalMapClickListener;
	}

	/**
	 * 
	 * @param additionalMapClickListener
	 */
	public void setAdditionalMapClickListener(OnClickListener additionalMapClickListener) {
		this.additionalMapClickListener = additionalMapClickListener;
	}

}
