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
package at.alladin.rmbt.android.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.client.helper.TestStatus;

public final class Helperfunctions
{
    
    public static String getLang()
    {
        return Locale.getDefault().getLanguage();
    }
    
    public static DateFormat getDateFormat(final boolean seconds)
    {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, seconds ? DateFormat.MEDIUM : DateFormat.SHORT);
    }
    
    public static String formatTimestampWithTimezone(final long time, final String timezoneID, final boolean seconds)
    {
        return formatTimestampWithTimezone(new Date(), getDateFormat(seconds), time, timezoneID);
    }
    
    public static String formatTimestampWithTimezone(final Date date, final DateFormat dateFormat, final long time,
            final String timezoneID)
    {
        if (time == 0)
            return null;
        date.setTime(time);
        dateFormat.setTimeZone(Helperfunctions.getTimeZone(timezoneID));
        return dateFormat.format(date);
    }
    
    // public static SimpleDateFormat getDateFormatLb() {
    // SimpleDateFormat format;
    //
    // if (getLang().equals("de")) {
    // format = new SimpleDateFormat("dd.MM.yyyy\nHH:mm");
    // }
    // else {
    // format = new SimpleDateFormat("yyyy-MM-dd\nHH:mm");
    // }
    //
    // return format;
    // }
    
    public static String getTimezone()
    {
        return TimeZone.getDefault().getID();
    }
    
    public static TimeZone getTimeZone(final String id)
    {
        if (id == null)
            return TimeZone.getDefault();
        else
            return TimeZone.getTimeZone(id);
    }
    
    public static int dpToPx(final float dp, final float scale)
    {
        if (dp > 0 && scale > 0)
            return (int) (dp * scale + 0.5f);
        else
            return 0;
    }
    
    public static String getLocationString(final Context context, final Resources res, final Location location, final int line)
    {
    	switch (line) {
    	case 0: // used for main screen
    		return String.format("%s %s", convertLocation(res, location.getLatitude(), true),
    				convertLocation(res, location.getLongitude(), false));
    	case 1: // used for test screen, additional altitude information
    		return String.format("%s %s %s", convertLocation(res, location.getLatitude(), true),
    				convertLocation(res, location.getLongitude(), false),
    				convertLocationAltitude(res, location.hasAltitude(), location.getAltitude()));
    	default:
    		if (!ConfigHelper.isDevEnabled(context))
    			return String.format("%s (%s)", convertLocationProvider(res, location.getProvider()),
    					convertLocationAccuracy(res, location.hasAccuracy(), location.getAccuracy(),0));
    		else
    			// additional debug information
    			return String.format("%s (%s) %s %s",
    					convertLocationProvider(res, location.getProvider()),
    					convertLocationAccuracy(res, location.hasAccuracy(), location.getAccuracy(),0),
    					convertLocationSpeed(res, location.hasSpeed(), location.getSpeed()),
    					convertLocationTime(location.getTime()));
    	}

    }
    
    public static String convertLocation(final Resources res, final double coordinate, final boolean latitude)
    {
        final String rawStr = Location.convert(coordinate, Location.FORMAT_MINUTES);
        //[+-]DDD:MM.MMMMM - FORMAT_MINUTES
        final String[] split = rawStr.split(":");
        final String direction;
        float min = 0f;
       
        
        try
        {
            split[1] = split[1].replace(",",".");
            min = Float.parseFloat(split[1]);
        }
        catch (NumberFormatException e)
        {
        }
        
        if (latitude)
        {
            if (coordinate >= 0)
                direction = res.getString(R.string.test_location_dir_n);
            else
                direction = res.getString(R.string.test_location_dir_s);
        }
        else if (coordinate >= 0)
            direction = res.getString(R.string.test_location_dir_e);
        else
            direction = res.getString(R.string.test_location_dir_w);
        return String.format("%s %s°%.3f'", direction, split[0].replace("-", ""), min);
    }
    
    public static String convertLocationSpeed(final Resources res, final boolean hasspeed, final float speed)
    {   
        if ((!hasspeed) || (speed <= 1.0)) // ignore speed < 3.6km
            return "";
        else 
            return String.format ("%.0f %s", speed*3.6,res.getString(R.string.test_location_km_h));
    }
    
    public static String convertLocationAccuracy(final Resources res, final boolean hasaccuracy, final float accuracy, final int satellites)
    {   
        if ((!hasaccuracy)  || (accuracy < 0)) // ignore negative or null
            return "";
        else 
        	if (satellites > 0)
           		return String.format ("+/-%.0f %s (%d %s)", accuracy,res.getString(R.string.test_location_m),satellites,res.getString(R.string.test_location_sat));
        	else
        		return String.format ("+/-%.0f %s", accuracy,res.getString(R.string.test_location_m));
    }
    
    public static String convertLocationAltitude(final Resources res, final boolean hasaltitude, final double altitude)
    {   
        if ((!hasaltitude)  || (altitude < 0)) // ignore negative or null
            return "";
        else
            return String.format ("%.0f %s", altitude,res.getString(R.string.test_location_m));
    }
    
    public static String convertLocationProvider(final Resources res, final String provider)
    {   
        final String localized_provider;
        
        if (provider == null)
            return "";
        else {
            if (provider.equals("gps"))
                localized_provider = res.getString(R.string.test_location_gps);
            else if (provider.equals("network"))
                localized_provider = res.getString(R.string.test_location_network);
            else  //fallback for unexpected string
                localized_provider = provider;
            return String.format ("%s", localized_provider);
        }
    }
    
    public static String convertLocationTime(final long time)
    {   
        final long age;
        
        age = System.currentTimeMillis()-time; // in ms
        if (age < 1000) // < 1s
            return "< 1s";
        else
            return String.format ("%d s", age/1000);
    }
    
    public static String getNetworkTypeName(final int type)
    {
        switch (type)
        {
        case 1:
            return "GSM";
        case 2:
            return "EDGE";
        case 3:
            return "UMTS";
        case 4:
            return "CDMA";
        case 5:
            return "EVDO_0";
        case 6:
            return "EVDO_A";
        case 7:
            return "1xRTT";
        case 8:
            return "HSDPA";
        case 9:
            return "HSUPA";
        case 10:
            return "HSPA";
        case 11:
            return "IDEN";
        case 12:
            return "EVDO_B";
        case 13:
            return "LTE";
        case 14:
            return "EHRPD";
        case 15:
            return "HSPA+";
        case 98:
            return "LAN";
        case 99:
            return "WLAN";
        case 106:
        	return "ETHERNET";
        case 107:
        	return "BLUETOOTH";
        default:
            return "UNKNOWN";
        }
    }
    
    public static String getMapType(final int type)
    {
        if (type == 98)
            return "browser";
        if (type == 99)
            return "wifi";
        return "mobile";
    }
    
    public static String getTestStatusString(final Resources res, final TestStatus status)
    {
        switch (status)
        {
        case INIT:
            return res.getString(R.string.test_bottom_test_status_init);
            
        case PING:
            return res.getString(R.string.test_bottom_test_status_ping);
            
        case DOWN:
            return res.getString(R.string.test_bottom_test_status_down);
            
        case INIT_UP:
            return res.getString(R.string.test_bottom_test_status_init_up);
            
        case UP:
            return res.getString(R.string.test_bottom_test_status_up);
            
        case SPEEDTEST_END:
            return res.getString(R.string.test_bottom_test_status_end);
            
        case ERROR:
            return res.getString(R.string.test_bottom_test_status_error);
            
        case ABORTED:
            return res.getString(R.string.test_bottom_test_status_aborted);
            
		default:
			break;
            
        }
        return null;
    }
    
    public static String filterIP(final InetAddress inetAddress)
    {
        try
        {
            final String ipVersion;
            if (inetAddress instanceof Inet4Address)
                ipVersion = "ipv4";
            else if (inetAddress instanceof Inet6Address)
                ipVersion = "ipv6";
            else
                ipVersion = "ipv?";
            
            if (inetAddress.isAnyLocalAddress())
                return "wildcard";
            if (inetAddress.isSiteLocalAddress())
                return "site_local_" + ipVersion;
            if (inetAddress.isLinkLocalAddress())
                return "link_local_" + ipVersion;
            if (inetAddress.isLoopbackAddress())
                return "loopback_" + ipVersion;
            return inetAddress.getHostAddress();
        }
        catch (final IllegalArgumentException e)
        {
            return "illegal_ip";
        }
    }
    
    public static String byteArrayToHexString(final byte[] b)
    {
        if (b == null || b.length == 0)
            return null;
        final StringBuffer sb = new StringBuffer(b.length * 3 - 1);
        for (final byte element : b)
        {
            if (sb.length() > 0)
                sb.append(':');
            final int v = element & 0xff;
            if (v < 16)
                sb.append('0');
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }
    
    /**
     * 
     * @param classification
     * @return
     */
    public static int getClassificationColor(final int classification)
    {
        switch (classification)
        {
        case 0:
            return R.drawable.traffic_lights_grey;
        case 1:
            return R.drawable.traffic_lights_red;
        case 2:
            return R.drawable.traffic_lights_yellow;
        case 3:
            return R.drawable.traffic_lights_green;
        case 4:
        	return R.drawable.traffic_lights_ultra_green;
        default:
            return R.drawable.traffic_lights_none;
        }
    }
    
    public static String removeQuotationsInCurrentSSIDForJellyBean(String ssid)
    {
        if (Build.VERSION.SDK_INT >= 17 && ssid != null && ssid.startsWith("\"") && ssid.endsWith("\""))
            ssid = ssid.substring(1, ssid.length() - 1);
        return ssid;
    }
}
