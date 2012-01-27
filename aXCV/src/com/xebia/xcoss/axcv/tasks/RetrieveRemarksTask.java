package com.xebia.xcoss.axcv.tasks;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.logic.cache.DataCache;
import com.xebia.xcoss.axcv.model.Remark;

public class RetrieveRemarksTask extends CVTask<String, Void, List<Remark>> {

	public RetrieveRemarksTask(int action, BaseActivity ctx, TaskCallBack<List<Remark>> callback) {
		super(action, ctx, callback);
	}

	@Override
	protected List<Remark> background(Context context, String... sessionIds) throws Exception {
		for (String id : sessionIds) {	
			String key = DataCache.getKey("Remark", id);
			List<Remark> result = getStorage().getObject(key, new ArrayList<Remark>().getClass());
			if (result == null || result.isEmpty()) {
				String requestUrl = getRequestUrl("/feedback/", id, "/comment");
				result = RestClient.loadObjects(requestUrl, Remark.class);
				if (result == null) {
					return new ArrayList<Remark>();
				}
				getStorage().addObject(key, result);
			}
			return result;
		}
		return new ArrayList<Remark>();
	}
}
