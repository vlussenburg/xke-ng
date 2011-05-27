package com.xebia.xcoss.axcv;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.ConferenceList;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public abstract class BaseActivity extends Activity {
	
	public static final String IA_CONFERENCE = "ID-conference";
	public static final String IA_SESSION = "ID-session";
	
	private MenuItem miSettings;
	private MenuItem miSearch;
	private MenuItem miList;
	private MenuItem miAdd;
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	miAdd = menu.add(0, XCS.MENU.ADD, Menu.NONE, R.string.menu_add);
    	miList = menu.add(0, XCS.MENU.OVERVIEW, Menu.NONE, R.string.menu_overview);
    	miSettings = menu.add(0, XCS.MENU.SETTINGS, Menu.NONE, R.string.menu_settings);
    	miSearch = menu.add(0, XCS.MENU.SEARCH, Menu.NONE, R.string.menu_search);
    	
    	miAdd.setIcon(R.drawable.ic_menu_add);
    	miSettings.setIcon(R.drawable.ic_menu_agenda);
    	miSearch.setIcon(R.drawable.ic_btn_search);
    	miList.setIcon(R.drawable.menu_list);

    	return true;
    }  
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
			case XCS.MENU.SETTINGS:
				startActivity(new Intent(this, CVSettings.class));
				break;
			case XCS.MENU.ADD:
				break;
			case XCS.MENU.OVERVIEW:
				startActivity(new Intent(this, CVConferences.class));
				break;
			case XCS.MENU.SEARCH:
				break;
		}
		return true;
	}

	protected Conference getConference() {
		Conference conference = null;
		int identifier = -1;
		try {
			identifier = getIntent().getExtras().getInt(IA_CONFERENCE);
			conference = ConferenceList.getInstance().findConferenceById(identifier);
		} catch (Exception e) {
			Log.w(LOG.ALL, "No conference with ID " + identifier);
		}
		if ( conference == null ) {
			conference = ConferenceList.getInstance().getUpcomingConference();
			Log.w(LOG.ALL, "Conference default " + conference.getTitle());
		}
		return conference;
	}

	protected Session getSession(Conference conference) {
		Session session = null;
		int identifier = -1;
		try {
			identifier = getIntent().getExtras().getInt(IA_SESSION);
			session = conference.getSessionById(identifier);
		} catch (Exception e) {
			Log.w(LOG.ALL, "No session with ID " + identifier + " or conference not found.");
		}
		if ( session == null ) {
			Log.w(LOG.ALL, "Conference default : " + (conference == null ? "NULL" : conference.getTitle()));
			session = conference.getUpcomingSession();
			Log.w(LOG.ALL, "Session default " + (session == null ? "NULL" : session.getTitle()));
		}
		return session;
	}

}