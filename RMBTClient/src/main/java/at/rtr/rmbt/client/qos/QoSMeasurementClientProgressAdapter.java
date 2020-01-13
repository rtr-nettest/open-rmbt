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

import at.rtr.rmbt.shared.qos.QosMeasurementType;
import at.rtr.rmbt.client.v2.task.QoSTestEnum;

public abstract class QoSMeasurementClientProgressAdapter implements QoSMeasurementClientProgressListener {

    @Override
    public void onProgress(float progress) {

    }

    @Override
    public void onQoSTypeProgress(QosMeasurementType qosType, float progress) {

    }

    @Override
    public void onQoSTypeStarted(QosMeasurementType qosType) {

    }

    @Override
    public void onQoSTypeFinished(QosMeasurementType qosType) {

    }

    @Override
    public void onQoSStatusChanged(QoSTestEnum newStatus) {

    }

    @Override
    public void onQoSTestsDefined(int testCount) {

    }
}
