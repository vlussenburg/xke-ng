package com.xebia.xcoss.axcv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.xebia.xcoss.axcv.tasks.RetrieveLabelsTask;
import com.xebia.xcoss.axcv.tasks.TaskCallBack;
import com.xebia.xcoss.axcv.ui.LabelAdapter;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class CVSearchLabel extends BaseActivity {

	private List<String> selectedLabels;
	private LabelAdapter labelAdapter;
	private AutoCompleteTextView textView;
	private Set<String> allLabels;
	private String startText;
	private Intent result = new Intent();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.search_items);

		allLabels = new TreeSet<String>();
		new RetrieveLabelsTask(R.string.action_retrieve_labels, this, new TaskCallBack<List<String>>() {
			@Override
			public void onCalled(List<String> labels) {
				if (labels != null) {
					updateAllLabels(labels);
				}
			}
		}).showProgress().execute();

		startText = getResources().getString(R.string.default_input_text);
		textView = (AutoCompleteTextView) findViewById(R.id.ssa_text);
		textView.setSelection(0, startText.length());
		textView.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
				CVSearchLabel.this.updateTypedText();
				return true;
			}
		});
		initSelectedLabels();

		Button closeButton = (Button) findViewById(R.id.ssa_close);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateTypedText();
				finish();
			}
		});

		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		initSelectedLabels();
		labelAdapter = new LabelAdapter(this, R.layout.author_item_small, selectedLabels);
		ListView labelList = (ListView) findViewById(R.id.ssa_list);
		labelList.setAdapter(labelAdapter);
		super.onResume();
	}

	public void updateAllLabels(List<String> labels) {
		allLabels.addAll(labels);
		String[] labellist = allLabels.toArray(new String[allLabels.size()]);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, labellist);
		textView.setAdapter(adapter);
	}

	protected void updateTypedText() {
		String name = textView.getText().toString();
		if (startText.equals(name)) {
			textView.getText().clear();
			name = null;
		}

		if (!StringUtil.isEmpty(name)) {
			if (textView.isPopupShowing() && textView.getListSelection() != ListView.INVALID_POSITION) {
				name = (String) textView.getAdapter().getItem(textView.getListSelection());
			}
			if (!addLabel(name)) {
				// Seems the server will add the label upon updating a session
				// TODO storeLabel(name);
			}
			textView.getText().clear();
			updateResult();
		}
	}

	private void updateResult() {
		result.putExtra(IA_LABELS, (Serializable) selectedLabels);
		setResult(RESULT_OK, result);
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

	// private void storeLabel(final String name) {
	// AlertDialog.Builder builder = new AlertDialog.Builder(this);
	// builder.setTitle("Add label...").setMessage("Create the new label '" + name + "'?")
	// .setIcon(R.drawable.x_conference).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int id) {
	// getConferenceServer().createLabel(name);
	// allLabels.add(name);
	// dialog.dismiss();
	// }
	// }).setNegativeButton("No", new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int id) {
	// selectedLabels.remove(name);
	// labelAdapter.notifyDataSetChanged();
	// dialog.dismiss();
	// }
	// });
	// AlertDialog create = builder.create();
	// create.show();
	// }

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
		updateTypedText();
		super.onBackPressed();
	}

	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		int position = menuItem.getGroupId();

		switch (menuItem.getItemId()) {
			case R.id.remove:
				selectedLabels.remove(position);
				updateResult();
				labelAdapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), R.string.label_removed, Toast.LENGTH_SHORT).show();
				return true;
			default:
				return super.onContextItemSelected(menuItem);
		}
	}
}
