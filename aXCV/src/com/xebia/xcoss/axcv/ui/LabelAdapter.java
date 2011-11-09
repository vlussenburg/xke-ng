package com.xebia.xcoss.axcv.ui;

import java.util.List;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.R;

public class LabelAdapter extends BaseAdapter {

	private List<String> label;
	private BaseActivity ctx;
	private int viewResource;
	
	public LabelAdapter(BaseActivity context, int viewResourceId, List<String> data) {
		this.ctx = context;
		this.viewResource = viewResourceId;
		this.label = data;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final String label = (String) getItem(position);
		LayoutInflater inflater = ctx.getLayoutInflater();
		View row = inflater.inflate(viewResource, parent, false);
		TextView titleView = (TextView) row.findViewById(R.id.author_name);
		row.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				createContextMenu(menu, v, menuInfo, position, label);
			}
		});
		titleView.setText(label);
		return row;
	}
	
	
	protected void createContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo, int position, String label) {
		menu.setHeaderTitle(label);
		menu.add(position, R.id.remove, Menu.NONE, R.string.context_menu_label_remove);
	}

	@Override
	public int getCount() {
		return label.size();
	}

	@Override
	public Object getItem(int position) {
		return label.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
