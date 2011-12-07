package com.xebia.xcoss.axcv.preference;

import android.content.Context;
import android.util.AttributeSet;

import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.service.SignalRetriever;
import com.xebia.xcoss.axcv.util.StringUtil;

public class ListPreference extends android.preference.ListPreference {

	private SignalRetriever signalRetriever;

	public ListPreference(Context context) {
		super(context);
	}

	public ListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		int count = attrs.getAttributeCount();
		for (int i = 0; i < count; i++) {
			if ("signalRetriever".equals(attrs.getAttributeName(i))) {
				String srname = attrs.getAttributeValue(i);
				if (!StringUtil.isEmpty(srname)) {
					if (srname.startsWith(".")) {
						srname = getContext().getPackageName() + srname;
					}
					try {
						Class<?> srClass = Class.forName(srname);
						Object instance = srClass.newInstance();
						if (instance instanceof SignalRetriever) {
							this.signalRetriever = (SignalRetriever) instance;
						}
					}
					catch (Exception e) {
					}
				}
				break;
			}
		}
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
		signalRetriever.onSignal(getContext());
	}
}
