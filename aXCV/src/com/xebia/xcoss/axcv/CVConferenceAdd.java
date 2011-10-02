package com.xebia.xcoss.axcv;

import hirondelle.date4j.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.ui.FormatUtil;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.ui.TextInputDialog;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class CVConferenceAdd extends AdditionActivity {

	private static final String ADD_NEW_LOCATION = "Add new...";
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
	 * @param checked
	 *            Indicates a set (true) or a reset (false)
	 */
	public void updateField(int field, Object selection, boolean checked) {
		String value = selection.toString();
		switch (field) {
			case R.id.conferenceName:
				conference.setTitle(value);
			break;
			case R.id.conferenceDate:
				conference.setDate((DateTime) selection);
			break;
			case R.id.conferenceStart:
				conference.setStartTime((DateTime) selection);
			break;
			case R.id.conferenceEnd:
				conference.setEndTime((DateTime) selection);
			break;
			case R.id.conferenceDescription:
				conference.setDescription(value);
			break;
			case R.id.conferenceOrganiser:
				conference.setOrganiser((Author) selection);
			break;
			case R.id.conferenceLocText:
				if (!StringUtil.isEmpty(value)) {
					getConferenceServer().createLocation(value);
				}
			// fallThrough
			case R.id.conferenceLocations:
				if (checked) {
					if (ADD_NEW_LOCATION.equals(value)) {
						showDialog(XCS.DIALOG.CREATE_LOCATION);
						removeDialog(XCS.DIALOG.INPUT_LOCATION);
						return;
					}
					// Add the location
					Location[] locations = getConferenceServer().getLocations();
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
				} else if (!ADD_NEW_LOCATION.equals(value)) {
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
		DateTime time;
		OnTimeSetListener timeSetListener;

		switch (id) {
			case XCS.DIALOG.INPUT_TITLE:
				dialog = new TextInputDialog(this, R.id.conferenceName);
			break;
			case XCS.DIALOG.INPUT_DESCRIPTION:
				dialog = new TextInputDialog(this, R.id.conferenceDescription);
			break;
			case XCS.DIALOG.CREATE_LOCATION:
				dialog = new TextInputDialog(this, R.id.conferenceLocText);
			break;
			case XCS.DIALOG.INPUT_LOCATION:
				Location[] locations = getConferenceServer().getLocations();
				int size = locations.length + 1;

				items = new String[size];
				boolean[] check = new boolean[size];
				
				for (int i = 0; i < size-1; i++) {
					items[i] = locations[i].getDescription();
					check[i] = false;
				}
				items[size-1] = ADD_NEW_LOCATION;

				builder = new AlertDialog.Builder(this);
				builder.setTitle("Select locations");
				builder.setMultiChoiceItems(items, check, new DialogHandler(this, items, R.id.conferenceLocations));
				dialog = builder.create();
			break;
			case XCS.DIALOG.INPUT_TIME_START:
				time = conference.getStartTime();
				if (time == null) {
					time = DateTime.forTimeOnly(9, 0, 0, 0);
				}
				timeSetListener = new OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker paramTimePicker, int h, int m) {
						updateField(R.id.conferenceStart, DateTime.forTimeOnly(h, m, 0, 0), true);
					}
				};
				dialog = new TimePickerDialog(this, timeSetListener, time.getHour(), time.getMinute(), true);
			break;
			case XCS.DIALOG.INPUT_TIME_END:
				time = conference.getEndTime();
				if (time == null) {
					time = DateTime.forTimeOnly(21, 0, 0, 0);
				}
				timeSetListener = new OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker tp, int h, int m) {
						updateField(R.id.conferenceEnd, DateTime.forTimeOnly(h, m, 0, 0), true);
					}
				};
				dialog = new TimePickerDialog(this, timeSetListener, time.getHour(), time.getMinute(), true);
			break;
			case XCS.DIALOG.INPUT_DATE:
				time = conference.getDate();
				if (time == null) {
					time = DateTime.today(XCS.TZ);
				}
				OnDateSetListener dateSetListener = new OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker paramDatePicker, int y, int m, int d) {
						updateField(R.id.conferenceDate, DateTime.forDateOnly(y, m + 1, d), true);
					}
				};
				dialog = new DatePickerDialog(this, dateSetListener, time.getYear(), time.getMonth() - 1, time.getDay());
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
			case XCS.DIALOG.CREATE_LOCATION:
				tid = (TextInputDialog) dialog;
				tid.setDescription("Location name");
				tid.setValue("");
				break;
			case XCS.DIALOG.INPUT_LOCATION:
				AlertDialog ad = (AlertDialog) dialog;
				Set<Location> locations = conference.getLocations();
				Set<String> locNames = new HashSet<String>();
				for (Location location : locations) {
					locNames.add(location.getDescription());
				}
				int size = ad.getListView().getCount();
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
			case R.id.conferenceStart:
				showDialog(XCS.DIALOG.INPUT_TIME_START);
			break;
			case R.id.conferenceEnd:
				showDialog(XCS.DIALOG.INPUT_TIME_END);
			break;
			case R.id.conferenceDate:
				showDialog(XCS.DIALOG.INPUT_DATE);
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
						updateField(R.id.conferenceOrganiser, selected, true);
					}
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