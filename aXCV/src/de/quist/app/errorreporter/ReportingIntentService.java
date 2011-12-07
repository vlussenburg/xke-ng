/*
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at LICENSE.txt
 * or http://www.opensource.org/licenses/cddl1.php.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 * 
 * 
 * Copyright 2010 Tom Quist 
 * All rights reserved Use is subject to license terms.
 */
package de.quist.app.errorreporter;

import android.app.IntentService;
import com.xebia.xcoss.axcv.R;

public abstract class ReportingIntentService extends IntentService {

	public ReportingIntentService(String name) {
		super(name);
	}

	private ExceptionReporter exceptionReporter;

	protected ExceptionReporter getExceptionReporter() {
		return exceptionReporter;
	}

	@Override
	public void onCreate() {
		exceptionReporter = ExceptionReporter.register(this);
		super.onCreate();
	}

}
