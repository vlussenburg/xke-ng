package com.xebia.xcoss.axcv.tasks;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.util.XCS;

public class RetrieveNextConferenceTask extends CVTask<String, Void, Conference> {

	public RetrieveNextConferenceTask(int action, BaseActivity ctx, TaskCallBack<Conference> callback) {
		super(action, ctx, callback);
		useCustomDialog(XCS.DIALOG.WAITING);
	}

	@Override
	protected Conference background(Context ctx, String... params) throws Exception {
		Conference conference = null;
		try {
			conference = getStorage().getConference(params[0]);
		}
		catch (Exception e) {
		}
		if ( conference == null ) {
			String requestUrl = getRequestUrl("/conference/next");
			conference = RestClient.loadObject(requestUrl, Conference.class);
			getStorage().add(conference);
		}
		return conference;
	}
}
