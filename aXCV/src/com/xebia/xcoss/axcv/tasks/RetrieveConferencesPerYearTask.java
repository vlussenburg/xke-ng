package com.xebia.xcoss.axcv.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.logic.RestClient;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.util.ConferenceComparator;
import com.xebia.xcoss.axcv.util.XCS;

public class RetrieveConferencesPerYearTask extends CVTask<Integer, Void, List<Conference>> {

	public RetrieveConferencesPerYearTask(int action, BaseActivity ctx, TaskCallBack<List<Conference>> callback) {
		super(action, ctx, callback);
		useCustomDialog(XCS.DIALOG.WAITING);
	}

	@Override
	protected List<Conference> background(Context context, Integer... year) throws Exception {
		if ( year.length < 1) {
			return new ArrayList<Conference>();
		}
		return loadConferencesForYear(this, year[0]);
	}

	public static List<Conference> loadConferencesForYear(CVTask<?,?,?> task, Integer yearValue) {
		List<Conference> result = task.getStorage().getConferences(yearValue);
		if (result == null || result.isEmpty()) {
			String requestUrl = task.getRequestUrl("/conferences/", String.valueOf(yearValue));
			result = RestClient.loadObjects(requestUrl, Conference.class);
			if (result == null) {
				return new ArrayList<Conference>();
			}
			for (Conference conference : result) {
				task.getStorage().add(conference);
			}
		}
		Collections.sort(result, new ConferenceComparator());
		return result;
	}
}
