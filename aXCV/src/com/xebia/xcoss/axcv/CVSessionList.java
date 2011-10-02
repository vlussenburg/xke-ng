package com.xebia.xcoss.axcv;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.ui.SessionAdapter;
import com.xebia.xcoss.axcv.util.XCS;


public class CVSessionList extends SwipeActivity {

	private Conference currentConference;
	private int currentLocation;
	private Session[] sessions;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.schedule);
		super.onCreate(savedInstanceState);
		addGestureDetection(R.id.scheduleSwipeBase);

		currentConference = getConference();
		currentLocation = getIntent().getExtras().getInt(IA_LOCATION_ID);

		TextView title = (TextView) findViewById(R.id.conferenceTitle);
		title.setText(currentConference.getTitle());

		TextView date = (TextView) findViewById(R.id.leftText);
		String val = new ScreenTimeUtil(this).getAbsoluteDate(currentConference.getDate());
		date.setText(val);

		ListView sessionList = (ListView) findViewById(R.id.sessionList);
		sessionList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int paramInt, long paramLong) {
				// Adapter = listview, view = tablelayout.
				switchTo(currentConference, paramInt);
			}
		});
	}

	@Override
	protected void onResume() {
		Location[] locations = currentConference.getLocations().toArray(new Location[0]);
		Location loc = locations[currentLocation];
		sessions = currentConference.getSessions(loc).toArray(new Session[0]);
		TextView location = (TextView) findViewById(R.id.rightText);
		location.setText(loc.getDescription());
		location.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onSwipeRightToLeft();
			}
		});

		SessionAdapter adapter = new SessionAdapter(this, R.layout.session_item, R.layout.mandatory_item, sessions);
		ListView sessionList = (ListView) findViewById(R.id.sessionList);
		sessionList.setAdapter(adapter);
		super.onResume();
	}

	private void switchTo(Conference conference, int sessionIndex) {
		Session session = sessions[sessionIndex];
		if (session.isBreak()) {
			// No navigation to this session
			return;
		}
		Intent intent = new Intent(this, CVSessionView.class);
		intent.putExtra(BaseActivity.IA_CONFERENCE, conference.getId());
		intent.putExtra(BaseActivity.IA_SESSION, session.getId());
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Add a session
		if (item.getItemId() == XCS.MENU.ADD) {
			Intent intent = new Intent(this, CVSessionAdd.class);
			intent.putExtra(BaseActivity.IA_CONFERENCE, currentConference.getId());
			startActivity(intent);
			return true;
		}
		// Edit the conference
		if (item.getItemId() == XCS.MENU.EDIT) {
			Intent intent = new Intent(this, CVConferenceAdd.class);
			intent.putExtra(BaseActivity.IA_CONFERENCE, currentConference.getId());
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSwipeLeftToRight() {
		int maxIndex = currentConference.getLocations().size() - 1;
		if (currentLocation == 0) {
			currentLocation = maxIndex;
		} else {
			currentLocation--;
		}
		Intent intent = getIntent();
		intent.putExtra(IA_CONFERENCE, currentConference.getId());
		intent.putExtra(IA_LOCATION_ID, currentLocation);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_right, R.anim.slide_left);
	}

	@Override
	public void onSwipeRightToLeft() {
		int maxIndex = currentConference.getLocations().size() - 1;
		if (currentLocation == maxIndex) {
			currentLocation = 0;
		} else {
			currentLocation++;
		}
		Intent intent = getIntent();
		intent.putExtra(IA_CONFERENCE, currentConference.getId());
		intent.putExtra(IA_LOCATION_ID, currentLocation);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
	}

	@Override
	public void onSwipeTopToBottom() {}

	@Override
	public void onSwipeBottomToTop() {}
}