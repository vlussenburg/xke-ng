package com.xebia.xcoss.axcv;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.xebia.xcoss.axcv.ui.SwipeGestureListener;

public class SwipeActivity extends BaseActivity {

	private GestureDetector gestureDetector;

	protected void addGestureDetection(int viewId) {
		gestureDetector = new GestureDetector(new SwipeGestureListener(this));
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
		int count = group.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = group.getChildAt(i);
			if (child instanceof ViewGroup) {
				assignGestureListener((ViewGroup) child, listener);
			}
			child.setOnTouchListener(listener);
		}
		group.setOnTouchListener(listener);
	}

	public void onSwipeBottomToTop() {
		Toast.makeText(this, "Top Swipe", Toast.LENGTH_SHORT).show();
	}

	public void onSwipeLeftToRight() {
		Toast.makeText(this, "Right Swipe", Toast.LENGTH_SHORT).show();
	}

	public void onSwipeRightToLeft() {
		Toast.makeText(this, "Left Swipe", Toast.LENGTH_SHORT).show();
	}

	public void onSwipeTopToBottom() {
		Toast.makeText(this, "Bottom Swipe", Toast.LENGTH_SHORT).show();
	}
}
