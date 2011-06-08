package com.xebia.xcoss.axcv;

import java.net.SocketException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.SecurityUtils;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public abstract class BaseActivity extends Activity {

	public static final String IA_CONFERENCE = "ID-conference";
	public static final String IA_SESSION = "ID-session";

	private MenuItem miSettings;
	private MenuItem miSearch;
	private MenuItem miList;
	private MenuItem miAdd;
	private MenuItem miEdit;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		miAdd = menu.add(0, XCS.MENU.ADD, Menu.NONE, R.string.menu_add);
		miEdit = menu.add(0, XCS.MENU.EDIT, Menu.NONE, R.string.menu_edit);
		miList = menu.add(0, XCS.MENU.OVERVIEW, Menu.NONE, R.string.menu_overview);
		miSettings = menu.add(0, XCS.MENU.SETTINGS, Menu.NONE, R.string.menu_settings);
		miSearch = menu.add(0, XCS.MENU.SEARCH, Menu.NONE, R.string.menu_search);

		miAdd.setIcon(R.drawable.ic_menu_add);
		miEdit.setIcon(R.drawable.ic_menu_allfriends);
		miSettings.setIcon(R.drawable.ic_menu_agenda);
		miSearch.setIcon(R.drawable.ic_btn_search);
		miList.setIcon(R.drawable.menu_list);

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
				Intent intent = new Intent(this, CVConferences.class);
				intent.putExtra("redirect", false);
				startActivity(intent);
			break;
		}
		return true;
	}

	protected Conference getConference() {
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
		if (conference == null) {
			conference = server.getUpcomingConference();
			Log.w(LOG.ALL, "Conference default " + conference.getTitle());
		}
		Log.e("XCS", "Conference = " + conference);
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
		ConferenceServer server = ConferenceServer.getInstance();
		if (server == null) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			String user = sp.getString(XCS.PREF.USERNAME, null);
			String password = SecurityUtils.decrypt(sp.getString(XCS.PREF.PASSWORD, ""));
			server = ConferenceServer.createInstance(user, password, XCS.SETTING.URL);
		}
		if (!server.isLoggedIn()) {
			showDialog(XCS.DIALOG.CONNECT_FAILED);
		}
		return server;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == XCS.DIALOG.CONNECT_FAILED) {
			return createDialog("Connection failed", "The connection to the server failed. Either the server is down or your credentials are wrong.");
		}
		return super.onCreateDialog(id);
	}

	protected Dialog createDialog(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
			.setTitle(title)
			.setMessage(message)
			.setIcon(R.drawable.icon)
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
			} catch (Exception e) {
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			dialog.cancel();
			
			if (result) {
				ctx.onSuccess();
			} else {
				Dialog errorDialog = createDialog("Error", "Connection to URL:\n"+XCS.SETTING.URL+"\nfailed");
				errorDialog.setOnCancelListener(new Dialog.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface di) {
						ctx.onFailure();
					}
				});
				errorDialog.setOnDismissListener(new Dialog.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface paramDialogInterface) {
						ctx.onFailure();
					}
				});
				errorDialog.show();
			}
		}
	}

}