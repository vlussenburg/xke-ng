package com.xebia.xcoss.axcv.tasks;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.model.Session;

public class DeleteSessionTask extends CVTask<Session, Void, Boolean> {

	public DeleteSessionTask(int action, BaseActivity ctx, TaskCallBack<Boolean> callback) {
		super(action, ctx, callback);
	}
	
	@Override
	protected Boolean background(Context context, Session... sessions) throws Exception {
		for (Session session : sessions) {
			String requestUrl = getRequestUrl("/session/", session.getId());
			getStorage().remove(session);
			RestClient.deleteObject(requestUrl);
		}
		return true;
	}
}
