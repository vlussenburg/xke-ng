package com.xebia.xcoss.axcv;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.xebia.xcoss.axcv.layout.SwipeLayout;
import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.tasks.SearchAuthorsTask;
import com.xebia.xcoss.axcv.tasks.SearchSessionsTask;
import com.xebia.xcoss.axcv.tasks.TaskCallBack;
import com.xebia.xcoss.axcv.ui.SearchResultAdapter;

public abstract class SearchActivity extends BaseActivity implements SwipeActivity {

	private SearchResultAdapter authorAdapter;
	private SearchResultAdapter sessionAdapter;
	private List<Author> authorResults;
	private List<Session> sessionResults;
	private ViewFlipper flipper;
	private ImageView flipButton;
	private ListView peopleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.search);
		((SwipeLayout) findViewById(R.id.swipeLayout)).setGestureListener(this);

		flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		peopleView = (ListView) findViewById(R.id.searchResultsPeople);
		flipButton = (ImageView) findViewById(R.id.flipSearchResult);

		flipButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				flip();
			}
		});

		searchSessions(null);
		searchAuthors(null);

		final TextView input = (TextView) findViewById(R.id.searchTerm);
		final ImageView searchButton = (ImageView) findViewById(R.id.searchAction);

		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				String text = input.getText().toString();
				input.setText("");
				searchSessions(text);
				searchAuthors(text);
			}
		});

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

	private void updateAuthors(List<Author> results) {
		this.authorResults = results;
		authorAdapter = new SearchResultAdapter(this, results);
		ListView sessionList = (ListView) findViewById(R.id.searchResultsPeople);
		sessionList.setAdapter(authorAdapter);
		sessionList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int paramInt, long paramLong) {
				showAuthor(paramInt);
			}
		});
		authorAdapter.notifyDataSetChanged();
	}

	private void updateSessions(List<Session> results) {
		this.sessionResults = results;
		sessionAdapter = new SearchResultAdapter(this, results);
		ListView sessionList = (ListView) findViewById(R.id.searchResultsSessions);
		sessionList.setAdapter(sessionAdapter);
		sessionList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int paramInt, long paramLong) {
				showSession(paramInt);
			}
		});
		sessionAdapter.notifyDataSetChanged();
	}

	private void showSession(int index) {
		if (index < sessionResults.size()) {
			Session session = sessionResults.get(index);
			if (session.getStartTime() != null) {
				Intent intent = new Intent(this, CVSessionView.class);
//				Conference conference = getConferenceServer().getConference(session.getStartTime());
//				intent.putExtra(BaseActivity.IA_CONFERENCE, conference.getId());
				intent.putExtra(BaseActivity.IA_SESSION, session.getId());
				startActivity(intent);
			}
		}
	}

	private void showAuthor(int index) {
		if (index < authorResults.size()) {
			Author author = authorResults.get(index);
			Intent intent = new Intent(this, CVAuthor.class);
			intent.putExtra(BaseActivity.IA_AUTHOR, author.getUserId());
			startActivity(intent);
		}
	}

	protected void searchAuthors(String text) {
		new SearchAuthorsTask(R.string.action_search_authors, this, new TaskCallBack<List<Author>>() {
			@Override
			public void onCalled(List<Author> result) {
				updateAuthors(result);
			}
		}).execute(new Search().onFreeText(text));
	}

	protected void searchSessions(String text) {
		new SearchSessionsTask(R.string.action_search_sessions, this, new TaskCallBack<List<Session>>() {
			@Override
			public void onCalled(List<Session> result) {
				updateSessions(result);
			}
		}).execute(new Search().onFreeText(text));
	}

	private void flip() {
		if (flipper.getCurrentView() == peopleView) {
			flipButton.setImageResource(R.drawable.ic_menu_people);
		} else {
			flipButton.setImageResource(R.drawable.ic_menu_sessions);
		}
		flipper.showNext();
	}

	@Override
	public void onSwipeLeftToRight() {
		flip();
	}

	@Override
	public void onSwipeRightToLeft() {
		flip();
	}

	@Override
	public void onSwipeBottomToTop() {
		// No action required
	}

	@Override
	public void onSwipeTopToBottom() {
		// No action required
	}
}
