package com.xebia.xcoss.axcv.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xebia.xcoss.axcv.AdditionActivity;
import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.util.StringUtil;

public class LocationInputDialog extends Dialog {

	private AdditionActivity activity;
	private int identifier;

	public LocationInputDialog(AdditionActivity activity, int id) {
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
		setContentView(R.layout.dialog_locationinput);

		LayoutParams params = getWindow().getAttributes();
		params.width = LayoutParams.FILL_PARENT;
		getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

		setTitle("Enter new location");

		Button submit = (Button) findViewById(R.id.seCommit);
		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				int color = activity.getResources().getColor(R.color.tc_itemdefault);
				((TextView) findViewById(R.id.seDescription)).setTextColor(color);
				((TextView) findViewById(R.id.seRoomSizeLabel)).setTextColor(color);

				TextView tv1 = (TextView) findViewById(R.id.seValue);
				String name = tv1.getText().toString().trim();
				if ( StringUtil.isEmpty(name) ) {
					((TextView) findViewById(R.id.seDescription)).setTextColor(Color.RED);
					tv1.requestFocus();
					return;
				}
				
				TextView tv2 = (TextView) findViewById(R.id.seRoomSize);
				String size = tv2.getText().toString().trim();
				if ( StringUtil.isEmpty(size) ) {
					((TextView) findViewById(R.id.seRoomSizeLabel)).setTextColor(Color.RED);
					tv2.requestFocus();
					return;
				}

				Location selection = new Location(0, name);
				selection.setCapacity(Integer.parseInt(size));
				activity.updateField(identifier, selection, true);
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
