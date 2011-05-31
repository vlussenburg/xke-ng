package com.xebia.xcoss.axcv;

import java.util.Set;

import hirondelle.date4j.DateTime;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.ConferenceList;
import com.xebia.xcoss.axcv.ui.ConferenceAdapter;
import com.xebia.xcoss.axcv.util.XCS;

public class CVConferences extends BaseActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conferences);
		
		DateTime dt = DateTime.today(XCS.TZ);
		String year = String.valueOf(dt.getYear());
		ConferenceList collection = getConferenceServer().getConferences();
		final Conference[] conferences = collection.getConferencesAsList(year);
		ConferenceAdapter adapter = new ConferenceAdapter(this, R.layout.conference_item, conferences);
		ListView conferencesList = (ListView) findViewById(R.id.conferencesList);
		conferencesList.setAdapter(adapter);
		conferencesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int paramInt, long paramLong) {
				// Adapter = listview, view = tablelayout.
				switchTo(conferences[paramInt]);
			}
			
		});
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		if (sp.getBoolean(XCS.PREF.JUMPTOFIRST, true)) {
			Log.i(XCS.LOG.NAVIGATE, "Jumping to first upcomming conference.");
			switchTo(getConferenceServer().getConferences().getUpcomingConference());
		}
		
		TextView title = (TextView) findViewById(R.id.conferencesTitle);
		title.setText(title.getText() + " " + year);
	}

	private void switchTo(Conference conference) {
		Intent intent = new Intent(this, CVSessionList.class);
		intent.putExtra(BaseActivity.IA_CONFERENCE, conference.getId());
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.removeItem(XCS.MENU.OVERVIEW);
		menu.removeItem(XCS.MENU.EDIT);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == XCS.MENU.ADD) {
			// TODO : Conference addition
			startActivity(new Intent(this, CVSettings.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}