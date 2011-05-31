package com.xebia.xcoss.axcv;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
import com.xebia.xcoss.axcv.util.XCS;

public class CVSessionView extends BaseActivity {

	private Conference conference;
	private Session session;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.session);

		conference = getConference();
		session = getSession(conference);

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

		sessionLocation.setText(session.getLocation().getLocation());
		sessionTitle.setText(session.getTitle());
		sessionDescription.setText(session.getDescription());
		sessionAuthor.setText(session.getAuthor());

		// Optional fields (hide when not available)
		updateTextField(R.id.scAudience, R.id.scAudienceLabel, session.getIntendedAudience());
		updateTextField(R.id.scLabels, R.id.scLabelsLabel, FormatUtil.getLabels(session));
		updateTextField(R.id.scLanguage, R.id.scLanguageLabel, session.getLanguage());
		updateTextField(R.id.scLimit, R.id.scLimitLabel, session.getLimit());
		updateTextField(R.id.scPreparation, R.id.scPreparationLabel, session.getPreparation());

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
//		view2.setText(server.getRemarks(session));
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
				text.setText(session.getTitle());

				Button submit = (Button) dialog.findViewById(R.id.drSubmit);
				final SeekBar seekbar = (SeekBar) dialog.findViewById(R.id.drSessionRate);
				final TextView rateText = (TextView) dialog.findViewById(R.id.drRateText);
				rateText.setText(RatingValue.message(seekbar.getProgress()));

				// Or use a DialogInterface.OnClickListener to directly access the dialog
				submit.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View paramView) {
						int rate = 1 + seekbar.getProgress();
						getConferenceServer().registerRate(rate);
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
				text.setText(session.getTitle());

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
		if (item.getItemId() == XCS.MENU.ADD) {
			// TODO : Session addition
			startActivity(new Intent(this, CVSettings.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}