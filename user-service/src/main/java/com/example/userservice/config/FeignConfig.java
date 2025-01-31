package com.example.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.userservice.error.FeignErrorDecoder;

import feign.Logger;

@Configuration
public class FeignConfig {

	@Bean
	public FeignErrorDecoder feignErrorDecoder() {
		return new FeignErrorDecoder();
	}

	@Bean
	public Logger.Level feginLoggerLevel() {
		return Logger.Level.FULL;
	}
}
