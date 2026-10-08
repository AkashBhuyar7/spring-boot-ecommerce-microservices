package com.micro.inventoryservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.micro.inventoryservice.dto.InventoryResponse;
import com.micro.inventoryservice.model.Inventory;
import com.micro.inventoryservice.repository.InventoryRepository;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

	@Mock
	private InventoryRepository inventoryRepository;

	@InjectMocks
	private InventoryService inventoryService;

	@Test
	void reportsWhetherEachKnownSkuIsInStock() {
		when(inventoryRepository.findBySkuCodeIn(List.of("iphone_13", "iphone_13_red")))
				.thenReturn(List.of(new Inventory(1L, "iphone_13", 100), new Inventory(2L, "iphone_13_red", 0)));

		List<InventoryResponse> responses = inventoryService.isInStock(List.of("iphone_13", "iphone_13_red"));

		assertThat(responses).containsExactly(
				new InventoryResponse("iphone_13", true),
				new InventoryResponse("iphone_13_red", false));
	}

	@Test
	void leavesOutUnknownSkus() {
		when(inventoryRepository.findBySkuCodeIn(List.of("unknown_sku"))).thenReturn(List.of());

		assertThat(inventoryService.isInStock(List.of("unknown_sku"))).isEmpty();
	}
}
