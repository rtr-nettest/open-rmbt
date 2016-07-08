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

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.loopmode.DetailsListItem;

public class DetailsInfoListAdapter extends BaseAdapter {

	private class ViewHolder {
		TextView title;
		ImageView status;
		TextView current;
	}
	
	private final List<DetailsListItem> items;
	private final Activity context;
	
	public DetailsInfoListAdapter(final Activity conetxt, List<DetailsListItem> items) {
		this.items = items;
		this.context = conetxt;
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
			LayoutInflater inflater = context.getLayoutInflater();
			convertView = inflater.inflate(R.layout.loop_mode_details_list_item, parent, false);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.lm_details_title);
			holder.status = (ImageView) convertView.findViewById(R.id.lm_details_status);
			holder.current = (TextView) convertView.findViewById(R.id.lm_details_current);
			convertView.setTag(holder);
		}

		holder = (ViewHolder) convertView.getTag();
		
		final DetailsListItem md = (DetailsListItem) getItem(position);
		holder.title.setText(md.getTitle());
		holder.current.setText(md.getCurrent());			
		
		if (md.getStatusResource() != DetailsListItem.STATUS_RESOURCE_NOT_AVAILABLE) {
			holder.status.setVisibility(View.VISIBLE);
			holder.status.setImageResource(md.getStatusResource());
		}
		else {
			holder.status.setVisibility(View.GONE);
		}
		
		return convertView;
	}
	
}