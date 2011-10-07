package com.xebia.xcoss.axcv.ui;

import java.util.List;
import java.util.Set;

import com.xebia.xcoss.axcv.model.Remark;
import com.xebia.xcoss.axcv.util.StringUtil;

public class FormatUtil {

	public static final String NONE_FOUND = "<None>";
	private static final String LINE = System.getProperty("line.separator");
	
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

	public static <T> String getList(Set<T> data) {
		return getList(data, true);
	}

	public static <T> String getList(Set<T> data, boolean emptyIndication) {
		if ( data == null ) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (T value: data) {
			sb.append(value.toString());
			sb.append(", ");
		}
		if ( sb.length() > 0 ) {
			return sb.substring(0, sb.length()-2);
		}
		return emptyIndication ? NONE_FOUND : "";
	}

	public static CharSequence getText(String value) {
		if ( StringUtil.isEmpty(value) ) {
			return "<Specify value>";
		}
		return value;
	}

	public static String getText(List<String> messages) {
		StringBuilder sb = new StringBuilder();
		for (String string : messages) {
			sb.append(LINE);
			sb.append(" - ");
			sb.append(string);
		}
		return sb.toString();
	}

	public static String getText(Object object) {
		if ( object == null ) {
			return "<Specify value>";
		}
		return object.toString();
	}
}
