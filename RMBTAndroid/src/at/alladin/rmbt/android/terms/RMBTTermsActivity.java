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
package at.alladin.rmbt.android.terms;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.view.WindowManager;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.AppConstants;
import at.alladin.rmbt.android.terms.RMBTCheckFragment.CheckType;

public class RMBTTermsActivity extends FragmentActivity
{	
	public final static String EXTRA_KEY_CHECK_TYPE = "check_type";
	
	public final static String EXTRA_KEY_CHECK_TERMS_AND_COND = "check_t&c";

	private CheckType checkType = null;
	
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        boolean showTermsAndConditions = true;
        
        if (getIntent().getExtras() != null) {
        	final String checkTypeName = getIntent().getExtras().getString(EXTRA_KEY_CHECK_TYPE, null);
        	if (checkTypeName != null) {
        		checkType = CheckType.valueOf(checkTypeName);
        	}
        	
        	showTermsAndConditions = getIntent().getExtras().getBoolean(EXTRA_KEY_CHECK_TERMS_AND_COND, true);
        }
        
        final Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
        window.addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        
        if (savedInstanceState == null)
        {
            if (showTermsAndConditions) {
            	showTermsCheck();
            }
            else {
            	continueWorkflow();
            }
        }
    }
    
    public void continueWorkflow() {
    	switch (checkType) {
    	case LOOP_MODE:
    		showLoopModeCheck();
    		break;
    	case NDT:
    		showNdtCheck();
    		break;
    	}    	
    }
    
    @Override
    public void onBackPressed() {
    	final boolean showTermsAndConditions = getIntent().getExtras().getBoolean(EXTRA_KEY_CHECK_TERMS_AND_COND, true);
    	if (!showTermsAndConditions) {
    		finish();
    	}
    	else {
    		super.onBackPressed();
    	}
    }
    
    public void showTermsCheck()
    {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_content, RMBTTermsCheckFragment.newInstance(checkType), "terms_check");
        ft.commit();
        setTitle("terms_check");
    }
    
    public void showLoopModeCheck()
    {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_content, RMBTCheckFragment.newInstance(CheckType.LOOP_MODE), AppConstants.PAGE_TITLE_LOOP_MODE_CHECK);
        ft.addToBackStack(AppConstants.PAGE_TITLE_LOOP_MODE_CHECK);
        ft.commit();
        setTitle(AppConstants.PAGE_TITLE_LOOP_MODE_CHECK);
    }

    public void showNdtCheck()
    {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_content, RMBTCheckFragment.newInstance(CheckType.NDT), AppConstants.PAGE_TITLE_NDT_CHECK);
        ft.addToBackStack(AppConstants.PAGE_TITLE_NDT_CHECK);
        ft.commit();
        setTitle(AppConstants.PAGE_TITLE_NDT_CHECK);
    }
    
    public void setTitle(String fragName)
    {
        Integer id = null;
        if (fragName != null)
            id = AppConstants.TITLE_MAP.get(fragName);
        if (id == null)
            id = R.string.terms;
        getActionBar().setTitle(id);
    }
}
