package com.xebia.xcoss.axcv;

import java.util.ArrayList;
import java.util.List;

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
import com.xebia.xcoss.axcv.ui.FormatUtil;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.ui.TextInputDialog;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class CVConferenceAdd extends AdditionActivity implements OnCancelListener, OnDismissListener {

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
			TextView view = (TextView) findViewById(R.id.conferenceDate);
			view.setText(timeFormatter.getAbsoluteDate(conference.getDate()));

			view = (TextView) findViewById(R.id.conferenceName);
			view.setText(conference.getTitle());
		}
	}


	private void registerActions() {
		AddOnTouchListener touchListener = new AddOnTouchListener();
		AddOnClickListener clickListener = new AddOnClickListener();
		Drawable drawable = getResources().getDrawable(R.drawable.touchtext_disable);

		int[] identifiers = new int[] { R.id.conferenceName, R.id.conferenceDate };
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
				if (!conference.check(messages)) {
					createDialog("Failed", "Please specify the following attributes: " + FormatUtil.getText(messages))
							.show();
					return;
				}
				
				if ( create ) {
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
			default:
				Log.w(LOG.NAVIGATE, "Don't know how to process: " + field);
		}
		showConference();
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
			case XCS.DIALOG.INPUT_TITLE:
				dialog = new TextInputDialog(this, R.id.sessionTitle);
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
		private Object[] items;
		private CVConferenceAdd activity;

		public DialogHandler(CVConferenceAdd activity, Object[] items, int field) {
			this.activity = activity;
			this.items = items;
			this.field = field;
		}

		public void onClick(DialogInterface dialog, int item) {
			onClick(dialog, item, true);
		}

		@Override
		public void onClick(DialogInterface dialog, int item, boolean state) {
			Toast.makeText(getApplicationContext(), items[item].toString(), Toast.LENGTH_SHORT).show();
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