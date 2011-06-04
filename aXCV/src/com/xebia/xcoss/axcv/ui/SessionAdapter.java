package com.xebia.xcoss.axcv.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.MandatorySession;
import com.xebia.xcoss.axcv.model.Session;

public class SessionAdapter extends BaseAdapter {

	private Activity ctx;
	private int viewResource;
	private int alternativeViewResource;
	private Session[] data;
	private ScreenTimeUtil timeUtil;

	public SessionAdapter(Activity context, int viewResourceId, int altViewResourceId, Conference conference) {
		this.ctx = context;
		this.viewResource = viewResourceId;
		this.alternativeViewResource = altViewResourceId;
		this.data = conference.getSessions().toArray(new Session[0]);
		timeUtil = new ScreenTimeUtil(context);
	}

	@Override
	public View getView(int paramInt, View paramView, ViewGroup parent) {

		Session session = (Session) getItem(paramInt);
		int colorId = ctx.getResources().getColor(R.color.tc_itemdefault);
		boolean now = timeUtil.isNow(session.getStartTime(), session.getEndTime());
//		if (timeUtil.isHistory(session.getEndDate())) {
//			colorId = ctx.getResources().getColor(R.color.tc_itemgone);
//		} else if (now) {
//			colorId = ctx.getResources().getColor(R.color.tc_itemactive);
//		}

		if ( session instanceof MandatorySession ) {
			return constructMandatoryView(parent, session, colorId);
		}
		return constructSessionView(parent, session, colorId);
	}

	private View constructSessionView(ViewGroup parent, Session session, int colorId) {

		LayoutInflater inflater = ctx.getLayoutInflater();
		View row = inflater.inflate(viewResource, parent, false);

		TextView titleView = (TextView) row.findViewById(R.id.ses_title);
		TextView authorView = (TextView) row.findViewById(R.id.ses_author);
		TextView labelView = (TextView) row.findViewById(R.id.ses_labels);
		TextView locDateView = (TextView) row.findViewById(R.id.ses_locdate);

		titleView.setText(session.getTitle());
		titleView.setTextColor(colorId);
//			titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);

		String authors = FormatUtil.getList(session.getAuthors());
		if ( StringUtil.isEmpty(authors) ) {
			authorView.setVisibility(View.GONE);
		} else {
			authorView.setText("Author: " + authors);
		}

		String labels = FormatUtil.getList(session.getLabels());
		if ( StringUtil.isEmpty(labels) ) {
			labelView.setVisibility(View.GONE);
		} else {
			labelView.setText("Labels: " + labels);
		}
		
		locDateView.setText(getLocationAndDate(session));
		return row;
	}

	private View constructMandatoryView(ViewGroup parent, Session session, int colorId) {

		LayoutInflater inflater = ctx.getLayoutInflater();
		View row = inflater.inflate(alternativeViewResource, parent, false);

		TextView titleView = (TextView) row.findViewById(R.id.ses_title);
		TextView locDateView = (TextView) row.findViewById(R.id.ses_locdate);

		titleView.setText(session.getTitle());
		titleView.setTextColor(colorId);
//			titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);

		locDateView.setText(getLocationAndDate(session));
		return row;
	}

	private CharSequence getLocationAndDate(Session session) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(session.getLocation().getLocation());
		sb.append(" | ");
		sb.append(timeUtil.getAbsoluteTime(session.getStartTime()));
		sb.append(" - ");
		sb.append(timeUtil.getAbsoluteTime(session.getEndTime()));
		return sb.toString();
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
