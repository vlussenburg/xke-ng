package com.xebia.xcoss.axcv.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.logic.cache.DataCache;
import com.xebia.xcoss.axcv.model.Location;

public class RetrieveLocationsTask extends CVTask<Void, Void, List<Location>> {

	public RetrieveLocationsTask(int action, BaseActivity ctx, TaskCallBack<List<Location>> callback) {
		super(action, ctx, callback);
	}

	@Override
	protected List<Location> background(Context context, Void... v) throws Exception {
		List<Location> result = getStorage().getObject(DataCache.CK_ALL_LOCATIONS, new ArrayList<Location>().getClass());
		if (result == null || result.isEmpty()) {
			String requestUrl = getRequestUrl("/locations");
			result = RestClient.loadObjects(requestUrl, Location.class);
			if (result == null) {
				return new ArrayList<Location>();
			}
			Collections.sort(result, new Comparator<Location>() {
				public int compare(Location object1, Location object2) {
					return object1.getDescription().compareTo(object2.getDescription());
				}
			});
			getStorage().addObject(DataCache.CK_ALL_LOCATIONS, result);
		}
		return result;
	}
}
