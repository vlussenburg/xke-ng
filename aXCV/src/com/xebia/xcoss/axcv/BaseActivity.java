package com.xebia.xcoss.axcv;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.github.droidfu.activities.BetterDefaultActivity;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Moment;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

import de.quist.app.errorreporter.ExceptionReporter;

/**
 * IA_NOTIFICATION_ID - ID of notification (optional). Clears the notification flag.
 * 
 * @author Michael
 *
 */
public abstract class BaseActivity extends BetterDefaultActivity {

	public static final String IA_CONFERENCE_ID = "ID-conference";
	public static final String IA_SESSION = "ID-session";
	public static final String IA_AUTHORS = "ID-authors";
	public static final String IA_AUTHOR = "ID-author";
	public static final String IA_LABELS = "ID-labels";
	public static final String IA_CONF_YEAR = "ID-year";
	public static final String IA_MOMENT = "ID-moment";
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
	private MenuItem miRunning;
	private ExceptionReporter exceptionReporter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		exceptionReporter = ExceptionReporter.register(this);

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
	protected Dialog onCreateDialog(int id) {
		if (id == XCS.DIALOG.WAITING) {
			Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
			dialog.setContentView(R.layout.waiting);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface arg0) {
					BaseActivity.this.finish();
				}
			});
			return dialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	public void finish() {
		removeDialog(XCS.DIALOG.WAITING);
		// TODO Remove any warning dialog created by createDialog. This leaks...
		super.finish();
	}
	
	protected void populateMenuOptions(ArrayList<Integer> list) {};

	@Override
	public final boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		ArrayList<Integer> list = new ArrayList<Integer>();
		populateMenuOptions(list);

		if (list.contains(XCS.MENU.ADD)) {
			miAdd = menu.add(0, XCS.MENU.ADD, Menu.NONE, R.string.menu_add);
			miAdd.setIcon(android.R.drawable.ic_menu_add);
		}
		if (list.contains(XCS.MENU.EDIT)) {
			miEdit = menu.add(0, XCS.MENU.EDIT, Menu.NONE, R.string.menu_edit);
			miEdit.setIcon(android.R.drawable.ic_menu_edit);
		}
		if (list.contains(XCS.MENU.SETTINGS)) {
			miSettings = menu.add(0, XCS.MENU.SETTINGS, Menu.NONE, R.string.menu_settings);
			miSettings.setIcon(android.R.drawable.ic_menu_preferences);
		}
		if (list.contains(XCS.MENU.SEARCH)) {
			miSearch = menu.add(0, XCS.MENU.SEARCH, Menu.NONE, R.string.menu_search);
			miSearch.setIcon(android.R.drawable.ic_menu_search);
		}
		if (list.contains(XCS.MENU.LIST)) {
			MenuItem menuItem = menu.add(0, XCS.MENU.LIST, Menu.NONE, R.string.menu_list);
			menuItem.setIcon(R.drawable.ic_menu_list);
		}
		if (list.contains(XCS.MENU.RUNNING)) {
			MenuItem menuItem = menu.add(0, XCS.MENU.RUNNING, Menu.NONE, R.string.menu_running);
			menuItem.setIcon(android.R.drawable.ic_menu_recent_history);
		}
		if (list.contains(XCS.MENU.TRACK) && !StringUtil.isEmpty(getUser())) {
			miTrack = menu.add(0, XCS.MENU.TRACK, Menu.NONE, R.string.menu_track);
			miTrack.setIcon(android.R.drawable.ic_menu_agenda);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case XCS.MENU.SETTINGS:
				startActivity(new Intent(this, CVSettings.class));
				return true;
			case XCS.MENU.OVERVIEW:
				showConferencesList();
				return true;
			case XCS.MENU.SEARCH:
				startActivity(new Intent(this, CVSearch.class));
				return true;
			case XCS.MENU.TRACK:
				startActivity(new Intent(this, CVTrack.class));
				return true;
			case XCS.MENU.RUNNING:
				startActivity(new Intent(this, CVRunning.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
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

	protected String getSelectedConferenceId() {
		try {
			return getIntent().getExtras().getString(IA_CONFERENCE_ID);
		}
		catch (Exception e) {
			Log.w(LOG.ALL, "No conference in intent.");
		}
		return null;
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

	protected Dialog createDialog(int title, int message) {
		return createDialog(this, getString(title), getString(message));
	}

	protected Dialog createDialog(String title, String message) {
		return createDialog(this, title, message);
	}

	public static Dialog createDialog(Context ctx, String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(title).setMessage(message).setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		return builder.create();
	}

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

	public String getUser() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String user = sp.getString(XCS.PREF.USERNAME, null);
		return user;
	}

	public Conference findUpcomming(List<Conference> conferences) {
		for (Conference conference : conferences) {
			Moment cdate = conference.getStartTime();
			if (cdate.isBeforeToday()) {
				continue;
			}
			return conference;
		}
		return null;
	}

	public ConferenceViewerApplication getMyApplication() {
		return (ConferenceViewerApplication) getApplication();
	}
	
	public ExceptionReporter getExceptionReporter() {
		return exceptionReporter;
	}
}