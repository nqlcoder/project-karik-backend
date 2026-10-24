package fashionstorebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Register / Login
                        .requestMatchers("/api/auth/**")
                        .permitAll()

                        // Tạm thời public GET products
                        .requestMatchers("/api/products/**")
                        .permitAll()

                        // Tạm thời public GET categories
                        .requestMatchers("/api/categories/**")
                        .permitAll()

                        // Tạm thời public GET coupons
                        .requestMatchers("/api/coupons/**")
                        .permitAll()

                        // Tạm thời cho toàn bộ API
                        .requestMatchers("/api/**")
                        .permitAll()

                        .anyRequest()
                        .authenticated()
                );

        return http.build();
    }
}