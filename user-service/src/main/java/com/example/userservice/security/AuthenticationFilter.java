package com.example.userservice.security;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestLogin;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final UserService userService;
	private final Environment environment;

	public AuthenticationFilter(
		AuthenticationManager authenticationManager,
		UserService userService,
		Environment environment) {

		super(authenticationManager);
		this.userService = userService;
		this.environment = environment;
	}

	@Override
	public Authentication attemptAuthentication(
		HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

		try {
			RequestLogin creds = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);
			return getAuthenticationManager().authenticate(
				new UsernamePasswordAuthenticationToken(
					creds.getEmail(),
					creds.getPassword(),
					new ArrayList<>()));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
		Authentication auth) throws IOException, ServletException {

		String userName = ((User)auth.getPrincipal()).getUsername();
		UserDto userDetails = userService.getUserDetailsByEmail(userName);

		byte[] secretKeyBytes = environment.getProperty("token.secret").getBytes();
		SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);

		Instant now = Instant.now();

		String token = Jwts.builder()
			.setSubject(userDetails.getUserId())
			.setExpiration(Date.from(now.plusMillis(Long.parseLong(environment.getProperty("token.expiration_time")))))
			.setIssuedAt(Date.from(now))
			.signWith(secretKey)
			.compact();

		res.addHeader("token", token);
		res.addHeader("userId", userDetails.getUserId());
	}
}
