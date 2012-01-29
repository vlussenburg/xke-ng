package com.xebia.xcoss.axcv.tasks;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.util.StringUtil;

public class RetrieveConferenceTask extends CVTask<String, Void, Conference> {

	public RetrieveConferenceTask(int action, BaseActivity ctx, TaskCallBack<Conference> callback) {
		super(action, ctx, callback);
	}

	@Override
	protected Conference background(Context context, String... conferenceId) throws Exception {
		for (String id : conferenceId) {
			if (!StringUtil.isEmpty(id)) {
				Conference result = getStorage().getConference(id);
				if (result == null) {
					String requestUrl = getRequestUrl("/conference/", id);
					result = RestClient.loadObject(requestUrl, Conference.class);
				}
				getStorage().add(result);
				return result;
			}
		}
		return null;
	}
}
