package com.fleetbite.shared.infrastructure.inbound.rest;

import com.fleetbite.delivery.domain.exception.ActiveAssignmentAlreadyExistsException;
import com.fleetbite.delivery.domain.exception.DriverNotAssignableException;
import com.fleetbite.delivery.domain.exception.InvalidAssignmentDataException;
import com.fleetbite.delivery.domain.exception.OrderNotAssignableException;
import com.fleetbite.driver.domain.exception.DriverAlreadyLinkedToUserException;
import com.fleetbite.driver.domain.exception.DriverNotDeletableException;
import com.fleetbite.driver.domain.exception.DriverUserNotEligibleException;
import com.fleetbite.driver.domain.exception.DuplicateDriverPhoneException;
import com.fleetbite.driver.domain.exception.InvalidDriverDataException;
import com.fleetbite.driver.domain.exception.VehicleAlreadyAssignedException;
import com.fleetbite.driver.domain.exception.VehicleAssignedToDriverException;
import com.fleetbite.identity.domain.exception.AuthenticationFailedException;
import com.fleetbite.identity.domain.exception.DuplicateUserEmailException;
import com.fleetbite.identity.domain.exception.InvalidUserDataException;
import com.fleetbite.identity.domain.exception.UserInactiveException;
import com.fleetbite.order.domain.exception.InvalidOrderDataException;
import com.fleetbite.order.domain.exception.InvalidOrderTransitionException;
import com.fleetbite.order.domain.exception.OrderNotDeletableException;
import com.fleetbite.order.domain.exception.OrderNotEditableException;
import com.fleetbite.shared.application.exception.ApplicationException;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.exception.DomainException;
import com.fleetbite.vehicle.domain.exception.DuplicateVehiclePlateException;
import com.fleetbite.vehicle.domain.exception.InvalidVehicleDataException;
import com.fleetbite.vehicle.domain.exception.InvalidVehicleTransitionException;
import com.fleetbite.vehicle.domain.exception.VehicleNotAssignableException;
import com.fleetbite.vehicle.domain.exception.VehicleNotDeletableException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(this::formatFieldError)
				.collect(Collectors.joining("; "));
		return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request.getRequestURI());
	}

	@ExceptionHandler({
			InvalidOrderDataException.class,
			InvalidDriverDataException.class,
			InvalidVehicleDataException.class,
			InvalidAssignmentDataException.class,
			InvalidUserDataException.class
	})
	public ResponseEntity<ApiErrorResponse> handleInvalidDomainData(
			DomainException exception,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, exception.getCode(), exception.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(AuthenticationFailedException.class)
	public ResponseEntity<ApiErrorResponse> handleAuthenticationFailed(
			AuthenticationFailedException exception,
			HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, exception.getCode(), exception.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(UserInactiveException.class)
	public ResponseEntity<ApiErrorResponse> handleUserInactive(
			UserInactiveException exception,
			HttpServletRequest request) {
		return build(HttpStatus.FORBIDDEN, exception.getCode(), exception.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler({
			OrderNotEditableException.class,
			OrderNotDeletableException.class,
			InvalidOrderTransitionException.class,
			DriverNotDeletableException.class,
			DuplicateDriverPhoneException.class,
			DriverUserNotEligibleException.class,
			DriverAlreadyLinkedToUserException.class,
			VehicleAlreadyAssignedException.class,
			VehicleAssignedToDriverException.class,
			VehicleNotAssignableException.class,
			VehicleNotDeletableException.class,
			DuplicateVehiclePlateException.class,
			InvalidVehicleTransitionException.class,
			ActiveAssignmentAlreadyExistsException.class,
			OrderNotAssignableException.class,
			DriverNotAssignableException.class,
			DuplicateUserEmailException.class
	})
	public ResponseEntity<ApiErrorResponse> handleConflict(
			DomainException exception,
			HttpServletRequest request) {
		return build(HttpStatus.CONFLICT, exception.getCode(), exception.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler({
			OptimisticLockingFailureException.class,
			ObjectOptimisticLockingFailureException.class
	})
	public ResponseEntity<ApiErrorResponse> handleOptimisticLock(
			OptimisticLockingFailureException exception,
			HttpServletRequest request) {
		return build(
				HttpStatus.CONFLICT,
				"OPTIMISTIC_LOCK_CONFLICT",
				"The resource was modified concurrently; please retry",
				request.getRequestURI());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(
			ResourceNotFoundException exception,
			HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, exception.getCode(), exception.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<ApiErrorResponse> handleApplication(
			ApplicationException exception,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, exception.getCode(), exception.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ApiErrorResponse> handleDomain(
			DomainException exception,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, exception.getCode(), exception.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
			MethodArgumentTypeMismatchException exception,
			HttpServletRequest request) {
		return build(
				HttpStatus.BAD_REQUEST,
				"INVALID_REQUEST",
				"Invalid value for parameter '" + exception.getName() + "'",
				request.getRequestURI());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadable(
			HttpMessageNotReadableException exception,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Malformed JSON request", request.getRequestURI());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(
			Exception exception,
			HttpServletRequest request) {
		return build(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"INTERNAL_ERROR",
				"Unexpected server error",
				request.getRequestURI());
	}

	private String formatFieldError(FieldError error) {
		return error.getField() + ": " + error.getDefaultMessage();
	}

	private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String code, String message, String path) {
		ApiErrorResponse body = new ApiErrorResponse(Instant.now(), status.value(), code, message, path);
		return ResponseEntity.status(status).body(body);
	}
}
