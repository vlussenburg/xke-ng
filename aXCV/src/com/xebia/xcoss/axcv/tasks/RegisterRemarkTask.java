package com.xebia.xcoss.axcv.tasks;

import java.util.ArrayList;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.logic.cache.DataCache;
import com.xebia.xcoss.axcv.model.Remark;

public class RegisterRemarkTask extends CVTask<Remark, Void, Boolean> {

	public RegisterRemarkTask(int action, BaseActivity ctx) {
		super(action, ctx, null);
	}
	
	@Override
	protected Boolean background(Context context, Remark... remarks) throws Exception {
		for (Remark remark : remarks) {
			String requestUrl = getRequestUrl("/feedback/", remark.getSessionId(), "/comment");
			RestClient.createObject(requestUrl, remark, Remark[].class);
			String key = DataCache.getKey("Remark", remark.getSessionId());
			getStorage().removeObject(key, new ArrayList<Remark>().getClass());
		}
		return true;
	}
}
