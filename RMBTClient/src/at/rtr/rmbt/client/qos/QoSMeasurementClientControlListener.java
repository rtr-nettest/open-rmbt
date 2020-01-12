/*******************************************************************************
 * Copyright 2019 alladin-IT GmbH
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

package at.rtr.rmbt.client.qos;

import java.util.List;

import at.rtr.rmbt.shared.qos.QosMeasurementType;
import at.rtr.rmbt.client.v2.task.result.QoSResultCollector;

public interface QoSMeasurementClientControlListener {

    public void onMeasurementStarted(List<QosMeasurementType> testsToBeExecuted);

    /**
     * If the user stopped the test
     */
    public void onMeasurementStopped();

    /**
     * If the qos test ran into an error
     */
    public void onMeasurementError(Exception e);

    /**
     * If the qos test finished naturally
     * @param
     */
    public void onMeasurementFinished(String qostTestUuid, QoSResultCollector resultCollector);
}
