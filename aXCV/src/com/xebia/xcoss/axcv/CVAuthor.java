package com.xebia.xcoss.axcv;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.ui.FormatUtil;
import com.xebia.xcoss.axcv.ui.SearchResultAdapter;
import com.xebia.xcoss.axcv.util.XCS;

public class CVAuthor extends BaseActivity {

	private List<Object> searchResults;
	private SearchResultAdapter searchAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.author);

		// TODO Author => null and getting author by ID.
		Author author = null;
		String authorId = getIntent().getExtras().getString(IA_AUTHOR);
		Author[] authors = getConferenceServer().getAllAuthors();
		for (int i = 0; i < authors.length; i++) {
			if (authorId.equals(authors[i].getUserId())) {
				Log.v("XCS", "Match (" + authorId + "):" + authors[i].getUserId());
				author = authors[i];
				break;
			}
			Log.v("XCS", "No match (" + authorId + "):" + authors[i].getUserId());
		}
		Search search = new Search();
		search.onAuthor(author);
		// TODO Try/catch handle CommException

		searchResults = new ArrayList<Object>();
		searchResults.addAll(getConferenceServer().searchSessions(search));

		searchAdapter = new SearchResultAdapter(this, searchResults);
		searchAdapter.addType(Session.class, R.layout.session_item_small);
		ListView sessionList = (ListView) findViewById(R.id.searchResults);
		sessionList.setAdapter(searchAdapter);
		sessionList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int paramInt, long paramLong) {
				show(paramInt);
			}
		});

//		TextView view = (TextView) findViewById(R.id.av_name);
//		view.setText(author.getName());
		TextView view = (TextView) findViewById(R.id.av_labels);
		HashSet<String> labels = new HashSet<String>();
		labels.addAll(getConferenceServer().getLabels(author));
		view.setText(FormatUtil.getList(labels));
		Linkify.addLinks(view, XCS.TAG.PATTERN, XCS.TAG.LINK);
		view.setFocusable(false);

		LayoutInflater inflater = getLayoutInflater();
		ViewGroup layout = (ViewGroup) findViewById(R.id.av_author_layout);
		View row = inflater.inflate(R.layout.author_item, layout);
		SearchResultAdapter.createAuthorView(row, author);

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}