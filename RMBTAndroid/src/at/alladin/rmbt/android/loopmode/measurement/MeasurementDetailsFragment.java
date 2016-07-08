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
package at.alladin.rmbt.android.loopmode.measurement;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.loopmode.DetailsListItem;
import at.alladin.rmbt.android.loopmode.LoopModeResults;
import at.alladin.rmbt.android.loopmode.measurement.SpeedMeasurementDetails.SpeedType;

public class MeasurementDetailsFragment extends Fragment {

	public static interface MeasurementDetailsItem extends DetailsListItem {
		boolean isRunning();
		boolean isDone();
		String getMedian();
	}
	
	public static MeasurementDetailsFragment newInstance() {
		final MeasurementDetailsFragment f = new MeasurementDetailsFragment();
		return f;
	}
	
	private LoopModeResults results;

	private ListView listView;
	
	private TextView currentText;
	
	private TextView medianText;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.loop_mode_measurement_fragment, container, false);
		listView = (ListView) v.findViewById(R.id.lm_list);
		currentText = (TextView) v.findViewById(R.id.lm_measurement_list_header_current);
		medianText = (TextView) v.findViewById(R.id.lm_measurement_list_header_median);
		return v;
	}
	
	public boolean hasResults() {
		return results != null;
	}
	
	public void initList(LoopModeResults results) {
		this.results = results;
		final List<MeasurementDetailsItem> mdList = new ArrayList<MeasurementDetailsFragment.MeasurementDetailsItem>();
		mdList.add(new PingMeasurementDetails(getActivity(), results));
		mdList.add(new SpeedMeasurementDetails(getActivity(), results, SpeedType.DOWN));
		mdList.add(new SpeedMeasurementDetails(getActivity(), results, SpeedType.UP));
		mdList.add(new QoSMeasurementDetails(getActivity(), results));
		listView.setAdapter(new MeasurementListAdapter(mdList));
	}
	
	public void updateList() {
		if (listView.getAdapter() != null) {
			((MeasurementListAdapter) listView.getAdapter()).notifyDataSetChanged();
			
			if (results != null && LoopModeResults.Status.RUNNING.equals(results.getStatus())) {
				currentText.setText(R.string.loop_test_current);
				if (results.getLastTestResults() != null) {
					medianText.setText(R.string.loop_test_last);
				}
				else {
					medianText.setText("");
				}
			}
			else {
				currentText.setText(R.string.loop_test_last);
				medianText.setText(R.string.loop_test_median);				
			}
		}
	}
	
	public class MeasurementListAdapter extends BaseAdapter {

		private class ViewHolder {
			TextView measurement;
			ProgressBar progress;
			ImageView status;
			TextView current;
			TextView median;
		}
		
		private final List<MeasurementDetailsItem> items;
		
		public MeasurementListAdapter(List<MeasurementDetailsItem> items) {
			this.items = items;
		}
		
		@Override
		public int getCount() {
			return items != null ? items.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return items != null ? items.get(position) : null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			
			if (convertView == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				convertView = inflater.inflate(R.layout.loop_mode_measurement_list_item, parent, false);
				holder = new ViewHolder();
				holder.measurement = (TextView) convertView.findViewById(R.id.lm_measurement);
				holder.progress = (ProgressBar) convertView.findViewById(R.id.lm_measurement_progress);
				holder.status = (ImageView) convertView.findViewById(R.id.lm_measurement_status);
				holder.current = (TextView) convertView.findViewById(R.id.lm_measurement_current);
				holder.median = (TextView) convertView.findViewById(R.id.lm_measurement_median);
				convertView.setTag(holder);
			}

			holder = (ViewHolder) convertView.getTag();
			
			final MeasurementDetailsItem md = (MeasurementDetailsItem) getItem(position);
			holder.measurement.setText(md.getTitle());
			holder.current.setText(md.getCurrent());
			holder.median.setText(md.getMedian());
			
			if (md.isDone()) {
				holder.progress.setVisibility(View.GONE);
				holder.status.setVisibility(View.VISIBLE);
				holder.status.setImageResource(md.getStatusResource());
			}
			else {
				holder.status.setVisibility(View.GONE);
				holder.progress.setVisibility(md.isRunning() ? View.VISIBLE : View.INVISIBLE);
			}
			
			return convertView;
		}
		
	}
}
