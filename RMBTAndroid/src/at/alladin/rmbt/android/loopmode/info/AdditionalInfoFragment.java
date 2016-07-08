/*******************************************************************************
 * Copyright 2016 Specure GmbH
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
package at.alladin.rmbt.android.loopmode.info;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.loopmode.DetailsListItem;
import at.alladin.rmbt.android.util.InformationCollector;

public class AdditionalInfoFragment extends Fragment {

	public static AdditionalInfoFragment newInstance() {
		final AdditionalInfoFragment f = new AdditionalInfoFragment();
		return f;
	}
	
	private InformationCollector infoCollector;

	private ListView listView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.loop_mode_details_fragment, container, false);
		listView = (ListView) v.findViewById(R.id.lm_list);
		return v;
	}
	
	public boolean hasResults() {
		return infoCollector != null;
	}
		
	public void initList(InformationCollector infoCollector) {
		this.infoCollector = infoCollector;
		final List<DetailsListItem> mdList = new ArrayList<DetailsListItem>();
		mdList.add(new NetworkTypeDetailsItem(getActivity(), infoCollector));
		mdList.add(new NetworkIdDetailsItem(getActivity(), infoCollector));
		mdList.add(new SignalDetailsItem(getActivity(), infoCollector));
		mdList.add(new GpsDetailsItem(getActivity(), infoCollector));
		mdList.add(new GpsAccuracyDetailsItem(getActivity(), infoCollector));
		listView.setAdapter(new DetailsInfoListAdapter(getActivity(), mdList));
	}
	
	public void updateList() {
		if (listView.getAdapter() != null && infoCollector != null) {
			infoCollector.reInit();
			((DetailsInfoListAdapter) listView.getAdapter()).notifyDataSetChanged();
		}
	}

}
