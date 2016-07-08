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

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.AppConstants;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.util.ConfigHelper;

public class RMBTCheckFragment extends Fragment
{
	public enum CheckType {
		NDT("file:///android_res/raw/ndt_info.html", AppConstants.PAGE_TITLE_NDT_CHECK, 
				R.string.terms_ndt_header, R.string.terms_ndt_accept_text, false),
		LOOP_MODE("file:///android_res/raw/loop_mode_info.html", AppConstants.PAGE_TITLE_LOOP_MODE_CHECK, 
				R.string.terms_loop_mode_header, R.string.terms_loop_mode_accept_text, true);
		
		private final String templateFile;
		private final String fragmentTag;
		private final boolean defaultIsChecked;
		private final int titleId;
		private final int textId;
		
		CheckType(final String templateFile, final String fragmentTag, final int titleId, final int textId, final boolean defaultIsChecked) {
			this.templateFile = templateFile;
			this.fragmentTag = fragmentTag;
			this.titleId = titleId;
			this.textId = textId;
			this.defaultIsChecked = defaultIsChecked;
		}

		public String getTemplateFile() {
			return templateFile;
		}

		public String getFragmentTag() {
			return fragmentTag;
		}

		public int getTitleId() {
			return titleId;
		}

		public int getTextId() {
			return textId;
		}

		public boolean isDefaultIsChecked() {
			return defaultIsChecked;
		}
	}
	
	private CheckType checkType;
	
    private CheckBox checkBox;
    
    boolean firstTime = true;
    
    public static RMBTCheckFragment newInstance(final CheckType checkType) {
    	final RMBTCheckFragment f = new RMBTCheckFragment();
    	final Bundle bdl = new Bundle(1);
    	bdl.putSerializable("checkType", checkType);
        f.setArguments(bdl);
        return f;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	checkType = (CheckType) getArguments().get("checkType");
    }
    
    public CheckType getCheckType() {
		return checkType;
	}

	@Override
    public void onSaveInstanceState(final Bundle b)
    {
	    super.onSaveInstanceState(b);
	    if (checkBox != null)
	        b.putBoolean("isChecked", checkBox.isChecked());
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        if (! (getActivity() instanceof RMBTMainActivity))
            firstTime = false;
        
        final View v = inflater.inflate(R.layout.ndt_check, container, false);
                
        if (! firstTime)
            v.findViewById(R.id.termsNdtButtonBack).setVisibility(View.GONE);
        
        final TextView textTitle = (TextView) v.findViewById(R.id.check_fragment_title);
        textTitle.setText(checkType.getTitleId());
        
        checkBox = (CheckBox) v.findViewById(R.id.ndtCheckBox);
        checkBox.setText(checkType.getTextId());
        
        if (savedInstanceState != null) {
            checkBox.setChecked(savedInstanceState.getBoolean("isChecked"));
        }
        else {
        	checkBox.setChecked(checkType.isDefaultIsChecked());
        }

        
        final Button buttonAccept = (Button) v.findViewById(R.id.termsNdtAcceptButton);
        
        if (! firstTime)
        {    
            checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    buttonAccept.setEnabled(isChecked);
                }
            });
        }
        
        final WebView wv = (WebView) v.findViewById(R.id.ndtInfoWebView);
        wv.loadUrl(checkType.getTemplateFile());
        
        buttonAccept.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                final FragmentActivity activity = getActivity();
                
                switch (checkType) {
                case NDT:
                    ConfigHelper.setNDT(activity, checkBox.isChecked());
                    ConfigHelper.setNDTDecisionMade(activity, true);                	
                	break;
                case LOOP_MODE:
                	ConfigHelper.setLoopMode(activity, checkBox.isChecked());
                	break;
                }
                
                activity.getSupportFragmentManager().popBackStack(checkType.getFragmentTag(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                
                if (firstTime && CheckType.NDT.equals(checkType)) {
                    ((RMBTMainActivity) activity).initApp(false);
                }
                else
                {
                    getActivity().setResult(checkBox.isChecked() ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
                    getActivity().finish();
                }                
            }
        });
        
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                buttonAccept.setEnabled(firstTime || checkBox.isChecked());
            }
        }, 500);
        
        final Button buttonBack = (Button) v.findViewById(R.id.termsNdtBackButton);
        buttonBack.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        
        return v;
    }

}