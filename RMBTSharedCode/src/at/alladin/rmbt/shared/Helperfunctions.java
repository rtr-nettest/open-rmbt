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
package at.alladin.rmbt.shared;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.postgresql.util.Base64;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.google.common.net.InetAddresses;

public class Helperfunctions
{
    
    // Suppress default constructor for noninstantiability
    private Helperfunctions()
    {
        throw new AssertionError();
    }
    
    public static String calculateHMAC(final String secret, final String data)
    {
        try
        {
            final SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA1");
            final Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            final byte[] rawHmac = mac.doFinal(data.getBytes());
            final String result = new String(Base64.encodeBytes(rawHmac));
            return result;
        }
        catch (final GeneralSecurityException e)
        {
            
            System.out.println("Unexpected error while creating hash: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * @return
     */
    public static String getTimezoneId()
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
    
    public static Calendar getTimeWithTimeZone(final String timezoneId)
    {
        
        final TimeZone timeZone = TimeZone.getTimeZone(timezoneId);
        
        final Calendar timeWithZone = Calendar.getInstance(timeZone);
        
        return timeWithZone;
    }
    
    public static SimpleDateFormat getDateFormat(final String lang)
    {
        SimpleDateFormat format;
        
        if (lang.equals("de"))
            format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        else
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        return format;
    }
    
    public static String geoToString(final Double geoLat, final Double geoLong)
    {
        
        if (geoLat == null || geoLong == null)
            return null;
        
        int latd, lond; // latitude degrees and minutes, longitude degrees and
                        // minutes
        double latm, lonm; // latitude and longitude seconds.
        
        // decimal degrees to degrees minutes seconds
        
        double temp;
        // latitude
        temp = java.lang.Math.abs(geoLat);
        latd = (int) temp;
        latm = (temp - latd) * 60.0;
        if (geoLat < 0)
            latd = -latd;
        
        // longitude
        temp = java.lang.Math.abs(geoLong);
        lond = (int) temp;
        lonm = (temp - lond) * 60.0;
        if (geoLong < 0)
            lond = -lond;
        
        final String dirLat;
        if (geoLat >= 0)
            dirLat = "N";
        else
            dirLat = "S";
        final String dirLon;
        if (geoLong >= 0)
            dirLon = "E";
        else
            dirLon = "W";
        
        return String.format("%s %02d°%02.3f'  %s %02d°%02.3f'", dirLat, latd, latm, dirLon, lond, lonm);
    }
    
    public static String getNetworkTypeName(final int type)
    {
        
        // TODO: read from DB
        switch (type)
        {
        case 1:
            return "2G (GSM)";
        case 2:
            return "2G (EDGE)";
        case 3:
            return "3G (UMTS)";
        case 4:
            return "2G (CDMA)";
        case 5:
            return "2G (EVDO_0)";
        case 6:
            return "2G (EVDO_A)";
        case 7:
            return "2G (1xRTT)";
        case 8:
            return "3G (HSDPA)";
        case 9:
            return "3G (HSUPA)";
        case 10:
            return "3G (HSPA)";
        case 11:
            return "2G (IDEN)";
        case 12:
            return "2G (EVDO_B)";
        case 13:
            return "4G (LTE)";
        case 14:
            return "2G (EHRPD)";
        case 15:
            return "3G (HSPA+)";
        case 97:
            return "CLI";
        case 98:
            return "BROWSER";
        case 99:
            return "WLAN";
        case 101:
            return "2G/3G";
        case 102:
            return "3G/4G";
        case 103:
            return "2G/4G";
        case 104:
            return "2G/3G/4G";
        default:
            return "UNKNOWN";
        }
    }
    
    public static String getRoamingType(final ResourceBundle labels, final int roamingType)
    {
        final String roamingValue;
        switch (roamingType)
        {
        case 1:
            roamingValue = labels.getString("value_roaming_national");
            break;
        case 2:
            roamingValue = labels.getString("value_roaming_international");
            break;
        default:
            roamingValue = "?";
            break;
        }
        return roamingValue;
    }
    
    public static boolean isIPLocal(final InetAddress adr)
    {
        return adr.isLinkLocalAddress() || adr.isLoopbackAddress() || adr.isSiteLocalAddress();
    }
    
    public static String filterIpString(final String input)
    {
        try
        {
            final InetAddress inetAddress = InetAddresses.forString(input);
            final String ipVersion;
            if (inetAddress instanceof Inet4Address)
                ipVersion = "ipv4";
            else if (inetAddress instanceof Inet6Address)
                ipVersion = "ipv6";
            else
                ipVersion = "ipv?";
            
            if (inetAddress.isAnyLocalAddress())
                return "wildcard_" + ipVersion;
            if (inetAddress.isSiteLocalAddress())
                return "site_local_" + ipVersion;
            if (inetAddress.isLinkLocalAddress())
                return "link_local_" + ipVersion;
            if (inetAddress.isLoopbackAddress())
                return "loopback_" + ipVersion;
            return InetAddresses.toAddrString(inetAddress);
        }
        catch (final IllegalArgumentException e)
        {
            return "illegal_ip";
        }
    }
    
    public static String anonymizeIpString(final String input)
    {
        try
        {
            final InetAddress inetAddress = InetAddresses.forString(input);
            final byte[] address = inetAddress.getAddress();
            address[address.length - 1] = 0;
            if (address.length > 4) // ipv6
            {
                for (int i = 6; i < address.length; i++)
                    address[i] = 0;
            }
            
            String result = InetAddresses.toAddrString(InetAddress.getByAddress(address));
            if (address.length == 4)
                result = result.replaceFirst(".0$", "");
            return result;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String getNatType(final String localIP, final String publicIP)
    {
        try
        {
            final InetAddress localAdr = InetAddresses.forString(localIP);
            final InetAddress publicAdr = InetAddresses.forString(publicIP);
            
            final String ipVersion;
            if (publicAdr instanceof Inet4Address)
                ipVersion = "ipv4";
            else if (publicAdr instanceof Inet6Address)
                ipVersion = "ipv6";
            else
                ipVersion = "ipv?";
            
            if (localAdr.equals(publicAdr))
                return "no_nat_" + ipVersion;
            else
            {
                final String localType = isIPLocal(localAdr) ? "local" : "public";
                final String publicType = isIPLocal(publicAdr) ? "local" : "public";
                return String.format("nat_%s_to_%s_%s", localType, publicType, ipVersion);
            }
        }
        catch (final IllegalArgumentException e)
        {
            return "illegal_ip";
        }
    }
    
    public static String reverseDNSLookup(final String ip)
    {
        try
        {
            final InetAddress adr = InetAddresses.forString(ip);
            
            final Name name = ReverseMap.fromAddress(adr);
            
            final Lookup lookup = new Lookup(name, Type.PTR);
            lookup.setResolver(new SimpleResolver());
            lookup.setCache(null);
            final Record[] records = lookup.run();
            if (lookup.getResult() == Lookup.SUCCESSFUL)
                for (final Record record : records)
                    if (record instanceof PTRRecord)
                    {
                        final PTRRecord ptr = (PTRRecord) record;
                        return ptr.getTarget().toString();
                    }
        }
        catch (final Exception e)
        {
        }
        return null;
    }
    
    public static Name getReverseIPName(final InetAddress adr, final Name postfix)
    {
        final byte[] addr = adr.getAddress();
        final StringBuilder sb = new StringBuilder();
        if (addr.length == 4)
            for (int i = addr.length - 1; i >= 0; i--)
            {
                sb.append(addr[i] & 0xFF);
                if (i > 0)
                    sb.append(".");
            }
        else
        {
            final int[] nibbles = new int[2];
            for (int i = addr.length - 1; i >= 0; i--)
            {
                nibbles[0] = (addr[i] & 0xFF) >> 4;
                nibbles[1] = addr[i] & 0xFF & 0xF;
                for (int j = nibbles.length - 1; j >= 0; j--)
                {
                    sb.append(Integer.toHexString(nibbles[j]));
                    if (i > 0 || j > 0)
                        sb.append(".");
                }
            }
        }
        try
        {
            return Name.fromString(sb.toString(), postfix);
        }
        catch (final TextParseException e)
        {
            throw new IllegalStateException("name cannot be invalid");
        }
    }
    
    public static Long getASN(final String ip)
    {
        try
        {
            final InetAddress adr = InetAddresses.forString(ip);
            
            final Name postfix;
            if (adr instanceof Inet6Address)
                postfix = Name.fromConstantString("origin6.asn.cymru.com");
            else
                postfix = Name.fromConstantString("origin.asn.cymru.com");
            
            final Name name = getReverseIPName(adr, postfix);
            System.out.println("lookup: " + name);
            
            final Lookup lookup = new Lookup(name, Type.TXT);
            lookup.setResolver(new SimpleResolver());
            lookup.setCache(null);
            final Record[] records = lookup.run();
            if (lookup.getResult() == Lookup.SUCCESSFUL)
                for (final Record record : records)
                    if (record instanceof TXTRecord)
                    {
                        final TXTRecord txt = (TXTRecord) record;
                        @SuppressWarnings("unchecked")
                        final List<String> strings = txt.getStrings();
                        if (strings != null && !strings.isEmpty())
                        {
                            final String result = strings.get(0);
                            final String[] parts = result.split(" ?\\| ?");
                            if (parts != null && parts.length >= 1)
                                return new Long(parts[0]);
                        }
                    }
        }
        catch (final Exception e)
        {
        }
        return null;
    }
    
    public static String getASName(final long asn)
    {
        try
        {
            final Name postfix = Name.fromConstantString("asn.cymru.com.");
            final Name name = new Name(String.format("AS%d", asn), postfix);
            System.out.println("lookup: " + name);
            
            final Lookup lookup = new Lookup(name, Type.TXT);
            lookup.setResolver(new SimpleResolver());
            lookup.setCache(null);
            final Record[] records = lookup.run();
            if (lookup.getResult() == Lookup.SUCCESSFUL)
                for (final Record record : records)
                    if (record instanceof TXTRecord)
                    {
                        final TXTRecord txt = (TXTRecord) record;
                        @SuppressWarnings("unchecked")
                        final List<String> strings = txt.getStrings();
                        if (strings != null && !strings.isEmpty())
                        {
                            System.out.println(strings);
                            
                            final String result = strings.get(0);
                            final String[] parts = result.split(" ?\\| ?");
                            if (parts != null && parts.length >= 1)
                                return parts[4];
                        }
                    }
        }
        catch (final Exception e)
        {
        }
        return null;
    }
    
}
