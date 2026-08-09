package com.fleetbite.shared.infrastructure.inbound.rest;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Standard API envelope for success and error responses")
public record ApiResponse<T>(
		@Schema(description = "Whether the request succeeded", example = "true")
		boolean success,
		@Schema(description = "Stable application code", example = "OK")
		String code,
		@Schema(description = "Payload when success is true")
		T data,
		@Schema(description = "Error details when success is false")
		List<ApiErrorItem> errors) {

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, "OK", data, List.of());
	}

	public static <T> ApiResponse<T> success(String code, T data) {
		return new ApiResponse<>(true, code, data, List.of());
	}

	public static <T> ApiResponse<T> failure(String code, String message) {
		return new ApiResponse<>(false, code, null, List.of(new ApiErrorItem(message)));
	}

	public static <T> ApiResponse<T> failure(String code, List<ApiErrorItem> errors) {
		return new ApiResponse<>(false, code, null, errors == null ? List.of() : List.copyOf(errors));
	}
}
