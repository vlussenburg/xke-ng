package com.xebia.xcoss.axcv;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.ui.SearchResultAdapter;
import com.xebia.xcoss.axcv.util.XCS;

public class CVTag extends BaseActivity {

	private List<Object> searchResults;
	private SearchResultAdapter searchAdapter;

	// TODO Use baseclass
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.search);

		String tag = getIntent().getDataString().replace(XCS.TAG.LINK, "");

		Search search = new Search();
		search.onFreeText(tag.trim());
		// TODO Try/catch handle CommException

		searchResults = new ArrayList<Object>();
		searchResults.addAll(getConferenceServer().searchSessions(search));
		searchResults.addAll(getConferenceServer().searchAuthors(search));

		searchAdapter = new SearchResultAdapter(this, searchResults);
		searchAdapter.addType(Session.class, R.layout.session_item_small);
		searchAdapter.addType(Author.class, R.layout.author_item);
		ListView sessionList = (ListView) findViewById(R.id.searchResults);
		sessionList.setAdapter(searchAdapter);
		sessionList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int paramInt, long paramLong) {
				show(paramInt);
			}
		});

		// Specific for tags
		findViewById(R.id.searchBlock).setVisibility(View.GONE);
		findViewById(R.id.firstDivider).setVisibility(View.GONE);

		TextView view = (TextView) findViewById(R.id.searchTitle);
		view.setText(tag);
		
		super.onCreate(savedInstanceState);
	}

	private void show(int index) {
		Object selected = searchResults.get(index);
		if (selected instanceof Session) {
			Session session = (Session) selected;
			if (session.getDate() != null) {
				Intent intent = new Intent(this, CVSessionView.class);
				Conference conference = getConferenceServer().getConference(session.getDate());
				intent.putExtra(BaseActivity.IA_CONFERENCE, conference.getId());
				intent.putExtra(BaseActivity.IA_SESSION, session.getId());
				startActivity(intent);
			}
			return;
		}
		if (selected instanceof Author) {
			Author author = (Author) selected;
			Intent intent = new Intent(this, CVAuthor.class);
			intent.putExtra(BaseActivity.IA_AUTHOR, author.getUserId());
			startActivity(intent);
			return;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}