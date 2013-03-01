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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.util.Log;

// TODO GET RID OF FILE - JUST FOR MIGRATION FROM vc <= 51 | (<=0.9.11)

public class AppSettings
{
    
    private static String DEBUG_TAG = "AppSettings";
    
    private static AppSettings instance = null;
    
    private String uuid = "";
    private String lastOpened = "";
    // private String agbs = "";
    
    private Context context = null;
    
    private AppSettings(final Context context)
    {
        
        this.context = context;
        final File installation = new File(context.getFilesDir(), Config.RMBT_SETTINGS_FILENAME);
        try
        {
            if (installation.exists())
            {
                final String tmpString = readInstallationFile(Config.RMBT_SETTINGS_FILENAME);
                
                final String[] values = tmpString.split(",");
                
                if (values.length > 0)
                {
                    uuid = values[0];
                    Log.i(DEBUG_TAG, uuid);
                }
                if (values.length > 1)
                {
                    lastOpened = values[1];
                    Log.i(DEBUG_TAG, lastOpened);
                }
            }
            else
            {
                uuid = "";
                lastOpened = "";
            }
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
        
    }
    
    public static AppSettings getInstance(final Context context)
    {
        if (instance == null)
            instance = new AppSettings(context);
        return instance;
    }
    
    public String getUUID()
    {
        
        return uuid;
        
    }
    
    public String getLastOpened()
    {
        
        return lastOpened;
        
    }
    
    public boolean setUUID(final String newUuid)
    {
        boolean success = false;
        
        if (newUuid != null && newUuid.length() != 0)
        {
            uuid = newUuid;
            try
            {
                writeInstallationFile(Config.RMBT_SETTINGS_FILENAME, getFileContent());
                success = true;
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        
        return success;
        
    }
    
    public boolean setLastOpened(final String newTStamp)
    {
        boolean success = false;
        
        if (newTStamp != null && newTStamp.length() > 0)
        {
            lastOpened = newTStamp;
            try
            {
                writeInstallationFile(Config.RMBT_SETTINGS_FILENAME, getFileContent());
                success = true;
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return success;
    }
    
    private String readInstallationFile(final String filename) throws IOException
    {
        final FileInputStream fis = context.openFileInput(filename);
        final InputStreamReader in = new InputStreamReader(fis);
        final BufferedReader br = new BufferedReader(in);
        final String data = br.readLine();
        
        fis.close();
        return data;
    }
    
    private void writeInstallationFile(final String filename, final String fileContent) throws IOException
    {
        final FileOutputStream out = context.openFileOutput(filename, Context.MODE_PRIVATE);
        out.write(fileContent.getBytes());
        out.close();
    }
    
    private String getFileContent()
    {
        return uuid + "," + lastOpened;
    }
    
    public void removeFile()
    {
        try
        {
            context.deleteFile(Config.RMBT_SETTINGS_FILENAME);
        }
        catch (Exception e)
        {
        }
    }
    
    /*
     * public String getAgbs() { File installation = new
     * File(context.getFilesDir(), Config.RMBT_AGB_FILENAME); try { if
     * (installation.exists()) { agbs =
     * readInstallationFile(Config.RMBT_AGB_FILENAME); } else { agbs = ""; } }
     * catch (Exception e) { throw new RuntimeException(e); }
     * 
     * return agbs; }
     * 
     * public boolean setAgbs(String newAgbs) { boolean success = false;
     * 
     * if (newAgbs != null && newAgbs.length() > 0) { agbs = newAgbs; try {
     * writeInstallationFile(Config.RMBT_SETTINGS_FILENAME, agbs); success =
     * true; } catch (Exception e) { throw new RuntimeException(e); } } return
     * success; }
     */
}
