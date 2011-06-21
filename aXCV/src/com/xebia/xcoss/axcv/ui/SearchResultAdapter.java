package com.xebia.xcoss.axcv.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.opengl.Visibility;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xebia.xcoss.axcv.BaseActivity;
import com.xebia.xcoss.axcv.R;
import com.xebia.xcoss.axcv.logic.ConferenceServer;
import com.xebia.xcoss.axcv.model.Author;
import com.xebia.xcoss.axcv.model.Session;
import com.xebia.xcoss.axcv.util.XCS;

public class SearchResultAdapter extends BaseAdapter {

	private BaseActivity ctx;
	private List<Object> data;
	private ScreenTimeUtil timeUtil;
	private Map<Class<?>, Integer> classMapping;

	public SearchResultAdapter(BaseActivity context, List<Object> data) {
		this.ctx = context;
		this.data = data;
		this.timeUtil = new ScreenTimeUtil(context);
		this.classMapping = new HashMap<Class<?>, Integer>();
	}

	@Override
	public View getView(int paramInt, View paramView, ViewGroup parent) {

		Object selected = getItem(paramInt);
		LayoutInflater inflater = ctx.getLayoutInflater();
		View row = inflater.inflate(classMapping.get(selected.getClass()), parent, false);

		if (selected instanceof Author) {
			createAuthorView(row, (Author) selected);
		}
		if (selected instanceof Session) {
			createSessionView(row, (Session) selected);
		}

		return row;
	}

	public void createSessionView(View row, Session session) {

		int colorId = ctx.getResources().getColor(R.color.tc_itemdefault);

		TextView titleView = (TextView) row.findViewById(R.id.ses_title);
		TextView dateView = (TextView) row.findViewById(R.id.ses_date);
		TextView labelView = (TextView) row.findViewById(R.id.ses_labels);
		TextView ratingView = (TextView) row.findViewById(R.id.ses_rating);

		titleView.setText(session.getTitle());
		titleView.setTextColor(colorId);

		String list = FormatUtil.getList(session.getLabels(), false);
		labelView.setText(list);
		if ( StringUtil.isEmpty(list) ) {
			row.findViewById(R.id.ses_separator).setVisibility(View.GONE);
		} else {
			Linkify.addLinks(labelView, XCS.TAG.PATTERN, XCS.TAG.LINK);
			// TODO Blog worthy : Allows clickable links inside a listview item.
			// If linkified without, the list item will not be clickable any more...
			labelView.setFocusable(false);
		}
		if ( session.getDate() == null ) {
			row.findViewById(R.id.ses_separator).setVisibility(View.GONE);
			dateView.setVisibility(View.GONE);
		} else {
			dateView.setText(timeUtil.getAbsoluteDate(session.getDate()));
		}

		double rate = ConferenceServer.getInstance().getRate(session);
		ratingView.setText(FormatUtil.getText(rate));
	}

	// TODO Move to helper class
	public static void createAuthorView(View row, Author author) {

		TextView nameView = (TextView) row.findViewById(R.id.author_name);
		TextView mailView = (TextView) row.findViewById(R.id.author_mail);
		TextView phoneView = (TextView) row.findViewById(R.id.author_phone);

		nameView.setText(author.getName());
		mailView.setText(author.getMail());
		phoneView.setText(author.getPhone());

		Linkify.addLinks(mailView, Linkify.EMAIL_ADDRESSES);
		Linkify.addLinks(phoneView, Linkify.PHONE_NUMBERS);
		mailView.setFocusable(false);
		phoneView.setFocusable(false);

		// TODO : Author image
		// ImageView icon = (ImageView) row.findViewById(R.id.author_image);
		// icon.setImageBitmap(null);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int paramInt) {
		return data.get(paramInt);
	}

	@Override
	public long getItemId(int paramInt) {
		return paramInt;
	}

	public void addType(Class<?> class1, int layoutId) {
		classMapping.put(class1, layoutId);
	}
}
