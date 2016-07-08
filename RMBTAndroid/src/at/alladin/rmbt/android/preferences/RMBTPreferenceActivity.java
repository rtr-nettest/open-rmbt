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
package at.alladin.rmbt.android.preferences;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.widget.ListView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.AppConstants;
import at.alladin.rmbt.android.terms.RMBTCheckFragment.CheckType;
import at.alladin.rmbt.android.terms.RMBTTermsActivity;
import at.alladin.rmbt.android.util.ConfigHelper;
import at.alladin.rmbt.android.util.Server;

public class RMBTPreferenceActivity extends PreferenceActivity
{
    protected static final int REQUEST_NDT_CHECK = 1;
    protected static final int REQUEST_LOOP_MODE_CHECK = 2;
    protected Method mLoadHeaders = null;
    protected Method mHasHeaders = null;
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case android.R.id.home:
    		finish();
    		break;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
    
    /**
     * Checks to see if using new v11+ way of handling PrefsFragments.
     * 
     * @return Returns false pre-v11, else checks to see if using headers.
     */
    public boolean isNewV11Prefs()
    {
        if (mHasHeaders != null && mLoadHeaders != null)
            try
            {
                return (Boolean) mHasHeaders.invoke(this);
            }
            catch (final IllegalArgumentException e)
            {
            }
            catch (final IllegalAccessException e)
            {
            }
            catch (final InvocationTargetException e)
            {
            }
        return false;
    }
    
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        // onBuildHeaders() will be called during super.onCreate()
        try
        {
            mLoadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class);
            mHasHeaders = getClass().getMethod("hasHeaders");
        }
        catch (final NoSuchMethodException e)
        {
        }
        super.onCreate(savedInstanceState);
        if (!isNewV11Prefs())
        {

 
            if (ConfigHelper.isUserLoopModeActivated(this) && !ConfigHelper.isDevEnabled(this)) {
            	addPreferencesFromResource(R.xml.preferences_loop);
            }
 
            addPreferencesFromResource(R.xml.preferences);
            
            if (ConfigHelper.isDevEnabled(this)) {
                addPreferencesFromResource(R.xml.preferences_dev);
            }

        }
        
        
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setDisplayHomeAsUpEnabled(true);
       
        final ListView v = getListView();
        v.setCacheColorHint(0);
        
        //final float scale = getResources().getDisplayMetrics().density;
        //final int padding = Helperfunctions.dpToPx(10, scale);
        
        //final ViewGroup vg = (ViewGroup) v.getRootView();
        //vg.setPadding(padding, padding, padding, padding);
        
        //final int paddingTopBottom = Helperfunctions.dpToPx(3, scale);
        //final int paddingLeftRight = Helperfunctions.dpToPx(10, scale);
        //v.setBackgroundResource(R.drawable.box_large);
        v.setBackgroundResource(R.drawable.app_bgdn_radiant);
        //v.setPadding(paddingLeftRight, paddingTopBottom, paddingLeftRight, paddingTopBottom);
        
        final Preference loopModeMaxDelayPreference = findPreference("loop_mode_max_delay");
        if (loopModeMaxDelayPreference != null && !ConfigHelper.isDevEnabled(this)) {
        	loopModeMaxDelayPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					return checkInputValidity(RMBTPreferenceActivity.this, newValue,
							AppConstants.LOOP_MODE_MIN_DELAY, AppConstants.LOOP_MODE_MAX_DELAY, R.string.loop_mode_max_delay_invalid);
				}
			});
        }
        
        final Preference loopModeMaxMovementPreference = findPreference("loop_mode_max_movement");
        if (loopModeMaxMovementPreference != null && !ConfigHelper.isDevEnabled(this)) {
        	loopModeMaxMovementPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					return checkInputValidity(RMBTPreferenceActivity.this, newValue, 
							AppConstants.LOOP_MODE_MIN_MOVEMENT, AppConstants.LOOP_MODE_MAX_MOVEMENT, R.string.loop_mode_max_movement_invalid);
				}
			});
        }
        
        final Preference loopModePref = (Preference) findPreference("loop_mode");
        if (loopModePref != null)
        {
        	loopModePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    if (preference instanceof CheckBoxPreference)
                    {
                        final CheckBoxPreference cbp = (CheckBoxPreference) preference;
                        
                        if (cbp.isChecked())
                        {
                            cbp.setChecked(false);
                            final Intent intent = new Intent(getBaseContext(), RMBTTermsActivity.class);
                            intent.putExtra(RMBTTermsActivity.EXTRA_KEY_CHECK_TYPE, CheckType.LOOP_MODE.name());
                            intent.putExtra(RMBTTermsActivity.EXTRA_KEY_CHECK_TERMS_AND_COND, false);
                            startActivityForResult(intent, REQUEST_LOOP_MODE_CHECK);
                        }
                    }
                    return true;
                }
            });
        }
        
        final Preference ndtPref = (Preference) findPreference("ndt");
        if (ndtPref != null)
        {
            ndtPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    if (preference instanceof CheckBoxPreference)
                    {
                        final CheckBoxPreference cbp = (CheckBoxPreference) preference;
                        
                        if (cbp.isChecked())
                        {
                            cbp.setChecked(false);
                            final Intent intent = new Intent(getBaseContext(), RMBTTermsActivity.class);
                            intent.putExtra(RMBTTermsActivity.EXTRA_KEY_CHECK_TYPE, CheckType.NDT.name());
                            startActivityForResult(intent, REQUEST_NDT_CHECK);
                        }
                    }
                    return true;
                }
            });
        }
        
        final Preference gpsPref = (Preference) findPreference("location_settings");
        if (gpsPref != null)
        {
            gpsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    return true;
                }
            });
        }
                
        // v.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        
        // addPreferencesFromResource(R.xml.preferences);
        
        final Preference serverSelectionPrefCat = findPreference("server_selection_preferences");
        if (serverSelectionPrefCat != null)
        {
            if (! ConfigHelper.isUserServerSelectionActivated(this) || ConfigHelper.getServers(this) == null)
                getPreferenceScreen().removePreference(serverSelectionPrefCat);
            else
            {
                final ListPreference serverSelectionPref = (ListPreference) findPreference("server_selection");
                if (serverSelectionPref != null)
                {
                    boolean foundCurrentSelection = false;
                    String currentSelection = ConfigHelper.getServerSelection(this);
                    if (currentSelection == null)
                    {
                        ConfigHelper.setServerSelection(this, ConfigHelper.DEFAULT_SERVER);
                        currentSelection = ConfigHelper.DEFAULT_SERVER;
                    }
                    
                    if (currentSelection.equals(ConfigHelper.DEFAULT_SERVER))
                        foundCurrentSelection = true;
                    
                    final Set<String> servers = ConfigHelper.getServers(this);
                    if (servers != null)
                    {
                        final List<String> entries = new ArrayList<String>();
                        final List<String> entryValues = new ArrayList<String>();
                        entries.add("Default");
                        entryValues.add(ConfigHelper.DEFAULT_SERVER);
                        for (String serverStr : servers)
                        {
                            final Server server = Server.decode(serverStr);
                            entries.add(server.getName());
                            final String serverUuid = server.getUuid();
                            if (currentSelection.equals(serverUuid))
                                foundCurrentSelection = true;
                            entryValues.add(serverUuid);
                        }
                        serverSelectionPref.setEntries(entries.toArray(new String[]{}));
                        serverSelectionPref.setEntryValues(entryValues.toArray(new String[]{}));
                        
                        if (! foundCurrentSelection)
                        {
                            ConfigHelper.setServerSelection(this, ConfigHelper.DEFAULT_SERVER);
                            recreate();
                        }
        //                    final String serverSelection = ConfigHelper.getServerSelection(this);
        //                    serverSelectionPref.setSummary(serverSelection != null ? serverSelection : "");
                    }
                }
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NDT_CHECK) {
            ((CheckBoxPreference) findPreference("ndt")).setChecked(ConfigHelper.isNDT(this));
        }
        else if (requestCode == REQUEST_LOOP_MODE_CHECK) {
        	((CheckBoxPreference) findPreference("loop_mode")).setChecked(ConfigHelper.isLoopMode(this));
        }
    }
    
    
    public static boolean checkInputValidity(final Context context, final Object v, final long minValue, final long maxValue, final int errorMsgId) {
    	boolean showErrorDialog = false;
    	try {
    		final long value = Long.valueOf(String.valueOf(v));
    		if (value > maxValue || value < minValue) {
    			showErrorDialog = true;
    		}
    	}
    	catch (Exception e) {
    		showErrorDialog = true;
    	}
		
    	if (showErrorDialog) {
			final String msg = context.getString(errorMsgId);
			final AlertDialog dialog = new AlertDialog.Builder(context)
					.setTitle(R.string.value_invalid)
					.setMessage(MessageFormat.format(msg, minValue, maxValue))
					.setPositiveButton(android.R.string.ok, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
						}
					})
					.create();
			
			dialog.show();
    	}
		
		return !showErrorDialog;	
    }
    
    /*
     * @Override public void onBuildHeaders(List<Header> aTarget) { try {
     * mLoadHeaders.invoke(this,new Object[]{R.xml.pref_headers,aTarget}); }
     * catch (IllegalArgumentException e) { } catch (IllegalAccessException e) {
     * } catch (InvocationTargetException e) { } }
     * 
     * @TargetApi(11) static public class PrefsFragment extends
     * PreferenceFragment {
     * 
     * @Override public void onCreate(Bundle aSavedState) {
     * super.onCreate(aSavedState); Context anAct =
     * getActivity().getApplicationContext(); int thePrefRes =
     * anAct.getResources
     * ().getIdentifier(getArguments().getString("pref-resource"),
     * "xml",anAct.getPackageName()); addPreferencesFromResource(thePrefRes);
     * //addPreferencesFromResource(R.xml.preferences);
     * Log.i("test","Preferences Loaded"); } }
     */
}
