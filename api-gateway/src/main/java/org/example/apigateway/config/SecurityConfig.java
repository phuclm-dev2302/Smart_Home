package org.example.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityWebFilterChain(ServerHttpSecurity serverHttpSecurity){
        return serverHttpSecurity
                .csrf(csrfSpec -> csrfSpec.disable())
                .cors(withDefaults())  // Chỉ bật để Security biết có CORS, header do Gateway trả
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Cho phép preflight
                        .pathMatchers("/favicon.ico").permitAll()
                        .pathMatchers("/eureka/**", "/api/v1/auth/**","/uploads/images/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/posts/**","/api/v1/images/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/ai/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
                .build();
    }

}