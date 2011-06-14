package com.xebia.xcoss.axcv.ui;

import hirondelle.date4j.DateTime;
import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.util.XCS;

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

		Conference cfr = (Conference) getItem(paramInt);
		int colorId = ctx.getResources().getColor(R.color.tc_itemdefault);
		DateTime now = DateTime.today(XCS.TZ);
		boolean sameDayAs = cfr.getDate().isSameDayAs(now);
		if (cfr.getDate().isInThePast(XCS.TZ)) {
			colorId = ctx.getResources().getColor(R.color.tc_itemgone);
		}
		// Don't use else, since it is also regarded as being in the past
		if (sameDayAs) {
			colorId = ctx.getResources().getColor(R.color.tc_itemactive);
		}

		LayoutInflater inflater = ctx.getLayoutInflater();
		View row = inflater.inflate(viewResource, parent, false);

		TextView titleView = (TextView) row.findViewById(R.id.cnf_title);
		TextView whenView = (TextView) row.findViewById(R.id.cnf_when);
		TextView statusView = (TextView) row.findViewById(R.id.cnf_status);
		TextView dateView = (TextView) row.findViewById(R.id.cnf_date);

		whenView.setText(timeFormatter.getRelativeDate(cfr.getDate()));
		dateView.setText(timeFormatter.getAbsoluteDate(cfr.getDate()));
		titleView.setText(cfr.getTitle());
		statusView.setText(ConferenceStatus.getStatus(cfr));

		titleView.setTextColor(colorId);
		whenView.setTextColor(colorId);
		statusView.setTextColor(colorId);
		dateView.setTextColor(colorId);

		if ( sameDayAs ) {
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
