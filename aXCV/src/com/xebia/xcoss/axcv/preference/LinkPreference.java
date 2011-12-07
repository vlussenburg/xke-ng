package com.xebia.xcoss.axcv.preference;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import com.xebia.xcoss.axcv.R;

public class LinkPreference extends android.preference.Preference {

	private String url;

	public LinkPreference(Context context) {
		super(context);
	}

	public LinkPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public LinkPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		int count = attrs.getAttributeCount();
		for (int i = 0; i < count; i++) {
			if ("url".equals(attrs.getAttributeName(i))) {
				url = attrs.getAttributeValue(i);
				break;
			}
		}
	}

	@Override
	protected void onClick() {
		Intent i = new Intent(Intent.ACTION_VIEW);  
		i.setData(Uri.parse(url));  
		getContext().startActivity(i);
		super.onClick();
	}
}
