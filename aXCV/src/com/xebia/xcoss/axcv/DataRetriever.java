package com.xebia.xcoss.axcv;

import java.util.List;

import android.app.Dialog;
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

	private ProgressDialog dialog;
	private BaseActivity ctx;

	public DataRetriever(BaseActivity ctx) {
		this.ctx = ctx;
	}

	@Override
	protected void onPreExecute() {
		this.dialog = ProgressDialog.show(ctx, null, "Loading. Please wait...", true, true);
		this.dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				Dialog errorDialog = ctx.createDialog("Cancelled",
						"The initial loading was cancelled. The application will be closed.");
				errorDialog.setOnDismissListener(new Dialog.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface di) {
						ctx.onFailure();
					}
				});
				errorDialog.show();
			}
		});
	}

	@Override
	protected Boolean doInBackground(String... arg0) {
		try {
			ConferenceServer server = ctx.getConferenceServer();
			if ( server != null ) {
				List<Conference> conferences = server.getUpcomingConferences(1);
				for (Conference conference : conferences) {
					conference.getSessions();
				}
				return true;
			}
		}
		catch (DataException e) {
			if (e.missing()) {
				Log.e(XCS.LOG.COMMUNICATE, "[Initial load] Wrong server or server not running.");
			} else if (e.denied()) {
				Log.e(XCS.LOG.COMMUNICATE, "[Initial load] Invalid credentials specified.");
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
			dialog.dismiss();

			if (result) {
				// Note, authentication may still be invalid.
				ctx.onSuccess();
			} else {
				Dialog errorDialog = ctx.createDialog("Error", "Connection to server failed ("+BaseActivity.lastError+").");
				errorDialog.setOnDismissListener(new Dialog.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface di) {
						ctx.onFailure();
						di.dismiss();
					}
				});
				errorDialog.show();
			}
		}
		catch (Exception e) {
			Log.e(XCS.LOG.DATA, "Failure during initial load: " + StringUtil.getExceptionMessage(e));
			e.printStackTrace();
			ctx.onFailure();
		}
	}
}
