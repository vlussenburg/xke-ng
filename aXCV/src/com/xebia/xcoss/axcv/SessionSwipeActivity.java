package com.xebia.xcoss.axcv;

import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS;

/**
 * IA_CONFERENCE_ID - ID of selected conference (required).
 * IA_LOCATION_ID - ID of selected location (optional).
 * 
 * @author Michael
 */

public abstract class SessionSwipeActivity extends BaseActivity implements SwipeActivity {

	private String currentConferenceId;
	private Location[] locations;
	protected int currentLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentConferenceId = (String) getIntent().getExtras().get(IA_CONFERENCE_ID);
		currentLocation = getIntent().getExtras().getInt(IA_LOCATION_ID);
		locations = new Location[0];

		TextView location = (TextView) findViewById(R.id.nextLocationText);
		location.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				showDialog(XCS.DIALOG.INPUT_LOCATION);
				return true;
			}
		});
		location = (TextView) findViewById(R.id.prevLocationText);
		location.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				showDialog(XCS.DIALOG.INPUT_LOCATION);
				return true;
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == XCS.DIALOG.INPUT_LOCATION) {
			Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.select_location);
			ListAdapter la = new ArrayAdapter<Location>(this, R.layout.simple_list_item, locations);
			builder.setSingleChoiceItems(la, -1, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int i) {
					dialog.dismiss();
					Intent intent = getIntent();
					intent.putExtra(IA_CONFERENCE_ID, currentConferenceId);
					intent.putExtra(IA_LOCATION_ID, i);
					intent.removeExtra(IA_SESSION);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					overridePendingTransition(i > currentLocation ? R.anim.slide_left : R.anim.slide_right, 0);
				}
			});
			return builder.create();
		}
		return super.onCreateDialog(id);
	}

	protected Location getCurrentLocation() {
		if (locations != null && locations.length > currentLocation) {
			return locations[currentLocation];
		}
		return null;
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
			intent.putExtra(IA_CONFERENCE_ID, currentConferenceId);
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
			intent.putExtra(IA_CONFERENCE_ID, currentConferenceId);
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

	protected void updateLocations(Conference cc) {
		List<Location> list = cc.getLocations();
		locations = list.toArray(new Location[list.size()]);
	}
	
	protected void updateCurrentLocation(Session session) {
		if (session != null && !session.isMandatory()) {
			for (int i = 0; i < locations.length; i++) {
				if (locations[i].equals(session.getLocation())) {
					currentLocation = i;
					break;
				}
			}
		}
	}
	
	protected void updateLocationNavigation() {
		Location previous = getPreviousLocation();
		Location next = getNextLocation();

//		StringBuilder sb = new StringBuilder();
//		if (locations != null) for (Location loc : locations)
//			sb.append(loc);
//
//		Log.i(XCS.LOG.NAVIGATE, "Update locations: 1. " + sb.toString());
//		Log.i(XCS.LOG.NAVIGATE, "Update locations: 2. " + getCurrentLocation());
//		Log.i(XCS.LOG.NAVIGATE, "Update locations: 3. " + previous);
//		Log.i(XCS.LOG.NAVIGATE, "Update locations: 4. " + next);

		TextView location = (TextView) findViewById(R.id.nextLocationText);
//		ViewGroup group = ((ViewGroup) findViewById(R.id.nextLocationLayout));
		if (next == null) {
			location.setVisibility(View.INVISIBLE);
		} else {
			location.setVisibility(View.VISIBLE);
			location.setText(next.getDescription());
			location.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onSwipeRightToLeft();
				}
			});
		}
		location = (TextView) findViewById(R.id.prevLocationText);
//		group = ((ViewGroup) findViewById(R.id.prevLocationLayout));
		if (previous == null) {
			location.setVisibility(View.INVISIBLE);
		} else {
			location.setVisibility(View.VISIBLE);
			location.setText(previous.getDescription());
			location.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onSwipeLeftToRight();
				}
			});
		}
	}

}
