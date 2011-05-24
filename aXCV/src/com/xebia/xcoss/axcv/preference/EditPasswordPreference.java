package com.xebia.xcoss.axcv.preference;

import android.content.Context;
import android.content.res.Resources;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.util.SecurityUtils;

public class EditPasswordPreference extends EditTextPreference {

	private Context ctx;
	
	public EditPasswordPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.ctx = context;
	}

	public EditPasswordPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ctx = context;
	}

	public EditPasswordPreference(Context context) {
		super(context);
		this.ctx = context;
	}

	@Override
	public String getText() {
		String text = super.getText();
		if ( text != null && text.trim().length() != 0 ) {
			return SecurityUtils.decrypt(text);
		}
		return "";
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		super.setText(restoreValue ? getPersistedString(null) : (String) defaultValue);
		updateMessage();
	}

	@Override
	public void setText(String text) {
		String result = "";
		if ( text != null && text.trim().length() != 0 ) {
			result = SecurityUtils.encrypt(text);
		}
		super.setText(result);
		updateMessage();
	}
	
	private void updateMessage() {
		String result = getText();
		Resources resources = ctx.getResources();
		if ( result != null && result.trim().length() != 0 ) {
			result = resources.getString(R.string.password_set);
		} else {
			result = resources.getString(R.string.password_unset);
		}
		setSummary(result);
	}
}
