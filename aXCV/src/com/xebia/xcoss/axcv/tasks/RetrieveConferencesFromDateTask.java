package com.xebia.xcoss.axcv.tasks;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.util.XCS;

public class RetrieveConferencesFromDateTask extends CVTask<Integer, Void, List<Conference>> {

	private Moment moment = new Moment();

	public RetrieveConferencesFromDateTask(int action, BaseActivity ctx, TaskCallBack<List<Conference>> callback) {
		super(action, ctx, callback);
		useCustomDialog(XCS.DIALOG.WAITING);
	}

	public void setMoment(Moment moment) {
		this.moment = moment;
	}

	@Override
	protected List<Conference> background(Context context, Integer... count) throws Exception {
		int size = 1;
		if (count.length > 0) {
			size = count[0];
		}
		Integer yearValue = moment.getYear();
		List<Conference> result = new ArrayList<Conference>();
		do {
			List<Conference> conferences = RetrieveConferencesPerYearTask.loadConferencesForYear(this, yearValue);
			if (conferences.isEmpty()) break /* from do while loop */;
			
			for (Conference conference : conferences) {
				Moment cdate = conference.getStartTime();
				if (cdate.isBeforeToday()) {
					continue;
				}
				if ( result.size() < size ) {
					result.add(conference);
				}
			}

		} while (result.size() < size);
		return result;
	}
}
