package com.xebia.xcoss.axcv;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.xebia.xcoss.axcv.layout.SwipeLayout;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.tasks.RetrieveConferenceTask;
import com.xebia.xcoss.axcv.tasks.SimpleCallBack;
import com.xebia.xcoss.axcv.tasks.TaskCallBack;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.ui.SessionCMAdapter;
import com.xebia.xcoss.axcv.util.FormatUtil;
import com.xebia.xcoss.axcv.util.XCS;

/**
 * IA_CONFERENCE_ID - ID of selected conference (required by parent).
 * IA_LOCATION_ID - ID of selected location (optional by parent).
 * 
 * @author Michael
 */

public class CVSessionList extends SessionSwipeActivity {

	private Session[] sessions;
	private Conference conference;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.schedule);
		super.onCreate(savedInstanceState);
		((SwipeLayout) findViewById(R.id.swipeLayout)).setGestureListener(this);
	}

	@Override
	protected void onResume() {
		refreshScreen();
		super.onResume();
	}

	private void refreshScreen() {
		new RetrieveConferenceTask(R.string.action_retrieve_conference, this, new TaskCallBack<Conference>() {
			@Override
			public void onCalled(Conference cc) {
				if (cc != null) {
					conference = cc;
					updateLocations(conference);
					TextView title = (TextView) findViewById(R.id.conferenceTitle);
					title.setText(cc.getTitle());

					TextView date = (TextView) findViewById(R.id.conferenceDate);
					ScreenTimeUtil timeUtil = new ScreenTimeUtil(CVSessionList.this);
					String val = timeUtil.getAbsoluteDate(cc.getStartTime());
					date.setText(val);

					sessions = cc.getSessions(getCurrentLocation()).toArray(new Session[0]);
					SessionCMAdapter adapter = new SessionCMAdapter(CVSessionList.this, R.layout.session_item,
							R.layout.mandatory_item, sessions);
					ListView sessionList = (ListView) findViewById(R.id.sessionList);
					sessionList.setAdapter(adapter);

					updateLocationNavigation();

					TextView sessionLocation = ((TextView) findViewById(R.id.sessionLocation));
					sessionLocation.setText(getCurrentLocation().getDescription());
				} else {
					// TODO The CVTask currently shows a dialog, which will leak when finishing...
					finish();
				}
			}
		}).execute(getConferenceId());
	}
	
	public void switchTo(int paramInt) {
		switchTo(getConferenceId(), paramInt);
	}

	private void switchTo(String conferenceId, int sessionIndex) {
		Session session = sessions[sessionIndex];
		Intent intent = new Intent(this, CVSessionView.class);
		intent.putExtra(BaseActivity.IA_CONFERENCE_ID, conferenceId);
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
			intent.putExtra(BaseActivity.IA_CONFERENCE_ID, getConferenceId());
			startActivity(intent);
			return true;
		}
		// Edit the conference
		if (item.getItemId() == XCS.MENU.EDIT) {
			Intent intent = new Intent(this, CVConferenceAdd.class);
			intent.putExtra(BaseActivity.IA_CONFERENCE_ID, getConferenceId());
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
				switchTo(getConferenceId(), position);
				return true;
			case R.id.edit:
				Intent intent = new Intent(this, CVSessionAdd.class);
				intent.putExtra(BaseActivity.IA_CONFERENCE_ID, getConferenceId());
				intent.putExtra(BaseActivity.IA_SESSION, sessions[position].getId());
				startActivity(intent);
				return true;
			case R.id.delete:
				CVSessionAdd.createDeleteDialog(this, sessions[position], new SimpleCallBack() {
					@Override
					public void onCalled(Boolean result) {
						getMyApplication().getCache().remove(conference);
						refreshScreen();
					}
				}).show();
				return true;
		}
		return super.onContextItemSelected(menuItem);
	}
}