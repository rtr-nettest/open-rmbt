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
package at.alladin.rmbt.android.test;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import at.alladin.rmbt.android.util.ConfigHelper;

public class LoopModeUnlockTest {

	@Test
	public void unlockCodeCheck() {
		assertEquals("valid code 56676602", true, ConfigHelper.isValidCheckSum(56676602));
	}
}
