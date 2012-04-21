package com.xebia.xcoss.axcv;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.github.droidfu.DroidFuApplication;
import com.xebia.xcoss.axcv.logic.ProfileManager;
import com.xebia.xcoss.axcv.logic.cache.DataCache;
import com.xebia.xcoss.axcv.logic.cache.DataCache.Type;
import com.xebia.xcoss.axcv.logic.cache.MemoryCache;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Rate;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.model.SessionType;
import com.xebia.xcoss.axcv.ui.ConferenceStatus;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class ConferenceViewerApplication extends DroidFuApplication {

	private ProfileManager profileManager;
	private DataCache storage;

	public ConferenceViewerApplication() {
		// Don't init credentials here; preferences not yet initialized
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Resources rsc = getApplicationContext().getResources();
		Rate.init(rsc.getStringArray(R.array.rateValues));
		Conference.init(rsc.getStringArray(R.array.conferenceFields));
		Session.init(rsc.getStringArray(R.array.sessionFields));
		ConferenceStatus.init(rsc.getStringArray(R.array.statusFields));
		SessionType.init(rsc.getStringArray(R.array.sessionTypes));
	}
	
	protected ProfileManager getProfileManager() {
		if (profileManager == null) {
			profileManager = new ProfileManager(this);
		}
		profileManager.openConnection();
		return profileManager;
	}

	protected void closeProfileManager() {
		if (profileManager != null) {
			profileManager.closeConnection();
		}
	}

	public String getServerUrl() {
		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			// Invoke trim to make sure the value is specified
			return ai.metaData.getString("com.xebia.xcoss.serverUrl").trim();
		}
		catch (Exception e) {
			throw new IllegalArgumentException("serverUrl is undefined");
		}
	}

	public String getUser() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		return sp.getString(XCS.PREF.USERNAME, null);
	}

	public String getPassword() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		return sp.getString(XCS.PREF.PASSWORD, "");
	}

	public DataCache getCache() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String type = sp.getString(XCS.PREF.CACHETYPE, null);
		Type typeOf = null;
		try {
			typeOf = DataCache.Type.valueOf(type);
		} catch (Exception e) {
			// ignore
		}
		
		if (typeOf == null) {
			typeOf = DataCache.Type.Memory;
			sp.edit().putString(XCS.PREF.CACHETYPE, typeOf.name()).commit();
		}

		if (storage == null || typeOf != storage.getType()) {
			if ( storage != null) {
				storage.destroy();
			}
			try {
				Log.i(XCS.LOG.PROPERTIES, "Using storage type: " + type);
				storage = typeOf.newInstance(this);
			}
			catch (Exception e) {
				Log.w(XCS.LOG.PROPERTIES,
						"Cannot instantiate storage of type " + type + ": " + StringUtil.getExceptionMessage(e));
				storage = new MemoryCache(this);
			}
		}
		return storage;
	}
	
	@Override
	public void onClose() {
		if (profileManager != null) {
			profileManager.closeConnection();
		}
		if ( storage != null ) {
			storage.destroy();
		}
		super.onClose();
	}

	public void markSession(Session session, View view, boolean update) {
		// Breaks are not supported for marking
		if (session.isBreak()) return;

		ProfileManager pm = getProfileManager();
		boolean hasMarked = pm.isMarked(getUser(), session.getId());
		if (update) {
			if (hasMarked) {
				if (pm.unmarkSession(getUser(), session)) hasMarked = false;
			} else {
				if (pm.markSession(getUser(), session)) hasMarked = true;
			}
		}

		if (view instanceof ImageView) {
			((ImageView) view).setImageResource(hasMarked ? android.R.drawable.btn_star_big_on
					: android.R.drawable.btn_star_big_off);
		}
	}

}
