package com.fleetbite.identity.infrastructure.inbound.rest;

import com.fleetbite.identity.application.dto.CreateUserCommand;
import com.fleetbite.identity.application.dto.UpdateUserCommand;
import com.fleetbite.identity.application.dto.UserResult;
import com.fleetbite.identity.application.port.in.ActivateUserUseCase;
import com.fleetbite.identity.application.port.in.CreateUserUseCase;
import com.fleetbite.identity.application.port.in.DeactivateUserUseCase;
import com.fleetbite.identity.application.port.in.GetUserByIdUseCase;
import com.fleetbite.identity.application.port.in.ListUsersUseCase;
import com.fleetbite.identity.application.port.in.UpdateUserUseCase;
import com.fleetbite.identity.domain.exception.DuplicateUserEmailException;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.identity.domain.model.UserRole;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.shared.infrastructure.inbound.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({IdentityHttpMapper.class, GlobalExceptionHandler.class})
class UserControllerTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CreateUserUseCase createUserUseCase;
	@MockitoBean
	private GetUserByIdUseCase getUserByIdUseCase;
	@MockitoBean
	private ListUsersUseCase listUsersUseCase;
	@MockitoBean
	private UpdateUserUseCase updateUserUseCase;
	@MockitoBean
	private ActivateUserUseCase activateUserUseCase;
	@MockitoBean
	private DeactivateUserUseCase deactivateUserUseCase;

	@Test
	void listUsers_shouldReturnCollection() throws Exception {
		when(listUsersUseCase.execute()).thenReturn(List.of(sampleResult()));

		mockMvc.perform(get("/api/v1/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].email").value("admin@fleetbite.local"));
	}

	@Test
	void createUser_shouldReturn201() throws Exception {
		when(createUserUseCase.execute(any(CreateUserCommand.class))).thenReturn(sampleResult());

		mockMvc.perform(post("/api/v1/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "admin@fleetbite.local",
								  "password": "Fleetbite1!",
								  "fullName": "FleetBite Admin",
								  "role": "ADMIN"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/v1/users/")))
				.andExpect(jsonPath("$.id").value(USER_ID.toString()));
	}

	@Test
	void createUser_shouldMapDuplicateEmailTo409() throws Exception {
		when(createUserUseCase.execute(any(CreateUserCommand.class)))
				.thenThrow(new DuplicateUserEmailException("admin@fleetbite.local"));

		mockMvc.perform(post("/api/v1/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "admin@fleetbite.local",
								  "password": "Fleetbite1!",
								  "fullName": "FleetBite Admin",
								  "role": "ADMIN"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("DUPLICATE_USER_EMAIL"));
	}

	@Test
	void getUserById_shouldReturn404WhenMissing() throws Exception {
		when(getUserByIdUseCase.execute(any(UserId.class)))
				.thenThrow(new ResourceNotFoundException("User", USER_ID));

		mockMvc.perform(get("/api/v1/users/{id}", USER_ID))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
	}

	@Test
	void updateUser_shouldReturnUpdatedProfile() throws Exception {
		UserResult updated = UserResult.from(User.create(
				UserId.of(USER_ID),
				"admin@fleetbite.local",
				"$2a$hash",
				"Updated Admin",
				UserRole.DISPATCHER,
				CREATED_AT));
		when(updateUserUseCase.execute(eq(UserId.of(USER_ID)), any(UpdateUserCommand.class))).thenReturn(updated);

		mockMvc.perform(put("/api/v1/users/{id}", USER_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "fullName": "Updated Admin",
								  "role": "DISPATCHER"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fullName").value("Updated Admin"))
				.andExpect(jsonPath("$.role").value("DISPATCHER"));
	}

	@Test
	void activateAndDeactivate_shouldReturnUser() throws Exception {
		when(activateUserUseCase.execute(UserId.of(USER_ID))).thenReturn(sampleResult());
		when(deactivateUserUseCase.execute(UserId.of(USER_ID))).thenReturn(sampleResult());

		mockMvc.perform(post("/api/v1/users/{id}/activate", USER_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(USER_ID.toString()));

		mockMvc.perform(post("/api/v1/users/{id}/deactivate", USER_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(USER_ID.toString()));
	}

	private static UserResult sampleResult() {
		return UserResult.from(User.create(
				UserId.of(USER_ID),
				"admin@fleetbite.local",
				"$2a$hash",
				"FleetBite Admin",
				UserRole.ADMIN,
				CREATED_AT));
	}
}
