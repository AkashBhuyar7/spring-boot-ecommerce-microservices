package com.micro.orderservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mssqlserver.MSSQLServerContainer;

import com.micro.orderservice.client.InventoryClient;
import com.micro.orderservice.exception.ServiceUnavailableException;

/**
 * Starts the full application against a real SQL Server. Eureka is disabled, so inventory-service
 * can never be found, which exercises the retry and circuit-breaker fallback.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class OrderServiceApplicationTests {

	@Container
	@ServiceConnection
	static MSSQLServerContainer sqlServer = new MSSQLServerContainer("mcr.microsoft.com/mssql/server:2022-CU20-ubuntu-22.04")
			.acceptLicense();

	@Autowired
	private InventoryClient inventoryClient;

	@Test
	void resilienceAnnotationsAreAppliedToInventoryClient() {
		assertThat(AopUtils.isAopProxy(inventoryClient)).isTrue();
	}

	@Test
	void inventoryClientFallsBackWhenInventoryServiceIsUnavailable() {
		assertThatThrownBy(() -> inventoryClient.getInventory(List.of("iphone_13")).join())
				.isInstanceOf(CompletionException.class)
				.hasCauseInstanceOf(ServiceUnavailableException.class);
	}
}
