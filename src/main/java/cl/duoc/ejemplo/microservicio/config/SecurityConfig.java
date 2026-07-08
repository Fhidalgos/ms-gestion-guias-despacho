package cl.duoc.ejemplo.microservicio.config;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    /**
     * Valida la firma del JWT mediante las claves públicas de Azure AD B2C
     * y comprueba que el token provenga del emisor configurado.
     */
    @Bean
    public JwtDecoder jwtDecoder() {

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .build();

        jwtDecoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(issuerUri)
        );

        return jwtDecoder;
    }

    /**
     * Convierte el custom claim de Azure AD B2C en un rol de Spring.
     *
     * consulta -> ROLE_CONSULTA
     * gestion  -> ROLE_GESTION
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            String rol = jwt.getClaimAsString(
                    "extension_consultaRole"
            );

            if (rol == null || rol.isBlank()) {
                return Collections.<GrantedAuthority>emptyList();
            }

            String autoridad = "ROLE_"
                    + rol.trim().toUpperCase(Locale.ROOT);

            List<GrantedAuthority> autoridades = List.of(
                    new SimpleGrantedAuthority(autoridad)
            );

            return autoridades;
        });

        return converter;
    }

    /**
     * Define qué rol puede acceder a cada endpoint.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {

        http
                // Se desactiva CSRF porque se trabaja con una API REST
                // protegida mediante tokens JWT.
                .csrf(csrf -> csrf.disable())

                // La aplicación no guardará sesiones en el servidor.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Endpoint exclusivo del rol consulta.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/guias/*/descargar"
                        ).hasRole("CONSULTA")

                        // Crear una guía.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/guias"
                        ).hasRole("GESTION")

                        // Subir una guía a S3.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/guias/*/s3"
                        ).hasRole("GESTION")

                        // Actualizar una guía.
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/guias/*"
                        ).hasRole("GESTION")

                        // Eliminar una guía.
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/guias/*"
                        ).hasRole("GESTION")

                        // Consultar guías por fecha y transportista.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/guias"
                        ).hasRole("GESTION")

                        // Enviar mensajes hacia RabbitMQ.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/mensajes"
                        ).hasRole("GESTION")

                        // Permite mostrar correctamente los errores de Spring.
                        .requestMatchers(
                                "/error"
                        ).permitAll()

                        // Cualquier otra ruta necesita un usuario autenticado.
                        .anyRequest().authenticated()
                )

                // Configuración del backend como Resource Server.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }
}

