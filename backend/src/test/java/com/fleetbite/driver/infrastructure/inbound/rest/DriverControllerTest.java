package com.fleetbite.driver.infrastructure.inbound.rest;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.dto.UpdateDriverCommand;
import com.fleetbite.driver.application.dto.UpdateDriverLocationCommand;
import com.fleetbite.driver.application.port.in.AssignVehicleToDriverUseCase;
import com.fleetbite.driver.application.port.in.DeleteDriverUseCase;
import com.fleetbite.driver.application.port.in.GetDriverByIdUseCase;
import com.fleetbite.driver.application.port.in.ListDriversUseCase;
import com.fleetbite.driver.application.port.in.SetDriverOfflineUseCase;
import com.fleetbite.driver.application.port.in.SetDriverOnlineUseCase;
import com.fleetbite.driver.application.port.in.UnassignVehicleFromDriverUseCase;
import com.fleetbite.driver.application.port.in.UpdateDriverLocationUseCase;
import com.fleetbite.driver.application.port.in.UpdateDriverUseCase;
import com.fleetbite.driver.domain.exception.DriverNotDeletableException;
import com.fleetbite.driver.domain.exception.InvalidDriverTransitionException;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.identity.domain.model.UserId;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DriverController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({DriverHttpMapper.class, GlobalExceptionHandler.class, ApiResponseBodyAdvice.class})
class DriverControllerTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final UUID USER_UUID = UUID.fromString("44444444-4444-4444-4444-444444444444");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GetDriverByIdUseCase getDriverByIdUseCase;

	@MockitoBean
	private ListDriversUseCase listDriversUseCase;

	@MockitoBean
	private UpdateDriverUseCase updateDriverUseCase;

	@MockitoBean
	private DeleteDriverUseCase deleteDriverUseCase;

	@MockitoBean
	private UpdateDriverLocationUseCase updateDriverLocationUseCase;

	@MockitoBean
	private SetDriverOnlineUseCase setDriverOnlineUseCase;

	@MockitoBean
	private SetDriverOfflineUseCase setDriverOfflineUseCase;

	@MockitoBean
	private AssignVehicleToDriverUseCase assignVehicleToDriverUseCase;

	@MockitoBean
	private UnassignVehicleFromDriverUseCase unassignVehicleFromDriverUseCase;

	@Test
	void listDrivers_shouldReturn200WithItems() throws Exception {
		when(listDriversUseCase.execute()).thenReturn(List.of(sampleResult(null)));

		mockMvc.perform(get("/api/v1/drivers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(1)))
				.andExpect(jsonPath("$.data[0].status").value("OFFLINE"));
	}

	@Test
	void listDrivers_shouldReturnEmptyArray() throws Exception {
		when(listDriversUseCase.execute()).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/drivers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(0)));
	}

	@Test
	void getDriverById_shouldReturn200() throws Exception {
		DriverResult result = sampleResult(null);
		when(getDriverByIdUseCase.execute(DriverId.of(result.id()))).thenReturn(result);

		mockMvc.perform(get("/api/v1/drivers/{id}", result.id()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(result.id().toString()));
	}

	@Test
	void getDriverById_shouldReturn404() throws Exception {
		UUID id = UUID.randomUUID();
		when(getDriverByIdUseCase.execute(DriverId.of(id)))
				.thenThrow(new ResourceNotFoundException("Driver", id));

		mockMvc.perform(get("/api/v1/drivers/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
	}

	@Test
	void updateDriver_shouldReturn200() throws Exception {
		DriverResult result = sampleResult(null);
		when(updateDriverUseCase.execute(eq(DriverId.of(result.id())), any(UpdateDriverCommand.class)))
				.thenReturn(result);

		mockMvc.perform(put("/api/v1/drivers/{id}", result.id())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "phone": "988000111"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(result.id().toString()));
	}

	@Test
	void updateDriver_shouldReturn400WhenInvalid() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(put("/api/v1/drivers/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "phone": "   "
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		verifyNoInteractions(updateDriverUseCase);
	}

	@Test
	void deleteDriver_shouldReturn204() throws Exception {
		UUID id = UUID.randomUUID();
		doNothing().when(deleteDriverUseCase).execute(DriverId.of(id));

		mockMvc.perform(delete("/api/v1/drivers/{id}", id))
				.andExpect(status().isNoContent());

		verify(deleteDriverUseCase).execute(DriverId.of(id));
	}

	@Test
	void deleteDriver_shouldReturn409WhenNotDeletable() throws Exception {
		UUID id = UUID.randomUUID();
		doThrow(new DriverNotDeletableException(DriverStatus.AVAILABLE))
				.when(deleteDriverUseCase).execute(DriverId.of(id));

		mockMvc.perform(delete("/api/v1/drivers/{id}", id))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("DRIVER_NOT_DELETABLE"));
	}

	@Test
	void updateLocation_shouldReturn200() throws Exception {
		DriverResult result = sampleResult(new Location(-12.10, -77.03));
		when(updateDriverLocationUseCase.execute(
				eq(DriverId.of(result.id())),
				any(UpdateDriverLocationCommand.class))).thenReturn(result);

		mockMvc.perform(patch("/api/v1/drivers/{id}/location", result.id())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "latitude": -12.10,
								  "longitude": -77.03
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.currentLatitude").value(-12.10))
				.andExpect(jsonPath("$.data.currentLongitude").value(-77.03));
	}

	@Test
	void goOnline_shouldReturn200() throws Exception {
		DriverResult result = availableResult();
		when(setDriverOnlineUseCase.execute(DriverId.of(result.id()))).thenReturn(result);

		mockMvc.perform(post("/api/v1/drivers/{id}/online", result.id()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("AVAILABLE"));
	}

	@Test
	void goOnline_shouldReturn400WithoutLocation() throws Exception {
		UUID id = UUID.randomUUID();
		when(setDriverOnlineUseCase.execute(DriverId.of(id)))
				.thenThrow(new InvalidDriverTransitionException(
						"Driver cannot go online without a valid currentLocation"));

		mockMvc.perform(post("/api/v1/drivers/{id}/online", id))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_DRIVER_TRANSITION"));
	}

	@Test
	void goOffline_shouldReturn200() throws Exception {
		DriverResult result = sampleResult(new Location(-12.10, -77.03));
		when(setDriverOfflineUseCase.execute(DriverId.of(result.id()))).thenReturn(result);

		mockMvc.perform(post("/api/v1/drivers/{id}/offline", result.id()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("OFFLINE"));
	}

	private static DriverResult sampleResult(Location location) {
		return DriverResult.from(
				Driver.create(DriverId.generate(), UserId.of(USER_UUID), "999888777", location, CREATED_AT),
				"Carlos Perez");
	}

	private static DriverResult availableResult() {
		Driver driver = Driver.create(
				DriverId.generate(),
				UserId.of(USER_UUID),
				"999888777",
				new Location(-12.10, -77.03),
				CREATED_AT);
		driver.goOnline(CREATED_AT.plusMinutes(1));
		return DriverResult.from(driver, "Carlos Perez");
	}
}
