package com.xebia.xcoss.axcv;

import java.util.TreeSet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.logic.ProfileManager.Trackable;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.model.util.SessionComparator;
import com.xebia.xcoss.axcv.ui.SessionAdapter;
import com.xebia.xcoss.axcv.util.XCS;

public class CVTrack extends BaseActivity {

	private Session[] sessions;

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
		Session session = sessions[sessionIndex];
		Intent intent = new Intent(this, CVSessionView.class);
		intent.putExtra(BaseActivity.IA_CONFERENCE, session.getConferenceId());
		intent.putExtra(BaseActivity.IA_SESSION, session.getId());
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		TreeSet<Session> selectedSessions = new TreeSet<Session>(new SessionComparator());
		ConferenceServer server = getConferenceServer();
		Trackable[] markedSessions = getProfileManager().getMarkedSessions(getUser());
		boolean hasExpiredSession = false;
		for (Trackable id : markedSessions) {
			try {
				Session session = server.getSession(id.sessionId, id.conferenceId);
				if (session != null) {
					if (session.isExpired()) {
						hasExpiredSession = true;
					} else {
						selectedSessions.add(session);
					}
				}
			}
			catch (Exception e) {
				Log.v(XCS.LOG.COMMUNICATE, "No marked session with id " + id);
			}
		}
		if (hasExpiredSession) {
			getProfileManager().pruneMarked();
		}
		sessions = selectedSessions.toArray(new Session[selectedSessions.size()]);
		SessionAdapter adapter = new SessionAdapter(this, R.layout.session_item, R.layout.mandatory_item, sessions);
		adapter.setIncludeDate(true);
		ListView sessionList = (ListView) findViewById(R.id.sessionList);
		sessionList.setAdapter(adapter);
		super.onResume();
	}
}