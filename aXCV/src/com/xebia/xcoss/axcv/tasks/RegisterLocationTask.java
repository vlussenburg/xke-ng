package com.xebia.xcoss.axcv.tasks;

import java.util.ArrayList;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.logic.cache.DataCache;
import com.xebia.xcoss.axcv.model.Location;

public class RegisterLocationTask extends CVTask<Location, Void, Boolean> {

	public RegisterLocationTask(int action, BaseActivity ctx) {
		super(action, ctx, null);
	}
	
	@Override
	protected Boolean background(Context context, Location... locations) throws Exception {
		for (Location location : locations) {
			getStorage().removeObject(DataCache.CK_ALL_LOCATIONS, new ArrayList<Location>().getClass());
			String requestUrl = getRequestUrl("/location");
			RestClient.createObject(requestUrl, location, Location.class);
		}
		return true;
	}
}
