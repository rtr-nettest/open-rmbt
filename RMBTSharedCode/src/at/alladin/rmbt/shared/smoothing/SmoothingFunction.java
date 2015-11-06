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
package at.alladin.rmbt.shared.smoothing;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author lb
 *
 */
public enum SmoothingFunction implements SmoothingFunctionCalculation {
	
	/**
	 * equal number of data on each side
	 */
	CENTRAL_MOVING_AVARAGE {
		
		@Override
		public double smoothYPoint(final List<? extends Smoothable> valueList, final int index, final int dataAmount) {
			final int i = Math.max(index - (dataAmount / 2), 0);
			final int j = Math.min(index + (dataAmount / 2), valueList.size());
			
			double sum = 0;
			String s = "(";
			for (int x = i; x <= j; x++) {
				sum += valueList.get(x).getYValue();
				s += valueList.get(x).getYValue() + " ";
			}
			
			//System.out.println("smoothing i: " + index + " start i: " + i + ", end i: " + j + ", result: " + (sum / (double)(j-i+1)) + " -> " + s +")");
			
			return sum / (double)(j-i+1);
		}

		@Override
		public double smoothXPoint(List<? extends Smoothable> valueList, int index, int dataAmount) {
			final int i = Math.max(index - (dataAmount / 2), 0);
			final int j = Math.min(index + (dataAmount / 2), valueList.size());
			
			double sum = 0;
			String s = "(";
			for (int x = i; x <= j; x++) {
				sum += valueList.get(x).getXValue();
				s += valueList.get(x).getXValue() + " ";
			}
			
			//System.out.println("smoothing i: " + index + " start i: " + i + ", end i: " + j + ", result: " + (sum / (double)(j-i+1)) + " -> " + s +")");
			
			return sum / (double)(j-i+1);
		}
		
		@Override
		public int getStartingIndex(List<? extends Smoothable> valueList,	int dataAmount) {
			return Math.min(dataAmount/2, valueList.size());
		}

		@Override
		public int getEndingIndex(List<? extends Smoothable> valueList, int dataAmount) {
			return Math.max(valueList.size() - dataAmount/2 - 1, 0);
		}
	},
	
	/**
	 * previous n number of data
	 */
	SIMPLE_MOVING_AVARAGE {
		
		@Override
		public double smoothYPoint(final List<? extends Smoothable> valueList, final int index, final int dataAmount) {
			final int i = Math.max(index - dataAmount + 1, 0);
			final int j = Math.min(index, valueList.size());
			
			double sum = 0;
			for (int x = i; x <= j; x++) {
				sum += valueList.get(x).getYValue();
			}
			
			return sum / (double)(j-i+1);
		}

		@Override
		public double smoothXPoint(final List<? extends Smoothable> valueList, final int index, final int dataAmount) {
			final int i = Math.max(index - dataAmount + 1, 0);
			final int j = Math.min(index, valueList.size());
			
			double sum = 0;
			for (int x = i; x <= j; x++) {
				sum += valueList.get(x).getXValue();
			}
			
			return sum / (double)(j-i+1);
		}

		@Override
		public int getStartingIndex(List<? extends Smoothable> valueList, int dataAmount) {
			return Math.min(dataAmount-1, valueList.size());
		}

		@Override
		public int getEndingIndex(List<? extends Smoothable> valueList, int dataAmount) {
			return valueList.size() - 1;
		}
	};
	
	public static List<? extends Smoothable> smooth(final SmoothingFunction smoothingFunction, final List<? extends Smoothable> valueList, final int dataAmount) {
		
		if (valueList == null || valueList.size() < dataAmount) {
			return valueList; 
		}

		final int startingIndex = smoothingFunction.getStartingIndex(valueList, dataAmount);
		final int endingIndex = smoothingFunction.getEndingIndex(valueList, dataAmount);
		
		System.out.println("smoothing " + smoothingFunction + " width: " + dataAmount + ", start: " + startingIndex + ", end: " + endingIndex);
		
		if (startingIndex > endingIndex) {
			return valueList;
		}

		final List<Smoothable> resultList = new ArrayList<Smoothable>();
		
		for (int i = startingIndex; i <= endingIndex; i++) {
			resultList.add(new SmoothableImpl(smoothingFunction.smoothXPoint(valueList, i, dataAmount), smoothingFunction.smoothYPoint(valueList, i, dataAmount)));
		}
		
		return resultList;
	}
	
	/**
	 * 
	 * @author lb
	 *
	 */
	private final static class SmoothableImpl implements Smoothable {
		double xValue;
		double yValue;
		
		public SmoothableImpl(double xValue, double yValue) {
			this.xValue = xValue;
			this.yValue = yValue;
		}
		
		@Override
		public double getXValue() {
			return xValue;
		}
		
		@Override
		public double getYValue() {
			return yValue;
		}

		@Override
		public String toString() {
			return "SmoothableImpl [xValue=" + xValue + ", yValue=" + yValue
					+ "]";
		}
	}
}