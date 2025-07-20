package com.qa.atlibs.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectMapperConfiguration {

	private static ObjectMapper yamlObjectMapper;

	public static ObjectMapper getYamlObjectMapper() {
		if (yamlObjectMapper == null) {
			yamlObjectMapper = new ObjectMapper(new YAMLFactory());
			yamlObjectMapper.registerModule(new JavaTimeModule());
		}
		return yamlObjectMapper;
	}
}
