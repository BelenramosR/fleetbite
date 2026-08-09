package com.fleetbite.order.infrastructure.inbound.rest;

import com.fleetbite.order.application.dto.CreateOrderCommand;
import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.in.CreateOrderUseCase;
import com.fleetbite.order.application.port.in.GetOrderByIdUseCase;
import com.fleetbite.order.domain.exception.InvalidOrderDataException;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.shared.infrastructure.config.SecurityConfig;
import com.fleetbite.shared.infrastructure.inbound.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@Import({OrderHttpMapper.class, GlobalExceptionHandler.class, SecurityConfig.class})
class OrderControllerTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime PROMISED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 45, 0, 0, BusinessTime.ZONE_OFFSET);

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CreateOrderUseCase createOrderUseCase;

	@MockitoBean
	private GetOrderByIdUseCase getOrderByIdUseCase;

	@Test
	void createOrder_shouldReturn201WithLocationAndBusinessOffsetTimestamps() throws Exception {
		OrderResult result = sampleResult();
		when(createOrderUseCase.execute(any(CreateOrderCommand.class))).thenReturn(result);

		String body = """
				{
				  "customerName": "Ana Torres",
				  "customerPhone": "999999999",
				  "deliveryAddress": "Av. Example 123",
				  "deliveryLatitude": -12.1001,
				  "deliveryLongitude": -77.0201,
				  "totalAmount": 85.90
				}
				""";

		mockMvc.perform(post("/api/v1/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/api/v1/orders/" + result.id())))
				.andExpect(jsonPath("$.id").value(result.id().toString()))
				.andExpect(jsonPath("$.status").value("CREATED"))
				.andExpect(jsonPath("$.createdAt").value("2026-08-08T22:00:00-05:00"))
				.andExpect(jsonPath("$.promisedDeliveryAt").value("2026-08-08T22:45:00-05:00"));

		verify(createOrderUseCase).execute(any(CreateOrderCommand.class));
		verifyNoInteractions(getOrderByIdUseCase);
	}

	@Test
	void createOrder_shouldReturn400WhenCustomerNameBlank() throws Exception {
		String body = """
				{
				  "customerName": "   ",
				  "customerPhone": "999999999",
				  "deliveryAddress": "Av. Example 123",
				  "deliveryLatitude": -12.1001,
				  "deliveryLongitude": -77.0201,
				  "totalAmount": 85.90
				}
				""";

		mockMvc.perform(post("/api/v1/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.status").value(400));

		verifyNoInteractions(createOrderUseCase);
	}

	@Test
	void createOrder_shouldReturn400WhenDomainRejectsData() throws Exception {
		when(createOrderUseCase.execute(any(CreateOrderCommand.class)))
				.thenThrow(new InvalidOrderDataException("invalid order data"));

		String body = """
				{
				  "customerName": "Ana Torres",
				  "customerPhone": "999999999",
				  "deliveryAddress": "Av. Example 123",
				  "deliveryLatitude": -12.1001,
				  "deliveryLongitude": -77.0201,
				  "totalAmount": 85.90
				}
				""";

		mockMvc.perform(post("/api/v1/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_ORDER_DATA"));
	}

	@Test
	void getOrderById_shouldReturn200WhenFound() throws Exception {
		OrderResult result = sampleResult();
		when(getOrderByIdUseCase.execute(OrderId.of(result.id()))).thenReturn(result);

		mockMvc.perform(get("/api/v1/orders/{id}", result.id()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(result.id().toString()))
				.andExpect(jsonPath("$.createdAt").value("2026-08-08T22:00:00-05:00"))
				.andExpect(jsonPath("$.promisedDeliveryAt").value("2026-08-08T22:45:00-05:00"));

		verify(getOrderByIdUseCase).execute(OrderId.of(result.id()));
		verifyNoInteractions(createOrderUseCase);
	}

	@Test
	void getOrderById_shouldReturn404WhenMissing() throws Exception {
		UUID id = UUID.randomUUID();
		when(getOrderByIdUseCase.execute(OrderId.of(id)))
				.thenThrow(new ResourceNotFoundException("Order", id));

		mockMvc.perform(get("/api/v1/orders/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void getOrderById_shouldReturn400WhenIdIsNotUuid() throws Exception {
		mockMvc.perform(get("/api/v1/orders/{id}", "not-a-uuid"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

		verifyNoInteractions(getOrderByIdUseCase);
	}

	private static OrderResult sampleResult() {
		Order order = Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-ABCDEF12"),
				"Ana Torres",
				"999999999",
				"Av. Example 123",
				new Location(-12.1001, -77.0201),
				Money.of(new BigDecimal("85.90")),
				CREATED_AT,
				PROMISED_AT);
		return OrderResult.from(order);
	}
}
