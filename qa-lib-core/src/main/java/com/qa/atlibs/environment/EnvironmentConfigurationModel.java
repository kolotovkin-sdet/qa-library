package com.qa.atlibs.environment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record EnvironmentConfigurationModel(
		Map<String, Environment> environment
) {

	public record Environment(
			@JsonProperty("test-data-dir")
			String testDataDir,
			@JsonProperty("apps")
			Applications appsConfig,
			@JsonProperty("mock")
			MockConfig mockConfig) {
	}

	public record Applications(
			Map<String, AppConfig> appConfig
	) {
	}

	public record AppConfig(
			String host,
			String port,
			boolean isEnabled
	) {
	}

	public record MockConfig(
			String host,
			String port,
			boolean isEnabled,
			@JsonProperty("stubs-path")
			String stubsPath
	) {
	}
}
