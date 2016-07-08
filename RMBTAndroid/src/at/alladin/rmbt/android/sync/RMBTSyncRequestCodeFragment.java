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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.util.CheckSyncTask;
import at.alladin.rmbt.android.util.EndTaskListener;

public class RMBTSyncRequestCodeFragment extends Fragment implements EndTaskListener
{
    
    // private static final String DEBUG_TAG = "RMBTSyncRequestCodeFragment";
    
    private RMBTMainActivity activity;
    
    private CheckSyncTask syncTask;
    
    private View view;
    
    private TextView codeText;
    
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        activity = (RMBTMainActivity) getActivity();
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.sync_request_code, container, false);
        
        codeText = (TextView) view.findViewById(R.id.code);
        
        if (syncTask == null || syncTask != null || syncTask.isCancelled())
        {
            syncTask = new CheckSyncTask(activity);
            
            syncTask.setEndTaskListener(this);
            syncTask.execute("");
        }
        
        return view;
    }
    
    @Override
    public void taskEnded(final JSONArray resultList)
    {
        if (resultList != null && resultList.length() > 0 && !syncTask.hasError())
            for (int i = 0; i < resultList.length(); i++)
            {
                
                JSONObject resultListItem;
                try
                {
                    resultListItem = resultList.getJSONObject(i);
                    
                    codeText.setText(resultListItem.getString("sync_code"));
                    registerForContextMenu(codeText);
                    
                }
                catch (final JSONException e)
                {
                    e.printStackTrace();
                }
                
            }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo contextMenuInfo)
    {
        if (view instanceof TextView && view == codeText)
            menu.add(Menu.NONE, view.getId(), Menu.NONE, R.string.sync_request_code_context_copy);
        else
            super.onCreateContextMenu(menu, view, contextMenuInfo);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem mi)
    {
        if (mi.getItemId() == codeText.getId())
        {
        	ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        	ClipData clip = ClipData.newPlainText("sync_code", codeText.getText());
        	clipboard.setPrimaryClip(clip);
            return true;
        }
        return super.onContextItemSelected(mi);
    }

}
