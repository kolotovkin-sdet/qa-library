package com.qa.atlibs.manager;

import com.qa.atlibs.environment.EnvironmentConfigurationLoader;
import com.qa.atlibs.environment.EnvironmentConfigurationModel;
import com.qa.atlibs.exception.CoreTestException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnvironmentManager {

	private static EnvironmentConfigurationModel environmentsConfiguration;

	static {
		setupEnvironment();
	}

	public static void setupEnvironment() {
		environmentsConfiguration = new EnvironmentConfigurationLoader().loadConfiguration(
				Optional.ofNullable(System.getProperty("config.path"))
						.orElse("qa-application.yml"), EnvironmentConfigurationModel.class);
	}

	public static EnvironmentConfigurationModel.Environment getEnvironmentVariables() {
		return Optional.ofNullable(environmentsConfiguration.environment().get(Optional.ofNullable(System.getProperty("env"))
						.orElse("default")))
				.orElseThrow(() ->
						new CoreTestException(
								"Environment configuration haven't been set up. "
										+ "Set environment variables in env.default or env.${env}"));
	}
}