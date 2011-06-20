package com.xebia.xcoss.axcv;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.ui.SearchResultAdapter;
import com.xebia.xcoss.axcv.ui.StringUtil;

public class CVSearch extends BaseActivity {

	private List<Object> searchResults;
	private SearchResultAdapter searchAdapter;
	private ImageView searchButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.search);

		searchResults = new ArrayList<Object>();

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

		searchButton = (ImageView) findViewById(R.id.searchAction);
		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				searchResults.clear();
				String text = ((TextView) findViewById(R.id.searchTerm)).getText().toString();
				if (!StringUtil.isEmpty(text)) {
					Search search = new Search();
					search.onFreeText(text.trim());
					// TODO Try/catch handle CommException

					searchResults.addAll(getConferenceServer().searchSessions(search));
					searchResults.addAll(getConferenceServer().searchAuthors(search));
				}
				searchAdapter.notifyDataSetChanged();
			}
		});

		TextView input = (TextView) findViewById(R.id.searchTerm);
		input.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					searchButton.performClick();
					return true;
				}
				return false;
			}

		});
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
			// Intent intent = new Intent(this, CVAuthorView.class);
			// intent.putExtra(BaseActivity.IA_AUTHOR, author.getUserId());
			// startActivity(intent);
			return;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}