package com.xebia.xcoss.axcv.model;

import hirondelle.date4j.DateTime;

import java.util.HashMap;
import java.util.Map;

import com.xebia.xcoss.axcv.util.StringUtil;

public class Search {

	public enum Field {
		FREETEXT, NAME, AUTHOR, MAIL, FROM,
	}

	private Map<Field, String> searchParms;

	public Search() {
		this.searchParms = new HashMap<Field, String>();
	}

	public Search onFreeText(String text) {
		if (!StringUtil.isEmpty(text)) {
			searchParms.put(Field.FREETEXT, text.trim());
		}
		return this;
	}

	public Search onAuthor(Author author) {
		if (author != null) {
			searchParms.put(Field.AUTHOR, author.getUserId());
		}
		return this;
	}

	public Search onDateStart(DateTime dt) {
		if (dt != null) {
			searchParms.put(Field.FROM, dt.format("YYYY-MM-DD|T|hh:mm:ss.fff|Z|"));
		}
		return this;
	}

}
