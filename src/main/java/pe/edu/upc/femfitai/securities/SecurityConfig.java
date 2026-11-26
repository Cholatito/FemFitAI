package pe.edu.upc.femfitai.securities;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Frontend Angular local
        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "https://femfitai-web.netlify.app"
        ));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS",
                "PATCH"
        ));

        // Headers permitidos
        configuration.setAllowedHeaders(List.of("*"));

        // Headers que Angular puede leer de la respuesta
        configuration.setExposedHeaders(List.of("Authorization"));

        // Utilizamos JWT, no autenticación mediante cookies
        configuration.setAllowCredentials(false);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())

                .csrf(csrf -> csrf.disable())

                .authenticationProvider(authenticationProvider())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Login público
                        .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                                // ADMIN: administra las cuentas de todas las usuarias.
                                .requestMatchers(HttpMethod.GET, "/usuarios").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/usuarios/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/usuarios/**").hasRole("ADMIN")

// ADMIN: único rol que modifica el catálogo de ejercicios
// (la consulta del catálogo queda abierta a cualquier cuenta autenticada).
                                .requestMatchers(HttpMethod.POST, "/ejercicios").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/ejercicios/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/ejercicios/**").hasRole("ADMIN")
// Sincronización del catálogo desde wger.
                                .requestMatchers(HttpMethod.POST, "/ejercicios/wger/sincronizar").hasRole("ADMIN")

// USUARIA: datos personales y funciones de la app.
// ADMIN no accede a los ciclos, rutinas, sesiones, progreso, etc.
                                .requestMatchers(
                                        "/ejercicios/usuario/**", "/ciclos/**", "/detalle-diario/**",
                                        "/rutinas/**", "/rutina-ejercicios/**", "/sesiones/**",
                                        "/detalle-sesion/**", "/detalle-serie/**", "/progreso/**",
                                        "/perfiles/**", "/recomendaciones/**", "/ia/**")
                                .hasRole("USUARIA")
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        new CustomJwtAuthenticationConverter()
                                )
                        )
                );

        return http.build();
    }
}
