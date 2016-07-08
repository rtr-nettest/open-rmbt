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
package at.alladin.rmbt.android.sync;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;

public class RMBTSyncFragment extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        FrameLayout frameLayout = new FrameLayout(getActivity());
        populateViewForOrientation(inflater, frameLayout);
        return frameLayout;
    }
    
    public void populateViewForOrientation(final LayoutInflater inflater, final ViewGroup container)
    {
        container.removeAllViewsInLayout();
        final View view = inflater.inflate(R.layout.sync, container);
        
        final Button buttonRequestCode = (Button) view.findViewById(R.id.requestCodeButton);
        
        buttonRequestCode.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                final FragmentManager fm = getFragmentManager();
                FragmentTransaction ft;
                
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_content, new RMBTSyncRequestCodeFragment(), "sync_request_code");
                ft.addToBackStack("sync_request_code");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
            }
        });
        
        final Button buttonEnterCode = (Button) view.findViewById(R.id.enterCodeButton);
        
        buttonEnterCode.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                final FragmentManager fm = getFragmentManager();
                FragmentTransaction ft;
                
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_content, new RMBTSyncEnterCodeFragment(), "sync_enter_code");
                ft.addToBackStack("sync_enter_code");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
            }
        });
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
    }
    
    public boolean onBackPressed()
    {
        ((RMBTMainActivity) getActivity()).setHistoryDirty(true);
        ((RMBTMainActivity) getActivity()).checkSettings(true, null);
        ((RMBTMainActivity) getActivity()).setSettings(null, null);
//        ((RMBTMainActivity) getActivity()).waitForSettings(false, false, true);
        return false;
    }
}
