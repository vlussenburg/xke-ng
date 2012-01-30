package com.xebia.xcoss.axcv.util;



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

	public static void showCallStack()
	{
		StackTraceElement[] stackTraceElements =
			Thread.currentThread().getStackTrace();
		for (int i=2 ; i<stackTraceElements.length; i++)
		{
			StackTraceElement ste = stackTraceElements[i];
		    String classname = ste.getClassName();
		    String methodName = ste.getMethodName();
		    int lineNumber = ste.getLineNumber();
		    System.out.println(
		    	classname+"."+methodName+":"+lineNumber);
		}
	}
	

}
