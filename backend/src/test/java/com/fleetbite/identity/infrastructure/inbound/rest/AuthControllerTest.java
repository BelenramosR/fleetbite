package com.fleetbite.identity.infrastructure.inbound.rest;

import com.fleetbite.identity.application.dto.LoginResult;
import com.fleetbite.identity.application.port.in.AuthenticationUseCase;
import com.fleetbite.identity.domain.exception.AuthenticationFailedException;
import com.fleetbite.identity.domain.exception.UserInactiveException;
import com.fleetbite.shared.infrastructure.inbound.rest.ApiResponseBodyAdvice;
import com.fleetbite.infrastructure.inbound.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({IdentityHttpMapperImpl.class, GlobalExceptionHandler.class, ApiResponseBodyAdvice.class})
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthenticationUseCase authenticationUseCase;

	@Test
	void login_shouldReturnToken() throws Exception {
		when(authenticationUseCase.login(any())).thenReturn(
				LoginResult.bearer("jwt-token", 3600, "refresh-token-uuid"));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "admin@fleetbite.local",
								  "password": "Fleetbite1!"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
				.andExpect(jsonPath("$.data.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.data.expiresIn").value(3600))
				.andExpect(jsonPath("$.data.refreshToken").value("refresh-token-uuid"));
	}

	@Test
	void login_shouldMapAuthenticationFailedTo401() throws Exception {
		when(authenticationUseCase.login(any())).thenThrow(new AuthenticationFailedException());

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "admin@fleetbite.local",
								  "password": "wrong"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
	}

	@Test
	void login_shouldMapInactiveUserTo403() throws Exception {
		when(authenticationUseCase.login(any())).thenThrow(new UserInactiveException());

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "admin@fleetbite.local",
								  "password": "Fleetbite1!"
								}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("USER_INACTIVE"));
	}

	@Test
	void refresh_shouldReturnNewTokens() throws Exception {
		when(authenticationUseCase.refresh(any())).thenReturn(
				LoginResult.bearer("new-jwt", 3600, "new-refresh"));

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "refreshToken": "old-refresh"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.accessToken").value("new-jwt"))
				.andExpect(jsonPath("$.data.refreshToken").value("new-refresh"));
	}

	@Test
	void refresh_shouldMapAuthenticationFailedTo401() throws Exception {
		when(authenticationUseCase.refresh(any())).thenThrow(new AuthenticationFailedException());

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "refreshToken": "revoked-or-invalid"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
	}

	@Test
	void logout_shouldReturn204() throws Exception {
		doNothing().when(authenticationUseCase).logout(any());

		mockMvc.perform(post("/api/v1/auth/logout")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "refreshToken": "any-refresh"
								}
								"""))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));
	}
}
