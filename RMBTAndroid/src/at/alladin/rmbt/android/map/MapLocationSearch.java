/*******************************************************************************
 * Copyright 2015, 2016 alladin-IT GmbH
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

import java.util.concurrent.TimeUnit;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.location.Address;
import android.location.Geocoder;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.map.MapLocationRequestTask.OnRequestFinished;

public class MapLocationSearch {
	
	private final static int TIMEOUT_MS = 10000;
	
	public static void showDialog(final RMBTMapFragment mapFragment) {
		if (!Geocoder.isPresent()) {
			Toast.makeText(mapFragment.getActivity(), R.string.map_search_location_not_supported, Toast.LENGTH_SHORT).show();
			return;
		}
		
		final AlertDialog dialog;
		
        final EditText input = new EditText(mapFragment.getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        
        final AlertDialog.Builder builder = new AlertDialog.Builder(mapFragment.getActivity());
        builder.setView(input);
        builder.setTitle(R.string.map_search_location_dialog_title);
        builder.setMessage(R.string.map_search_location_dialog_info);
        builder.setPositiveButton(android.R.string.search_go, new OnClickListener()
        {
            @Override
            public void onClick(final DialogInterface dialog, final int which)
            {            	
                try {
                	final Runnable taskRunnable = new Runnable() {
						
						@Override
						public void run() {
		                	final MapLocationRequestTask task = new MapLocationRequestTask(mapFragment.getActivity(), new OnRequestFinished() {
								@Override
								public void finished(Address address) {
									if (address != null) {
					           			mapFragment.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(
					           					new LatLng(address.getLatitude(), address.getLongitude()), mapFragment.getMap().getCameraPosition().zoom));
									}
									else {
					                	Toast.makeText(mapFragment.getActivity(), R.string.map_search_location_dialog_not_found, Toast.LENGTH_SHORT).show();
									}
								}
							});
		                	
		                	try {
								task.execute(input.getText().toString()).get(TIMEOUT_MS, TimeUnit.NANOSECONDS);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};
               		
					final Thread t = new Thread(taskRunnable);
					t.start();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }                
            }
        });
        
        builder.setNegativeButton(android.R.string.cancel, null);
        
        dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
	}
}
