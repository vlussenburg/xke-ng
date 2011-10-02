package com.xebia.xcoss.axcv;

import java.util.HashSet;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
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
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class CVAuthor extends BaseActivity {

	private List<Session> searchResults;
	private SearchResultAdapter searchAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.author);

		Author author = findAuthor();
		Search search = new Search();
		search.onAuthor(author);

		searchResults = getConferenceServer().searchSessions(search);
		searchAdapter = new SearchResultAdapter(this, searchResults);
		ListView sessionList = (ListView) findViewById(R.id.searchResults);
		sessionList.setAdapter(searchAdapter);
		sessionList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int paramInt, long paramLong) {
				showSession(paramInt);
			}
		});

		// TextView view = (TextView) findViewById(R.id.av_name);
		// view.setText(author.getName());
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

	private Author findAuthor() {
		String authorId = getIntent().getExtras().getString(IA_AUTHOR);
		Author[] authors = getConferenceServer().getAllAuthors();
		if (StringUtil.isEmpty(authorId)) {
			// From link (which only contains the full name)
			String authorName = getIntent().getDataString().replace(XCS.AUTHOR.LINK, "").trim();
			for (int i = 0; i < authors.length; i++) {
				if (authorName.equals(authors[i].getName())) {
					return authors[i];
				}
			}
		} else {
			// From the id passed via the intent
			for (int i = 0; i < authors.length; i++) {
				if (authorId.equals(authors[i].getUserId())) {
					return authors[i];
				}
			}
		}
		return new Author("noauthor", "Author not found", "", "");
	}

	private void showSession(int index) {
		Session session = searchResults.get(index);
		if (session.getStartTime() != null) {
			Intent intent = new Intent(this, CVSessionView.class);
			Conference conference = getConferenceServer().getConference(session.getStartTime());
			intent.putExtra(BaseActivity.IA_CONFERENCE, conference.getId());
			intent.putExtra(BaseActivity.IA_SESSION, session.getId());
			startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}