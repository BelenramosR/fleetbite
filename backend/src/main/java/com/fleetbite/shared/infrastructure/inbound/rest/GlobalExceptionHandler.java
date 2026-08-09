package com.fleetbite.shared.infrastructure.inbound.rest;

import com.fleetbite.order.domain.exception.InvalidOrderDataException;
import com.fleetbite.order.domain.exception.OrderNotDeletableException;
import com.fleetbite.order.domain.exception.OrderNotEditableException;
import com.fleetbite.shared.application.exception.ApplicationException;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

	@ExceptionHandler(InvalidOrderDataException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidOrderData(
			InvalidOrderDataException exception,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, exception.getCode(), exception.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler({OrderNotEditableException.class, OrderNotDeletableException.class})
	public ResponseEntity<ApiErrorResponse> handleConflict(
			DomainException exception,
			HttpServletRequest request) {
		return build(HttpStatus.CONFLICT, exception.getCode(), exception.getMessage(), request.getRequestURI());
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
