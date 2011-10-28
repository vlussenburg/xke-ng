package com.xebia.xcoss.axcv.ui;

import hirondelle.date4j.DateTime;
import hirondelle.date4j.DateTime.DayOverflow;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.Dialog;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.xebia.xcoss.axcv.AdditionActivity;
import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Conference.TimeSlot;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.model.Session.Type;
import com.xebia.xcoss.axcv.util.StringUtil;

public class AddBreakDialog extends Dialog {

	private AdditionActivity activity;
	private int identifier;
	private Conference conference;
	private ScreenTimeUtil timeFormatter;

	public AddBreakDialog(AdditionActivity activity, int id) {
		super(activity);
		this.activity = activity;
		this.identifier = id;
		init();
	}

	private void init() {
		setContentView(R.layout.dialog_addbreak);

		timeFormatter = new ScreenTimeUtil(activity);

		LayoutParams params = getWindow().getAttributes();
		params.width = LayoutParams.FILL_PARENT;
		getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

		setTitle("Insert break");
		TextView view = (TextView) findViewById(R.id.seValue);
		view.setText(R.string.default_break_name);

		Spinner spinner = (Spinner) findViewById(R.id.bDuration);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int selection, long arg3) {
				updateStartTimes();
			}

			public void onNothingSelected(AdapterView<?> arg0) {}
		});

		updateSpinners();

		Button submit = (Button) findViewById(R.id.seCommit);
		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				int duration = getDuration();
				Location location = getLocation();
				DateTime starttime = getDateTime();
				TextView view = (TextView) findViewById(R.id.seValue);
				String title = view.getText().toString();
				if (StringUtil.isEmpty(title)) {
					title = activity.getResources().getText(R.string.default_break_name).toString();
				}

				if (duration > 0 && location != null && starttime != null) {
					Session session = new Session();
					session.setType(Type.BREAK);
					session.setTitle(title);
					session.setDescription(title);
					session.setLocation(location);
					session.setStartTime(conference.getDate());
					session.setStartTime(starttime);
					session.setEndTime(session.getStartTime().plus(0, 0, 0, 0, duration, 0, DayOverflow.Spillover));

					activity.updateField(identifier, session, true);
					dismiss();
				}
			}
		});
		Button cancel = (Button) findViewById(R.id.seCancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				dismiss();
			}
		});
	}

	private int getDuration() {
		Spinner spinner = (Spinner) findViewById(R.id.bDuration);
		String duration = (String) spinner.getSelectedItem();
		return StringUtil.getFirstInteger(duration);
	}

	private Location getLocation() {
		Spinner spinner = (Spinner) findViewById(R.id.bLocation);
		return (Location) spinner.getSelectedItem();
	}

	private DateTime getDateTime() {
		Spinner spinner = (Spinner) findViewById(R.id.bStartTime);
		String date = (String) spinner.getSelectedItem();
		return timeFormatter.getAbsoluteTime(date);
	}

	private void updateSpinners() {

		String[] durations = new String[] { "5 min", "10 min", "15 min", "30 min", "60 min", "90 min", "120 min" };
		Spinner spinner = (Spinner) findViewById(R.id.bDuration);
		spinner.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, durations));
		spinner.setSelection(4);

		Set<Location> locations = updateStartTimes();

		Location[] locarray = locations.toArray(new Location[locations.size()]);
		spinner = (Spinner) findViewById(R.id.bLocation);
		spinner.setAdapter(new ArrayAdapter<Location>(getContext(), android.R.layout.simple_spinner_item, locarray));
	}

	private Set<Location> updateStartTimes() {
		Set<Location> locations = new HashSet<Location>();

		if (conference != null) {
			Set<DateTime> startTime = new HashSet<DateTime>();

			int duration = getDuration();
			if (duration == 0) duration = TimeSlot.LENGTH;

			Set<TimeSlot> timeSlots = conference.getAvailableTimeSlots(duration);

			for (TimeSlot timeSlot : timeSlots) {
				locations.add(timeSlot.location);
				startTime.add(timeSlot.start);
			}

			SortedSet<String> startdata = new TreeSet<String>();
			for (DateTime dateTime : startTime) {
				startdata.add(timeFormatter.getAbsoluteTime(dateTime));
			}
			String[] startarray = startdata.toArray(new String[startdata.size()]);
			Spinner spinner = (Spinner) findViewById(R.id.bStartTime);
			Object selectedItem = spinner.getSelectedItem();
			spinner.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, startarray));
			int position = 0;
			for (int i = 0; i < startarray.length; i++) {
				if ( startarray[i].equals(selectedItem)) {
					position = i;
				}
			}
			spinner.setSelection(position);
		}
		return locations;
	}

	public void setConference(Conference conf) {
		this.conference = conf;
		updateSpinners();
	}
}
