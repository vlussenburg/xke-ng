package com.xebia.xcoss.axcv.ui;

import java.util.ArrayList;

import com.xebia.xcoss.axcv.model.Remark;
import com.xebia.xcoss.axcv.model.Session;

public class FormatUtil {

	public static String getLabels(Session session) {
		ArrayList<String> labels = session.getLabels();
		if ( labels.size() == 0 ) {
			return null;
		}
		final String divider = ", ";
		StringBuilder sb = new StringBuilder();
		for (String label : labels) {
			sb.append(label);
			sb.append(divider);
		}
		return sb.substring(0, sb.length()-divider.length()).toString();
	}

	public static String getText(double rate) {
		return getText(rate, 1);
	}

	public static String getText(double rate, int precision) {
		double pow = Math.pow(10, 2);
		return String.valueOf(Math.round(rate*pow)/pow);
	}

	public static String getHtml(Remark[] remarks) {
		StringBuilder sb = new StringBuilder();
		for (Remark remark : remarks) {
			sb.append("<b>");
			sb.append(remark.getUser());
			sb.append("</b> - ");
			sb.append(remark.getComment());
			sb.append("<br/>");
		}
		return sb.toString();
	}
}
