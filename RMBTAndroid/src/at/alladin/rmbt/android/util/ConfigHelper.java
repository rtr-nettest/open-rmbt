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
package at.alladin.rmbt.android.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import at.alladin.rmbt.client.helper.Config;
import at.alladin.rmbt.client.helper.ConfigLocal;

public final class ConfigHelper
{
    public static SharedPreferences getSharedPreferences(final Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public static void setUUID(final Context context, final String uuid)
    {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isOverrideControlServer(pref);
        pref.edit().putString(devMode ? "uuid_dev" : "uuid", uuid).commit();
    }
    
    public static String getUUID(final Context context)
    {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isOverrideControlServer(pref);
        return pref.getString(devMode ? "uuid_dev" : "uuid", "");
    }
    
    public static void setLastNewsUid(final Context context, final long newsUid)
    {
        getSharedPreferences(context).edit().putLong("lastNewsUid", newsUid).commit();
    }
    
    public static long getLastNewsUid(final Context context)
    {
        return getSharedPreferences(context).getLong("lastNewsUid", 0);
    }
    
    public static boolean isGPS(final Context context)
    {
        return ! getSharedPreferences(context).getBoolean("no_gps", false);
    }
    
    public static boolean isRepeatTest(final Context context)
    {
        if (! isDevEnabled(context))
            return false;
        return getSharedPreferences(context).getBoolean("repeat_test", false);
    }
    
    public static boolean isNDT(final Context context)
    {
        return getSharedPreferences(context).getBoolean("ndt", false);
    }
    
    public static void setNDT(final Context context, final boolean value)
    {
        getSharedPreferences(context).edit().putBoolean("ndt", value).commit();
    }
    
    public static boolean isNDTDecisionMade(final Context context)
    {
        return getSharedPreferences(context).getBoolean("ndt_decision", false);
    }
    
    public static void setNDTDecisionMade(final Context context, final boolean value)
    {
        getSharedPreferences(context).edit().putBoolean("ndt_decision", value).commit();
    }
    
    public static String getPreviousTestStatus(final Context context)
    {
        return getSharedPreferences(context).getString("previous_test_status", null);
    }
    
    public static void setPreviousTestStatus(final Context context, final String status)
    {
        getSharedPreferences(context).edit().putString("previous_test_status", status).commit();
    }
    
    public static int getTestCounter(final Context context)
    {
        int counter = getSharedPreferences(context).getInt("test_counter", 0);
        return counter;
    }
    
    public static int incAndGetNextTestCounter(final Context context)
    {
        int lastValue = getSharedPreferences(context).getInt("test_counter", 0);
        lastValue++;
        getSharedPreferences(context).edit().putInt("test_counter", lastValue).commit();
        return lastValue;
    }
    
    public static boolean isTCAccepted(final Context context)
    {
        return getSharedPreferences(context).getBoolean("terms_and_conditions_accepted", false);
    }
    
    public static void setTCAccepted(final Context context, final boolean accepted)
    {
        getSharedPreferences(context).edit().putBoolean("terms_and_conditions_accepted", accepted).commit();
    }
    
    private static boolean isOverrideControlServer(final SharedPreferences pref)
    {
        return pref.getBoolean("dev_control_override", false);
    }
    
    private static boolean isOverrideMapServer(final SharedPreferences pref)
    {
        return pref.getBoolean("dev_map_override", false);
    }
    
    private static String getDefaultControlServerName(final SharedPreferences pref)
    {
        final boolean ipv4Only = pref.getBoolean("ipv4_only", false);
        if (ipv4Only)
            return ConfigLocal.RMBT_CONTROL_HOST_IPV4;
        else
            return ConfigLocal.RMBT_CONTROL_HOST;
    }
    
    public static String getControlServerName(final Context context)
    {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isOverrideControlServer(pref);
        if (devMode)
        {
            final boolean noControlServer = pref.getBoolean("dev_no_control_server", false);
            if (noControlServer)
                return null;
            return pref.getString("dev_control_hostname", getDefaultControlServerName(pref));
        }
        else
            return getDefaultControlServerName(pref);
    }
    
    public static int getControlServerPort(final Context context)
    {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isOverrideControlServer(pref);
        if (devMode)
        {
            final boolean noControlServer = pref.getBoolean("dev_no_control_server", false);
            if (noControlServer)
                return -1;
            
            try
            {
                return Integer.parseInt(pref.getString("dev_control_port", ""));
            }
            catch (final NumberFormatException e)
            {
                return Config.RMBT_CONTROL_PORT;
            }
        }
        else
            return Config.RMBT_CONTROL_PORT;
    }
    
    public static boolean isControlSeverSSL(final Context context)
    {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isOverrideControlServer(pref);
        if (devMode)
        {
            final boolean noControlServer = pref.getBoolean("dev_no_control_server", false);
            if (noControlServer)
                return false;
            if (pref.contains("dev_control_port"))
                return pref.getBoolean("dev_control_ssl", Config.RMBT_CONTROL_SSL);
            return Config.RMBT_CONTROL_SSL;
        }
        else
            return Config.RMBT_CONTROL_SSL;
    }
    
    public static String getMapServerName(final Context context)
    {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isOverrideMapServer(pref);
        if (devMode)
            return pref.getString("dev_map_hostname", "");
        else
        {
            final String host = mapHost.get();
            if (host != null)
                return host;
            else
                return getControlServerName(context);
        }
    }
    
    public static int getMapServerPort(final Context context)
    {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isOverrideMapServer(pref);

        if (devMode)
        {
            try
            {
                return Integer.parseInt(pref.getString("dev_map_port", ""));
            }
            catch (final NumberFormatException e)
            {
                return -1;
            }
        }
        else
        {
            if (mapHost.get() != null)
                return mapPort.get();
            else
                return getControlServerPort(context);
        }
    }
    
    public static boolean isMapSeverSSL(final Context context)
    {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isOverrideMapServer(pref);
        if (devMode)
            return pref.getBoolean("dev_map_ssl", false);
        else
        {
            if (mapHost.get() != null)
                return mapSSL.get();
            else
                return isControlSeverSSL(context);
        }
    }
    
    private static AtomicReference<String> mapHost = new AtomicReference<String>();
    private static AtomicInteger mapPort = new AtomicInteger();
    private static AtomicBoolean mapSSL = new AtomicBoolean();
    
    public static void setMapServer(final String host, final int port, final boolean ssl)
    {
        mapHost.set(host);
        mapPort.set(port);
        mapSSL.set(ssl);
    }
    
    private static ConcurrentMap<String, String> volatileSettings = new ConcurrentHashMap<String, String>();
    
    public static ConcurrentMap<String, String> getVolatileSettings()
    {
        return volatileSettings;
    }
    
    public static String getVolatileSetting(String key)
    {
        return volatileSettings.get(key);
    }
    
    public static boolean isDevEnabled(final Context ctx)
    {
        return PackageManager.SIGNATURE_MATCH == ctx.getPackageManager().checkSignatures(ctx.getPackageName(), at.alladin.rmbt.android.util.Config.RMBT_DEV_UNLOCK_PACKAGE_NAME);
    }
}
