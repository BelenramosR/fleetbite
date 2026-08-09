package com.fleetbite.order.infrastructure.inbound.rest;

import com.fleetbite.order.application.dto.CancelOrderCommand;
import com.fleetbite.order.application.dto.CreateOrderCommand;
import com.fleetbite.order.application.dto.OrderHistoryResult;
import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.dto.UpdateOrderCommand;
import com.fleetbite.order.application.port.in.CancelOrderUseCase;
import com.fleetbite.order.application.port.in.ConfirmOrderUseCase;
import com.fleetbite.order.application.port.in.CreateOrderUseCase;
import com.fleetbite.order.application.port.in.DeleteOrderUseCase;
import com.fleetbite.order.application.port.in.GetOrderByIdUseCase;
import com.fleetbite.order.application.port.in.GetOrderHistoryUseCase;
import com.fleetbite.order.application.port.in.ListOrdersUseCase;
import com.fleetbite.order.application.port.in.MarkOrderReadyUseCase;
import com.fleetbite.order.application.port.in.StartOrderPreparationUseCase;
import com.fleetbite.order.application.port.in.UpdateOrderUseCase;
import com.fleetbite.order.domain.exception.InvalidOrderDataException;
import com.fleetbite.order.domain.exception.InvalidOrderTransitionException;
import com.fleetbite.order.domain.exception.OrderNotDeletableException;
import com.fleetbite.order.domain.exception.OrderNotEditableException;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.shared.infrastructure.inbound.rest.ApiResponseBodyAdvice;
import com.fleetbite.shared.infrastructure.inbound.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({OrderHttpMapperImpl.class, GlobalExceptionHandler.class, ApiResponseBodyAdvice.class})
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

	@MockitoBean
	private ListOrdersUseCase listOrdersUseCase;

	@MockitoBean
	private UpdateOrderUseCase updateOrderUseCase;

	@MockitoBean
	private DeleteOrderUseCase deleteOrderUseCase;
	@MockitoBean
	private ConfirmOrderUseCase confirmOrderUseCase;
	@MockitoBean
	private StartOrderPreparationUseCase startOrderPreparationUseCase;
	@MockitoBean
	private MarkOrderReadyUseCase markOrderReadyUseCase;
	@MockitoBean
	private CancelOrderUseCase cancelOrderUseCase;
	@MockitoBean
	private GetOrderHistoryUseCase getOrderHistoryUseCase;

	@Test
	void createOrder_shouldReturn201WithLocationAndBusinessOffsetTimestamps() throws Exception {
		OrderResult result = sampleResult();
		when(createOrderUseCase.execute(any(CreateOrderCommand.class))).thenReturn(result);

		mockMvc.perform(post("/api/v1/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody()))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/api/v1/orders/" + result.id())))
				.andExpect(jsonPath("$.data.createdAt").value("2026-08-08T22:00:00-05:00"))
				.andExpect(jsonPath("$.data.promisedDeliveryAt").value("2026-08-08T22:45:00-05:00"));
	}

	@Test
	void listOrders_shouldReturn200WithItems() throws Exception {
		when(listOrdersUseCase.execute()).thenReturn(List.of(sampleResult()));

		mockMvc.perform(get("/api/v1/orders"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(1)))
				.andExpect(jsonPath("$.data[0].status").value("CREATED"));
	}

	@Test
	void listOrders_shouldReturnEmptyArray() throws Exception {
		when(listOrdersUseCase.execute()).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/orders"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(0)));
	}

	@Test
	void updateOrder_shouldReturn200() throws Exception {
		OrderResult result = sampleResult();
		when(updateOrderUseCase.execute(eq(OrderId.of(result.id())), any(UpdateOrderCommand.class)))
				.thenReturn(result);

		mockMvc.perform(put("/api/v1/orders/{id}", result.id())
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(result.id().toString()));
	}

	@Test
	void updateOrder_shouldReturn400WhenInvalid() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(put("/api/v1/orders/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "customerName": "   ",
								  "customerPhone": "999999999",
								  "deliveryAddress": "Av. Example 123",
								  "deliveryLatitude": -12.1001,
								  "deliveryLongitude": -77.0201,
								  "totalAmount": 85.90
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		verifyNoInteractions(updateOrderUseCase);
	}

	@Test
	void updateOrder_shouldReturn404WhenMissing() throws Exception {
		UUID id = UUID.randomUUID();
		when(updateOrderUseCase.execute(eq(OrderId.of(id)), any(UpdateOrderCommand.class)))
				.thenThrow(new ResourceNotFoundException("Order", id));

		mockMvc.perform(put("/api/v1/orders/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
	}

	@Test
	void updateOrder_shouldReturn409WhenNotEditable() throws Exception {
		UUID id = UUID.randomUUID();
		when(updateOrderUseCase.execute(eq(OrderId.of(id)), any(UpdateOrderCommand.class)))
				.thenThrow(new OrderNotEditableException(OrderStatus.PREPARING));

		mockMvc.perform(put("/api/v1/orders/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("ORDER_NOT_EDITABLE"));
	}

	@Test
	void deleteOrder_shouldReturn204() throws Exception {
		UUID id = UUID.randomUUID();
		doNothing().when(deleteOrderUseCase).execute(OrderId.of(id));

		mockMvc.perform(delete("/api/v1/orders/{id}", id))
				.andExpect(status().isNoContent());

		verify(deleteOrderUseCase).execute(OrderId.of(id));
	}

	@Test
	void deleteOrder_shouldReturn404WhenMissing() throws Exception {
		UUID id = UUID.randomUUID();
		doThrow(new ResourceNotFoundException("Order", id))
				.when(deleteOrderUseCase).execute(OrderId.of(id));

		mockMvc.perform(delete("/api/v1/orders/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
	}

	@Test
	void deleteOrder_shouldReturn409WhenNotDeletable() throws Exception {
		UUID id = UUID.randomUUID();
		doThrow(new OrderNotDeletableException(OrderStatus.CONFIRMED))
				.when(deleteOrderUseCase).execute(OrderId.of(id));

		mockMvc.perform(delete("/api/v1/orders/{id}", id))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("ORDER_NOT_DELETABLE"));
	}

	@Test
	void createOrder_shouldReturn400WhenDomainRejectsData() throws Exception {
		when(createOrderUseCase.execute(any(CreateOrderCommand.class)))
				.thenThrow(new InvalidOrderDataException("invalid order data"));

		mockMvc.perform(post("/api/v1/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_ORDER_DATA"));
	}

	@Test
	void getOrderById_shouldReturn200WhenFound() throws Exception {
		OrderResult result = sampleResult();
		when(getOrderByIdUseCase.execute(OrderId.of(result.id()))).thenReturn(result);

		mockMvc.perform(get("/api/v1/orders/{id}", result.id()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(result.id().toString()));
	}

	@Test
	void confirm_shouldReturn200() throws Exception {
		OrderResult result = sampleResult();
		when(confirmOrderUseCase.execute(any(OrderId.class))).thenReturn(result);

		mockMvc.perform(post("/api/v1/orders/{id}/confirm", result.id()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(result.id().toString()));
	}

	@Test
	void confirm_shouldReturn409OnInvalidTransition() throws Exception {
		UUID id = UUID.randomUUID();
		when(confirmOrderUseCase.execute(any(OrderId.class)))
				.thenThrow(new InvalidOrderTransitionException(OrderStatus.CONFIRMED, OrderStatus.CONFIRMED));

		mockMvc.perform(post("/api/v1/orders/{id}/confirm", id))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVALID_ORDER_TRANSITION"));
	}

	@Test
	void ready_shouldReturn200() throws Exception {
		OrderResult result = sampleResult();
		when(markOrderReadyUseCase.execute(any(OrderId.class))).thenReturn(result);

		mockMvc.perform(post("/api/v1/orders/{id}/ready", result.id()))
				.andExpect(status().isOk());
	}

	@Test
	void cancel_shouldReturn200WithOptionalReason() throws Exception {
		OrderResult result = sampleResult();
		when(cancelOrderUseCase.execute(any(OrderId.class), any(CancelOrderCommand.class))).thenReturn(result);

		mockMvc.perform(post("/api/v1/orders/{id}/cancel", result.id())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "reason": "Customer requested cancellation" }
								"""))
				.andExpect(status().isOk());
	}

	@Test
	void cancel_shouldReturn400WhenReasonBlank() throws Exception {
		UUID id = UUID.randomUUID();
		when(cancelOrderUseCase.execute(any(OrderId.class), any(CancelOrderCommand.class)))
				.thenThrow(new InvalidOrderDataException("reason must not be blank when provided"));

		mockMvc.perform(post("/api/v1/orders/{id}/cancel", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "reason": "   " }
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_ORDER_DATA"));
	}

	@Test
	void history_shouldReturn200() throws Exception {
		UUID orderId = UUID.randomUUID();
		when(getOrderHistoryUseCase.execute(OrderId.of(orderId))).thenReturn(List.of(
				new OrderHistoryResult(
						UUID.randomUUID(),
						OrderHistoryEventType.ORDER_CREATED,
						null,
						OrderStatus.CREATED,
						null,
						CREATED_AT)));

		mockMvc.perform(get("/api/v1/orders/{id}/history", orderId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(1)))
				.andExpect(jsonPath("$.data[0].eventType").value("ORDER_CREATED"))
				.andExpect(jsonPath("$.data[0].createdAt").value("2026-08-08T22:00:00-05:00"));
	}

	@Test
	void history_shouldReturn404WhenOrderMissing() throws Exception {
		UUID orderId = UUID.randomUUID();
		when(getOrderHistoryUseCase.execute(OrderId.of(orderId)))
				.thenThrow(new ResourceNotFoundException("Order", orderId));

		mockMvc.perform(get("/api/v1/orders/{id}/history", orderId))
				.andExpect(status().isNotFound());
	}

	private static String validBody() {
		return """
				{
				  "customerName": "Ana Torres",
				  "customerPhone": "999999999",
				  "deliveryAddress": "Av. Example 123",
				  "deliveryLatitude": -12.1001,
				  "deliveryLongitude": -77.0201,
				  "totalAmount": 85.90
				}
				""";
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
