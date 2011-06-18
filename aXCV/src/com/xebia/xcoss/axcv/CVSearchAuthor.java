package com.xebia.xcoss.axcv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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

import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.ui.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class CVSearchAuthor extends BaseActivity {

	private List<Author> selectedAuthors;
	private ArrayAdapter<Author> listAdapter;
	private AutoCompleteTextView view;
	private HashMap<String, Author> allAuthors;
	private boolean singleMode;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.search_items);
		view = (AutoCompleteTextView) findViewById(R.id.ssa_text);
		ListView authorList = (ListView) findViewById(R.id.ssa_list);
		final String startText = getResources().getString(R.string.default_input_text);
		view.setSelection(0, startText.length());
		initSelectedAuthors();

		// Fill the list of options
		allAuthors = new HashMap<String, Author>();
		Author[] allPersons = getConferenceServer().getAllAuthors();
		String[] data = new String[allPersons.length];
		for (int i = 0; i < allPersons.length; i++) {
			data[i] = allPersons[i].getName();
			allAuthors.put(data[i], allPersons[i]);
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, data);
		view.setAdapter(adapter);
		view.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				String authorName = view.getText().toString();
				if (startText.equals(authorName)) {
					view.getText().clear();
				}

				if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
				if (StringUtil.isEmpty(authorName)) return true;

				if (view.isPopupShowing() && view.getListSelection() != ListView.INVALID_POSITION) {
					authorName = (String) view.getAdapter().getItem(view.getListSelection());
				}

				if (!addAuthor(authorName)) {
					Toast.makeText(getApplicationContext(), "Select a valid author!", Toast.LENGTH_SHORT).show();
					return true;
				}
				view.getText().clear();
				return true;
			}

		});

		listAdapter = new ArrayAdapter<Author>(this, android.R.layout.simple_list_item_1, selectedAuthors);
		authorList.setAdapter(listAdapter);
		authorList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int i, long l) {
				listAdapter.remove(listAdapter.getItem(i));
				Toast.makeText(getApplicationContext(), "Author removed", Toast.LENGTH_SHORT).show();
			}
		});
		super.onCreate(savedInstanceState);
	}

	private boolean addAuthor(String authorName) {
		if (StringUtil.isEmpty(authorName)) {
			return true;
		}
		if (allAuthors.containsKey(authorName)) {
			final Author author = allAuthors.get(authorName);
			boolean contains = false;
			for (Author a : selectedAuthors) {
				if (author.compareTo(a) == 0) {
					contains = true;
				}
			}
			if (!contains) {
				if (singleMode && selectedAuthors.size() > 0) {
					Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Single select!");
					builder.setMessage("You cannot select more than one person! What shall I do with the previously selected one?");
					builder.setIcon(android.R.drawable.ic_dialog_alert);
					builder.setPositiveButton("Replace", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int i) {
							selectedAuthors.clear();
							selectedAuthors.add(author);
							listAdapter.notifyDataSetChanged();
							dialog.dismiss();
						}
					});
					builder.setNegativeButton("Keep", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int i) {
							dialog.dismiss();
						}
					});
					builder.create().show();
				} else {
					selectedAuthors.add(author);
					Collections.sort(selectedAuthors);
					listAdapter.notifyDataSetChanged();
				}
			}
			return true;
		}
		return false;
	}

	private void initSelectedAuthors() {
		if (selectedAuthors == null) {
			selectedAuthors = new ArrayList<Author>();
		}
		try {
			Bundle extras = getIntent().getExtras();
			selectedAuthors.clear();
			selectedAuthors.addAll((List<Author>) extras.getSerializable(IA_AUTHORS));
			singleMode = extras.getBoolean("singleSelectionMode", false);
		}
		catch (Exception e) {
			Log.w(XCS.LOG.COMMUNICATE, "No authors loaded: " + e.getMessage());
		}
	}

	@Override
	protected void onResume() {
		initSelectedAuthors();
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		addAuthor(view.getText().toString());
		Intent result = new Intent();
		result.putExtra(IA_AUTHORS, (Serializable) selectedAuthors);
		setResult(RESULT_OK, result);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
}
