package com.xebia.xcoss.axcv;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class CVSettings extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
