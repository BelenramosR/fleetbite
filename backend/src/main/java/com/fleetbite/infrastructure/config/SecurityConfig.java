package com.fleetbite.infrastructure.config;

import com.fleetbite.identity.application.port.out.TokenProviderPort;
import com.fleetbite.identity.infrastructure.security.JwtAuthenticationFilter;
import com.fleetbite.identity.infrastructure.security.LoginRateLimitFilter;
import com.fleetbite.identity.infrastructure.jwt.JwtProperties;
import com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.time.Clock;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	JwtAuthenticationFilter jwtAuthenticationFilter(TokenProviderPort tokenProviderPort) {
		return new JwtAuthenticationFilter(tokenProviderPort);
	}

	@Bean
	LoginRateLimitFilter loginRateLimitFilter(
			JsonMapper jsonMapper,
			Clock clock,
			@Value("${fleetbite.security.login-rate-limit.max-attempts:10}") int maxAttempts,
			@Value("${fleetbite.security.login-rate-limit.window-seconds:60}") long windowSeconds) {
		return new LoginRateLimitFilter(jsonMapper, clock, maxAttempts, windowSeconds);
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			LoginRateLimitFilter loginRateLimitFilter,
			JsonMapper jsonMapper,
			CorsConfigurationSource corsConfigurationSource) throws Exception {
		http
				.cors(cors -> cors.configurationSource(corsConfigurationSource))
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()
						.requestMatchers("/actuator/health").permitAll()
						.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.requestMatchers("/api/v1/users/**").hasRole("ADMIN")
						.requestMatchers("/api/v1/drivers/me", "/api/v1/drivers/me/**").hasRole("DRIVER")
						.requestMatchers("/api/v1/driver/assignments/**").hasRole("DRIVER")
						.requestMatchers(HttpMethod.POST, "/api/v1/orders/*/assign")
						.hasAnyRole("ADMIN", "DISPATCHER")
						.requestMatchers(HttpMethod.POST, "/api/v1/orders/*/auto-assign")
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
				.addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${fleetbite.security.cors.allowed-origin}") String allowedOrigin) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(allowedOrigin));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		configuration.setExposedHeaders(List.of("Location"));
		configuration.setAllowCredentials(false);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}

	private AuthenticationEntryPoint authenticationEntryPoint(JsonMapper jsonMapper) {
		return (request, response, authException) -> writeError(
				jsonMapper,
				response,
				HttpServletResponse.SC_UNAUTHORIZED,
				"AUTHENTICATION_FAILED",
				"Authentication is required");
	}

	private AccessDeniedHandler accessDeniedHandler(JsonMapper jsonMapper) {
		return (request, response, accessDeniedException) -> writeError(
				jsonMapper,
				response,
				HttpServletResponse.SC_FORBIDDEN,
				"ACCESS_DENIED",
				"Access is denied");
	}

	private void writeError(
			JsonMapper jsonMapper,
			HttpServletResponse response,
			int status,
			String code,
			String message) throws java.io.IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		ApiResponse<Void> body = ApiResponse.failure(code, message);
		jsonMapper.writeValue(response.getOutputStream(), body);
	}
}
