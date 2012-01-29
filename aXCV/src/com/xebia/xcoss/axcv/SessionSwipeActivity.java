package com.xebia.xcoss.axcv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.tasks.RetrieveConferenceTask;
import com.xebia.xcoss.axcv.tasks.TaskCallBack;

public abstract class SessionSwipeActivity extends BaseActivity implements SwipeActivity {

	private String currentConferenceId;
	private Location[] locations;
	protected int currentLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentConferenceId = (String) getIntent().getExtras().get(IA_CONFERENCE);
		currentLocation = getIntent().getExtras().getInt(IA_LOCATION_ID);
		locations = new Location[0];

		new RetrieveConferenceTask(R.string.action_retrieve_conference, this, new TaskCallBack<Conference>() {
			@Override
			public void onCalled(Conference cc) {
				if (cc != null) {
					locations = cc.getLocations().toArray(new Location[0]);
					if (currentLocation < 0 || currentLocation > (locations.length - 1)) {
						currentLocation = 0;
					}
				}
			}
		}).execute(getConferenceId());
	}

	protected Location getCurrentLocation() {
		if (locations != null && locations.length > currentLocation) {
			return locations[currentLocation];
		}
		return null;
	}

	protected void updateLocation(Session session) {
		if (session != null && !session.isBreak()) {
			for (int i = 0; i < locations.length; i++) {
				if (locations[i].equals(session.getLocation())) {
					currentLocation = i;
					break;
				}
			}
		}
	}

	protected Location getNextLocation() {
		int next = currentLocation + 1;
		if (next < locations.length) {
			return locations[next];
		}
		return null;
	}

	protected Location getPreviousLocation() {
		int prev = currentLocation - 1;
		if (prev >= 0 && prev < locations.length) {
			return locations[prev];
		}
		return null;
	}

	public String getConferenceId() {
		return currentConferenceId;
	}

	@Override
	public void onSwipeLeftToRight() {
		Location location = getPreviousLocation();
		if (location != null) {
			currentLocation--;
			Intent intent = getIntent();
			// intent.putExtra(IA_CONFERENCE, currentConference.getId());
			intent.putExtra(IA_LOCATION_ID, currentLocation);
			intent.removeExtra(IA_SESSION);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_right, 0);
		}
	}

	@Override
	public void onSwipeRightToLeft() {
		Location location = getNextLocation();
		if (location != null) {
			currentLocation++;
			Intent intent = getIntent();
			// intent.putExtra(IA_CONFERENCE, currentConference.getId());
			intent.putExtra(IA_LOCATION_ID, currentLocation);
			intent.removeExtra(IA_SESSION);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_left, 0);
		}
	}

	@Override
	public void onSwipeTopToBottom() {}

	@Override
	public void onSwipeBottomToTop() {}

	protected void updateLocations() {
		Location loc = getNextLocation();
		TextView location = (TextView) findViewById(R.id.rightText);
		ViewGroup group = ((ViewGroup) findViewById(R.id.nextLocationLayout));
		if (loc == null) {
			group.setVisibility(View.INVISIBLE);
		} else {
			group.setVisibility(View.VISIBLE);
			location.setText(loc.getDescription());
			location.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onSwipeRightToLeft();
				}
			});
		}
		loc = getPreviousLocation();
		location = (TextView) findViewById(R.id.leftText);
		group = ((ViewGroup) findViewById(R.id.prevLocationLayout));
		if (loc == null) {
			group.setVisibility(View.INVISIBLE);
		} else {
			group.setVisibility(View.VISIBLE);
			location.setText(loc.getDescription());
			location.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onSwipeLeftToRight();
				}
			});
		}
	}

}
