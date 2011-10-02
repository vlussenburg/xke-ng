package com.xebia.xcoss.axcv.util;

import java.lang.Thread.UncaughtExceptionHandler;

import android.util.Log;

import com.xebia.xcoss.axcv.BaseActivity;

import de.quist.app.errorreporter.ExceptionReporter;

public class ProxyExceptionReporter implements UncaughtExceptionHandler {

//	public static final String TOKEN = "com.xebia.Exception";

	private static ProxyExceptionReporter instance = new ProxyExceptionReporter();
	
	private ExceptionReporter exceptionReporter;
	
	public static void register(BaseActivity ctx) {
		UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
		if ( !(handler instanceof ProxyExceptionReporter) ) {
			Log.v(XCS.LOG.ALL, "Current uncaugt handler = " + handler);
			instance.exceptionReporter = ExceptionReporter.register(ctx);
			Thread.setDefaultUncaughtExceptionHandler(instance);
		}
	}

	@Override
	public void uncaughtException(Thread t, Throwable throwable) {
		Log.e(XCS.LOG.ALL, "[FATAL] Fault in application: ", throwable);
		exceptionReporter.reportException(t, throwable);

        // System.exit causes an undesired restart of only the current activity.
		// This causes the exit strategy not to work (there is no CVSplashLoader)
		// Also, if the server is down, it keeps on crashing...
        // Since other mechanisms cause hangs, still using this, but correcting it...
		System.exit(-1);
	}

}
