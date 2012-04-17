package com.xebia.xcoss.axcv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.xebia.xcoss.axcv.logic.cache.DataCache;
import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Conference.TimeSlot;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.model.SessionType;
import com.xebia.xcoss.axcv.tasks.DeleteSessionTask;
import com.xebia.xcoss.axcv.tasks.RegisterSessionTask;
import com.xebia.xcoss.axcv.tasks.RetrieveConferenceTask;
import com.xebia.xcoss.axcv.tasks.RetrieveConferencesFromDateTask;
import com.xebia.xcoss.axcv.tasks.SimpleCallBack;
import com.xebia.xcoss.axcv.tasks.TaskCallBack;
import com.xebia.xcoss.axcv.ui.Identifiable;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.ui.TextInputDialog;
import com.xebia.xcoss.axcv.util.DebugUtil;
import com.xebia.xcoss.axcv.util.FormatUtil;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class CVSessionAdd extends AdditionActivity {

	private ScreenTimeUtil timeFormatter;
	private List<Conference> nextConferences;
	private Conference conference;
	private Session session;
	private Session originalSession;
	private boolean create = true;
	private DialogInterface.OnClickListener cancelClickListener;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.add_session);
		super.onCreate(savedInstanceState);

		session = new Session();
		timeFormatter = new ScreenTimeUtil(this);
		cancelClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		};

		new RetrieveConferencesFromDateTask(R.string.action_retrieve_conferences, this,
				new TaskCallBack<List<Conference>>() {
					@Override
					public void onCalled(List<Conference> result) {
						nextConferences = result;
					}
				}).execute(6);

		if (!loadFrom(savedInstanceState)) {
			new RetrieveConferenceTask(R.string.action_retrieve_conference, this, new TaskCallBack<Conference>() {
				@Override
				public void onCalled(Conference result) {
					conference = result;
					originalSession = getSelectedSession(result);
					if (originalSession != null) {
						create = false;
						session = new Session(originalSession);
					}
					TextView tv = (TextView) findViewById(R.id.addModifyTitle);
					tv.setText(originalSession == null ? R.string.session_add : R.string.session_edit);

					showConference();
					showSession();
				}
			}).execute(getSelectedConferenceId());
		}
		registerActions();
	}

	private boolean loadFrom(Bundle savedInstanceState) {
		if (savedInstanceState != null && savedInstanceState.isEmpty() == false) {
			Log.w("debug", "Initialize from SIS");
			conference = (Conference) savedInstanceState.getSerializable("SIS_CONFERENCE");
			originalSession = (Session) savedInstanceState.getSerializable("SIS_ORIGINAL_SESSION");
			create = savedInstanceState.getBoolean("SIS_CREATE");
			session = (Session) savedInstanceState.getSerializable("SIS_SESSION");
			Log.w("debug", "Initialized from SIS: " + session);
			return conference != null && session != null;
		}
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("SIS_CONFERENCE", conference);
		outState.putSerializable("SIS_ORIGINAL_SESSION", originalSession);
		outState.putSerializable("SIS_SESSION", session);
		outState.putBoolean("SIS_CREATE", create);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		showConference();
		showSession();
		super.onResume();
	}

	private void showConference() {
		if (conference != null) {
			TextView view = (TextView) findViewById(R.id.conferenceDate);
			view.setText(timeFormatter.getAbsoluteDate(conference.getStartTime()));

			view = (TextView) findViewById(R.id.conferenceName);
			view.setText(conference.getTitle());
		}
	}

	private void showSession() {
		if (session != null) {
			Moment startTime = session.getStartTime();
			int duration = session.getDuration();

			TextView view = (TextView) findViewById(R.id.sessionTitle);
			view.setText(FormatUtil.getText(session.getTitle()));

			view = (TextView) findViewById(R.id.sessionDescription);
			view.setText(FormatUtil.getText(session.getDescription()));
			view.setEllipsize(TruncateAt.END);

			if (startTime != null) {
				view = (TextView) findViewById(R.id.sessionStart);
				view.setText(timeFormatter.getAbsoluteTime(startTime));
			}
			view = (TextView) findViewById(R.id.sessionLanguage);
			view.setText(FormatUtil.getList(session.getLanguages()));

			if (duration > 0) {
				view = (TextView) findViewById(R.id.sessionDuration);
				view.setText(getString(R.string.duration_minutes, duration));
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
			view.setText(FormatUtil.getText(session.getLocation()));

			view = (TextView) findViewById(R.id.sessionPreps);
			view.setText(FormatUtil.getText(session.getPreparation()));

			view = (TextView) findViewById(R.id.sessionType);
			view.setText(FormatUtil.getText(SessionType.get(session.getType()).toString()));

			ImageView iv = (ImageView) findViewById(R.id.completeness);
			int completeness = session.calculateCompleteness(8);
			if (completeness == 0) {
				iv.setVisibility(View.INVISIBLE);
			} else {
				iv.setVisibility(View.VISIBLE);
				iv.setImageResource(R.drawable.x_complete_1 + completeness - 1);
				iv.invalidate();
			}

			activateDetails(!session.isBreak() && !session.isMandatory());
		}
	}

	private void registerActions() {
		int[] identifiers = new int[] { R.id.conferenceName, R.id.sessionAudience, R.id.sessionAuthors,
				R.id.sessionCount, R.id.sessionDescription, R.id.sessionDuration, R.id.sessionLabels,
				R.id.sessionLanguage, R.id.sessionLocation, R.id.sessionPreps, R.id.sessionStart, R.id.sessionTitle };

		if (create) {
			activateViews(new int[] { R.id.sessionType });
		} else {
			passivateView(R.id.sessionType);
		}
		activateViews(identifiers);

		Button button = (Button) findViewById(R.id.actionSave);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				List<String> messages = new ArrayList<String>();
				if (!session.check(messages)) {
					createDialog(getString(R.string.failed),
							getString(R.string.specify_attributes, FormatUtil.getText(messages))).show();
					return;
				}
				new RegisterSessionTask(R.string.action_register_session, CVSessionAdd.this,
						new TaskCallBack<Boolean>() {
							@Override
							public void onCalled(Boolean result) {
								if (result != null) {
									// A session has been added, so the cache is
									// invalid.
									DataCache cache = getMyApplication().getCache();
									cache.remove(conference);
									if (!create) {
										cache.remove(session);
									}
									CVSessionAdd.this.finish();
								}
							}
						}).execute(session);
			}
		});
		button = (Button) findViewById(R.id.actionDelete);
		if (create) {
			button.setVisibility(View.GONE);
		} else {
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View paramView) {
					createDeleteDialog(CVSessionAdd.this, originalSession, new SimpleCallBack() {
						@Override
						public void onCalled(Boolean result) {
							DataCache cache = getMyApplication().getCache();
							cache.remove(conference);
							cache.remove(session);
							finish();
						}
					}).show();
				}

			});
		}
		button = (Button) findViewById(R.id.actionReschedule);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				View view = findViewById(R.id.sessionDuration);
				CharSequence text = ((TextView) view).getText();
				int duration = StringUtil.getFirstInteger(text.toString());
				rescheduleSession(duration == 0 ? session.getDuration() : duration);
				showConference();
				showSession();
			}
		});
	}

	public static AlertDialog.Builder createDeleteDialog(final BaseActivity ctx, final Session session,
			final SimpleCallBack scb) {
		String moment = new ScreenTimeUtil(ctx).getAbsoluteDate(session.getStartTime());
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(R.string.delete_session);
		builder.setMessage(ctx.getString(R.string.confirm_delete_session, session.getTitle(), moment));
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				new DeleteSessionTask(R.string.action_delete_session, ctx, null).execute(session);
				// A session has been deleted, so the cache needs to be
				// invalided.
				scb.onCalled(true);
			}
		});
		return builder;
	}

	private void rescheduleSession(final int duration) {
		if (conference == null) {
			return;
		}
		List<Location> locations = null;
		Location sessionLocation = session.getLocation();
		if (sessionLocation != null && conference.hasLocation(sessionLocation)) {
			locations = new ArrayList<Location>();
			locations.add(sessionLocation);
		} else {
			locations = conference.getLocations();
		}

		// TODO
		if (duration == 0) {
			DebugUtil.showCallStack();
		}

		TimeSlot slot = null;
		Iterator<Location> iterator = locations.iterator();
		while (slot == null && iterator.hasNext()) {
			Location location = iterator.next();
			Log.v("XCS", "Reschedule [" + session.getStartTime() + ", " + duration + ", " + location.getDescription()
					+ "] => ");
			slot = conference.getNextAvailableTimeSlot(session, session.getStartTime(), duration, location);
			Log.v("XCS",
					slot == null ? "NONE" : slot.start + " till " + slot.end + " @ " + slot.location.getDescription());
		}
		/*
		 * 
		 * // Move up to the next conference and call this method recursively if
		 * (slot == null) { RetrieveConferencesFromDateTask rcTask = new
		 * RetrieveConferencesFromDateTask( R.string.action_retrieve_conference,
		 * CVSessionAdd.this, new TaskCallBack<List<Conference>>() {
		 * 
		 * @Override public void onCalled(List<Conference> result) { if
		 * (result.size() > 0) { conference = result.get(0);
		 * rescheduleSession(duration); } } });
		 * rcTask.setMoment(conference.getEndTime()); rcTask.execute(); }
		 */
		if (slot != null) {
			session.reschedule(conference, slot);
		} else {
			createDialog(getString(R.string.rescheduling_failed), getString(R.string.reschedule_failed_message)).show();
		}
	}

	/**
	 * Takes the chosen attribute and converts this (String) value to a value
	 * for the object
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
		int duration;
		switch (field) {
		case R.id.conferenceName:
			String id = ((Identifiable) selection).getIdentifier();
			for (Conference conf : nextConferences) {
				if (id.equals(conf.getId())) {
					conference = conf;
					Moment m = conference.getStartTime();
					session.onStartTime().setDate(m.getYear(), m.getMonth(), m.getDay());
					rescheduleSession(session.getDuration());
				}
			}
			break;
		case R.id.sessionStart:
			TimeSlot t = (TimeSlot) selection;
			duration = session.getDuration();
			session.onStartTime().setTime(t.start.getHour(), t.start.getMinute());
			rescheduleSession(duration);
			break;
		case R.id.sessionDuration:
			duration = StringUtil.getFirstInteger(value);
			rescheduleSession(duration);
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
		case R.id.sessionCount:
			session.setLimit(value);
			break;
		case R.id.sessionDescription:
			session.setDescription(value);
			break;
		case R.id.sessionPreps:
			session.setPreparation(value);
			break;
		case R.id.sessionTitle:
			session.setTitle(value);
			break;
		case R.id.sessionLocation:
			session.setLocation((Location) selection);
			rescheduleSession(session.getDuration());
			break;
		case R.id.sessionType:
			SessionType type = (SessionType) selection;
			session.setType(type.getType());
			activateDetails(!type.isBreak() && !type.isMandatory());
			break;
		default:
			Log.w(LOG.NAVIGATE, "Don't know how to process: " + field);
		}
		showSession();
		showConference();
	}

	private void activateDetails(boolean state) {
		// We could hide the section altogether
		// findViewById(R.id.detailsTitle).setVisibility(session.isBreak() ?
		// View.GONE : View.VISIBLE);
		// findViewById(R.id.detailsLayout).setVisibility(session.isBreak() ?
		// View.GONE : View.VISIBLE);
		findViewById(R.id.sessionAudience).setEnabled(state);
		findViewById(R.id.sessionCount).setEnabled(state);
		findViewById(R.id.sessionPreps).setEnabled(state);
		findViewById(R.id.sessionLanguage).setEnabled(state);
		findViewById(R.id.sessionLabels).setEnabled(state);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		String[] items;
		AlertDialog.Builder builder;
		int idx;

		switch (id) {
		case XCS.DIALOG.SELECT_CONFERENCE:
			Identifiable[] data = new Identifiable[nextConferences.size()];
			idx = 0;
			for (Conference conference : nextConferences) {
				String title = conference.getTitle() + " ("
						+ timeFormatter.getAbsoluteShortDate(conference.getStartTime()) + ")";
				data[idx++] = new Identifiable(title, conference.getId());
			}
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.pick_conference);
			builder.setNegativeButton(R.string.cancel, cancelClickListener);
			builder.setItems(Identifiable.stringValue(data), new DialogHandler(this, data, R.id.conferenceName));
			dialog = builder.create();
			break;
		case XCS.DIALOG.INPUT_TIME_START:
			Set<TimeSlot> tslist = null;
			if (session.getLocation() == null) {
				tslist = conference.getAvailableTimeSlots(session.getDuration(), null);
			} else {
				ArrayList<Location> locs = new ArrayList<Location>();
				locs.add(session.getLocation());
				tslist = conference.getAvailableTimeSlots(session.getDuration(), locs);
			}

			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.pick_start);
			if (tslist.size() == 0) {
				builder.setMessage(R.string.conference_fully_booked);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
			} else {
				TimeSlot[] slots = tslist.toArray(new TimeSlot[0]);
				items = new String[slots.length];
				for (int i = 0; i < slots.length; i++) {
					items[i] = timeFormatter.getAbsoluteTime(slots[i].start) + " @ " + slots[i].location;
				}
				builder.setItems(items, new DialogHandler(this, slots, R.id.sessionStart));
			}
			builder.setNegativeButton(R.string.cancel, cancelClickListener);
			dialog = builder.create();
			dialog.setOnCancelListener(this);
			dialog.setOnDismissListener(this);
			break;
		case XCS.DIALOG.INPUT_DURATION:
			items = getResources().getStringArray(R.array.durationInterval);
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.pick_duration);
			builder.setItems(items, new DialogHandler(this, items, R.id.sessionDuration));
			builder.setNegativeButton(R.string.cancel, cancelClickListener);
			dialog = builder.create();
			break;
		case XCS.DIALOG.INPUT_LANGUAGE:
			items = getResources().getStringArray(R.array.languages);
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.select_languages);
			DialogHandler msdhandler = new DialogHandler(this, items, R.id.sessionLanguage);
			msdhandler.setCloseOnSelection(false);
			builder.setMultiChoiceItems(items, new boolean[items.length], msdhandler);
			builder.setPositiveButton(R.string.close, cancelClickListener);
			dialog = builder.create();
			break;
		case XCS.DIALOG.INPUT_LOCATION:
			Location[] locations = conference.getLocations().toArray(new Location[0]);

			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.select_location);
			ListAdapter la = new ArrayAdapter<Location>(this, R.layout.simple_list_item, locations);
			builder.setSingleChoiceItems(la, -1, new DialogHandler(this, locations, R.id.sessionLocation));
			dialog = builder.create();
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
		case XCS.DIALOG.INPUT_TYPE:
			SessionType[] values = SessionType.getAllTypes();
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.pick_type);
			ListAdapter ta = new ArrayAdapter<SessionType>(this, R.layout.simple_list_item_single_choice, values);
			builder.setSingleChoiceItems(ta, -1, new DialogHandler(this, values, R.id.sessionType));
			builder.setNegativeButton(R.string.cancel, cancelClickListener);
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
		ListView lv;

		switch (id) {
		case XCS.DIALOG.INPUT_AUDIENCE:
			tid = (TextInputDialog) dialog;
			tid.setDescription(getString(R.string.intended_audience));
			tid.setValue(session.getIntendedAudience());
			break;
		case XCS.DIALOG.INPUT_LIMIT:
			tid = (TextInputDialog) dialog;
			tid.setDescription(getString(R.string.number_of_people));
			tid.setValue(session.getLimit());
			break;
		case XCS.DIALOG.INPUT_DESCRIPTION:
			tid = (TextInputDialog) dialog;
			tid.setDescription(getString(R.string.description));
			tid.setValue(session.getDescription());
			break;
		case XCS.DIALOG.INPUT_PREPARATION:
			tid = (TextInputDialog) dialog;
			tid.setDescription(getString(R.string.preparation));
			tid.setValue(session.getPreparation());
			break;
		case XCS.DIALOG.INPUT_TITLE:
			tid = (TextInputDialog) dialog;
			tid.setDescription(getString(R.string.session_title));
			tid.setValue(session.getTitle());
			break;
		case XCS.DIALOG.INPUT_LOCATION:
			lv = ((AlertDialog) dialog).getListView();
			if (session.getLocation() != null) {
				int sid = session.getLocation().getId();
				int size = lv.getCount();
				for (int idx = 0; idx < size; idx++) {
					if (((Location) lv.getItemAtPosition(idx)).getId() == sid) {
						lv.setItemChecked(idx, true);
						break;
					}
				}
			}
			break;
		case XCS.DIALOG.INPUT_TYPE:
			lv = ((AlertDialog) dialog).getListView();
			if (session.getType() != null) {
				SessionType type = SessionType.get(session.getType());
				int size = lv.getCount();
				for (int idx = 0; idx < size; idx++) {
					SessionType listType = (SessionType) lv.getItemAtPosition(idx);
					if (type.equals(listType)) {
						lv.setItemChecked(idx, true);
						break;
					}
				}
			}
			break;
		}
		super.onPrepareDialog(id, dialog);
	}

	private void showAuthorPage() {
		Intent authorIntent = getAuthorIntent();
		ArrayList<Author> authorList = new ArrayList<Author>();
		authorList.addAll(session.getAuthors());
		authorIntent.putExtra(BaseActivity.IA_AUTHORS, (Serializable) authorList);
		startActivityForResult(authorIntent, ACTIVITY_SEARCH_AUTHOR);
	}

	private void showLabelPage() {
		Intent labelIntent = getLabelIntent();
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(session.getLabels());
		labelIntent.putExtra(BaseActivity.IA_LABELS, (Serializable) list);
		startActivityForResult(labelIntent, ACTIVITY_SEARCH_LABEL);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTIVITY_SEARCH_AUTHOR:
			if (data != null && data.hasExtra(IA_AUTHORS)) {
				session.getAuthors().clear();
				Serializable extra = data.getSerializableExtra(IA_AUTHORS);
				for (Author selected : ((ArrayList<Author>) extra)) {
					session.addAuthor(selected);
				}
				showSession();
			}
			break;
		case ACTIVITY_SEARCH_LABEL:
			if (data != null && data.hasExtra(IA_LABELS)) {
				session.getLabels().clear();
				Serializable extra = data.getSerializableExtra(IA_LABELS);
				for (String selected : ((List<String>) extra)) {
					session.addLabel(selected);
				}
				showSession();
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	public void onTextClick(int id) {

		switch (id) {
		case R.id.conferenceDate:
		case R.id.conferenceName:
			showDialog(XCS.DIALOG.SELECT_CONFERENCE);
			break;
		case R.id.sessionStart:
			showDialog(XCS.DIALOG.INPUT_TIME_START);
			break;
		case R.id.sessionDuration:
			showDialog(XCS.DIALOG.INPUT_DURATION);
			break;
		case R.id.sessionAudience:
			showDialog(XCS.DIALOG.INPUT_AUDIENCE);
			break;
		case R.id.sessionAuthors:
			showAuthorPage();
			break;
		case R.id.sessionCount:
			showDialog(XCS.DIALOG.INPUT_LIMIT);
			break;
		case R.id.sessionDescription:
			showDialog(XCS.DIALOG.INPUT_DESCRIPTION);
			break;
		case R.id.sessionLabels:
			showLabelPage();
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
		case R.id.sessionType:
			showDialog(XCS.DIALOG.INPUT_TYPE);
			break;
		default:
			Log.w(XCS.LOG.NAVIGATE, "Click on text not handled: " + id);
			break;
		}
	}

	@Override
	public void onDismiss(DialogInterface di) {
		// Remove the dialog to recreate it with correct values
		removeDialog(XCS.DIALOG.INPUT_TIME_START);
	}

	@Override
	public void onCancel(DialogInterface di) {
		// Remove the dialog to recreate it with correct values
		removeDialog(XCS.DIALOG.INPUT_TIME_START);
	}
}