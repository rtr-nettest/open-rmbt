/*******************************************************************************
 * Copyright 2015, 2016 alladin-IT GmbH
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
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * @author leo
 *
 */
public abstract class DualSimDetector
{
    private static final String TAG = "DualSimDetector";
    static
    {
        DualSimNewApiWrapper wrapper = null;
        try
        {
            if (DualSimNewApiWrapper.checkAvailable())
                wrapper = new DualSimNewApiWrapper();
        }
        catch (Throwable e)
        {
            //e.printStackTrace();
        }
        DUAL_SIM_NEW_API_WRAPPER = wrapper;
    }
    
    private final static DualSimNewApiWrapper DUAL_SIM_NEW_API_WRAPPER;
    
    private final static String DUAL_SIM_METHOD_API = "api_" + Build.VERSION.SDK_INT;
    
    public static class DualSimDetectedException extends Exception
    {
        private static final long serialVersionUID = 1L;
        private final String method;
        private final String detail;

        public DualSimDetectedException(String method, String detail)
        {
            this.method = method;
            this.detail = detail;
        }

        public DualSimDetectedException(String method)
        {
            this(method, "");
        }

        public String getMethod()
        {
            return method;
        }
        
        public String getDetail()
        {
            return detail;
        }
    }
    
    public static String getDualSIM(Context ctx)
    {
        try
        {
            checkDualSIM(ctx);
            return null;
        }
        catch (DualSimDetectedException e)
        {
            return e.getMethod();
        }
    }
    
    public static void checkDualSIM(Context ctx) throws DualSimDetectedException
    {
        try
        {
            if (DUAL_SIM_NEW_API_WRAPPER != null)
            {
                final boolean dualSim = DUAL_SIM_NEW_API_WRAPPER.isDualSim(ctx);
                if (dualSim)
                    throw new DualSimDetectedException(DUAL_SIM_METHOD_API);
                else
                    return; // other methods do not need to be checked in this case (hopefully...)
            }
//            printTelephonyManagerMethodNamesForThisDevice(ctx);
            tryVoodooMethods(ctx);
        }
        catch (DualSimDetectedException e)
        {
            Log.i(TAG, "dual sim detected: " + e.getMethod() + "; " + e.getDetail());
            throw e; // pass dual sim exc.
        }
        catch (Throwable t)
        {  // do not fail!
        }
    }
    
    
    /**
     * if android dual sim api is not available, try to find out with undocumented methods if we
     * are dealing with a dual sim device
     * @return
     */
    private static void tryVoodooMethods(Context ctx) throws DualSimDetectedException
    {
        final TelephonyManager tm = ((TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE));
        
        tryMethod("tm.gnog(i/1)", tm, "getNetworkOperatorGemini", int.class, 1);
        tryMethod("tm.gno(i/1)", tm, "getNetworkOperator", int.class, 1);
        
        // NO, NO! Do NOT do this:
        ////////xxtryMethod(tm, "getNetworkOperator", long.class, 1);
        // this leads to problems with s4 (see #1245)
        
        tryMethod("tm.gsig(i/1)", tm, "getSubscriberIdGemini", int.class, 1);
        tryMethod("tm.gsi(i/1)", tm, "getSubscriberId", int.class, 1);
        
        // this seems to be problematic on titan_umtsds / Motorola G 2014 according to dz
        tryMethod("tm.gsi(l/1)", tm, "getSubscriberId", long.class, 1); 
    }
    
    private static void tryMethod(String detectionMethod, TelephonyManager telephonyManager, String predictedMethodName, Class<?> paramClass, int slotID) throws DualSimDetectedException
    {
        try
        {
            final Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());
            final Class<?>[] parameter = new Class[1];
            parameter[0] = paramClass;
            final Method method = telephonyClass.getMethod(predictedMethodName, parameter);
            final Object[] param = new Object[1];
            param[0] = slotID;
            final Object result = method.invoke(telephonyManager, param);
            
            final String resultString = method + " (" + slotID + ") = " + result;
            Log.d(TAG, resultString);
            if (result != null && !result.equals(""))
                throw new DualSimDetectedException(detectionMethod, resultString);
        }
        catch (DualSimDetectedException e)
        {
            throw e; // pass dual sim exc.
        }
        catch (Throwable e)
        {
            // ignore failures
        }
    }

    private static AtomicBoolean METHODS_PRINTED = new AtomicBoolean();
    // for debugging
    public static void printTelephonyManagerMethodNamesForThisDevice(Context context)
    {
        if (!METHODS_PRINTED.compareAndSet(false, true))
            return; // only once
            
        try
        {
            final TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
            final Method[] methods = telephonyClass.getMethods();
            for (int idx = 0; idx < methods.length; idx++)
                System.out.println("\nMethod: " + methods[idx] + " declared by " + methods[idx].getDeclaringClass());
        }
        catch (Throwable e)
        { // do not fail!
        }
    }
}
