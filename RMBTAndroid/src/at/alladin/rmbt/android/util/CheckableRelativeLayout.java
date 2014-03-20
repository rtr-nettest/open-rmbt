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
package at.alladin.rmbt.android.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.RelativeLayout;

/**
 * @see http 
 *      ://stackoverflow.com/questions/4842349/android-listview-with-radiobutton
 *      -checkbox-in-singlechoice-mode-and-a-custom-row
 * 
 */
public class CheckableRelativeLayout extends /* LinearLayout */RelativeLayout implements Checkable
{
    
    private CheckedTextView mCheckedTextView;
    private final Drawable mCheckDrawable;
    private final Drawable mRadioDrawable;
    private boolean mIsChecked;
    
    /**
     * Constructor.
     * 
     * @param context
     *            The context to operate in.
     * @param attrs
     *            The attributes defined in XML for this element.
     */
    public CheckableRelativeLayout(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
        
        TypedArray typedArray = null;
        
        // Cache the check box drawable.
        typedArray = context.getTheme()
                .obtainStyledAttributes(new int[] { android.R.attr.listChoiceIndicatorMultiple });
        
        if (typedArray != null && typedArray.length() > 0)
            mCheckDrawable = typedArray.getDrawable(0);
        else
            // Fallback if the target theme doesn't define a check box drawable.
            // Perhaps an application specific drawable should be used instead
            // of null.
            mCheckDrawable = null;
        
        // Careful with resources like this, we don't need any memory leaks.
        typedArray.recycle();
        
        // Cache the radio button drawable.
        typedArray = context.getTheme().obtainStyledAttributes(new int[] { android.R.attr.listChoiceIndicatorSingle });
        
        if (typedArray != null && typedArray.length() > 0)
            mRadioDrawable = typedArray.getDrawable(0);
        else
            // Fallback if the target theme doesn't define a radio button
            // drawable.
            // Perhaps an application specific drawable should be used instead
            // of null
            mRadioDrawable = null;
        
        // Careful with resources like this, we don't need any memory leaks.
        typedArray.recycle();
        
        mIsChecked = false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Checkable#isChecked()
     */
    @Override
    public boolean isChecked()
    {
        return mIsChecked;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onAttachedToWindow()
     */
    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        
        // Check if there is a valid GUI element that can visualize the current
        // check-state.
        if (mCheckedTextView != null)
        {
            final ViewParent p = getParent();
            
            // Check if the parent of this list item is a ListView
            if (p instanceof ListView)
            {
                final int choiceMode = ((ListView) p).getChoiceMode();
                
                // Decide which check-state notation to visualize (check box,
                // radio button or none).
                switch (choiceMode)
                {
                case ListView.CHOICE_MODE_MULTIPLE:
                    mCheckedTextView.setCheckMarkDrawable(mCheckDrawable);
                    break;
                
                case ListView.CHOICE_MODE_SINGLE:
                    mCheckedTextView.setCheckMarkDrawable(mRadioDrawable);
                    break;
                
                default:
                    mCheckedTextView.setCheckMarkDrawable(null);
                    break;
                }
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onFinishInflate()
     */
    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        mCheckedTextView = (CheckedTextView) findViewById(android.R.id.text1);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Checkable#setChecked(boolean)
     */
    @Override
    public void setChecked(final boolean checked)
    {
        mIsChecked = checked;
        
        if (mCheckedTextView != null)
            mCheckedTextView.setChecked(mIsChecked);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Checkable#toggle()
     */
    @Override
    public void toggle()
    {
        setChecked(!mIsChecked);
    }
    
}
