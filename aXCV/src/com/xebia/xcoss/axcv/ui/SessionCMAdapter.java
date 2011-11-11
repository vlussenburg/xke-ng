package com.xebia.xcoss.axcv.ui;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.View;

import com.xebia.xcoss.axcv.CVSessionList;
import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.model.Session;

public class SessionCMAdapter extends SessionAdapter {

	private CVSessionList ctx;

	public SessionCMAdapter(CVSessionList context, int viewResourceId, int altViewResourceId, Session[] data) {
		super(context, viewResourceId, altViewResourceId, data);
		this.ctx = context;
	}

	@Override
	public void registerClicks(View row, final int paramInt, final Session session) {
		row.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ctx.switchTo(paramInt);
			}
		});
		row.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				menu.setHeaderTitle(session.getTitle());
				menu.add(paramInt, R.id.edit, Menu.NONE, R.string.context_menu_session_edit);
				menu.add(paramInt, R.id.view, Menu.NONE, R.string.context_menu_session_view);
			}
		});
	}
}
