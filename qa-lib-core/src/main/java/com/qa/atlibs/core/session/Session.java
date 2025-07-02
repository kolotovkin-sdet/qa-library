package com.qa.atlibs.core.session;

import com.qa.atlibs.core.exception.CoreTestException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session {
	private static final ThreadLocal<Session> threadLocal = new InheritableThreadLocal<>();
	private final Map<Object, Object> sessionMap = new HashMap<>();

	public static void initNewSession() {
		threadLocal.set(new Session());
	}

	public static Session getCurrentSession() {
		return threadLocal.get();
	}

	public static Object getSessionValue(Object key) {
		var getSession = getCurrentSession();
		if (getSession == null) {
			throw new CoreTestException("Session is not initialized!");
		} else {
			return getSession.sessionMap.get(key);
		}
	}

	public static void setSessionValue(Object key, Object value) {
		var getSession = getCurrentSession();
		if (getSession == null) {
			throw new CoreTestException("Session is not initialized!");
		} else {
			getCurrentSession().sessionMap.put(key, value);
		}
	}

	public static List<Object> getSessionValues() {
		var getSession = getCurrentSession();
		if (getSession == null) {
			throw new CoreTestException("Session is not initialized!");
		} else {
			return Collections.singletonList(getSession.sessionMap.values());
		}
	}
}