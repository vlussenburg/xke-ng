package com.xebia.xcoss.axcv;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.xebia.xcoss.axcv.util.XCS;

public class CVSplashLoader extends Activity {

	private boolean loaded = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.loader);

		ProgressDialog dialog = ProgressDialog.show(CVSplashLoader.this, "", "Loading. Please wait...", true);

		// Do the loading stuff

		loaded = true;
		dialog.dismiss();
		showHomeScreen();
	}

	private void showHomeScreen() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		boolean jump = sp.getBoolean(XCS.PREF.JUMPTOFIRST, false);

		if (jump) {
			startActivity(new Intent(this, CVSettings.class));
		} else {
			startActivity(new Intent(this, CVConferences.class));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem miSettings = menu.add(0, XCS.MENU.SETTINGS, Menu.NONE, R.string.menu_settings);
		miSettings.setIcon(R.drawable.menu_add);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
			case XCS.MENU.SETTINGS:
				startActivity(new Intent(this, CVSettings.class));
			break;
		}
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if ( loaded && event.getAction() == MotionEvent.ACTION_UP ) {
			showHomeScreen();
			return true;
		}
		return super.onTouchEvent(event);
	}
}