package com.xebia.xcoss.axcv;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.service.NotificationServiceManager;
import com.xebia.xcoss.axcv.util.XCS;

public class CVSplashLoader extends BaseActivity {

	private boolean loaded = false;
	private DataRetriever task;
	private Dialog errorDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.splashloader);
		super.onCreate(savedInstanceState);
		
		if (!loaded) {
			task = new DataRetriever(this);
			task.execute();
		}
	}

	@Override
	protected void onStop() {
		task.stop();
		if ( errorDialog != null && errorDialog.isShowing() ) {
			errorDialog.dismiss();
		}
		super.onStop();
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
	protected void onFailure(String message, String detail) {
		errorDialog = createDialog(message, detail);
		errorDialog.setOnDismissListener(new Dialog.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface di) {
				finish();
			}
		});
		errorDialog.show();
	};

	@Override
	protected void onDestroy() {
		if (isFinishing()) {
			Log.i(XCS.LOG.NAVIGATE, "Closing application.");
			closeProfileManager();
			ConferenceServer.close();
		}
		super.onDestroy();
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
			NotificationType type = (NotificationType) getIntent().getSerializableExtra(IA_NOTIFICATION_TYPE);
			// Do this only once
			getIntent().removeExtra(IA_NOTIFICATION_TYPE);
			if ( type != null && type == NotificationType.TRACKED) {
				startActivity(new Intent(this, CVTrack.class));
			} else {
				startActivity(new Intent(this, CVConferences.class));
			}
		} else {
			Dialog dialog = createDialog(getString(R.string.warning), getString(R.string.auth_failed));
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