package com.xebia.xcoss.axcv.ui;

import java.util.HashSet;
import java.util.List;
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
import com.xebia.xcoss.axcv.model.Moment;
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

		Button submit = (Button) findViewById(R.id.seCommit);
		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				int duration = getDuration();
				Location location = getLocation();
				Moment time = getMoment();
				TextView view = (TextView) findViewById(R.id.seValue);
				String title = view.getText().toString();
				if (StringUtil.isEmpty(title)) {
					title = activity.getResources().getText(R.string.default_break_name).toString();
				}

				if (duration > 0 && location != null && time != null) {
					Session session = new Session();
					session.setType(Type.BREAK);
					session.setTitle(title);
					session.setDescription(title);
					session.setLocation(location);
					Moment s = conference.getStartTime();
					session.onStartTime().setDate(s.getYear(), s.getMonth(), s.getDay());
					session.onStartTime().setTime(time.getHour(), time.getMinute());

					time = session.getStartTime().plusMinutes(duration);
					session.onEndTime().setDate(s.getYear(), s.getMonth(), s.getDay());
					session.onEndTime().setTime(time.getHour(), time.getMinute());

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

		updateSpinners();
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

	private Moment getMoment() {
		Spinner spinner = (Spinner) findViewById(R.id.bStartTime);
		String date = (String) spinner.getSelectedItem();
		return timeFormatter.getAbsoluteTime(date);
	}

	private void updateSpinners() {
		final Spinner spinner = (Spinner) findViewById(R.id.bDuration);
		if (spinner.getSelectedItemPosition() == Spinner.INVALID_POSITION) {
			String[] durations = new String[] { "5 min", "10 min", "15 min", "30 min", "60 min", "90 min", "120 min" };
			ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getContext(),
					android.R.layout.simple_spinner_item, durations);
			adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter1);
			spinner.setSelection(4);
			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> v1, View v2, int arg1, long arg2) {
					updateSpinners();
				}
				@Override
				public void onNothingSelected(AdapterView<?> v1) {
				}
			});
		}

		Button submit = (Button) findViewById(R.id.seCommit);
		boolean updateStartTimes = updateStartTimes();
		if (updateStartTimes) {
			List<Location> locations = conference.getLocations();
			Location[] locarray = locations.toArray(new Location[locations.size()]);
			Spinner spinner2 = (Spinner) findViewById(R.id.bLocation);
			ArrayAdapter<Location> adapter2 = new ArrayAdapter<Location>(getContext(),
					android.R.layout.simple_spinner_item, locarray);
			adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner2.setAdapter(adapter2);
		}
		submit.setEnabled(updateStartTimes);
	}

	private boolean updateStartTimes() {
		boolean hasTimeSlots = false;
		if (conference != null) {
			Set<Moment> startTime = new HashSet<Moment>();

			int duration = getDuration();
			if (duration == 0) duration = TimeSlot.LENGTH;

			Set<TimeSlot> timeSlots = conference.getAvailableTimeSlots(duration, conference.getLocations());

			for (TimeSlot timeSlot : timeSlots) {
				startTime.add(timeSlot.start);
			}

			SortedSet<String> startdata = new TreeSet<String>();
			for (Moment dateTime : startTime) {
				startdata.add(timeFormatter.getAbsoluteTime(dateTime));
			}
			String[] startarray = startdata.toArray(new String[startdata.size()]);
			Spinner spinner = (Spinner) findViewById(R.id.bStartTime);
			Object selectedItem = spinner.getSelectedItem();
			ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(getContext(),
					android.R.layout.simple_spinner_item, startarray);
			adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter3);
			int position = 0;
			for (int i = 0; i < startarray.length; i++) {
				if (startarray[i].equals(selectedItem)) {
					position = i;
				}
			}
			spinner.setSelection(position);
			hasTimeSlots = timeSlots.size() > 0;
		}
		return hasTimeSlots;
	}

	public void setConference(Conference conf) {
		this.conference = conf;
		updateSpinners();
	}
}
