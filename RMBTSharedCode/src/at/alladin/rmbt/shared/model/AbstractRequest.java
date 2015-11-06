/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
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
package at.alladin.rmbt.shared.model;

import java.util.UUID;

public abstract class AbstractRequest
{
    protected String language;
    protected UUID uuid;
    
    protected String api_level;
    protected String model;
    protected String softwareRevision;
    protected String softwareVersionName;
    protected String softwareVersionCode;
    protected String type;
    protected String version;
    protected String product;
    protected String timezone;
    protected String device;
    protected String plattform;
    
    public String getLanguage()
    {
        return language;
    }
    public void setLanguage(String language)
    {
        this.language = language;
    }
    public UUID getUuid()
    {
        return uuid;
    }
    public void setUuid(UUID uuid)
    {
        this.uuid = uuid;
    }
    public String getApi_level()
    {
        return api_level;
    }
    public void setApi_level(String api_level)
    {
        this.api_level = api_level;
    }
    public String getModel()
    {
        return model;
    }
    public void setModel(String model)
    {
        this.model = model;
    }
    public String getSoftwareRevision()
    {
        return softwareRevision;
    }
    public void setSoftwareRevision(String softwareRevision)
    {
        this.softwareRevision = softwareRevision;
    }
    public String getSoftwareVersionName()
    {
        return softwareVersionName;
    }
    public void setSoftwareVersionName(String softwareVersionName)
    {
        this.softwareVersionName = softwareVersionName;
    }
    public String getSoftwareVersionCode()
    {
        return softwareVersionCode;
    }
    public void setSoftwareVersionCode(String softwareVersionCode)
    {
        this.softwareVersionCode = softwareVersionCode;
    }
    public String getType()
    {
        return type;
    }
    public void setType(String type)
    {
        this.type = type;
    }
    public String getVersion()
    {
        return version;
    }
    public void setVersion(String version)
    {
        this.version = version;
    }
    public String getProduct()
    {
        return product;
    }
    public void setProduct(String product)
    {
        this.product = product;
    }
    public String getTimezone()
    {
        return timezone;
    }
    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }
    public String getDevice()
    {
        return device;
    }
    public void setDevice(String device)
    {
        this.device = device;
    }
    public String getPlattform()
    {
        return plattform;
    }
    public void setPlattform(String plattform)
    {
        this.plattform = plattform;
    }
    
    
    
    
}
