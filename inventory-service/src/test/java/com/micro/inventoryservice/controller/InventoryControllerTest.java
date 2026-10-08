package com.micro.inventoryservice.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.micro.inventoryservice.dto.InventoryResponse;
import com.micro.inventoryservice.service.InventoryService;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private InventoryService inventoryService;

	// order-service reads the "inStock" field, so the JSON field name is part of the contract
	@Test
	void returnsStockForRequestedSkus() throws Exception {
		when(inventoryService.isInStock(List.of("iphone_13", "iphone_13_red")))
				.thenReturn(List.of(new InventoryResponse("iphone_13", true), new InventoryResponse("iphone_13_red", false)));

		mockMvc.perform(get("/api/inventory").param("skuCode", "iphone_13", "iphone_13_red"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].skuCode").value("iphone_13"))
				.andExpect(jsonPath("$[0].inStock").value(true))
				.andExpect(jsonPath("$[1].skuCode").value("iphone_13_red"))
				.andExpect(jsonPath("$[1].inStock").value(false));
	}
}
