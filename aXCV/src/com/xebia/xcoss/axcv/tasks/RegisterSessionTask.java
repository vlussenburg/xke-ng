package com.xebia.xcoss.axcv.tasks;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class RegisterSessionTask extends CVTask<Session, Void, Boolean> {

	public RegisterSessionTask(int action, BaseActivity ctx, TaskCallBack<Boolean> callback) {
		super(action, ctx, callback);
		useCustomDialog(XCS.DIALOG.WAITING);
	}
	
	@Override
	protected Boolean background(Context context, Session... sessions) throws Exception {
		return createUpdateSessions(this, sessions);
	}

	protected static Boolean createUpdateSessions(CVTask<?,?,?> task, Session... sessions) {
		for (Session session : sessions) {
			String conferenceId = session.getConferenceId();
			if ( StringUtil.isEmpty(session.getId()) ) {
				String requestUrl = task.getRequestUrl("/conference/", session.getConferenceId(), "/session");
				session = RestClient.createObject(requestUrl, session, Session.class);
				// Server does not return conferenceId, so extracted before creation.
			} else {
				String requestUrl = task.getRequestUrl("/session/", session.getId());
				RestClient.updateObject(requestUrl, session);
			}
			task.getStorage().add(conferenceId, session);
		}
		return true;
	}
}
