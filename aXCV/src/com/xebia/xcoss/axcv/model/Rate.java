package com.xebia.xcoss.axcv.model;

import java.util.List;

public class Rate {
	
	@SuppressWarnings("unused")
	private int rate;
	// This value is not included in the JSON
	private transient String value;

	public Rate(int rate) {
		this.rate = rate;
		this.value = String.valueOf(rate);
	}

	public Rate(List<Integer> list) {
		if (list == null || list.size() == 0) {
			value = "n/a";
		} else {
			int total = 0;
			for (Integer i : list) {
				total += i;
			}
			value = String.valueOf((total * 10 / list.size()) / 10.0);
		}
	}

	public String toString() {
		return value;
	}
}
