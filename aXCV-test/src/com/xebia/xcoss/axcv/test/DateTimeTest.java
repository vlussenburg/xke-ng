package com.xebia.xcoss.axcv.test;

import java.util.Date;

import com.xebia.xcoss.axcv.model.Moment;

import android.test.AndroidTestCase;

public class DateTimeTest extends AndroidTestCase {

	public DateTimeTest() {
	}
	
	public void testCreateNewDate() {
		Moment moment = new Moment();
		Date now = new Date();
		assertEquals(1900+now.getYear(), (int)moment.getYear());
		assertEquals(1+now.getMonth(), (int)moment.getMonth());
		assertEquals(now.getDay(), (int)moment.getDay());
		assertEquals(now.getMinutes(), (int)moment.getMinute());
		assertEquals(now.getHours(), (int)moment.getHour());
	}
	
	public void testA() {
		Moment moment = new Moment();
		Moment copy = new Moment(moment);
		assertTrue(moment.asMinutes() > 0);
		assertTrue(moment.compare(copy) == 0);
		// is in millis
//		assertTrue(moment.isAfterNow());
		assertFalse(moment.isAfter(copy));
		assertFalse(moment.isBefore(copy));
		assertEquals("non", moment.toString());
	}

}
