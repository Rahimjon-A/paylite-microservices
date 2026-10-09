package uz.paylite.cardbank.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class FeignSecurityConfig {

    @Bean
    public RequestInterceptor bearerTokenInterceptor() {

        return requestTemplate -> {

            var authentication =
                SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {

                String token =
                    jwtAuthentication.getToken().getTokenValue();

                requestTemplate.header(
                    "Authorization",
                    "Bearer " + token
                );
            }
        };
    }
}
