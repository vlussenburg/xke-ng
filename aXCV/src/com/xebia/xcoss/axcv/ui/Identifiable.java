package com.xebia.xcoss.axcv.ui;

public class Identifiable {
	private String text;
	private int identifier;
	
	public Identifiable(String value, int id) {
		this.text = value;
		this.identifier = id;
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	public int getIdentifier() {
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
