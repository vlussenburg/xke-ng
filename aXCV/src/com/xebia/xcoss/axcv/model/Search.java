package com.xebia.xcoss.axcv.model;

import java.util.HashMap;
import java.util.Map;

public class Search {

	public enum Field {
		FREETEXT,
		NAME,
		MAIL,
	}
	
	private Map<Field, String> searchParms;

	public Search() {
		this.searchParms = new HashMap<Field, String>();
	}

	public void onFreeText(String text) {
		searchParms.put(Field.FREETEXT, text);
	}

}