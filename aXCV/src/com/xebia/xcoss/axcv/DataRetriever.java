package com.xebia.xcoss.axcv;

import java.util.List;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;

import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.logic.DataException;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class DataRetriever extends AsyncTask<String, Void, Boolean> {

	// http://appfulcrum.com/?p=126

	private ProgressDialog processDialog;
	private BaseActivity ctx;
	private volatile ConferenceServer server;
	
	public DataRetriever(BaseActivity ctx) {
		this.ctx = ctx;
	}

	@Override
	protected void onPreExecute() {
		processDialog = ProgressDialog.show(ctx, null, ctx.getString(R.string.loading), true, true);
		processDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				ctx.onFailure(ctx.getString(R.string.cancelled),
						ctx.getString(R.string.cancel_during_load));
			}
		});
	}

	protected void stop() {
		if (processDialog.isShowing()) {
			processDialog.dismiss();
		}
	}

	@Override
	protected Boolean doInBackground(String... arg0) {
		try {
			server = ctx.getConferenceServer();
			if (server != null) {
				List<Conference> conferences = server.getUpcomingConferences(1);
				for (Conference conference : conferences) {
					conference.getSessions();
				}
				return true;
			}
		}
		catch (DataException e) {
			if (e.denied()) {
				Log.e(XCS.LOG.COMMUNICATE, "[Initial load] Invalid credentials specified.");
			} else  if (e.missing()) {
				Log.e(XCS.LOG.COMMUNICATE, "[Initial load] Wrong server or server not running.");
			} else {
				Log.e(XCS.LOG.COMMUNICATE, "[Initial load] Communication error: " + e.getMessage());
			}
		}
		catch (Exception e) {
			Log.e(XCS.LOG.COMMUNICATE, "[Initial load] Failure: " + StringUtil.getExceptionMessage(e), e);
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {

		try {
			processDialog.dismiss();

			if (result) {
				ctx.onSuccess();
				return;
			}
			
			String error = BaseActivity.lastError;
			if ( ctx.getConferenceServer().isLoggedIn() ) {
				ctx.onFailure(ctx.getString(R.string.error), ctx.getString(R.string.connection_to_server_failed, error));
			} else {
				ctx.onAuthenticationFailed("startup");
			}
		}
		catch (Exception e) {
			Log.e(XCS.LOG.DATA, "Failure during initial load: " + StringUtil.getExceptionMessage(e), e);
			ctx.onFailure(ctx.getString(R.string.error), ctx.getString(R.string.connection_to_server_failed, StringUtil.getExceptionMessage(e)));
		}
	}
}
