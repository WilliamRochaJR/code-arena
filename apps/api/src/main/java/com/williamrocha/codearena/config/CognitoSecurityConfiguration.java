package com.williamrocha.codearena.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Profile("!local & !test")
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CognitoSecurityProperties.class)
public class CognitoSecurityConfiguration {

	@Bean
	JwtDecoder jwtDecoder(CognitoSecurityProperties properties) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder
			.withIssuerLocation(properties.issuerUri())
			.build();
		OAuth2TokenValidator<Jwt> issuerValidator =
			JwtValidators.createDefaultWithIssuer(properties.issuerUri());
		OAuth2TokenValidator<Jwt> cognitoValidator =
			new CognitoAccessTokenValidator(properties.clientId());
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
			issuerValidator,
			cognitoValidator));
		return decoder;
	}
}
