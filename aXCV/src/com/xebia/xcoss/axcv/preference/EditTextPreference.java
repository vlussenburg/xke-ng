package com.xebia.xcoss.axcv.preference;

import com.xebia.xcoss.axcv.util.SecurityUtils;
import com.xebia.xcoss.axcv.util.XCS;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

public class EditTextPreference extends android.preference.EditTextPreference {

	public EditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public EditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EditTextPreference(Context context) {
		super(context);
	}

	@Override
	public void setText(String text) {
		super.setText(text);
		Log.i(XCS.PROPERTIES, "Setting text to '"+text+"'");
		setSummary(text);
	}
}
