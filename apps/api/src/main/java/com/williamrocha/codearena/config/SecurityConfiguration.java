package com.williamrocha.codearena.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

	@Bean
	@Order(1)
	@Profile({"local", "test"})
	SecurityFilterChain controlledApiSecurityFilterChain(HttpSecurity http)
		throws Exception {
		return http
			.securityMatcher("/api/v1/**")
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.anyRequest()
				.permitAll())
			.build();
	}

	@Bean
	@Order(1)
	@Profile("!local & !test")
	SecurityFilterChain authenticatedApiSecurityFilterChain(HttpSecurity http)
		throws Exception {
		return http
			.securityMatcher("/api/v1/**")
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.anyRequest()
				.authenticated())
			.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
			.build();
	}

	@Bean
	@Order(2)
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(
					"/actuator/health",
					"/actuator/health/**",
					"/actuator/prometheus"
				)
				.permitAll()
				.anyRequest()
				.denyAll())
			.build();
	}
}
