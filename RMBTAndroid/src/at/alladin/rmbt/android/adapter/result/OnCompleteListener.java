/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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
package at.alladin.rmbt.android.adapter.result;


/**
 * 
 * @author lb
 *
 */
public interface OnCompleteListener {
	
	public final static int ERROR = -1;
	
	public final static int INITIALIZED = 0;
	
	public final static int DATA_LOADED = 1;
	
	/**
	 * 
	 */
	public void onComplete(int flag, Object object);
}
