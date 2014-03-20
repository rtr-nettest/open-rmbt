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
package at.alladin.rmbt.android.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.about.RMBTAboutFragment;
import at.alladin.rmbt.android.map.RMBTMapFragment;
import at.alladin.rmbt.android.preferences.RMBTPreferenceActivity;
import at.alladin.rmbt.android.util.ConfigHelper;

/**
 * 
 * @author
 * 
 */
public class RMBTMainMenuFragment extends Fragment
{
    
    // private static final String DEBUG_TAG = "RMBTMainMenuFragment";
    
    /**
	 * 
	 */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    
    /**
     * 
     * @return
     */
    public RMBTMainActivity getMainActivity()
    {
        return (RMBTMainActivity) getActivity();
    }
    
    /**
	 * 
	 */
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        
        final View view = inflater.inflate(R.layout.menu, container, false);
        
        final Button startButton = (Button) view.findViewById(R.id.menuButtonStartButton);
        final Button historyButton = (Button) view.findViewById(R.id.menuButtonHistoryButton);
        final Button mapButton = (Button) view.findViewById(R.id.menuButtonMapButton);
        final Button statisticsButton = (Button) view.findViewById(R.id.menuButtonStatisticsButton);
        final Button helpButton = (Button) view.findViewById(R.id.menuButtonHelpButton);
        final Button aboutButton = (Button) view.findViewById(R.id.menuButtonAboutButton);
        final Button settingsButton = (Button) view.findViewById(R.id.menuButtonSettingsButton);
        final ImageButton logoButton = (ImageButton) view.findViewById(R.id.buttonLogo);
        
        final FragmentManager fm = getActivity().getSupportFragmentManager();
        
        startButton.setOnClickListener(new OnClickListener()
        {
            
            /**
	    	 * 
	    	 */
            @Override
            public void onClick(final View v)
            {
                getMainActivity().startTest(false);
            }
        });
        
        historyButton.setOnClickListener(new OnClickListener()
        {
            
            /**
	    	 * 
	    	 */
            @Override
            public void onClick(final View v)
            {
                getMainActivity().showHistory(false);
            }
        });
        
        mapButton.setOnClickListener(new OnClickListener()
        {
            
            /**
	    	 * 
	    	 */
            @Override
            public void onClick(final View v)
            {
                FragmentTransaction ft;
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_content, new RMBTMapFragment(), "map");
                ft.addToBackStack("map");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
            }
        });
        
        settingsButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                final RMBTMainActivity activity = (RMBTMainActivity) getActivity();
                startActivity(new Intent(activity, RMBTPreferenceActivity.class));
            }
        });
        
        statisticsButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                final RMBTMainActivity activity = (RMBTMainActivity) getActivity();
                final String urlStatistic = ConfigHelper.getVolatileSetting("url_statistics");
                if (urlStatistic == null || urlStatistic.length() == 0)
                    return;
                activity.showHelp(urlStatistic);
            }
        });
        
        helpButton.setOnClickListener(new OnClickListener()
        {
            
            /**
	    	 * 
	    	 */
            @Override
            public void onClick(final View v)
            {
                final RMBTMainActivity activity = (RMBTMainActivity) getActivity();
                activity.showHelp("");
            }
        });
        
        aboutButton.setOnClickListener(new OnClickListener()
        {
            
            /**
	    	 * 
	    	 */
            @Override
            public void onClick(final View v)
            {
                FragmentTransaction ft;
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_content, new RMBTAboutFragment(), "about");
                ft.addToBackStack("about");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
            }
        });
        
        logoButton.setOnClickListener(new OnClickListener()
        {
            
            /**
	    	 * 
	    	 */
            @Override
            public void onClick(final View v)
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.menu_rtr_web_link))));
            }
        });
        
        return view;
    }
}
