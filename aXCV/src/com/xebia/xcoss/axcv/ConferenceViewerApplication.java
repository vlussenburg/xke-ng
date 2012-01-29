package com.xebia.xcoss.axcv;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.github.droidfu.DroidFuApplication;
import com.xebia.xcoss.axcv.logic.ProfileManager;
import com.xebia.xcoss.axcv.logic.cache.DataCache;
import com.xebia.xcoss.axcv.logic.cache.MemoryCache;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class ConferenceViewerApplication extends DroidFuApplication {

	private ProfileManager profileManager;
	private DataCache storage;
	private String user;
	private String password;

	public ConferenceViewerApplication() {
		// Don't init credentials here; preferences not yet initialized
	}

	private void initCredentials() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		this.user = sp.getString(XCS.PREF.USERNAME, null);
		this.password = sp.getString(XCS.PREF.PASSWORD, "");
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
		if (StringUtil.isEmpty(user)) {
			initCredentials();
		}
		return user;
	}

	public String getPassword() {
		if (StringUtil.isEmpty(password)) {
			initCredentials();
		}
		return password;
	}

	public DataCache getStorage() {
		if (storage == null) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			String type = null;
			try {
				type = sp.getString(XCS.PREF.CACHETYPE, null);
				if (type == null) {
					type = DataCache.Type.Memory.name();
					sp.edit().putString(XCS.PREF.CACHETYPE, type).commit();
				}
				Log.i(XCS.LOG.PROPERTIES, "Using storage type: " + type);
				storage = DataCache.Type.valueOf(type).newInstance(this);
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
		if (session.getType() == Session.Type.BREAK) return;

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
