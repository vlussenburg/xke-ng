package com.xebia.xcoss.axcv;

import hirondelle.date4j.DateTime;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.ui.ConferenceAdapter;
import com.xebia.xcoss.axcv.util.XCS;

public class CVConferences extends SwipeActivity {

	private int shownYear;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.conferences);
		addGestureDetection(R.id.conferencesSwipeBase);
		super.onCreate(savedInstanceState);

		shownYear = getIntent().getIntExtra(IA_CONF_YEAR, DateTime.today(XCS.TZ).getYear());

		List<Conference> list = getConferenceServer().getConferences(shownYear);
		final Conference[] conferences = list.toArray(new Conference[list.size()]);
		ConferenceAdapter adapter = new ConferenceAdapter(this, R.layout.conference_item, conferences);
		ListView conferencesList = (ListView) findViewById(R.id.conferencesList);
		conferencesList.setAdapter(adapter);
		conferencesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int paramInt, long paramLong) {
				// Adapter = listview, view = tablelayout.
				switchTo(conferences[paramInt]);
			}
		});

		// Check for redirection. Not the case if menu option is used.
		if (getIntent().getBooleanExtra(IA_REDIRECT, true)) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			if (sp.getBoolean(XCS.PREF.JUMPTOFIRST, true)) {
				Log.i(XCS.LOG.NAVIGATE, "Jumping to first upcomming conference.");
				switchTo(getConferenceServer().getUpcomingConference());
			}
		}
		
		TextView title = (TextView) findViewById(R.id.conferencesTitle);
		title.setText(title.getText() + " " + shownYear);
	}

	private void switchTo(Conference conference) {
		Intent intent = new Intent(this, CVSessionList.class);
		intent.putExtra(BaseActivity.IA_CONFERENCE, conference.getId());
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.removeItem(XCS.MENU.OVERVIEW);
		menu.removeItem(XCS.MENU.EDIT);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Add a conference
		if (item.getItemId() == XCS.MENU.ADD) {
			Intent intent = new Intent(this, CVConferenceAdd.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSwipeLeftToRight() {
		Intent intent = getIntent();
		intent.putExtra(IA_REDIRECT, false);
		intent.putExtra(IA_CONF_YEAR, shownYear-1);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_right, 0);
	}

	@Override
	public void onSwipeRightToLeft() {
		Intent intent = getIntent();
		intent.putExtra(IA_REDIRECT, false);
		intent.putExtra(IA_CONF_YEAR, shownYear+1);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_left, 0);
	}

	@Override
	public void onSwipeTopToBottom() {
	}

	@Override
	public void onSwipeBottomToTop() {
	}
}