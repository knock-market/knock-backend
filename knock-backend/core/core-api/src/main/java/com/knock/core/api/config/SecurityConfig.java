package com.knock.core.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository securityContextRepository)
			throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.securityContext(securityContext -> {
				securityContext.securityContextRepository(securityContextRepository);
				securityContext.requireExplicitSave(true);
			})
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.GET, "/api/v1/members/\\d+/items"))
				.permitAll()
				.requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.GET,
						"/api/v1/items/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"))
				.permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/seller-shares/my")
				.authenticated()
				.requestMatchers(
						RegexRequestMatcher.regexMatcher(HttpMethod.GET, "/api/v1/seller-shares/[A-Za-z0-9_-]+"))
				.permitAll()
				.requestMatchers("/api/v1/auth/**", "/api/v1/members", "/health", "/actuator/health", "/favicon.ico",
						"/.well-known/**")
				.permitAll()
				.anyRequest()
				.authenticated())
			.sessionManagement(session -> session.maximumSessions(1).maxSessionsPreventsLogin(false));

		return http.build();
	}

}
