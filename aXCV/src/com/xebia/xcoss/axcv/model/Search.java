package com.xebia.xcoss.axcv.model;

import java.util.HashMap;
import java.util.Map;

import com.xebia.xcoss.axcv.util.StringUtil;

public class Search {

	public enum Field {
		FREETEXT, AUTHOR, AFTER,
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

	public Search after(Moment moment) {
		if ( moment != null ) {
			searchParms.put(Field.AFTER, String.valueOf(moment.asLong()));
		}
		return this;
	}
}
