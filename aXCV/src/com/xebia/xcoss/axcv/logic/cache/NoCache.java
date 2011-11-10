package com.xebia.xcoss.axcv.logic.cache;

import java.util.ArrayList;
import java.util.List;

import com.xebia.xcoss.axcv.logic.ProfileManager;

import android.content.Context;

public class NoCache extends DataCache {

	public NoCache(Context ctx) {
		super(ctx);
		new ProfileManager(ctx).removeAllCache();
	}

	@Override
	public <T> CachedObject<T> doGetCachedObject(String key, Class<T> type) {
		return null;
	}

	@Override
	public <T> List<CachedObject<T>> doGetCachedObjects(Class<T> type) {
		return new ArrayList<CachedObject<T>>();
	}

	@Override
	public <T> void doPutCachedObject(String key, CachedObject<T> cachedObject) {
	}

	@Override
	public <T> void doRemoveCachedObject(String key, Class<T> type) {
	}

}
