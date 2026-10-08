package com.micro.orderservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.micro.orderservice.dto.OrderResponse;
import com.micro.orderservice.exception.OutOfStockException;
import com.micro.orderservice.exception.ServiceUnavailableException;
import com.micro.orderservice.service.OrderService;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

	private static final String VALID_ORDER = """
			{"orderLineItemsDtoList": [{"skuCode": "iphone_13", "price": 1200, "quantity": 1}]}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OrderService orderService;

	@Test
	void returnsCreatedWithOrderNumber() throws Exception {
		when(orderService.placeOrder(any())).thenReturn(new OrderResponse("order-123", "Order placed successfully"));

		mockMvc.perform(post("/api/order").contentType(MediaType.APPLICATION_JSON).content(VALID_ORDER))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.orderNumber").value("order-123"));
	}

	@Test
	void returnsConflictWhenAProductIsOutOfStock() throws Exception {
		when(orderService.placeOrder(any())).thenThrow(new OutOfStockException(List.of("iphone_13_red")));

		mockMvc.perform(post("/api/order").contentType(MediaType.APPLICATION_JSON).content(VALID_ORDER))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Product not in stock"))
				.andExpect(jsonPath("$.skuCodes[0]").value("iphone_13_red"));
	}

	@Test
	void returnsServiceUnavailableWhenInventoryIsDown() throws Exception {
		when(orderService.placeOrder(any()))
				.thenThrow(new ServiceUnavailableException("Inventory service is unavailable, please try again later", null));

		mockMvc.perform(post("/api/order").contentType(MediaType.APPLICATION_JSON).content(VALID_ORDER))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.title").value("Service unavailable"));
	}

	@Test
	void rejectsOrderWithoutLineItems() throws Exception {
		mockMvc.perform(post("/api/order").contentType(MediaType.APPLICATION_JSON).content("{\"orderLineItemsDtoList\": []}"))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(orderService);
	}

	@Test
	void rejectsLineItemWithInvalidQuantity() throws Exception {
		String order = """
				{"orderLineItemsDtoList": [{"skuCode": "iphone_13", "price": 1200, "quantity": 0}]}
				""";

		mockMvc.perform(post("/api/order").contentType(MediaType.APPLICATION_JSON).content(order))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(orderService);
	}
}
