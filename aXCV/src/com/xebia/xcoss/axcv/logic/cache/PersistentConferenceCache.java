package com.xebia.xcoss.axcv.logic.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.xebia.xcoss.axcv.logic.ProfileManager;
import com.xebia.xcoss.axcv.util.XCS;

import android.content.Context;
import android.util.Log;
import biz.source_code.base64Coder.Base64Coder;

public class PersistentConferenceCache extends ConferenceCache {

	private ProfileManager profileManager;

	protected PersistentConferenceCache(Context ctx) {
		super(ctx);
		this.profileManager = new ProfileManager(ctx);
	}

	@Override
	public void init() {
		profileManager.openConnection();
		super.init();
	}
	
	@Override
	public void destroy() {
		profileManager.purgeCache();
		profileManager.closeConnection();
		super.destroy();
	}
	
	@Override
	public <T> CachedObject<T> doGetCachedObject(String key, Class<T> type) {
		String ck = createCacheKey(key, type);
		String[] objects = profileManager.getCachedObjects(ck);
		if (objects.length == 1) {
			return (CachedObject<T>) deserialize(objects[0]);
		}
		Log.i(XCS.LOG.CACHE, "Cache miss " + ck);
		return null;
	}

	@Override
	public <T> List<CachedObject<T>> doGetCachedObjects(Class<T> type) {
		String[] objects = profileManager.getCachedObjects(type);
		List<CachedObject<T>> result = new ArrayList<CachedObject<T>>();
		for (String obj : objects) {
			CachedObject<T> co = (CachedObject<T>) deserialize(obj);
			if (co != null) {
				result.add(co);
			}
		}
		Log.i(XCS.LOG.CACHE, "Cache get on " + type.getSimpleName() + " : " + result.size());
		return result;
	}

	@Override
	public <T> void doPutCachedObject(String key, CachedObject<T> object) {
		String ck = createCacheKey(key, object);
		Log.i(XCS.LOG.CACHE, "Cache store on " + ck);
		profileManager.updateCachedObject(ck, serialize(object));
	}

	@Override
	public <T> void doRemoveCachedObject(String key, Class<T> type) {
		String ck = createCacheKey(key, type);
		profileManager.deleteCachedObject(ck);
		Log.i(XCS.LOG.CACHE, "Cache remove " + ck);
	}

	private <T> String createCacheKey(String key, CachedObject<T> object) {
		return createCacheKey(key, object.object.getClass());
	}

	private String createCacheKey(String key, Class<?> type) {
		return type.getSimpleName() + "." + key;
	}

	public static String serialize(final Serializable s) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(s);
			return Base64Coder.encodeLines(baos.toByteArray());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object deserialize(final String s) {
		try {
			byte[] bytes = Base64Coder.decodeLines(s);
			ByteArrayInputStream bios = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bios);
			return ois.readObject();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
