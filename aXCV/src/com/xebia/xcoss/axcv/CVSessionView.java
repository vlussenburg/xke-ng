package com.xebia.xcoss.axcv;

import java.util.ArrayList;
import java.util.Set;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.model.Rate;
import com.xebia.xcoss.axcv.model.Remark;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.ui.FormatUtil;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

public class CVSessionView extends SessionSwipeActivity {

	private Session currentSession;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.sessionview);
		super.onCreate(savedInstanceState);
		addGestureDetection(R.id.relativeLayoutLowest);
	}

	@Override
	protected void onResume() {
		Conference conference = getCurrentConference();
		currentSession = getSelectedSession(conference);

		if (currentSession == null) {
			Location location = getCurrentLocation();
			ArrayList<Session> options = new ArrayList<Session>();
			for (Session s : conference.getSessions()) {
				if (s.getLocation().equals(location) || s.isBreak()) {
					options.add(s);
				}
			}
			int start = getIntent().getIntExtra(IA_SESSION_START, 0);
			for (Session session : options) {
				if (session.getStartTime().asMinutes() == start) {
					currentSession = session;
					break;
				}
			}
			if (currentSession == null && options.size() > 0) {
				currentSession = options.get(0);
			}
		}
		if (currentSession == null) {
			currentSession = getDefaultSession(conference);
		}
		if (currentSession != null) {
			updateLocation(currentSession);
			fill(conference);
			updateLocations();
			updateSessions();
			updateRateAndReview();
		}
		super.onResume();
	}

	private void fill(Conference conference) {
		TextView title = (TextView) findViewById(R.id.conferenceTitle);
		title.setText(conference.getTitle());

		TextView date = (TextView) findViewById(R.id.conferenceDate);
		ScreenTimeUtil timeUtil = new ScreenTimeUtil(this);
		String val = timeUtil.getAbsoluteDate(conference.getStartTime());
		date.setText(val);

		TextView sessionDate = (TextView) findViewById(R.id.sessionTime);
		StringBuilder sb = new StringBuilder();
		sb.append(timeUtil.getAbsoluteTime(currentSession.getStartTime()));
		sb.append(" - ");
		sb.append(timeUtil.getAbsoluteTime(currentSession.getEndTime()));
		sessionDate.setText(sb.toString());

		TextView sessionLocation = ((TextView) findViewById(R.id.sessionLocation));
		TextView sessionTitle = (TextView) findViewById(R.id.scTitle);
		TextView sessionDescription = (TextView) findViewById(R.id.scDescription);
		TextView sessionAuthor = (TextView) findViewById(R.id.scAuthor);

		sessionLocation.setText(currentSession.getLocation().getDescription());
		sessionTitle.setText(currentSession.getTitle());
		sessionDescription.setText(currentSession.getDescription());
		sessionAuthor.setText(FormatUtil.getList(currentSession.getAuthors()));

		// Optional fields (hide when not available)
		updateTextField(R.id.scAudience, R.id.scAudienceLabel, currentSession.getIntendedAudience());
		updateTextField(R.id.scLabels, R.id.scLabelsLabel, FormatUtil.getList(currentSession.getLabels(), false));
		updateTextField(R.id.scLanguage, R.id.scLanguageLabel, FormatUtil.getList(currentSession.getLanguages(), false));
		updateTextField(R.id.scLimit, R.id.scLimitLabel, currentSession.getLimit());
		updateTextField(R.id.scPreparation, R.id.scPreparationLabel, currentSession.getPreparation());

		TextView labelView = (TextView) findViewById(R.id.scLabels);
		Linkify.addLinks(labelView, XCS.TAG.PATTERN, XCS.TAG.LINK);
		if (currentSession.getAuthors().size() > 0) {
			Linkify.addLinks(sessionAuthor, XCS.AUTHOR.PATTERN, XCS.AUTHOR.LINK);
		}

		OnClickListener lRate = new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				showDialog(XCS.DIALOG.ADD_RATING);
			}
		};
		OnClickListener lReview = new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				showDialog(XCS.DIALOG.CREATE_REVIEW);
			}
		};

		TextView view = (TextView) findViewById(R.id.scRating);
		view.setOnClickListener(lRate);
		findViewById(R.id.scRatingLayout).setOnClickListener(lRate);

		view = (TextView) findViewById(R.id.scComments);
		view.setOnClickListener(lReview);

		ImageView button = (ImageView) findViewById(R.id.sessionMarkButton);
		if (currentSession.getType() == Session.Type.BREAK || StringUtil.isEmpty(getUser())) {
			button.setVisibility(View.GONE);
		} else {
			markSession(currentSession, button, false);
			button.setVisibility(View.VISIBLE);
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					markSession(currentSession, view, true);
				}
			});
		}
	}

	private void updateRateAndReview() {
		ConferenceServer server = getConferenceServer();

		TextView view = (TextView) findViewById(R.id.scRating);
		view.setText(FormatUtil.getText(server.getRate(currentSession)));

		view = (TextView) findViewById(R.id.scComments);
		Spanned spannedContent = Html.fromHtml(FormatUtil.getHtml(server.getRemarks(currentSession)));
		view.setText(spannedContent, BufferType.SPANNABLE);

	}

	private void updateSessions() {
		Session session = getNextSession(getCurrentLocation());
		Log.i(XCS.LOG.NAVIGATE, "Find sessions at " + getCurrentLocation());
		Log.i(XCS.LOG.NAVIGATE, "Current session = " + currentSession);
		Log.i(XCS.LOG.NAVIGATE, "Next session = " + session);
		View viewById = findViewById(R.id.textNextSession);
		LinearLayout layout = (LinearLayout) viewById.getParent();
		if (session == null) {
			layout.setVisibility(View.GONE);
		} else {
			layout.setVisibility(View.VISIBLE);
			TextView sessionText = (TextView) viewById;
			sessionText.setText(session.getTitle());
			sessionText.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onSwipeBottomToTop();
				}
			});
		}

		session = getPreviousSession(getCurrentLocation());
		Log.i(XCS.LOG.NAVIGATE, "Previous session = " + session);
		viewById = findViewById(R.id.textPreviousSession);
		layout = (LinearLayout) viewById.getParent();
		if (session == null) {
			layout.setVisibility(View.GONE);
		} else {
			layout.setVisibility(View.VISIBLE);
			TextView sessionText = (TextView) viewById;
			sessionText.setText(session.getTitle());
			sessionText.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onSwipeTopToBottom();
				}
			});
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
			case XCS.DIALOG.ADD_RATING:
				dialog = new Dialog(this);
				dialog.setContentView(R.layout.dialog_rating);
				dialog.setTitle("Your rating");
				TextView text = (TextView) dialog.findViewById(R.id.drSessionTitle);
				text.setText(currentSession.getTitle());

				Button submit = (Button) dialog.findViewById(R.id.drSubmit);
				final RatingBar ratingBar = (RatingBar) dialog.findViewById(R.id.drSessionRate);
				final TextView rateText = (TextView) dialog.findViewById(R.id.drRateText);
				rateText.setText(new Rate(ratingBar).getMessage());

				// Or use a DialogInterface.OnClickListener to directly access the dialog
				submit.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View paramView) {
						Rate rate = new Rate(ratingBar);
						getConferenceServer().registerRate(currentSession, rate);
						dismissDialog(XCS.DIALOG.ADD_RATING);
						updateRateAndReview();
					}
				});

				ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
					
					@Override
					public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
						rateText.setText(new Rate(ratingBar).getMessage());
					}
				});
				return dialog;
			case XCS.DIALOG.CREATE_REVIEW:
				dialog = new Dialog(this);
				dialog.setContentView(R.layout.dialog_review);
				dialog.setTitle("Your remark");
				text = (TextView) dialog.findViewById(R.id.dvSessionTitle);
				text.setText(currentSession.getTitle());

				submit = (Button) dialog.findViewById(R.id.dvSubmit);
				final TextView edit = (TextView) dialog.findViewById(R.id.dvEditText);

				// Or use a DialogInterface.OnClickListener to directly access the dialog
				submit.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View paramView) {
						String remark = edit.getText().toString();
						getConferenceServer().registerRemark(currentSession, new Remark(getUser(), remark));
						dismissDialog(XCS.DIALOG.CREATE_REVIEW);
						updateRateAndReview();
					}
				});
				return dialog;
		}
		return super.onCreateDialog(id);
	}

	private void updateTextField(int field, int label, String value) {
		if (StringUtil.isEmpty(value)) {
			findViewById(field).setVisibility(View.GONE);
			findViewById(label).setVisibility(View.GONE);
		} else {
			((TextView) findViewById(field)).setText(value);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem menuItem = menu.add(0, XCS.MENU.LIST, Menu.NONE, R.string.menu_list);
		menuItem.setIcon(R.drawable.ic_menu_list);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Add or edit a session
		Intent intent = new Intent(this, CVSessionAdd.class);
		intent.putExtra(BaseActivity.IA_CONFERENCE, getConference().getId());
		boolean processed = false;

		switch (item.getItemId()) {
			case XCS.MENU.ADD:
				startActivity(intent);
				processed = true;
			break;
			case XCS.MENU.EDIT:
				intent.putExtra(BaseActivity.IA_SESSION, currentSession.getId());
				startActivity(intent);
				processed = true;
			break;
			case XCS.MENU.LIST:
				intent = new Intent(this, CVSessionList.class);
				intent.putExtra(IA_LOCATION_ID, currentLocation);
				intent.putExtra(IA_CONFERENCE, currentSession.getConferenceId());
				startActivity(intent);
				processed = true;
			break;
		}
		return processed ? true : super.onOptionsItemSelected(item);
	}

	public void onSwipeBottomToTop() {
		Session nextSession = getNextSession(getCurrentLocation());
		if (nextSession != null) {
			currentSession = nextSession;
			startActivityCurrentSession();
			overridePendingTransition(R.anim.slide_bottom_to_top, 0);
		} else {
			Toast.makeText(this, "No later session", Toast.LENGTH_LONG).show();
		}
	}

	public void onSwipeTopToBottom() {
		Session previousSession = getPreviousSession(getCurrentLocation());
		if (previousSession != null) {
			currentSession = previousSession;
			startActivityCurrentSession();
			overridePendingTransition(R.anim.slide_top_to_bottom, 0);
		} else {
			Toast.makeText(this, "No earlier session", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onSwipeLeftToRight() {
		getIntent().putExtra(IA_SESSION_START, currentSession.getStartTime().asMinutes());
		super.onSwipeLeftToRight();
	}

	@Override
	public void onSwipeRightToLeft() {
		getIntent().putExtra(IA_SESSION_START, currentSession.getStartTime().asMinutes());
		super.onSwipeRightToLeft();
	}

	private Session getNextSession(Location location) {
		Set<Session> sessionsSet = this.getConference().getSessions();
		ArrayList<Session> sessions = new ArrayList<Session>(sessionsSet);
		int index = -1;
		if (currentSession != null) {
			for (int i = 0; i < sessions.size(); i++) {
				if (currentSession.equals(sessions.get(i))) {
					index = i;
					break;
				}
			}
		}
		int max = sessions.size() - 1;
		while (index < max) {
			Session session = sessions.get(++index);
			if (session.isMandatory() || session.getLocation().equals(location)) {
				return session;
			}
		}
		return null;
	}

	private Session getPreviousSession(Location location) {
		Set<Session> sessionsSet = this.getConference().getSessions();
		ArrayList<Session> sessions = new ArrayList<Session>(sessionsSet);
		int index = -1;
		if (currentSession != null) {
			for (int i = 0; i < sessions.size(); i++) {
				if (currentSession.equals(sessions.get(i))) {
					index = i;
					break;
				}
			}
		}
		while (index > 0) {
			Session session = sessions.get(--index);
			if (session.isMandatory() || session.getLocation().equals(location)) {
				return session;
			}
		}
		return null;
	}

	private void startActivityCurrentSession() {
		Intent intent = getIntent();
		intent.putExtra(BaseActivity.IA_CONFERENCE, getConference().getId());
		intent.putExtra(BaseActivity.IA_SESSION, currentSession.getId());
		startActivity(intent);
		// Finish this activity to let the back button go directly to the overview page
		finish();
	}
}
