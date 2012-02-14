package com.xebia.xcoss.axcv.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.xebia.xcoss.axcv.SwipeActivity;

public class SwipeLayout extends FrameLayout {

    private MotionEvent mCurrentDownEvent;
    private MotionEvent mCurrentUpEvent;
    private static final int swipeMinDistance = 50;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;
	private SwipeActivity activity;

	public SwipeLayout(Context context) {
		super(context);
		init();
	}

	public SwipeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setBackgroundColor(android.R.color.transparent);
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	public void setGestureListener(SwipeActivity activity) {
		this.activity = activity;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (processTouchEvent(ev)) {
			return true;
		}
		return super.onInterceptTouchEvent(ev);
	}

    /**
     * Analyzes the given motion event and if applicable triggers the
     * appropriate callbacks on the {@link OnGestureListener} supplied.
     *
     * @param ev The current motion event.
     * @return true if the {@link OnGestureListener} consumed the event,
     *              else false.
     */
    public boolean processTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        boolean handled = false;

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mCurrentDownEvent = MotionEvent.obtain(ev);
            break;

        case MotionEvent.ACTION_UP:
            mCurrentUpEvent = MotionEvent.obtain(ev);
            // A fling must travel the minimum tap distance
            final VelocityTracker velocityTracker = mVelocityTracker;
            // Number of pixels per 100 milliseconds
            velocityTracker.computeCurrentVelocity(100);
            final float velocityY = velocityTracker.getYVelocity();
            final float velocityX = velocityTracker.getXVelocity();
            
            if ((Math.abs(velocityY) > ViewConfiguration.getMinimumFlingVelocity())
                    || (Math.abs(velocityX) > ViewConfiguration.getMinimumFlingVelocity())){
                handled = onFling(mCurrentDownEvent, mCurrentUpEvent, velocityX, velocityY);
            }
            mVelocityTracker.recycle();
            mVelocityTracker = null;
            break;
        case MotionEvent.ACTION_CANCEL:
            mVelocityTracker.recycle();
            mVelocityTracker = null;
            break;
        }
        return handled;
    }

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		float deltaX = e1.getX() - e2.getX();
		float deltaY = e1.getY() - e2.getY();

		if (Math.abs(deltaX) > Math.abs(deltaY)) {
			// Left/right swipe
			if (Math.abs(deltaX) > swipeMinDistance) {
				if (deltaX > 0)
					activity.onSwipeRightToLeft();
				else
					activity.onSwipeLeftToRight();
				return true;
			}
		} else {
			// Up/down swipe
			if (Math.abs(deltaY) > swipeMinDistance) {
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
