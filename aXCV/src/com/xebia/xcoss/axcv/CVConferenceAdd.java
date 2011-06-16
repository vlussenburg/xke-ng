package com.xebia.xcoss.axcv;

import hirondelle.date4j.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.ui.FormatUtil;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.ui.TextInputDialog;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class CVConferenceAdd extends AdditionActivity {

	private ScreenTimeUtil timeFormatter;
	private Conference originalConference;
	private Conference conference;
	private boolean create = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.add_conference);
		this.timeFormatter = new ScreenTimeUtil(this);

		originalConference = getConference(false);
		if (originalConference == null) {
			create = true;
			conference = new Conference();
			((TextView) findViewById(R.id.addModifyTitle)).setText("Add conference");
		} else {
			conference = new Conference(originalConference);
			((TextView) findViewById(R.id.addModifyTitle)).setText("Edit conference");
		}

		showConference();
		registerActions();
		super.onCreate(savedInstanceState);
	}

	private void showConference() {
		if (conference != null) {
			TextView view = (TextView) findViewById(R.id.conferenceName);
			view.setText(FormatUtil.getText(conference.getTitle()));

			DateTime date = conference.getDate();
			if (date != null) {
				view = (TextView) findViewById(R.id.conferenceDate);
				view.setText(timeFormatter.getAbsoluteDate(date));
			}
			view = (TextView) findViewById(R.id.conferenceStart);
			view.setText(timeFormatter.getAbsoluteTime(conference.getStartTime()));

			view = (TextView) findViewById(R.id.conferenceEnd);
			view.setText(timeFormatter.getAbsoluteTime(conference.getEndTime()));

			view = (TextView) findViewById(R.id.conferenceDescription);
			view.setText(FormatUtil.getText(conference.getDescription()));

			view = (TextView) findViewById(R.id.conferenceOrganiser);
			view.setText(FormatUtil.getText(conference.getOrganiser()));

			view = (TextView) findViewById(R.id.conferenceLocations);
			view.setText(FormatUtil.getList(conference.getLocations()));
		}
	}

	private void registerActions() {
		int[] identifiers = new int[] { R.id.conferenceName, R.id.conferenceDate, R.id.conferenceStart,
				R.id.conferenceEnd, R.id.conferenceDescription, R.id.conferenceOrganiser, R.id.conferenceLocations };

		activateViews(identifiers);

		Button button = (Button) findViewById(R.id.actionSave);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				List<String> messages = new ArrayList<String>();
				if (!conference.check(messages)) {
					createDialog("Failed", "Please specify the following attributes: " + FormatUtil.getText(messages))
							.show();
					return;
				}

				if (create) {
					Conference.create(conference);
				} else {
					conference.update();
				}
				CVConferenceAdd.this.finish();
			}
		});
		button = (Button) findViewById(R.id.actionDelete);
		if (create) {
			button.setVisibility(View.GONE);
		} else {
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View paramView) {
					// TODO Confirmation dialog
					conference.delete();
					CVConferenceAdd.this.finish();
				}
			});
		}
	}

	/**
	 * Takes the chosen attribute and converts this (String) value to a value for the object
	 * 
	 * @param field
	 *            Identifier for the attribute on screen
	 * @param value
	 *            The value the user selected
	 * @param state
	 *            Indicates a set (true) or a reset (false)
	 */
	public void updateField(int field, Object selection, boolean state) {
		String value = selection.toString();
		switch (field) {
			case R.id.conferenceName:
				conference.setTitle(value);
			break;
			// case R.id.conferenceDate:
			// conference.setDate(value);
			// break;
			// case R.id.conferenceStart:
			// conference.setStartTime(value);
			// break;
			// case R.id.conferenceEnd:
			// conference.setEndTime(value);
			// break;
			case R.id.conferenceDescription:
				conference.setDescription(value);
			break;
			// case R.id.conferenceOrganiser:
			// conference.setOrganiser(value);
			// break;
			case R.id.conferenceLocations:
				if (state) {
					// Add the location
					Location[] locations = getConferenceServer().getLocations(false);
					Location selected = null;
					for (int i = 0; i < locations.length; i++) {
						if (locations[i].getDescription().equals(value)) {
							selected = locations[i];
							break;
						}
					}
					if (selected != null) {
						conference.getLocations().add(selected);
					}
				} else {
					Location contained = null;
					Set<Location> locations = conference.getLocations();
					for (Location location : locations) {
						if (location.getDescription().equals(value)) {
							contained = location;
							break;
						}
					}
					if (contained != null) {
						locations.remove(contained);
					}
				}
			break;
			default:
				Log.w(LOG.NAVIGATE, "Don't know how to process: " + field);
		}
		showConference();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		String[] items;
		AlertDialog.Builder builder;

		switch (id) {
			case XCS.DIALOG.INPUT_TITLE:
				dialog = new TextInputDialog(this, R.id.conferenceName);
			break;
			case XCS.DIALOG.INPUT_DESCRIPTION:
				dialog = new TextInputDialog(this, R.id.conferenceDescription);
			break;
			case XCS.DIALOG.INPUT_LOCATION:
				Location[] locations = getConferenceServer().getLocations(false);
				items = new String[locations.length];
				boolean[] check = new boolean[locations.length];
				for (int i = 0; i < locations.length; i++) {
					items[i] = locations[i].getDescription();
				}
				builder = new AlertDialog.Builder(this);
				builder.setTitle("Select locations");
				builder.setMultiChoiceItems(items, check, new DialogHandler(this, items, R.id.conferenceLocations));
				dialog = builder.create();
			break;
		}
		if (dialog != null) {
			return dialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		TextInputDialog tid;

		switch (id) {
			case XCS.DIALOG.INPUT_TITLE:
				tid = (TextInputDialog) dialog;
				tid.setDescription("Conference title");
				tid.setValue(conference.getTitle());
			break;
			case XCS.DIALOG.INPUT_DESCRIPTION:
				tid = (TextInputDialog) dialog;
				tid.setDescription("Description");
				tid.setValue(conference.getDescription());
			break;
			case XCS.DIALOG.INPUT_LOCATION:
				AlertDialog ad = (AlertDialog) dialog;
				int size = ad.getListView().getCount();
				Set<Location> locations = conference.getLocations();
				Set<String> locNames = new HashSet<String>();
				for (Location location : locations) {
					locNames.add(location.getDescription());
				}
				for (int idx = 0; idx < size; idx++) {
					String loc = (String) ad.getListView().getItemAtPosition(idx);
					ad.getListView().setItemChecked(idx, locNames.contains(loc));
				}
			break;
		}
		super.onPrepareDialog(id, dialog);
	}

	protected void onTextClick(int id) {
		switch (id) {
			case R.id.conferenceName:
				showDialog(XCS.DIALOG.INPUT_TITLE);
			break;
			case R.id.conferenceDescription:
				showDialog(XCS.DIALOG.INPUT_DESCRIPTION);
			break;
			case R.id.conferenceLocations:
				showDialog(XCS.DIALOG.INPUT_LOCATION);
			break;
			case R.id.conferenceOrganiser:
				showAuthorPage();
			break;
			default:
				Log.w(XCS.LOG.NAVIGATE, "Click on text not handled: " + id);
			break;
		}
	}

	private void showAuthorPage() {
		Intent authorIntent = getAuthorIntent();
		ArrayList<Author> authorList = new ArrayList<Author>();
		Author organiser = conference.getOrganiser();
		if (organiser != null) authorList.add(organiser);
		authorIntent.putExtra(BaseActivity.IA_AUTHORS, (Serializable) authorList);
		authorIntent.putExtra("singleSelectionMode", true);
		startActivityForResult(authorIntent, ACTIVITY_SEARCH_AUTHOR);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case ACTIVITY_SEARCH_AUTHOR:
				if (data != null && data.hasExtra(IA_AUTHORS)) {
					Serializable extra = data.getSerializableExtra(IA_AUTHORS);
					for (Author selected : ((ArrayList<Author>) extra)) {
						conference.setOrganiser(selected);
					}
					showConference();
				}
			break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	@Override
	public void onDismiss(DialogInterface di) {
	}

	@Override
	public void onCancel(DialogInterface di) {
	}
}