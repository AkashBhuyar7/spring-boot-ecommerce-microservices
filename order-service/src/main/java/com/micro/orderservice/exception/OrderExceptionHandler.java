package com.micro.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OrderExceptionHandler {

	@ExceptionHandler(OutOfStockException.class)
	public ProblemDetail handleOutOfStock(OutOfStockException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
		problem.setTitle("Product not in stock");
		problem.setProperty("skuCodes", exception.getSkuCodes());
		return problem;
	}

	@ExceptionHandler(ServiceUnavailableException.class)
	public ProblemDetail handleServiceUnavailable(ServiceUnavailableException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
		problem.setTitle("Service unavailable");
		return problem;
	}
}
