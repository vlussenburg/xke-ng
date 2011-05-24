package com.xebia.xcoss.axcv;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.xebia.xcoss.axcv.util.XCS;

public abstract class BaseActivity extends Activity {
	
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
				break;
			case XCS.MENU.SEARCH:
				break;
		}
		return true;
	}
}