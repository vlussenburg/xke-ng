package com.xebia.xcoss.axcv.tasks;

import android.content.Context;

import com.github.droidfu.concurrent.BetterAsyncTask;
import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.ConferenceViewerApplication;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.logic.cache.DataCache;
import com.xebia.xcoss.axcv.model.Credential;
import com.xebia.xcoss.axcv.util.SecurityUtils;
import com.xebia.xcoss.axcv.util.XCS;

public abstract class CVTask<ParameterT, ProgressT, ReturnT> extends BetterAsyncTask<ParameterT, ProgressT, ReturnT> {

	private String action;
	private ReturnT result;
	private ConferenceViewerApplication application;

	public CVTask(int action, BaseActivity ctx) {
		super(ctx);
		this.application = (ConferenceViewerApplication) ctx.getApplication();
		this.action = ctx.getString(action);
		useCustomDialog(XCS.DIALOG.WAITING);
	}

	@Override
	protected ReturnT doCheckedInBackground(Context ctx, ParameterT... params) throws Exception {
		validateLogin();
		return background(ctx, params);
	};
	
	protected abstract ReturnT background(Context ctx, ParameterT... params) throws Exception;

	@Override
	protected void after(Context ctx, ReturnT result) {
		this.result = result;
		((BaseActivity) ctx).notifyTaskFinished();
	}

	@Override
	protected void handleError(Context ctx, Exception e) {
		// TODO Move from BaseActivity to this class
		BaseActivity.handleException(ctx, action, e);
	}

	private void validateLogin() {
		if (!RestClient.isAuthenticated()) {
			String requestUrl = getRequestUrl("/login");
			String decrypt = SecurityUtils.decrypt(application.getPassword());
			Credential credential = new Credential(application.getUser(), decrypt);
			RestClient.postObject(requestUrl.toString(), credential, void.class, null);
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

	public ReturnT getResult() {
		return result;
	}

	public DataCache getStorage() {
		return application.getStorage();
	}
}
