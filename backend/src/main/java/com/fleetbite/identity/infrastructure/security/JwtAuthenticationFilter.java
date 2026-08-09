package com.fleetbite.identity.infrastructure.security;

import com.fleetbite.identity.application.dto.AuthenticatedPrincipal;
import com.fleetbite.identity.application.port.out.TokenProviderPort;
import com.fleetbite.identity.domain.exception.AuthenticationFailedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final TokenProviderPort tokenProviderPort;

	public JwtAuthenticationFilter(TokenProviderPort tokenProviderPort) {
		this.tokenProviderPort = Objects.requireNonNull(tokenProviderPort);
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
			String token = authorization.substring(BEARER_PREFIX.length()).trim();
			if (!token.isEmpty()) {
				try {
					AuthenticatedPrincipal principal = tokenProviderPort.parse(token);
					var authentication = new UsernamePasswordAuthenticationToken(
							principal,
							null,
							List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name())));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
				catch (AuthenticationFailedException ignored) {
					SecurityContextHolder.clearContext();
				}
			}
		}
		filterChain.doFilter(request, response);
	}
}
