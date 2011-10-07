package com.xebia.xcoss.axcv.logic.cache;

import java.io.Serializable;

class CachedObject<T> implements Serializable {
	public T object;
	public long moment;
	public boolean dirty;

	public CachedObject(T data) {
		this.object = data;
		this.dirty = false;
		this.moment = System.currentTimeMillis();
	}

	@Override
	public int hashCode() {
		return object == null ? 0 : object.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CachedObject<T> other = (CachedObject<T>) obj;
		return object == null ? other.object == null : object.equals(other.object);
	}
};
