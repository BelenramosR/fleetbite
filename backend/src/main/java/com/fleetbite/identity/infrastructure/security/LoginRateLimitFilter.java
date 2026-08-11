package com.fleetbite.identity.infrastructure.security;

import com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;

public final class LoginRateLimitFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(LoginRateLimitFilter.class);
	private static final String LOGIN_PATH = "/api/v1/auth/login";

	private final ConcurrentHashMap<String, Window> attempts = new ConcurrentHashMap<>();
	private final JsonMapper jsonMapper;
	private final Clock clock;
	private final int maxAttempts;
	private final long windowMillis;

	public LoginRateLimitFilter(JsonMapper jsonMapper, Clock clock, int maxAttempts, long windowSeconds) {
		if (maxAttempts < 1 || windowSeconds < 1) {
			throw new IllegalArgumentException("Login rate limit values must be positive");
		}
		this.jsonMapper = jsonMapper;
		this.clock = clock;
		this.maxAttempts = maxAttempts;
		this.windowMillis = windowSeconds * 1000L;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !"POST".equals(request.getMethod()) || !LOGIN_PATH.equals(request.getRequestURI());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String client = request.getRemoteAddr();
		long now = clock.millis();
		Window window = attempts.compute(client, (key, current) ->
				current == null || now - current.startedAtMillis() >= windowMillis
						? new Window(now, 1)
						: new Window(current.startedAtMillis(), current.count() + 1));

		if (window.count() > maxAttempts) {
			log.warn("Login rate limit exceeded for client {}", client);
			response.setStatus(429);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setHeader("Retry-After", Long.toString(Math.max(1, windowMillis / 1000L)));
			jsonMapper.writeValue(response.getOutputStream(),
					ApiResponse.failure("LOGIN_RATE_LIMIT_EXCEEDED", "Too many login attempts. Try again later"));
			return;
		}

		filterChain.doFilter(request, response);
	}

	private record Window(long startedAtMillis, int count) {
	}
}
