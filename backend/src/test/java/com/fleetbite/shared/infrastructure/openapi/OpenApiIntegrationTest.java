package com.fleetbite.shared.infrastructure.openapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OpenApiIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiDocs_shouldBePublicAndContainExpectedContract() throws Exception {
		String body = mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists())
				.andExpect(jsonPath("$.paths['/api/v1/auth/login']").exists())
				.andExpect(jsonPath("$.paths['/api/v1/orders']").exists())
				.andExpect(jsonPath("$.paths['/api/v1/drivers']").exists())
				.andExpect(jsonPath("$.paths['/api/v1/vehicles']").exists())
				.andExpect(jsonPath("$.paths['/api/v1/assignments']").exists())
				.andExpect(jsonPath("$.paths['/api/v1/users']").exists())
				.andExpect(jsonPath("$.paths['/api/v1/orders/{orderId}/auto-assign']").exists())
				.andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.security", hasSize(0)))
				.andExpect(jsonPath("$.paths['/api/v1/orders'].get.security[0].bearerAuth").exists())
				.andExpect(jsonPath("$.paths['/api/v1/users'].get.security[0].bearerAuth").exists())
				.andReturn()
				.getResponse()
				.getContentAsString();

		assertThat(body).contains("bearerAuth");
	}

	@Test
	void swaggerUi_shouldBePublic() throws Exception {
		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(result -> {
					int status = result.getResponse().getStatus();
					assertThat(status).isIn(200, 302, 301);
				});
	}
}
