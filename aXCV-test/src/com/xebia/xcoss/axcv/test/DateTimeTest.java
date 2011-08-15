package com.xebia.xcoss.axcv.test;

import hirondelle.date4j.DateTime;
import android.test.AndroidTestCase;

import com.xebia.xcoss.axcv.logic.gson.GsonDateTimeAdapter;

public class DateTimeTest extends AndroidTestCase {

	public DateTimeTest() {
	}
	
	public void testDateOnly() {
		DateTime extractDate = GsonDateTimeAdapter.extractDate("2011-03-01T00:00:00.000Z");
		assertEquals("2011-03-01", extractDate.toString());
		assertEquals(false, extractDate.hasHourMinuteSecond());
		assertEquals(true, extractDate.hasYearMonthDay());
	}

	public void testTimeOnly() {
		DateTime extractDate = GsonDateTimeAdapter.extractDate("0000-00-00T12:01:01.345Z");
		assertEquals("12:01:01", extractDate.toString());
		assertEquals(true, extractDate.hasHourMinuteSecond());
		assertEquals(false, extractDate.hasYearMonthDay());
	}

	public void testNoTimezone() {
		DateTime extractDate = GsonDateTimeAdapter.extractDate("2011-03-01T12:01:01.345");
		assertEquals("2011-03-01 12:01:01", extractDate.toString());
	}

	public void testStandardTimezone() {
		DateTime extractDate = GsonDateTimeAdapter.extractDate("2011-03-01T12:01:01.345Z");
		assertEquals("2011-03-01 12:01:01", extractDate.toString());
	}

	public void testShiftPlusTwoTimezone() {
		DateTime extractDate = GsonDateTimeAdapter.extractDate("2011-03-01T12:01:01.345+02:00");
		assertEquals("2011-03-01 12:01:01", extractDate.toString());
	}

	public void testShiftMinusTwoTimezone() {
		DateTime extractDate = GsonDateTimeAdapter.extractDate("2011-03-01T12:01:01.345-02:00");
		assertEquals("2011-03-01 12:01:01", extractDate.toString());
	}

	public void testBeforeNoon() {
		DateTime extractDate = GsonDateTimeAdapter.extractDate("2011-03-01T08:01:01.345");
		assertEquals("2011-03-01 08:01:01", extractDate.toString());
	}

}
