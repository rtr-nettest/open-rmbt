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
package at.alladin.rmbt.android.adapter.result;

import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import at.alladin.rmbt.android.fragments.result.QoSTestDetailView;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.client.v2.task.result.QoSServerResult;
import at.alladin.rmbt.client.v2.task.result.QoSServerResultDesc;

public class QoSTestDetailPagerAdapter extends PagerAdapter {

    private final RMBTMainActivity activity;
    private final List<QoSServerResult> resultList;
    private final List<QoSServerResultDesc> descList;
        
	public QoSTestDetailPagerAdapter(final RMBTMainActivity _activity, 
			final List<QoSServerResult> resultList, final List<QoSServerResultDesc> descList) {
		
		super();
		
    	this.activity = _activity;
        this.resultList = resultList;
        this.descList = descList;
    }

	@Override
	public CharSequence getPageTitle(int position) {
		return "#" + (position + 1);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
        final Context context = container.getContext();
        View view = null;
        //view = new QoSCategoryView(context, activity, results.getTestDescMap().get(key), resultMap.get(key), descMap.get(key));
        view = new QoSTestDetailView(context, activity, resultList.get(position), descList);
        container.addView(view);
        return view;
	}
	
	@Override
	public int getItemPosition(Object object) {
	    return PagerAdapter.POSITION_NONE;
	}
	
	@Override
	public int getCount() {
		return resultList.size();
	}

    public boolean onBackPressed()
    {
    	activity.getSupportFragmentManager().popBackStack();
    	return true;
    }

	@Override
	public boolean isViewFromObject(View view, Object object) {
        return view == object;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
        final View view = (View) object;
        container.removeView(view);
	}	
}
