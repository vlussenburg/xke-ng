package com.xebia.xcoss.axcv;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.model.Session;
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
				markSession(sessions[paramInt], view, true);
			}
		});
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		List<Session> selectedSessions = new ArrayList<Session>();
		ConferenceServer server = getConferenceServer();
		int[] markedSessions = getProfileManager().getMarkedSessionIds(getUser());
		boolean hasExpiredSession = false;
		DateTime today = DateTime.today(XCS.TZ);
		for (int i : markedSessions) {
			try {
				Session session = server.getSession(i);
				if (session != null) {
					// session.isExpired() works also on the day itself
					if (today.gt(session.getDate())) {
						hasExpiredSession = true;
					} else {
						selectedSessions.add(session);
					}
				}
			}
			catch (Exception e) {
				Log.v(XCS.LOG.COMMUNICATE, "No marked session with id " + i);
			}
		}
		if ( hasExpiredSession ) {
			getProfileManager().pruneMarked(today);
		}
		sessions = selectedSessions.toArray(new Session[selectedSessions.size()]);
		SessionAdapter adapter = new SessionAdapter(this, R.layout.session_item, R.layout.mandatory_item, sessions);
		adapter.setIncludeDate(true);
		ListView sessionList = (ListView) findViewById(R.id.sessionList);
		sessionList.setAdapter(adapter);
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.removeItem(XCS.MENU.ADD);
		menu.removeItem(XCS.MENU.EDIT);
		menu.removeItem(XCS.MENU.TRACK);
		return true;
	}
}