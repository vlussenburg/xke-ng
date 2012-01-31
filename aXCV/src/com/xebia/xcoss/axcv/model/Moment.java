package com.xebia.xcoss.axcv.model;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import android.util.Log;

import com.xebia.xcoss.axcv.util.XCS;

public class Moment implements Serializable {
	private static final long serialVersionUID = -3196519454517487299L;

	private Integer year;
	private Integer month;
	private Integer day;
	private Integer hour;
	private Integer minute;

	class FixedMoment extends Moment {
		private static final long serialVersionUID = -3196519454517487299L;

		public FixedMoment(Moment clone) {
			super(clone);
		}
		
		public void setDate(int year, int month, int day) {
			Log.e(XCS.LOG.DATA, "Date cannot be set on a fixed moment!");
		}

		public void setTime(int hour, int minute) {
			Log.e(XCS.LOG.DATA, "Time cannot be set on a fixed moment!");
		}
	}
	
	public Moment(Moment clone) {
		this.year = clone.year;
		this.month = clone.month;
		this.day = clone.day;
		this.hour = clone.hour;
		this.minute = clone.minute;
	}

	public Moment(int hour, int minute) {
		this();
		setTime(hour, minute);
	}

	public Moment(int yr, int mon, int dy) {
		setDate(yr, mon, dy);
		setTime(0, 0);
	}

	public Moment() {
		DateTime now = DateTime.now();
		setTime(now.getHourOfDay(), now.getMinuteOfHour());
		setDate(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth());
	}

	public void setDate(int year, int month, int day) {
		this.day = day;
		this.month = month;
		this.year = year;
	}

	public void setDate(Moment moment) {
		this.day = moment.day;
		this.month = moment.month;
		this.year = moment.year;
	}

	public void setTime(int hour, int minute) {
		this.hour = hour;
		this.minute = minute;
	}

	public int asMinutes() {
		int result = hour == null ? 0 : hour * 60;
		if (minute != null) {
			result += minute;
		}
		return result;
	}

	public Moment plusMinutes(int length) {
		if (minute == null || hour == null) {
			throw new IllegalArgumentException("Moment has no valid time!");
		}
		int minuteValue = minute + length;
		int hourValue = hour;
		if (minuteValue >= 60) {
			hourValue += (minuteValue / 60);
			minuteValue = minuteValue % 60;
		}
		return new Moment(hourValue, minuteValue);
	}

	private DateTime getDate() {
		return new DateTime(year, month, day, hour, minute);
	}

	public Integer getYear() {
		return year;
	}

	public Integer getMonth() {
		return month;
	}

	public Integer getDay() {
		return day;
	}

	public Integer getHour() {
		return hour;
	}

	public Integer getMinute() {
		return minute;
	}

	public String toString() {
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		return fmt.print(getDate());
	}

	public boolean isBeforeNow() {
		return getDate().isBeforeNow();
	}

	public boolean isBeforeToday() {
		DateTime setCopy = DateTime.now().hourOfDay().setCopy(0).minuteOfHour().setCopy(0);
		return getDate().isBefore(setCopy);
	}

	public boolean isAfterNow() {
		return getDate().isAfter(new DateTime());
	}

	public boolean isAfterToday() {
		DateTime setCopy = DateTime.now().hourOfDay().setCopy(23).minuteOfHour().setCopy(59);
		return getDate().isAfter(setCopy);
	}

	public boolean isAfter(Moment dt) {
		return getDate().isAfter(dt.getDate());
	}

	public boolean isBefore(Moment dt) {
		return getDate().isBefore(dt.getDate());
	}

	public int compare(Moment m2) {
		if (m2 == null) {
			return 1;
		}
		return getDate().compareTo(m2.getDate());
	}

	public long asLong() {
		return getDate().getMillis();
	}

	public int getDaysFromNow() {
		DateTime now = DateTime.now();
		DateTime ths = getDate();
		int yearDiff = ths.getYear() - now.getYear();
		int daysDiff = ths.getDayOfYear() - now.getDayOfYear();
		return yearDiff*365 + daysDiff;
	}

	public int getYearOffset() {
		DateTime now = DateTime.now();
		DateTime ths = getDate();
		return ths.getYear() - now.getYear();
	}
	
	public static Moment fromString(String timeValue) {
		DateTime dt = DateTime.parse(timeValue);
		DateTime dtLocal = dt.withZone(DateTimeZone.getDefault());
		Moment moment = new Moment(dtLocal.getHourOfDay(), dtLocal.getMinuteOfHour());
		moment.setDate(dtLocal.getYear(), dtLocal.getMonthOfYear(), dtLocal.getDayOfMonth());
		return moment;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((day == null) ? 0 : day.hashCode());
		result = prime * result + ((hour == null) ? 0 : hour.hashCode());
		result = prime * result + ((minute == null) ? 0 : minute.hashCode());
		result = prime * result + ((month == null) ? 0 : month.hashCode());
		result = prime * result + ((year == null) ? 0 : year.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Moment other = (Moment) obj;
		if (day == null) {
			if (other.day != null) return false;
		} else if (!day.equals(other.day)) return false;
		if (hour == null) {
			if (other.hour != null) return false;
		} else if (!hour.equals(other.hour)) return false;
		if (minute == null) {
			if (other.minute != null) return false;
		} else if (!minute.equals(other.minute)) return false;
		if (month == null) {
			if (other.month != null) return false;
		} else if (!month.equals(other.month)) return false;
		if (year == null) {
			if (other.year != null) return false;
		} else if (!year.equals(other.year)) return false;
		return true;
	}
}
