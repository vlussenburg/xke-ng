package com.xebia.xcoss.axcv;

import hirondelle.date4j.DateTime;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Conference.TimeSlot;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class CVSessionAdd extends BaseActivity {

	private ScreenTimeUtil timeFormatter;
	private Conference conference;
	private Session session;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_session);
		this.timeFormatter = new ScreenTimeUtil(this);
		updateConference();
		updateSession();
		registerActions();
	}

	private void updateConference() {
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

	private void updateSession() {
		if (session == null) {
			session = getSession(conference, false);
		}
		if (session != null) {
			// TODO
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
	}

	public void updateField(int field, String value) {
		switch (field) {
			case R.id.conferenceName:
				this.conference = getConferenceServer().getConferences().findConferenceByName(value);
				updateConference();
			case R.id.sessionStart:
				// TODO
				updateSession();
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.removeItem(XCS.MENU.ADD);
		menu.removeItem(XCS.MENU.EDIT);
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
//				builder.setMessage("It will be auto scheduled to fit the first suitable time.");
				builder.setItems(items, new DialogHandler(this, items, R.id.sessionStart));
				dialog = builder.create();
			break;
			case XCS.DIALOG.SELECT_DURATION:
				items = new String[] { "5 min", "10 min", "15 min", "30 min", "60 min", "90 min", "120 min" };
				builder = new AlertDialog.Builder(this);
				builder.setTitle("Pick a duration");
				builder.setItems(items, new DialogHandler(this, items, R.id.sessionDuration));
				dialog = builder.create();
			break;
		}
		if (dialog != null) {
			return dialog;
		}
		return super.onCreateDialog(id);
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
			// boolean handled = false;

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
				default:
					Log.w(XCS.LOG.NAVIGATE, "Click on text not handled: " + view.getId());
				break;
			}
			// return handled;
		}
	}

	private class DialogHandler implements DialogInterface.OnClickListener {
		private int field;
		private String[] items;
		private CVSessionAdd activity;

		public DialogHandler(CVSessionAdd activity, String[] items, int field) {
			this.activity = activity;
			this.items = items;
			this.field = field;
		}

		public void onClick(DialogInterface dialog, int item) {
			Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
			TextView view = (TextView) activity.findViewById(field);
			view.setText(items[item]);
			activity.updateField(field, items[item]);
		}
	}
}