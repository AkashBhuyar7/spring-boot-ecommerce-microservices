package com.micro.orderservice.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.micro.orderservice.dto.InventoryResponse;
import com.micro.orderservice.exception.ServiceUnavailableException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Calls inventory-service through Eureka. Only this read-only lookup is protected by the
 * circuit breaker, time limiter and retry, so a slow inventory call can never cause an
 * order to be saved twice.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryClient {

	private final WebClient.Builder webClientBuilder;
	private final AsyncTaskExecutor applicationTaskExecutor;

	@CircuitBreaker(name = "inventory-service", fallbackMethod = "inventoryUnavailable")
	@TimeLimiter(name = "inventory-service")
	@Retry(name = "inventory-service")
	public CompletableFuture<List<InventoryResponse>> getInventory(List<String> skuCodes) {
		return CompletableFuture.supplyAsync(() -> fetchInventory(skuCodes), applicationTaskExecutor);
	}

	private List<InventoryResponse> fetchInventory(List<String> skuCodes) {
		InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
				.uri("http://inventory-service/api/inventory",
						uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
				.retrieve()
				.bodyToMono(InventoryResponse[].class)
				.block();
		return inventoryResponses == null ? List.of() : List.of(inventoryResponses);
	}

	private CompletableFuture<List<InventoryResponse>> inventoryUnavailable(List<String> skuCodes, Throwable throwable) {
		log.warn("Inventory check for {} failed: {}", skuCodes, throwable.toString());
		return CompletableFuture.failedFuture(
				new ServiceUnavailableException("Inventory service is unavailable, please try again later", throwable));
	}
}
