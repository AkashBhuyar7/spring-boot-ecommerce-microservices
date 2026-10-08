package com.micro.inventoryservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.micro.inventoryservice.model.Inventory;
import com.micro.inventoryservice.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;

/**
 * Inserts sample stock for trying out the order flow. Only runs against an empty table.
 */
@Component
@RequiredArgsConstructor
public class InventoryDataLoader implements CommandLineRunner {

	private final InventoryRepository inventoryRepository;

	@Override
	public void run(String... args) {
		if (inventoryRepository.count() > 0) {
			return;
		}

		Inventory inventory = new Inventory();
		inventory.setSkuCode("iphone_13");
		inventory.setQuantity(100);

		Inventory inventory1 = new Inventory();
		inventory1.setSkuCode("iphone_13_red");
		inventory1.setQuantity(0);

		inventoryRepository.save(inventory);
		inventoryRepository.save(inventory1);
	}
}
