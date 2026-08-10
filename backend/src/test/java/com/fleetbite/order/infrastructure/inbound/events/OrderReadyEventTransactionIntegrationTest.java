package com.fleetbite.order.infrastructure.inbound.events;

import com.fleetbite.delivery.application.port.in.AutoAssignOrderUseCase;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.event.OrderReadyEvent;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OrderReadyEventTransactionIntegrationTest {

	private static final String PASSWORD = "Fleetbite1!";

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private org.springframework.context.ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private OrderRepositoryPort orderRepositoryPort;

	@MockitoBean
	private AutoAssignOrderUseCase autoAssignOrderUseCase;

	@Test
	void rollbackBeforeCommit_shouldNotInvokeAutoAssignListener() {
		UUID orderId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
		OrderReadyEvent event = OrderReadyEvent.of(
				orderId,
				BusinessTime.toBusinessTime(java.time.Instant.parse("2026-08-09T03:00:00Z")));

		assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
			applicationEventPublisher.publishEvent(event);
			throw new IllegalStateException("force rollback before commit");
		})).isInstanceOf(IllegalStateException.class);

		verify(autoAssignOrderUseCase, never()).execute(any());
	}

	@Test
	void autoAssignFailureAfterCommit_shouldKeepOrderReady() throws Exception {
		when(autoAssignOrderUseCase.execute(any()))
				.thenThrow(new IllegalStateException("auto-assign failed after commit"));

		String token = login("dispatcher@fleetbite.local");
		UUID orderId = createPreparingOrder(token);

		mockMvc.perform(post("/api/v1/orders/{id}/ready", orderId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("READY"));

		verify(autoAssignOrderUseCase).execute(orderId);

		assertThat(orderRepositoryPort.findById(orderId))
				.isPresent()
				.get()
				.extracting(order -> order.status())
				.isEqualTo(OrderStatus.READY);

		mockMvc.perform(get("/api/v1/orders/{id}", orderId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("READY"));
	}

	private UUID createPreparingOrder(String token) throws Exception {
		MvcResult created = mockMvc.perform(post("/api/v1/orders")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "customerName": "Ana Torres",
								  "customerPhone": "999777666",
								  "deliveryAddress": "Av. Example 456",
								  "deliveryLatitude": -12.1001,
								  "deliveryLongitude": -77.0201,
								  "totalAmount": 45.50
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();
		UUID orderId = UUID.fromString(readJson(created).path("data").get("id").asString());

		mockMvc.perform(post("/api/v1/orders/{id}/confirm", orderId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/v1/orders/{id}/start-preparation", orderId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
		return orderId;
	}

	private String login(String email) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s"
								}
								""".formatted(email, PASSWORD)))
				.andExpect(status().isOk())
				.andReturn();
		return readJson(result).path("data").get("accessToken").asString();
	}

	private JsonNode readJson(MvcResult result) throws Exception {
		return jsonMapper.readTree(result.getResponse().getContentAsString());
	}
}
