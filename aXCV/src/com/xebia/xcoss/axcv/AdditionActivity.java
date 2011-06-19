package com.xebia.xcoss.axcv;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public abstract class AdditionActivity extends BaseActivity implements OnCancelListener, OnDismissListener {

	protected static final int ACTIVITY_SEARCH_AUTHOR = 938957;
	protected static final int ACTIVITY_SEARCH_LABEL = 938958;

	private Intent authorIntent;
	private Intent labelIntent;

	public abstract void updateField(int field, Object selection, boolean state);

	abstract protected void onTextClick(int field);

	protected Intent getAuthorIntent() {
		if (authorIntent == null) {
			authorIntent = new Intent(this, CVSearchAuthor.class);
		}
		return authorIntent;
	}

	protected Intent getLabelIntent() {
		if (labelIntent == null) {
			labelIntent = new Intent(this, CVSearchLabel.class);
		}
		return labelIntent;
	}

	protected void activateViews(int[] identifiers) {
		AddOnTouchListener touchListener = new AddOnTouchListener();
		AddOnClickListener clickListener = new AddOnClickListener();
		Drawable drawable = getResources().getDrawable(R.drawable.touchtext_disable);

		for (int i = 0; i < identifiers.length; i++) {
			TextView view = (TextView) findViewById(identifiers[i]);
			if (view != null) {
				view.setOnTouchListener(touchListener);
				view.setOnClickListener(clickListener);
				view.setBackgroundDrawable(drawable);
			}
		}
	}
	
	protected void passivateView(int id) {
		Drawable drawable = getResources().getDrawable(R.drawable.touchtext_disable);
		TextView view = (TextView) findViewById(id);
		view.setBackgroundDrawable(drawable);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	protected class DialogHandler implements DialogInterface.OnClickListener,
			DialogInterface.OnMultiChoiceClickListener {
		private int field;
		private Object[] items;
		private AdditionActivity activity;

		public DialogHandler(AdditionActivity activity, Object[] items, int field) {
			this.activity = activity;
			this.items = items;
			this.field = field;
		}

		public void onClick(DialogInterface dialog, int item) {
			onClick(dialog, item, true);
		}

		@Override
		public void onClick(DialogInterface dialog, int item, boolean state) {
			activity.updateField(field, items[item], state);
		}
	}

	private class AddOnTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			Drawable drawable = null;
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_POINTER_DOWN:
					drawable = getResources().getDrawable(R.drawable.touchtext);
				break;
				default:
				break;
			}
			view.setBackgroundDrawable(drawable);
			// Need to return false to handle click event
			return false;
		}
	}

	private class AddOnClickListener implements OnClickListener {
		@Override
		public void onClick(View view) {
			onTextClick(view.getId());
		}

	}

}
