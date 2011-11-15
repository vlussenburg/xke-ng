package com.xebia.xcoss.axcv.ui;

import android.graphics.Typeface;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xebia.xcoss.axcv.CVConferences;
import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Moment;

public class ConferenceAdapter extends BaseAdapter {

	private CVConferences ctx;
	private int viewResource;
	private Conference[] data;
	private ScreenTimeUtil timeFormatter;
	private View[] views;

	public ConferenceAdapter(CVConferences context, int viewResourceId, Conference[] conferences) {
		views = new View[conferences.length];
		this.ctx = context;
		this.viewResource = viewResourceId;
		this.data = conferences;
		this.timeFormatter = new ScreenTimeUtil(ctx);
	}

	@Override
	public View getView(final int paramInt, View paramView, ViewGroup parent) {
		if (views[paramInt] != null)
			return views[paramInt];

		final Conference cfr = (Conference) getItem(paramInt);
		boolean sameDay = false;
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

		row.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ctx.switchTo(paramInt);
			}
		});
		row.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				menu.setHeaderTitle(cfr.getTitle());
				menu.add(paramInt, R.id.edit, Menu.NONE, R.string.context_menu_session_edit);
				menu.add(paramInt, R.id.view, Menu.NONE, R.string.context_menu_session_view);
			}
		});

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
		views[paramInt]=row;
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
