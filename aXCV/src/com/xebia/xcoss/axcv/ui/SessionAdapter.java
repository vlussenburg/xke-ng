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
import com.xebia.xcoss.axcv.model.Session;

public class SessionAdapter extends BaseAdapter {

	private Activity ctx;
	private int viewResource;
	private Session[] data;
	private ScreenTimeUtil timeUtil;

	public SessionAdapter(Activity context, int viewResourceId, Conference conference) {
		this.ctx = context;
		this.viewResource = viewResourceId;
		this.data = conference.getSessions().toArray(new Session[0]);
		timeUtil = new ScreenTimeUtil(context);
	}

	@Override
	public View getView(int paramInt, View paramView, ViewGroup parent) {

		Session session = (Session) getItem(paramInt);
		int colorId = ctx.getResources().getColor(R.color.tc_itemdefault);
		boolean now = timeUtil.isCurrent(session.getStartTime(), session.getEndTime());
//		if (timeUtil.isHistory(session.getEndDate())) {
//			colorId = ctx.getResources().getColor(R.color.tc_itemgone);
//		} else if (now) {
//			colorId = ctx.getResources().getColor(R.color.tc_itemactive);
//		}

		LayoutInflater inflater = ctx.getLayoutInflater();
		View row = inflater.inflate(viewResource, parent, false);

		TextView titleView = (TextView) row.findViewById(R.id.ses_title);
		TextView authorView = (TextView) row.findViewById(R.id.ses_author);
		TextView labelView = (TextView) row.findViewById(R.id.ses_labels);
		TextView locDateView = (TextView) row.findViewById(R.id.ses_locdate);

		titleView.setText(session.getTitle());
		authorView.setText("Author: " + session.getAuthor());
		labelView.setText("Labels: " + getLabels(session));
		locDateView.setText(getLocationAndDate(session));

		titleView.setTextColor(colorId);

		if ( now ) {
			titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);
		}
		return row;
	}

	private String getLabels(Session session) {
		// TODO Auto-generated method stub
		return null;
	}

	private CharSequence getLocationAndDate(Session session) {
		// TODO Auto-generated method stub
		return null;
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
