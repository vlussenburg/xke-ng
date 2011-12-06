package com.xebia.xcoss.axcv.ui;

import com.xebia.xcoss.axcv.R;


public class Identifiable {
	private String text;
	private String identifier;
	
	public Identifiable(String value, String id) {
		this.text = value;
		this.identifier = id;
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public static CharSequence[] stringValue(Identifiable[] data) {
		if ( data == null ) {
			return null;
		}
		CharSequence[] result = new CharSequence[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = data[i].toString();
		}
		return result;
	}
}
