package com.xebia.xcoss.util;

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;

public class DateRange {
	
	public enum Block {
		YEAR, MONTH, DAY
	}
	
	private DateTime fromDate;
	private DateTime toDate;

	public DateRange(Date date, Block block, int span) {
		DateTime dd = new DateTime(date);
		
		switch (block) {
			case YEAR:
				fromDate = new DateTime(dd.getYear(), 1, 1, 0, 0, 0, 0);
				toDate = new DateTime(fromDate).plusYears(span);
				break;
			case MONTH:
				fromDate = new DateTime(dd.getYear(), dd.getMonthOfYear(), 1, 0, 0, 0, 0);
				toDate = new DateTime(fromDate).plusMonths(span);
				break;
			case DAY:
				fromDate = new DateTime(dd.getYear(), dd.getMonthOfYear(), dd.getDayOfMonth(), 0, 0, 0, 0);
				toDate = new DateTime(fromDate).plusDays(span);
				break;
			default:
				throw new RuntimeException("Invalid date range");
		}
	}

	public DateRange(Date date, String format) {
		this.fromDate = new DateTime(date);
		this.toDate = new DateTime(date);
		
		if (format.indexOf('m') >= 0) {
			toDate.plusMinutes(1);
		} else if (format.indexOf('H') >= 0) {
			toDate.plusHours(1);
		} else if (format.indexOf('k') >= 0) {
			toDate.plusHours(1);
		} else if (format.indexOf('h') >= 0) {
			toDate.plusHours(1);
		} else if (format.indexOf('K') >= 0) {
			toDate.plusHours(1);
		} else if (format.indexOf('a') >= 0) {
			toDate.plusHours(12);
		} else if (format.indexOf('d') >= 0) {
			toDate.plusDays(1);
		} else if (format.indexOf('D') >= 0) {
			toDate.plusDays(1);
		} else if (format.indexOf('E') >= 0) {
			toDate.plusDays(1);
		} else if (format.indexOf('F') >= 0) {
			toDate.plusDays(1);
		} else if (format.indexOf('W') >= 0) {
			toDate.plusWeeks(1);
		} else if (format.indexOf('w') >= 0) {
			toDate.plusWeeks(1);
		} else if (format.indexOf('M') >= 0) {
			toDate.plusMonths(1);
		} else if (format.indexOf('y') >= 0) {
			toDate.plusYears(1);
		}
	}

	public Date getFromDate() {
		return fromDate.toDate();
	}

	public Date getToDate() {
		return toDate.toDate();
	}
}
