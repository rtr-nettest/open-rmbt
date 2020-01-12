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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.measurement.initiation;

import com.fasterxml.jackson.annotation.JsonClassDescription;

/**
 * Speed measurement request options sent by the measurement agent.
 * For example, these could contain demands from the measurement agent (e.g. extended duration, streams, etc.).
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@io.swagger.annotations.ApiModel(description = "Speed measurement request options sent by the measurement agent. For example, these could contain demands from the measurement agent (e.g. extended duration, streams, etc.).")
@JsonClassDescription("Speed measurement request options sent by the measurement agent. For example, these could contain demands from the measurement agent (e.g. extended duration, streams, etc.).")
public class SpeedMeasurementRequestOptions extends MeasurementTypeRequestOptions {

}
