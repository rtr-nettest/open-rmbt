/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
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
 *******************************************************************************/
package at.alladin.rmbt.android.map;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import at.alladin.openrmbt.android.R;

public class MapLocationRequestTask extends AsyncTask<String, Void, Address>  {

	public interface OnRequestFinished {
		void finished(Address address);
	}
	
	final Activity activity;
	final OnRequestFinished onRequestFinished;
	boolean showProgressDialog = true;
	ProgressDialog progress;
	
	public MapLocationRequestTask(final Activity activity, OnRequestFinished onRequestFinished) {
		this.activity = activity;
		this.onRequestFinished = onRequestFinished;
	}
	
	public boolean isShowProgressDialog() {
		return showProgressDialog;
	}

	public void setShowProgressDialog(boolean showProgressDialog) {
		this.showProgressDialog = showProgressDialog;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		if (showProgressDialog) {
			activity.runOnUiThread(new Runnable() {			
				@Override
				public void run() {
					progress = ProgressDialog.show(activity, 
							activity.getText(R.string.map_search_location_progress_title), 
							activity.getText(R.string.map_search_location_progress_message), true);
				}
			});
		}
	}
	
	@Override
	protected Address doInBackground(String... params) {
    	final Geocoder geocoder = new Geocoder(activity);
   		List<Address> addressList;
		try {
			addressList = geocoder.getFromLocationName(params[0], 1);
	   		if (addressList != null && addressList.size() > 0) {
	   			return addressList.get(0);
	   		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
   		return null;
	}

	@Override
	protected void onPostExecute(Address result) {
		super.onPostExecute(result);
		
		if (showProgressDialog && progress != null) {
			activity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					progress.dismiss();
				}
			});
		}
		
		if (onRequestFinished != null) {
			onRequestFinished.finished(result);
		}		
	}
}
