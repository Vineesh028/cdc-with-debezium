package com.debezium.example.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;


@Configuration
public class AppConfig {
	
	@Bean
	public ModelMapper modelMapper() {

		return new ModelMapper();
	}

	
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper().registerModule(new JavaTimeModule())
				.setSerializationInclusion(NON_NULL)
				.disable(FAIL_ON_UNKNOWN_PROPERTIES).disable(ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
				.disable(WRITE_DATES_AS_TIMESTAMPS)
				.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
	}

}
