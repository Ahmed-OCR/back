package com.openclassrooms.rentals.parser;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class JwtParserConfig {

	@Value("${security.token.jwtKey}")
	private String jwtKey;

	@Bean
	public JwtParser jwtParser() {
		byte[] signingKey = jwtKey.getBytes();
		SecretKey secretKey = new SecretKeySpec(signingKey, "HmacSHA256");
		return Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build();
	}
}
