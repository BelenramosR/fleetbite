package com.fleetbite.shared.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	public static final String BEARER_AUTH = "bearerAuth";

	@Bean
	OpenAPI fleetBiteOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("FleetBite API")
						.description("Operational order, delivery and fleet management API. "
								+ "Every JSON response uses the standard {code, success, data, errors} envelope; "
								+ "204 responses have no body.")
						.version("v1"))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
				.components(new Components()
						.addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
								.name(BEARER_AUTH)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")))
				.addTagsItem(new Tag().name("Authentication").description("Login and JWT issuance"))
				.addTagsItem(new Tag().name("Users").description("User administration (ADMIN only)"))
				.addTagsItem(new Tag().name("Orders").description("Order CRUD, workflow commands and assignment entry points"))
				.addTagsItem(new Tag().name("Drivers").description("Driver lifecycle and availability"))
				.addTagsItem(new Tag().name("Vehicles").description("Fleet vehicle lifecycle"))
				.addTagsItem(new Tag().name("Assignments").description("Delivery assignment lifecycle"));
	}
}
