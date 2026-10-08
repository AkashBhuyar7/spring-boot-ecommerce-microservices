package com.micro.orderservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.micro.orderservice.client.InventoryClient;
import com.micro.orderservice.dto.InventoryResponse;
import com.micro.orderservice.dto.OrderLineItemsDto;
import com.micro.orderservice.dto.OrderRequest;
import com.micro.orderservice.dto.OrderResponse;
import com.micro.orderservice.exception.OutOfStockException;
import com.micro.orderservice.exception.ServiceUnavailableException;
import com.micro.orderservice.model.Order;
import com.micro.orderservice.model.OrderLineItems;
import com.micro.orderservice.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private InventoryClient inventoryClient;

	@Mock
	private KafkaTemplate<String, String> kafkaTemplate;

	@InjectMocks
	private OrderService orderService;

	@Test
	void savesOrderAndPublishesEventWhenAllProductsAreInStock() {
		when(inventoryClient.getInventory(List.of("iphone_13")))
				.thenReturn(CompletableFuture.completedFuture(List.of(new InventoryResponse("iphone_13", true))));

		OrderResponse response = orderService.placeOrder(orderRequest("iphone_13"));

		ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
		verify(orderRepository).save(savedOrder.capture());
		assertThat(savedOrder.getValue().getOrderNumber()).isEqualTo(response.getOrderNumber());
		assertThat(savedOrder.getValue().getOrderLineItemsList())
				.extracting(OrderLineItems::getSkuCode)
				.containsExactly("iphone_13");
		verify(kafkaTemplate).sendDefault(response.getOrderNumber());
	}

	@Test
	void rejectsOrderWhenAProductIsOutOfStock() {
		when(inventoryClient.getInventory(List.of("iphone_13", "iphone_13_red")))
				.thenReturn(CompletableFuture.completedFuture(List.of(
						new InventoryResponse("iphone_13", true),
						new InventoryResponse("iphone_13_red", false))));

		assertThatThrownBy(() -> orderService.placeOrder(orderRequest("iphone_13", "iphone_13_red")))
				.isInstanceOfSatisfying(OutOfStockException.class,
						exception -> assertThat(exception.getSkuCodes()).containsExactly("iphone_13_red"));
		verifyNoInteractions(orderRepository, kafkaTemplate);
	}

	@Test
	void rejectsOrderWhenASkuIsUnknownToInventory() {
		when(inventoryClient.getInventory(List.of("unknown_sku")))
				.thenReturn(CompletableFuture.completedFuture(List.of()));

		assertThatThrownBy(() -> orderService.placeOrder(orderRequest("unknown_sku")))
				.isInstanceOfSatisfying(OutOfStockException.class,
						exception -> assertThat(exception.getSkuCodes()).containsExactly("unknown_sku"));
		verifyNoInteractions(orderRepository, kafkaTemplate);
	}

	@Test
	void reportsServiceUnavailableWhenInventoryCannotBeReached() {
		when(inventoryClient.getInventory(anyList()))
				.thenReturn(CompletableFuture.failedFuture(new ServiceUnavailableException("Inventory service is unavailable", null)));

		assertThatThrownBy(() -> orderService.placeOrder(orderRequest("iphone_13")))
				.isInstanceOf(ServiceUnavailableException.class);
		verifyNoInteractions(orderRepository, kafkaTemplate);
	}

	private OrderRequest orderRequest(String... skuCodes) {
		return new OrderRequest(Arrays.stream(skuCodes)
				.map(skuCode -> new OrderLineItemsDto(skuCode, BigDecimal.valueOf(1200), 1))
				.toList());
	}
}
