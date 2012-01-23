package com.xebia.xcoss.axcv;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.xebia.xcoss.axcv.layout.SwipeLayout;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.tasks.ConferencesPerYearTask;
import com.xebia.xcoss.axcv.ui.ConferenceAdapter;
import com.xebia.xcoss.axcv.util.XCS;

/**
 * <p>
 * Shows the conferences of a specific year.
 * </p>
 * <p>
 * The parameters used are:
 * <ul>
 * <li>IA_CONF_YEAR - Year to display (yyyy); defaults to the current year.
 * <li>IA_REDIRECT - Boolean indicating to progress directly to the upcoming conference. Must be enabled in preferences
 * </ul>
 * </p>
 * 
 * @author Michael
 */
public class CVConferences extends BaseActivity implements SwipeActivity {

	private int shownYear;
	private Conference[] conferences;
	private ConferencesPerYearTask cpyTask;

	/**
	 * Called when the activity is first created.
	 * Builds up the screen and loads the conferences for a certain year (not refreshed until
	 * the activity is newly created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.conferences);
		super.onCreate(savedInstanceState);
		((SwipeLayout) findViewById(R.id.swipeLayout)).setGestureListener(this);

		shownYear = getIntent().getIntExtra(IA_CONF_YEAR, new Moment().getYear());

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

	@Override
	protected void onResume() {
		cpyTask = new ConferencesPerYearTask(R.string.action_list_conferences, this);
		cpyTask.execute(shownYear);
		super.onResume();
	}
	
	@Override
	public void notifyTaskFinished() {
		List<Conference> list = cpyTask.getResult();
		Conference upcomingConference = getConferenceServer().getUpcomingConference();
		int position = 0;
		int idx = 0;
		conferences = new Conference[list.size()];
		for (Conference conference : list) {
			conferences[idx] = conference;
			if (conference.equals(upcomingConference)) {
				position = idx;
			}
			idx++;
		}
		ConferenceAdapter adapter = new ConferenceAdapter(this, R.layout.conference_item, conferences);
		ListView conferencesList = (ListView) findViewById(R.id.conferencesList);
		conferencesList.setAdapter(adapter);
		conferencesList.setSelection(position);
	}

	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		int position = menuItem.getGroupId();
		switch (menuItem.getItemId()) {
			case R.id.view:
				switchTo(position);
				return true;
			case R.id.edit:
				Intent intent = new Intent(this, CVConferenceAdd.class);
				intent.putExtra(BaseActivity.IA_CONFERENCE, conferences[position].getId());
				startActivity(intent);
				return true;
		}
		return super.onContextItemSelected(menuItem);
	}

	public void switchTo(int index) {
		if (index >= 0 && index < conferences.length) {
			switchTo(conferences[index]);
		}
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
		overridePendingTransition(R.anim.slide_right, 0);
	}

	@Override
	public void onSwipeRightToLeft() {
		Intent intent = getIntent();
		intent.putExtra(IA_REDIRECT, false);
		intent.putExtra(IA_CONF_YEAR, shownYear + 1);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_left, 0);
	}

	@Override
	public void onSwipeTopToBottom() {}

	@Override
	public void onSwipeBottomToTop() {}
}