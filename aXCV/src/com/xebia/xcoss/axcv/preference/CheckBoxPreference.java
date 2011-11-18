package com.xebia.xcoss.axcv.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.xebia.xcoss.axcv.service.SignalRetriever;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class CheckBoxPreference extends android.preference.CheckBoxPreference {

	private SignalRetriever signalRetriever;

	public CheckBoxPreference(Context context) {
		super(context);
	}

	public CheckBoxPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public CheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
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
	protected void onClick() {
		super.onClick();
		signalRetriever.onSignal(getContext());
	}
}
