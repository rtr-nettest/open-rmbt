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

package at.rtr.rmbt.shared.model.berec.collector.api.v1.dto.lmap.control;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * TODO
 * 
 * @author fk
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "deserialize_type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LmapStopDurationDto.class, name = "stop_duration"),
        @JsonSubTypes.Type(value = LmapStopEndDto.class, name = "stop_end")
})
public abstract class LmapStopDto {

}
