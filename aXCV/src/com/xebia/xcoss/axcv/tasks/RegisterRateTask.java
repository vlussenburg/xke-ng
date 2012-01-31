package com.xebia.xcoss.axcv.tasks;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.model.Rate;

public class RegisterRateTask extends CVTask<Rate, Void, Boolean> {

	public RegisterRateTask(int action, BaseActivity ctx) {
		super(action, ctx, null);
	}
	
	@Override
	protected Boolean background(Context context, Rate... rates) throws Exception {
		for (Rate rate : rates) {
			String requestUrl = getRequestUrl("/feedback/", rate.getSessionId(), "/rating");
			RestClient.createObject(requestUrl, rate, int[].class);
		}
		return true;
	}
}
