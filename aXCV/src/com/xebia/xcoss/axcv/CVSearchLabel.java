package com.xebia.xcoss.axcv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.ui.LabelAdapter;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class CVSearchLabel extends BaseActivity {

	private List<String> selectedLabels;
	private LabelAdapter labelAdapter;
	private AutoCompleteTextView view;
	private Set<String> allLabels;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.search_items);
		view = (AutoCompleteTextView) findViewById(R.id.ssa_text);
		final String startText = getResources().getString(R.string.default_input_text);
		view.setSelection(0, startText.length());
		initSelectedLabels();

		// Fill the list of options
		allLabels = new TreeSet<String>();
		String[] labels = getConferenceServer().getLabels();
		String[] data = new String[labels.length];

		for (int i = 0; i < labels.length; i++) {
			data[i] = labels[i];
			allLabels.add(labels[i]);
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, data);
		view.setAdapter(adapter);
		view.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				String name = view.getText().toString();
				if (startText.equals(name)) {
					view.getText().clear();
				}

				if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
				
				if (!StringUtil.isEmpty(name)) {
					if (view.isPopupShowing() && view.getListSelection() != ListView.INVALID_POSITION) {
						name = (String) view.getAdapter().getItem(view.getListSelection());
					}
					if (!addLabel(name)) {
						storeLabel(name);
					}

					view.getText().clear();
				}
				return true;
			}

		});

		super.onCreate(savedInstanceState);
	}

	private boolean addLabel(String name) {
		if (StringUtil.isEmpty(name)) {
			return true;
		}
		boolean contains = false;
		for (String sname : selectedLabels) {
			if (name.equals(sname)) {
				contains = true;
			}
		}
		if (!contains) {
			selectedLabels.add(name);
			Collections.sort(selectedLabels);
		}
		labelAdapter.notifyDataSetChanged();
		return allLabels.contains(name);
	}

	private void storeLabel(final String name) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Add label...").setMessage("Create the new label '" + name + "'?")
				.setIcon(R.drawable.x_conference).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						getConferenceServer().createLabel(name);
						allLabels.add(name);
						dialog.dismiss();
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						selectedLabels.remove(name);
						labelAdapter.notifyDataSetChanged();
						dialog.dismiss();
					}
				});
		AlertDialog create = builder.create();
		create.show();
	}

	private void initSelectedLabels() {
		try {
			Bundle extras = getIntent().getExtras();
			selectedLabels = (List<String>) extras.getSerializable(IA_LABELS);
		}
		catch (Exception e) {
			Log.w(XCS.LOG.COMMUNICATE, "No labels loaded: " + e.getMessage());
			selectedLabels = new ArrayList<String>();
		}
	}

	@Override
	protected void onResume() {
		initSelectedLabels();
		labelAdapter = new LabelAdapter(this, R.layout.author_item_small, selectedLabels);
		ListView labelList = (ListView) findViewById(R.id.ssa_list);
		labelList.setAdapter(labelAdapter);
		super.onResume();
	}
	
	@Override
	public void onBackPressed() {
		addLabel(view.getText().toString());
		Intent result = new Intent();
		result.putExtra(IA_LABELS, (Serializable) selectedLabels);
		setResult(RESULT_OK, result);
		finish();
	}

	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		int position = menuItem.getGroupId();
		
		switch (menuItem.getItemId()) {
			case R.id.remove:
				selectedLabels.remove(position);
				labelAdapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "Label removed", Toast.LENGTH_SHORT).show();
				return true;
			default:
				return super.onContextItemSelected(menuItem);
		}		
	}
}
