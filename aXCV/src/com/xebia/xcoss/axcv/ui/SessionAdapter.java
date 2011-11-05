package com.xebia.xcoss.axcv.ui;

import android.content.Intent;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.CVSessionAdd;
import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class SessionAdapter extends BaseAdapter {

	private BaseActivity ctx;
	private int viewResource;
	private int alternativeViewResource;
	private Session[] data;
	private ScreenTimeUtil timeUtil;
	private boolean includeDate = false;

	public SessionAdapter(BaseActivity context, int viewResourceId, int altViewResourceId, Session[] data) {
		this.ctx = context;
		this.viewResource = viewResourceId;
		this.alternativeViewResource = altViewResourceId;
		this.data = data;
		timeUtil = new ScreenTimeUtil(context);
	}

	@Override
	public View getView(int paramInt, View paramView, ViewGroup parent) {
		Session session = (Session) getItem(paramInt);
		int colorId = ctx.getResources().getColor(R.color.tc_itemdefault);
		if (session.isExpired()) {
			colorId = ctx.getResources().getColor(R.color.tc_itemgone);
		} else if (session.isRunning()) {
			colorId = ctx.getResources().getColor(R.color.tc_itemactive);
		}
		return session.isMandatory() ? 
				constructMandatoryView(parent, session, colorId) : 
				constructSessionView(parent, session, colorId);
	}

	private View constructSessionView(ViewGroup parent, final Session session, int colorId) {

		LayoutInflater inflater = ctx.getLayoutInflater();
		View row = inflater.inflate(viewResource, parent, false);

		TextView titleView = (TextView) row.findViewById(R.id.ses_title);
		TextView authorView = (TextView) row.findViewById(R.id.ses_author);
		TextView labelView = (TextView) row.findViewById(R.id.ses_labels);
		TextView locDateView = (TextView) row.findViewById(R.id.ses_locdate);

		titleView.setText(session.getTitle());
		titleView.setTextColor(colorId);

		String authors = FormatUtil.getList(session.getAuthors(), false);
		if (StringUtil.isEmpty(authors)) {
			authorView.setVisibility(View.GONE);
			row.findViewById(R.id.ses_author_label).setVisibility(View.GONE);
		} else {
			authorView.setText(authors);
		}

		String labels = FormatUtil.getList(session.getLabels(), false);
		if (StringUtil.isEmpty(labels)) {
			labelView.setVisibility(View.GONE);
			row.findViewById(R.id.ses_labels_label).setVisibility(View.GONE);
		} else {
			labelView.setText(labels);
			Linkify.addLinks(labelView, XCS.TAG.PATTERN, XCS.TAG.LINK);
			labelView.setFocusable(false);
		}

		locDateView.setText(getLocationAndDate(session));

		ImageView button;
		button = (ImageView) row.findViewById(R.id.markButton);
		if (StringUtil.isEmpty(ctx.getUser())) {
			button.setVisibility(View.GONE);
		} else {
			button.setVisibility(View.VISIBLE);
			ctx.markSession(session, button, false);
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ctx.markSession(session, view, true);
				}
			});
		}
		button = (ImageView) row.findViewById(R.id.editButton);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				editSession(session);
			}
		});

		return row;
	}

	private View constructMandatoryView(ViewGroup parent, Session session, int colorId) {

		LayoutInflater inflater = ctx.getLayoutInflater();
		View row = inflater.inflate(alternativeViewResource, parent, false);

		TextView titleView = (TextView) row.findViewById(R.id.ses_title);
		TextView locDateView = (TextView) row.findViewById(R.id.ses_locdate);

		titleView.setText(session.getTitle());
		titleView.setTextColor(colorId);
		// titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);

		locDateView.setText(getLocationAndDate(session));
		return row;
	}

	private void editSession(Session session) {
		Intent intent = new Intent(ctx, CVSessionAdd.class);
		intent.putExtra(BaseActivity.IA_CONFERENCE, ctx.getConference().getId());
		intent.putExtra(BaseActivity.IA_SESSION, session.getId());
		ctx.startActivity(intent);
	}

	private CharSequence getLocationAndDate(Session session) {

		StringBuilder sb = new StringBuilder();
		if (includeDate) {
			sb.append(timeUtil.getAbsoluteDate(session.getStartTime()));
			sb.append(" | ");
		}
		sb.append(session.getLocation().getDescription());
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

	public void setIncludeDate(boolean state) {
		includeDate = state;
	}
}
