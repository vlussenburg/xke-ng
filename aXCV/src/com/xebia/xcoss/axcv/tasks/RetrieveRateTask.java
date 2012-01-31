package com.xebia.xcoss.axcv.tasks;

import java.util.List;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.model.Rate;

public class RetrieveRateTask extends CVTask<String, Void, Rate> {

	public RetrieveRateTask(int action, BaseActivity ctx, TaskCallBack<Rate> callback) {
		super(action, ctx, callback);
	}

	@Override
	protected Rate background(Context context, String... sessionIds) throws Exception {
		for (String id : sessionIds) {
			String requestUrl = getRequestUrl("/feedback/", id, "/rating");
			List<Integer> list = RestClient.loadObjects(requestUrl, int.class);
			return new Rate(list);
		}
		return null;
	}
}
