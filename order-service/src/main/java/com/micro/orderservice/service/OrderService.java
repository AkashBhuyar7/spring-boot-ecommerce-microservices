package com.micro.orderservice.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.micro.orderservice.client.InventoryClient;
import com.micro.orderservice.dto.InventoryResponse;
import com.micro.orderservice.dto.OrderLineItemsDto;
import com.micro.orderservice.dto.OrderRequest;
import com.micro.orderservice.dto.OrderResponse;
import com.micro.orderservice.exception.OutOfStockException;
import com.micro.orderservice.model.Order;
import com.micro.orderservice.model.OrderLineItems;
import com.micro.orderservice.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

	private final OrderRepository orderRepository;
	private final InventoryClient inventoryClient;
	private final KafkaTemplate<String, String> kafkaTemplate;

	public OrderResponse placeOrder(OrderRequest orderRequest) {
		List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
				.stream()
				.map(this::mapToEntity)
				.toList();

		List<String> skuCodes = orderLineItems.stream()
				.map(OrderLineItems::getSkuCode)
				.distinct()
				.toList();

		// Every requested SKU must be known to inventory-service and in stock
		Set<String> inStockSkuCodes = checkInventory(skuCodes).stream()
				.filter(InventoryResponse::isInStock)
				.map(InventoryResponse::getSkuCode)
				.collect(Collectors.toSet());
		List<String> unavailableSkuCodes = skuCodes.stream()
				.filter(skuCode -> !inStockSkuCodes.contains(skuCode))
				.toList();
		if (!unavailableSkuCodes.isEmpty()) {
			throw new OutOfStockException(unavailableSkuCodes);
		}

		Order order = new Order();
		order.setOrderNumber(UUID.randomUUID().toString());
		order.setOrderLineItemsList(orderLineItems);
		orderRepository.save(order);

		kafkaTemplate.sendDefault(order.getOrderNumber());
		log.info("Order {} placed", order.getOrderNumber());
		return new OrderResponse(order.getOrderNumber(), "Order placed successfully");
	}

	private List<InventoryResponse> checkInventory(List<String> skuCodes) {
		try {
			return inventoryClient.getInventory(skuCodes).join();
		} catch (CompletionException e) {
			if (e.getCause() instanceof RuntimeException cause) {
				throw cause;
			}
			throw e;
		}
	}

	private OrderLineItems mapToEntity(OrderLineItemsDto orderLineItemsDto) {
		OrderLineItems orderLineItems = new OrderLineItems();
		orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
		orderLineItems.setPrice(orderLineItemsDto.getPrice());
		orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
		return orderLineItems;
	}
}
