package com.xebia.xcoss.axcv.util;

import java.io.Closeable;

public class StreamUtil {

	public static void close(Closeable closeable) {
		try {
			closeable.close();
		}
		catch (Exception e) {
			// Ignore
		}
	}

}
