package com.xebia.xcoss.axcv;

import hirondelle.date4j.DateTime;
import hirondelle.date4j.DateTime.DayOverflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Conference.TimeSlot;
import com.xebia.xcoss.axcv.model.ConferenceList;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.ui.FormatUtil;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.ui.StringUtil;
import com.xebia.xcoss.axcv.ui.TextInputDialog;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class CVSessionAdd extends BaseActivity implements OnCancelListener, OnDismissListener {

	private ScreenTimeUtil timeFormatter;
	private Conference conference;
	private Session session;
	private Session originalSession;
	private boolean create = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_session);
		this.timeFormatter = new ScreenTimeUtil(this);

		originalSession = getSession(conference, false);
		if (originalSession == null) {
			create = true;
			session = new Session();
		} else {
			session = new Session(originalSession);
		}

		showConference();
		showSession();
		registerActions();
	}

	private void showConference() {
		if (conference == null) {
			conference = getConference();
		}
		if (conference != null) {
			TextView view = (TextView) findViewById(R.id.conferenceDate);
			view.setText(timeFormatter.getAbsoluteDate(conference.getDate()));

			view = (TextView) findViewById(R.id.conferenceName);
			view.setText(conference.getTitle());
		}
	}

	private void showSession() {
		if (session == null) {
			session = getSession(conference, false);
		}
		if (session != null) {
			DateTime startTime = session.getStartTime();
			int duration = session.getDuration();

			TextView view = (TextView) findViewById(R.id.sessionTitle);
			view.setText(FormatUtil.getText(session.getTitle()));

			view = (TextView) findViewById(R.id.sessionDescription);
			view.setText(FormatUtil.getText(session.getDescription()));

			if (startTime != null) {
				view = (TextView) findViewById(R.id.sessionStart);
				view.setText(timeFormatter.getAbsoluteTime(startTime));
			}
			view = (TextView) findViewById(R.id.sessionLanguage);
			view.setText(FormatUtil.getList(session.getLanguages()));

			if (duration > 0) {
				view = (TextView) findViewById(R.id.sessionDuration);
				view.setText(String.valueOf(duration) + " min");
			}

			view = (TextView) findViewById(R.id.sessionAudience);
			view.setText(FormatUtil.getText(session.getIntendedAudience()));

			view = (TextView) findViewById(R.id.sessionAuthors);
			view.setText(FormatUtil.getList(session.getAuthors()));

			view = (TextView) findViewById(R.id.sessionCount);
			view.setText(FormatUtil.getText(session.getLimit()));

			view = (TextView) findViewById(R.id.sessionLabels);
			view.setText(FormatUtil.getList(session.getLabels()));

			view = (TextView) findViewById(R.id.sessionLocation);
			view.setText(FormatUtil.getText(session.getLocation().toString()));

			view = (TextView) findViewById(R.id.sessionPreps);
			view.setText(FormatUtil.getText(session.getPreparation()));
		}
	}

	private void registerActions() {
		AddOnTouchListener touchListener = new AddOnTouchListener();
		AddOnClickListener clickListener = new AddOnClickListener();
		Drawable drawable = getResources().getDrawable(R.drawable.touchtext_disable);

		int[] identifiers = new int[] { R.id.conferenceName, R.id.sessionAudience, R.id.sessionAuthors,
				R.id.sessionCount, R.id.sessionDescription, R.id.sessionDuration, R.id.sessionLabels,
				R.id.sessionLanguage, R.id.sessionLocation, R.id.sessionPreps, R.id.sessionStart, R.id.sessionTitle };
		for (int i = 0; i < identifiers.length; i++) {
			TextView view = (TextView) findViewById(identifiers[i]);
			view.setOnTouchListener(touchListener);
			view.setOnClickListener(clickListener);
			view.setBackgroundDrawable(drawable);
		}

		Button button = (Button) findViewById(R.id.actionSave);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				List<String> messages = new ArrayList<String>();
				if (!session.check(messages)) {
					createDialog("Failed", "Please specify the following attributes: " + FormatUtil.getText(messages)).show();
					return;
				}
				if (!conference.addSession(session)) {
					Log.e(LOG.ALL, "Adding session failed.");
					createDialog("No session added", "Session could not be added.");
				}
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
					conference.deleteSession(originalSession);
				}
			});
		}
		button = (Button) findViewById(R.id.actionReschedule);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View paramView) {

				TimeSlot slot = conference.getNextAvailableTimeSlot(session.getStartTime(), session.getDuration());
				if ( slot == null ) {
					ConferenceList allConferences = getConferenceServer().getConferences();
					while ( slot == null ) {
						conference = allConferences.getFirstConference(conference.getDate().plusDays(1));
						if ( conference == null ) {
							break;
						}
						slot = conference.getNextAvailableTimeSlot(session.getStartTime(), session.getDuration());
					}
				}
				
				if (slot != null) {
					session.setStartTime(slot.start);
					session.setEndTime(slot.end);
					session.setConference(conference);
					session.setDate(conference.getDate());
					showConference();
					showSession();
					// TODO Difference in create and modify
				} else {
					// No conferences available to fit slot in.
				}
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
	 * @param state
	 *            Indicates a set (true) or a reset (false)
	 */
	public void updateField(int field, String value, boolean state) {
		switch (field) {
			case R.id.conferenceName:
				conference = getConferenceServer().getConferences().findConferenceByName(value);
				session.setDate(conference.getDate());
				session.setConference(conference);
				showConference();
			break;
			case R.id.sessionStart:
				session.setStartTime(timeFormatter.getAbsoluteTime(value));
			break;
			case R.id.sessionDuration:
				int duration = StringUtil.getFirstInteger(value);
				if (session.getStartTime() == null) {
					DateTime startTime = conference.getStartTime();
					TimeSlot slot = conference.getNextAvailableTimeSlot(startTime, duration);
					if (slot != null) {
						session.setStartTime(slot.start);
					} else {
						session.setStartTime(startTime);
					}
				}
				session.setEndTime(session.getStartTime().plus(0, 0, 0, 0, duration, 0, DayOverflow.Spillover));
			break;
			case R.id.sessionLanguage:
				Set<String> languages = session.getLanguages();
				if (state)
					languages.add(value);
				else
					languages.remove(value);
			break;
			case R.id.sessionAudience:
				session.setIntendedAudience(value);
			break;
			case R.id.sessionAuthors:
				Set<String> authors = session.getAuthors();
				if (state)
					authors.add(value);
				else
					authors.remove(value);
			break;
			case R.id.sessionCount:
				session.setLimit(value);
			break;
			case R.id.sessionDescription:
				session.setDescription(value);
			break;
			case R.id.sessionLabels:
				Set<String> labels = session.getLabels();
				if (state)
					labels.add(value);
				else
					labels.remove(value);
			break;
			case R.id.sessionLocation:
				session.setLocation(new Location(value));
			break;
			case R.id.sessionPreps:
				session.setPreparation(value);
			break;
			case R.id.sessionTitle:
				session.setTitle(value);
			break;
		}
		showSession();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		String[] items;
		AlertDialog.Builder builder;
		int idx;

		switch (id) {
			case XCS.DIALOG.SELECT_CONFERENCE:
				List<Conference> list = getConferenceServer().getConferences().getUpcomingConferences(6);
				items = new String[list.size()];
				idx = 0;
				for (Conference conference : list) {
					items[idx++] = conference.getTitle();
				}
				builder = new AlertDialog.Builder(this);
				builder.setTitle("Pick a conference");
				builder.setItems(items, new DialogHandler(this, items, R.id.conferenceName));
				dialog = builder.create();
			break;
			case XCS.DIALOG.SELECT_TIME:
				List<TimeSlot> tslist = conference.getAvailableTimeSlots();
				items = new String[tslist.size()];
				idx = 0;
				for (TimeSlot timeSlot : tslist) {
					items[idx++] = timeFormatter.getAbsoluteTime(timeSlot.start);
				}
				builder = new AlertDialog.Builder(this);
				builder.setTitle("Pick a start time");
				if (items.length == 0) {
					builder.setMessage("This conference is fully booked!");
					// TODO Use alert icon
					builder.setIcon(R.drawable.icon);
				} else {
					builder.setItems(items, new DialogHandler(this, items, R.id.sessionStart));
				}
				dialog = builder.create();
				dialog.setOnCancelListener(this);
				dialog.setOnDismissListener(this);
			break;
			case XCS.DIALOG.SELECT_DURATION:
				items = new String[] { "5 min", "10 min", "15 min", "30 min", "60 min", "90 min", "120 min" };
				builder = new AlertDialog.Builder(this);
				builder.setTitle("Pick a duration");
				builder.setItems(items, new DialogHandler(this, items, R.id.sessionDuration));
				dialog = builder.create();
			break;
			case XCS.DIALOG.INPUT_LANGUAGE:
				items = new String[] { "Dutch", "English", "French", "Hindi" };
				builder = new AlertDialog.Builder(this);
				builder.setTitle("Select languages");
				builder.setMultiChoiceItems(items, new boolean[items.length], new DialogHandler(this, items,
						R.id.sessionLanguage));
				dialog = builder.create();
			case R.id.sessionLanguage:
			break;
			case XCS.DIALOG.INPUT_AUDIENCE:
				dialog = new TextInputDialog(this, R.id.sessionAudience);
			break;
			case XCS.DIALOG.INPUT_LIMIT:
				dialog = new TextInputDialog(this, R.id.sessionCount);
			break;
			case XCS.DIALOG.INPUT_DESCRIPTION:
				dialog = new TextInputDialog(this, R.id.sessionDescription);
			break;
			case XCS.DIALOG.INPUT_PREPARATION:
				dialog = new TextInputDialog(this, R.id.sessionPreps);
			break;
			case XCS.DIALOG.INPUT_TITLE:
				dialog = new TextInputDialog(this, R.id.sessionTitle);
			break;
			case XCS.DIALOG.INPUT_AUTHORS:
				// dialog = new AuthorInputDialog(this, R.id.);
			break;
			case XCS.DIALOG.INPUT_LABELS:
				// dialog = new LabelInputDialog(this, id);
			break;
			case XCS.DIALOG.INPUT_LOCATION:
				// dialog = new LocationInputDialog(this, id);
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
			case XCS.DIALOG.INPUT_AUDIENCE:
				tid = (TextInputDialog) dialog;
				tid.setDescription("Intended audience");
				tid.setValue(session.getIntendedAudience());
			break;
			case XCS.DIALOG.INPUT_LIMIT:
				tid = (TextInputDialog) dialog;
				tid.setDescription("Audience limit");
				tid.setValue(session.getLimit());
			break;
			case XCS.DIALOG.INPUT_DESCRIPTION:
				tid = (TextInputDialog) dialog;
				tid.setDescription("Description");
				tid.setValue(session.getDescription());
			break;
			case XCS.DIALOG.INPUT_PREPARATION:
				tid = (TextInputDialog) dialog;
				tid.setDescription("Preparation");
				tid.setValue(session.getPreparation());
			break;
			case XCS.DIALOG.INPUT_TITLE:
				tid = (TextInputDialog) dialog;
				tid.setDescription("Session title");
				tid.setValue(session.getTitle());
			break;
		}
		super.onPrepareDialog(id, dialog);
	}

	private class AddOnTouchListener implements OnTouchListener {

		@Override
		public boolean onTouch(View view, MotionEvent event) {
			Drawable drawable = null;
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_POINTER_DOWN:
					drawable = getResources().getDrawable(R.drawable.touchtext);
				break;
				default:
				break;
			}
			view.setBackgroundDrawable(drawable);
			// Need to return false to handle click event
			return false;
		}
	}

	private class AddOnClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {

			switch (view.getId()) {
				case R.id.conferenceDate:
				case R.id.conferenceName:
					showDialog(XCS.DIALOG.SELECT_CONFERENCE);
				break;
				case R.id.sessionStart:
					showDialog(XCS.DIALOG.SELECT_TIME);
				break;
				case R.id.sessionDuration:
					showDialog(XCS.DIALOG.SELECT_DURATION);
				break;
				case R.id.sessionAudience:
					showDialog(XCS.DIALOG.INPUT_AUDIENCE);
				break;
				case R.id.sessionAuthors:
					showDialog(XCS.DIALOG.INPUT_AUTHORS);
				break;
				case R.id.sessionCount:
					showDialog(XCS.DIALOG.INPUT_LIMIT);
				break;
				case R.id.sessionDescription:
					showDialog(XCS.DIALOG.INPUT_DESCRIPTION);
				break;
				case R.id.sessionLabels:
					showDialog(XCS.DIALOG.INPUT_LABELS);
				break;
				case R.id.sessionLanguage:
					showDialog(XCS.DIALOG.INPUT_LANGUAGE);
				break;
				case R.id.sessionLocation:
					showDialog(XCS.DIALOG.INPUT_LOCATION);
				break;
				case R.id.sessionPreps:
					showDialog(XCS.DIALOG.INPUT_PREPARATION);
				break;
				case R.id.sessionTitle:
					showDialog(XCS.DIALOG.INPUT_TITLE);
				break;
				default:
					Log.w(XCS.LOG.NAVIGATE, "Click on text not handled: " + view.getId());
				break;
			}
		}
	}

	private class DialogHandler implements DialogInterface.OnClickListener, DialogInterface.OnMultiChoiceClickListener {
		private int field;
		private String[] items;
		private CVSessionAdd activity;

		public DialogHandler(CVSessionAdd activity, String[] items, int field) {
			this.activity = activity;
			this.items = items;
			this.field = field;
		}

		public void onClick(DialogInterface dialog, int item) {
			onClick(dialog, item, true);
		}

		@Override
		public void onClick(DialogInterface dialog, int item, boolean state) {
			Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
			// TextView view = (TextView) activity.findViewById(field);
			// view.setText(items[item]);
			activity.updateField(field, items[item], state);
		}
	}

	@Override
	public void onDismiss(DialogInterface di) {
		removeDialog(XCS.DIALOG.SELECT_TIME);
	}

	@Override
	public void onCancel(DialogInterface di) {
		removeDialog(XCS.DIALOG.SELECT_TIME);
	}
}