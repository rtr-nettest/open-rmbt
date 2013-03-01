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

import android.content.Context;
import android.content.SharedPreferences;
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
        getSharedPreferences(context).edit().putString("uuid", uuid).commit();
    }
    
    public static String getUUID(final Context context)
    {
        return getSharedPreferences(context).getString("uuid", "");
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
    
    public static boolean isDevMode(final Context context)
    {
        return isDevMode(getSharedPreferences(context));
    }
    
    public static String getPreviousTestStatus(final Context context)
    {
        return getSharedPreferences(context).getString("previous_test_status", null);
    }
    
    public static void setPreviousTestStatus(final Context context, final String status)
    {
        getSharedPreferences(context).edit().putString("previous_test_status", status).commit();
    }
    
    public static int getNextTestCounter(final Context context)
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
    
    private static boolean isDevMode(final SharedPreferences pref)
    {
        return pref.getBoolean("dev_mode", false);
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
        final boolean devMode = isDevMode(pref);
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
        final boolean devMode = isDevMode(pref);
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
        final boolean devMode = isDevMode(pref);
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
    
    public static String getTestServerNameOverride(final Context context)
    {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isDevMode(pref);
        if (!devMode)
            return null;
        else
            return pref.getString("dev_test_hostname", null);
    }
    
    public static int getTestServerPortOverride(final Context context)
    {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isDevMode(pref);
        if (!devMode)
            return -1;
        else
            try
            {
                return Integer.parseInt(pref.getString("dev_test_port", ""));
            }
            catch (final NumberFormatException e)
            {
                return -1;
            }
    }
    
    public static Boolean getTestServerSSLOverride(final Context context)
    {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isDevMode(pref);
        if (!devMode)
            return null;
        else
        {
            if (pref.contains("dev_test_port") && pref.contains("dev_test_ssl"))
                return pref.getBoolean("dev_test_ssl", false);
            return null;
        }
    }
    
    public static Integer getNumThreadsOverride(final Context context)
    {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isDevMode(pref);
        if (!devMode)
            return null;
        else
        {
            if (pref.contains("dev_num_threads"))
                return pref.getInt("dev_num_threads", -1);
            return null;
        }
    }
    
    public static Integer getTestDurationOverride(final Context context)
    {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isDevMode(pref);
        if (!devMode)
            return null;
        else
        {
            if (pref.contains("dev_test_duration"))
                return pref.getInt("dev_test_duration", -1);
            return null;
        }
    }
}
