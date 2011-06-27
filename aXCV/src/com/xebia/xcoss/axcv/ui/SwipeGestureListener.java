package com.xebia.xcoss.axcv.ui;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.xebia.xcoss.axcv.SwipeActivity;

public class SwipeGestureListener extends SimpleOnGestureListener {

	private int swipeMinDistance;
	private int swipeThresholdVelocity;
	private SwipeActivity activity;

	public SwipeGestureListener(SwipeActivity swipeable) {
		ViewConfiguration vc = ViewConfiguration.get(swipeable);
		swipeMinDistance = vc.getScaledTouchSlop();
		swipeThresholdVelocity = vc.getScaledMinimumFlingVelocity();
		activity = swipeable;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (e1 == null || e2 == null) return false;
		float deltaX = e1.getX() - e2.getX();
		float deltaY = e1.getY() - e2.getY();

		if (Math.abs(deltaX) > Math.abs(deltaY)) {
			// Left/right swipe
			if (Math.abs(velocityX) > swipeThresholdVelocity && Math.abs(deltaX) > swipeMinDistance) {
				if (deltaX > 0)
					activity.onSwipeRightToLeft();
				else
					activity.onSwipeLeftToRight();
				return true;
			}
		} else {
			// Up/down swipe
			if (Math.abs(velocityY) > swipeThresholdVelocity && Math.abs(deltaY) > swipeMinDistance) {
				if (deltaY > 0)
					activity.onSwipeBottomToTop();
				else
					activity.onSwipeTopToBottom();
				return true;
			}
		}
		return false;
	}

}
