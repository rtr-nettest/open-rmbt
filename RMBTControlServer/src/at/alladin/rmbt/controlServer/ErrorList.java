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
package at.alladin.rmbt.controlServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONException;

public class ErrorList
{
    
    private JSONArray errorList = null;
    
    private PropertyResourceBundle labels = null;
    
    private final List<String> labelList;
    
    public ErrorList()
    {
        labelList = new ArrayList<String>();
        
        errorList = new JSONArray();
        
        labels = (PropertyResourceBundle) ResourceBundle.getBundle("at.alladin.rmbt.res.SystemMessages");
    }
    
    public void addError(final String errorLabel)
    {
        if (!labelList.contains(errorLabel))
            try
            {
                final String errorText = labels.getString(errorLabel);
                addErrorString(errorText);
            }
            catch (final MissingResourceException e)
            {
                System.out.println("Error writing to ErrorList: Label" + errorLabel + "not found in"
                        + labels.getLocale().toString());
            }
            catch (final NullPointerException e)
            {
                System.out.println("Error writing to ErrorList: Label" + errorLabel + "not found in"
                        + labels.getLocale().toString());
            }
        
    }
    
    public void addErrorString(final String errorText)
    {
        try
        {
            errorList.put(errorList.length(), errorText);
            System.out.println(errorText);
        }
        catch (final JSONException e)
        {
            System.out.println("Error writing ErrorList: " + e.toString());
        }
    }
    
    public void setLanguage(final String lang)
    {
        labels = (PropertyResourceBundle) ResourceBundle.getBundle("at.alladin.rmbt.res.SystemMessages", new Locale(
                lang));
    }
    
    public int getLength()
    {
        return errorList.length();
    }
    
    public JSONArray getList()
    {
        return errorList;
    }
    
}
