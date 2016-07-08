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
package at.alladin.rmbt.android.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.main.RMBTMainMenuFragment;
import at.alladin.rmbt.client.helper.RevisionHelper;
import at.alladin.rmbt.client.v2.task.result.QoSResultCollector;
import at.alladin.rmbt.util.model.shared.LoopModeSettings;

public class InformationCollector
{
	/**
	 * set to true if location information should be included to server request
	 */
	public final static boolean BASIC_INFORMATION_INCLUDE_LOCATION = true;
	
	/**
	 * set to true if last signal information should be included to server request
	 */
	public final static boolean BASIC_INFORMATION_INCLUDE_LAST_SIGNAL_ITEM = true;
	
    private static final int UNKNOWN = Integer.MIN_VALUE;
    
    private static final String PLATTFORM_NAME = "Android";
    
    private static final String DEBUG_TAG = "InformationCollector";
    
    private static final int ACCEPT_WIFI_RSSI_MIN = -113;
    
    public static final int SINGAL_TYPE_NO_SIGNAL = 0;
    public static final int SINGAL_TYPE_MOBILE = 1;
    public static final int SINGAL_TYPE_RSRP = 2;
    public static final int SINGAL_TYPE_WLAN = 3;
    
    /** Returned by getNetwork() if Wifi */
    public static final int NETWORK_WIFI = 99;
    
    /** Returned by getNetwork() if Ethernet */
    public static final int NETWORK_ETHERNET = 106;
    
    /** Returned by getNetwork() if Bluetooth */
    public static final int NETWORK_BLUETOOTH = 107;
    
    private ConnectivityManager connManager = null;
    
    private TelephonyManager telManager = null;
    
    private TelephonyStateListener telListener = null;
    
    private WifiManager wifiManager = null;
    
    // Handlers and Receivers for phone and network state
    private NetworkStateBroadcastReceiver networkReceiver;
    
    private InfoGeoLocation locationManager = null;
    
    private Location lastLocation;
    
    private String testServerName;
    
    private Properties fullInfo = null;
    
    private Context context = null;
    private boolean collectInformation;
    private boolean registerNetworkReiceiver;
    private boolean enableGeoLocation;
    
    private final List<GeoLocationItem> geoLocations = new ArrayList<GeoLocationItem>();
    private final List<CellLocationItem> cellLocations = new ArrayList<CellLocationItem>();
    private final List<SignalItem> signals = new ArrayList<SignalItem>();
    
    private final AtomicInteger signal = new AtomicInteger(Integer.MIN_VALUE);
    private final AtomicInteger signalType = new AtomicInteger(SINGAL_TYPE_NO_SIGNAL);
    private final AtomicInteger signalRsrq = new AtomicInteger(UNKNOWN);
    
    private final AtomicReference<SignalItem> lastSignalItem = new AtomicReference<InformationCollector.SignalItem>();
    private final AtomicInteger lastNetworkType = new AtomicInteger(TelephonyManager.NETWORK_TYPE_UNKNOWN);
    private final AtomicBoolean illegalNetworkTypeChangeDetcted = new AtomicBoolean(false);
    
    public static QoSResultCollector qoSResult;
    
    public InformationCollector(final Context context, final boolean collectInformation, final boolean registerNetworkReceiver) {
    	this(context, collectInformation, registerNetworkReceiver, true);
    }
    
    private static boolean haveCourseLocationPerm;
    private static boolean haveAnyLocationPerm;
    
    public InformationCollector(final Context context, final boolean collectInformation, final boolean registerNetworkReceiver, final boolean enableGeoLocation)
    {
        // create and load default properties
        
        haveCourseLocationPerm = PermissionHelper.checkCoarseLocationPermission(context);
        haveAnyLocationPerm = PermissionHelper.checkAnyLocationPermission(context);
        
        this.context = context;
        this.collectInformation = collectInformation;
        this.registerNetworkReiceiver = registerNetworkReceiver;
        this.enableGeoLocation = haveAnyLocationPerm ? enableGeoLocation : false;
        
        init();
        
    }
    
    public void init()
    {
        
        // this.unload();
        
        reset();
        
        initNetwork();
        
        getClientInfo();
        
        getTelephonyInfo();
        
        getWiFiInfo();
        
        getLocationInfo();
        
        registerListeners();
        
        registerNetworkReceiver();
        
    }
    
    public void reInit()
    {
    	reset();	
        
        initNetwork();
        
        getClientInfo();
        
        getTelephonyInfo();
        
        getWiFiInfo();
        
        getLocationInfo();
        
        registerListeners();
        
        registerNetworkReceiver();
        
    }
    
    public void clearLists()
    {
        // Reset all Lists but store Last Item for next test.
        if (geoLocations.size() > 0)
        {
            final GeoLocationItem lastLocation = geoLocations.get(geoLocations.size() - 1);
            geoLocations.clear();
            geoLocations.add(lastLocation);
        }
        else
            geoLocations.clear();
        
        if (cellLocations.size() > 0)
        {
            final CellLocationItem lastCell = cellLocations.get(cellLocations.size() - 1);
            cellLocations.clear();
            cellLocations.add(lastCell);
        }
        else
            cellLocations.clear();
        
        if (signals.size() > 0)
        {
            final SignalItem lastSignal = signals.get(signals.size() - 1);
            signals.clear();
            signals.add(lastSignal);
        }
        else
            signals.clear();
    }
    
    public void reset()
    {
        testServerName = "";
       	lastLocation = null;
        
        lastNetworkType.set(TelephonyManager.NETWORK_TYPE_UNKNOWN);
        illegalNetworkTypeChangeDetcted.set(false);
        
        // create and load default properties
        fullInfo = new Properties();
        
        fullInfo.setProperty("UUID", "");
        
        fullInfo.setProperty("PLATTFORM", "");
        fullInfo.setProperty("OS_VERSION", "");
        fullInfo.setProperty("API_LEVEL", "");
        
        fullInfo.setProperty("DEVICE", "");
        fullInfo.setProperty("MODEL", "");
        fullInfo.setProperty("PRODUCT", "");
        
        fullInfo.setProperty("CLIENT_NAME", "");
        fullInfo.setProperty("CLIENT_SOFTWARE_VERSION", "");
        
        fullInfo.setProperty("NETWORK_TYPE", "");
        
        fullInfo.setProperty("TELEPHONY_PHONE_TYPE", "");
        fullInfo.setProperty("TELEPHONY_DATA_STATE", "");
        
        fullInfo.setProperty("TELEPHONY_NETWORK_COUNTRY", "");
        fullInfo.setProperty("TELEPHONY_NETWORK_OPERATOR", "");
        fullInfo.setProperty("TELEPHONY_NETWORK_OPERATOR_NAME", "");
        
        fullInfo.setProperty("TELEPHONY_NETWORK_SIM_COUNTRY", "");
        fullInfo.setProperty("TELEPHONY_NETWORK_SIM_OPERATOR", "");
        fullInfo.setProperty("TELEPHONY_NETWORK_SIM_OPERATOR_NAME", "");
        
        fullInfo.setProperty("TELEPHONY_NETWORK_IS_ROAMING", "");
        
        fullInfo.setProperty("WIFI_SSID", "");
        fullInfo.setProperty("WIFI_BSSID", "");
        fullInfo.setProperty("WIFI_NETWORK_ID", "");
        // fullInfo.setProperty("WIFI_LINKSPEED", "");
        // fullInfo.setProperty("WIFI_RSSI", "");
        fullInfo.setProperty("WIFI_SUPPLICANT_STATE", "");
        fullInfo.setProperty("WIFI_SUPPLICANT_STATE_DETAIL", "");
        
        /*
         * fullInfo.setProperty("GEO_TIME", ""); fullInfo.setProperty("GEO_LAT",
         * ""); fullInfo.setProperty("GEO_LONG","");
         * fullInfo.setProperty("GEO_ACCURACY", "");
         * fullInfo.setProperty("GEO_ALTITUDE", "");
         * fullInfo.setProperty("GEO_BEARING", "");
         * fullInfo.setProperty("GEO_SPEED", "");
         * fullInfo.setProperty("GEO_PROVIDER", "");
         */
        
        clearLists();
    }
    
    // removes the listener
    public void unload()
    {
        
        if (locationManager != null)
        {
            // remove Location Listener
            locationManager.stop();
            locationManager = null;
        }
        
        unregisterListeners();
        
        if (connManager != null)
            connManager = null;
        
        // stop network/wifi listener
        unregisterNetworkReceiver();
        
        if (wifiManager != null)
            wifiManager = null;
        
        fullInfo = null;
    }
    
    private void getClientInfo()
    {
        final String tmpuuid = ConfigHelper.getUUID(context);
        
        if (tmpuuid == null || tmpuuid.length() == 0)
            fullInfo.setProperty("UUID", "");
        else
            fullInfo.setProperty("UUID", tmpuuid);
        
        fullInfo.setProperty("PLATTFORM", PLATTFORM_NAME);
        
        fullInfo.setProperty("OS_VERSION", android.os.Build.VERSION.RELEASE + "("
                + android.os.Build.VERSION.INCREMENTAL + ")");
        
        fullInfo.setProperty("API_LEVEL", String.valueOf(android.os.Build.VERSION.SDK_INT));
        
        fullInfo.setProperty("DEVICE", android.os.Build.DEVICE);
        
        fullInfo.setProperty("MODEL", android.os.Build.MODEL);
        
        fullInfo.setProperty("PRODUCT", android.os.Build.PRODUCT);
        
        fullInfo.setProperty("NETWORK_TYPE", String.valueOf(getNetwork()));
        
        if (connManager != null)
        {
            final NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null)
                fullInfo.setProperty("TELEPHONY_NETWORK_IS_ROAMING", String.valueOf(activeNetworkInfo.isRoaming()));
        }
        
        PackageInfo pInfo;
        String clientVersion = "";
        try
        {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            clientVersion = pInfo.versionName;
        }
        catch (final NameNotFoundException e)
        {
            // e1.printStackTrace();
            Log.e(DEBUG_TAG, "version of the application cannot be found", e);
        }
        
        fullInfo.setProperty("CLIENT_NAME", Config.RMBT_CLIENT_NAME);
        fullInfo.setProperty("CLIENT_SOFTWARE_VERSION", clientVersion);
    }
    
    public static PackageInfo getPackageInfo(Context ctx)
    {
        PackageInfo pInfo = null;
        try
        {
            pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
        }
        catch (final NameNotFoundException e)
        {
            // e1.printStackTrace();
            Log.e(DEBUG_TAG, "version of the application cannot be found", e);
        }
        return pInfo;
    }
    
    public static JSONObject fillBasicInfo(JSONObject object, Context ctx) throws JSONException
    {
        object.put("plattform", PLATTFORM_NAME);
        object.put("os_version", android.os.Build.VERSION.RELEASE + "(" + android.os.Build.VERSION.INCREMENTAL
                + ")");
        object.put("api_level", String.valueOf(android.os.Build.VERSION.SDK_INT));
        object.put("device", android.os.Build.DEVICE);
        object.put("model", android.os.Build.MODEL);
        object.put("product", android.os.Build.PRODUCT);
        object.put("language", Locale.getDefault().getLanguage());
        object.put("timezone", TimeZone.getDefault().getID());
        object.put("softwareRevision", RevisionHelper.getVerboseRevision());
        PackageInfo pInfo = getPackageInfo(ctx);
        if (pInfo != null)
        {
            object.put("softwareVersionCode", pInfo.versionCode);
            object.put("softwareVersionName", pInfo.versionName);
        }
        object.put("type", at.alladin.rmbt.android.util.Config.RMBT_CLIENT_TYPE);
        
        addClientFeatures(object, ctx);
        
        if (BASIC_INFORMATION_INCLUDE_LOCATION) {
	        Location loc = GeoLocation.getLastKnownLocation(ctx);
	        if (loc != null) {
	        JSONObject locationJson = new JSONObject();
		        locationJson.put("lat", loc.getLatitude());
		        locationJson.put("long", loc.getLongitude());
		        locationJson.put("provider", loc.getProvider());
		        if (loc.hasSpeed())
		        	locationJson.put("speed", loc.getSpeed());
		        if (loc.hasAltitude())
		        	locationJson.put("altitude", loc.getAltitude());
		        locationJson.put("age", System.currentTimeMillis() - loc.getTime()); //getElapsedRealtimeNanos() would be better, but require higher API-level
		        if (loc.hasAccuracy())
		        	locationJson.put("accuracy", loc.getAccuracy());
		        if (loc.hasSpeed())
		        	locationJson.put("speed", loc.getSpeed());
		        /*
		         *  would require API level 18
		        if (loc.isFromMockProvider())
		        	locationJson.put("mock",loc.isFromMockProvider());
		        */
		        object.put("location", locationJson);
	        }
        }
        
        InformationCollector infoCollector = null;
        
        if (ctx instanceof RMBTMainActivity) {
        	Fragment curFragment = ((RMBTMainActivity) ctx).getCurrentFragment();
            if (curFragment != null) {
	        	if (curFragment instanceof RMBTMainMenuFragment) {
	        		infoCollector = ((RMBTMainMenuFragment) curFragment).getInformationCollector();
	        	}
            }
        }

        if (BASIC_INFORMATION_INCLUDE_LAST_SIGNAL_ITEM && (infoCollector != null)) {
        	SignalItem signalItem = infoCollector.getLastSignalItem();
        	if (signalItem != null) {
        		object.put("last_signal_item", signalItem.toJson());
        	}
        	else {
        		object.put("last_signal_item", JSONObject.NULL);
        	}
        }

        return object;
    }

    private static void addClientFeatures(JSONObject object, Context ctx) throws JSONException
    {
        if (ConfigHelper.isUserServerSelectionActivated(ctx)) {
        	object.put("user_server_selection", true);
        }
        if (ConfigHelper.isDevEnabled(ctx)) {
        	object.put("developer_mode", true);
        }
        if (ConfigHelper.isUserLoopModeActivated(ctx)) {
        	object.put("user_loop_mode", true);

        }
        
    }


	public JSONObject getInitialInfo()
    {
        try
        {
            final JSONObject result = new JSONObject();
            fillBasicInfo(result, context);
            
            result.put("ndt", ConfigHelper.isNDT(context));
            
            result.put("testCounter", ConfigHelper.incAndGetNextTestCounter(context));
            result.put("previousTestStatus", ConfigHelper.getPreviousTestStatus(context));
            ConfigHelper.setPreviousTestStatus(context, null);
            
            result.put("android_permission_status", PermissionHelper.getPermissionStatusAsJSONArray(context));
            
            if (ConfigHelper.isLoopMode(context)) {
            	result.put("loopmode_info", new Gson().toJson(new LoopModeSettings(
            			ConfigHelper.getLoopModeMaxDelay(context),
            			ConfigHelper.getLoopModeMaxMovement(context),
            			ConfigHelper.getLoopModeMaxTests(context), 
            			ConfigHelper.getLoopModeTestCounter(context))));
            }

            if (ConfigHelper.isUserServerSelectionActivated(context))
            { 
                final String serverSelection = ConfigHelper.getServerSelection(context);
                if (serverSelection != null && ! serverSelection.equals(ConfigHelper.DEFAULT_SERVER)) {
                	Log.i(DEBUG_TAG, "prefer_server " + serverSelection);
                    result.put("prefer_server", serverSelection);
                }
            }
            
            return result;
        }
        catch (final JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    private void getWiFiInfo()
    {
        initNetwork();
        if (wifiManager != null)
        {
            final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            fullInfo.setProperty("WIFI_SSID",
            String.valueOf(Helperfunctions.removeQuotationsInCurrentSSIDForJellyBean(wifiInfo.getSSID())));
            		
            /*
             * fullInfo.setProperty("WIFI_LINKSPEED",
             * String.valueOf(wifiInfo.getLinkSpeed()));
             */
            fullInfo.setProperty("WIFI_BSSID", String.valueOf(wifiInfo.getBSSID()));
            fullInfo.setProperty("WIFI_NETWORK_ID", String.valueOf(wifiInfo.getNetworkId()));
            /*
             * fullInfo.setProperty("WIFI_RSSI",
             * String.valueOf(wifiInfo.getRssi()));
             */
            final SupplicantState wifiState = wifiInfo.getSupplicantState();
            fullInfo.setProperty("WIFI_SUPPLICANT_STATE", String.valueOf(wifiState.name()));
            final DetailedState wifiDetail = WifiInfo.getDetailedStateOf(wifiState);
            fullInfo.setProperty("WIFI_SUPPLICANT_STATE_DETAIL", String.valueOf(wifiDetail.name()));
            
            if (getNetwork() == NETWORK_WIFI)
            {
                
                final int rssi = wifiInfo.getRssi();
                if (rssi != -1 && rssi >= ACCEPT_WIFI_RSSI_MIN)
                {
                    int linkSpeed = wifiInfo.getLinkSpeed();
                    if (linkSpeed < 0) {
                        linkSpeed = 0;
                    }
                    
                    final SignalItem signalItem = SignalItem.getWifiSignalItem(linkSpeed, rssi);
                    if (this.collectInformation) {
                        signals.add(signalItem);	
                    }
                    lastSignalItem.set(signalItem);
                    signal.set(rssi);
                    signalType.set(SINGAL_TYPE_WLAN);
//                    Log.i(DEBUG_TAG, "Signals1: " + signals.toString());
                }
            }
        }
    }
    
    private void getTelephonyInfo()
    {
        initNetwork();
        if (telManager != null)
        {
            try
            {
                // Get Cell Location
                CellLocation.requestLocationUpdate();
            }
            catch (Exception e)
            {
                // some devices with Android 5.1 seem to throw a NPE is some cases
                e.printStackTrace();
            }
            
            final CellLocation cellLocation;
            if (haveCourseLocationPerm)
                cellLocation = telManager.getCellLocation();
            else
                cellLocation = null;
            if (cellLocation != null && (cellLocation instanceof GsmCellLocation))
            {
                final GsmCellLocation gcl = (GsmCellLocation) cellLocation;
                if (gcl.getCid() > 0 && this.collectInformation) {
                    cellLocations.add(new CellLocationItem(gcl));	
                }
            }
            
            fullInfo.setProperty("TELEPHONY_NETWORK_OPERATOR_NAME", String.valueOf(telManager.getNetworkOperatorName()));
            String networkOperator = telManager.getNetworkOperator();
            if (networkOperator != null && networkOperator.length() >= 5)
                networkOperator = String.format("%s-%s", networkOperator.substring(0, 3), networkOperator.substring(3));
            fullInfo.setProperty("TELEPHONY_NETWORK_OPERATOR", String.valueOf(networkOperator));
            fullInfo.setProperty("TELEPHONY_NETWORK_COUNTRY", String.valueOf(telManager.getNetworkCountryIso()));
            fullInfo.setProperty("TELEPHONY_NETWORK_SIM_COUNTRY", String.valueOf(telManager.getSimCountryIso()));
            String simOperator = telManager.getSimOperator();
            if (simOperator != null && simOperator.length() >= 5)
                simOperator = String.format("%s-%s", simOperator.substring(0, 3), simOperator.substring(3));
            fullInfo.setProperty("TELEPHONY_NETWORK_SIM_OPERATOR", String.valueOf(simOperator));
            
            try // hack for Motorola Defy (#594)
            {
                fullInfo.setProperty("TELEPHONY_NETWORK_SIM_OPERATOR_NAME", String.valueOf(telManager.getSimOperatorName()));
            }
            catch (SecurityException e)
            {
                e.printStackTrace();
                fullInfo.setProperty("TELEPHONY_NETWORK_SIM_OPERATOR_NAME", "s.exception");
            }
            
            fullInfo.setProperty("TELEPHONY_PHONE_TYPE", String.valueOf(telManager.getPhoneType()));
            
            try // some devices won't allow this w/o READ_PHONE_STATE. conflicts with Android API doc
            {
                fullInfo.setProperty("TELEPHONY_DATA_STATE", String.valueOf(telManager.getDataState()));
            }
            catch (SecurityException e)
            {
                e.printStackTrace();
                fullInfo.setProperty("TELEPHONY_DATA_STATE", "s.exception");
            }
            
            try
            {
                final String dualSimDetectionMethod = DualSimDetector.getDualSIM(context);
                fullInfo.setProperty("DUAL_SIM", String.valueOf(dualSimDetectionMethod != null));
                if (dualSimDetectionMethod != null)
                    fullInfo.setProperty("DUAL_SIM_DETECTION_METHOD", dualSimDetectionMethod);
                else
                    fullInfo.remove("DUAL_SIM_DETECTION_METHOD");
            }
            catch (Exception e) // never fail b/c of dual sim detection
            {
            }
            
            // telManager.listen(telListener,
            // PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
    }
    
    public Location getLocationInfo()
    {
        if (enableGeoLocation) {
	        if (locationManager == null)
	        {
	            // init location Manager
	            locationManager = new InfoGeoLocation(context);
	            locationManager.start();
	        }
	        final Location curLocation = locationManager.getLastKnownLocation();
	        
	        if (curLocation != null && this.collectInformation)
	        {
	            geoLocations.add(new GeoLocationItem(curLocation.getTime(), curLocation.getLatitude(), curLocation
	                    .getLongitude(), curLocation.getAccuracy(), curLocation.getAltitude(), curLocation.getBearing(),
	                    curLocation.getSpeed(), curLocation.getProvider()));
	            Log.i(DEBUG_TAG, "Location: " + curLocation.toString());
	        }
	        
	        return curLocation;
        }
        else {
        	return null;
        }
        
    }

    
    // public boolean setInfo(String key, String value) {
    // if (fullInfo.containsKey(key)) {
    // fullInfo.setProperty(key, value);
    // return true;
    // } else
    // return false;
    // }
    
    public String getInfo(final String key)
    {
        String value = "";
        if (fullInfo.containsKey(key))
            value = fullInfo.getProperty(key);
        return value;
    }
    
    public String getUUID()
    {
        return fullInfo.getProperty("UUID");
    }
    
    public void setUUID(final String uuid)
    {
        if (uuid != null && uuid.length() != 0)
        {
            fullInfo.setProperty("UUID", uuid);
            ConfigHelper.setUUID(context, uuid);
        }
    }
    
    public String getOperatorName()
    {
    	int network = getNetwork();
    	
    	if (fullInfo != null) {
	        if (network == NETWORK_WIFI)
	            return fullInfo.getProperty("WIFI_SSID");
	        else if (network == NETWORK_ETHERNET)
	        	return "Ethernet";
	        else if (network == NETWORK_BLUETOOTH)
	        	return "Bluetooth";
	        else {
	        	String TelephonyNetworkOperator = fullInfo.getProperty("TELEPHONY_NETWORK_OPERATOR");
	        	String TelephonyNetworkOperatorName = fullInfo.getProperty("TELEPHONY_NETWORK_OPERATOR_NAME");
	        	if (TelephonyNetworkOperator.length() == 0 && TelephonyNetworkOperatorName.length() == 0)
	        		return "-";
	        	else if (TelephonyNetworkOperator.length() == 0)
	        		return TelephonyNetworkOperatorName;
	        	else if (TelephonyNetworkOperatorName.length() == 0)
	        		return TelephonyNetworkOperator;
	        	else 
	        		return String.format("%s (%s)", TelephonyNetworkOperatorName, TelephonyNetworkOperator);
	        }
    	}

    	return "-";
    }
    
    public ArrayList<String> getCurLocation()
    {
        
        if (geoLocations.size() > 0)
        {
            final int pos = geoLocations.size() - 1;
            final GeoLocationItem curLocation = geoLocations.get(pos);
            
            final ArrayList<String> geoInfo = new ArrayList<String>(Arrays.asList(String.valueOf(curLocation.tstamp),
                    String.valueOf(curLocation.geo_lat), String.valueOf(curLocation.geo_long),
                    String.valueOf(curLocation.accuracy), String.valueOf(curLocation.altitude),
                    String.valueOf(curLocation.bearing), String.valueOf(curLocation.speed), curLocation.provider));
            
            return geoInfo;
        }
        else
            return null;
    }
    
    public JSONObject getResultValues(long startTimestampNs) throws JSONException
    {
        
        final JSONObject result = new JSONObject();
        
        final Enumeration<?> pList = fullInfo.propertyNames();
        
        final int network = getNetwork();
        while (pList.hasMoreElements())
        {
            final String key = (String) pList.nextElement();
            boolean add = true;
            if (network == NETWORK_WIFI)
            {
                if (key.startsWith("TELEPHONY_")) // no mobile data if wifi
                    add = false;
            }
            else if (key.startsWith("WIFI_")) // no wifi data if mobile
                add = false;
            if ((network == NETWORK_ETHERNET || network == NETWORK_BLUETOOTH)  &&
            		(key.startsWith("TELEPHONY_") || key.startsWith("WIFI_"))  ) // add neither mobile nor wifi data 
            	add = false;
            if (add)
                result.put(key.toLowerCase(Locale.US), fullInfo.getProperty(key));
        }
        
        if (geoLocations.size() > 0)
        {
            
            final JSONArray itemList = new JSONArray();
            
            for (int i = 0; i < geoLocations.size(); i++)
            {
                
                final GeoLocationItem tmpItem = geoLocations.get(i);
                
                final JSONObject jsonItem = new JSONObject();
                
                jsonItem.put("tstamp", tmpItem.tstamp);
                jsonItem.put("time_ns", tmpItem.tstampNano - startTimestampNs);
                jsonItem.put("geo_lat", tmpItem.geo_lat);
                jsonItem.put("geo_long", tmpItem.geo_long);
                jsonItem.put("accuracy", tmpItem.accuracy);
                jsonItem.put("altitude", tmpItem.altitude);
                jsonItem.put("bearing", tmpItem.bearing);
                jsonItem.put("speed", tmpItem.speed);
                jsonItem.put("provider", tmpItem.provider);
                
                itemList.put(jsonItem);
            }
            
            result.put("geoLocations", itemList);
            
        }
        
        if (cellLocations.size() > 0)
        {
            
            final JSONArray itemList = new JSONArray();
            
            for (int i = 0; i < cellLocations.size(); i++)
            {
                
                final CellLocationItem tmpItem = cellLocations.get(i);
                
                final JSONObject jsonItem = new JSONObject();
                

                jsonItem.put("time", tmpItem.tstamp); //add for backward compatibility
                jsonItem.put("time_ns", tmpItem.tstampNano - startTimestampNs);
                jsonItem.put("location_id", tmpItem.locationId);
                Log.i(DEBUG_TAG, "Cell ID:" + tmpItem.locationId);
                jsonItem.put("area_code", tmpItem.areaCode);
                jsonItem.put("primary_scrambling_code", tmpItem.scramblingCode);
                Log.i(DEBUG_TAG, "Scrambling Code:" + tmpItem.scramblingCode);
                itemList.put(jsonItem);
            }
            
            result.put("cellLocations", itemList);
        }
        
        //Log.i(DEBUG_TAG, "Signals: " + signals.toString());
        
        if (signals.size() > 0)
        {
            
            final JSONArray itemList = new JSONArray();
            
            for (int i = 0; i < signals.size(); i++)
            {
                final SignalItem tmpItem = signals.get(i);
                
                final JSONObject jsonItem = tmpItem.toJson();
                jsonItem.put("time_ns", tmpItem.tstampNano - startTimestampNs);                
                itemList.put(jsonItem);
            }
            
            result.put("signals", itemList);
        }
        
        final String tag = ConfigHelper.getTag(context);
        if (tag != null && ! tag.isEmpty())
            result.put("tag", tag);
        
        addClientFeatures(result, context);
        
        result.put("android_permission_status", PermissionHelper.getPermissionStatusAsJSONArray(context));
        
        return result;
    }
    
    /**
     * Lazily initializes the network managers.
     * 
     * As a side effect, assigns connectivityManager and telephonyManager.
     */
    private synchronized void initNetwork()
    {
        if (connManager == null)
        {
            final ConnectivityManager tryConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            
            final TelephonyManager tryTelephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            
            final WifiManager tryWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            
            // Assign to member vars only after all the get calls succeeded,
            
            connManager = tryConnectivityManager;
            telManager = tryTelephonyManager;
            wifiManager = tryWifiManager;
            
            // Some interesting info to look at in the logs
            //final NetworkInfo[] infos = connManager.getAllNetworkInfo();
            //for (final NetworkInfo networkInfo : infos)
            //    Log.i(DEBUG_TAG, "Network: " + networkInfo);
        }
        assert connManager != null;
        assert telManager != null;
        assert wifiManager != null;
    }
    
    /** Returns the network that the phone is on (e.g. Wifi, Edge, GPRS, etc). */
    public int getNetwork()
    {
        int result = TelephonyManager.NETWORK_TYPE_UNKNOWN;
        
        if (connManager != null)
        {
            final NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null)
            {
                final int type = activeNetworkInfo.getType();
                switch (type)
                {
                case ConnectivityManager.TYPE_WIFI:
                    result = NETWORK_WIFI;
                    break;
                    
                case ConnectivityManager.TYPE_BLUETOOTH:
                    result = NETWORK_BLUETOOTH;
                    break;
                    
                case ConnectivityManager.TYPE_ETHERNET:
                    result = NETWORK_ETHERNET;
                    break;
                    
                case ConnectivityManager.TYPE_MOBILE:
                case ConnectivityManager.TYPE_MOBILE_DUN:
                case ConnectivityManager.TYPE_MOBILE_HIPRI:
                case ConnectivityManager.TYPE_MOBILE_MMS:
                case ConnectivityManager.TYPE_MOBILE_SUPL:
                    result = telManager.getNetworkType();
                    break;
                }
            }
        }
        
        /* detect change from wifi to mobile or reverse */
        final int lastNetworkType = this.lastNetworkType.get();
        if (result != TelephonyManager.NETWORK_TYPE_UNKNOWN && lastNetworkType != TelephonyManager.NETWORK_TYPE_UNKNOWN)
        {
            if (
                (result == NETWORK_WIFI && lastNetworkType != NETWORK_WIFI)
                    ||
                (result != NETWORK_WIFI && lastNetworkType == NETWORK_WIFI)
                )
                illegalNetworkTypeChangeDetcted.set(true);
        }
        if (result != lastNetworkType)
        {
            this.lastNetworkType.set(result);
            if (telListener != null)
                telListener.onSignalStrengthsChanged(null);
        }
            
        return result;
    }
    
    public boolean getIllegalNetworkTypeChangeDetcted()
    {
        return illegalNetworkTypeChangeDetcted.get();
    }
    
    /*
     * private static final String[] NETWORK_TYPES = {
     * 
     * "UNKNOWN", // 0 - NETWORK_TYPE_UNKNOWN OR NONE "GSM", // 1 -
     * NETWORK_TYPE_GPRS "EDGE", // 2 - NETWORK_TYPE_EDGE "UMTS", // 3 -
     * NETWORK_TYPE_UMTS "CDMA", // 4 - NETWORK_TYPE_CDMA "EVDO_0", // 5 -
     * NETWORK_TYPE_EVDO_0 "EVDO_A", // 6 - NETWORK_TYPE_EVDO_A "1xRTT", // 7 -
     * NETWORK_TYPE_1xRTT "HSDPA", // 8 - NETWORK_TYPE_HSDPA "HSUPA", // 9 -
     * NETWORK_TYPE_HSUPA "HSPA", // 10 - NETWORK_TYPE_HSPA "IDEN", // 11 -
     * NETWORK_TYPE_IDEN "EVDO_B", // 12 - NETWORK_TYPE_EVDO_B "LTE", // 13 -
     * NETWORK_TYPE_LTE "EHRPD", // 14 - NETWORK_TYPE_EHRPD "HSPA+", //15 -
     * NETWORK_TYPE_HSPAP };
     */
    
    /** Returns mobile data network connection type. */
    /*
     * private int getTelephonyNetworkType() { //assert
     * NETWORK_TYPES[14].compareTo("EHRPD") == 0;
     * 
     * int networkType = telManager.getNetworkType(); if (networkType <
     * NETWORK_TYPES.length) {
     * 
     * } else { return 0; } }
     */
    
    // Listeners
    private void registerListeners()
    {
        initNetwork();
        
        if (telListener == null)
        {
            telListener = new TelephonyStateListener();
            
            int events = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
            
            if (haveCourseLocationPerm)
                events |= PhoneStateListener.LISTEN_CELL_LOCATION;
                
            telManager.listen(telListener, events);
        }
    }
    
    private void unregisterListeners()
    {
        Log.d(DEBUG_TAG, "unregistering listener");
        
        if (telManager != null)
        {
            telManager.listen(telListener, PhoneStateListener.LISTEN_NONE);
            telListener = null;
            telManager = null;
        }
    }
    
    private void registerNetworkReceiver()
    {
        if (networkReceiver == null && registerNetworkReiceiver)
        {
            networkReceiver = new NetworkStateBroadcastReceiver();
            IntentFilter intentFilter;
            intentFilter = new IntentFilter();
            // intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
            Log.d(DEBUG_TAG, "registering receiver");
            context.registerReceiver(networkReceiver, intentFilter);
        }
    }
    
    private void unregisterNetworkReceiver()
    {
        Log.d(DEBUG_TAG, "unregistering receiver");
        if (networkReceiver != null)
            context.unregisterReceiver(networkReceiver);
        networkReceiver = null;
    }
    
    public Integer getSignal()
    {
        final int _signal = signal.get();
        if (_signal == Integer.MIN_VALUE)
            return null;
        return _signal;
    }
    
    public Integer getSignalRsrq() {
        final int _signal = signalRsrq.get();
        if (_signal == Integer.MIN_VALUE)
            return null;
        return _signal;    	
    }
    
    public int getSignalType()
    {
        return signalType.get();
    }
    
    public SignalItem getLastSignalItem() {
    	return lastSignalItem.get();
    }
    
    public void setTestServerName(final String serverName)
    {
        testServerName = serverName;
    }
    
    public String getTestServerName()
    {
        return testServerName;
    }
    
    /**
     * Listener + recorder for mobile or wifi updates
     */
    private class NetworkStateBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(final Context context, final Intent intent)
        {
            final String action = intent.getAction();
            
            if (action.equals(WifiManager.RSSI_CHANGED_ACTION))
            {
                Log.d(DEBUG_TAG, "Wifi RSSI changed");
                
                if (getNetwork() == NETWORK_WIFI)
                {
                    final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    final int rssi = wifiInfo.getRssi();
                    if (rssi != -1 && rssi >= ACCEPT_WIFI_RSSI_MIN)
                    {
                    	final SignalItem signalItem = SignalItem.getWifiSignalItem(wifiInfo.getLinkSpeed(), rssi);
                    	if (InformationCollector.this.collectInformation) {
                            signals.add(signalItem);	
                    	}
                    	lastSignalItem.set(signalItem);
                        signal.set(rssi);
                        signalType.set(SINGAL_TYPE_WLAN);
                    }
                }
                
            }
        }
    }
    
    public class TelephonyStateListener extends PhoneStateListener
    {
        @Override
        public void onSignalStrengthsChanged(final SignalStrength signalStrength)
        {
            //Log.d(DEBUG_TAG, "SignalStrength changed");
            if (signalStrength != null)
                Log.d(DEBUG_TAG, signalStrength.toString());
            final int network = getNetwork();
            int strength = UNKNOWN;
            int lteRsrp = UNKNOWN;
            int lteRsrq = UNKNOWN;
            int lteRsssnr = UNKNOWN;
            int lteCqi = UNKNOWN;
            int errorRate = UNKNOWN;
            
            
            // discard signal strength from GT-I9100G (Galaxy S II) - passes wrong info
            if (android.os.Build.MODEL != null)
            {
                if (android.os.Build.MODEL.equals("GT-I9100G")
                    ||
                    android.os.Build.MODEL.equals("HUAWEI P2-6011"))
                return;
            }
            
            if (network != NETWORK_WIFI && network != NETWORK_BLUETOOTH && network != NETWORK_ETHERNET)
            {
                if (signalStrength != null)
                {
                    if (network == TelephonyManager.NETWORK_TYPE_CDMA)
                        strength = signalStrength.getCdmaDbm();
                    else if (network == TelephonyManager.NETWORK_TYPE_EVDO_0
                            || network == TelephonyManager.NETWORK_TYPE_EVDO_A
                    /* || network == TelephonyManager.NETWORK_TYPE_EVDO_B */)
                        strength = signalStrength.getEvdoDbm();
                    else if (network == 13) /* TelephonyManager.NETWORK_TYPE_LTE ; not avail in api 8 */
                    {
                        try
                        {
                            lteRsrp = (Integer) SignalStrength.class.getMethod("getLteRsrp").invoke(signalStrength);
                            lteRsrq = (Integer) SignalStrength.class.getMethod("getLteRsrq").invoke(signalStrength);
                            lteRsssnr = (Integer) SignalStrength.class.getMethod("getLteRssnr").invoke(signalStrength);
                            lteCqi = (Integer) SignalStrength.class.getMethod("getLteCqi").invoke(signalStrength);
                            
                            if (lteRsrp == Integer.MAX_VALUE)
                                lteRsrp = UNKNOWN;
                            if (lteRsrq == Integer.MAX_VALUE)
                                lteRsrq = UNKNOWN;
                            if (lteRsrq > 0)
                                lteRsrq = -lteRsrq; // fix invalid rsrq values for some devices (see #996)
                            if (lteRsssnr == Integer.MAX_VALUE)
                                lteRsssnr = UNKNOWN;
                            if (lteCqi == Integer.MAX_VALUE)
                                lteCqi = UNKNOWN;
                        }
                        catch (Throwable t)
                        {
                            t.printStackTrace();
                        }
                    }
                    else if (signalStrength.isGsm())
                    {
                        try
                        {
                            final Method getGsmDbm = SignalStrength.class.getMethod("getGsmDbm");
                            final Integer result = (Integer) getGsmDbm.invoke(signalStrength);
                            if (result != -1)
                                strength = result;
                        }
                        catch (Throwable t)
                        {   
                        }
                        if (strength == UNKNOWN)
                        {   // fallback if not implemented
                            int dBm;
                            int gsmSignalStrength = signalStrength.getGsmSignalStrength();
                            int asu = (gsmSignalStrength == 99 ? -1 : gsmSignalStrength);
                            if (asu != -1)
                                dBm = -113 + (2 * asu);
                            else
                                dBm = UNKNOWN;
                            strength = dBm;
                        }
                        errorRate = signalStrength.getGsmBitErrorRate();
                    }
                    if (lteRsrp != UNKNOWN)
                    {
                        signal.set(lteRsrp);
                        signalType.set(SINGAL_TYPE_RSRP);
                    }
                    else
                    {
                        signal.set(strength);
                        signalType.set(SINGAL_TYPE_MOBILE);
                    }
                    
                    signalRsrq.set(lteRsrq);
                }
                
                final SignalItem lastSignal = lastSignalItem.get();
                if (strength == UNKNOWN && lastSignal != null && lastSignal.networkId != NETWORK_WIFI 
                		&& lastSignal.signalStrength != strength && lastSignal.signalStrength != UNKNOWN) {
                	strength = lastSignal.signalStrength;
                }
                
                final SignalItem signalItem = SignalItem.getCellSignalItem(network, strength, errorRate, lteRsrp, lteRsrq, lteRsssnr, lteCqi);
                if (InformationCollector.this.collectInformation) {
                    signals.add(signalItem);	
                }
                                
                lastSignalItem.set(signalItem);
            }
        }
        
        @Override
        public void onCellLocationChanged(final CellLocation location)
        {
            GsmCellLocation cellLocation = null;
            if (telManager != null)
            {
                final CellLocation _cellLocation = telManager.getCellLocation();
                if (_cellLocation instanceof GsmCellLocation)
                    cellLocation = (GsmCellLocation) _cellLocation;
            }
            
            if (cellLocation != null && cellLocation.getCid() > 0 && InformationCollector.this.collectInformation)
                cellLocations.add(new CellLocationItem(cellLocation));
        }
    }
    
    public Location getLastLocation()
    {
        return lastLocation;
    }
    
    private class InfoGeoLocation extends GeoLocation
    {
        
        public InfoGeoLocation(final Context context)
        {
            super(context, ConfigHelper.isGPS(context));
        }
        
        @Override
        public void onLocationChanged(final Location curLocation)
        {
            if (curLocation != null)
            {
            	//System.out.println("onLocationChanged..");
                lastLocation = curLocation;
                
                if (InformationCollector.this.collectInformation) {
                    geoLocations.add(new GeoLocationItem(curLocation.getTime(), curLocation.getLatitude(), curLocation
                            .getLongitude(), curLocation.getAccuracy(), curLocation.getAltitude(),
                            curLocation.getBearing(), curLocation.getSpeed(), curLocation.getProvider()));                	
                }
            }
        }
    }
    
    private class GeoLocationItem
    {
        
        public final long tstamp;
        public final long tstampNano;
        public final double geo_lat;
        public final double geo_long;
        public final float accuracy;
        public final double altitude;
        public final float bearing;
        public final float speed;
        public final String provider;
        
        public GeoLocationItem(final long tstamp, final double geo_lat, final double geo_long, final float accuracy,
                final double altitude, final float bearing, final float speed, final String provider)
        {
            this.tstamp = tstamp;
            this.tstampNano = System.nanoTime();
            this.geo_lat = geo_lat;
            this.geo_long = geo_long;
            this.accuracy = accuracy;
            this.altitude = altitude;
            this.bearing = bearing;
            this.speed = speed;
            this.provider = provider;
        }
        
    }
    
    private class CellLocationItem
    {
        
        public final long tstamp;
        public final long tstampNano;
        public final int locationId;
        public final int areaCode;
        public final int scramblingCode;
        
        public CellLocationItem(final GsmCellLocation cellLocation)
        {
            
            tstamp = System.currentTimeMillis();
            tstampNano = System.nanoTime();
            locationId = cellLocation.getCid();
            areaCode = cellLocation.getLac();
            
            final Class<?> cellClass = cellLocation.getClass();
            
            int tmpCode = -1;
            
            Method pscMethod;
            
            try
            {
                pscMethod = cellClass.getMethod("getPsc");
                final Integer result = (Integer) pscMethod.invoke(cellLocation, (Object[]) null);
                tmpCode = result.intValue();
            }
            catch (final SecurityException e)
            {
                Log.i(DEBUG_TAG, "CellLocationItem scramblingCode failed Security");
            }
            catch (final NoSuchMethodException e)
            {
                Log.i(DEBUG_TAG, "CellLocationItem scramblingCode failed NoSuchMethod");
            }
            catch (final IllegalArgumentException e)
            {
                Log.i(DEBUG_TAG, "CellLocationItem scramblingCode failed IllegalArgument");
            }
            catch (final IllegalAccessException e)
            {
                Log.i(DEBUG_TAG, "CellLocationItem scramblingCode failed IllegalAccess");
            }
            catch (final InvocationTargetException e)
            {
                Log.i(DEBUG_TAG, "CellLocationItem scramblingCode failed InvocationTarget");
            }
            
            scramblingCode = tmpCode;
        }
        
    }
    
    private static class SignalItem
    {
        
        public final long tstamp;
        public final int networkId;
        public final int signalStrength;
        public final int gsmBitErrorRate;
        public final int wifiLinkSpeed;
        public final int wifiRssi;
        
        public final int lteRsrp;
        public final int lteRsrq;
        public final int lteRssnr;
        public final int lteCqi;
        public final long tstampNano; 
        
        public static SignalItem getWifiSignalItem(final int wifiLinkSpeed, final int wifiRssi)
        {
            return new SignalItem(NETWORK_WIFI, UNKNOWN, UNKNOWN, wifiLinkSpeed, wifiRssi, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN);
        }
        
        public static SignalItem getCellSignalItem(final int networkId, final int signalStrength, final int gsmBitErrorRate,
                final int lteRsrp, final int lteRsrq, final int lteRssnr, final int lteCqi)
        {
            return new SignalItem(networkId, signalStrength, gsmBitErrorRate, UNKNOWN, UNKNOWN, lteRsrp, lteRsrq, lteRssnr, lteCqi);
        }
        
        private SignalItem(final int networkId, final int signalStrength, final int gsmBitErrorRate,
                final int wifiLinkSpeed, final int wifiRssi, final int lteRsrp,
                final int lteRsrq, final int lteRssnr, final int lteCqi)
        {
            tstamp = System.currentTimeMillis();
            tstampNano = System.nanoTime();
            this.networkId = networkId;
           	this.signalStrength = signalStrength;
            this.gsmBitErrorRate = gsmBitErrorRate;
            this.wifiLinkSpeed = wifiLinkSpeed;
            this.wifiRssi = wifiRssi;
            this.lteRsrp = lteRsrp;
            this.lteRsrq = lteRsrq;
            this.lteRssnr = lteRssnr;
            this.lteCqi = lteCqi;
        }
        
        public JSONObject toJson() throws JSONException {
            final JSONObject jsonItem = new JSONObject();
            
            jsonItem.put("time", tstamp); //add for backward compatibility
            jsonItem.put("network_type_id", networkId);
            if (signalStrength != UNKNOWN) {
                jsonItem.put("signal_strength", signalStrength);
            }
            if (gsmBitErrorRate != UNKNOWN) {
                jsonItem.put("gsm_bit_error_rate", gsmBitErrorRate);
            }
            if (wifiLinkSpeed != UNKNOWN) {
                jsonItem.put("wifi_link_speed", wifiLinkSpeed);
            }
            if (wifiRssi != UNKNOWN) {
                jsonItem.put("wifi_rssi", wifiRssi);
            }
            if (lteRsrp != UNKNOWN) {
                jsonItem.put("lte_rsrp", lteRsrp);
            }
            if (lteRsrq != UNKNOWN) {
                jsonItem.put("lte_rsrq", lteRsrq);
            }
            if (lteRssnr != UNKNOWN) {
                jsonItem.put("lte_rssnr", lteRssnr);
            }
            if (lteCqi != UNKNOWN) {
                jsonItem.put("lte_cqi", lteCqi);
            }
            
            return jsonItem;
        }

		@Override
		public String toString() {
			return "SignalItem [tstamp=" + tstamp + ", networkId=" + networkId
					+ ", signalStrength=" + signalStrength
					+ ", gsmBitErrorRate=" + gsmBitErrorRate
					+ ", wifiLinkSpeed=" + wifiLinkSpeed + ", wifiRssi="
					+ wifiRssi + ", lteRsrp=" + lteRsrp + ", lteRsrq="
					+ lteRsrq + ", lteRssnr=" + lteRssnr + ", lteCqi=" + lteCqi
					+ ", tstampNano=" + tstampNano + "]";
		}        
    }
}
