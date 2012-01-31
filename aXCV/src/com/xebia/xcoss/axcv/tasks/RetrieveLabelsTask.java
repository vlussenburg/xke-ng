package com.xebia.xcoss.axcv.tasks;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.logic.cache.DataCache;

public class RetrieveLabelsTask extends CVTask<Void, Void, List<String>> {

	public RetrieveLabelsTask(int action, BaseActivity ctx, TaskCallBack<List<String>> callback) {
		super(action, ctx, callback);
	}

	@Override
	protected List<String> background(Context context, Void... v) throws Exception {
		List<String> result = getStorage().getObject(DataCache.CK_ALL_LABELS, new ArrayList<String>().getClass());
		if (result == null || result.isEmpty()) {
			String requestUrl = getRequestUrl("/labels");
			Type collectionType = new TypeToken<List<String>>() {}.getType();
			result = RestClient.loadCollection(requestUrl, collectionType);
			if (result == null) {
				return new ArrayList<String>();
			}
			getStorage().addObject(DataCache.CK_ALL_LABELS, result);
		}
		return result;
	}
}
