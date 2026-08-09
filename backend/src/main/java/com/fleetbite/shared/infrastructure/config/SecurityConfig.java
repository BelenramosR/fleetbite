package com.fleetbite.shared.infrastructure.config;

import com.fleetbite.identity.application.port.out.TokenProviderPort;
import com.fleetbite.identity.infrastructure.security.JwtAuthenticationFilter;
import com.fleetbite.identity.infrastructure.security.JwtProperties;
import com.fleetbite.shared.infrastructure.inbound.rest.ApiErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

	@Bean
	JwtAuthenticationFilter jwtAuthenticationFilter(TokenProviderPort tokenProviderPort) {
		return new JwtAuthenticationFilter(tokenProviderPort);
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			JsonMapper jsonMapper) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
						.requestMatchers("/actuator/health").permitAll()
						.requestMatchers("/api/v1/users/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/v1/orders/*/assign")
						.hasAnyRole("ADMIN", "DISPATCHER")
						.requestMatchers("/api/v1/orders/**")
						.hasAnyRole("ADMIN", "RESTAURANT_OPERATOR", "DISPATCHER")
						.requestMatchers("/api/v1/drivers/**").hasAnyRole("ADMIN", "DISPATCHER")
						.requestMatchers("/api/v1/vehicles/**").hasAnyRole("ADMIN", "DISPATCHER")
						.requestMatchers("/api/v1/assignments/**").hasAnyRole("ADMIN", "DISPATCHER")
						.anyRequest().authenticated())
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(authenticationEntryPoint(jsonMapper))
						.accessDeniedHandler(accessDeniedHandler(jsonMapper)))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	private AuthenticationEntryPoint authenticationEntryPoint(JsonMapper jsonMapper) {
		return (request, response, authException) -> writeError(
				jsonMapper,
				response,
				HttpServletResponse.SC_UNAUTHORIZED,
				"AUTHENTICATION_FAILED",
				"Authentication is required",
				request.getRequestURI());
	}

	private AccessDeniedHandler accessDeniedHandler(JsonMapper jsonMapper) {
		return (request, response, accessDeniedException) -> writeError(
				jsonMapper,
				response,
				HttpServletResponse.SC_FORBIDDEN,
				"ACCESS_DENIED",
				"Access is denied",
				request.getRequestURI());
	}

	private void writeError(
			JsonMapper jsonMapper,
			HttpServletResponse response,
			int status,
			String code,
			String message,
			String path) throws java.io.IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		ApiErrorResponse body = new ApiErrorResponse(Instant.now(), status, code, message, path);
		jsonMapper.writeValue(response.getOutputStream(), body);
	}
}
