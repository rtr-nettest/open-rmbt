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

package at.rtr.rmbt.qos.testserver.mock;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

import mockit.Mock;
import mockit.MockUp;

/**
 * 
 * @author Lukasz Budryk (lb@alladin.at)
 *
 */
public class RecordingFilterOutputStreamMock extends MockUp<FilterOutputStream> {
	
	final ByteArrayOutputStream baos;
	
	public RecordingFilterOutputStreamMock() {
		 this.baos = new ByteArrayOutputStream();
	}
	
	@Mock
    public void write(byte b[]) throws IOException {
    	baos.write(b);
    }

	public String getContentWithoutReset() {
		return baos.toString();
	}

	public String getContent() {
		final String content = baos.toString();
		baos.reset();
		return content;
	}

	public ByteArrayOutputStream getStream() {
		return baos;
	}
}
