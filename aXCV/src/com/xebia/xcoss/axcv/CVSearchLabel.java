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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.ui.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class CVSearchLabel extends BaseActivity {

	private List<String> selectedLabels;
	private ArrayAdapter<String> listAdapter;
	private AutoCompleteTextView view;
	private Set<String> allLabels;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.search_items);
		view = (AutoCompleteTextView) findViewById(R.id.ssa_text);
		ListView labelList = (ListView) findViewById(R.id.ssa_list);
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

				if (keyCode != KeyEvent.KEYCODE_ENTER || StringUtil.isEmpty(name)) {
					return false;
				}

				if (view.isPopupShowing() && view.getListSelection() != ListView.INVALID_POSITION) {
					name = (String) view.getAdapter().getItem(view.getListSelection());
				}
				if (!addLabel(name)) {
					storeLabel(name);
				}

				view.getText().clear();
				return true;
			}

		});

		listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, selectedLabels);
		labelList.setAdapter(listAdapter);
		labelList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int i, long l) {
				listAdapter.remove(listAdapter.getItem(i));
				Toast.makeText(getApplicationContext(), "Label removed", Toast.LENGTH_SHORT).show();
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
		listAdapter.notifyDataSetChanged();
		return allLabels.contains(name);
	}

	private void storeLabel(final String name) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
			.setTitle("Add label...")
			.setMessage("Create the new label '" + name + "'?")
			.setIcon(R.drawable.x_conference)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					ConferenceServer.getInstance().createLabel(name);
					allLabels.add(name);
					dialog.dismiss();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					selectedLabels.remove(name);
					listAdapter.notifyDataSetChanged();
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
	public void onBackPressed() {
		addLabel(view.getText().toString());
		Intent result = new Intent();
		result.putExtra(IA_LABELS, (Serializable) selectedLabels);
		setResult(RESULT_OK, result);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
}
