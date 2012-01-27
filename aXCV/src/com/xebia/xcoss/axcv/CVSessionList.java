package com.xebia.xcoss.axcv;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.xebia.xcoss.axcv.layout.SwipeLayout;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.tasks.RetrieveConferenceTask;
import com.xebia.xcoss.axcv.tasks.TaskCallBack;
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
	}

	@Override
	protected void onResume() {
		new RetrieveConferenceTask(R.string.action_retrieve_conference, this, new TaskCallBack<Conference>() {
			@Override
			public void onCalled(Conference cc) {
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

				updateLocations();
			}
		}).execute(getConferenceId());

		super.onResume();
	}

	public void switchTo(int paramInt) {
		switchTo(getConferenceId(), paramInt);
	}

	private void switchTo(String conferenceId, int sessionIndex) {
		Session session = sessions[sessionIndex];
		Intent intent = new Intent(this, CVSessionView.class);
		intent.putExtra(BaseActivity.IA_CONFERENCE, conferenceId);
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
			intent.putExtra(BaseActivity.IA_CONFERENCE, getConferenceId());
			startActivity(intent);
			return true;
		}
		// Edit the conference
		if (item.getItemId() == XCS.MENU.EDIT) {
			Intent intent = new Intent(this, CVConferenceAdd.class);
			intent.putExtra(BaseActivity.IA_CONFERENCE, getConferenceId());
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
				intent.putExtra(BaseActivity.IA_CONFERENCE, getConferenceId());
				intent.putExtra(BaseActivity.IA_SESSION, sessions[position].getId());
				startActivity(intent);
				return true;
			default:
				return super.onContextItemSelected(menuItem);
		}
	}
}