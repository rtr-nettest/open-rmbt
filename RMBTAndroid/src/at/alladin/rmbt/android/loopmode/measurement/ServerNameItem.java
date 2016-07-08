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

import android.content.Context;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.loopmode.DetailsListItem;
import at.alladin.rmbt.android.loopmode.LoopModeResults;

/**
 * provides information about the server name during a test
 * @author lb
 *
 */
public class ServerNameItem implements DetailsListItem {

	final LoopModeResults results;
	final Context context;
	
	public ServerNameItem(final Context context, final LoopModeResults results) {
		this.results = results;
		this.context = context;
	}
	
	@Override
	public String getTitle() {
		return context.getResources().getString(R.string.lm_measurement_server);
	}

	@Override
	public String getCurrent() {
		if (results != null && results.getCurrentTest() != null) {
			return results.getCurrentTest().getServerName();
		}
		
		return null;
	}

	@Override
	public int getStatusResource() {
		return DetailsListItem.STATUS_RESOURCE_NOT_AVAILABLE;
	}

}
