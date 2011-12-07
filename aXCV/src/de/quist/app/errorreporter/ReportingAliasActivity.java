package de.quist.app.errorreporter;

import android.app.AliasActivity;
import android.os.Bundle;
import com.xebia.xcoss.axcv.R;

public class ReportingAliasActivity extends AliasActivity {

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
