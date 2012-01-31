package com.xebia.xcoss.axcv.tasks;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Search;
import com.xebia.xcoss.axcv.util.XCS;

public class SearchAuthorsTask extends CVTask<Search, Void, List<Author>> {

	public SearchAuthorsTask(int action, BaseActivity ctx, TaskCallBack<List<Author>> callback) {
		super(action, ctx, callback);
		useCustomDialog(XCS.DIALOG.WAITING);
	}
	
	@Override
	protected List<Author> background(Context context, Search... search) throws Exception {
		String requestUrl = getRequestUrl("/search/authors");
		List<Author> result = RestClient.searchObjects(requestUrl, "authors", Author.class, search);
		if (result == null) {
			return new ArrayList<Author>();
		}
		return result;
	}
}
