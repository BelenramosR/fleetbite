package com.fleetbite.shared.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiEnvelopeOpenApiCustomizerTest {

	private final ApiEnvelopeOpenApiCustomizer customizer = new ApiEnvelopeOpenApiCustomizer();

	@Test
	void shouldWrapSuccessfulPayloadAndPreserveItAsData() {
		StringSchema payload = new StringSchema();
		ApiResponse response = new ApiResponse().content(new Content().addMediaType(
				"application/json", new MediaType().schema(payload)));
		OpenAPI openApi = apiWith("200", response);

		customizer.customise(openApi);

		var schema = response.getContent().get("application/json").getSchema();
		assertThat(schema.getProperties()).containsKeys("code", "success", "data", "errors");
		assertThat(schema.getProperties().get("data")).isSameAs(payload);
	}

	@Test
	void shouldLeaveNoContentResponseBodyless() {
		ApiResponse response = new ApiResponse();
		OpenAPI openApi = apiWith("204", response);

		customizer.customise(openApi);

		assertThat(response.getContent()).isNull();
	}

	private OpenAPI apiWith(String status, ApiResponse response) {
		Operation operation = new Operation().responses(new ApiResponses().addApiResponse(status, response));
		return new OpenAPI().paths(new Paths().addPathItem("/test", new PathItem().get(operation)));
	}
}
