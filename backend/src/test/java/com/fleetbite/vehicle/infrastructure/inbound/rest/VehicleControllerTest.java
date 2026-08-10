package com.fleetbite.vehicle.infrastructure.inbound.rest;

import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.shared.infrastructure.inbound.rest.ApiResponseBodyAdvice;
import com.fleetbite.infrastructure.inbound.rest.GlobalExceptionHandler;
import com.fleetbite.vehicle.application.dto.CreateVehicleCommand;
import com.fleetbite.vehicle.application.dto.UpdateVehicleCommand;
import com.fleetbite.vehicle.application.dto.VehicleResult;
import com.fleetbite.vehicle.application.port.in.CreateVehicleUseCase;
import com.fleetbite.vehicle.application.port.in.DeleteVehicleUseCase;
import com.fleetbite.vehicle.application.port.in.VehicleLifecycleUseCase;
import com.fleetbite.vehicle.application.port.in.VehicleQueryUseCase;
import com.fleetbite.vehicle.application.port.in.UpdateVehicleUseCase;
import com.fleetbite.vehicle.domain.exception.DuplicateVehiclePlateException;
import com.fleetbite.vehicle.domain.exception.InvalidVehicleTransitionException;
import com.fleetbite.vehicle.domain.exception.VehicleNotDeletableException;
import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleStatus;
import com.fleetbite.vehicle.domain.model.VehicleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(controllers = VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({VehicleHttpMapperImpl.class, GlobalExceptionHandler.class, ApiResponseBodyAdvice.class})
class VehicleControllerTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CreateVehicleUseCase createVehicleUseCase;

	@MockitoBean
	private VehicleQueryUseCase vehicleQueryUseCase;

	@MockitoBean
	private UpdateVehicleUseCase updateVehicleUseCase;

	@MockitoBean
	private DeleteVehicleUseCase deleteVehicleUseCase;

	@MockitoBean
	private VehicleLifecycleUseCase vehicleLifecycleUseCase;

	@Test
	void createVehicle_shouldReturn201() throws Exception {
		VehicleResult result = sampleResult();
		when(createVehicleUseCase.execute(any(CreateVehicleCommand.class))).thenReturn(result);

		mockMvc.perform(post("/api/v1/vehicles")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "plate": "ABC-123",
								  "type": "MOTORCYCLE"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/api/v1/vehicles/" + result.id())))
				.andExpect(jsonPath("$.data.status").value("AVAILABLE"))
				.andExpect(jsonPath("$.data.createdAt").value("2026-08-08T22:00:00-05:00"));
	}

	@Test
	void createVehicle_shouldReturn409OnDuplicatePlate() throws Exception {
		when(createVehicleUseCase.execute(any(CreateVehicleCommand.class)))
				.thenThrow(new DuplicateVehiclePlateException("ABC-123"));

		mockMvc.perform(post("/api/v1/vehicles")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "plate": "ABC-123",
								  "type": "MOTORCYCLE"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("DUPLICATE_VEHICLE_PLATE"));
	}

	@Test
	void listVehicles_shouldReturn200() throws Exception {
		when(vehicleQueryUseCase.findAll()).thenReturn(List.of(sampleResult()));

		mockMvc.perform(get("/api/v1/vehicles"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(1)))
				.andExpect(jsonPath("$.data[0].plate").value("ABC-123"));
	}

	@Test
	void listVehicles_shouldReturnEmptyArray() throws Exception {
		when(vehicleQueryUseCase.findAll()).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/vehicles"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(0)));
	}

	@Test
	void getVehicleById_shouldReturn200() throws Exception {
		VehicleResult result = sampleResult();
		when(vehicleQueryUseCase.getById(result.id())).thenReturn(result);

		mockMvc.perform(get("/api/v1/vehicles/{id}", result.id()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(result.id().toString()));
	}

	@Test
	void getVehicleById_shouldReturn404() throws Exception {
		UUID id = UUID.randomUUID();
		when(vehicleQueryUseCase.getById(id))
				.thenThrow(new ResourceNotFoundException("Vehicle", id));

		mockMvc.perform(get("/api/v1/vehicles/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
	}

	@Test
	void updateVehicle_shouldReturn200() throws Exception {
		VehicleResult result = sampleResult();
		when(updateVehicleUseCase.execute(eq(result.id()), any(UpdateVehicleCommand.class)))
				.thenReturn(result);

		mockMvc.perform(put("/api/v1/vehicles/{id}", result.id())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "plate": "XYZ-999",
								  "type": "CAR"
								}
								"""))
				.andExpect(status().isOk());
	}

	@Test
	void updateVehicle_shouldReturn400WhenInvalid() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(put("/api/v1/vehicles/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "plate": "   ",
								  "type": "CAR"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.errors[0].field").isNotEmpty())
				.andExpect(jsonPath("$.errors[0].message").isNotEmpty());

		verifyNoInteractions(updateVehicleUseCase);
	}

	@Test
	void deleteVehicle_shouldReturn204() throws Exception {
		UUID id = UUID.randomUUID();
		doNothing().when(deleteVehicleUseCase).execute(id);

		mockMvc.perform(delete("/api/v1/vehicles/{id}", id))
				.andExpect(status().isNoContent());

		verify(deleteVehicleUseCase).execute(id);
	}

	@Test
	void deleteVehicle_shouldReturn409WhenNotDeletable() throws Exception {
		UUID id = UUID.randomUUID();
		doThrow(new VehicleNotDeletableException(VehicleStatus.AVAILABLE))
				.when(deleteVehicleUseCase).execute(id);

		mockMvc.perform(delete("/api/v1/vehicles/{id}", id))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("VEHICLE_NOT_DELETABLE"));
	}

	@Test
	void maintenance_shouldReturn200() throws Exception {
		VehicleResult result = maintenanceResult();
		when(vehicleLifecycleUseCase.sendToMaintenance(result.id())).thenReturn(result);

		mockMvc.perform(post("/api/v1/vehicles/{id}/maintenance", result.id()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("MAINTENANCE"));
	}

	@Test
	void activate_shouldReturn200() throws Exception {
		VehicleResult result = sampleResult();
		when(vehicleLifecycleUseCase.activate(result.id())).thenReturn(result);

		mockMvc.perform(post("/api/v1/vehicles/{id}/activate", result.id()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("AVAILABLE"));
	}

	@Test
	void deactivate_shouldReturn200() throws Exception {
		VehicleResult result = inactiveResult();
		when(vehicleLifecycleUseCase.deactivate(result.id())).thenReturn(result);

		mockMvc.perform(post("/api/v1/vehicles/{id}/deactivate", result.id()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("INACTIVE"));
	}

	@Test
	void maintenance_shouldReturn409OnInvalidTransition() throws Exception {
		UUID id = UUID.randomUUID();
		when(vehicleLifecycleUseCase.sendToMaintenance(id))
				.thenThrow(new InvalidVehicleTransitionException(VehicleStatus.INACTIVE, VehicleStatus.MAINTENANCE));

		mockMvc.perform(post("/api/v1/vehicles/{id}/maintenance", id))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVALID_VEHICLE_TRANSITION"));
	}

	private static VehicleResult sampleResult() {
		return VehicleResult.from(
				Vehicle.create(UUID.randomUUID(), "ABC-123", VehicleType.MOTORCYCLE, CREATED_AT));
	}

	private static VehicleResult maintenanceResult() {
		Vehicle vehicle = Vehicle.create(UUID.randomUUID(), "ABC-123", VehicleType.MOTORCYCLE, CREATED_AT);
		vehicle.sendToMaintenance(CREATED_AT.plusMinutes(1));
		return VehicleResult.from(vehicle);
	}

	private static VehicleResult inactiveResult() {
		Vehicle vehicle = Vehicle.create(UUID.randomUUID(), "ABC-123", VehicleType.MOTORCYCLE, CREATED_AT);
		vehicle.deactivate(CREATED_AT.plusMinutes(1));
		return VehicleResult.from(vehicle);
	}
}
