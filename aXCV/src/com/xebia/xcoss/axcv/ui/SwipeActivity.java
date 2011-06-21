package com.xebia.xcoss.axcv.ui;

import android.view.WindowManager;


public interface SwipeActivity {

	public void onSwipeBottomToTop();

	public void onSwipeLeftToRight();

	public void onSwipeRightToLeft();

	public void onSwipeTopToBottom();
	
	public WindowManager getWindowManager();
}
