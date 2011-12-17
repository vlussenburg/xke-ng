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

public abstract class SessionSwipeActivity extends BaseActivity implements SwipeActivity {

	private Conference currentConference;
	private Location[] locations;
	protected int currentLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentConference = getConference();
		currentLocation = getIntent().getExtras().getInt(IA_LOCATION_ID);
		locations = currentConference.getLocations().toArray(new Location[0]);
		if (currentLocation < 0 || currentLocation > (locations.length - 1)) {
			currentLocation = 0;
		}
	}

	protected Location getCurrentLocation() {
		return locations.length > 0 ? locations[currentLocation] : null;
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
		return (next < locations.length ? locations[next] : null);
	}

	protected Location getPreviousLocation() {
		int prev = currentLocation - 1;
		return (prev >= 0 ? locations[prev] : null);
	}

	public Conference getCurrentConference() {
		return currentConference;
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
