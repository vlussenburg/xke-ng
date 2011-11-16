package com.xebia.xcoss.axcv;

import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.xebia.xcoss.axcv.logic.CommException;
import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.logic.ConferenceServerProxy;
import com.xebia.xcoss.axcv.logic.DataException;
import com.xebia.xcoss.axcv.logic.ProfileManager;
import com.xebia.xcoss.axcv.logic.cache.DataCache;
import com.xebia.xcoss.axcv.logic.cache.MemoryCache;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.ProxyExceptionReporter;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public abstract class BaseActivity extends Activity {

	public static final String IA_CONFERENCE = "ID-conference";
	public static final String IA_SESSION = "ID-session";
	public static final String IA_AUTHORS = "ID-authors";
	public static final String IA_AUTHOR = "ID-author";
	public static final String IA_LABELS = "ID-labels";
	public static final String IA_CONF_YEAR = "ID-year";
	public static final String IA_REDIRECT = "ID-redirect";
	public static final String IA_LOCATION_ID = "ID-location";
	public static final String IA_SESSION_START = "ID-sstart";
	public static final String IA_NOTIFICATION_ID = "ID-notified";
	public static final String IA_NOTIFICATION_TYPE = "ID-notytype";

	public enum NotificationType {
		TRACKED, OWNED;
	}

	private MenuItem miSettings;
	private MenuItem miSearch;
	private MenuItem miAdd;
	private MenuItem miEdit;
	private MenuItem miTrack;
	private MenuItem miExit;

	private static Activity rootActivity;
	private static ProfileManager profileManager;
	protected static String lastError;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ProxyExceptionReporter.register(this);

		if (rootActivity == null) {
			rootActivity = this;
		}

		ImageView conferenceButton = (ImageView) findViewById(R.id.conferenceButton);
		if (conferenceButton != null) {
			conferenceButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showConferencesList();
				}
			});
		}
	}

	@Override
	protected void onResume() {
		String notificationId = getIntent().getStringExtra(IA_NOTIFICATION_ID);
		if (notificationId != null) {
			NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			Log.w("debug", "Cancel on " + notificationId);
			mgr.cancel(notificationId.hashCode());
		}
		super.onResume();
	}
	
	private void resetApplication(boolean exit) {
		Log.e(LOG.ALL, "Reset application from " + this + " : " + exit);
		rootActivity = null;
		Intent intent = new Intent(BaseActivity.this, CVSplashLoader.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if (exit) {
			intent.putExtra("exit", true);
		}
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		miAdd = menu.add(0, XCS.MENU.ADD, Menu.NONE, R.string.menu_add);
		miEdit = menu.add(0, XCS.MENU.EDIT, Menu.NONE, R.string.menu_edit);
		miSettings = menu.add(0, XCS.MENU.SETTINGS, Menu.NONE, R.string.menu_settings);
		miSearch = menu.add(0, XCS.MENU.SEARCH, Menu.NONE, R.string.menu_search);
		miExit = menu.add(0, XCS.MENU.EXIT, Menu.NONE, R.string.menu_exit);

		miAdd.setIcon(android.R.drawable.ic_menu_add);
		miEdit.setIcon(android.R.drawable.ic_menu_edit);
		miSettings.setIcon(android.R.drawable.ic_menu_preferences);
		miSearch.setIcon(android.R.drawable.ic_menu_search);
		miExit.setIcon(R.drawable.ic_menu_exit);

		if (!StringUtil.isEmpty(getUser())) {
			miTrack = menu.add(0, XCS.MENU.TRACK, Menu.NONE, R.string.menu_track);
			miTrack.setIcon(android.R.drawable.ic_menu_agenda);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
			case XCS.MENU.SETTINGS:
				startActivity(new Intent(this, CVSettings.class));
			break;
			case XCS.MENU.OVERVIEW:
				showConferencesList();
			break;
			case XCS.MENU.SEARCH:
				startActivity(new Intent(this, CVSearch.class));
			break;
			case XCS.MENU.TRACK:
				startActivity(new Intent(this, CVTrack.class));
			break;
			case XCS.MENU.EXIT:
				resetApplication(true);
			break;
		}
		return true;
	}

	private void showConferencesList() {
		Intent intent = new Intent(this, CVConferences.class);
		intent.putExtra(IA_REDIRECT, false);
		// Clears out the activity call stack
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		// Finish the current activity
		finish();
	}

	public Conference getConference() {
		return getConference(true);
	}

	protected Conference getConference(boolean useDefault) {
		Conference conference = null;
		ConferenceServer server = getConferenceServer();

		String identifier = null;
		try {
			identifier = getIntent().getExtras().getString(IA_CONFERENCE);
			conference = server.getConference(identifier);
		}
		catch (Exception e) {
			Log.w(LOG.ALL, "No conference with ID " + identifier);
		}
		if (conference == null && useDefault) {
			conference = server.getUpcomingConference();
			Log.w(LOG.ALL, "Conference default " + conference.getTitle());
		}
		// Log.i("XCS", "[GET] Conference (on '" + identifier + "') = " + conference);
		return conference;
	}

	protected Session getSelectedSession(Conference conference) {
		Session session = null;
		String identifier = null;
		try {
			identifier = getIntent().getExtras().getString(IA_SESSION);
			session = conference.getSessionById(identifier);
		}
		catch (Exception e) {
			Log.w(LOG.ALL, "No session with ID " + identifier + " or conference not found.");
		}
		return session;
	}

	protected Session getDefaultSession(Conference conference) {
		Set<Session> sessions = conference.getSessions();
		for (Session s : sessions) {
			if (!s.isExpired()) {
				return s;
			}
		}
		return sessions.isEmpty() ? null : sessions.iterator().next();
	}

	protected ConferenceServer getConferenceServer() {
		ConferenceServer server = ConferenceServerProxy.getInstance(this);
		if (server == null || server.isLoggedIn() == false) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			String user = sp.getString(XCS.PREF.USERNAME, null);
			// TODO Encrypt/decrypt
			String password = /* SecurityUtils.decrypt( */sp.getString(XCS.PREF.PASSWORD, "")/* ) */;
			String type = "?";
			DataCache cache;
			try {
				type = sp.getString(XCS.PREF.CACHETYPE, null);
				if (type == null) {
					type = DataCache.Type.Memory.name();
					sp.edit().putString(XCS.PREF.CACHETYPE, type).commit();
				}
				Log.i(XCS.LOG.PROPERTIES, "Using cache type: " + type);
				cache = DataCache.Type.valueOf(type).newInstance(this);
			}
			catch (Exception e) {
				Log.w(XCS.LOG.PROPERTIES,
						"Cannot instantiate cache of type " + type + ": " + StringUtil.getExceptionMessage(e));
				cache = new MemoryCache(this);
			}
			server = ConferenceServer.createInstance(user, password, getServerUrl(this), cache);
		}
		return server;
	}

	protected ProfileManager getProfileManager() {
		if (profileManager == null) {
			profileManager = new ProfileManager(rootActivity);
		}
		profileManager.openConnection();
		return profileManager;
	}

	protected void closeProfileManager() {
		if (profileManager != null) {
			profileManager.closeConnection();
		}
	}

	protected Dialog createDialog(String title, String message) {
		return createDialog(this, title, message);
	}

	protected static Dialog createDialog(Activity ctx, String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(title).setMessage(message).setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton("Close", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		return builder.create();
	}

	protected void onSuccess() {}

	protected void onFailure() {}

	public static String getServerUrl(Context ctx) {
		try {
			// Invoke trim to make sure the value is specified
			ApplicationInfo ai = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(),
					PackageManager.GET_META_DATA);
			return ai.metaData.getString("com.xebia.xcoss.serverUrl").trim();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException("serverUrl is undefined");
		}
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

	public static void handleException(final Activity context, String activity, CommException e) {
		if (e instanceof DataException) {
			if (((DataException) e).missing()) {
				lastError = "No result for '" + activity + "'.";
				Log.w(XCS.LOG.COMMUNICATE, lastError);
				showMessage(context, lastError);
			} else if (((DataException) e).timedOut()) {
				lastError = "Time out on '" + activity + "'.";
				Log.w(XCS.LOG.COMMUNICATE, lastError);
				showMessage(context, lastError);
			} else {
				// Authentication failure
				if (context != null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Not allowed!")
							.setMessage("Access for " + activity + " is denied. Specify credentials?")
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.dismiss();
									context.startActivity(new Intent(context, CVSettings.class));
								}
							}).setNegativeButton("Continue", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.dismiss();
									// The next request will do the login again
									ConferenceServer.close();
								}
							});
					builder.create().show();
				} else {
					Log.e(XCS.LOG.COMMUNICATE, "Resource not found while " + activity + ".");
					lastError = "Not allowed: " + activity;
				}
			}
			return;
		}
		Log.e(XCS.LOG.COMMUNICATE, "Communication failure on " + activity + ", due to " + e.getMessage());
		throw new CommException("Failure on activity '" + activity + "': " + StringUtil.getExceptionMessage(e), e);
	}

	private static void showMessage(Activity context, String msg) {
		Context ctxt = context;
		if (context == null) {
			ctxt = rootActivity.getBaseContext();
		}
		// TODO Does this work?
		if (Looper.myLooper() != null) Toast.makeText(ctxt, msg, Toast.LENGTH_SHORT);
	}

	public String getUser() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String user = sp.getString(XCS.PREF.USERNAME, null);
		return user;
	}

	public static String getLastError() {
		String error = lastError;
		lastError = null;
		return error;
	}
}