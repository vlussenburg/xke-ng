package com.xebia.xcoss.axcv;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.xebia.xcoss.axcv.util.XCS;

public class CVSplashLoader extends BaseActivity {

	private boolean loaded = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.loader);

		if ( !loaded ) {
	        DataRetriever task = new DataRetriever(this);
	        task.execute();
		}
	}
	
	@Override
	protected void onSuccess() {
		loaded = true;
        startActivity(new Intent(this, CVConferences.class));
	};
	
	@Override
	protected void onFailure() {
        finish();
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem miSettings = menu.add(0, XCS.MENU.SETTINGS, Menu.NONE, R.string.menu_settings);
		miSettings.setIcon(R.drawable.menu_add);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
			startActivity(new Intent(this, CVConferences.class));
			return true;
		}
		return super.onTouchEvent(event);
	}
}