package com.xebia.xcoss.axcv.tasks;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS;

public class RetrieveSessionTask extends CVTask<String, Void, Session> {

	/**
	 * This might make the cached conference invalid...
	 * 
	 * @param action
	 * @param ctx
	 * @param callback
	 */
	public RetrieveSessionTask(int action, BaseActivity ctx, TaskCallBack<Session> callback) {
		super(action, ctx, callback);
		useCustomDialog(XCS.DIALOG.WAITING);
	}

	@Override
	protected Session background(Context context, String... ids) throws Exception {
		if ( ids.length < 1 ) {
			return null;
		}
		Session session = getStorage().getSession(ids[0]);
		if ( session == null ) {
			String requestUrl = getRequestUrl("/session/", ids[0]);
			session = RestClient.loadObject(requestUrl, Session.class);
			if ( ids.length > 1 ) {
				session.setConferenceId(ids[1]);
				getStorage().add(ids[1], session);
			}
		}
		return session;
	}
}
