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
package at.alladin.rmbt.android.about;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.util.ConfigHelper;
import at.alladin.rmbt.android.util.RMBTTermsFragment;
import at.alladin.rmbt.client.helper.RevisionHelper;

import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * 
 * @author
 * 
 */
public class RMBTAboutFragment extends Fragment
{
    
    /**
	 * 
	 */
    private static final String DEBUG_TAG = "RMBTAboutFragment";
    
    /**
	 * 
	 */
    public static String[] titles;
    
    /**
	 * 
	 */
    public static String[] dialogue;
    
    /**
	 * 
	 */
    private String clientVersion;
    
    /**
	 * 
	 */
    private String clientName;
    
    /**
	 * 
	 */
    private ListAdapter sa;
    
    /**
	 * 
	 */
    private Activity activity;
    
    /**
	 * 
	 */
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.about, container, false);

        return createView(view, inflater);
    }
    
    private View createView(View view, LayoutInflater inflater) {
        activity = getActivity();
        
        getAppInfo(activity);
        
        final String clientUUID = String.format("U%s", ConfigHelper.getUUID(activity.getApplicationContext()));
        
        final String controlServerVersion = ConfigHelper.getControlServerVersion(activity);
        
        final ListView listView = (ListView) view.findViewById(R.id.aboutList);
        
        final ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        
        HashMap<String, String> item;
        item = new HashMap<String, String>();
        item.put("title", clientName);
        item.put("text1", this.getString(R.string.about_rtr_line1));
        list.add(item);
        item = new HashMap<String, String>();
        item.put("title", this.getString(R.string.about_version_title));
        item.put("text1", clientVersion);
        item.put("text2", "");
        list.add(item);
        item = new HashMap<String, String>();
        item.put("title", getString(R.string.about_clientid_title));
        item.put("text1", clientUUID);
        item.put("text2", "");
        list.add(item);
        item = new HashMap<String, String>();
        item.put("title", getString(R.string.about_web_title));
        item.put("text1", getString(R.string.about_web_line1));
        item.put("text2", "");
        list.add(item);
        item = new HashMap<String, String>();
        item.put("title", getString(R.string.about_email_title));
        item.put("text1", getString(R.string.about_email_line1));
        item.put("text2", "");
        list.add(item);
        item = new HashMap<String, String>();
        item.put("title", getString(R.string.about_terms_title));
        item.put("text1", getString(R.string.about_terms_line1));
        item.put("text2", "");
        list.add(item);
        item = new HashMap<String, String>();
        item.put("title", getString(R.string.about_git_title));
        item.put("text1", getString(R.string.about_git_line1));
        item.put("text2", "");
        list.add(item);
        item = new HashMap<String, String>();
        item.put("title", getString(R.string.about_dev_title));
        item.put("text1", getString(R.string.about_dev_line1));
        item.put("text2", getString(R.string.about_dev_line2));
        list.add(item);

        final String openSourceSoftwareLicenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getActivity());
        
        if (openSourceSoftwareLicenseInfo != null)
        {
            item = new HashMap<String, String>();
            item.put("title", getString(R.string.about_gms_legal_title));
            item.put("text1", getString(R.string.about_gms_legal_line1));
            item.put("text2", "");
            list.add(item);
        }
        
        if (ConfigHelper.isDevEnabled(getActivity()))
        {
            item = new HashMap<String, String>();
            item.put("title", getString(R.string.about_test_counter_title));
            item.put("text1", Integer.toString(ConfigHelper.getTestCounter(getActivity())));
            item.put("text2", "");
            list.add(item);
        }
        
        item = new HashMap<String, String>();
        item.put("title", getString(R.string.about_control_server_version));
        item.put("text1", controlServerVersion != null ? controlServerVersion : "---");
        item.put("text2", "");
        list.add(item);
        
        sa = new RMBTAboutAdapter(getActivity(), list, R.layout.about_item, new String[] { "title", "text1", "text2" },
                new int[] { R.id.title, R.id.text1, R.id.text2 });
        
        listView.setAdapter(sa);
        
        listView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(final AdapterView<?> l, final View v, final int position, final long id)
            {
                
                switch (position)
                {
                
                case 2:
                    final ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(clientUUID);
                    final Toast toast = Toast.makeText(getActivity(), R.string.about_clientid_toast, Toast.LENGTH_LONG);
                    toast.show();
                    break;
                
                case 3:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_web_link))));
                    break;
                
                case 4:
                    /* Create the Intent */
                    final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    
                    /* Fill it with Data */
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.about_email_email) });
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_email_subject));
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                    
                    /* Send it off to the Activity-Chooser */
                    startActivity(Intent.createChooser(emailIntent, getString(R.string.about_email_sending)));
                    
                    break;
                
                case 5:
                    final FragmentManager fm = activity.getFragmentManager();
                    FragmentTransaction ft;
                    ft = fm.beginTransaction();
                    ft.replace(R.id.fragment_content, new RMBTTermsFragment(), "terms");
                    ft.addToBackStack("terms");
                    ft.commit();
                    break;
                
                case 6:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_git_link))));
                    break;
                
                case 7:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_dev_link))));
                    break;
                    
                case 8:
                    final String licenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getActivity());
                    AlertDialog.Builder licenseDialog = new AlertDialog.Builder(getActivity());
                    licenseDialog.setMessage(licenseInfo);
                    licenseDialog.show();
                    break;
                
                default:
                    break;
                }
            }
            
        });
        
        return view;
    }
    
    /**
     * 
     * @param context
     * @return
     */
    private String getAppInfo(final Context context)
    {
        PackageInfo pInfo;
        try
        {
            
            String date = "";
            try
            {
                final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
                final ZipFile zf = new ZipFile(ai.sourceDir);
                final ZipEntry ze = zf.getEntry("classes.dex");
                zf.close();
                final long time = ze.getTime();
                date = DateFormat.getDateTimeInstance().format(new java.util.Date(time));
                
            }
            catch (final Exception e)
            {// not much we can do
            }
            
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            clientVersion = pInfo.versionName + " (" + pInfo.versionCode + ")\n(" + RevisionHelper.getVerboseRevision() + ")\n(" + date + ")";
            clientName = context.getResources().getString(R.string.app_name);
        }
        catch (final Exception e)
        {
            // e1.printStackTrace();
            Log.e(DEBUG_TAG, "version of the application cannot be found", e);
        }
        
        return clientVersion;
    }
    
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	LayoutInflater inflater = LayoutInflater.from(getActivity());
    	populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    /**
     * 
     * @param inflater
     * @param view
     */
	private void populateViewForOrientation(LayoutInflater inflater, ViewGroup view) {
		view.removeAllViewsInLayout();
        View v = inflater.inflate(R.layout.about, view);
        createView(v, inflater);
	}

}
