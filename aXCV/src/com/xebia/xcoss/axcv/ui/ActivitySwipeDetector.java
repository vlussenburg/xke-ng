package com.xebia.xcoss.axcv.ui;

import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import com.xebia.xcoss.axcv.util.XCS;

public class ActivitySwipeDetector implements View.OnTouchListener {

	private static final int WIDTH_DIVIDER = 2;
	private static final int HEIGHT_DIVIDER = 4;
	
	private SwipeActivity activity;
	private float downX, downY, upX, upY;
	private int minHeightDistance;
	private int minWidthDistance;

	public ActivitySwipeDetector(SwipeActivity activity) {
		Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
		Log.v(XCS.LOG.SWIPE, "Screen = " + defaultDisplay.getHeight() + "/" + defaultDisplay.getWidth());
		this.minHeightDistance = defaultDisplay.getHeight() / HEIGHT_DIVIDER;
		this.minWidthDistance = defaultDisplay.getWidth() / WIDTH_DIVIDER;
		this.activity = activity;
	}

	public void onRightToLeftSwipe() {
		Log.i(XCS.LOG.SWIPE, "RightToLeftSwipe!");
		activity.onSwipeRightToLeft();
	}

	public void onLeftToRightSwipe() {
		Log.i(XCS.LOG.SWIPE, "LeftToRightSwipe!");
		activity.onSwipeLeftToRight();
	}

	public void onTopToBottomSwipe() {
		Log.i(XCS.LOG.SWIPE, "onTopToBottomSwipe!");
		activity.onSwipeTopToBottom();
	}

	public void onBottomToTopSwipe() {
		Log.i(XCS.LOG.SWIPE, "onBottomToTopSwipe!");
		activity.onSwipeBottomToTop();
	}

	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				downX = event.getX();
				downY = event.getY();
				return true;
			}
			case MotionEvent.ACTION_UP: {
				upX = event.getX();
				upY = event.getY();

				float deltaX = downX - upX;
				float deltaY = downY - upY;

				// swipe horizontal?
				if (Math.abs(deltaX) > minWidthDistance) {
					// left or right
					if (deltaX < 0) {
						this.onLeftToRightSwipe();
						return true;
					}
					if (deltaX > 0) {
						this.onRightToLeftSwipe();
						return true;
					}
				} else {
					Log.i(XCS.LOG.SWIPE, "Swipe was only " + Math.abs(deltaX) + " long, need at least "
							+ minWidthDistance);
				}

				// swipe vertical?
				if (Math.abs(deltaY) > minHeightDistance) {
					// top or down
					if (deltaY < 0) {
						this.onTopToBottomSwipe();
						return true;
					}
					if (deltaY > 0) {
						this.onBottomToTopSwipe();
						return true;
					}
				} else {
					Log.i(XCS.LOG.SWIPE, "Swipe was only " + Math.abs(deltaX) + " long, need at least "
							+ minHeightDistance);
				}

				return true;
			}
		}
		return false;
	}

}
