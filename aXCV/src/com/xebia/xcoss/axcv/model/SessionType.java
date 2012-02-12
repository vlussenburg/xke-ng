package com.xebia.xcoss.axcv.model;

import java.util.ArrayList;
import java.util.List;

import com.xebia.xcoss.axcv.model.Session.Type;

public class SessionType {

	private Type key;
	private String name;
	private boolean mandatoryType;
	private boolean breakType;
	private boolean defaultType;

	private static final int DEFAULTTYPE = 0x01;
	private static final int MANDATORYTYPE = 0x02;
	private static final int BREAKTYPE = 0x04;

	private static List<SessionType> types = new ArrayList<SessionType>();

	public static void init(String[] input) {
		for (String string : input) {
			String[] split = string.split(":");
			SessionType st = new SessionType();
			st.key = Type.valueOf(split[0]);
			st.name = split[0];
			if (split.length > 1) {
				st.name = split[1];
			}
			if (split.length > 2) {
				int value = Integer.parseInt(split[2]);
				st.defaultType = (value & DEFAULTTYPE) != 0;
				st.mandatoryType = (value & MANDATORYTYPE) != 0;
				st.breakType = (value & BREAKTYPE) != 0;
			}
			types.add(st);
		}
	}
	
	private SessionType() {
	}

	public static SessionType get(Type type) {
		for (SessionType st : types) {
			if ( st.getType() == type ) {
				return st;
			}
		}
		return getDefaultType();
	}

	public boolean isBreak() {
		return breakType;
	}

	public boolean isMandatory() {
		return mandatoryType;
	}

	private boolean isDefault() {
		return defaultType;
	}

	public static SessionType[] getAllTypes() {
		return types.toArray(new SessionType[types.size()]);
	}

	public static SessionType getDefaultType() {
		for (SessionType type : types) {
			if (type.isDefault()) return type;
		}
		return null;
	}

	public static SessionType getBreakType() {
		for (SessionType type : types) {
			if (type.isBreak()) return type;
		}
		return types.get(0);
	}

	@Override
	public String toString() {
		return name;
	}
	
	public Type getType() {
		return key;
	}
}
