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
package at.alladin.rmbt.android.main;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PixelFormat;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.help.RMBTHelpFragment;
import at.alladin.rmbt.android.history.RMBTHistoryFragment;
import at.alladin.rmbt.android.history.RMBTHistoryPagerFragment;
import at.alladin.rmbt.android.history.RMBTTestResultDetailFragment;
import at.alladin.rmbt.android.map.MapListEntry;
import at.alladin.rmbt.android.map.MapListSection;
import at.alladin.rmbt.android.map.MapProperties;
import at.alladin.rmbt.android.map.RMBTMapFragment;
import at.alladin.rmbt.android.sync.RMBTSyncFragment;
import at.alladin.rmbt.android.terms.RMBTNDTCheckFragment;
import at.alladin.rmbt.android.terms.RMBTTermsCheckFragment;
import at.alladin.rmbt.android.test.RMBTService;
import at.alladin.rmbt.android.test.RMBTTestFragment;
import at.alladin.rmbt.android.util.AppSettings;
import at.alladin.rmbt.android.util.CheckHistoryTask;
import at.alladin.rmbt.android.util.CheckNewsTask;
import at.alladin.rmbt.android.util.CheckSettingsTask;
import at.alladin.rmbt.android.util.Config;
import at.alladin.rmbt.android.util.ConfigHelper;
import at.alladin.rmbt.android.util.EndTaskListener;
import at.alladin.rmbt.android.util.GeoLocation;
import at.alladin.rmbt.android.util.GetMapOptionsInfoTask;
import at.alladin.rmbt.android.util.Helperfunctions;

/**
 * 
 * @author
 * 
 */
public class RMBTMainActivity extends FragmentActivity implements MapProperties
{
    
    /**
	 * 
	 */
    private static final String DEBUG_TAG = "RMBTMainActivity";
    
    /**
	 * 
	 */
    private FragmentManager fm;
    
    /**
	 * 
	 */
    private GeoLocation geoLocation;
    
    /**
	 * 
	 */
    private CheckNewsTask newsTask;
    
    /**
	 * 
	 */
    private CheckSettingsTask settingsTask;
    
    /**
     * 
     */
    private GetMapOptionsInfoTask getMapOptionsInfoTask;
    
    /**
     * 
     */
    private CheckHistoryTask historyTask;
    
    /**
     * 
     */
    private String historyFilterDevices[];
    
    /**
	 * 
	 */
    private String historyFilterNetworks[];
    
    /**
	 * 
	 */
    private ArrayList<String> historyFilterDevicesFilter;
    
    /**
	 * 
	 */
    private ArrayList<String> historyFilterNetworksFilter;
    
    /**
	 * 
	 */
    private final ArrayList<Map<String, String>> historyItemList = new ArrayList<Map<String, String>>();
    
    /**
	 * 
	 */
    private final ArrayList<Map<String, String>> historyStorageList = new ArrayList<Map<String, String>>();
    
    /**
     * 
     */
    private int historyResultLimit;
    
    /**
	 * 
	 */
    private final HashMap<String, String> currentMapOptions = new HashMap<String, String>();
    
    /**
	 * 
	 */
    private HashMap<String, String> currentMapOptionTitles = new HashMap<String, String>();
    
    /**
	 * 
	 */
    private ArrayList<MapListSection> mapTypeListSectionList;
    
    /**
	 * 
	 */
    private HashMap<String,List<MapListSection>> mapFilterListSectionListMap;
    
    private MapListEntry currentMapType;
    
    // /
    
    /**
	 * 
	 */
    private IntentFilter mNetworkStateChangedFilter;
    
    /**
	 * 
	 */
    private BroadcastReceiver mNetworkStateIntentReceiver;
    
    // /
    
    private boolean mapTypeSatellite;
    
    /**
	 * 
	 */
    private boolean historyDirty = true;
    
    /**
     * 
     */
    private int mapOverlayType = MapProperties.MAP_OVERLAY_TYPE_AUTO;
    
    /**
     * 
     */
    private boolean mapFirstRun = true;
    
    private ProgressDialog loadingDialog; 
    
    /**
     * 
     */
    private void preferencesUpdate()
    {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        final Context context = getApplicationContext();
        final PackageInfo pInfo;
        final int clientVersion;
        try
        {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            clientVersion = pInfo.versionCode;
            
            final int lastVersion = preferences.getInt("LAST_VERSION_CODE", -1);
            if (lastVersion == -1 || lastVersion <= 17)
            {
                preferences.edit().clear().commit();
                Log.d(DEBUG_TAG, "preferences cleared");
            }
            
            // migrate uuid
            if (lastVersion <= 51)
            {
                final String uuid = ConfigHelper.getUUID(context);
                if (uuid == null || uuid.length() == 0)
                {
                    // try to read old uuid
                    final AppSettings appSettings = AppSettings.getInstance(context);
                    final String oldUUID = appSettings.getUUID();
                    if (oldUUID != null && oldUUID.length() > 0)
                        ConfigHelper.setUUID(context, oldUUID);
                    appSettings.removeFile();
                }
            }
            
            if (lastVersion != clientVersion)
                preferences.edit().putInt("LAST_VERSION_CODE", clientVersion).commit();
        }
        catch (final NameNotFoundException e)
        {
            Log.e(DEBUG_TAG, "version of the application cannot be found", e);
        }
    }
    
    /**
	 * 
	 */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        restoreInstance(savedInstanceState);
        super.onCreate(savedInstanceState);
        preferencesUpdate();
        
        setContentView(R.layout.main);
        
        // Do something against banding effect in gradients
        // Dither flag might mess up on certain devices??
        final Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
        window.addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        
        // Setzt Default-Werte, wenn noch keine Werte vorhanden
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        final String uuid = ConfigHelper.getUUID(getApplicationContext());
        
        fm = getSupportFragmentManager();
        final Fragment fragment = fm.findFragmentById(R.id.fragment_content);
        if (! ConfigHelper.isTCAccepted(this))
        {
            if (fragment != null && fm.getBackStackEntryCount() >= 1)
                // clear fragment back stack
                fm.popBackStack(fm.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            
            showTermsCheck();
        }
        else
        {
            currentMapOptions.put("highlight", uuid);
            if (fragment == null)
            {
                if (! ConfigHelper.isNDTDecisionMade(this))
                {
                    showTermsCheck();
                    showNdtCheck();
                }
                else
                    initApp(true);
            }
        }
        
        geoLocation = new MainGeoLocation(getApplicationContext());
        
        // final Context applicationContext = getApplicationContext();
        
        // LinearLayout overlay = (LinearLayout) findViewById(R.id.overlay);
        
        mNetworkStateChangedFilter = new IntentFilter();
        mNetworkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        
        mNetworkStateIntentReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(final Context context, final Intent intent)
            {
                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
                {
                    
                    final boolean connected = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                    
                    if (connected)
                        hideInternetWarning();
                    else
                        showInternetWarning();
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
    protected void restoreInstance(Bundle b)
    {
        if (b == null)
            return;
        historyFilterDevices = (String[]) b.getSerializable("historyFilterDevices");
        historyFilterNetworks = (String[]) b.getSerializable("historyFilterNetworks");
        historyFilterDevicesFilter = (ArrayList<String>) b.getSerializable("historyFilterDevicesFilter");
        historyFilterNetworksFilter = (ArrayList<String>) b.getSerializable("historyFilterNetworksFilter");
        historyItemList.clear();
        historyItemList.addAll((ArrayList<Map<String, String>>) b.getSerializable("historyItemList"));
        historyStorageList.clear();
        historyStorageList.addAll((ArrayList<Map<String, String>>) b.getSerializable("historyStorageList"));
        historyResultLimit = b.getInt("historyResultLimit");
        currentMapOptions.clear();
        currentMapOptions.putAll((HashMap<String, String>) b.getSerializable("currentMapOptions"));
        currentMapOptionTitles = (HashMap<String, String>) b.getSerializable("currentMapOptionTitles");
        mapTypeListSectionList = (ArrayList<MapListSection>) b.getSerializable("mapTypeListSectionList");
        mapFilterListSectionListMap = (HashMap<String,List<MapListSection>>) b.getSerializable("mapFilterListSectionListMap");
        currentMapType = (MapListEntry) b.getSerializable("currentMapType");
    }
    
    @Override
    protected void onSaveInstanceState(Bundle b)
    {
        super.onSaveInstanceState(b);
        b.putSerializable("historyFilterDevices", historyFilterDevices);
        b.putSerializable("historyFilterNetworks", historyFilterNetworks);
        b.putSerializable("historyFilterDevicesFilter", historyFilterDevicesFilter);
        b.putSerializable("historyFilterNetworksFilter", historyFilterNetworksFilter);
        b.putSerializable("historyItemList", historyItemList);
        b.putSerializable("historyStorageList", historyStorageList);
        b.putInt("historyResultLimit", historyResultLimit);
        b.putSerializable("currentMapOptions", currentMapOptions);
        b.putSerializable("currentMapOptionTitles", currentMapOptionTitles);
        b.putSerializable("mapTypeListSectionList", mapTypeListSectionList);
        b.putSerializable("mapFilterListSectionListMap", mapFilterListSectionListMap);
        b.putSerializable("currentMapType", currentMapType);
    }
    
    /**
	 * 
	 */
    @Override
    public void onStart()
    {
        super.onStart();
        
        registerReceiver(mNetworkStateIntentReceiver, mNetworkStateChangedFilter);
        // init location Manager
        
        if (ConfigHelper.isTCAccepted(this) && ConfigHelper.isNDTDecisionMade(this))
            geoLocation.start();
        
    }
    
    /**
	 * 
	 */
    @Override
    public void onStop()
    {
        super.onStop();
        stopBackgroundProcesses();
        unregisterReceiver(mNetworkStateIntentReceiver);
    }
    
    /**
	 * 
	 */
    private void showInternetWarning()
    {
        final LinearLayout overlay = (LinearLayout) findViewById(R.id.overlay);
        overlay.setVisibility(View.VISIBLE);
        overlay.setClickable(true);
        overlay.bringToFront();
        
        findViewById(R.id.fragment_content).setOnClickListener(null);
    }
    
    /**
     * 
     */
    private void hideInternetWarning()
    {
        final LinearLayout overlay = (LinearLayout) findViewById(R.id.overlay);
        overlay.setVisibility(View.GONE);
        overlay.setClickable(false);
    }
    
    /**
     * 
     * @param context
     */
    private void checkNews(final Context context)
    {
        newsTask = new CheckNewsTask(this);
        newsTask.execute();
        // newsTask.setEndTaskListener(this);
    }
    
    public boolean haveUuid()
    {
        final String uuid = ConfigHelper.getUUID(getApplicationContext());
        return (uuid != null && uuid.length() > 0);
    }
    
    public boolean haveHistoryFilters()
    {
        return (historyFilterDevices != null && historyFilterNetworks != null);
    }
    
    /**
     * 
     */
    public void checkSettings(boolean force, final EndTaskListener endTaskListener)
    {
        if (settingsTask != null && settingsTask.getStatus() == AsyncTask.Status.RUNNING)
            return;
        
        if (! force && haveUuid() && haveHistoryFilters())
            return;
        
        settingsTask = new CheckSettingsTask(this);
        settingsTask.setEndTaskListener(new EndTaskListener()
        {
            @Override
            public void taskEnded(JSONArray result)
            {
                if (loadingDialog != null)
                    loadingDialog.dismiss();
                if (endTaskListener != null)
                    endTaskListener.taskEnded(result);
            }
        });
        
        settingsTask.execute();
    }
    
    public void waitForSettings(boolean waitForUUID, boolean waitForHistoryFilters, boolean forceWait)
    {
        final boolean haveUuid = haveUuid();
        if (forceWait || (waitForUUID && ! haveUuid) || (waitForHistoryFilters && ! haveHistoryFilters()))
        {
            if (loadingDialog != null)
                loadingDialog.dismiss();
            if (settingsTask != null && settingsTask.getStatus() == AsyncTask.Status.RUNNING)
            {
                final CharSequence title = getResources().getText(! haveUuid ? R.string.main_dialog_registration_title : R.string.main_dialog_reload_title);
                final CharSequence text = getResources().getText(! haveUuid ? R.string.main_dialog_registration_text : R.string.main_dialog_reload_text);
                loadingDialog = ProgressDialog.show(this, title, text, true, true);
                loadingDialog.setOnCancelListener(new ProgressDialog.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        onBackPressed();
                    }
                });
            }
        }
    }
    
    /**
     * 
     */
    public void fetchMapOptions()
    {
        if (getMapOptionsInfoTask != null && getMapOptionsInfoTask.getStatus() == AsyncTask.Status.RUNNING)
            return;
        
        getMapOptionsInfoTask = new GetMapOptionsInfoTask(this);
        getMapOptionsInfoTask.execute();
    }
    
    /**
     * 
     * @param popStack
     */
    public void startTest(final boolean popStack)
    {
        FragmentTransaction ft;
        ft = fm.beginTransaction();
        final RMBTTestFragment rmbtTestFragment = new RMBTTestFragment();
        ft.replace(R.id.fragment_content, rmbtTestFragment, "test");
        ft.addToBackStack("test");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (popStack)
            fm.popBackStack();
        ft.commit();
        
        final Intent service = new Intent(this, RMBTService.class);
        startService(service);
    }
    
    public void showTermsCheck()
    {
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, new RMBTTermsCheckFragment(), "tegetrms_check");
        ft.commit();
    }
    
    public void showNdtCheck()
    {
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, new RMBTNDTCheckFragment(), "ndt_check");
        ft.addToBackStack("ndt_check");
        ft.commit();
    }
    
    /**
     * 
     * @param agbChecked
     */
    public void initApp(boolean duringCreate)
    {
        FragmentTransaction ft;
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, new RMBTMainMenuFragment(), "main");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        
        checkNews(getApplicationContext());
        checkSettings(false, null);
        waitForSettings(true, false, false);
        fetchMapOptions();
        historyResultLimit = Config.HISTORY_RESULTLIMIT_DEFAULT;
        
        if (! duringCreate && geoLocation != null)
            geoLocation.start();
    }
    
    /**
     * 
     * @param popStack
     */
    public void showHistory(final boolean popStack)
    {
        FragmentTransaction ft;
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, new RMBTHistoryFragment(), "history");
        ft.addToBackStack("history");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (popStack)
            fm.popBackStack();
        ft.commit();
    }
    
    /**
     * 
     * @param itemList
     * @param pos
     */
    public void showHistoryPager(final int pos)
    {
        if (historyStorageList != null)
        {
            final RMBTHistoryPagerFragment fragment = new RMBTHistoryPagerFragment();
            
            final Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(RMBTHistoryPagerFragment.ARG_POS, pos);
            args.putSerializable(RMBTHistoryPagerFragment.ARG_ITEMS, historyStorageList);
            
            fragment.setArguments(args);
            
            final FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_content, fragment, "history_pager");
            ft.addToBackStack("history_pager");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
        }
    }
    
    /**
     * 
     * @param uid
     */
    public void showResultDetail(final String testUUid)
    {
        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        
        // FragmentManager fm = getSupportFragmentManager();
        
        FragmentTransaction ft;
        
        final Fragment fragment = new RMBTTestResultDetailFragment();
        
        final Bundle args = new Bundle();
        
        args.putString(RMBTTestResultDetailFragment.ARG_UID, testUUid);
        fragment.setArguments(args);
        
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, "result_detail");
        ft.addToBackStack("result_detail");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }
    
    /**
     * @param testPoint 
     * @param mapType 
     * 
     */
    public void showMap(String mapType, LatLng initialCenter)
    {
        FragmentTransaction ft;
        
        setCurrentMapType(mapType);
        
        final Fragment fragment = new RMBTMapFragment();
        
        final Bundle bundle = new Bundle();
        bundle.putParcelable("initialCenter", initialCenter);
        fragment.setArguments(bundle);
        
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, "map");
        ft.addToBackStack("map");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }
    
    /**
     * 
     * @param url
     */
    public void showHelp(final String url)
    {
        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        
        // FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft;
        
        final Fragment fragment = new RMBTHelpFragment();
        
        final Bundle args = new Bundle();
        
        args.putString(RMBTHelpFragment.ARG_URL, url);
        fragment.setArguments(args);
        
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, "help");
        ft.addToBackStack("help");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }
    
    /**
     * 
     */
    public void showSync()
    {
        
        // FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft;
        
        final Fragment fragment = new RMBTSyncFragment();
        
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, "sync");
        ft.addToBackStack("sync");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }
    
    /**
     * 
     */
    private void stopBackgroundProcesses()
    {
        geoLocation.stop();
        if (newsTask != null)
        {
            newsTask.cancel(true);
            newsTask = null;
        }
        if (settingsTask != null)
        {
            settingsTask.cancel(true);
            settingsTask = null;
        }
        if (loadingDialog != null)
        {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
        
        if (getMapOptionsInfoTask != null)
        {
            getMapOptionsInfoTask.cancel(true);
            getMapOptionsInfoTask = null;
        }
        if (historyTask != null)
        {
            historyTask.cancel(true);
            historyTask = null;
        }
    }
    
    /**
     * 
     * @param history_filter_devices
     * @param history_filter_networks
     */
    public void setSettings(final String[] history_filter_devices, final String[] history_filter_networks)
    {
        historyFilterDevices = history_filter_devices;
        historyFilterNetworks = history_filter_networks;
        
        historyFilterDevicesFilter = new ArrayList<String>();
        for (final String history_filter_device : history_filter_devices)
            historyFilterDevicesFilter.add(history_filter_device);
        
        historyFilterNetworksFilter = new ArrayList<String>();
        for (final String history_filter_network : history_filter_networks)
            historyFilterNetworksFilter.add(history_filter_network);
    }
    
    /**
     * 
     * @return
     */
    public String[] getHistoryFilterDevices()
    {
        return historyFilterDevices;
    }
    
    /**
     * 
     * @return
     */
    public String[] getHistoryFilterNetworks()
    {
        return historyFilterNetworks;
    }
    
    /**
     * 
     * @param history_filter_devices_filter
     */
    public void setHistoryFilterDevicesFilter(final ArrayList<String> historyFilterDevicesFilter)
    {
        this.historyFilterDevicesFilter = historyFilterDevicesFilter;
        historyDirty = true;
    }
    
    /**
     * 
     * @param history_filter_networks_filter
     */
    public void setHistoryFilterNetworksFilter(final ArrayList<String> historyFilterNetworksFilter)
    {
        this.historyFilterNetworksFilter = historyFilterNetworksFilter;
        historyDirty = true;
    }
    
    /**
     * 
     * @return
     */
    public ArrayList<String> getHistoryFilterDevicesFilter()
    {
        return historyFilterDevicesFilter;
    }
    
    /**
     * 
     * @return
     */
    public ArrayList<String> getHistoryFilterNetworksFilter()
    {
        return historyFilterNetworksFilter;
    }
    
    /**
     * 
     * @return
     */
    public List<Map<String, String>> getHistoryItemList()
    {
        return historyItemList;
    }
    
    /**
     * 
     * @return
     */
    public ArrayList<Map<String, String>> getHistoryStorageList()
    {
        return historyStorageList;
    }
    
    /**
     * 
     */
    @Override
    public Map<String, String> getCurrentMapOptions()
    {
        return currentMapOptions;
    }
    
    /**
     * 
     */
    public Map<String, String> getCurrentMapOptionTitles()
    {
        return currentMapOptionTitles;
    }
    
    // /
    
    public void setCurrentMapType(MapListEntry currentMapType)
    {
        this.currentMapType = currentMapType;
        
     // set the filter options in activity
        currentMapOptions.put(currentMapType.getKey(), currentMapType.getValue());
        currentMapOptions.put("overlay_type", currentMapType.getOverlayType());
        currentMapOptionTitles.put(currentMapType.getKey(),
                currentMapType.getSection().getTitle() + ": " + currentMapType.getTitle());
    }
    
    public void setCurrentMapType(String mapType)
    {
        if (mapTypeListSectionList == null || mapType == null)
            return;
        for (final MapListSection section : mapTypeListSectionList)
        {
            for (MapListEntry entry : section.getMapListEntryList())
            {
                if (entry.getValue().equals(mapType))
                    setCurrentMapType(entry);
            }
        }
    }
    
    public MapListEntry getCurrentMapType()
    {
        return currentMapType;
    }
    
    public String getCurrentMainMapType()
    {
        final String mapTypeString = currentMapType == null ? null : currentMapType.getValue();
        String part = null;
        if (mapTypeString != null)
        {
            final String[] parts = mapTypeString.split("/");
            part = parts[0];
        }
        return part;
    }
    
    /**
     * 
     * @return
     */
    public List<MapListSection> getMapTypeListSectionList()
    {
        return mapTypeListSectionList;
    }
    
    /**
     * 
     * @param mapTypeListSectionList
     */
    public void setMapTypeListSectionList(final ArrayList<MapListSection> mapTypeListSectionList)
    {
        this.mapTypeListSectionList = mapTypeListSectionList;
    }
    
    /**
     * 
     * @return
     */
    public Map<String,List<MapListSection>> getMapFilterListSectionListMap()
    {
        return mapFilterListSectionListMap;
    }
    
    /**
     * 
     * @param mapFilterListSectionList
     */
    public void setMapFilterListSectionListMap(final HashMap<String,List<MapListSection>> mapFilterListSectionList)
    {
        this.mapFilterListSectionListMap = mapFilterListSectionList;
    }
    
    /**
     * 
     * @author
     * 
     */
    private class MainGeoLocation extends GeoLocation
    {
        /**
         * 
         * @param context
         */
        public MainGeoLocation(final Context context)
        {
            super(context);
        }
        
        /**
		 * 
		 */
        @Override
        public void onLocationChanged(final Location curLocation)
        {
        }
    }
    
    /**
     * 
     * @return
     */
    public Location getLastKnownLocation()
    {
        
        try
        {
            return geoLocation.getLastKnownLocation();
        }
        catch (final Throwable t)
        {
            return null;
        }
    }
    
    /**
     * 
     * @return
     */
    public boolean isMapFirstRun()
    {
        return mapFirstRun;
    }
    
    /**
     * 
     * @param mapFirstRun
     */
    public void setMapFirstRun(final boolean mapFirstRun)
    {
        this.mapFirstRun = mapFirstRun;
    }
    
    /**
	 * 
	 */
    @Override
    public void onBackPressed()
    {
//        final RMBTNDTCheckFragment ndtCheckFragment = (RMBTNDTCheckFragment) getSupportFragmentManager().findFragmentByTag("ndt_check");
//        if (ndtCheckFragment != null)
//            if (ndtCheckFragment.onBackPressed())
//                return;
        
        final RMBTTermsCheckFragment tcFragment = (RMBTTermsCheckFragment) getSupportFragmentManager().findFragmentByTag("terms_check");
        if (tcFragment != null && tcFragment.isResumed())
            if (tcFragment.onBackPressed())
                return;
        
        final RMBTTestFragment testFragment = (RMBTTestFragment) getSupportFragmentManager().findFragmentByTag("test");
        if (testFragment != null && testFragment.isResumed())
            if (testFragment.onBackPressed())
                return;
        
        final RMBTHistoryPagerFragment historyPagerFragment = (RMBTHistoryPagerFragment) getSupportFragmentManager()
                .findFragmentByTag("history_pager");
        if (historyPagerFragment != null && historyPagerFragment.isResumed())
            if (historyPagerFragment.onBackPressed())
                return;
        
        final RMBTSyncFragment syncCodeFragment = (RMBTSyncFragment) getSupportFragmentManager()
                .findFragmentByTag("sync");
        if (syncCodeFragment != null && syncCodeFragment.isResumed())
            if (syncCodeFragment.onBackPressed())
                return;
        
        super.onBackPressed();
    }
    
    /**
     * 
     * @return
     */
    public boolean isHistoryDirty()
    {
        return historyDirty;
    }
    
    /**
     * 
     * @param historyDirty
     */
    public void setHistoryDirty(final boolean historyDirty)
    {
        this.historyDirty = historyDirty;
    }
    
    /**
     * 
     * @author bp
     * 
     */
    public interface HistoryUpdatedCallback
    {
        /**
         * 
         * @param success
         */
        public void historyUpdated(boolean success);
    }
    
    /**
     * 
     * @param callback
     */
    public void updateHistory(final HistoryUpdatedCallback callback)
    {
        if (historyDirty
                && (historyTask == null || historyTask.isCancelled() || historyTask.getStatus() == AsyncTask.Status.FINISHED))
        {
            historyTask = new CheckHistoryTask(this, historyFilterDevicesFilter, historyFilterNetworksFilter);
            
            historyTask.setEndTaskListener(new EndTaskListener()
            {
                @Override
                public void taskEnded(final JSONArray resultList)
                {
                    if (resultList != null && resultList.length() > 0 && !historyTask.hasError())
                    {
                        historyStorageList.clear();
                        historyItemList.clear();
                        
                        final Date tmpDate = new Date();
                        final DateFormat dateFormat = Helperfunctions.getDateFormat(false);
                        
                        for (int i = 0; i < resultList.length(); i++)
                        {
                            
                            JSONObject resultListItem;
                            try
                            {
                                resultListItem = resultList.getJSONObject(i);
                                
                                final HashMap<String, String> storageItem = new HashMap<String, String>();
                                storageItem.put("test_uuid", resultListItem.optString("test_uuid", null));
                                storageItem.put("time", String.valueOf(resultListItem.optLong("time", 0)));
                                storageItem.put("timezone", resultListItem.optString("timezone", null));
                                historyStorageList.add(storageItem);
                                
                                final HashMap<String, String> viewItem = new HashMap<String, String>();
                                // viewIitem.put( "device",
                                // resultListItem.optString("plattform","none"));
                                viewItem.put("device", resultListItem.optString("model", "-"));
                                
                                viewItem.put("type", resultListItem.optString("network_type"));
                                
                                final String timeString = Helperfunctions.formatTimestampWithTimezone(tmpDate,
                                        dateFormat, resultListItem.optLong("time", 0),
                                        resultListItem.optString("timezone", null));
                                
                                viewItem.put("date", timeString == null ? "-" : timeString);
                                
                                viewItem.put("down", resultListItem.optString("speed_download", "-"));
                                viewItem.put("up", resultListItem.optString("speed_upload", "-"));
                                viewItem.put("ping", resultListItem.optString("ping_shortest", "-"));
                                historyItemList.add(viewItem);
                                
                            }
                            catch (final JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        historyDirty = false;
                        if (callback != null)
                            callback.historyUpdated(true);
                    }
                    else if (callback != null)
                        callback.historyUpdated(false);
                }
            });
            historyTask.execute();
        }
        else if (callback != null)
            callback.historyUpdated(!(historyStorageList.isEmpty() && historyStorageList.isEmpty()));
    }

    public void setMapOverlayType(int mapOverlayType)
    {
        this.mapOverlayType = mapOverlayType;
    }
    
    public int getMapOverlayType()
    {
        return mapOverlayType;
    }
    
    /**
     * 
     * @return
     */
    public int getHistoryResultLimit()
    {
        return historyResultLimit;
    }
    
    /**
     * 
     * @param limit
     */
    public void setHistoryResultLimit(final int limit)
    {
        historyResultLimit = limit;
    }

    public void setMapTypeSatellite(boolean mapTypeSatellite)
    {
        this.mapTypeSatellite = mapTypeSatellite;
    }

    public boolean getMapTypeSatellite()
    {
        return mapTypeSatellite;
    }
}
