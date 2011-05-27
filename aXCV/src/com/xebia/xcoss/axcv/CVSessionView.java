package com.xebia.xcoss.axcv;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class CVSessionView extends BaseActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.session);

		Conference conference = getConference();
		Session session = getSession(conference);
		TextView title = (TextView) findViewById(R.id.conferenceTitle);
		title.setText(conference.getTitle());

		TextView date = (TextView) findViewById(R.id.conferenceDate);
		ScreenTimeUtil timeUtil = new ScreenTimeUtil(this);
		String val = timeUtil.getAbsoluteDate(conference.getDate());
		date.setText(val);
		
		TextView sessionDate = (TextView) findViewById(R.id.sessionTime);
		StringBuilder sb = new StringBuilder();
		sb.append(timeUtil.getAbsoluteTime(session.getStartTime()));
		sb.append(" - ");
		sb.append(timeUtil.getAbsoluteTime(session.getEndTime()));
		sessionDate.setText(sb.toString());
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