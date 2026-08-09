package com.fleetbite.order.infrastructure.inbound.events;

import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.order.domain.event.OrderReadyEvent;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OrderReadyAutoAssignIntegrationTest {

	private static final String PASSWORD = "Fleetbite1!";

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	private OrderReadyEventListener orderReadyEventListener;

	@Autowired
	private DeliveryAssignmentRepositoryPort assignmentRepositoryPort;

	@Test
	void ready_withAvailableDriver_shouldAssignAfterCommit() throws Exception {
		String token = login("dispatcher@fleetbite.local");
		UUID orderId = createPreparingOrder(token);
		UUID driverId = createAvailableDriver(token, "900100001", -12.1002, -77.0202);

		mockMvc.perform(post("/api/v1/orders/{id}/ready", orderId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("READY"));

		mockMvc.perform(get("/api/v1/orders/{id}", orderId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(OrderStatus.ASSIGNED.name()));

		mockMvc.perform(get("/api/v1/drivers/{id}", driverId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("BUSY"));

		assertThat(assignmentRepositoryPort.findActiveByOrderId(OrderId.of(orderId)))
				.isPresent()
				.get()
				.extracting(a -> a.status().name())
				.isEqualTo("PENDING");

		mockMvc.perform(get("/api/v1/orders/{id}/history", orderId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].eventType", hasItem("ORDER_READY")))
				.andExpect(jsonPath("$[*].eventType", hasItem("DRIVER_ASSIGNED")));
	}

	@Test
	void ready_withoutDriver_shouldWaitForDriverAfterCommit() throws Exception {
		String token = login("dispatcher@fleetbite.local");
		UUID orderId = createPreparingOrder(token);

		mockMvc.perform(post("/api/v1/orders/{id}/ready", orderId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("READY"));

		mockMvc.perform(get("/api/v1/orders/{id}", orderId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(OrderStatus.WAITING_FOR_DRIVER.name()));

		mockMvc.perform(get("/api/v1/orders/{id}/history", orderId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].eventType", hasItem("ORDER_READY")))
				.andExpect(jsonPath("$[*].eventType", hasItem("ORDER_WAITING_FOR_DRIVER")));
	}

	@Test
	void duplicateOrderReadyProcessing_shouldNotCreateSecondActiveAssignment() throws Exception {
		String token = login("dispatcher@fleetbite.local");
		UUID orderId = createPreparingOrder(token);
		createAvailableDriver(token, "900100002", -12.1003, -77.0203);

		mockMvc.perform(post("/api/v1/orders/{id}/ready", orderId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		assertThat(assignmentRepositoryPort.existsActiveByOrderId(OrderId.of(orderId))).isTrue();

		OrderReadyEvent duplicate = OrderReadyEvent.of(
				OrderId.of(orderId),
				OffsetDateTime.of(2026, 8, 8, 23, 0, 0, 0, BusinessTime.ZONE_OFFSET));
		orderReadyEventListener.onOrderReady(duplicate);

		AtomicInteger activeCount = new AtomicInteger();
		assignmentRepositoryPort.findAll().forEach(assignment -> {
			if (assignment.orderId().equals(OrderId.of(orderId)) && assignment.status().isActive()) {
				activeCount.incrementAndGet();
			}
		});
		assertThat(activeCount.get()).isEqualTo(1);
		assertThat(assignmentRepositoryPort.existsActiveByOrderId(OrderId.of(orderId))).isTrue();
	}

	private UUID createPreparingOrder(String token) throws Exception {
		MvcResult created = mockMvc.perform(post("/api/v1/orders")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "customerName": "Ana Torres",
								  "customerPhone": "999888777",
								  "deliveryAddress": "Av. Example 123",
								  "deliveryLatitude": -12.1001,
								  "deliveryLongitude": -77.0201,
								  "totalAmount": 45.50
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();
		UUID orderId = UUID.fromString(readJson(created).get("id").asString());

		mockMvc.perform(post("/api/v1/orders/{id}/confirm", orderId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/v1/orders/{id}/start-preparation", orderId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PREPARING"));
		return orderId;
	}

	private UUID createAvailableDriver(String dispatcherToken, String phone, double lat, double lon)
			throws Exception {
		String adminToken = login("admin@fleetbite.local");
		String email = "driver-" + phone + "@fleetbite.local";
		MvcResult userCreated = mockMvc.perform(post("/api/v1/users")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s",
								  "fullName": "Driver Ready %s",
								  "role": "DRIVER"
								}
								""".formatted(email, PASSWORD, phone)))
				.andExpect(status().isCreated())
				.andReturn();
		UUID userId = UUID.fromString(readJson(userCreated).get("id").asString());

		MvcResult vehicleCreated = mockMvc.perform(post("/api/v1/vehicles")
						.header("Authorization", "Bearer " + dispatcherToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "plate": "PLT-%s",
								  "type": "MOTORCYCLE"
								}
								""".formatted(phone.substring(phone.length() - 4))))
				.andExpect(status().isCreated())
				.andReturn();
		UUID vehicleId = UUID.fromString(readJson(vehicleCreated).get("id").asString());

		MvcResult driverCreated = mockMvc.perform(post("/api/v1/drivers")
						.header("Authorization", "Bearer " + dispatcherToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "userId": "%s",
								  "phone": "%s",
								  "currentLatitude": %s,
								  "currentLongitude": %s
								}
								""".formatted(userId, phone, lat, lon)))
				.andExpect(status().isCreated())
				.andReturn();
		UUID driverId = UUID.fromString(readJson(driverCreated).get("id").asString());

		mockMvc.perform(put("/api/v1/drivers/{id}/vehicle", driverId)
						.header("Authorization", "Bearer " + dispatcherToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "vehicleId": "%s"
								}
								""".formatted(vehicleId)))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/drivers/{id}/online", driverId)
						.header("Authorization", "Bearer " + dispatcherToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("AVAILABLE"));
		return driverId;
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
		return readJson(result).get("accessToken").asString();
	}

	private JsonNode readJson(MvcResult result) throws Exception {
		return jsonMapper.readTree(result.getResponse().getContentAsString());
	}
}
