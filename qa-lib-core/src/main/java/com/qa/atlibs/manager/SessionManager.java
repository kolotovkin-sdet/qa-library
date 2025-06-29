package com.qa.atlibs.manager;

import com.qa.atlibs.session.Session;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionManager {

	private static final Pattern VARIABLE_PARAMETER_PATTERN = Pattern.compile("\\{\\{((?:(?!}}|\\{\\{).)+)}}");

	public static String setSessionVariables(String value) {
		if (value == null || !value.contains("{{")) {
			return value;
		}

		Matcher matcher = VARIABLE_PARAMETER_PATTERN.matcher(value);
		StringBuilder result = new StringBuilder();

		while (matcher.find()) {
			String variableName = matcher.group(1);
			Object sessionValue = Session.getSessionValue(variableName);

			if (sessionValue != null) {
				matcher.appendReplacement(result, Matcher.quoteReplacement(sessionValue.toString()));
			} else {
				log.debug("Session has no variable called {}", variableName);
				matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
			}
		}

		matcher.appendTail(result);
		return result.toString();
	}

	public static String processTestData(String data) {
		return setSessionVariables(data);
	}
}

