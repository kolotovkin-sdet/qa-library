package com.qa.atlibs.core.environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.atlibs.core.configuration.ObjectMapperConfiguration;
import com.qa.atlibs.core.exception.CoreTestException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class EnvironmentConfigurationLoader {
	private final ObjectMapper mapper;
	private final StringSubstitutor propertyStringSubstitutor;
	private final StringSubstitutor environmentStringSubstitutor;

	public EnvironmentConfigurationLoader() {
		this.mapper = ObjectMapperConfiguration.getYamlObjectMapper();
		this.environmentStringSubstitutor = new StringSubstitutor(StringLookupFactory.INSTANCE.environmentVariableStringLookup());
		this.propertyStringSubstitutor = new StringSubstitutor(StringLookupFactory.INSTANCE.systemPropertyStringLookup());
	}

	public <T> T loadConfiguration(String configPath, Class<T> cls) {
		try (InputStream fileInputStream = Optional.ofNullable(EnvironmentConfigurationLoader.class.getClassLoader()
						.getResourceAsStream(configPath))
				.orElseThrow(() -> new CoreTestException("File qa-application.yml does not exist. "
						+ "Setup qa-application.yml in resources directory or define config.path"))) {
			String contents = this.propertyStringSubstitutor.replace(
					this.environmentStringSubstitutor.replace(IOUtils.toString(fileInputStream, StandardCharsets.UTF_8))
			);
			return this.mapper.readValue(contents, cls);
		} catch (IOException e) {
			throw new CoreTestException(e);
		}
	}
}
