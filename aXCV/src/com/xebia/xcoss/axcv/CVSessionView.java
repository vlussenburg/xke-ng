package com.xebia.xcoss.axcv;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.util.Linkify;
import android.util.Log;
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

import com.xebia.xcoss.axcv.layout.SwipeLayout;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.Location;
import com.xebia.xcoss.axcv.model.Rate;
import com.xebia.xcoss.axcv.model.Remark;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.tasks.RegisterRateTask;
import com.xebia.xcoss.axcv.tasks.RegisterRemarkTask;
import com.xebia.xcoss.axcv.tasks.RetrieveConferenceTask;
import com.xebia.xcoss.axcv.tasks.RetrieveRateTask;
import com.xebia.xcoss.axcv.tasks.RetrieveRemarksTask;
import com.xebia.xcoss.axcv.tasks.TaskCallBack;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.util.FormatUtil;
import com.xebia.xcoss.axcv.util.StringUtil;
import com.xebia.xcoss.axcv.util.XCS;

/**
 * IA_CONFERENCE_ID - ID of selected conference (required by parent).
 * IA_SESSION_ID - ID of selected conference (optional). IA_LOCATION_ID - ID of
 * selected location (optional by parent). IA_SESSION_START - ID of selected
 * session (optional).
 * 
 * @author Michael
 */

public class CVSessionView extends SessionSwipeActivity {

	private Session currentSession;
	private Conference currentConference;
	private Timer timer = new Timer();
	private TimerTask timerTask;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.sessionview);
		super.onCreate(savedInstanceState);
		((SwipeLayout) findViewById(R.id.swipeLayout)).setGestureListener(this);
		LinearLayout layout = (LinearLayout) findViewById(R.id.sc_content_layout);
		layout.setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onPause() {
		cancelTasks();
		super.onPause();
	}

	private void cancelTasks() {
		if (timerTask != null) {
			timerTask.cancel();
		}
		if (timer != null) {
			// timer.cancel();
			timer.purge();
		}
		// timer = new Timer();
	}

	@Override
	protected void onResume() {
		new RetrieveConferenceTask(R.string.action_retrieve_conference, this,
				new TaskCallBack<Conference>() {
					@Override
					public void onCalled(Conference conference) {
						if (conference != null) {
							currentConference = conference;
							updateLocations(conference);

							currentSession = determineSelectedSession(conference);
							if (currentSession != null) {
								updateCurrentLocation(currentSession);
								fill();
								scheduleRateAndReviewRefresh();
							}
							updateLocationNavigation();
							updatePreviousAndNextSessionButtons();
						} else {
							// TODO The CVTask currently shows a dialog, which
							// will leak when finishing...
							finish();
						}
					}
				}).execute(getConferenceId());
		super.onResume();
	}

	private void scheduleRateAndReviewRefresh() {
		cancelTasks();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				updateRateAndReview();
			}
		};
		timer.schedule(timerTask, 1500, 30000);
	}

	private void fill() {
		TextView title = (TextView) findViewById(R.id.conferenceTitle);
		title.setText(currentConference.getTitle());

		TextView date = (TextView) findViewById(R.id.conferenceDate);
		ScreenTimeUtil timeUtil = new ScreenTimeUtil(this);
		String val = timeUtil.getAbsoluteDate(currentConference.getStartTime());
		date.setText(val);

		if (currentSession != null) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.sc_content_layout);
			layout.setVisibility(View.VISIBLE);
			TextView sessionDate = (TextView) findViewById(R.id.sessionTime);
			StringBuilder sb = new StringBuilder();
			sb.append(timeUtil.getAbsoluteTime(currentSession.getStartTime()));
			sb.append(" - "); //$NON-NLS-1$
			sb.append(timeUtil.getAbsoluteTime(currentSession.getEndTime()));
			sessionDate.setText(sb.toString());

			TextView sessionLocation = ((TextView) findViewById(R.id.sessionLocation));
			TextView sessionTitle = (TextView) findViewById(R.id.scTitle);
			TextView sessionDescription = (TextView) findViewById(R.id.scDescription);
			TextView sessionAuthor = (TextView) findViewById(R.id.scAuthor);

			// sessionLocation.setText(currentSession.getLocation().getDescription());
			sessionLocation.setText(getCurrentLocation().getDescription());
			sessionTitle.setText(currentSession.getTitle());
			sessionDescription.setText(currentSession.getDescription());
			sessionAuthor.setText(FormatUtil.getList(currentSession
					.getAuthors()));

			// Optional fields (hide when not available)
			updateTextField(R.id.scAudience, R.id.scAudienceLabel,
					currentSession.getIntendedAudience());
			updateTextField(R.id.scLabels, R.id.scLabelsLabel,
					FormatUtil.getList(currentSession.getLabels(), false));
			updateTextField(R.id.scLanguage, R.id.scLanguageLabel,
					FormatUtil.getList(currentSession.getLanguages(), false));
			updateTextField(R.id.scLimit, R.id.scLimitLabel,
					currentSession.getLimit());
			updateTextField(R.id.scPreparation, R.id.scPreparationLabel,
					currentSession.getPreparation());

			TextView labelView = (TextView) findViewById(R.id.scLabels);
			Linkify.addLinks(labelView, XCS.TAG.PATTERN, XCS.TAG.LINK);
			if (currentSession.getAuthors().size() > 0) {
				Linkify.addLinks(sessionAuthor, XCS.AUTHOR.PATTERN,
						XCS.AUTHOR.LINK);
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
			if (currentSession.isBreak() || StringUtil.isEmpty(getUser())
					|| currentSession.isExpired()) {
				button.setVisibility(View.GONE);
			} else {
				getMyApplication().markSession(currentSession, button, false);
				button.setVisibility(View.VISIBLE);
				button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						getMyApplication().markSession(currentSession, view,
								true);
					}
				});
			}
		}
	}

	private void updateRateAndReview() {
		if (currentSession == null
				|| StringUtil.isEmpty(currentSession.getId())) {
			return;
		}
		Log.i(XCS.LOG.COMMUNICATE, "Updating rate and reviews"); //$NON-NLS-1$
		new RetrieveRateTask(R.string.action_retrieve_rate, this,
				new TaskCallBack<Rate>() {
					@Override
					public void onCalled(Rate result) {
						if (result != null) {
							TextView view = (TextView) findViewById(R.id.scRating);
							view.setText(FormatUtil.getText(result));
						}
					}
				}).silent().execute(currentSession.getId());

		new RetrieveRemarksTask(R.string.action_retrieve_remarks, this,
				new TaskCallBack<List<Remark>>() {
					@Override
					public void onCalled(List<Remark> result) {
						TextView view = (TextView) findViewById(R.id.scComments);
						Spanned spannedContent = Html.fromHtml(FormatUtil
								.getHtml(result));
						view.setText(spannedContent, BufferType.SPANNABLE);
					}
				}).silent().execute(currentSession.getId());
	}

	private void updatePreviousAndNextSessionButtons() {
		Session session = getNextSession(getCurrentLocation());
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
		Log.i(XCS.LOG.NAVIGATE, "Previous session = " + session); //$NON-NLS-1$
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
		if (currentSession != null) {
			Dialog dialog = null;
			switch (id) {
			case XCS.DIALOG.ADD_RATING:
				dialog = new Dialog(this);
				dialog.setContentView(R.layout.dialog_rating);
				dialog.setTitle(R.string.your_rating);
				TextView text = (TextView) dialog
						.findViewById(R.id.drSessionTitle);
				text.setText(currentSession.getTitle());

				Button submit = (Button) dialog.findViewById(R.id.drSubmit);
				final RatingBar ratingBar = (RatingBar) dialog
						.findViewById(R.id.drSessionRate);
				final TextView rateText = (TextView) dialog
						.findViewById(R.id.drRateText);
				rateText.setText(new Rate(ratingBar, null).getMessage());

				// Or use a DialogInterface.OnClickListener to directly access
				// the dialog
				submit.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View paramView) {
						Rate rate = new Rate(ratingBar, currentSession.getId());
						if (rate.isRated()) {
							new RegisterRateTask(R.string.action_register_rate,
									CVSessionView.this).execute(rate);
							if (timer != null)
								scheduleRateAndReviewRefresh();
						}
						dismissDialog(XCS.DIALOG.ADD_RATING);
					}
				});

				ratingBar
						.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

							@Override
							public void onRatingChanged(RatingBar ratingBar,
									float rating, boolean fromUser) {
								rateText.setText(new Rate(ratingBar, null)
										.getMessage());
							}
						});
				return dialog;
			case XCS.DIALOG.CREATE_REVIEW:
				dialog = new Dialog(this);
				dialog.setContentView(R.layout.dialog_review);
				dialog.setTitle(R.string.your_remark);
				text = (TextView) dialog.findViewById(R.id.dvSessionTitle);
				text.setText(currentSession.getTitle());

				submit = (Button) dialog.findViewById(R.id.dvSubmit);
				final TextView edit = (TextView) dialog
						.findViewById(R.id.dvEditText);

				// Or use a DialogInterface.OnClickListener to directly access
				// the dialog
				submit.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View paramView) {
						Remark remark = new Remark(getUser(), edit.getText()
								.toString(), currentSession.getId());
						edit.setText("");
						new RegisterRemarkTask(R.string.action_register_remark,
								CVSessionView.this).execute(remark);
						dismissDialog(XCS.DIALOG.CREATE_REVIEW);
						if (timer != null)
							scheduleRateAndReviewRefresh();
					}
				});
				return dialog;
			}
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
	protected void populateMenuOptions(ArrayList<Integer> list) {
		if (currentConference != null && !currentConference.isExpired()) {
			list.add(XCS.MENU.ADD);
		}
		if (currentSession != null && !currentSession.isExpired()) {
			list.add(XCS.MENU.EDIT);
		}
		list.add(XCS.MENU.LIST);
		list.add(XCS.MENU.SETTINGS);
		list.add(XCS.MENU.SEARCH);
		list.add(XCS.MENU.TRACK);
		list.add(XCS.MENU.RUNNING);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Add or edit a session
		Intent intent = new Intent(this, CVSessionAdd.class);
		intent.putExtra(BaseActivity.IA_CONFERENCE_ID, getConferenceId());

		switch (item.getItemId()) {
		case XCS.MENU.ADD:
			startActivity(intent);
			return true;
		case XCS.MENU.EDIT:
			if (currentSession != null) {
				intent.putExtra(BaseActivity.IA_SESSION, currentSession.getId());
				startActivity(intent);
			}
			return true;
		case XCS.MENU.LIST:
			intent = new Intent(this, CVSessionList.class);
			intent.putExtra(IA_LOCATION_ID, currentLocation);
			intent.putExtra(IA_CONFERENCE_ID, getConferenceId());
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onSwipeBottomToTop() {
		Session nextSession = getNextSession(getCurrentLocation());
		if (nextSession != null) {
			currentSession = nextSession;
			startActivityCurrentSession();
			overridePendingTransition(R.anim.slide_bottom_to_top, 0);
		} else {
			Toast.makeText(this, R.string.no_later_session, Toast.LENGTH_LONG)
					.show();
		}
	}

	public void onSwipeTopToBottom() {
		Session previousSession = getPreviousSession(getCurrentLocation());
		if (previousSession != null) {
			currentSession = previousSession;
			startActivityCurrentSession();
			overridePendingTransition(R.anim.slide_top_to_bottom, 0);
		} else {
			Toast.makeText(this, R.string.no_earlier_session, Toast.LENGTH_LONG)
					.show(); //$NON-NLS-1$
		}
	}

	@Override
	public void onSwipeLeftToRight() {
		if (currentSession != null) {
			getIntent().putExtra(IA_SESSION_START,
					currentSession.getStartTime().asMinutes());
		}
		super.onSwipeLeftToRight();
	}

	@Override
	public void onSwipeRightToLeft() {
		if (currentSession != null) {
			getIntent().putExtra(IA_SESSION_START,
					currentSession.getStartTime().asMinutes());
		}
		super.onSwipeRightToLeft();
	}

	private Session getNextSession(Location location) {
		Set<Session> sessionsSet = currentConference.getSessions();
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
		Set<Session> sessionsSet = currentConference.getSessions();
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
		intent.putExtra(IA_CONFERENCE_ID, getConferenceId());
		intent.putExtra(IA_SESSION, currentSession.getId());
		intent.putExtra(IA_LOCATION_ID, currentLocation);
		startActivity(intent);
		// Finish this activity to let the back button go directly to the
		// overview page
		finish();
	}

	public Session determineSelectedSession(Conference conference) {
		Session session = getSelectedSession(conference);

		if (session == null && getCurrentLocation() != null) {
			// Find all sessions on this particular location
			ArrayList<Session> options = new ArrayList<Session>();
			for (Session s : conference.getSessions()) {
				if (s.isMandatory()
						|| s.getLocation().equals(getCurrentLocation())) {
					options.add(s);
				}
			}
			// Now find the first session starting from this time
			int delta = Integer.MAX_VALUE;
			int start = getIntent().getIntExtra(IA_SESSION_START, 0);
			for (Session option : options) {
				int td = option.getStartTime().asMinutes() - start;
				if (td > 0 && td < delta) {
					session = option;
					delta = td;
				}
			}
			if (session == null && options.size() > 0) {
				session = options.get(options.size() - 1);
			}
		}
		return session;
	}
}
