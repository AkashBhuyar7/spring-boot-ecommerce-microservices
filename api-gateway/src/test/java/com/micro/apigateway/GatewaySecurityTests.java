package com.micro.apigateway;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * No downstream service is registered in these tests. A request that gets past security reaches
 * the load balancer and ends with 503 "Unable to find instance", proving security let it through.
 */
@SpringBootTest(properties = "EUREKA_DASHBOARD_URL=lb://discovery-server")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GatewaySecurityTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void productRouteRequiresToken() throws Exception {
		mockMvc.perform(get("/api/product"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void orderRouteRequiresToken() throws Exception {
		mockMvc.perform(post("/api/order"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void requestWithJwtIsRoutedToProductService() throws Exception {
		mockMvc.perform(get("/api/product").with(jwt()))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.detail").value("Unable to find instance for product-service"));
	}

	@Test
	void eurekaDashboardIsRoutedWithoutToken() throws Exception {
		mockMvc.perform(get("/eureka/web"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.detail").value("Unable to find instance for discovery-server"));
	}
}
