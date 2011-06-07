package com.xebia.xcoss.axcv.logic;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class ConferenceRetriever extends AsyncTask<String, Void, String> {

	// http://appfulcrum.com/?p=126

	private ProgressDialog dialog;
	private Context ctx;

	public ConferenceRetriever(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	protected void onPreExecute() {
		this.dialog = ProgressDialog.show(ctx, "Loading...", "Conferences", true);
	}

	@Override
	protected String doInBackground(String... arg0) {
		ConferenceServer.getInstance().loadConferences(arg0[0], null);
		return "Result";
	}

	@Override
	protected void onPostExecute(String result) {
		dialog.cancel();
	}
}
