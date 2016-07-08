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

import java.util.Locale;

import android.content.Context;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.loopmode.LoopModeLastTestResults;
import at.alladin.rmbt.android.loopmode.LoopModeResults;
import at.alladin.rmbt.android.loopmode.measurement.MeasurementDetailsFragment.MeasurementDetailsItem;
import at.alladin.rmbt.client.helper.IntermediateResult;
import at.alladin.rmbt.client.helper.TestStatus;

/**
 * provides a {@link MeasurementDetailsItem} for the both speed measurements (up + down)
 * @author lb
 *
 */
public class SpeedMeasurementDetails implements MeasurementDetailsItem {

	public static enum SpeedType {
		UP,
		DOWN
	}
	
	final LoopModeResults results;
	final Context context;
	final SpeedType speedType;
	
	public SpeedMeasurementDetails(final Context context, final LoopModeResults results, final SpeedType speedType) {
		this.results = results;
		this.context = context;
		this.speedType = speedType;
	}
	
	@Override
	public boolean isRunning() {
		if (results != null && LoopModeResults.Status.RUNNING.equals(results.getStatus())) {
			final IntermediateResult r = results.getIntermediateResult();
			if (r != null) {
				if (TestStatus.DOWN.equals(r.status) &&	SpeedType.DOWN.equals(speedType)) {
					return true;
				}
				else if ((TestStatus.UP.equals(r.status) || TestStatus.INIT_UP.equals(r.status))  && SpeedType.UP.equals(speedType)) {
					return true;
				}
			}
		}
				
		return false;
	}

	@Override
	public int getStatusResource() {
		return R.drawable.loop_measurement_done;
	}

	@Override
	public String getTitle() {
		switch (speedType) {
		case DOWN:
			return context.getResources().getString(R.string.lm_measurement_down);
		case UP:
			return context.getResources().getString(R.string.lm_measurement_up);
		}
		
		return null;
	}

	@Override
	public String getCurrent() {
		if (results != null) {
			if (LoopModeResults.Status.RUNNING.equals(results.getStatus()) && (isDone() || isRunning())) {
				final IntermediateResult r = results.getIntermediateResult();
				if (r != null) {
					switch (speedType) {
					case DOWN:
						return String.format(Locale.US, "%.2f", Math.abs(((float)r.downBitPerSec / 1e6f)));
					case UP:
						if (r.upBitPerSec > 0 || isDone()) {
							return String.format(Locale.US, "%.2f", Math.abs(((float)r.upBitPerSec / 1e6f)));
						}
					} 
				}
			}
			else if(!LoopModeResults.Status.RUNNING.equals(results.getStatus())) {
				LoopModeLastTestResults r = results.getLastTestResults();
				if (r != null && r.getTestResults() != null) {
					switch (speedType) {
					case DOWN:
						return String.format(Locale.US, "%.2f", (((float)r.getTestResults().optLong("download_kbit", 0)) / 1e3f));
					case UP:
						return String.format(Locale.US, "%.2f", (((float)r.getTestResults().optLong("upload_kbit", 0)) / 1e3f));
					}
				}
			}
		}
		
		return null;
	}

	@Override
	public String getMedian() {
		//show median values only if on waiting screen
		if (results != null) {
			if (!LoopModeResults.Status.RUNNING.equals(results.getStatus())) {
				switch (speedType) {
				case DOWN:
					if (results.getDownList().size() > 0) {
						return String.format(Locale.US, "%.2f", ((float)results.getDownMedian() / 1e3f));
					}
					break;
				case UP:
					if (results.getUpList().size() > 0) {
						return String.format(Locale.US, "%.2f", ((float)results.getUpMedian() / 1e3f));
					}
					break;
				}
			}
			//show last test result if test is running and last test was successful 
			else if(LoopModeResults.Status.RUNNING.equals(results.getStatus())) {
				LoopModeLastTestResults r = results.getLastTestResults();
				if (r != null && r.getTestResults() != null) {
					switch (speedType) {
					case DOWN:
						return String.format(Locale.US, "%.2f", (((float)r.getTestResults().optLong("download_kbit", 0)) / 1e3f));
					case UP:
						return String.format(Locale.US, "%.2f", (((float)r.getTestResults().optLong("upload_kbit", 0)) / 1e3f));
					}
				}
			}
		}
		
		return null;
	}

	@Override
	public boolean isDone() {
		if (results != null) {
			if (LoopModeResults.Status.RUNNING.equals(results.getStatus())) {
				final IntermediateResult r = results.getIntermediateResult();
				switch(speedType) {
				case DOWN:
					return r != null && r.status.ordinal() > TestStatus.DOWN.ordinal();
				case UP:
					return r != null && r.status.ordinal() > TestStatus.INIT_UP.ordinal();
				}
			}
		}
		return false;
	}

}
