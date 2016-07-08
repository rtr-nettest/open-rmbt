/*******************************************************************************
 * Copyright 2015, 2016 alladin-IT GmbH
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
/*******************************************************************************
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

import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.about.RMBTAboutFragment;
import at.alladin.rmbt.android.fragments.history.RMBTFilterFragment;
import at.alladin.rmbt.android.fragments.history.RMBTHistoryFragment;
import at.alladin.rmbt.android.fragments.result.QoSCategoryPagerFragment;
import at.alladin.rmbt.android.fragments.result.QoSTestDetailPagerFragment;
import at.alladin.rmbt.android.fragments.result.RMBTResultPagerFragment;
import at.alladin.rmbt.android.fragments.result.RMBTTestResultDetailFragment;
import at.alladin.rmbt.android.help.RMBTHelpFragment;
import at.alladin.rmbt.android.loopmode.LoopModeStartFragment;
import at.alladin.rmbt.android.loopmode.LoopModeTestFragment;
import at.alladin.rmbt.android.map.MapListEntry;
import at.alladin.rmbt.android.map.MapListSection;
import at.alladin.rmbt.android.map.MapProperties;
import at.alladin.rmbt.android.map.RMBTMapFragment;
import at.alladin.rmbt.android.preferences.RMBTPreferenceActivity;
import at.alladin.rmbt.android.sync.RMBTSyncFragment;
import at.alladin.rmbt.android.terms.RMBTCheckFragment;
import at.alladin.rmbt.android.terms.RMBTCheckFragment.CheckType;
import at.alladin.rmbt.android.terms.RMBTTermsCheckFragment;
import at.alladin.rmbt.android.test.RMBTLoopService;
import at.alladin.rmbt.android.test.RMBTService;
import at.alladin.rmbt.android.test.RMBTTestFragment;
import at.alladin.rmbt.android.util.CheckHistoryTask;
import at.alladin.rmbt.android.util.CheckNewsTask;
import at.alladin.rmbt.android.util.CheckSettingsTask;
import at.alladin.rmbt.android.util.Config;
import at.alladin.rmbt.android.util.ConfigHelper;
import at.alladin.rmbt.android.util.DebugPrintStream;
import at.alladin.rmbt.android.util.EndTaskListener;
import at.alladin.rmbt.android.util.GeoLocation;
import at.alladin.rmbt.android.util.GetMapOptionsInfoTask;
import at.alladin.rmbt.android.util.Helperfunctions;
import at.alladin.rmbt.android.util.PermissionHelper;
import at.alladin.rmbt.android.util.net.NetworkInfoCollector;
import at.alladin.rmbt.client.v2.task.result.QoSServerResult;
import at.alladin.rmbt.client.v2.task.result.QoSServerResult.DetailType;
import at.alladin.rmbt.client.v2.task.result.QoSServerResultCollection;
import at.alladin.rmbt.client.v2.task.result.QoSServerResultDesc;
import at.alladin.rmbt.util.model.option.OptionFunctionCallback;
import at.alladin.rmbt.util.model.option.ServerOptionContainer;

/**
 * 
 * @author
 * 
 */
public class RMBTMainActivity extends FragmentActivity implements MapProperties, ActivityCompat.OnRequestPermissionsResultCallback
{
	/**
	 * 
	 */
	private final static boolean VIEW_HIERARCHY_SERVER_ENABLED = false; 
	
    /**
	 * 
	 */
    private static final String DEBUG_TAG = "RMBTMainActivity";

    /**
	 * 
	 */
    private android.support.v4.app.FragmentManager fm;
    
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
    private GetMapOptionsInfoTask getMapOptionsTask;
    
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
    
    private ServerOptionContainer mapOptions;
    
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
    private boolean mapFirstRun = true;
    
    private ProgressDialog loadingDialog; 
    
    private DrawerLayout drawerLayout;
    
    private ListView drawerList;
    
	private ActionBarDrawerToggle drawerToggle;
	
	private boolean exitAfterDrawerClose = false;
	
	private Menu actionBarMenu;
	
	private String title;
	
	private NetworkInfoCollector networkInfoCollector;
	
    /**
     * 
     */
    private void preferencesUpdate()
    {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //remove control server version on start
        ConfigHelper.setControlServerVersion(this, null);
        
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
    	//Log.i("MAIN ACTIVITY", "onCreate");
        restoreInstance(savedInstanceState);
        super.onCreate(savedInstanceState);
        NetworkInfoCollector.init(this);
        networkInfoCollector = NetworkInfoCollector.getInstance();
        
        preferencesUpdate();
        setContentView(R.layout.main_with_navigation_drawer);
        
        if (VIEW_HIERARCHY_SERVER_ENABLED) {
        	ViewServer.get(this).addWindow(this);
        }

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
                
        // initialize the navigation drawer with the main menu list adapter:
        String[] mainTitles = getResources().getStringArray(R.array.navigation_main_titles);
        int[] navIcons = new int[] {R.drawable.ic_action_home, R.drawable.ic_action_history, 
        		R.drawable.ic_action_map, R.drawable.ic_action_stat, R.drawable.ic_action_help, 
        		R.drawable.ic_action_about, R.drawable.ic_action_settings};

        MainMenuListAdapter mainMenuAdapter = new MainMenuListAdapter(this, mainTitles, navIcons);
        
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerLayout.setBackgroundResource(R.drawable.ic_drawer);
        
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.page_title_title_page, R.string.page_title_title_page) {
        	
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //refreshActionBar(null);
				exitAfterDrawerClose = false;
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setOnKeyListener(new OnKeyListener() {		
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (KeyEvent.KEYCODE_BACK == event.getKeyCode() && exitAfterDrawerClose) {
					onBackPressed();
					return true;
				}				
				return false;
			}
		});
        drawerLayout.setDrawerListener(drawerToggle);
        drawerList.setAdapter(mainMenuAdapter);
        drawerList.setOnItemClickListener(new OnItemClickListener() {
            final int[] menuIds = new int[] {R.id.action_title_page, R.id.action_history, R.id.action_map, R.id.action_stats, R.id.action_help, R.id.action_info, R.id.action_settings};

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				selectMenuItem(menuIds[position]);
				drawerLayout.closeDrawers();
			}
		});
        
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

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
            
            getActionBar().hide();
            setLockNavigationDrawer(true);
            
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
        
        if (PermissionHelper.checkAnyLocationPermission(this))
            geoLocation = new MainGeoLocation(getApplicationContext());
        
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
                	final boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
                	
                	                    
                	if (connected) {
                    	if (networkInfoCollector != null) {
                    		networkInfoCollector.setHasConnectionFromAndroidApi(true);
                    	}                		
                	}
                	else {              		
                    	if (networkInfoCollector != null) {
                    		networkInfoCollector.setHasConnectionFromAndroidApi(false);
                    	}                     	
                	}
                }
            }
        };
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
        case PermissionHelper.REQUEST_AT_INIT:
            if (geoLocation == null)
                geoLocation = new MainGeoLocation(getApplicationContext());
            geoLocation.start();
            checkSettings(true, null);
            
            
            // reinit main menu fragment mainly to update location info
            final FragmentTransaction ft;
            ft = fm.beginTransaction();
            ft.replace(R.id.fragment_content, new RMBTMainMenuFragment(), AppConstants.PAGE_TITLE_MAIN);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            //hack for v4 support library bug:
            ft.commitAllowingStateLoss();
            //ft.commit();
            
            return;
        
        case PermissionHelper.REQUEST_AT_TEST_START:
            startTest();
            return;
        }
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	ViewServer.get(this).removeWindow(this);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
    	super.onPostCreate(savedInstanceState);
    	drawerToggle.syncState();
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        this.actionBarMenu = menu;
        
        title = getTitle(getCurrentFragmentName());
        refreshActionBar(getCurrentFragmentName());
    	return true;
        //return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
        	//show/hide navigation drawer if home button (on the actionbar) was pressed
            if (drawerLayout.isDrawerOpen(drawerList)) {
                drawerLayout.closeDrawer(drawerList);
            } else {
                drawerLayout.openDrawer(drawerList);
            }
        }
        else {
        	selectMenuItem(item.getItemId());
        }
    	return true;
    }
    
    /**
     * 
     * @param id
     */
    public void selectMenuItem(int id) {
    	if (id != R.id.action_settings && id != R.id.action_title_page && id != R.id.action_info) {
    		if (networkInfoCollector != null) {
    			if (!networkInfoCollector.hasConnectionFromAndroidApi()) {
    				showNoNetworkConnectionToast();
    				return;
    			}
    		}
    	}
    	
    	switch (id) {
    	case R.id.action_title_page:
    		popBackStackFull();
    		break;
    	case R.id.action_help:
    		showHelp(true);
    		break;
    	case R.id.action_history:
    		showHistory(false);
    		break;
    	case R.id.action_info:
    		showAbout();
    		break;
    	case R.id.action_map:
    		showMap(true);
    		break;
    	case R.id.action_settings:
    		showSettings();
    		break;
    	case R.id.action_stats:
    		showStatistics();
    		break;
    	case R.id.action_menu_filter:
    		showFilter();
    		break;
    	case R.id.action_menu_sync:
    		showSync();
    		break;
    	case R.id.action_menu_help:
    		showHelp(false);
    		break;
    	case R.id.action_menu_share:
    		showShareResultsIntent();
    		break;
    	case R.id.action_menu_rtr:
    		showRtrWebPage();
    		break;
    	case R.id.action_menu_map:
    		showMapFromPager();
    		break;
    	}
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
        mapOptions = (ServerOptionContainer) b.getSerializable("mapOptions");
        currentMapType = (MapListEntry) b.getSerializable("currentMapType");
    }
    
    @Override
    protected void onSaveInstanceState(Bundle b)
    {
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
        b.putSerializable("mapOptions", mapOptions);
        b.putSerializable("currentMapType", currentMapType);
        super.onSaveInstanceState(b);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	ViewServer.get(this).setFocusedWindow(this);
        redirectSystemOutput(ConfigHelper.isSystemOutputRedirectedToFile(this));
    }
    
    /**
	 * 
	 */
    @Override
    public void onStart()
    {
    	Log.i(DEBUG_TAG, "onStart");
        super.onStart();
        
        registerReceiver(mNetworkStateIntentReceiver, mNetworkStateChangedFilter);
        // init location Manager
        
        if (ConfigHelper.isTCAccepted(this) && ConfigHelper.isNDTDecisionMade(this) && geoLocation != null)
            geoLocation.start();
        
        title = getTitle(getCurrentFragmentName());
        refreshActionBar(getCurrentFragmentName());
    }
    
    /**
	 * 
	 */
    @Override
    public void onStop()
    {
    	Log.i(DEBUG_TAG, "onStop");
        super.onStop();
        stopBackgroundProcesses();
        unregisterReceiver(mNetworkStateIntentReceiver);
    }
        
    public void setOverlayVisibility(boolean isVisible) {
        final LinearLayout overlay = (LinearLayout) findViewById(R.id.overlay);

        if (isVisible) {
        	overlay.setVisibility(View.VISIBLE);
        	overlay.setClickable(true);
        	overlay.bringToFront();
        }
        else {
        	overlay.setVisibility(View.GONE);
        }
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
    	Log.i(DEBUG_TAG,"checkSettings force="+force);
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
        if (getMapOptionsTask != null && getMapOptionsTask.getStatus() == AsyncTask.Status.RUNNING)
            return;
        
        getMapOptionsTask = new GetMapOptionsInfoTask(this);
        getMapOptionsTask.execute();
    }
    
   
    public void checkPermissionsAndStartTest()
    {
        PermissionHelper.checkPermissionAtTestStartAndStartTest(this);
    }
    
    public void startLoopTest() {
    	startLoopService();
    	
        FragmentTransaction ft;
        ft = fm.beginTransaction();
        final LoopModeTestFragment rmbtTestFragment = new LoopModeTestFragment();
        ft.replace(R.id.fragment_content, rmbtTestFragment, AppConstants.PAGE_TITLE_LOOP_TEST);
        ft.addToBackStack(AppConstants.PAGE_TITLE_LOOP_TEST);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fm.popBackStack();
        ft.commit();
    }
    
    public void startLoopService() {
    	ConfigHelper.setLoopModeTestCounter(this, 0);
    	startService(new Intent(RMBTLoopService.ACTION_START, null, getApplicationContext(), RMBTLoopService.class));
    }
    
    public void stopLoopService() {
    	//stop loop service
    	final Intent stopIntent = new Intent(getApplicationContext(), RMBTLoopService.class);
    	stopIntent.setAction(RMBTLoopService.ACTION_STOP);
    	startService(stopIntent);
    	setHistoryDirty(true);
    }
    
    public void startTest()
    {
        if (networkInfoCollector != null) {
            if (!networkInfoCollector.hasConnectionFromAndroidApi()) {
                showNoNetworkConnectionToast();
                return;
            }
        }
        
        final boolean loopMode = ConfigHelper.isLoopMode(RMBTMainActivity.this);
        if (loopMode)
        {
        	final LoopModeStartFragment f = LoopModeStartFragment.newInstance();
        	final FragmentTransaction ft = fm.beginTransaction();
        	ft.add(f, "loop_mode_start_dialog");
        	ft.commitAllowingStateLoss();
        	
        	//doesn't work with v4 support lib:
//        	f.setShowsDialog(true);
//        	f.setCancelable(true);
//        	f.show(getSupportFragmentManager(), "loop_mode_start_dialog");
        }
        else
        {
            FragmentTransaction ft;
            ft = fm.beginTransaction();
            final RMBTTestFragment rmbtTestFragment = new RMBTTestFragment();
            ft.replace(R.id.fragment_content, rmbtTestFragment, AppConstants.PAGE_TITLE_TEST);
            ft.addToBackStack(AppConstants.PAGE_TITLE_TEST);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            //fix for v4 support lib:
            ft.commitAllowingStateLoss();
            //ft.commit();
            
            final Intent service = new Intent(RMBTService.ACTION_START_TEST, null, RMBTMainActivity.this, RMBTService.class);
            startService(service);
        }
    }
    
    private void showShareResultsIntent() {
    	Fragment f = getCurrentFragment();
    	try {
	    	if (f != null && f instanceof RMBTResultPagerFragment) {
	    		((RMBTResultPagerFragment) f).getPagerAdapter().startShareResultsIntent();
	    	}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
	}
    
    public void showTermsCheck()
    {
    	popBackStackFull();
    	
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, RMBTTermsCheckFragment.newInstance(null), AppConstants.PAGE_TITLE_TERMS_CHECK);
        ft.commit();
    }
    
    public void showRtrWebPage() {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.menu_rtr_web_link))));
    }
    
    public boolean showNdtCheckIfNecessary()
    {
        final boolean ndtDecisionMade = ConfigHelper.isNDTDecisionMade(this);
        if (! ndtDecisionMade)
            showNdtCheck();
        return ! ndtDecisionMade;
    }
    
    public void showNdtCheck()
    {
        final FragmentTransaction ft = fm.beginTransaction();
        
        ft.replace(R.id.fragment_content, RMBTCheckFragment.newInstance(CheckType.NDT), AppConstants.PAGE_TITLE_NDT_CHECK);
        ft.addToBackStack(AppConstants.PAGE_TITLE_NDT_CHECK);
        ft.commit();
        
        /*
        ft.replace(R.id.fragment_content, new RMBTNDTCheckFragment(), "ndt_check");
        ft.addToBackStack("ndt_check");
        ft.commit();
        */
    }
    
    public void showResultsAfterTest(String testUuid) {
    	popBackStackFull();
    	
        final RMBTResultPagerFragment fragment = new RMBTResultPagerFragment();
        final Bundle args = new Bundle();
        args.putString(RMBTResultPagerFragment.ARG_TEST_UUID, testUuid);
        fragment.setArguments(args);
        
        
        final FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft;
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_HISTORY_PAGER);
        ft.addToBackStack(AppConstants.PAGE_TITLE_HISTORY_PAGER);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        
        refreshActionBar(AppConstants.PAGE_TITLE_HISTORY_PAGER);
    }
    
    public void initApp(boolean duringCreate)
    {
    	popBackStackFull();
    	
        FragmentTransaction ft;
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, new RMBTMainMenuFragment(), AppConstants.PAGE_TITLE_MAIN);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        
        checkNews(getApplicationContext());
        checkSettings(false, null);
        //checkIp();
        waitForSettings(true, false, false);
        //fetchMapOptions();
        historyResultLimit = Config.HISTORY_RESULTLIMIT_DEFAULT;
        
        if (! duringCreate) // only the very first time after t+c
            PermissionHelper.checkPermissionAtInit(this);
        
        if (! duringCreate && geoLocation != null)
            geoLocation.start();
    }
    
    /**
     * 
     */
    public void showNoNetworkConnectionToast() {
    	Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 
     * @param popStack
     */
    public void showHistory(final boolean popStack)
    {
    	popBackStackFull();

        FragmentTransaction ft;
        ft = fm.beginTransaction();
        
        ft.replace(R.id.fragment_content, new RMBTHistoryFragment(), AppConstants.PAGE_TITLE_HISTORY);
        ft.addToBackStack(AppConstants.PAGE_TITLE_HISTORY);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (popStack) {
        	fm.popBackStack();
        }
        
        ft.commit();
        
        refreshActionBar(AppConstants.PAGE_TITLE_HISTORY);
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
//            final RMBTHistoryPagerFragment fragment = new RMBTHistoryPagerFragment();
            
            final Bundle args = new Bundle();
            
            final RMBTResultPagerFragment fragment = new RMBTResultPagerFragment();
            String testUuid = historyStorageList.get(pos).get("test_uuid");
            //testUuid = "842356d7-a863-48f9-8220-678125fb3a76";
            //testUuid = "0d765559-ab16-4fa1-b776-4040e18bf134";
            //testUuid = "dbf47f08-711f-4cfa-9fd9-78f06a7a7df3";
            //testUuid = "7c4f961b-0807-461b-aa8b-d2ae0b89b662";
            args.putString(RMBTResultPagerFragment.ARG_TEST_UUID, testUuid);
            fragment.setArguments(args);
            
            final FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_HISTORY_PAGER);
            ft.addToBackStack(AppConstants.PAGE_TITLE_HISTORY_PAGER);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
            
            refreshActionBar(AppConstants.PAGE_TITLE_HISTORY_PAGER);
        }
    }
    
    /**
     * 
     * @param uid
     */
    public void showResultDetail(final String testUUid)
    {    	
        FragmentTransaction ft;
        
        final Fragment fragment = new RMBTTestResultDetailFragment();
        
        final Bundle args = new Bundle();
        
        args.putString(RMBTTestResultDetailFragment.ARG_UID, testUUid);
        fragment.setArguments(args);
        
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_RESULT_DETAIL);
        ft.addToBackStack(AppConstants.PAGE_TITLE_RESULT_DETAIL);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        
        refreshActionBar(AppConstants.PAGE_TITLE_RESULT_DETAIL);
    }
    
    public void showAbout() {
    	popBackStackFull();

        FragmentTransaction ft;
        ft = fm.beginTransaction();
        
        ft.replace(R.id.fragment_content, new RMBTAboutFragment(), AppConstants.PAGE_TITLE_ABOUT);
        ft.addToBackStack(AppConstants.PAGE_TITLE_ABOUT);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);        	
        
        ft.commit();
        refreshActionBar(AppConstants.PAGE_TITLE_ABOUT);
    }
    
    /**
     * 
     * @param uid
     */
    public void showExpandedResultDetail(QoSServerResultCollection testResultArray, DetailType detailType, int position)
    {
        FragmentTransaction ft;
        
        //final RMBTResultDetailPagerFragment fragment = new RMBTResultDetailPagerFragment();
        final QoSCategoryPagerFragment fragment = new QoSCategoryPagerFragment();
        
        fragment.setQoSResult(testResultArray);
        fragment.setDetailType(detailType);
        
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_RESULT_QOS);
        ft.addToBackStack("result_detail_expanded");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        
        fragment.setCurrentPosition(position);
        refreshActionBar(AppConstants.PAGE_TITLE_RESULT_QOS);
    }
    
    /**
     * 
     * @param uid
     */
    public void showQoSTestDetails(List<QoSServerResult> resultList, List<QoSServerResultDesc> descList, int index)
    {
        FragmentTransaction ft;
        
        //final RMBTResultDetailPagerFragment fragment = new RMBTResultDetailPagerFragment();
        final QoSTestDetailPagerFragment fragment = new QoSTestDetailPagerFragment();

        fragment.setQoSResultList(resultList);
        fragment.setQoSDescList(descList);
        fragment.setInitPosition(index);
        
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_TEST_DETAIL_QOS);
        ft.addToBackStack(AppConstants.PAGE_TITLE_TEST_DETAIL_QOS);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        
        refreshActionBar(AppConstants.PAGE_TITLE_TEST_DETAIL_QOS);
    }

    public void showMapFromPager() {
    	try {
    		Fragment f = getCurrentFragment();
    		if (f != null) {
    			((RMBTResultPagerFragment) f).getPagerAdapter().showMap();
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public void showMap(boolean popBackStack) {
    	if (popBackStack) {
    		popBackStackFull();
    	}
    	
        FragmentTransaction ft;
        ft = fm.beginTransaction();
        Fragment f = new RMBTMapFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(RMBTMapFragment.OPTION_ENABLE_ALL_GESTURES, true);
        bundle.putBoolean(RMBTMapFragment.OPTION_SHOW_INFO_TOAST, true);
        bundle.putBoolean(RMBTMapFragment.OPTION_ENABLE_CONTROL_BUTTONS, true);
        f.setArguments(bundle);
        ft.replace(R.id.fragment_content, f, AppConstants.PAGE_TITLE_MAP);
        ft.addToBackStack(AppConstants.PAGE_TITLE_MAP);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();

        refreshActionBar(AppConstants.PAGE_TITLE_MAP);
    }
    
    public RMBTMapFragment showMap(String mapType, LatLng initialCenter, boolean clearFilter, boolean popBackStack) {
    	return showMap(mapType, initialCenter, clearFilter, -1, popBackStack);
    }
    
    /**
     * @param testPoint 
     * @param mapType 
     * 
     */
    public RMBTMapFragment showMap(String mapType, LatLng initialCenter, boolean clearFilter, int viewId, boolean popBackStack)
    {
    	if (popBackStack) {
    		popBackStackFull();
    	}
    	
        FragmentTransaction ft;
        
        setCurrentMapType(mapType);
        
        if (clearFilter)
        {
            final List<MapListSection> mapFilterListSelectionList = getMapFilterListSelectionList();
            if (mapFilterListSelectionList != null)
            {
                for (final MapListSection section : mapFilterListSelectionList)
                {
                    for (final MapListEntry entry : section.getMapListEntryList())
                        entry.setChecked(entry.isDefault());
                }
                updateMapFilter(null);
            }
        }
        
        final RMBTMapFragment fragment = new RMBTMapFragment();
        
        final Bundle bundle = new Bundle();
        bundle.putParcelable("initialCenter", initialCenter);
        
        if (viewId >= 0) {
        	bundle.putBoolean(RMBTMapFragment.OPTION_ENABLE_ALL_GESTURES, false);
        	bundle.putBoolean(RMBTMapFragment.OPTION_SHOW_INFO_TOAST, false);
        	bundle.putBoolean(RMBTMapFragment.OPTION_ENABLE_CONTROL_BUTTONS, false);
        	bundle.putBoolean(RMBTMapFragment.OPTION_ENABLE_OVERLAY, false);
            fragment.setArguments(bundle);
            ft = fm.beginTransaction();
        	//replace the given viewgroup, but do not add to backstack
            ft.replace(viewId, fragment, AppConstants.PAGE_TITLE_MINI_MAP);
            //ft.addToBackStack(AppConstants.PAGE_TITLE_MINI_MAP);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit(); 	
        }
        else {
        	System.out.println("SHOW MAP");
            fragment.setArguments(bundle);
            ft = fm.beginTransaction();
            ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_MAP);
            ft.addToBackStack(AppConstants.PAGE_TITLE_MAP);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
            refreshActionBar(AppConstants.PAGE_TITLE_MAP);
        }

        return fragment;
    }
    
    /**
     * 
     * @param url
     */
    
    public void showHelp(final int resource, boolean popBackStack)
    {
        showHelp(getResources().getString(resource), popBackStack, AppConstants.PAGE_TITLE_HELP);
    }
    
    public void showHelp(boolean popBackStack) {
    	showHelp("", popBackStack, AppConstants.PAGE_TITLE_HELP);
    }
    
    public void showHelp(final String url, boolean popBackStack, String titleId)
    {
    	if (popBackStack) {
    		popBackStackFull();
    	}
        
    	FragmentTransaction ft;
    	
        
        ft = fm.beginTransaction();
        
        final Fragment fragment = new RMBTHelpFragment();        
        final Bundle args = new Bundle();
            
        args.putString(RMBTHelpFragment.ARG_URL, url);
        fragment.setArguments(args);
        ft.replace(R.id.fragment_content, fragment, titleId);
        ft.addToBackStack(titleId);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        ft.commit();
        refreshActionBar(titleId);
    }
    
    /**
     * 
     */
    public void showSync()
    {
        FragmentTransaction ft;        
        ft = fm.beginTransaction();
        
        final Fragment fragment = new RMBTSyncFragment();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_SYNC);
        ft.addToBackStack(AppConstants.PAGE_TITLE_SYNC);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);        	
        
        ft.commit();
        refreshActionBar(AppConstants.PAGE_TITLE_SYNC);
    }
    
    public void showFilter() {
        final FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft;
        
        final Fragment fragment = new RMBTFilterFragment();
        
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_HISTORY_FILTER);
        ft.addToBackStack(AppConstants.PAGE_TITLE_HISTORY_FILTER);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        
        refreshActionBar(AppConstants.PAGE_TITLE_HISTORY_FILTER);
    }

    /**
     * 
     */
    public void showSettings() {
        startActivity(new Intent(this, RMBTPreferenceActivity.class));
    }
    
    /**
     * 
     */
    public void showStatistics() {
        String urlStatistic = ConfigHelper.getVolatileSetting("url_statistics");
        if (urlStatistic == null || urlStatistic.length() == 0) {
        	if ((urlStatistic = ConfigHelper.getCachedStatisticsUrl(getApplicationContext())) == null) {
        		return;
        	}
        }
        showHelp(urlStatistic, true, AppConstants.PAGE_TITLE_STATISTICS);
    }
    
    /**
     * 
     */
    private void stopBackgroundProcesses()
    {
        if (geoLocation != null)
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
        
        if (getMapOptionsTask != null)
        {
            getMapOptionsTask.cancel(true);
            getMapOptionsTask = null;
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
        if (history_filter_devices != null)
            for (final String history_filter_device : history_filter_devices)
                historyFilterDevicesFilter.add(history_filter_device);
        
        historyFilterNetworksFilter = new ArrayList<String>();
        if (history_filter_networks != null)
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
    public Map<String, String> getCurrentMapOptions(final OptionFunctionCallback callback)
    {
    	updateMapFilter(callback);
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
        final String uuid = ConfigHelper.getUUID(getApplicationContext());
        currentMapOptions.clear();
        currentMapOptionTitles.clear();
        currentMapOptions.put("highlight", uuid);
        currentMapOptions.put(currentMapType.getKey(), currentMapType.getValue());
        currentMapOptions.put("overlay_type", currentMapType.getOverlayType());
        currentMapOptionTitles.put(currentMapType.getKey(),
                currentMapType.getSection().getTitle() + ": " + currentMapType.getTitle());
        
        updateMapFilter(null);
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
                {
                    setCurrentMapType(entry);
                    return;
                }
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
    
    public ServerOptionContainer getMapOptions() {
    	return mapOptions;
    }
    
    public void setMapOptions(final ServerOptionContainer mapOptions) {
    	this.mapOptions = mapOptions;
    }
    
    /**
     * 
     * @return
     */
    public Map<String,List<MapListSection>> getMapFilterListSectionListMap()
    {
        return mapFilterListSectionListMap;
    }
    
    public List<MapListSection> getMapFilterListSelectionList()
    {
        
        final Map<String, List<MapListSection>> mapFilterListSectionListMap = getMapFilterListSectionListMap();
        if (mapFilterListSectionListMap == null)
            return null;
        return mapFilterListSectionListMap.get(getCurrentMainMapType());
    }
    
    /**
     * 
     * @param mapFilterListSectionList
     */
    public void setMapFilterListSectionListMap(final HashMap<String,List<MapListSection>> mapFilterListSectionList)
    {
        this.mapFilterListSectionListMap = mapFilterListSectionList;
        updateMapFilter(null);
    }
    
    public void updateMapFilter(final OptionFunctionCallback callback)
    {
    	ServerOptionContainer mapOption = getMapOptions();
    	
    	if (mapOption != null) {
    		//currentMapOptionTitles.clear();
    		currentMapOptions.clear();
    		final String uuid = ConfigHelper.getUUID(getApplicationContext());
            currentMapOptions.put("highlight", uuid);
    		
        	mapOption.registerFunctionCallback(callback);
    		Map<String, Object> params = mapOption.getSelectedParams();
    		if (params != null) {
    			for (Entry<String, Object> e : params.entrySet()) {
    				String val = String.valueOf(e.getValue());
    				if (e.getValue() instanceof Double) {
        				if (((Double)e.getValue()).doubleValue() == (long) ((Double)e.getValue()).doubleValue()) {
        					val = String.valueOf((long) ((Double)e.getValue()).doubleValue());
        				}
    				} 
    				currentMapOptions.put(e.getKey(), val);
    				//currentMapOptionTitles.put(e.getKey(), e.getKey() + ": " + val);
    			}
    		}
    		
        	mapOption.unregisterFunctionCallback();
    	}
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
            super(context, ConfigHelper.isGPS(context));
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
        

        final RMBTTermsCheckFragment tcFragment = (RMBTTermsCheckFragment) getSupportFragmentManager().findFragmentByTag(AppConstants.PAGE_TITLE_TERMS_CHECK);
        if (tcFragment != null && tcFragment.isResumed()) {
            if (tcFragment.onBackPressed())
                return;
        }
        
        final RMBTTestFragment testFragment = (RMBTTestFragment) getSupportFragmentManager().findFragmentByTag("test");
        if (testFragment != null && testFragment.isResumed()) {
            if (testFragment.onBackPressed())
                return;
        }

        final LoopModeTestFragment loopTestFragment = (LoopModeTestFragment) getSupportFragmentManager().findFragmentByTag(AppConstants.PAGE_TITLE_LOOP_TEST);
        if (loopTestFragment != null && loopTestFragment.isResumed()) {
            if (loopTestFragment.onBackPressed())
                return;
        }


        final RMBTSyncFragment syncCodeFragment = (RMBTSyncFragment) getSupportFragmentManager()
                .findFragmentByTag("sync");
        if (syncCodeFragment != null && syncCodeFragment.isResumed()) {
            if (syncCodeFragment.onBackPressed())
                return;
        } 

        final RMBTMainMenuFragment mainMenuCodeFragment = (RMBTMainMenuFragment) getSupportFragmentManager()
                .findFragmentByTag(AppConstants.PAGE_TITLE_MAIN);
        if (mainMenuCodeFragment != null && mainMenuCodeFragment.isResumed()) {
            if (mainMenuCodeFragment.onBackPressed())
                return;
        } 
        
        refreshActionBarAndTitle();
        
        if (getSupportFragmentManager().getBackStackEntryCount() > 0 || exitAfterDrawerClose) {
            super.onBackPressed();            
        }
        else {
        	if (ConfigHelper.isDontShowMainMenuOnClose(this)) {
        		super.onBackPressed();
        	}
        	else {
        		exitAfterDrawerClose = true;
        		drawerLayout.openDrawer(drawerList);
        	}
        }
    }
    
    private void refreshActionBarAndTitle() {
        title = getTitle(getPreviousFragmentName());
        refreshActionBar(getPreviousFragmentName());
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
    	public final static int SUCCESSFUL = 0;
    	public final static int LIST_EMPTY = 1;
    	public final static int ERROR = 2;
    	
        /**
         * 
         * @param success
         */
        public void historyUpdated(int status);
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
                                viewItem.put("ping", resultListItem.optString("ping", "-"));
                                historyItemList.add(viewItem);
                            }
                            catch (final JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        historyDirty = false;
                        if (callback != null)
                            callback.historyUpdated(HistoryUpdatedCallback.SUCCESSFUL);
                    }
                    else if (callback != null) {
                        callback.historyUpdated(historyTask.hasError() ? HistoryUpdatedCallback.ERROR : HistoryUpdatedCallback.LIST_EMPTY);
                    }
                }
            });
            historyTask.execute();
        }
        else if (callback != null)
            callback.historyUpdated(!(historyStorageList.isEmpty() && historyStorageList.isEmpty()) ? HistoryUpdatedCallback.SUCCESSFUL : HistoryUpdatedCallback.LIST_EMPTY);
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
    
    public void popBackStackFull() {
    	runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
		    	if (fm.getBackStackEntryCount() > 0) {
		        	fm.popBackStack(fm.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);	
		    	}
		    	
		    	refreshActionBarAndTitle();				
			}
		});
    }
    
    /**
     * 
     * @param toFile
     */
    public void redirectSystemOutput(boolean toFile) {
        try {
        	if (toFile) {
        		Log.i(DEBUG_TAG,"redirecting sysout to file");
        		//Redirecting console output and runtime exceptions to file (System.out.println)
        		File f = new File(Environment.getExternalStorageDirectory(), "qosdebug");
        		if (!f.exists()) {
        			f.mkdir();
        		}
            
        		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.GERMAN);
        		//PrintStream fileStream = new PrintStream(new File(f, sdf.format(new Date()) + ".txt"));
        		PrintStream fileStream = new DebugPrintStream(new File(f, sdf.format(new Date()) + ".txt"));
             
        		//System.setOut(fileStream);
                System.setErr(fileStream);
        	}
        	else {        		
        		//Redirecting console output and runtime exceptions to default output stream
                //System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                //System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
                //Log.i(DEBUG_TAG,"redirecting sysout to default");
        	}
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    public Fragment getCurrentFragment()
    {
    	final int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount > 0)
        {
    		try
            {
                final BackStackEntry backStackEntryAt = getSupportFragmentManager().getBackStackEntryAt(backStackEntryCount - 1);
                String fragmentTag = backStackEntryAt.getName();
                Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
                return currentFragment;
            }
            catch (Exception e)
            {
                // fix possible race condition:
                // when called in background thread - back stack could be different between call of
                // getBackStackEntryCount() and getBackStackEntryAt()
                e.printStackTrace();
            }
    	}
    	
    	return getSupportFragmentManager().findFragmentByTag(AppConstants.PAGE_TITLE_MAIN);
    }

    /**
     * 
     * @return
     */
    public String getCurrentFragmentName(){
    	if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
    		String fragmentTag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
            return fragmentTag;
    	}

    	Fragment f = getSupportFragmentManager().findFragmentByTag(AppConstants.PAGE_TITLE_MAIN);
    	return f != null ? AppConstants.PAGE_TITLE_MAIN : null;
    }

    /**
     * 
     * @return
     */
    protected String getPreviousFragmentName(){
    	if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
    		String fragmentTag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 2).getName();
            return fragmentTag;
    	}

    	return null;
    }

    /**
     * 
     * @return
     */
    protected String getTitle(String fragmentName) {
    	String name = fragmentName; // (fragmentName != null ? fragmentName : getCurrentFragmentName());
    	Integer id = null;
    	if (name != null)
    	    id = AppConstants.TITLE_MAP.get(name);
    	
    	if (id == null)
    	    id = R.string.page_title_title_page;
    	    
        title = getResources().getString(id);

    	return title;
    }
    
    /**
     * 
     * @param isEnabled
     */
    public void setLockNavigationDrawer(boolean isLocked) {
    	if (isLocked) {
    		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    	}
    	else {
    		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    	}
    }
    
    public void refreshActionBar(String name) {
    	if (name == null && title == null) {
    		getActionBar().setTitle(getTitle(getCurrentFragmentName()));
    	}
    	else {
    		getActionBar().setTitle((name != null || title == null) ? getTitle(name) : title);
    	}
    	
    	if (actionBarMenu != null) {
    		if (AppConstants.PAGE_TITLE_HISTORY.equals(name)) {
        		setVisibleMenuItems(R.id.action_menu_filter, R.id.action_menu_sync);
        	}
    		else if (AppConstants.PAGE_TITLE_ABOUT.equals(name)) {
    			setVisibleMenuItems(R.id.action_menu_rtr);
    		}
    		else if (AppConstants.PAGE_TITLE_HISTORY_PAGER.equals(name)) {
    			Fragment f = getCurrentFragment();
    			if (f != null && f instanceof RMBTResultPagerFragment) {
    				((RMBTResultPagerFragment) f).setActionBarItems();
    			}
    		}
        	else {
        		setVisibleMenuItems();
        	}    	
    	}
    }
    
    /**
     * 
     * @param id
     */
    public void setVisibleMenuItems(Integer...id) {
    	if (actionBarMenu != null) {
    		if (id != null && id.length > 0) {
        		Set<Integer> idSet = new HashSet<Integer>(); 
        		Collections.addAll(idSet, id);
        		for (int i = 0; i < actionBarMenu.size(); i++) {
        			MenuItem item = actionBarMenu.getItem(i);
        			if (idSet.contains(item.getItemId())) {
        				item.setVisible(true);
        			}
        			else {
        				item.setVisible(false);
        			}
        		}
    		}
    		else {
        		for (int i = 0; i < actionBarMenu.size(); i++) {
        			MenuItem item = actionBarMenu.getItem(i);
        			item.setVisible(false);
        		}
    		}
    	}
    }
    
    /**
     * 
     * @return
     */
    public NetworkInfoCollector getNetworkInfoCollector() {
    	return this.networkInfoCollector;
    }
    
    /*
     */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
        boolean isMobile = false, isWifi = false;

        try {
            NetworkInfo[] infoAvailableNetworks = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getAllNetworkInfo();

            if (infoAvailableNetworks != null) {
                for (NetworkInfo network : infoAvailableNetworks) {

                    if (network.getType() == ConnectivityManager.TYPE_WIFI) {
                        if (network.isConnected() && network.isAvailable())
                            isWifi = true;
                    }
                    if (network.getType() == ConnectivityManager.TYPE_MOBILE) {
                        if (network.isConnected() && network.isAvailable())
                            isMobile = true;
                    }
                }
            }

            return isMobile || isWifi;
        }
        catch (Exception e) {
        	return false;
        }
    }
}