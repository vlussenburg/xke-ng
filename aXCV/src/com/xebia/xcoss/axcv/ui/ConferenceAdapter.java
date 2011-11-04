package com.xebia.xcoss.axcv.ui;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Moment;

public class ConferenceAdapter extends BaseAdapter {

	private Activity ctx;
	private int viewResource;
	private Conference[] data;
	private ScreenTimeUtil timeFormatter;

	public ConferenceAdapter(Activity context, int viewResourceId, Conference[] conferences) {
		this.ctx = context;
		this.viewResource = viewResourceId;
		this.data = conferences;
		this.timeFormatter = new ScreenTimeUtil(ctx);
	}

	@Override
	public View getView(int paramInt, View paramView, ViewGroup parent) {

		boolean sameDay = false;
		Conference cfr = (Conference) getItem(paramInt);
		int colorId = ctx.getResources().getColor(R.color.tc_itemdefault);
		Moment dt = cfr.getStartTime();
		if (dt.isBeforeToday()) {
			colorId = ctx.getResources().getColor(R.color.tc_itemgone);
		} else if ( !dt.isAfterToday() ) {
			sameDay = true;
			colorId = ctx.getResources().getColor(R.color.tc_itemactive);
		}

		LayoutInflater inflater = ctx.getLayoutInflater();
		View row = inflater.inflate(viewResource, parent, false);

		TextView titleView = (TextView) row.findViewById(R.id.cnf_title);
		TextView whenView = (TextView) row.findViewById(R.id.cnf_when);
		TextView statusView = (TextView) row.findViewById(R.id.cnf_status);
		TextView dateView = (TextView) row.findViewById(R.id.cnf_date);

		whenView.setText(timeFormatter.getRelativeDate(cfr.getStartTime()));
		dateView.setText(timeFormatter.getAbsoluteDate(cfr.getStartTime()));
		titleView.setText(cfr.getTitle());
		statusView.setText(ConferenceStatus.getStatus(cfr));

		titleView.setTextColor(colorId);
		whenView.setTextColor(colorId);
		statusView.setTextColor(colorId);
		dateView.setTextColor(colorId);

		if ( sameDay ) {
			titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);
			whenView.setTypeface(whenView.getTypeface(), Typeface.BOLD);
		}
		return row;
	}

	@Override
	public int getCount() {
		return data.length;
	}

	@Override
	public Object getItem(int paramInt) {
		return data[paramInt];
	}

	@Override
	public long getItemId(int paramInt) {
		return paramInt;
	}
}
