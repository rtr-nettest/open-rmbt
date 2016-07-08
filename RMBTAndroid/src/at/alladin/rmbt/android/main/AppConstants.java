/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
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
package at.alladin.rmbt.android.main;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.util.capability.Capabilities;

public class AppConstants {
	
	//////////////////////////////////////////////////
	// user loop mode default values 
	////////////////////////////////////////////////
	
	/**
	 * multiplies the delay with this value. 
	 * could be helpful when debugging, set to 1 and the loop mode will wait for seconds not for minutes
	 */
	public final static float LOOP_MODE_TIME_MOD = 60.0f;
	
	public final static int LOOP_MODE_MAX_DELAY = 24*60;
	public final static int LOOP_MODE_MIN_DELAY = 15;
	public final static int LOOP_MODE_DEFAULT_DELAY = 30;

	public final static int LOOP_MODE_MIN_MOVEMENT = 50;
	public final static int LOOP_MODE_MAX_MOVEMENT = 10000;
	public final static int LOOP_MODE_DEFAULT_MOVEMENT = 250;
	public final static int LOOP_MODE_GPS_ACCURACY_CRITERIA = 30;
	
	public final static int LOOP_MODE_MIN_TESTS = 1;
	public final static int LOOP_MODE_MAX_TESTS = 100;
	public final static int LOOP_MODE_DEFAULT_TESTS = 10;
	
	public final static boolean LOOP_MODE_DEFAULT_USES_GPS = true;
	public final static boolean LOOP_MODE_DEFAULT_USES_WAKELOCK = false;
	

	public final static int LOOP_MODE_UNLOCK_CODE = 1234;
	public final static int LOOP_MODE_LOCK_CODE = 1234;
	
	
	//////////////////////////////////////////////////
	// server selection values 
	////////////////////////////////////////////////

	public final static int SERVER_SELECTION_UNLOCK_CODE = 1234;
	public final static int SERVER_SELECTION_LOCK_CODE = 1234;
	//////////////////////////////////////////////////
	// server selection values 
	////////////////////////////////////////////////

	public final static int DEVELOPER_UNLOCK_CODE = 1234;
	public final static int DEVELOPER_LOCK_CODE = 1234;
	
	
	//////////////////////////////////////////////////
	// fragment ids and titles 
	////////////////////////////////////////////////	
	
	public final static String PAGE_TITLE_MAIN = "main";
	public final static String PAGE_TITLE_TEST = "test";
	public final static String PAGE_TITLE_LOOP_TEST = "loop_test";
	public final static String PAGE_TITLE_MAP = "map";
	public final static String PAGE_TITLE_MINI_MAP = "mini_map";
	public final static String PAGE_TITLE_NDT_CHECK = "ndt_check";
	public final static String PAGE_TITLE_LOOP_MODE_CHECK = "loop_mode_check";
	public final static String PAGE_TITLE_HISTORY = "history";
	public final static String PAGE_TITLE_HISTORY_FILTER = "history_filter";
	public final static String PAGE_TITLE_HISTORY_PAGER = "history_pager";
	public final static String PAGE_TITLE_SYNC = "sync";
	public final static String PAGE_TITLE_ABOUT = "about";
	public final static String PAGE_TITLE_RESULT_DETAIL = "result_detail";
	public final static String PAGE_TITLE_RESULT_QOS = "result_detail_expanded";
	public final static String PAGE_TITLE_TEST_DETAIL_QOS = "result_qos_test_detail";
	public final static String PAGE_TITLE_TERMS_CHECK = "terms_check";
	public final static String PAGE_TITLE_HELP = "help";
	public final static String PAGE_TITLE_STATISTICS = "statistics";
	public final static String BACK_PRESSED = "back_pressed";
	
	public final static Map<String, Integer> TITLE_MAP;
	
	static {
		TITLE_MAP = new HashMap<String, Integer>();
		TITLE_MAP.put(PAGE_TITLE_MAIN, R.string.page_title_title_page);
		TITLE_MAP.put(PAGE_TITLE_TEST, R.string.app_name);
		TITLE_MAP.put(PAGE_TITLE_MAP, R.string.page_title_map);
		TITLE_MAP.put(PAGE_TITLE_HISTORY, R.string.page_title_history);
		TITLE_MAP.put(PAGE_TITLE_HISTORY_PAGER, R.string.page_title_main_result);
		TITLE_MAP.put(PAGE_TITLE_HISTORY_FILTER, R.string.history_button_filter);
		TITLE_MAP.put(PAGE_TITLE_SYNC, R.string.page_title_sync);
		TITLE_MAP.put(PAGE_TITLE_ABOUT, R.string.page_title_about);
		TITLE_MAP.put(PAGE_TITLE_RESULT_DETAIL, R.string.page_title_main_result);
		TITLE_MAP.put(PAGE_TITLE_RESULT_QOS, R.string.page_title_qos_result);
		TITLE_MAP.put(PAGE_TITLE_TERMS_CHECK, R.string.terms);
		TITLE_MAP.put(PAGE_TITLE_HELP, R.string.page_title_help);
		TITLE_MAP.put(PAGE_TITLE_TEST_DETAIL_QOS, R.string.page_title_qos_result);
		TITLE_MAP.put(PAGE_TITLE_STATISTICS, R.string.page_title_statistics);
		TITLE_MAP.put(PAGE_TITLE_NDT_CHECK, R.string.terms);
		TITLE_MAP.put(PAGE_TITLE_LOOP_MODE_CHECK, R.string.terms_loop_mode);
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static String getUserAgentString(Context context) {
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return "(Android; " + Locale.getDefault().toString()  + "; API" + Build.VERSION.SDK_INT + ") Open-RMBT/" + pInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public static Capabilities getCapabilities() {
    	final Capabilities capabilities = new Capabilities();
    	capabilities.getClassificationCapability().setCount(4);
    	return capabilities; 
	}
}
