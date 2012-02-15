package com.xebia.xcoss.axcv.ui;

import android.app.Dialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.xebia.xcoss.axcv.AdditionActivity;
import com.xebia.xcoss.axcv.R;

public class TextInputDialog extends Dialog {

	private AdditionActivity activity;
	private int identifier;

	public TextInputDialog(AdditionActivity activity, int id) {
		super(activity);
		this.activity = activity;
		this.identifier = id;
		init();
		setSingleLine();
	}

	private void setSingleLine() {
		final Button submit = (Button) findViewById(R.id.seCommit);
		TextView text = (TextView) findViewById(R.id.seValue);
		text.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent e) {
				if (keyCode == KeyEvent.KEYCODE_ENTER ) {
					submit.performClick();
					return true;
				}
				return false;
			}
		});
	}

	private void init() {
		setContentView(R.layout.dialog_textinput);

		LayoutParams params = getWindow().getAttributes();
		params.width = LayoutParams.FILL_PARENT;
		getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

		setTitle(R.string.set_value);

		Button submit = (Button) findViewById(R.id.seCommit);
		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				TextView text = (TextView) findViewById(R.id.seValue);
				String result = text.getText().toString().trim();
				activity.updateField(identifier, result, true);
				dismiss();
			}
		});
		Button cancel = (Button) findViewById(R.id.seCancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				dismiss();
			}
		});
	}

	public void setDescription(String value) {
		TextView text = (TextView) findViewById(R.id.seDescription);
		text.setText(value);
	}

	public void setValue(String value) {
		TextView text = (TextView) findViewById(R.id.seValue);
		text.setText(value);
	}

}
