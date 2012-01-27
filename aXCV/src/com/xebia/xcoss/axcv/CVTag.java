package com.xebia.xcoss.axcv;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.xebia.xcoss.axcv.util.XCS;

public class CVTag extends SearchActivity {

	private String searchTag = "";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		searchTag = getIntent().getDataString().replace(XCS.TAG.LINK, "").trim();
		TextView view = (TextView) findViewById(R.id.searchTitle);
		view.setText(searchTag);

		findViewById(R.id.searchBlock).setVisibility(View.GONE);
		findViewById(R.id.firstDivider).setVisibility(View.GONE);
	}
}