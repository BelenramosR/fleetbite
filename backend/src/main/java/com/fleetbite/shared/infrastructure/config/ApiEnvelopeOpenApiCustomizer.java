package com.fleetbite.shared.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class ApiEnvelopeOpenApiCustomizer implements OpenApiCustomizer {

	private static final String JSON = "application/json";

	@Override
	public void customise(OpenAPI openApi) {
		if (openApi.getPaths() == null) {
			return;
		}
		openApi.getPaths().values().stream()
				.flatMap(path -> path.readOperations().stream())
				.forEach(operation -> operation.getResponses().forEach(this::wrapSuccessfulResponse));
	}

	private void wrapSuccessfulResponse(String status, ApiResponse response) {
		if (!status.startsWith("2") || "204".equals(status)) {
			return;
		}
		Content content = response.getContent();
		if (content == null) {
			content = new Content();
			response.setContent(content);
		}
		MediaType mediaType = content.computeIfAbsent(JSON, ignored -> new MediaType());
		Schema<?> payload = mediaType.getSchema();
		if (isEnvelope(payload)) {
			return;
		}
		mediaType.setSchema(envelope(payload == null ? new ObjectSchema() : payload));
	}

	private Schema<?> envelope(Schema<?> payload) {
		ObjectSchema envelope = new ObjectSchema();
		envelope.description("Standard API success envelope");
		envelope.addProperty("code", new StringSchema().example("OK"));
		envelope.addProperty("success", new BooleanSchema().example(true));
		envelope.addProperty("data", payload);
		envelope.addProperty("errors", new ArraySchema()
				.items(new Schema<>().$ref("#/components/schemas/ApiErrorItem")));
		envelope.required(List.of("code", "success", "data", "errors"));
		return envelope;
	}

	private boolean isEnvelope(Schema<?> schema) {
		return schema != null && schema.getProperties() != null
				&& schema.getProperties().containsKey("success")
				&& schema.getProperties().containsKey("data")
				&& schema.getProperties().containsKey("errors");
	}
}
