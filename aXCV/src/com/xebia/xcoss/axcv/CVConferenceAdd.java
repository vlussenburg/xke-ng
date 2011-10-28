package com.xebia.xcoss.axcv;

import hirondelle.date4j.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

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
import android.widget.Toast;

import com.xebia.xcoss.axcv.logic.CommException;
import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Conference.TimeSlot;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.ui.AddBreakDialog;
import com.xebia.xcoss.axcv.ui.FormatUtil;
import com.xebia.xcoss.axcv.ui.LocationInputDialog;
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
	private List<Session> breakSessions;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.add_conference);
		super.onCreate(savedInstanceState);

		this.timeFormatter = new ScreenTimeUtil(this);
		this.originalConference = getConference(false);
		this.breakSessions = new ArrayList<Session>();
		
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

			List<Session> breakSessions = findBreakSessions();
			StringBuilder sb = new StringBuilder();
			String NL = System.getProperty("line.separator");
			for (Session s : breakSessions) {
				if (sb.length() > 0) {
					sb.append(NL);
				}
				sb.append(s.getTitle());
				sb.append(" [").append(s.getLocation());
				sb.append(" @ ").append(timeFormatter.getAbsoluteTime(s.getStartTime()));
				sb.append("]");
			}
			if (sb.length() == 0) {
				sb.append(FormatUtil.NONE_FOUND);
			}
			view = (TextView) findViewById(R.id.breakTime);
			view.setText(sb.toString());
		}
	}

	private List<Session> findBreakSessions() {
		ArrayList<Session> breaks = new ArrayList<Session>();
		for (Session s : conference.getSessions()) {
			if (s.isBreak()) {
				breaks.add(s);
			}
		}
		breaks.addAll(breakSessions);
		return breaks;
	}

	private void registerActions() {
		int[] identifiers = new int[] { R.id.conferenceName, R.id.conferenceDate, R.id.conferenceStart,
				R.id.conferenceEnd, R.id.conferenceDescription, R.id.conferenceOrganiser, R.id.conferenceLocations,
				R.id.breakTime };

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
					conference = Conference.create(conference);
				} else {
					conference.update();
				}
				for (Session s : breakSessions) {
					try {
						conference.addSession(s, true);
					} catch (CommException e) {
						Log.e(XCS.LOG.COMMUNICATE, "Cannot add break session " + s);
					}
				}
				CVConferenceAdd.this.finish();
			}
		});
		button = (Button) findViewById(R.id.actionDelete);
		if (create || conference.getDate().isInThePast(XCS.TZ)) {
			button.setVisibility(View.GONE);
		} else {
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View paramView) {
					int size = conference.getSessions().size();
					StringBuilder message = new StringBuilder();
					String NEWLINE = System.getProperty("line.separator");
					if (size > 0) {
						message.append("Warning!");
						message.append(NEWLINE);
						message.append("This conference has ").append(size).append(" session(s).");
						message.append(NEWLINE);
					}
					message.append("Are you sure to delete conference '").append(conference.getTitle()).append("'");
					message.append(" on ");
					message.append(timeFormatter.getAbsoluteDate(conference.getDate()));
					message.append("?");

					AlertDialog.Builder builder = new AlertDialog.Builder(CVConferenceAdd.this);
					builder.setTitle("Delete conference");
					builder.setMessage(message.toString());
					builder.setIcon(android.R.drawable.ic_dialog_alert);
					builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
					builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							conference.delete();
							CVConferenceAdd.this.finish();
						}
					});
					if (size > 0) {
						builder.setNeutralButton("Move & delete", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Conference nextConference = getConferenceServer().getUpcomingConference(
										conference.getDate().plusDays(1));
								Set<Session> sessions = conference.getSessions();
								for (Session s : sessions) {
									SortedSet<TimeSlot> slots = nextConference.getAvailableTimeSlots(s.getDuration());
									if (slots.isEmpty()) {
										dialog.dismiss();
										Toast.makeText(CVConferenceAdd.this, "Failed! Session cannot be moved.",
												Toast.LENGTH_LONG);
										return;
									}
									s.reschedule(nextConference, slots.first());
									if (!nextConference.addSession(s, false)) {
										dialog.dismiss();
										Toast.makeText(CVConferenceAdd.this, "Failed! Session cannot be moved.",
												Toast.LENGTH_LONG);
										return;
									}
								}
								conference.delete();
								CVConferenceAdd.this.finish();
							}
						});
					}
					AlertDialog alertDialog = builder.create();
					alertDialog.show();
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
				conference.updateStartTime((DateTime) selection);
				conference.updateEndTime((DateTime) selection);
			break;
			case R.id.conferenceStart:
				conference.updateStartTime((DateTime) selection);
			break;
			case R.id.conferenceEnd:
				conference.updateEndTime((DateTime) selection);
			break;
			case R.id.conferenceDescription:
				conference.setDescription(value);
			break;
			case R.id.conferenceOrganiser:
				conference.setOrganiser((Author) selection);
			break;
			case R.id.conferenceLocText:
				if (!StringUtil.isEmpty(value)) {
					if (selection instanceof Location) {
						getConferenceServer().createLocation((Location) selection);
					}
				}
				// fallThrough
			case R.id.conferenceLocations:
				if (ADD_NEW_LOCATION.equals(value)) {
					showDialog(XCS.DIALOG.CREATE_LOCATION);
					removeDialog(XCS.DIALOG.INPUT_LOCATION);
					return;
				}

				if (checked) {
					// Add the location
					Location[] locations = getConferenceServer().getLocations();
					for (int i = 0; i < locations.length; i++) {
						if (locations[i].getDescription().equals(value)) {
							conference.addLocation(locations[i]);
							break;
						}
					}
				} else {
					Iterable<Location> locations = conference.getLocations();
					for (Location location : locations) {
						if (location.getDescription().equals(value)) {
							conference.removeLocation(location);
							break;
						}
					}
				}
			break;
			case R.id.breakTime:
				if ( selection instanceof Session ){
					breakSessions.add((Session) selection);
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
				dialog = new LocationInputDialog(this, R.id.conferenceLocText);
			break;
			case XCS.DIALOG.INPUT_LOCATION:
				Location[] locations = getConferenceServer().getLocations();
				int size = locations.length + 1;

				items = new String[size];
				boolean[] check = new boolean[size];

				for (int i = 0; i < size - 1; i++) {
					items[i] = locations[i].getDescription();
					check[i] = false;
				}
				items[size - 1] = ADD_NEW_LOCATION;

				builder = new AlertDialog.Builder(this);
				builder.setTitle("Select locations");
				builder.setMultiChoiceItems(items, check, new DialogHandler(this, items, R.id.conferenceLocations));
				dialog = builder.create();
			break;
			case XCS.DIALOG.CREATE_BREAK:
				if (conference.getLocations().size() == 0 || conference.getDate() == null) {
					createDialog("Conference detail", "No locations and/or date specified yet").show();
				} else {
					dialog = new AddBreakDialog(this, R.id.breakTime);
				}
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
			case XCS.DIALOG.CREATE_BREAK:
				AddBreakDialog abd = (AddBreakDialog) dialog;
				abd.setConference(conference);
			break;
			case XCS.DIALOG.INPUT_DESCRIPTION:
				tid = (TextInputDialog) dialog;
				tid.setDescription("Description");
				tid.setValue(conference.getDescription());
			break;
			case XCS.DIALOG.INPUT_LOCATION:
				AlertDialog ad = (AlertDialog) dialog;
				Iterable<Location> locations = conference.getLocations();
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
			case R.id.breakTime:
				showDialog(XCS.DIALOG.CREATE_BREAK);
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
	public void onDismiss(DialogInterface di) {}

	@Override
	public void onCancel(DialogInterface di) {}
}