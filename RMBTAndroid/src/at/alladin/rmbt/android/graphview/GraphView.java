/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
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
package at.alladin.rmbt.android.graphview;

import java.util.List;

import at.alladin.rmbt.android.util.net.NetworkUtil.MinMax;

/**
 * 
 * @author lb
 *
 */
public interface GraphView {
	
	/**
	 * 
	 * @author lb
	 *
	 */
	public static class GraphLabel {
		protected String color;
		protected String text;
		
		public GraphLabel(String text, String color) {
			this.color = color;
			this.text = text;
		}
	
		public String getText() {
			return text;
		}
	
		public void setText(String text) {
			this.text = text;
		}
	
		public String getColor() {
			return color;
		}
	
		public void setColor(String color) {
			this.color = color;
		}
		
		public static String colorToHex(int color) {
			return String.format("#%06X", (0xFFFFFF & color));
		}
	
		@Override
		public String toString() {
			return "GraphLabel [color=" + color + ", text=" + text + "]";
		}
	}

	public void addGraph(final GraphService graph);
	
    public int getGraphWidth();
    
    public int getGraphHeight();
    
    public float getGraphStrokeWidth();
    
    public void setSignalRange(final int min, final int max);
    
    public MinMax<Integer> getSignalRange();
    
    public void removeSignalRange();
    
    public void invalidate();
    
    public void recycle();
    
    public void setVisibility(final int visibility);
    
	public List<GraphView.GraphLabel> getLabelInfoVerticalList();

	public void setLabelInfoVerticalList(List<GraphView.GraphLabel> labelInfoVerticalList);
	
	public void updateGrid(int cells, float rows);
	
	public List<GraphView.GraphLabel> getRowLinesLabelList();

	public void setRowLinesLabelList(List<GraphView.GraphLabel> rowLabelList);

}
