package com.xebia.xcoss.axcv.model;

import java.util.HashMap;
import java.util.Map;

import com.xebia.xcoss.axcv.ui.StringUtil;

public class Search {

	public enum Field {
		FREETEXT, NAME, AUTHOR, MAIL,
	}

	private Map<Field, String> searchParms;

	public Search() {
		this.searchParms = new HashMap<Field, String>();
	}

	public void onFreeText(String text) {
		if (!StringUtil.isEmpty(text)) {
			searchParms.put(Field.FREETEXT, text.trim());
		}
	}

	public void onAuthor(Author author) {
		if (author != null) {
			searchParms.put(Field.AUTHOR, author.getUserId());
		}
	}

}
