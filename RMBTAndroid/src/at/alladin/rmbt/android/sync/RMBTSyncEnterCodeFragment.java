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

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.util.CheckSyncTask;
import at.alladin.rmbt.android.util.EndTaskListener;
import at.alladin.rmbt.android.util.RMBTAlertDialogFragment;

public class RMBTSyncEnterCodeFragment extends Fragment implements EndTaskListener
{
    
//    private static final String DEBUG_TAG = "RMBTSyncEnterCodeFragment";
    
    private CheckSyncTask syncTask;
    
    private View view;
    
    private EditText codeField;
    
    private Button syncButton;
    
    private LinearLayout overlay;
    
    private OnClickListener listener;
    
    // private OnClickListener
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        
        view = inflater.inflate(R.layout.sync_enter_code, container, false);
        
        syncButton = (Button) view.findViewById(R.id.button);
        
        overlay = (LinearLayout) view.findViewById(R.id.overlay);
        
        codeField = (EditText) view.findViewById(R.id.code);
        
        final RMBTSyncEnterCodeFragment tmp = this;
        
        listener = new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                
                final String syncCode = codeField.getText().toString().toUpperCase(Locale.US);
                
                if (syncCode.length() == 12)
                {
                    if (syncTask == null || syncTask != null || syncTask.isCancelled())
                    {
                        overlay.setVisibility(View.VISIBLE);
                        overlay.setClickable(true);
                        overlay.bringToFront();
                        
                        syncButton.setOnClickListener(null);
                        // codeField.setClickable(false);
                        
                        syncTask = new CheckSyncTask(getActivity());
                        
                        syncTask.setEndTaskListener(tmp);
                        syncTask.execute(syncCode);
                    }
                }
                else
                    codeField.setError(getActivity().getString(R.string.sync_enter_code_length));
            }
        };
        
        codeField.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event)
            {
                listener.onClick(v);
                return true;
            }
        });
        
        syncButton.setOnClickListener(listener);
        
        return view;
    }
    
    @Override
    public void onStop()
    {
        super.onStop();
        if (syncTask != null)
            syncTask.cancel(true);
    }
    
    @Override
    public void taskEnded(final JSONArray resultList)
    {
        
        overlay.setVisibility(View.GONE);
        overlay.setClickable(false);
        codeField.setClickable(true);
        syncButton.setOnClickListener(listener);
        
        if (resultList != null && resultList.length() > 0 && !syncTask.hasError())
            for (int i = 0; i < resultList.length(); i++)
            {
                
                codeField.clearFocus();
                syncButton.requestFocus();
                
                JSONObject resultListItem;
                try
                {
                    resultListItem = resultList.getJSONObject(i);
                    
                    final String title = resultListItem.optString("msg_title");
                    final String text = resultListItem.optString("msg_text");
                    final boolean success = resultListItem.optBoolean("success");
                    
                    if (text.length() > 0)
                    {
                        String popBackStackIncluding = null;
                        if (success)
                        {
                            popBackStackIncluding = "sync";
                            ((RMBTMainActivity) getActivity()).setHistoryDirty(true);
                            ((RMBTMainActivity) getActivity()).setSettings(null, null);
                            ((RMBTMainActivity) getActivity()).checkSettings(true, null);
                        }
                        
                        final DialogFragment newFragment = RMBTAlertDialogFragment.newInstance(title, text,
                                popBackStackIncluding);
                        
                        newFragment.show(getActivity().getFragmentManager(), "sync_msg");
                    }
                    
                }
                catch (final JSONException e)
                {
                    e.printStackTrace();
                }
                
            }
    }
}
