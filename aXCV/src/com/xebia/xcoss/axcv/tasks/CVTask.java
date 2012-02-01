package com.xebia.xcoss.axcv.tasks;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.github.droidfu.concurrent.BetterAsyncTask;
import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.CVSettings;
import com.xebia.xcoss.axcv.ConferenceViewerApplication;
import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.logic.DataException;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.logic.cache.DataCache;
import com.xebia.xcoss.axcv.model.Credential;
import com.xebia.xcoss.axcv.util.DebugUtil;
import com.xebia.xcoss.axcv.util.SecurityUtils;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public abstract class CVTask<ParameterT, ProgressT, ReturnT> extends BetterAsyncTask<ParameterT, ProgressT, ReturnT> {

	private String action;
	private ConferenceViewerApplication application;
	private TaskCallBack<ReturnT> callback;
	private boolean silent = false;

	public CVTask(int action, BaseActivity ctx, TaskCallBack<ReturnT> callback) {
		super(ctx);
		this.application = (ConferenceViewerApplication) ctx.getApplication();
		this.action = ctx.getString(action);
		this.callback = callback;
		disableDialog();
		Log.w(XCS.LOG.COMMUNICATE, "Task created: " + getClass().getSimpleName());
//		DebugUtil.showCallStack();
	}

	@Override
	protected ReturnT doCheckedInBackground(Context ctx, ParameterT... params) throws Exception {
		validateLogin();
		ReturnT t = background(ctx, params);
		return t;
	};

	protected abstract ReturnT background(Context ctx, ParameterT... params) throws Exception;

	@Override
	protected void after(Context ctx, ReturnT result) {
		if (callback != null) {
			callback.onCalled(result);
		}
	}

	public CVTask<ParameterT,ProgressT, ReturnT> silent() {
		this.silent = true;
		return this;
	}

	public CVTask<ParameterT,ProgressT, ReturnT> showProgress() {
		useCustomDialog(XCS.DIALOG.WAITING);
		return this;
	}
	
	@Override
	protected void handleError(final Context ctx, Exception e) {
		// Do not throw exception here. It will block the async task waiting dialog.
		if (callback != null) {
			try {
				callback.onCalled(null);
			}
			catch (Exception ex) {
				Log.w(XCS.LOG.COMMUNICATE, "Processing error callback failed: " + StringUtil.getExceptionMessage(ex));
			}
		}
		String msg = null;
		if (e instanceof DataException) {
			if (((DataException) e).missing()) {
				msg = ctx.getString(R.string.server_missing_url, action);
			} else if (((DataException) e).networkError()) {
				msg = ctx.getString(R.string.server_unreachable, action);
			} else if (((DataException) e).timedOut()) {
				msg = ctx.getString(R.string.server_timeout, action);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
				builder.setTitle("Not allowed!")
						.setMessage("Access for " + action + " is denied. Specify credentials?")
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
								ctx.startActivity(new Intent(ctx, CVSettings.class));
							}
						}).setNegativeButton("Continue", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
								RestClient.logout();
							}
						});
				AlertDialog alertDialog = builder.create();
				alertDialog.show();
			}
			if (!silent && msg != null) {
				Log.w(XCS.LOG.COMMUNICATE, msg);
				BaseActivity.createDialog(ctx, "Action failed", msg).show();
			}
			return;
		}
		if (!silent) {
			msg = "Communication failure on '" + action + "' due to " + StringUtil.getExceptionMessage(e);
			Log.w(XCS.LOG.COMMUNICATE, msg);
			BaseActivity.createDialog(ctx, "Action failed", msg).show();
		}
	}

	private void validateLogin() {
		if (!RestClient.isAuthenticated()) {
			String requestUrl = getRequestUrl("/login");
			String decrypt = SecurityUtils.decrypt(application.getPassword());
			Credential credential = new Credential(application.getUser(), decrypt);
			RestClient.postObject(requestUrl, credential, void.class);
		}
	}

	protected String getRequestUrl(String... parms) {
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(application.getServerUrl());
		for (String elem : parms) {
			requestUrl.append(elem);
		}
		return requestUrl.toString();
	}

	public DataCache getStorage() {
		return application.getCache();
	}
}
