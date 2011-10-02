package com.xebia.xcoss.axcv.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConferenceCacheImpl extends ConferenceCache {

	private HashMap<String, CachedObject<?>> cachedObjects;

	protected ConferenceCacheImpl() {
		this.cachedObjects = new HashMap<String, CachedObject<?>>();
	}

	@Override
	public <T> CachedObject<T> doGetCachedObject(String key, Class<T> type) {
		return (CachedObject<T>) cachedObjects.get(createCacheKey(key, type));
	}

	@Override
	public <T> List<CachedObject<T>> doGetCachedObjects(Class<T> type) {
		List<CachedObject<T>> result = new ArrayList<CachedObject<T>>();
		for (String key : cachedObjects.keySet()) {
			if (isKeyOfType(key, type)) {
				result.add((CachedObject<T>) cachedObjects.get(key));
			}
		}
		return result;
	}

	@Override
	public <T> void doPutCachedObject(String key, CachedObject<T> object) {
		cachedObjects.put(createCacheKey(key, object), object);
	}

	@Override
	public <T> void doRemoveCachedObject(String key, Class<T> type) {
		cachedObjects.remove(createCacheKey(key, type));
	}

	private String createCacheKey(String key, CachedObject<?> object) {
		return createCacheKey(key, object.object.getClass());
	}
	
	private String createCacheKey(String key, Class<?> type) {
		return type.getSimpleName() + "." + key;
	}

	private boolean isKeyOfType(String key, Class<?> type) {
		return key.startsWith(type.getSimpleName() + ".");
	}
}
