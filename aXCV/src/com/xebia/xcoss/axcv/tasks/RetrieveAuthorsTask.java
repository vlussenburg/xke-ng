package com.xebia.xcoss.axcv.tasks;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.logic.cache.DataCache;
import com.xebia.xcoss.axcv.model.Author;

public class RetrieveAuthorsTask extends CVTask<Void, Void, List<Author>> {

	public RetrieveAuthorsTask(int action, BaseActivity ctx, TaskCallBack<List<Author>> callback) {
		super(action, ctx, callback);
	}

	@Override
	protected List<Author> background(Context context, Void... voids) throws Exception {
		List<Author> result = getStorage().getObject(DataCache.CK_ALL_AUTHORS, new ArrayList<Author>().getClass());
		if (result == null || result.isEmpty()) {
			String requestUrl = getRequestUrl("/authors");
			result = RestClient.loadObjects(requestUrl, Author.class);
			if (result == null) {
				return new ArrayList<Author>();
			}
			getStorage().addObject(DataCache.CK_ALL_AUTHORS, result);
		}
		return result;
	}
}
