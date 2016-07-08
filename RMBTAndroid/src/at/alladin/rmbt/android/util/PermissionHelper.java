/*******************************************************************************
 * Copyright 2016 Specure GmbH
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
package at.alladin.rmbt.android.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;

public abstract class PermissionHelper
{
    private static final String TAG = "PermissionHelper";
    
    public static final int REQUEST_AT_INIT = 1;
    public static final int REQUEST_AT_TEST_START = 2;
    protected static final String PREF_LAST_TIME_PERMISSION_SHOWN = "lastTimePermissionShown";
    protected static final long SHOW_PERMISSION_DIALOG_INTERVAL_SEC = 3600; // 1h
    
//    protected static final long SHOW_PERMISSION_DIALOG_INTERVAL_SEC = 10; // TODO: REMOVE, only for debugging
    
    protected static final ConcurrentHashMap<String, Boolean> DYNAMIC_PERMISSIONS = new ConcurrentHashMap<String, Boolean>();
    
    protected static class PermissionGroup
    {
        private final String groupName;
        private final String[] permissions;
        private final int dialogTitle;
        private final int dialogMessage;
        
        public PermissionGroup(String groupName, String[] permissions)
        {
            this.groupName = groupName;
            this.dialogTitle = -1;
            this.dialogMessage = -1;
            this.permissions = permissions;
        }
        
        public PermissionGroup(String groupName, int dialogTitle, int dialogMessage, String[] permissions)
        {
            this.groupName = groupName;
            this.dialogTitle = dialogTitle;
            this.dialogMessage = dialogMessage;
            this.permissions = permissions;
        }
    }
    
    protected static final PermissionGroup LOCATION_GROUP = new PermissionGroup("location", 
            new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    }
    );
    
    protected static final PermissionGroup TELEPHONY_GROUP = new PermissionGroup("telephony",
            R.string.permission_telephony_dialog_title,
            R.string.permission_telephony_dialog_text,
            new String[] {
                    Manifest.permission.READ_PHONE_STATE
                    }
    );
    
    protected static final PermissionGroup[] GROUPS = new PermissionGroup[] { LOCATION_GROUP, TELEPHONY_GROUP };
    
    protected static final String[] PERMISSIONS = mergePermissions(GROUPS);
    
    protected static String[] mergePermissions(PermissionGroup[] groups)
    {
        final List<String> permissions = new ArrayList<String>();
        for (PermissionGroup group : groups)
            permissions.addAll(Arrays.asList(group.permissions));
        return permissions.toArray(new String[permissions.size()]);
    }
    
    public static boolean checkPermission(Context ctx, String permission)
    {
        return ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED;
    }
    
    public static boolean checkAllPermissions(Context ctx, List<String> permissions)
    {
        for (String permission : permissions)
            if (! checkPermission(ctx, permission))
                return false;
        return true;
    }
    
    public static boolean checkAllPermissions(Context ctx, String[] permissions)
    {
        for (String permission : permissions)
            if (! checkPermission(ctx, permission))
                return false;
        return true;
    }
    
    public static boolean checkAnyPermission(Context ctx, String[] permissions)
    {
        for (String permission : permissions)
            if (checkPermission(ctx, permission))
                return true;
        return false;
    }
    
    public static boolean checkCoarseLocationPermission(Context ctx)
    {
        return checkPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION);
    }
    
    public static boolean checkFineLocationPermission(Context ctx)
    {
        return checkPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION);
    }
    
    public static boolean checkReadPhoneStatePermission(Context ctx)
    {
        return checkPermission(ctx, Manifest.permission.READ_PHONE_STATE);
    }
    
    public static boolean checkAnyLocationPermission(Context ctx)
    {
        return checkAnyPermission(ctx, LOCATION_GROUP.permissions);
    }
    
    protected static boolean checkAllDynamicPermissions(Context ctx, String[] permissions)
    {
        return checkAllPermissions(ctx, filterDynamicPermissions(PERMISSIONS));
    }
    
    protected static List<String> getMissingDynamicPermissions(Context ctx, String[] permissions)
    {
        final List<String> missingPermissions = new ArrayList<String>();
        for (String permission : filterDynamicPermissions(permissions))
        {
            if (! checkPermission(ctx, permission))
                missingPermissions.add(permission);
        }
        return missingPermissions;
    }
    
    protected static List<String> filterDynamicPermissions(String[] permissions)
    {
        final List<String> result = new ArrayList<String>();
        for (String permission : permissions)
        {
            final Boolean request = DYNAMIC_PERMISSIONS.get(permission);
            if (request == null || request) // default to true if not set
                result.add(permission);
        }
        return result;
    }
    
    protected static boolean shouldShowRequestPermissionRationale(Activity act, String[] permissions)
    {
        for (String permission : filterDynamicPermissions(permissions))
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(act, permission))
                return true;
        }
        return false;
    }
    
    public static void checkPermissionAtTestStartAndStartTest(final RMBTMainActivity act)
    {
        if (checkAllDynamicPermissions(act, PERMISSIONS)) // everything is fine
        {
            act.startTest();
            return;
        }
        
        final List<PermissionGroup> neededGroups = new ArrayList<PermissionHelper.PermissionGroup>();
        for (PermissionGroup group : GROUPS)
        {
            if (! checkAllDynamicPermissions(act, group.permissions) && isItTimeToShowPermissionDialogAgain(act, group.groupName))
                neededGroups.add(group);
        }
        
        if (neededGroups.isEmpty()) // nothing to do
        {
            act.startTest();
            return;
        }
        
        final List<String> neededPermissions = new ArrayList<String>();
        handleGroup(act, neededGroups, neededPermissions, 0);
    }
    
    protected static void handleGroup(final RMBTMainActivity act, final List<PermissionGroup> neededGroups, final List<String> neededPermissions, final int i)
    {
        final boolean afterLastGroup = i >= neededGroups.size();
        
        if (!afterLastGroup)
        {
            final PermissionGroup group = neededGroups.get(i);
            
            updatePermissionDialogTime(act, group.groupName);
            
            if (group.dialogTitle != -1 && group.dialogMessage != -1 && shouldShowRequestPermissionRationale(act, group.permissions))
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(act);
                builder
                    .setTitle(group.dialogTitle)
                    .setMessage(group.dialogMessage)
                    /*
                    .setNegativeButton("Abbrechen", new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            handleGroup(act, neededGroups, neededPermissions, i + 1);
                        }
                    })
                    */
                    .setPositiveButton("Weiter", new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            neededPermissions.addAll(getMissingDynamicPermissions(act, group.permissions));
                            handleGroup(act, neededGroups, neededPermissions, i + 1);
                        }
                    })
                    .show();
            }
            else
            {
                neededPermissions.addAll(getMissingDynamicPermissions(act, group.permissions));
                handleGroup(act, neededGroups, neededPermissions, i + 1);
            }
        }
        else // afterLastGroup
        {
            if (neededPermissions.isEmpty())
                act.startTest();
            else
                ActivityCompat.requestPermissions(act, neededPermissions.toArray(new String[neededPermissions.size()]), REQUEST_AT_TEST_START);
        }
    }
    
    public static void checkPermissionAtInit(final Activity act)
    {
        // request location permissions
        if (checkAllPermissions(act, LOCATION_GROUP.permissions))
            return;
        updatePermissionDialogTime(act, LOCATION_GROUP.groupName);
        ActivityCompat.requestPermissions(act, LOCATION_GROUP.permissions, REQUEST_AT_INIT);
    }
    
    public static boolean isItTimeToShowPermissionDialogAgain(Context ctx, String groupName)
    {
        final long lastTime = ConfigHelper.getSharedPreferences(ctx).getLong(PREF_LAST_TIME_PERMISSION_SHOWN + "_" + groupName, 0);
        return (lastTime <= 0 || lastTime + SHOW_PERMISSION_DIALOG_INTERVAL_SEC * 1000 < System.currentTimeMillis());
    }
    
    public static void updatePermissionDialogTime(Context ctx, String groupName)
    {
        ConfigHelper.getSharedPreferences(ctx).edit().putLong(PREF_LAST_TIME_PERMISSION_SHOWN + "_" + groupName, System.currentTimeMillis()).commit();
    }

    public static JSONArray getPermissionStatusAsJSONArray(Context ctx)
    {
        final JSONArray result = new JSONArray();
        for (String permission : PERMISSIONS)
        {
            final JSONObject obj = new JSONObject();
            try
            {
                obj.put("permission", permission);
                obj.put("status", checkPermission(ctx, permission));
                result.put(obj);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        Log.d(TAG, result.toString());
        return result;
    }
    
    public static void setRequestPermissions(JSONArray permissions)
    {
        DYNAMIC_PERMISSIONS.clear();
        for (int i = 0; i < permissions.length(); i++)
        {
            final JSONObject obj = permissions.optJSONObject(i);
            if (obj != null)
            {
                final String permission = obj.optString("permission");
                final boolean request = obj.optBoolean("request");
                if (permission != null)
                    DYNAMIC_PERMISSIONS.put(permission, request);
            }
        }
    }

}
