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


import at.rtr.rmbt.client.v2.task.QoSTestEnum;
import at.rtr.rmbt.shared.qos.QosMeasurementType;

public interface QoSMeasurementClientProgressListener {

    /**
     * Register for overall progress events
     * @param progress [0, 1]
     */
    public void onProgress(float progress);

    /**
     * On progress for the specific qosTypes
     * @param progress [0, 1]
     */
    public void onQoSTypeProgress(QosMeasurementType qosType, float progress);

//    /**
//     * On progress for the single qosTests
//     * @param event
//     */
//    public void onQoSTestProgress(Event event);

    /**
     * Called when the first test of the QosMeasurementType is started
     * @param qosType
     */
    public void onQoSTypeStarted(QosMeasurementType qosType);

    /**
     * Called when the last test of the QosMeasurementType has ended
     * @param qosType
     */
    public void onQoSTypeFinished(QosMeasurementType qosType);

    /**
     * Called when a new qosTestStatus has been reached (e.g. QoS_RUNNING when the first QoS test is executed)
     * @param newStatus
     */
    public void onQoSStatusChanged(QoSTestEnum newStatus);

    /**
     * Called when the qos tests have been defined
     * @param testCount the total # of qos tests to be executed
     */
    public void onQoSTestsDefined(int testCount);

//    public void onQoSTestStarted(Event event);

//    public void onQoSTestFinished(Event event);

}
