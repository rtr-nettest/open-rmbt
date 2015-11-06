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
package at.alladin.rmbt.shared.cache;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class CacheContextListener implements ServletContextListener
{
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        String addresses = sce.getServletContext().getInitParameter("RMBT_MEMCACHED_ADDRESSES");
        if (addresses == null || addresses.isEmpty())
            System.out.println("RMBT_MEMCACHED_ADDRESSES not set, cache deactivated");
        else
        {
            System.out.println("init memcached with: " + addresses);
            CacheHelper.getInstance().initMemcached(addresses);
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
    }
}
