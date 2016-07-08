/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
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
package at.alladin.rmbt.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author lb
 *
 */
public class Randomizer {

	private final List<Integer> numberList;
	private int indexPointer;
	
	public Randomizer(int minValue, int maxValue) {
		this(minValue, maxValue, 1);
	}
	
	public Randomizer(int minValue, int maxValue, int step) {
		numberList = new ArrayList<>(maxValue - minValue);
		for (int i = minValue; i <= maxValue; i += step) {
			numberList.add(i);
		}
		Collections.shuffle(numberList);		
	}
	
	public synchronized int next() {
		final int nextInt = numberList.get(indexPointer++);
		if (indexPointer >= numberList.size()) {
			indexPointer = 0;
		}
		return nextInt;
	}	
}
