package com.xebia.xcoss.axcv;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.service.NotificationService;
import com.xebia.xcoss.axcv.service.NotificationServiceManager;
import com.xebia.xcoss.axcv.util.XCS;

public class CVSplashLoader extends BaseActivity {

	private boolean loaded = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.loader);
		super.onCreate(savedInstanceState);
		// Ignore terminated; this is the main screen

		if (!loaded) {
			DataRetriever task = new DataRetriever(this);
			task.execute();
		}
	}

	@Override
	public void onBackPressed() {
		// Reset the credentials and force authentication next start
		ConferenceServer.close();
		super.onBackPressed();
	}

	@Override
	protected void onSuccess() {
		loaded = true;
		new NotificationServiceManager().onSignal(this);
		navigateToStartscreen();
	}

	@Override
	protected void onFailure() {
		finish();
	};

	@Override
	protected void onDestroy() {
		getProfileManager().closeConnection();
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// Called upon manually creating an exit intent
		getIntent().fillIn(intent, Intent.FILL_IN_DATA);
		super.onNewIntent(intent);
	}

	@Override
	protected void onRestart() {
		// When revived, check for an exit code
		if (getIntent().getBooleanExtra("exit", false)) {
			ConferenceServer.close();
			finish();
		}
		super.onRestart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem miSettings = menu.add(0, XCS.MENU.SETTINGS, Menu.NONE, R.string.menu_settings);
		miSettings.setIcon(android.R.drawable.ic_menu_preferences);

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
		if (loaded && event.getAction() == MotionEvent.ACTION_UP) {
			navigateToStartscreen();
			return true;
		}
		return super.onTouchEvent(event);
	}

	private void navigateToStartscreen() {
		// Use getConferenceServer to explicitly reissue the login
		if (getConferenceServer().isLoggedIn()) {
			startActivity(new Intent(this, CVConferences.class));
		} else {
			Dialog dialog = createDialog("Warning", "Authentication failed. Please set credentials.");
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface paramDialogInterface) {
					startActivity(new Intent(CVSplashLoader.this, CVSettings.class));
				}
			});
			dialog.show();
		}
	};

}