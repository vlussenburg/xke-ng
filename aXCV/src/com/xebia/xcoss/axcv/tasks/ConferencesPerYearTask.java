package com.xebia.xcoss.axcv.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.util.ConferenceComparator;

public class ConferencesPerYearTask extends CVTask<Integer, Void, List<Conference>> {

	public ConferencesPerYearTask(int action, BaseActivity ctx) {
		super(action, ctx);
	}
	
	@Override
	protected List<Conference> background(Context context, Integer... year) throws Exception {
		Integer yearValue = year[0];
		List<Conference> result = getStorage().getConferences(yearValue);
		if (result == null || result.isEmpty()) {
			String requestUrl = getRequestUrl("/conferences/", String.valueOf(yearValue));
			result = RestClient.loadObjects(requestUrl.toString(), Conference.class, null);
			if (result == null) {
				return new ArrayList<Conference>();
			}
			for (Conference conference : result) {
				getStorage().add(conference);
			}
		}
		Collections.sort(result, new ConferenceComparator());
		return result;
	}
}
