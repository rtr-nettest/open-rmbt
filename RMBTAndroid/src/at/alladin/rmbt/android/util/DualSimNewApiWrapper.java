/*******************************************************************************
 * Copyright 2016 Specure GmbH
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
package at.alladin.rmbt.android.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.telephony.SubscriptionManager;

@TargetApi(22)
public class DualSimNewApiWrapper
{
    public static boolean checkAvailable()
    {
        if (Build.VERSION.SDK_INT < 22)
            return false;
        try
        {
            final Class<?> sm = Class.forName("android.telephony.SubscriptionManager");
            sm.getMethod("from", Context.class);
            return true;
        }
        catch (Exception ex)
        {
            //ex.printStackTrace();
            return false;
        }
    }
    
    public boolean isDualSim(final Context context)
    {
        try
        {
            final SubscriptionManager sm = SubscriptionManager.from(context);
            final int activeSubscriptionInfoCount = sm.getActiveSubscriptionInfoCount();
            //System.out.println("active subscription count: " + activeSubscriptionInfoCount);
            return activeSubscriptionInfoCount > 1;
        }
        catch (Throwable t)
        {
            //t.printStackTrace();
            return false;
        }
    }
}
