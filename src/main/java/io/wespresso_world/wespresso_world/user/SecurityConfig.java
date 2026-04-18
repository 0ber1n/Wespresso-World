package io.wespresso_world.wespresso_world.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())) // Allow H2 console in iframes
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // Allow H2 console access
                .requestMatchers("/swagger-ui/**", "/api-docs/**").hasRole("ADMIN") // Allow Swagger UI access
                .requestMatchers("/actuator/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/menu/**").hasRole("admin")
                .requestMatchers(HttpMethod.POST, "/menu/**").hasRole("admin")
                .requestMatchers(HttpMethod.PATCH, "/menu/**").hasRole("admin")
                .requestMatchers(HttpMethod.PUT, "/menu/**").hasRole("admin")
                .requestMatchers(HttpMethod.DELETE, "/beans/**").hasRole("admin")
                .requestMatchers(HttpMethod.POST, "/beans/**").hasRole("admin")
                .requestMatchers(HttpMethod.PATCH, "/beans/**").hasRole("admin")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);


            return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
}
