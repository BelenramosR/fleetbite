package com.fleetbite.driver.infrastructure.inbound.rest;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.in.DriverSelfUseCase;
import com.fleetbite.driver.infrastructure.inbound.rest.request.UpdateDriverLocationRequest;
import com.fleetbite.driver.infrastructure.inbound.rest.response.DriverResponse;
import com.fleetbite.identity.application.dto.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/drivers/me")
public class DriverSelfController {

	private final DriverSelfUseCase driverSelfUseCase;
	private final DriverHttpMapper driverHttpMapper;

	public DriverSelfController(DriverSelfUseCase driverSelfUseCase, DriverHttpMapper driverHttpMapper) {
		this.driverSelfUseCase = Objects.requireNonNull(driverSelfUseCase);
		this.driverHttpMapper = Objects.requireNonNull(driverHttpMapper);
	}

	@GetMapping
	public DriverResponse profile(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
		return toResponse(driverSelfUseCase.getProfile(principal.userId()));
	}

	@PatchMapping("/location")
	public DriverResponse updateLocation(
			@AuthenticationPrincipal AuthenticatedPrincipal principal,
			@Valid @RequestBody UpdateDriverLocationRequest request) {
		return toResponse(driverSelfUseCase.updateLocation(
				principal.userId(), driverHttpMapper.toCommand(request)));
	}

	@PostMapping("/online")
	public DriverResponse goOnline(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
		return toResponse(driverSelfUseCase.goOnline(principal.userId()));
	}

	@PostMapping("/offline")
	public DriverResponse goOffline(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
		return toResponse(driverSelfUseCase.goOffline(principal.userId()));
	}

	private DriverResponse toResponse(DriverResult result) {
		return driverHttpMapper.toResponse(result);
	}
}
