package com.xebia.xcoss.axcv;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.xebia.xcoss.axcv.ui.SwipeGestureListener;

public abstract class SwipeActivity extends BaseActivity {

	private GestureDetector gestureDetector;

	protected void addGestureDetection(int viewId) {
		gestureDetector = new GestureDetector(new SwipeGestureListener(this));
		gestureDetector.setIsLongpressEnabled(false);
		View.OnTouchListener gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		};
		ViewGroup group = ((ViewGroup) findViewById(viewId));
		assignGestureListener(group, gestureListener);
	}

	private void assignGestureListener(ViewGroup group, OnTouchListener listener) {
		group.setOnTouchListener(listener);
		// This is needed to detect the swipe
		group.setClickable(true);
		int count = group.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = group.getChildAt(i);
			if (child instanceof ViewGroup) {
				assignGestureListener((ViewGroup) child, listener);
			} else {
				child.setOnTouchListener(listener);
				// Do not make the elements clickable. It shows (on texts)
				// child.setClickable(true);
			}
		}
	}

	abstract public void onSwipeBottomToTop();
	abstract public void onSwipeLeftToRight();
	abstract public void onSwipeRightToLeft();
	abstract public void onSwipeTopToBottom();
}
