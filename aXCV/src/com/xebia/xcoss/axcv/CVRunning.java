package com.xebia.xcoss.axcv;

import java.util.ArrayList;
import java.util.Set;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.xebia.xcoss.axcv.layout.SwipeLayout;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.tasks.RetrieveNextConferenceTask;
import com.xebia.xcoss.axcv.tasks.TaskCallBack;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.ui.SessionAdapter;
import com.xebia.xcoss.axcv.util.XCS;

/**
 * IA_CONFERENCE_ID - ID of selected conference (required by parent).
 * IA_MOMENT - Moment.asMinutes to show in this view
 * 
 * @author Michael
 */

public class CVRunning extends SessionSwipeActivity {

	private static final int TIME_DELTA = 60;
	private Session[] sessions;
	private Conference conference;
	private int moment;

	// TODO Next session from server does not take time into account.
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.schedule);
		super.onCreate(savedInstanceState);
		((SwipeLayout) findViewById(R.id.swipeLayout)).setGestureListener(this);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			moment = extras.getInt(IA_MOMENT);
		} else {
			moment = new Moment().asMinutes();
			moment = moment - moment % TIME_DELTA;
		}
	}

	@Override
	protected void onResume() {
		refreshScreen();
		super.onResume();
	}

	private void refreshScreen() {
		new RetrieveNextConferenceTask(R.string.action_retrieve_conference, this, new TaskCallBack<Conference>() {
			@Override
			public void onCalled(Conference cc) {
				if (cc != null) {
					conference = cc;
					
					updateMoment();
					
					setConferenceId(conference);
					TextView title = (TextView) findViewById(R.id.conferenceTitle);
					title.setText(cc.getTitle());

					TextView date = (TextView) findViewById(R.id.conferenceDate);
					ScreenTimeUtil timeUtil = new ScreenTimeUtil(CVRunning.this);
					String val = timeUtil.getAbsoluteDate(cc.getStartTime());
					date.setText(val);

					Set<Session> allSessions = cc.getSessions();
					sessions = filterSessions(allSessions);
					SessionAdapter adapter = new SessionAdapter(CVRunning.this, R.layout.session_item,
							R.layout.mandatory_item, sessions);
					ListView sessionList = (ListView) findViewById(R.id.sessionList);
					sessionList.setAdapter(adapter);

					updateTimeNavigation();

					TextView sessionLocation = ((TextView) findViewById(R.id.sessionLocation));
					sessionLocation.setText(timeUtil.getAbsoluteTime(Moment.fromMinutes(moment)));
				} else {
					// TODO The CVTask currently shows a dialog, which will leak when finishing...
					finish();
				}
			}
		}).execute(getConferenceId());
	}

	private void updateMoment() {
		int from = conference.getStartTime().asMinutes();
		if ( moment < from ) {
			moment = from;
			return;
		}
		int to = conference.getEndTime().asMinutes();
		if ( moment > to ) {
			moment = to - TIME_DELTA;
		}
	}

	private void updateTimeNavigation() {
		ScreenTimeUtil stu = new ScreenTimeUtil(this);
		int previous = getPreviousBlock();
		int next = getNextBlock();

		TextView location = (TextView) findViewById(R.id.nextLocationText);
		location.setOnLongClickListener(null);
		if (next == 0) {
			location.setVisibility(View.INVISIBLE);
		} else {
			location.setVisibility(View.VISIBLE);
			location.setText(stu.getAbsoluteTime(Moment.fromMinutes(next))); // R.string.later_in_time);
			location.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onSwipeRightToLeft();
				}
			});
		}
		location = (TextView) findViewById(R.id.prevLocationText);
		location.setOnLongClickListener(null);
		if (previous == 0) {
			location.setVisibility(View.INVISIBLE);
		} else {
			location.setVisibility(View.VISIBLE);
			location.setText(stu.getAbsoluteTime(Moment.fromMinutes(previous)));// R.string.earlier_in_time);
			location.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onSwipeLeftToRight();
				}
			});
		}
	}

	public int getNextBlock() {
		int next = moment + TIME_DELTA;
		int endTime = conference.getEndTime().asMinutes() - TIME_DELTA;

		if (next > endTime) {
			if (moment >= endTime) {
				next = 0;
			} else {
				next = endTime;
			}
		}
		return next;
	}

	public int getPreviousBlock() {
		int previous = moment - TIME_DELTA;
		int startTime = conference.getStartTime().asMinutes();
		
		if (previous < startTime) {
			if (moment <= startTime) {
				previous = 0;
			} else {
				previous = startTime;
			}
		}
		return previous;
	}

	@Override
	public void onSwipeLeftToRight() {
		int previous = getPreviousBlock();
		if (previous != 0) {
			Intent intent = getIntent();
			intent.putExtra(IA_CONFERENCE_ID, conference.getId());
			intent.putExtra(IA_MOMENT, previous);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_right, 0);
		}
	}

	@Override
	public void onSwipeRightToLeft() {
		int next = getNextBlock();
		if (next != 0) {
			Intent intent = getIntent();
			intent.putExtra(IA_CONFERENCE_ID, conference.getId());
			intent.putExtra(IA_MOMENT, next);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_left, 0);
		}
	}

	protected Session[] filterSessions(Set<Session> allSessions) {
		ArrayList<Session> filtered = new ArrayList<Session>();

		int startBlock = moment;
		int endBlock = moment + TIME_DELTA;
		
		for (Session session : allSessions) {
			if (session.getStartTime().asMinutes() < endBlock && session.getEndTime().asMinutes() > startBlock ) {
				filtered.add(session);
			}
		}
		return filtered.toArray(new Session[filtered.size()]);
	}

	public void switchTo(int paramInt) {
		switchTo(getConferenceId(), paramInt);
	}

	private void switchTo(String conferenceId, int sessionIndex) {
		Session session = sessions[sessionIndex];
		Intent intent = new Intent(this, CVSessionView.class);
		intent.putExtra(BaseActivity.IA_CONFERENCE_ID, conferenceId);
		intent.putExtra(BaseActivity.IA_SESSION, session.getId());
		startActivity(intent);
	}

	@Override
	protected void populateMenuOptions(ArrayList<Integer> list) {
		list.add(XCS.MENU.SETTINGS);
		list.add(XCS.MENU.SEARCH);
		list.add(XCS.MENU.TRACK);
		list.add(XCS.MENU.RUNNING);
	}

}