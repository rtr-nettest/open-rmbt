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
package at.alladin.rmbt.android.terms;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.util.ConfigHelper;

public class RMBTNDTCheckFragment extends Fragment
{
    private CheckBox ndtCheckBox;
    
    boolean firstTime = true;
    
    @Override
    public void onSaveInstanceState(final Bundle b)
    {
        b.putBoolean("ndtChecked", ndtCheckBox.isChecked());
        super.onSaveInstanceState(b);
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        if (! (getActivity() instanceof RMBTMainActivity))
            firstTime = false;
        
        final View v = inflater.inflate(R.layout.ndt_check, container, false);
        
        if (! firstTime)
            v.findViewById(R.id.termsNdtButtonBack).setVisibility(View.GONE);
        
        ndtCheckBox = (CheckBox) v.findViewById(R.id.ndtCheckBox);
        if (savedInstanceState != null)
            ndtCheckBox.setChecked(savedInstanceState.getBoolean("ndtChecked"));
        
        final Button buttonAccept = (Button) v.findViewById(R.id.termsNdtAcceptButton);
        
        if (! firstTime)
        {    
            ndtCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    buttonAccept.setEnabled(isChecked);
                }
            });
        }
        
        final WebView wv = (WebView) v.findViewById(R.id.ndtInfoWebView);
        wv.loadUrl("file:///android_res/raw/ndt_info.html");
        
        buttonAccept.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                final Activity activity = getActivity();
                ConfigHelper.setNDT(activity, ndtCheckBox.isChecked());
                ConfigHelper.setNDTDecisionMade(activity, true);
                activity.getFragmentManager().popBackStack("ndt_check", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                if (firstTime)
                    ((RMBTMainActivity) activity).initApp(false);
                else
                {
                    getActivity().setResult(ndtCheckBox.isChecked() ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
            }
        });
        
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                buttonAccept.setEnabled(firstTime || ndtCheckBox.isChecked());
            }
        }, 500);
        
        final Button buttonBack = (Button) v.findViewById(R.id.termsNdtBackButton);
        buttonBack.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                getActivity().getFragmentManager().popBackStack();
            }
        });
        
        return v;
    }

}
