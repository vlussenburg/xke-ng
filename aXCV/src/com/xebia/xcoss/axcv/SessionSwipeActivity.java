package com.xebia.xcoss.axcv;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Location;

import android.content.Intent;
import android.os.Bundle;

public abstract class SessionSwipeActivity extends SwipeActivity {

	private Conference currentConference;
	private Location[] locations;
	private int currentLocation;

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
		return locations[currentLocation];
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
		overridePendingTransition(R.anim.slide_right, 0);
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
		overridePendingTransition(R.anim.slide_left, 0);
	}

	@Override
	public void onSwipeTopToBottom() {}

	@Override
	public void onSwipeBottomToTop() {}
}
