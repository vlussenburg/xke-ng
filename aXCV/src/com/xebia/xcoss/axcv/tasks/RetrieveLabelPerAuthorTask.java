package com.xebia.xcoss.axcv.tasks;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.logic.cache.DataCache;
import com.xebia.xcoss.axcv.model.Author;

public class RetrieveLabelPerAuthorTask extends CVTask<Author, Void, List<String>> {

	public RetrieveLabelPerAuthorTask(int action, BaseActivity ctx, TaskCallBack<List<String>> callback) {
		super(action, ctx, callback);
	}

	@Override
	protected List<String> background(Context context, Author... authors) throws Exception {
		for (Author author : authors) {
			String key = DataCache.getKey("Label", author.getUserId());
			List<String> result = getStorage().getObject(key, new ArrayList<String>().getClass());
			if (result == null || result.isEmpty()) {
				String requestUrl = getRequestUrl("/labels/author/", author.getUserId());
				Type collectionType = new TypeToken<List<String>>() {}.getType();
				result = RestClient.loadCollection(requestUrl, collectionType);
				if (result != null) {
					getStorage().addObject(key, result);
				}
			}
			return result;
		}
		return null;
	}
}
