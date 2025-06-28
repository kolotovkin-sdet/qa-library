package com.qa.atlibs.util;

import com.qa.atlibs.manager.SessionManager;

public class CoreQaUtil {
    public static String processTestData(String data) {
        return SessionManager.setSessionVariables(data);
    }
}
