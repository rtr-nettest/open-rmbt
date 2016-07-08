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
import at.alladin.rmbt.android.loopmode.LoopModeLastTestResults;
import at.alladin.rmbt.android.loopmode.LoopModeResults;
import at.alladin.rmbt.android.loopmode.LoopModeResults.Status;
import at.alladin.rmbt.android.loopmode.measurement.MeasurementDetailsFragment.MeasurementDetailsItem;
import at.alladin.rmbt.client.helper.IntermediateResult;
import at.alladin.rmbt.client.helper.TestStatus;
import at.alladin.rmbt.client.v2.task.result.QoSServerResultCollection;

/**
 * provides a {@link MeasurementDetailsItem} for QoS results
 * @author lb
 *
 */
public class QoSMeasurementDetails implements MeasurementDetailsItem {

	final LoopModeResults results;
	final Context context;
	
	public QoSMeasurementDetails(final Context context, final LoopModeResults results) {
		this.results = results;
		this.context = context;
	}
	
	@Override
	public boolean isRunning() {
		if (results != null && LoopModeResults.Status.RUNNING.equals(results.getStatus())) {
			final IntermediateResult r = results.getIntermediateResult();
			if (r != null) {
				return (TestStatus.QOS_TEST_RUNNING.equals(r.status) || TestStatus.QOS_END.equals(r.status));
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
		return context.getResources().getString(R.string.lm_measurement_qos);
	}

	@Override
	public String getCurrent() {
		if (results != null && Status.IDLE.equals(results.getStatus())) {
			LoopModeLastTestResults r = results.getLastTestResults();
			if (r != null) {
				final QoSServerResultCollection qos = r.getQosResult();
				if (qos != null && qos.getQoSStatistics() != null) {
					return (qos.getQoSStatistics().getTestCounter()-qos.getQoSStatistics().getFailedTestsCounter()) 
							+ "/" + qos.getQoSStatistics().getTestCounter();
				}
			}
		}
		
		return null;
	}

	@Override
	public String getMedian() {		
		return null;
	}

	@Override
	public boolean isDone() {
		if (results != null) {
			if (LoopModeResults.Status.RUNNING.equals(results.getStatus())) {
				final IntermediateResult r = results.getIntermediateResult();
				return r != null && r.status.ordinal() > TestStatus.QOS_END.ordinal();
			}
		}
		return false;
	}

}
