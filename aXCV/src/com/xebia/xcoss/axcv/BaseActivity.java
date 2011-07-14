package com.xebia.xcoss.axcv;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.xebia.xcoss.axcv.logic.CommException;
import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.logic.ConferenceServerProxy;
import com.xebia.xcoss.axcv.logic.DataException;
import com.xebia.xcoss.axcv.logic.ProfileManager;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.ProxyExceptionReporter;
import com.xebia.xcoss.axcv.util.SecurityUtils;
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

	private MenuItem miSettings;
	private MenuItem miSearch;
	private MenuItem miList;
	private MenuItem miAdd;
	private MenuItem miEdit;
	private MenuItem miTrack;

	private static Activity rootActivity;
	private static ProfileManager profileManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ProxyExceptionReporter.register(this);

		if (rootActivity == null) {
			rootActivity = this;
		}
		Log.e("XCS", "========== ROOT activity [" + rootActivity.getLocalClassName() + "] ========== ");
		if ((rootActivity instanceof CVSplashLoader) == false) {
			Log.e("XCS", "========== ROOT activity RESET ========== ");
			resetApplication(true);
			return;
		}

		ImageView conferenceButton = (ImageView) findViewById(R.id.conferenceButton);
		if (conferenceButton != null) {
			conferenceButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showConferencesList();
				}
			});
			conferenceButton.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					resetApplication(true);
					return true;
				}
			});
		}
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
		miList = menu.add(0, XCS.MENU.OVERVIEW, Menu.NONE, R.string.menu_overview);
		miSettings = menu.add(0, XCS.MENU.SETTINGS, Menu.NONE, R.string.menu_settings);
		miSearch = menu.add(0, XCS.MENU.SEARCH, Menu.NONE, R.string.menu_search);
		miTrack = menu.add(0, XCS.MENU.TRACK, Menu.NONE, R.string.menu_track);

		miAdd.setIcon(android.R.drawable.ic_menu_add);
		miEdit.setIcon(android.R.drawable.ic_menu_edit);
		miSettings.setIcon(android.R.drawable.ic_menu_preferences);
		miSearch.setIcon(android.R.drawable.ic_menu_search);
		miList.setIcon(R.drawable.ic_menu_list);
		miTrack.setIcon(android.R.drawable.ic_menu_agenda);

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
		}
		return true;
	}

	private void showConferencesList() {
		Intent intent = new Intent(this, CVConferences.class);
		intent.putExtra(IA_REDIRECT, false);
		// Clears out the activity call stack
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	public Conference getConference() {
		return getConference(true);
	}

	protected Conference getConference(boolean useDefault) {
		Conference conference = null;
		ConferenceServer server = getConferenceServer();

		int identifier = -1;
		try {
			identifier = getIntent().getExtras().getInt(IA_CONFERENCE);
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

	protected Session getSession(Conference conference) {
		return getSession(conference, true);
	}

	protected Session getSession(Conference conference, boolean useDefault) {
		Session session = null;
		int identifier = -1;
		try {
			identifier = getIntent().getExtras().getInt(IA_SESSION);
			session = conference.getSessionById(identifier);
		}
		catch (Exception e) {
			Log.w(LOG.ALL, "No session with ID " + identifier + " or conference not found.");
		}
		if (session == null && useDefault) {
			Log.w(LOG.ALL, "Conference default : " + (conference == null ? "NULL" : conference.getTitle()));
			session = conference.getUpcomingSession();
			Log.w(LOG.ALL, "Session default " + (session == null ? "NULL" : session.getTitle()));
		}
		return session;
	}

	protected ConferenceServer getConferenceServer() {
		ConferenceServer server = ConferenceServerProxy.getInstance(this);
		if (server == null || server.isLoggedIn() == false) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			String user = sp.getString(XCS.PREF.USERNAME, null);
			String password = SecurityUtils.decrypt(sp.getString(XCS.PREF.PASSWORD, ""));
			server = ConferenceServer.createInstance(user, password, getServerUrl());
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

	class DataRetriever extends AsyncTask<String, Void, Boolean> {

		// http://appfulcrum.com/?p=126

		private ProgressDialog dialog;
		private BaseActivity ctx;
		private Exception resultingException;

		public DataRetriever(BaseActivity ctx) {
			this.ctx = ctx;
		}

		@Override
		protected void onPreExecute() {
			this.dialog = ProgressDialog.show(ctx, null, "Loading. Please wait...", true);
		}

		@Override
		protected Boolean doInBackground(String... arg0) {
			try {
				List<Conference> conferences = getConferenceServer().getUpcomingConferences(4);
				for (Conference conference : conferences) {
					conference.getSessions();
				}
			}
			catch (DataException e) {
				resultingException = e;
			}
			catch (Exception e) {
				resultingException = e;
				Log.e(XCS.LOG.COMMUNICATE, "[Initial load] Failure: " + StringUtil.getExceptionMessage(e));
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			dialog.cancel();

			if (result) {
				// Note, authentication may still be invalid.
				ctx.onSuccess();
			} else {
				Dialog errorDialog = createDialog("Error",
						"Connection to server failed (" + StringUtil.getExceptionMessage(resultingException) + ").");
				errorDialog.setOnDismissListener(new Dialog.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface di) {
						ctx.onFailure();
						di.dismiss();
					}
				});
				errorDialog.show();
			}
		}
	}

	private String getServerUrl() {
		try {
			// Invoke trim to make sure the value is specified
			ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
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
				Log.w(XCS.LOG.COMMUNICATE, "No result for '" + activity + "'.");
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
				}
			}
			return;
		}
		Log.e(XCS.LOG.COMMUNICATE, "Communication failure on " + activity + ", due to " + e.getMessage());
		throw new CommException("Failure on activity '" + activity + "': " + StringUtil.getExceptionMessage(e), e);
	}

	protected String getUser() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String user = sp.getString(XCS.PREF.USERNAME, null);
		return user;
	}
}