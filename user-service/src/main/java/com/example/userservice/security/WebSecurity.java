package com.example.userservice.security;

import com.example.userservice.service.UserService;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class WebSecurity {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Environment env;

    public static final String ALLOWED_IP_ADDRESS = "127.0.0.1";
    public static final String SUBNET = "/32";
    public static final IpAddressMatcher ALLOWED_IP_ADDRESS_MATCHER = new IpAddressMatcher(ALLOWED_IP_ADDRESS + SUBNET);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configure AuthenticationManagerBuilder
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);

        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests((authz) -> authz
                                .requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/users", "POST")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/users", "GET")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/welcome")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/health_check")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/swagger-resources/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
//                        .requestMatchers("/**").access(this::hasIpAddress)
                                .requestMatchers("/**")
                                .access(new WebExpressionAuthorizationManager(
                                        "hasIpAddress('127.0.0.1') or hasIpAddress('211.227.146.72')"))// host pc ip address
                                .anyRequest().authenticated()
                )
                .authenticationManager(authenticationManager)
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilter(getAuthenticationFilter(authenticationManager));
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
        return http.build();
    }

    private AuthorizationDecision hasIpAddress(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        return new AuthorizationDecision(ALLOWED_IP_ADDRESS_MATCHER.matches(object.getRequest()));
    }

    private AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) throws Exception {
        return new AuthenticationFilter(authenticationManager, userService, env);
    }
}
