package com.xebia.xcoss.axcv.util;

import com.xebia.xcoss.axcv.R;


public class DebugUtil {

	public static String whoCalledMe() {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		StackTraceElement caller = stackTraceElements[4];
		String classname = caller.getClassName();
		int idx = classname.lastIndexOf(".");
		if (idx > 0) classname = classname.substring(++idx);
		String methodName = caller.getMethodName();
		// int lineNumber = caller.getLineNumber();
		return classname + "." + methodName;// + ":" + lineNumber;
	}

}
