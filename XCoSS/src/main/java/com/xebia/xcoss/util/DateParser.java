package com.xebia.xcoss.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class DateParser {

	private static final Collection<String> PARSE_PATTERNS = Arrays
			.asList(new String[] { "d-M-yyyy", "d-MMM-yyyy", "dMyyyy", "dMyy", "dd-MM-yyyy", "ddMMyyyy", "ddMMyy",
					"ddMM", "dd/MM", "yyyy", "MMyyyy", "MM-yyyy", "MM-yy" });

	public static DateRange parseDate(String dateValue) throws ParseException {

		if (dateValue == null) {
			throw new IllegalArgumentException("dateValue is null");
		}

		if ((dateValue.length() > 1) && (dateValue.startsWith("'")) && (dateValue.endsWith("'"))) {
			dateValue = dateValue.substring(1, dateValue.length() - 1);
		}
		SimpleDateFormat dateParser = null;
		Iterator<String> formatIter = PARSE_PATTERNS.iterator();
		while (formatIter.hasNext()) {
			String format = formatIter.next();
			if (dateParser == null) {
				dateParser = new SimpleDateFormat(format, Locale.getDefault());
				// dateParser.setTimeZone(TimeZone.getTimeZone("GMT"));
				// dateParser.set2DigitYearStart(startDate);
			} else {
				dateParser.applyPattern(format);
			}
			try {
				Date date = dateParser.parse(dateValue);
				return new DateRange(date, format);
			}
			catch (ParseException pe) {
			}
		}
		throw new ParseException("Unable to parse the date " + dateValue, 0);
	}
}
