package de.quist.app.errorreporter;

import android.app.LauncherActivity;
import android.os.Bundle;
import com.xebia.xcoss.axcv.R;

public abstract class ReportingLauncherActivity extends LauncherActivity {

	private ExceptionReporter exceptionReporter;

	protected ExceptionReporter getExceptionReporter() {
		return exceptionReporter;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		exceptionReporter = ExceptionReporter.register(this);
		super.onCreate(savedInstanceState);
	}
}
