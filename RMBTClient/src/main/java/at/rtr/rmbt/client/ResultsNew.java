/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
 * Copyright 2013-2015 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
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
package at.rtr.rmbt.client;

import java.util.HashMap;
import java.util.Map;

class ResultsNewClient
{
    private static ResultsNewClient instance;

    public Map<Integer, Long> dict = new HashMap<Integer, Long>();
    public String host;

    private ResultsNewClient()
    {
      dict = new HashMap<Integer, Long>();
      host = "";
    }

    public static ResultsNewClient getInstance()
    {
      if (instance == null) 
      {
        //synchronized block to remove overhead
        synchronized (ResultsNewClient.class)
        {
          if(instance==null)
          {
            // if instance is null, initialize
            instance = new ResultsNewClient();
          }
         
        }
      }
      return instance;
    }

}
