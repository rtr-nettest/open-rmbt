/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
 * Copyright 2013-2014 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
package at.rtr.rmbt.shared;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;

import com.google.common.net.InetAddresses;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;

public abstract class GeoIPHelper
{
    private static volatile boolean lookupServiceFailure;
    private static volatile DatabaseReader lookupService;
    private final static Object LOOKUP_SERVICE_LOCK = new Object(); 
    
    private static DatabaseReader getLookupService()
    {
        if (lookupService != null) {
            return lookupService;
        }
        synchronized (LOOKUP_SERVICE_LOCK)
        {
            if (lookupServiceFailure) {
                return null;
            }
            // A File object pointing to your GeoIP2 or GeoLite2 database
            File database = new File("/usr/share/GeoIP/GeoLite2-Country.mmdb");
            try
            {
                lookupService = new DatabaseReader.Builder(database).build();
                return lookupService;
            }
            catch (Exception e)
            {
                lookupServiceFailure = true;
                System.out.println("Maxmind GeoIP database could not be loaded");
                return null;
            }
        }
    }

    
    public static String lookupCountry(final InetAddress adr)
    {
        try
        {
            CountryResponse country = getLookupService().country(adr);
            String countryCode = country.getCountry().getIsoCode();
            return countryCode;
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            return null;
        }
    }
    
    
    //???
    public static void main(String[] args)
    {
        System.out.println(lookupCountry(InetAddresses.forString("2a01:190:1700:39::75")));
        System.out.println(lookupCountry(InetAddresses.forString("78.47.24.204")));
    }
}
