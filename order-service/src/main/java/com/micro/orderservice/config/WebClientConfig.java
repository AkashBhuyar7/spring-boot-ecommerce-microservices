package com.micro.orderservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import io.micrometer.observation.ObservationRegistry;

@Configuration
public class WebClientConfig {

	// The observation registry makes outgoing calls carry the current trace to inventory-service
	@Bean
	@LoadBalanced
	public WebClient.Builder webClientBuilder(ObservationRegistry observationRegistry) {
		return WebClient.builder().observationRegistry(observationRegistry);
	}
}
