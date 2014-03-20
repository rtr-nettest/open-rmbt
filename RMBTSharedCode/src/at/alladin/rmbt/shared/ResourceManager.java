/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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

import java.util.Locale;
import java.util.ResourceBundle;

public abstract class ResourceManager
{
    private static final String BASE_NAME_SYSTEM_MESSAGES = "at.alladin.rmbt.res.SystemMessages";
    private static final String BASE_NAME_CONFIG = "at.alladin.rmbt.res.Configuration";
    
    private static final ResourceControl RESOURCE_CONTROL = new ResourceControl();
    
    public static ResourceBundle getBundle(String baseName, Locale locale)
    {
        return ResourceBundle.getBundle(baseName, locale, RESOURCE_CONTROL);
    }
    
    public static ResourceBundle getSysMsgBundle(Locale locale)
    {
        return getBundle(BASE_NAME_SYSTEM_MESSAGES, locale);
    }
    
    public static ResourceBundle getSysMsgBundle()
    {
        return getSysMsgBundle(Locale.getDefault());
    }
    
    public static ResourceBundle getCfgBundle(Locale locale)
    {
        return getBundle(BASE_NAME_CONFIG, locale);
    }
    
    public static ResourceBundle getCfgBundle()
    {
        return getCfgBundle(Locale.getDefault());
    }
    
    
}
