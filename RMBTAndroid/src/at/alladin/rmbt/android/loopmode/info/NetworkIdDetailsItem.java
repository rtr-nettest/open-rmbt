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

import android.content.Context;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.loopmode.DetailsListItem;
import at.alladin.rmbt.android.util.InformationCollector;

/**
 * 
 * @author lb
 *
 */
public class NetworkIdDetailsItem implements DetailsListItem {

	private InformationCollector infoCollector;
	private Context context;
	
	private String lastNetworkName;
	
	public NetworkIdDetailsItem(final Context context, final InformationCollector infoCollector) {
		this.infoCollector = infoCollector;
		this.context = context;
	}
	
	@Override
	public String getTitle() {
		return context.getResources().getString(R.string.lm_details_network_id);
	}

	@Override
	public String getCurrent() {
		if (infoCollector != null) {
			lastNetworkName = infoCollector.getOperatorName();
			if (lastNetworkName != null && !"()".equals(lastNetworkName)) {
				return lastNetworkName;
			}
		}
		
		return null;
	}

	@Override
	public int getStatusResource() {
		return DetailsListItem.STATUS_RESOURCE_NOT_AVAILABLE;
	}

}
