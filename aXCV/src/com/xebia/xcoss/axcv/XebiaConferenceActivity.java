package com.xebia.xcoss.axcv;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;

public abstract class XebiaConferenceActivity extends Activity {
	
	private static final int MENU_SETTINGS = Menu.FIRST;
	private static final int MENU_SEARCH   = Menu.FIRST+1;
	private static final int MENU_OVERVIEW = Menu.FIRST+2;
	private static final int MENU_ADD = Menu.FIRST+3;
	
	private static final String PREFERENCES = "XCV-prefs";
	private MenuItem miSettings;
	private MenuItem miSearch;
	private MenuItem miList;
	private MenuItem miAdd;
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	miAdd = menu.add(0, MENU_ADD, Menu.NONE, R.string.menu_add);
    	miList = menu.add(0, MENU_OVERVIEW, Menu.NONE, R.string.menu_overview);
    	miSettings = menu.add(0, MENU_SETTINGS, Menu.NONE, R.string.menu_settings);
    	miSearch = menu.add(0, MENU_SEARCH, Menu.NONE, R.string.menu_search);
    	
    	miAdd.setIcon(R.drawable.menu_add);
    	miSettings.setIcon(R.drawable.menu_add);
    	miSearch.setIcon(R.drawable.menu_search);
    	miList.setIcon(R.drawable.menu_list);
    	
    	enableMenu(false);
    	
    	return true;
    }  
    
    protected void enableMenu(boolean state) {
    	miAdd.setEnabled(state);
    	miSearch.setEnabled(state);
    	miList.setEnabled(state);
	}

	public SharedPreferences getSettings() {
    	return getSharedPreferences(PREFERENCES, MODE_PRIVATE);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
			case MENU_SETTINGS:
				startActivity(new Intent(this, CVSettings.class));
				break;
			case MENU_ADD:
				break;
			case MENU_OVERVIEW:
				break;
			case MENU_SEARCH:
				break;
		}
		return true;
	}
}