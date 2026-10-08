package com.micro.orderservice.exception;

import java.util.List;

import lombok.Getter;

@Getter
public class OutOfStockException extends RuntimeException {

	private final List<String> skuCodes;

	public OutOfStockException(List<String> skuCodes) {
		super("Products not in stock: " + String.join(", ", skuCodes));
		this.skuCodes = List.copyOf(skuCodes);
	}
}
