package com.xebia.xcoss.axcv.preference;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

public class EditDateTimeFormatPreference extends EditTextPreference {

	public EditDateTimeFormatPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public EditDateTimeFormatPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EditDateTimeFormatPreference(Context context) {
		super(context);
	}

	@Override
	public void setText(String text) {
		try {
			String value = new SimpleDateFormat(text.trim()).format(new Date());
			super.setText(text.trim());
			setSummary(value);
		}
		catch (Exception e) {
			Toast.makeText(getContext(), "Invalid format!", Toast.LENGTH_LONG);
		}
	}
}
