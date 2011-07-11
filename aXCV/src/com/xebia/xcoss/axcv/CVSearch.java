package com.xebia.xcoss.axcv;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.StringUtil;

public class CVSearch extends SearchActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected List<Author> searchAuthors(String text) {
		if ( StringUtil.isEmpty(text)) {
			return new ArrayList<Author>();
		}
		Search search = new Search();
		search.onFreeText(text);
		return getConferenceServer().searchAuthors(search);
	}

	@Override
	protected List<Session> searchSessions(String text) {
		if ( StringUtil.isEmpty(text)) {
			return new ArrayList<Session>();
		}
		Search search = new Search();
		search.onFreeText(text);
		return getConferenceServer().searchSessions(search);
	}
}