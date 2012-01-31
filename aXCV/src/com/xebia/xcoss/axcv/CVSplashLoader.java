package com.xebia.xcoss.axcv;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import com.xebia.xcoss.axcv.service.NotificationServiceManager;

public class CVSplashLoader extends BaseActivity {

	private Timer timer;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.splashloader);
		super.onCreate(savedInstanceState);
		new NotificationServiceManager().onSignal(this);
		
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				navigateToStartscreen();
			}
		}, 1000);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			navigateToStartscreen();
			return true;
		}
		return super.onTouchEvent(event);
	}

	private void navigateToStartscreen() {
		// Cancel the timer.
		timer.cancel();
		
		// Check if the application is triggered by a notification
		NotificationType type = (NotificationType) getIntent().getSerializableExtra(IA_NOTIFICATION_TYPE);
		getIntent().removeExtra(IA_NOTIFICATION_TYPE);

		if ( type != null && type == NotificationType.TRACKED) {
			startActivity(new Intent(this, CVTrack.class));
		} else {
			startActivity(new Intent(this, CVConferences.class));
		}
	};

}