package com.xebia.xcoss.axcv;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.tasks.RetrieveLabelPerAuthorTask;
import com.xebia.xcoss.axcv.tasks.SearchAuthorsTask;
import com.xebia.xcoss.axcv.tasks.SearchSessionsTask;
import com.xebia.xcoss.axcv.tasks.TaskCallBack;
import com.xebia.xcoss.axcv.ui.SearchResultAdapter;
import com.xebia.xcoss.axcv.util.FormatUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class CVAuthor extends BaseActivity {

	private SearchResultAdapter searchAdapter;
	private Author author;
	private SearchAuthorsTask saTask;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.author);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		TaskCallBack<List<Author>> authorListCallback = new TaskCallBack<List<Author>>() {
			@Override
			public void onCalled(List<Author> result) {
				updateAuthor(result);
			}
		};
		saTask = new SearchAuthorsTask(R.string.action_search_authors, this, authorListCallback);
		String id = getIntent().getExtras().getString(IA_AUTHOR);
		String name = getIntent().getDataString().replace(XCS.AUTHOR.LINK, "").trim();
		Author author = new Author(id, name, null, null);
		saTask.execute(new Search().onAuthor(author));

		super.onResume();
	}

	private void updateAuthor(List<Author> authors) {
		if (authors.size() > 0) {
			author = authors.get(0);

			new SearchSessionsTask(R.string.action_search_sessions, this, new TaskCallBack<List<Session>>() {
				@Override
				public void onCalled(List<Session> result) {
					updateSessions(result);
				}
			}).execute(new Search().onAuthor(author));

			new RetrieveLabelPerAuthorTask(R.string.action_retrieve_labels, this, new TaskCallBack<List<String>>() {
				@Override
				public void onCalled(List<String> result) {
					updateLabels(result);
				}
			}).execute(author);

		} else {
			Log.w(XCS.LOG.DATA, "No author found!");
			author = new Author("noauthor", "Author not found", "", "");
		}
		LayoutInflater inflater = getLayoutInflater();
		ViewGroup layout = (ViewGroup) findViewById(R.id.av_author_layout);
		View row = inflater.inflate(R.layout.author_item, layout);
		SearchResultAdapter.createAuthorView(row, author);
	}

	private void updateSessions(List<Session> searchResults) {
		if (searchResults != null) {
			searchAdapter = new SearchResultAdapter(this, searchResults);
			ListView sessionList = (ListView) findViewById(R.id.searchResults);
			sessionList.setAdapter(searchAdapter);
			sessionList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapter, View view, int paramInt, long paramLong) {
					Object item = searchAdapter.getItem(paramInt);
					if (item instanceof Session) {
						Session session = (Session) item;
						if (session.getStartTime() != null) {
							Intent intent = new Intent(CVAuthor.this, CVSessionView.class);
							intent.putExtra(BaseActivity.IA_SESSION, session.getId());
							startActivity(intent);
						}
					}
				}
			});
		}
	}

	private void updateLabels(List<String> labels) {
		TextView view = (TextView) findViewById(R.id.av_labels);
		String list = FormatUtil.getList(labels, true);
		view.setText(list);
		view.setFocusable(false);

		if (!list.equals(FormatUtil.NONE_FOUND)) {
			Linkify.addLinks(view, XCS.TAG.PATTERN, XCS.TAG.LINK);
		}
	}
}