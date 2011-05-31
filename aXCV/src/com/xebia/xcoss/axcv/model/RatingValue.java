package com.xebia.xcoss.axcv.model;

public class RatingValue {

	private static final String[] VALUES = {
		/*1*/ "Very poor",
		/*2*/ "Poor",
		/*3*/ "Less than moderate",
		/*4*/ "Moderate",
		/*5*/ "Fair",
		/*6*/ "Adequate",
		/*7*/ "Good",
		/*8*/ "Very good",
		/*9*/ "Excellent",
		/*10*/ "Outstanding",
	};
	
	public static String message(int paramInt) {
		if ( paramInt >= 0 && paramInt < VALUES.length ) 
			return VALUES[paramInt];
		return "?";
	}

}
