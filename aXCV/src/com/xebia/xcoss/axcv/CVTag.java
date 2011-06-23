package com.xebia.xcoss.axcv;

import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS;

public class CVTag extends SearchActivity {

	private String searchTag = "";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		searchTag = getIntent().getDataString().replace(XCS.TAG.LINK, "").trim();

		TextView view = (TextView) findViewById(R.id.searchTitle);
		view.setText(searchTag);

		findViewById(R.id.searchBlock).setVisibility(View.GONE);
		findViewById(R.id.firstDivider).setVisibility(View.GONE);
	}

	@Override
	protected List<Author> searchAuthors(String text) {
		Search search = new Search();
		search.onFreeText(searchTag);
		return getConferenceServer().searchAuthors(search);
	}

	@Override
	protected List<Session> searchSessions(String text) {
		Search search = new Search();
		search.onFreeText(searchTag);
		return getConferenceServer().searchSessions(search);
	}
}