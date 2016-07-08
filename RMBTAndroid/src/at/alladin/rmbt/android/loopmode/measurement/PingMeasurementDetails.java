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
 * provides a {@link MeasurementDetailsItem} for the ping measurements
 * @author lb
 *
 */
public class PingMeasurementDetails implements MeasurementDetailsItem {

	final LoopModeResults results;
	final Context context;
	
	public PingMeasurementDetails(final Context context, final LoopModeResults results) {
		this.results = results;
		this.context = context;
	}
	
	@Override
	public boolean isRunning() {
		if (results != null && LoopModeResults.Status.RUNNING.equals(results.getStatus())) {
			final IntermediateResult r = results.getIntermediateResult();
			if (r != null) {
				return (TestStatus.INIT.equals(r.status) || TestStatus.PING.equals(r.status));
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
		return context.getResources().getString(R.string.lm_measurement_ping);
	}

	@Override
	public String getCurrent() {
		if (results != null) {
			if (LoopModeResults.Status.RUNNING.equals(results.getStatus()) && isDone()) {
				final IntermediateResult r = results.getIntermediateResult();
				if (r != null) {
					return String.format(Locale.US, "%.2f", ((float)r.pingNano / 1e6f));
				}
			}
			else if (!LoopModeResults.Status.RUNNING.equals(results.getStatus())) {
				LoopModeLastTestResults r = results.getLastTestResults();
				if (r != null && r.getTestResults() != null) {
					return String.format(Locale.US, "%.2f", (float)r.getTestResults().optDouble("ping_ms"));
				}
			}
		}
		
		return null;
	}

	@Override
	public String getMedian() {
		if (results != null) {
			//show median only during waiting screen 
			if (results.getPingList().size() > 0 && !LoopModeResults.Status.RUNNING.equals(results.getStatus())) {
				return String.format(Locale.US, "%.2f", ((float)results.getPingMedian() / 1e6f));
			}
			//show last test result if test is running and last test was successful 
			else if (LoopModeResults.Status.RUNNING.equals(results.getStatus())) {
				LoopModeLastTestResults r = results.getLastTestResults();
				if (r != null && r.getTestResults() != null) {
					return String.format(Locale.US, "%.2f", (float)r.getTestResults().optDouble("ping_ms"));
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
				return r != null && r.status.ordinal() > TestStatus.PING.ordinal();
			}
		}
		return false;
	}

}
