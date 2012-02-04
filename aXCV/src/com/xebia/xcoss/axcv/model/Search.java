package com.xebia.xcoss.axcv.model;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.xebia.xcoss.axcv.util.StringUtil;

public class Search {

	public enum Field {
		FREETEXT ("text"), 
		AUTHOR_ID ("userid"), 
		AUTHOR_NAME ("username"), 
		AFTER ("from"),
		BEFORE ("until"),
		;
		
		protected String name;
		private Field(String name) { this.name = name; }
	}

	private Map<String, String> searchParms;

	public Search() {
		this.searchParms = new HashMap<String, String>();
	}

	public Search onFreeText(String text) {
		if (!StringUtil.isEmpty(text)) {
			searchParms.put(Field.FREETEXT.name, text.trim());
		}
		return this;
	}

	public Search onAuthor(Author author) {
		if (author != null) {
			if (!StringUtil.isEmpty(author.getUserId())) searchParms.put(Field.AUTHOR_ID.name, author.getUserId());
			if (!StringUtil.isEmpty(author.getName())) searchParms.put(Field.AUTHOR_NAME.name, author.getName());
		}
		Log.i("Search", searchParms.keySet().toString() + " = " + searchParms.values().toString());
		return this;
	}

	public Search after(Moment moment) {
		if (moment != null) {
			searchParms.put(Field.AFTER.name, String.valueOf(moment.asLong()));
		}
		return this;
	}
}
