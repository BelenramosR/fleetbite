package com.fleetbite.identity.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SecurityIntegrationTest {

	private static final String PASSWORD = "Fleetbite1!";
	private static final UUID FAKE_ORDER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

	@Autowired
	private MockMvc mockMvc;

	@Test
	void health_shouldBePublic() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk());
	}

	@Test
	void login_shouldReturnJwtForSeedAdmin() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "admin@fleetbite.local",
								  "password": "Fleetbite1!"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken", notNullValue()))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(3600))
				.andExpect(jsonPath("$.refreshToken", notNullValue()));
	}

	@Test
	void refresh_shouldReturnNewTokensAndInvalidatePrevious() throws Exception {
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "admin@fleetbite.local",
								  "password": "Fleetbite1!"
								}
								"""))
				.andExpect(status().isOk())
				.andReturn();
		String body = login.getResponse().getContentAsString();
		String refreshToken = extractJsonString(body, "refreshToken");

		MvcResult refreshed = mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "refreshToken": "%s"
								}
								""".formatted(refreshToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken", notNullValue()))
				.andExpect(jsonPath("$.refreshToken", notNullValue()))
				.andReturn();
		String newRefresh = extractJsonString(refreshed.getResponse().getContentAsString(), "refreshToken");

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "refreshToken": "%s"
								}
								""".formatted(refreshToken)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));

		mockMvc.perform(get("/api/v1/users")
						.header("Authorization", "Bearer "
								+ extractJsonString(refreshed.getResponse().getContentAsString(), "accessToken")))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "refreshToken": "%s"
								}
								""".formatted(newRefresh)))
				.andExpect(status().isOk());
	}

	@Test
	void refresh_shouldReturn401ForInvalidToken() throws Exception {
		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "refreshToken": "00000000-0000-0000-0000-000000000000"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
	}

	@Test
	void logout_thenRefreshShouldFail() throws Exception {
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "admin@fleetbite.local",
								  "password": "Fleetbite1!"
								}
								"""))
				.andExpect(status().isOk())
				.andReturn();
		String refreshToken = extractJsonString(login.getResponse().getContentAsString(), "refreshToken");

		mockMvc.perform(post("/api/v1/auth/logout")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "refreshToken": "%s"
								}
								""".formatted(refreshToken)))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/auth/logout")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "refreshToken": "%s"
								}
								""".formatted(refreshToken)))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "refreshToken": "%s"
								}
								""".formatted(refreshToken)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
	}

	@Test
	void login_shouldReturn401WhenCredentialsInvalid() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "admin@fleetbite.local",
								  "password": "wrong-password"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
	}

	@Test
	void login_shouldReturn403WhenUserInactive() throws Exception {
		String adminToken = login("admin@fleetbite.local");

		MvcResult created = mockMvc.perform(post("/api/v1/users")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "inactive.user@fleetbite.local",
								  "password": "Fleetbite1!",
								  "fullName": "Inactive User",
								  "role": "DRIVER"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();
		String createdBody = created.getResponse().getContentAsString();
		String userId = extractJsonString(createdBody, "id");

		mockMvc.perform(post("/api/v1/users/{id}/deactivate", userId)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("INACTIVE"));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "inactive.user@fleetbite.local",
								  "password": "Fleetbite1!"
								}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("USER_INACTIVE"));
	}

	@Test
	void protectedEndpoint_shouldReturn401WithoutToken() throws Exception {
		mockMvc.perform(get("/api/v1/orders"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
	}

	@Test
	void admin_shouldAccessUsers() throws Exception {
		String token = login("admin@fleetbite.local");

		mockMvc.perform(get("/api/v1/users")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].email", notNullValue()));
	}

	@Test
	void operator_shouldAccessOrdersButNotAssignOrDrivers() throws Exception {
		String token = login("operator@fleetbite.local");

		mockMvc.perform(get("/api/v1/orders")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/orders/{orderId}/assign", FAKE_ORDER_ID)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "driverId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
								}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

		mockMvc.perform(post("/api/v1/orders/{orderId}/auto-assign", FAKE_ORDER_ID)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

		mockMvc.perform(get("/api/v1/drivers")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

		mockMvc.perform(get("/api/v1/users")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
	}

	@Test
	void dispatcher_shouldAssignAndAccessDriversButNotUsers() throws Exception {
		String token = login("dispatcher@fleetbite.local");

		mockMvc.perform(get("/api/v1/drivers")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/orders/{orderId}/assign", FAKE_ORDER_ID)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "driverId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
								}
								"""))
				.andExpect(status().isNotFound());

		mockMvc.perform(post("/api/v1/orders/{orderId}/auto-assign", FAKE_ORDER_ID)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/v1/users")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
	}

	@Test
	void admin_shouldAccessAutoAssign() throws Exception {
		String token = login("admin@fleetbite.local");

		mockMvc.perform(post("/api/v1/orders/{orderId}/auto-assign", FAKE_ORDER_ID)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound());
	}

	@Test
	void driver_shouldBeDeniedAutoAssign() throws Exception {
		String token = login("driver@fleetbite.local");

		mockMvc.perform(post("/api/v1/orders/{orderId}/auto-assign", FAKE_ORDER_ID)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
	}

	@Test
	void driver_shouldBeAuthenticatedButDeniedBusinessModules() throws Exception {
		String token = login("driver@fleetbite.local");

		mockMvc.perform(get("/api/v1/orders")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

		mockMvc.perform(get("/api/v1/drivers")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

		mockMvc.perform(get("/api/v1/vehicles")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

		mockMvc.perform(get("/api/v1/assignments")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
	}

	@Test
	void driver_shouldBeDeniedOrderHistory() throws Exception {
		String token = login("driver@fleetbite.local");

		mockMvc.perform(get("/api/v1/orders/{id}/history", FAKE_ORDER_ID)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
	}

	private String login(String email) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s"
								}
								""".formatted(email, PASSWORD)))
				.andExpect(status().isOk())
				.andReturn();
		return extractJsonString(result.getResponse().getContentAsString(), "accessToken");
	}

	private static String extractJsonString(String body, String field) {
		String marker = "\"" + field + "\":\"";
		int start = body.indexOf(marker) + marker.length();
		int end = body.indexOf('"', start);
		return body.substring(start, end);
	}
}
