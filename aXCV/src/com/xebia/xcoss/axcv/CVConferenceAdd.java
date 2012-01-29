package com.xebia.xcoss.axcv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.model.util.SessionComparator;
import com.xebia.xcoss.axcv.tasks.DeleteConferenceTask;
import com.xebia.xcoss.axcv.tasks.RegisterConferenceTask;
import com.xebia.xcoss.axcv.tasks.RegisterLocationTask;
import com.xebia.xcoss.axcv.tasks.RetrieveConferenceTask;
import com.xebia.xcoss.axcv.tasks.RetrieveLocationsTask;
import com.xebia.xcoss.axcv.tasks.SimpleCallBack;
import com.xebia.xcoss.axcv.tasks.TaskCallBack;
import com.xebia.xcoss.axcv.ui.AddBreakDialog;
import com.xebia.xcoss.axcv.ui.LocationInputDialog;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.ui.TextInputDialog;
import com.xebia.xcoss.axcv.util.FormatUtil;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class CVConferenceAdd extends AdditionActivity {

	private String ADD_NEW_LOCATION;

	private ScreenTimeUtil timeFormatter;
	private Conference originalConference;
	private Conference conference;
	private boolean create = true;
	private TreeSet<Session> breakSessions;
	private List<Location> locations;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.add_conference);
		super.onCreate(savedInstanceState);

		new RetrieveLocationsTask(R.string.action_retrieve_locations, this, new TaskCallBack<List<Location>>() {
			@Override
			public void onCalled(List<Location> result) {
				locations = result;
			}
		}).execute();

		ADD_NEW_LOCATION = getString(R.string.add_location);
		timeFormatter = new ScreenTimeUtil(this);
		breakSessions = new TreeSet<Session>(new SessionComparator());
		conference = new Conference();
		locations = new ArrayList<Location>();

		if (!loadFrom(savedInstanceState)) {
			new RetrieveConferenceTask(R.string.action_retrieve_conference, this, new TaskCallBack<Conference>() {
				@Override
				public void onCalled(Conference result) {
					originalConference = result;
					if (originalConference != null) {
						create = false;
						conference = new Conference(originalConference);
					}
					TextView tv = (TextView) findViewById(R.id.addModifyTitle);
					tv.setText(originalConference == null ? R.string.add_conference : R.string.edit_conference);
					if (create) { // TODO || conference.getStartTime().isBeforeNow()) {
						((Button) findViewById(R.id.actionDelete)).setVisibility(View.GONE);
					}
					showConference();
					registerActions();
				}
			}).execute(getSelectedConferenceId());
		}
	}

	private boolean loadFrom(Bundle savedInstanceState) {
		if (savedInstanceState != null && savedInstanceState.isEmpty() == false) {
			Log.w("debug", "Initialize from SIS");
			conference = (Conference) savedInstanceState.getSerializable("SIS_CONFERENCE");
			originalConference = (Conference) savedInstanceState.getSerializable("SIS_ORIGINAL_CONFERENCE");
			create = savedInstanceState.getBoolean("SIS_CREATE");
			breakSessions = (TreeSet<Session>) savedInstanceState.getSerializable("SIS_BREAKS");
			Log.w("debug", "Initialized from SIS: " + conference);
			return conference != null;
		}
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("SIS_CONFERENCE", conference);
		outState.putSerializable("SIS_ORIGINAL_CONFERENCE", originalConference);
		outState.putSerializable("SIS_BREAKS", breakSessions);
		outState.putBoolean("SIS_CREATE", create);
		super.onSaveInstanceState(outState);
	}

	private void showConference() {
		if (conference != null) {
			TextView view = (TextView) findViewById(R.id.conferenceName);
			view.setText(FormatUtil.getText(conference.getTitle()));

			Moment date = conference.getStartTime();
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
					String message = getString(R.string.specify_attributes, FormatUtil.getText(messages));
					createDialog(getString(R.string.failed), message).show();
					return;
				}

				RegisterConferenceTask rcTask = new RegisterConferenceTask(R.string.action_register_conference,
						CVConferenceAdd.this, new TaskCallBack<Conference>() {
							@Override
							public void onCalled(Conference result) {
								if (result != null) {
									CVConferenceAdd.this.finish();
								} else {
									Log.e(LOG.ALL, "Adding conference failed.");
									createDialog(getString(R.string.no_conference_added),
											getString(R.string.conference_could_not_be_added)).show();
								}
							}
						});
				rcTask.setSessions(breakSessions);
				rcTask.execute(conference);
			}
		});
		button = (Button) findViewById(R.id.actionDelete);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				createDeleteDialog(CVConferenceAdd.this, conference, new SimpleCallBack() {@Override
				public void onCalled(Boolean result) {
					finish();
				}}).show();
			}
		});
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
		final String value = selection.toString();
		Moment moment;
		switch (field) {
			case R.id.conferenceName:
				conference.setTitle(value);
			break;
			case R.id.conferenceDate:
				moment = (Moment) selection;
				conference.onStartTime().setDate(moment);
				conference.onEndTime().setDate(moment);
				for (Session session : conference.getSessions()) {
					session.onStartTime().setDate(moment);
					session.onEndTime().setDate(moment);
				}
			break;
			case R.id.conferenceStart:
				moment = (Moment) selection;
				// TODO : Check if there are sessions invalid
				conference.onStartTime().setTime(moment.getHour(), moment.getMinute());
			break;
			case R.id.conferenceEnd:
				moment = (Moment) selection;
				// TODO : Check if there are sessions invalid
				conference.onEndTime().setTime(moment.getHour(), moment.getMinute());
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
						new RegisterLocationTask(R.string.action_register_location, this).execute((Location) selection);
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
					for (Location location : locations) {
						if (location.getDescription().equals(value)) {
							conference.addLocation(location);
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
				if (selection instanceof Session) {
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
		Moment time;
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
				int size = locations.size() + 1;

				String[] items = new String[size];
				boolean[] check = new boolean[size];

				int i = 0;
				for (Location location : locations) {
					items[i] = location.getDescription();
					check[i] = false;
					i++;
				}
				items[size - 1] = ADD_NEW_LOCATION;

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.select_location);
				DialogHandler handler = new DialogHandler(this, items, R.id.conferenceLocations);
				handler.setCloseOnSelection(false);
				builder.setMultiChoiceItems(items, check, handler);
				builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialog = builder.create();
			break;
			case XCS.DIALOG.CREATE_BREAK:
				dialog = new AddBreakDialog(this, R.id.breakTime);
			break;
			case XCS.DIALOG.INPUT_TIME_START:
				time = conference.getStartTime();
				timeSetListener = new OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker paramTimePicker, int h, int m) {
						updateField(R.id.conferenceStart, new Moment(h, m), true);
					}
				};
				int hour = time == null ? 9 : time.getHour();
				int minute = time == null ? 0 : time.getMinute();
				dialog = new TimePickerDialog(this, timeSetListener, hour, minute, true);
			break;
			case XCS.DIALOG.INPUT_TIME_END:
				time = conference.getEndTime();
				if (time == null) {
					time = new Moment(21, 0);
				}
				timeSetListener = new OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker tp, int h, int m) {
						updateField(R.id.conferenceEnd, new Moment(h, m), true);
					}
				};
				dialog = new TimePickerDialog(this, timeSetListener, time.getHour(), time.getMinute(), true);
			break;
			case XCS.DIALOG.INPUT_DATE:
				time = conference.getStartTime();
				if (time == null) {
					time = new Moment();
				}
				OnDateSetListener dateSetListener = new OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker paramDatePicker, int y, int m, int d) {
						updateField(R.id.conferenceDate, new Moment(y, m + 1, d), true);
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
				tid.setDescription(getString(R.string.conference_title));
				tid.setValue(conference.getTitle());
			break;
			case XCS.DIALOG.CREATE_BREAK:
				if (dialog instanceof AddBreakDialog) {
					AddBreakDialog abd = (AddBreakDialog) dialog;
					abd.setConference(conference);
				}
			break;
			case XCS.DIALOG.INPUT_DESCRIPTION:
				tid = (TextInputDialog) dialog;
				tid.setDescription(getString(R.string.description));
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
				if (conference.getLocations().size() == 0 || conference.getStartTime() == null) {
					createDialog(getString(R.string.conference_detail), getString(R.string.no_location_date)).show();
				} else {
					showDialog(XCS.DIALOG.CREATE_BREAK);
				}
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

	protected static AlertDialog createDeleteDialog(final BaseActivity ctx, final Conference conference, final SimpleCallBack scb) {
		int size = conference.getSessions().size();
		StringBuilder message = new StringBuilder();
		String NEWLINE = System.getProperty("line.separator");
		if (size > 0) {
			message.append(ctx.getString(R.string.warning));
			message.append(NEWLINE);
			message.append(ctx.getString(R.string.conference_with_sessions, size));
			message.append(NEWLINE);
		}
		String time = new ScreenTimeUtil(ctx).getAbsoluteDate(conference.getStartTime());
		message.append(ctx.getString(R.string.confirm_delete_conference, conference.getTitle(), time));

		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(R.string.delete_conference);
		builder.setMessage(message.toString());
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				DeleteConferenceTask dcTask = new DeleteConferenceTask(R.string.action_delete_conference, ctx, null);
				dcTask.setMoveSessions(false);
				dcTask.execute(conference);
				dialog.dismiss();
				scb.onCalled(true);
			}
		});
		if (size > 0) {
			builder.setNeutralButton(R.string.move_delete, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					new DeleteConferenceTask(R.string.action_delete_conference, ctx, null).execute(conference);
					dialog.dismiss();
					scb.onCalled(true);
				}
			});
		}
		AlertDialog alertDialog = builder.create();
		return alertDialog;
	}
}