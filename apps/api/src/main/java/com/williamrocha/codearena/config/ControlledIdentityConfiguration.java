package com.williamrocha.codearena.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.williamrocha.codearena.identity.CurrentUserIdentity;
import com.williamrocha.codearena.identity.CurrentUserProvider;

@Profile({"local", "test"})
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ControlledIdentityProperties.class)
public class ControlledIdentityConfiguration {

	@Bean
	CurrentUserProvider controlledCurrentUserProvider(
		ControlledIdentityProperties properties
	) {
		CurrentUserIdentity identity = new CurrentUserIdentity(
			properties.subject(),
			properties.email(),
			properties.displayName());

		return () -> identity;
	}
}
