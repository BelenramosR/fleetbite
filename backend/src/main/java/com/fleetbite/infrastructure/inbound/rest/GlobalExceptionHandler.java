package com.fleetbite.infrastructure.inbound.rest;

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
import com.fleetbite.vehicle.domain.exception.VehicleAssignedToDriverException;
import com.fleetbite.identity.domain.exception.AuthenticationFailedException;
import com.fleetbite.identity.domain.exception.DuplicateUserEmailException;
import com.fleetbite.identity.domain.exception.InvalidUserDataException;
import com.fleetbite.identity.domain.exception.UserInactiveException;
import com.fleetbite.order.domain.exception.InvalidOrderDataException;
import com.fleetbite.order.domain.exception.InvalidOrderTransitionException;
import com.fleetbite.order.domain.exception.OrderNotDeletableException;
import com.fleetbite.order.domain.exception.OrderNotEditableException;
import com.fleetbite.shared.application.exception.ApplicationException;
import com.fleetbite.shared.application.exception.ForbiddenOperationException;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.exception.DomainException;
import com.fleetbite.shared.infrastructure.inbound.rest.ApiErrorItem;
import com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse;
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

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		List<ApiErrorItem> errors = exception.getBindingResult().getFieldErrors().stream()
				.map(this::toApiError)
				.toList();
		return ResponseEntity.badRequest().body(ApiResponse.failure("VALIDATION_ERROR", errors));
	}

	@ExceptionHandler({
			InvalidOrderDataException.class,
			InvalidDriverDataException.class,
			InvalidVehicleDataException.class,
			InvalidAssignmentDataException.class,
			InvalidUserDataException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleInvalidDomainData(
			DomainException exception,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, exception.getCode(), exception.getMessage());
	}

	@ExceptionHandler(AuthenticationFailedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAuthenticationFailed(
			AuthenticationFailedException exception,
			HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, exception.getCode(), exception.getMessage());
	}

	@ExceptionHandler(UserInactiveException.class)
	public ResponseEntity<ApiResponse<Void>> handleUserInactive(
			UserInactiveException exception,
			HttpServletRequest request) {
		return build(HttpStatus.FORBIDDEN, exception.getCode(), exception.getMessage());
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
	public ResponseEntity<ApiResponse<Void>> handleConflict(
			DomainException exception,
			HttpServletRequest request) {
		return build(HttpStatus.CONFLICT, exception.getCode(), exception.getMessage());
	}

	@ExceptionHandler({
			OptimisticLockingFailureException.class,
			ObjectOptimisticLockingFailureException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(
			OptimisticLockingFailureException exception,
			HttpServletRequest request) {
		return build(
				HttpStatus.CONFLICT,
				"OPTIMISTIC_LOCK_CONFLICT",
				"The resource was modified concurrently; please retry");
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotFound(
			ResourceNotFoundException exception,
			HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, exception.getCode(), exception.getMessage());
	}

	@ExceptionHandler(ForbiddenOperationException.class)
	public ResponseEntity<ApiResponse<Void>> handleForbiddenOperation(
			ForbiddenOperationException exception,
			HttpServletRequest request) {
		return build(HttpStatus.FORBIDDEN, exception.getCode(), exception.getMessage());
	}

	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<ApiResponse<Void>> handleApplication(
			ApplicationException exception,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, exception.getCode(), exception.getMessage());
	}

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ApiResponse<Void>> handleDomain(
			DomainException exception,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, exception.getCode(), exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
			MethodArgumentTypeMismatchException exception,
			HttpServletRequest request) {
		return build(
				HttpStatus.BAD_REQUEST,
				"INVALID_REQUEST",
				"Invalid value for parameter '" + exception.getName() + "'");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleUnreadable(
			HttpMessageNotReadableException exception,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Malformed JSON request");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnexpected(
			Exception exception,
			HttpServletRequest request) {
		return build(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"INTERNAL_ERROR",
				"Unexpected server error");
	}

	private ApiErrorItem toApiError(FieldError error) {
		return new ApiErrorItem(error.getField(), error.getDefaultMessage());
	}

	private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String code, String message) {
		return ResponseEntity.status(status).body(ApiResponse.failure(code, message));
	}
}
