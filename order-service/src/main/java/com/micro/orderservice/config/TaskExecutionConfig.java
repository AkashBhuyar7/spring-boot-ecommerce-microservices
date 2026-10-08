package com.micro.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

@Configuration
public class TaskExecutionConfig {

	// Applied to Spring Boot's applicationTaskExecutor so async inventory calls keep the caller's trace
	@Bean
	public ContextPropagatingTaskDecorator contextPropagatingTaskDecorator() {
		return new ContextPropagatingTaskDecorator();
	}
}
