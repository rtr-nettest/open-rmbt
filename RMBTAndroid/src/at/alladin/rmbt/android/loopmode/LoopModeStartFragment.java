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
package at.alladin.rmbt.android.loopmode;

import java.text.MessageFormat;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.AppConstants;
import at.alladin.rmbt.android.main.RMBTMainActivity;
import at.alladin.rmbt.android.preferences.RMBTPreferenceActivity;
import at.alladin.rmbt.android.util.ConfigHelper;

public class LoopModeStartFragment extends DialogFragment {
	
	public static LoopModeStartFragment newInstance() {
		final LoopModeStartFragment f = new LoopModeStartFragment();
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().setTitle(R.string.preferences_category_loop_mode);
		
		final View v = inflater.inflate(R.layout.loop_mode_start_dialog, container, false);
		
		final int maxDelay = ConfigHelper.getLoopModeMaxDelay(getActivity());
		final int maxMovement = ConfigHelper.getLoopModeMaxMovement(getActivity());
		final String info = getString(R.string.loop_mode_info);
		
		TextView infoText = (TextView) v.findViewById(R.id.loop_mode_info);
		infoText.setText(MessageFormat.format(info, maxMovement, maxDelay));
		
		final int maxTests = ConfigHelper.getLoopModeMaxTests(getActivity());
		final EditText maxTestsEdit = (EditText) v.findViewById(R.id.loop_mode_max_tests);
		maxTestsEdit.setText(String.valueOf(maxTests));
		maxTestsEdit.setSelection(String.valueOf(maxTests).length());
		
		Button goButton = (Button) v.findViewById(R.id.loop_mode_start_button);
		goButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					boolean startTest = true;
					if (!ConfigHelper.isDevEnabled(getActivity())) {
						startTest = RMBTPreferenceActivity.checkInputValidity(getActivity(), maxTestsEdit.getText().toString(),
								AppConstants.LOOP_MODE_MIN_TESTS, AppConstants.LOOP_MODE_MAX_TESTS, R.string.loop_mode_max_tests_invalid);
					}
				
					if (startTest) {
						LoopModeStartFragment.this.dismiss();
						ConfigHelper.setLoopModeMaxTests(getActivity(), Integer.parseInt(maxTestsEdit.getText().toString()));
						((RMBTMainActivity) getActivity()).startLoopTest();
					}
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		Button goToSettingsButton = (Button) v.findViewById(R.id.loop_mode_go_to_settings_button);
		goToSettingsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LoopModeStartFragment.this.dismiss();
				((RMBTMainActivity) getActivity()).showSettings();
			}
		});
		
		return v;
	}
}
