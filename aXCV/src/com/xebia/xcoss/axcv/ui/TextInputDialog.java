package com.xebia.xcoss.axcv.ui;

import android.app.Dialog;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.xebia.xcoss.axcv.CVSessionAdd;
import com.xebia.xcoss.axcv.R;

public class TextInputDialog extends Dialog {

	private CVSessionAdd activity;
	private int identifier;

	public TextInputDialog(CVSessionAdd activity, int id) {
		super(activity);
		this.activity = activity;
		this.identifier = id;
		init();
	}

	private void init() {
		setContentView(R.layout.dialog_textinput);

		LayoutParams params = getWindow().getAttributes();
		params.width = LayoutParams.FILL_PARENT;
		getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

		setTitle("Enter value");

		Button submit = (Button) findViewById(R.id.seCommit);
		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				TextView text = (TextView) findViewById(R.id.seValue);
				String result = text.getText().toString();
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
