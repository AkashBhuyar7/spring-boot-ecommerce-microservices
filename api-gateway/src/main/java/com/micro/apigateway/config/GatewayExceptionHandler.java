package com.micro.apigateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Reports an unreachable downstream service as 503 instead of a generic 500.
 */
@RestControllerAdvice
public class GatewayExceptionHandler {

	// Raised by the load balancer, e.g. 503 "Unable to find instance for product-service"
	@ExceptionHandler(HttpServerErrorException.class)
	public ProblemDetail handleLoadBalancerError(HttpServerErrorException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(exception.getStatusCode(), exception.getStatusText());
		problem.setTitle("Service unavailable");
		return problem;
	}

	// An instance is registered but does not answer, e.g. it was just stopped
	@ExceptionHandler(ResourceAccessException.class)
	public ProblemDetail handleUnreachableService(ResourceAccessException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
				"The service is not reachable right now, please try again later");
		problem.setTitle("Service unavailable");
		return problem;
	}
}
