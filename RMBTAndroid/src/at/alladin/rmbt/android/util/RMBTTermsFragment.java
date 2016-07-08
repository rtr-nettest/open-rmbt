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

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import at.alladin.openrmbt.android.R;

public class RMBTTermsFragment extends Fragment
{
    
    // private static final String DEBUG_TAG = "RMBTTermsFragment";
    
    private WebView webview;
    
    private Activity activity;
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        
        activity = getActivity();
        
        webview = new WebView(activity);
        
        /* JavaScript must be enabled if you want it to work, obviously */
        // webview.getSettings().setJavaScriptEnabled(true);
        
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setBuiltInZoomControls(true);
        
        webview.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onReceivedError(final WebView view, final int errorCode, final String description,
                    final String failingUrl)
            {
                super.onReceivedError(view, errorCode, description, failingUrl);
                webview.loadUrl("file:///android_res/raw/error.html");
            }
        });
        
        webview.loadUrl(this.getString(R.string.url_terms));
        
        return webview;
    }
    
}
