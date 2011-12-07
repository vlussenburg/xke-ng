package de.quist.app.errorreporter;

import android.app.TabActivity;
import android.os.Bundle;
import com.xebia.xcoss.axcv.R;

public class ReportingTabActivity extends TabActivity {

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
