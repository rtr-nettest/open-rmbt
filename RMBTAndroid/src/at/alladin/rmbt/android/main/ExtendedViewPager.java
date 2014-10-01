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
package at.alladin.rmbt.android.main;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 
 * @author lb
 *
 */
public class ExtendedViewPager extends ViewPager {

	boolean isPagingDisabled = false;

	/**
	 * 
	 * @param context
	 */
	public ExtendedViewPager(Context context) {
		this(context, null);
	}

	/**
	 * 
	 * @param context
	 * @param attrs
	 */
	public ExtendedViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.view.ViewPager#onInterceptTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if (isPagingDisabled) {
			return false;
		}
		
		return super.onInterceptTouchEvent(arg0);
	}

	/**
	 * 
	 * @param isPagingDisabled
	 */
	public void setPagingDisabled(boolean isPagingDisabled) {
		this.isPagingDisabled = isPagingDisabled;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isPagingDisabled() {
		return this.isPagingDisabled;
	}
}