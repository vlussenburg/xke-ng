package com.xebia.xcoss.axcv.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.model.util.SessionComparator;
import com.xebia.xcoss.axcv.util.XCS;

public class SearchSessionsTask extends CVTask<Search, Void, List<Session>> {

	public SearchSessionsTask(int action, BaseActivity ctx, TaskCallBack<List<Session>> callback) {
		super(action, ctx, callback);
		useCustomDialog(XCS.DIALOG.WAITING);
	}
	
	@Override
	protected List<Session> background(Context context, Search... search) throws Exception {
		String requestUrl = getRequestUrl("/search/sessions");
		List<Session> result = RestClient.searchObjects(requestUrl, "sessions", Session.class, search);
		if (result == null) {
			return new ArrayList<Session>();
		}
		// TODO : Sessions do not have a conference ID
		Collections.sort(result, new SessionComparator());
		return result;
	}
}
