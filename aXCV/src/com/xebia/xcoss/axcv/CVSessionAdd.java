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

import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Conference.TimeSlot;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.model.Session.Type;
import com.xebia.xcoss.axcv.ui.FormatUtil;
import com.xebia.xcoss.axcv.ui.Identifiable;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.ui.TextInputDialog;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class CVSessionAdd extends AdditionActivity {

	private ScreenTimeUtil timeFormatter;
	private Conference conference;
	private Session session;
	private Session originalSession;
	private boolean create = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {	
		setContentView(R.layout.add_session);
		this.timeFormatter = new ScreenTimeUtil(this);

		conference = getConference();
		originalSession = getSelectedSession(conference);
		if (originalSession == null) {
			create = true;
			session = new Session();
			((TextView) findViewById(R.id.addModifyTitle)).setText("Add session");
		} else {
			session = new Session(originalSession);
			((TextView) findViewById(R.id.addModifyTitle)).setText("Edit session");
		}
		registerActions();
		super.onCreate(savedInstanceState);
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
		if (session == null) {
			session = getSelectedSession(conference);
		}
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
			view.setText(FormatUtil.getText(session.getLocation()));

			view = (TextView) findViewById(R.id.sessionPreps);
			view.setText(FormatUtil.getText(session.getPreparation()));

			view = (TextView) findViewById(R.id.sessionType);
			view.setText(FormatUtil.getText(session.getType()));

			ImageView iv = (ImageView) findViewById(R.id.completeness);
			int completeness = session.calculateCompleteness(8);
			if (completeness == 0) {
				iv.setVisibility(View.INVISIBLE);
			} else {
				iv.setVisibility(View.VISIBLE);
				iv.setImageResource(R.drawable.x_complete_1 + completeness - 1);
				iv.invalidate();
			}
			
			findViewById(R.id.detailsTitle).setVisibility( session.isBreak() ? View.GONE : View.VISIBLE);
			findViewById(R.id.detailsLayout).setVisibility( session.isBreak() ? View.GONE : View.VISIBLE);
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
					createDialog("Failed", "Please specify the following attributes: " + FormatUtil.getText(messages))
							.show();
					return;
				}
				if (!conference.addSession(session, create)) {
					Log.e(LOG.ALL, "Adding session failed.");
					createDialog("No session added", "Session could not be added.").show();
				}
				CVSessionAdd.this.finish();
			}
		});
		button = (Button) findViewById(R.id.actionDelete);
		if (create) {
			button.setVisibility(View.GONE);
		} else {
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View paramView) {
					StringBuilder message = new StringBuilder();
					message.append("Are you sure to delete session '").append(session.getTitle()).append("'");
					message.append(" on ");
					message.append(timeFormatter.getAbsoluteDate(session.getStartTime()));
					message.append("?");

					AlertDialog.Builder builder = new AlertDialog.Builder(CVSessionAdd.this);
					builder.setTitle("Delete session");
					builder.setMessage(message.toString());
					builder.setIcon(android.R.drawable.ic_dialog_alert);
					builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
					builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							conference.deleteSession(originalSession);
							CVSessionAdd.this.finish();
						}
					});
					builder.show();
				}
			});
		}
		button = (Button) findViewById(R.id.actionReschedule);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				View view = findViewById(R.id.sessionDuration);
				CharSequence text = ((TextView)view).getText();
				int duration = StringUtil.getFirstInteger(text.toString());
				rescheduleSession(duration);
				showConference();
				showSession();
			}
		});
	}

	private void rescheduleSession(int duration) {
		if (conference == null) {
			return;
		}
		if (duration == 0) {
			duration = session.getDuration();
		}
		List<Location> locations = null;
		Location sessionLocation = session.getLocation();
		if (sessionLocation != null && conference.hasLocation(sessionLocation)) {
			locations = new ArrayList<Location>();
			locations.add(sessionLocation);
		} else {
			locations = conference.getLocations();
		}

		TimeSlot slot = null;
		Iterator<Location> iterator = locations.iterator();
		while (slot == null && iterator.hasNext()) {
			Location location = iterator.next();
			Log.v("XCS", "Reschedule ["
					+ session.getStartTime()
					+ ", "
					+ duration
					+ ", "
					+ location.getDescription()
					+ "] => ");
			slot = conference.getNextAvailableTimeSlot(session, session.getStartTime(), duration, location);
			Log.v("XCS", slot == null ? "NONE" : slot.start + " till " + slot.end + " @ "
							+ slot.location.getDescription());
		}
//		// Move up to the next conference and call this method recursively
//		if (slot == null) {
//			conference = getConferenceServer().getUpcomingConference(conference.getDate().plusDays(1));
//			slot = rescheduleSession(duration);
//		}

		if (slot != null) {
			session.reschedule(conference, slot);
		} else {
			createDialog("Rescheduling failed",
					"The session cannot be scheduled. Shorten session, use another location or choose another conference.").show();
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
		int duration;
		switch (field) {
			case R.id.conferenceName:
				Identifiable ident = (Identifiable) selection;
				conference = getConferenceServer().getConference(ident.getIdentifier());
				Moment m = conference.getStartTime();
				session.onStartTime().setDate(m.getYear(), m.getMonth(), m.getDay());
				rescheduleSession(session.getDuration());
			break;
			case R.id.sessionStart:
				TimeSlot t = (TimeSlot) selection;
				duration = session.getDuration();
				session.onStartTime().setTime(t.start.getHour(), t.start.getMinute());
				rescheduleSession(duration);
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
				Type type = (Type) selection;
				session.setType(type);
				activateDetails(type.hasDetails());
			break;
			default:
				Log.w(LOG.NAVIGATE, "Don't know how to process: " + field);
		}
		showSession();
		showConference();
	}

	private void activateDetails(boolean state) {
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
				List<Conference> list = getConferenceServer().getUpcomingConferences(6);
				Identifiable[] data = new Identifiable[list.size()];
				idx = 0;
				for (Conference conference : list) {
					data[idx++] = new Identifiable(conference.getTitle(), conference.getId());
				}
				builder = new AlertDialog.Builder(this);
				builder.setTitle("Pick a conference");
				builder.setItems(Identifiable.stringValue(data), new DialogHandler(this, data, R.id.conferenceName));
				dialog = builder.create();
			break;
			case XCS.DIALOG.INPUT_TIME_START:
				Set<TimeSlot> tslist = null;
				if (session.getLocation() == null) {
					tslist = conference.getAvailableTimeSlots(session.getDuration());
				} else {
					tslist = conference.getAvailableTimeSlots(session.getDuration(), session.getLocation());
				}

				builder = new AlertDialog.Builder(this);
				builder.setTitle("Pick a start time");
				if (tslist.size() == 0) {
					builder.setMessage("This conference is fully booked!");
					builder.setIcon(android.R.drawable.ic_dialog_alert);
				} else {
					TimeSlot[] slots = tslist.toArray(new TimeSlot[0]);
					items = new String[slots.length];
					for (int i = 0; i < slots.length; i++) {
						items[i] = timeFormatter.getAbsoluteTime(slots[i].start) + " @ " + slots[i].location;
					}
					builder.setItems(items, new DialogHandler(this, slots, R.id.sessionStart));
				}
				dialog = builder.create();
				dialog.setOnCancelListener(this);
				dialog.setOnDismissListener(this);
			break;
			case XCS.DIALOG.INPUT_DURATION:
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
			break;
			case XCS.DIALOG.INPUT_LOCATION:
				Location[] locations = conference.getLocations().toArray(new Location[0]);

				builder = new AlertDialog.Builder(this);
				builder.setTitle("Select location");
				ListAdapter la = new ArrayAdapter<Location>(this, R.layout.simple_list_item_single_choice, locations);
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
				Type[] values = Session.Type.values();
				builder = new AlertDialog.Builder(this);
				builder.setTitle("Pick a type");
				ListAdapter ta = new ArrayAdapter<Type>(this, R.layout.simple_list_item_single_choice, values);
				builder.setSingleChoiceItems(ta, -1, new DialogHandler(this, values, R.id.sessionType));
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
				tid.setDescription("Intended audience");
				tid.setValue(session.getIntendedAudience());
			break;
			case XCS.DIALOG.INPUT_LIMIT:
				tid = (TextInputDialog) dialog;
				tid.setDescription("Number of people");
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
					Type type = session.getType();
					int size = lv.getCount();
					for (int idx = 0; idx < size; idx++) {
						Type listType = (Type) lv.getItemAtPosition(idx);
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