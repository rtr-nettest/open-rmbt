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
package at.alladin.rmbt.android.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.inputmethod.InputMethodManager;

public class RMBTAlertDialogFragment extends DialogFragment
{
    
    private String popBackStackIncluding;
    
    public static RMBTAlertDialogFragment newInstance(final String title, final String message, final String popBackStackIncluding)
    {
        final RMBTAlertDialogFragment frag = new RMBTAlertDialogFragment();
        final Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putString("popBackStackIncluding", popBackStackIncluding);
        frag.setArguments(args);
        return frag;
    }
    
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        final String title = getArguments().getString("title");
        final String message = getArguments().getString("message");
        popBackStackIncluding = getArguments().getString("popBackStackIncluding");
        
        final AlertDialog alert = new AlertDialog.Builder(getActivity())
        // .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(title).setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .create();
                
        return alert;
    }
    
    @Override
    public void onDismiss(final DialogInterface dialog)
    {
//         close keyboard if open
        try
        {
            final InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
        catch (Exception e)
        {
        }
        
        if (popBackStackIncluding != null)
            getActivity().getFragmentManager().popBackStack(popBackStackIncluding, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        
        super.onDismiss(dialog);
    }
}
