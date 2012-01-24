package com.xebia.xcoss.axcv;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.xebia.xcoss.axcv.layout.SwipeLayout;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.ui.SessionCMAdapter;
import com.xebia.xcoss.axcv.util.XCS;

public class CVSessionList extends SessionSwipeActivity {

	private Session[] sessions;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.schedule);
		super.onCreate(savedInstanceState);
		((SwipeLayout) findViewById(R.id.swipeLayout)).setGestureListener(this);

		Conference conference = getCurrentConference();

		TextView title = (TextView) findViewById(R.id.conferenceTitle);
		title.setText(conference.getTitle());

		TextView date = (TextView) findViewById(R.id.conferenceDate);
		ScreenTimeUtil timeUtil = new ScreenTimeUtil(this);
		String val = timeUtil.getAbsoluteDate(conference.getStartTime());
		date.setText(val);
	}

	@Override
	protected void onResume() {
		sessions = getCurrentConference().getSessions(getCurrentLocation()).toArray(new Session[0]);
		SessionCMAdapter adapter = new SessionCMAdapter(this, R.layout.session_item, R.layout.mandatory_item, sessions);
		ListView sessionList = (ListView) findViewById(R.id.sessionList);
		sessionList.setAdapter(adapter);

		updateLocations();
		super.onResume();
	}

	public void switchTo(int paramInt) {
		switchTo(getCurrentConference(), paramInt);
	}

	private void switchTo(Conference conference, int sessionIndex) {
		Session session = sessions[sessionIndex];
		Intent intent = new Intent(this, CVSessionView.class);
		intent.putExtra(BaseActivity.IA_CONFERENCE, conference.getId());
		intent.putExtra(BaseActivity.IA_SESSION, session.getId());
		startActivity(intent);
	}

	@Override
	protected void populateMenuOptions(ArrayList<Integer> list) {
		list.add(XCS.MENU.ADD);
		list.add(XCS.MENU.EDIT);
		list.add(XCS.MENU.SETTINGS);
		list.add(XCS.MENU.SEARCH);
		list.add(XCS.MENU.TRACK);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Add a session
		if (item.getItemId() == XCS.MENU.ADD) {
			Intent intent = new Intent(this, CVSessionAdd.class);
			intent.putExtra(BaseActivity.IA_CONFERENCE, getCurrentConference().getId());
			startActivity(intent);
			return true;
		}
		// Edit the conference
		if (item.getItemId() == XCS.MENU.EDIT) {
			Intent intent = new Intent(this, CVConferenceAdd.class);
			intent.putExtra(BaseActivity.IA_CONFERENCE, getCurrentConference().getId());
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		int position = menuItem.getGroupId();
		
		switch (menuItem.getItemId()) {
			case R.id.view:
				switchTo(getCurrentConference(), position);
				return true;
			case R.id.edit:
				Intent intent = new Intent(this, CVSessionAdd.class);
				intent.putExtra(BaseActivity.IA_CONFERENCE, getCurrentConference().getId());
				intent.putExtra(BaseActivity.IA_SESSION, sessions[position].getId());
				startActivity(intent);
				return true;
			default:
				return super.onContextItemSelected(menuItem);
		}		
	}
}