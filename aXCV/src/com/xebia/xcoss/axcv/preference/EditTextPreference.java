package com.xebia.xcoss.axcv.preference;

import android.content.Context;
import android.util.AttributeSet;
import com.xebia.xcoss.axcv.R;

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
		super.setText(text.trim());
		setSummary(text.trim());
	}
}
