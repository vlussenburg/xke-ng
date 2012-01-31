package com.xebia.xcoss.axcv.tasks;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Session;

public class DeleteConferenceTask extends CVTask<Conference, Void, Boolean> {

	// TODO Should be on true
	private boolean moveSessions = false;
	
	public DeleteConferenceTask(int action, BaseActivity ctx, TaskCallBack<Boolean> callback) {
		super(action, ctx, callback);
	}
	
	@Override
	protected Boolean background(Context context, Conference... conferences) throws Exception {
		for (Conference conference : conferences) {
			if ( moveSessions) {
				for (Session session : conference.getSessions()) {
					// TODO Rescheduling of sessions contained no yet supported
					String requestUrl = getRequestUrl("/session/", session.getId(), "/reschedule");
					RestClient.updateObject(requestUrl, session);
				}
			}
			String requestUrl = getRequestUrl("/conference/", conference.getId());
			getStorage().remove(conference);
			RestClient.deleteObject(requestUrl);
		}
		return true;
	}

	public void setMoveSessions(boolean b) {
		this.moveSessions = b;
	}
}
