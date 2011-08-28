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

/**
 * <p>Shows the conferences of a specific year.</p>
 * <p>
 * The parameters used are:
 * <ul>
 * <li>IA_CONF_YEAR - Year to display (yyyy); defaults to the current year.
 * <li>IA_REDIRECT - Boolean indicating to progress directly to the upcoming conference. Must be enabled in preferences
 * </ul></p>
 * 
 * @author Michael
 */
public class CVConferences extends SwipeActivity {

	private int shownYear;

	/** 
	 * Called when the activity is first created.
	 * Builds up the screen and loads the conferences for a certain year (not refreshed until
	 * the activity is newly created.
	 */
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
		if (conference != null) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			Intent intent = null;
			if (sp.getBoolean(XCS.PREF.SESSIONLIST, true)) {
				intent = new Intent(this, CVSessionList.class);
			} else {
				intent = new Intent(this, CVSessionView.class);
			}
			intent.putExtra(BaseActivity.IA_CONFERENCE, conference.getId());
			startActivity(intent);
		}
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
		intent.putExtra(IA_CONF_YEAR, shownYear - 1);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_right, R.anim.slide_left);
	}

	@Override
	public void onSwipeRightToLeft() {
		Intent intent = getIntent();
		intent.putExtra(IA_REDIRECT, false);
		intent.putExtra(IA_CONF_YEAR, shownYear + 1);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
	}

	@Override
	public void onSwipeTopToBottom() {}

	@Override
	public void onSwipeBottomToTop() {}
}