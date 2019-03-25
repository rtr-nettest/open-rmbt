/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
 * Copyright 2013-2016 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
package at.rtr.rmbt.controlServer;

import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import at.rtr.rmbt.shared.ResourceManager;
import at.rtr.rmbt.util.model.shared.exception.ErrorStatus;

public class ErrorList
{
    
    private JSONArray errorList = null;
    
    private Set<ErrorStatus> errorFlags = new HashSet<>();
    
    private ResourceBundle labels = null;
    
    public ErrorList()
    {
        errorList = new JSONArray();
        labels = ResourceManager.getSysMsgBundle();
    }

    public void addError(final String errorLabel) {
        addError(errorLabel, true);
    }

    public void addError(final String errorLabel, boolean logError)
    {
        try
        {
            final String errorText = labels.getString(errorLabel);
            addErrorString(errorText, logError);
            
            if ("ERROR_REQUEST_REJECTED".equals(errorLabel)) {
            	errorFlags.add(ErrorStatus.TEST_REJECTED);
            }
            else {
            	errorFlags.add(ErrorStatus.OTHER);
            }
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

    public void addErrorString(final String errorText) {
        addErrorString(errorText, true);
    }

    public void addErrorString(final String errorText, final boolean logError)
    {
        try
        {
            errorList.put(errorList.length(), errorText);

            if (logError) {
                System.out.println(errorText);
            }
        }
        catch (final JSONException e)
        {
            System.out.println("Error writing ErrorList: " + e.toString());
        }
    }
    
    public void setLanguage(final String lang)
    {
        labels = ResourceManager.getSysMsgBundle(new Locale(lang));
    }
    
    public int getLength()
    {
        return errorList.length();
    }
    
    public boolean isEmpty()
    {
        return getLength()==0;
    }
    
    public JSONArray getList()
    {
        return errorList;
    }
    
    public JSONArray getErrorFlags() {
        JSONArray errorFlagsArray = new JSONArray();
        for (ErrorStatus status :  errorFlags) {
            errorFlagsArray.put(status);
        }
    	return errorFlagsArray;
    }
}
