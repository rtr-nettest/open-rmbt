/*******************************************************************************
 * Copyright 2013 alladin-IT OG
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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.util.ConfigHelper;

public class RMBTTermsCheckFragment extends Fragment
{
//    private static final String DEBUG_TAG = "RMBTTermsCheckFragment";
    
    boolean firstTime = true;
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.terms_check, container, false);
        
        final WebView tcWvs = (WebView) v.findViewById(R.id.termsCheckWebViewShort);
        tcWvs.loadUrl("file:///android_res/raw/terms_conditions_short.html");
        
        final WebView tcWvl = (WebView) v.findViewById(R.id.termsCheckWebViewLong);
        tcWvl.loadUrl("file:///android_res/raw/terms_conditions_long.html");
        
        final FragmentActivity activity = getActivity();
        if (! (activity instanceof RMBTMainActivity))
            firstTime = false;
        final boolean tcAccepted = ConfigHelper.isTCAccepted(activity);
        
        final TextView acceptButtonTextView = (TextView) v.findViewById(R.id.termsAcceptButtonText);
        
        if (tcAccepted)
        {
            acceptButtonTextView.setText(R.string.terms_accept_button_continue);
            v.findViewById(R.id.termsAcceptText).setVisibility(View.GONE);
        }
        
        if (! firstTime)
            v.findViewById(R.id.termsButtonDecline).setVisibility(View.GONE);
        
        final Button buttonTermsAccept = (Button) v.findViewById(R.id.termsAcceptButton);
        buttonTermsAccept.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                ConfigHelper.setTCAccepted(getActivity(), true);
                acceptButtonTextView.setText(R.string.terms_accept_button_continue);
                if (firstTime)
                {
                    ((RMBTMainActivity)getActivity()).checkSettings(true, null);
                    ((RMBTMainActivity)getActivity()).showNdtCheck();
                }
                else
                    ((RMBTTermsActivity)getActivity()).showNdtCheck();
            }
        });
        
        final Button buttonTermsDecline = (Button) v.findViewById(R.id.termsDeclineButton);
        buttonTermsDecline.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                onBackPressed();
            }
        });
        
        return v;
    }

    public boolean onBackPressed()
    {
        // user has declined t+c!
        
        ConfigHelper.setTCAccepted(getActivity(), false);
        ConfigHelper.setUUID(getActivity(), "");
        getActivity().finish();
        return true;
    }
}
