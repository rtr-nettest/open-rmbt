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
package at.alladin.rmbt.android.util;

import java.lang.reflect.Method;

import android.content.Context;
import android.telephony.TelephonyManager;

public class DualSimDetector
{
    private boolean isDualSIM;
    
    public boolean isDualSIM()
    {
        return isDualSIM;
    }
    
    public DualSimDetector(Context context)
    {
        final TelephonyManager telephonyManager = ((TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE));
        
        try
        {
            isDualSIM = invokeMethod(telephonyManager, "getNetworkOperatorGemini", 1);
        }
        catch (GeminiMethodNotFoundException e)
        {
            try
            {
                isDualSIM = invokeMethod(telephonyManager, "getNetworkOperator", 1);
                
            }
            catch (GeminiMethodNotFoundException e1)
            {
            }
        }
    }
    
    private static boolean invokeMethod(TelephonyManager telephonyManager, String predictedMethodName, int slotID)
            throws GeminiMethodNotFoundException
    {
        
        try
        {
            Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method method = telephonyClass.getMethod(predictedMethodName, parameter);
            Object[] param = new Object[1];
            param[0] = slotID;
            Object result = method.invoke(telephonyManager, param);
            
            if (result != null && !result.equals(""))
            {
                System.out.println("dual sim detected: " + predictedMethodName + ":" + result);
                return true;
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            //throw new GeminiMethodNotFoundException(predictedMethodName);
        }
        
        return false;
    }
    
    private static class GeminiMethodNotFoundException extends Exception
    {
        private static final long serialVersionUID = 1;
        
        public GeminiMethodNotFoundException(String info)
        {
            super(info);
        }
    }
    
    public static void printTelephonyManagerMethodNamesForThisDevice(Context context)
    {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Class<?> telephonyClass;
        try
        {
            telephonyClass = Class.forName(telephony.getClass().getName());
            Method[] methods = telephonyClass.getMethods();
            for (int idx = 0; idx < methods.length; idx++)
            {
                
                System.out.println("\nMethod: " + methods[idx] + " declared by " + methods[idx].getDeclaringClass());
            }
        }
        catch (ClassNotFoundException e)
        {
            //e.printStackTrace();
        }
    }
}
