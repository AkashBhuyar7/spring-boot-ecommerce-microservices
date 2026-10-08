package com.micro.inventoryservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mssqlserver.MSSQLServerContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class InventoryServiceApplicationTests {

	@Container
	@ServiceConnection
	static MSSQLServerContainer sqlServer = new MSSQLServerContainer("mcr.microsoft.com/mssql/server:2022-CU20-ubuntu-22.04")
			.acceptLicense();

	@Autowired
	private MockMvc mockMvc;

	@Test
	void reportsSeededStockAndLeavesOutUnknownSkus() throws Exception {
		mockMvc.perform(get("/api/inventory").param("skuCode", "iphone_13", "iphone_13_red", "unknown_sku"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[?(@.skuCode == 'iphone_13')].inStock").value(true))
				.andExpect(jsonPath("$[?(@.skuCode == 'iphone_13_red')].inStock").value(false));
	}
}
