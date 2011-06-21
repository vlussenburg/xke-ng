package com.xebia.xcoss.axcv;

import java.util.ArrayList;
import java.util.SortedSet;

import android.app.Dialog;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.model.Conference;
import com.xebia.xcoss.axcv.model.RatingValue;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.ui.FormatUtil;
import com.xebia.xcoss.axcv.ui.ScreenTimeUtil;
import com.xebia.xcoss.axcv.ui.StringUtil;
import com.xebia.xcoss.axcv.ui.SwipeActivity;
import com.xebia.xcoss.axcv.util.XCS;
import com.xebia.xcoss.axcv.util.XCS.LOG;

public class CVSessionView extends BaseActivity implements OnGesturePerformedListener {

	private Session currentSession;
	private GestureLibrary mLibrary;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.session);
		mLibrary = GestureLibraries.fromRawResource(this, R.raw.swipegestures);
        if (!mLibrary.load()) {
        	finish();
        }
        Log.i(XCS.LOG.SWIPE, "mLibrary.getGestureEntries()" + mLibrary.getGestureEntries().size());

        GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.swipegestures);
        gestures.addOnGesturePerformedListener(this);
        
		Conference conference = getConference();
		currentSession = getSession(conference);

		fill(conference, currentSession);
		super.onCreate(savedInstanceState);
	}

	private void fill(Conference conference, Session session) {
		TextView title = (TextView) findViewById(R.id.conferenceTitle);
		title.setText(conference.getTitle());

		TextView date = (TextView) findViewById(R.id.conferenceDate);
		ScreenTimeUtil timeUtil = new ScreenTimeUtil(this);
		String val = timeUtil.getAbsoluteDate(conference.getDate());
		date.setText(val);

		TextView sessionDate = (TextView) findViewById(R.id.sessionTime);
		StringBuilder sb = new StringBuilder();
		sb.append(timeUtil.getAbsoluteTime(session.getStartTime()));
		sb.append(" - ");
		sb.append(timeUtil.getAbsoluteTime(session.getEndTime()));
		sessionDate.setText(sb.toString());

		TextView sessionLocation = ((TextView) findViewById(R.id.sessionLocation));
		TextView sessionTitle = (TextView) findViewById(R.id.scTitle);
		TextView sessionDescription = (TextView) findViewById(R.id.scDescription);
		TextView sessionAuthor = (TextView) findViewById(R.id.scAuthor);

		sessionLocation.setText(session.getLocation().getDescription());
		sessionTitle.setText(session.getTitle());
		sessionDescription.setText(session.getDescription());
		sessionAuthor.setText(FormatUtil.getList(session.getAuthors()));

		// Optional fields (hide when not available)
		updateTextField(R.id.scAudience, R.id.scAudienceLabel, session.getIntendedAudience());
		updateTextField(R.id.scLabels, R.id.scLabelsLabel, FormatUtil.getList(session.getLabels(), false));
		updateTextField(R.id.scLanguage, R.id.scLanguageLabel, FormatUtil.getList(session.getLanguages()));
		updateTextField(R.id.scLimit, R.id.scLimitLabel, session.getLimit());
		updateTextField(R.id.scPreparation, R.id.scPreparationLabel, session.getPreparation());

		TextView labelView = (TextView) findViewById(R.id.scLabels);
		Linkify.addLinks(labelView, XCS.TAG.PATTERN, XCS.TAG.LINK);

		ConferenceServer server = getConferenceServer();

		OnClickListener lRate = new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				showDialog(XCS.DIALOG.ADD_RATING);
			}
		};
		TextView view = (TextView) findViewById(R.id.scRating);
		view.setOnClickListener(lRate);
		view.setText(FormatUtil.getText(server.getRate(session)));

		findViewById(R.id.scRatingLayout).setOnClickListener(lRate);

		OnClickListener lReview = new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				showDialog(XCS.DIALOG.CREATE_REVIEW);
			}
		};
		TextView view2 = (TextView) findViewById(R.id.scComments);
		view2.setOnClickListener(lReview);
		Spanned spannedContent = Html.fromHtml(FormatUtil.getHtml(server.getRemarks(session)));
		view2.setText(spannedContent, BufferType.SPANNABLE);
		ImageView button = (ImageView) findViewById(R.id.sessionMarkButton);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.e(LOG.ALL, "Clicked on " + view);
			}
		});
		
		// Slide buttons
		ImageView im;
		im = (ImageView) findViewById(R.id.slideLocationMin);
		im.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				onSwipeLeftToRight();
			}
		});
		im = (ImageView) findViewById(R.id.slideLocationPlus);
		im.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				onSwipeRightToLeft();
			}
		});
		im = (ImageView) findViewById(R.id.slideTimeMin);
		im.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				onSwipeTopToBottom();
			}
		});
		im = (ImageView) findViewById(R.id.slideTimePlus);
		im.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				onSwipeBottomToTop();
			}
		});
		
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
				final SeekBar seekbar = (SeekBar) dialog.findViewById(R.id.drSessionRate);
				final TextView rateText = (TextView) dialog.findViewById(R.id.drRateText);
				rateText.setText(RatingValue.message(seekbar.getProgress()));

				// Or use a DialogInterface.OnClickListener to directly access the dialog
				submit.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View paramView) {
						int rate = 1 + seekbar.getProgress();
						getConferenceServer().registerRate(currentSession, rate);
						dismissDialog(XCS.DIALOG.ADD_RATING);
					}
				});
				
				seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar paramSeekBar) {
					}
					
					@Override
					public void onStartTrackingTouch(SeekBar paramSeekBar) {
					}
					
					@Override
					public void onProgressChanged(SeekBar paramSeekBar, int paramInt, boolean paramBoolean) {
						rateText.setText(RatingValue.message(paramInt));
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
						getConferenceServer().registerRemark(remark);
						dismissDialog(XCS.DIALOG.CREATE_REVIEW);
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
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Add or edit a session
		Intent intent = new Intent(this, CVSessionAdd.class);
		intent.putExtra(BaseActivity.IA_CONFERENCE, getConference().getId());

		if (item.getItemId() == XCS.MENU.ADD) {
			startActivity(intent);
			return true;
		}
		if (item.getItemId() == XCS.MENU.EDIT) {
			intent.putExtra(BaseActivity.IA_SESSION, currentSession.getId());
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onSwipeBottomToTop() {		
		SortedSet<Session> sessionsSet = this.getConference().getSessions();
		ArrayList<Session> sessions = new ArrayList<Session>(sessionsSet); 
		int index = sessions.indexOf(currentSession);
		if (index < sessions.size() -1) {
			currentSession = sessions.get(++index);
			fill(this.getConference(), currentSession);
		}
	}

	public void onSwipeLeftToRight() {
		// TODO Auto-generated method stub
		
	}

	public void onSwipeRightToLeft() {
		// TODO Auto-generated method stub
	}

	public void onSwipeTopToBottom() {
		SortedSet<Session> sessionsSet = this.getConference().getSessions();
		ArrayList<Session> sessions = new ArrayList<Session>(sessionsSet); 
		int index = sessions.indexOf(currentSession);
		if (index > 0) {
			currentSession = sessions.get(--index);
			fill(this.getConference(), currentSession);
		}
	}

	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		Log.i(XCS.LOG.SWIPE, "predictions.size=" + predictions.size());
		if (predictions.size() > 0) {
			if (predictions.get(0).score > 1.0) {
				String action = predictions.get(0).name;
				if ("onSwipeBottomToTop".equals(action)) {
					Log.i(XCS.LOG.SWIPE, "onSwipeBottomToTop!");
					this.onSwipeBottomToTop();
				} else if ("onSwipeTopToBottom".equals(action)) {
					Log.i(XCS.LOG.SWIPE, "onSwipeTopToBottom!");
					this.onSwipeTopToBottom();
				} else if ("onSwipeRightToLeft".equals(action)) {
					Log.i(XCS.LOG.SWIPE, "onSwipeRightToLeft!");
					this.onSwipeRightToLeft();
				} else if ("onSwipeLeftToRight".equals(action)) {
					Log.i(XCS.LOG.SWIPE, "onSwipeLeftToRight!");
					this.onSwipeLeftToRight();
				}
			}
		}
	}
}
