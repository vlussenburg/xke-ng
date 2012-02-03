package com.xebia.xcoss.axcv;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.xebia.xcoss.axcv.logic.ProfileManager;
import com.xebia.xcoss.axcv.logic.ProfileManager.Trackable;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.model.util.SessionComparator;
import com.xebia.xcoss.axcv.tasks.RetrieveSessionTask;
import com.xebia.xcoss.axcv.tasks.TaskCallBack;
import com.xebia.xcoss.axcv.ui.SessionAdapter;

public class CVTrack extends BaseActivity {

	private ArrayList<Session> sessions;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.my_track);

		ListView sessionList = (ListView) findViewById(R.id.sessionList);
		sessionList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int paramInt, long paramLong) {
				switchTo(paramInt);
			}
		});
		super.onCreate(savedInstanceState);
	}

	private void switchTo(int sessionIndex) {
		Session session = sessions.get(sessionIndex);
		Intent intent = new Intent(this, CVSessionView.class);
		intent.putExtra(BaseActivity.IA_CONFERENCE_ID, session.getConferenceId());
		intent.putExtra(BaseActivity.IA_SESSION, session.getId());
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		ProfileManager pm = getMyApplication().getProfileManager();
		Trackable[] markedSessions = pm.getMarkedSessions(getUser());
		sessions = new ArrayList<Session>();
		for (Trackable id : markedSessions) {
			new RetrieveSessionTask(R.string.action_retrieve_session, this, new TaskCallBack<Session>() {
				@Override
				public void onCalled(Session result) {
					if (result != null) {
						updateSessions(result);
					}
				}
			}).silent().execute(id.sessionId, id.conferenceId);
		}
		pm.pruneMarked();
		super.onResume();
	}
	
	private void updateSessions(Session session) {
		sessions.add(session);
		Collections.sort(sessions, new SessionComparator());
		Session[] sessionArray = sessions.toArray(new Session[sessions.size()]);
		SessionAdapter adapter = new SessionAdapter(this, R.layout.session_item, R.layout.mandatory_item, sessionArray);
		adapter.setIncludeDate(true);
		ListView sessionList = (ListView) findViewById(R.id.sessionList);
		sessionList.setAdapter(adapter);
	}
}