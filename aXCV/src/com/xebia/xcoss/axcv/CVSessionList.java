package com.xebia.xcoss.axcv;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.ConferenceList;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.ui.SessionAdapter;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class CVSessionList extends BaseActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule);

		Conference conference = null;
		Object object = getIntent().getExtras().getSerializable("conference");
		if (object instanceof Conference) {
			conference = (Conference) object;
			Log.w(LOG.ALL, "Conference from stream " + conference.getTitle());
		} else {
			conference = ConferenceList.getInstance().getUpcomingConference();
			Log.w(LOG.ALL, "Conference default " + conference.getTitle());
		}
		TextView title = (TextView) findViewById(R.id.conferenceTitle);
		title.setText(conference.getTitle());

		TextView date = (TextView) findViewById(R.id.conferenceDate);
		String val = new ScreenTimeUtil(this).getAbsoluteDate(conference.getDate());
		date.setText(val);

		Log.w(LOG.ALL, "Conference has " + conference.getSessions().size() + " sessions.");
		SessionAdapter adapter = new SessionAdapter(this, R.layout.session_item, conference);
		ListView sessionList = (ListView) findViewById(R.id.sessionList);
		sessionList.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == XCS.MENU.ADD) {
			// TODO : Session addition
			startActivity(new Intent(this, CVSettings.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}