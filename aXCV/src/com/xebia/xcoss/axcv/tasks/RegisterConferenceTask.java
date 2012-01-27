package com.xebia.xcoss.axcv.tasks;

import java.util.Set;
import java.util.TreeSet;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class RegisterConferenceTask extends CVTask<Conference, Void, Conference> {

	private Set<Session> sessionsToAdd;
	
	public RegisterConferenceTask(int action, BaseActivity ctx, TaskCallBack<Conference> callback) {
		super(action, ctx, callback);
		useCustomDialog(XCS.DIALOG.WAITING);
	}

	public void setSessions(TreeSet<Session> breakSessions) {
		sessionsToAdd = breakSessions;
	}

	@Override
	protected Conference background(Context context, Conference... conferences) throws Exception {
		if ( conferences == null || conferences.length != 1 ) {
			throw new Exception("Internal error: trying to store no or multiple conferences");
		}
		Conference conference = conferences[0];
		String requestUrl = getRequestUrl("/conference");
		getStorage().remove(conference);
		if (StringUtil.isEmpty(conference.getId())) {
			conference = RestClient.createObject(requestUrl, conference, Conference.class);
		} else {
			requestUrl = getRequestUrl("/conference/", conference.getId());
			conference = RestClient.updateObject(requestUrl , conference);
		}
		
		if ( sessionsToAdd != null) {
			Session[] sessions = sessionsToAdd.toArray(new Session[sessionsToAdd.size()]);
			RegisterSessionTask.createUpdateSessions(this, sessions);
		}
		
		getStorage().add(conference);
		return conference;
	}
}
