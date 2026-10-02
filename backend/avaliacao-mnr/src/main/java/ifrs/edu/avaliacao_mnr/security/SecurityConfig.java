package ifrs.edu.avaliacao_mnr.security;

import ifrs.edu.avaliacao_mnr.authorization.Permission;
import ifrs.edu.avaliacao_mnr.security.AuthenticatedUser;
import ifrs.edu.avaliacao_mnr.service.AuthAuditService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@Profile("!dev")
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthAuditService auditService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AuthAuditService auditService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.auditService = auditService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> {
                            auditService.record("UNAUTHENTICATED_ACCESS", null, null, false,
                                    request.getMethod() + " " + request.getRequestURI(), request);
                            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "unauthorized");
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                            if (authentication != null && authentication.isAuthenticated()
                                    && authentication.getPrincipal() instanceof AuthenticatedUser user) {
                                auditService.recordActor("ACCESS_DENIED", user.getId(), user.getEmail(), false,
                                        request.getMethod() + " " + request.getRequestURI(), request);
                            } else {
                                auditService.record("ACCESS_DENIED", null, null, false,
                                        request.getMethod() + " " + request.getRequestURI(), request);
                            }
                            writeError(response, HttpServletResponse.SC_FORBIDDEN, "forbidden");
                        }))
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml", "/webjars/**").permitAll()
                        .requestMatchers("/auth/login", "/auth/refresh", "/auth/logout", "/api/test").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/projects/import")
                            .hasAuthority(Permission.PROJECT_IMPORT.name())
                        .requestMatchers(HttpMethod.GET, "/api/projects/**")
                            .hasAuthority(Permission.PROJECT_READ.name())
                        .requestMatchers("/users", "/users/**").hasAuthority(Permission.USER_MANAGE.name())
                        .requestMatchers(HttpMethod.GET, "/api/events/**")
                            .hasAuthority(Permission.EVENT_READ.name())
                        .requestMatchers("/api/events/**").hasAuthority(Permission.EVENT_MANAGE.name())
                        .requestMatchers("/api/criteria/**").hasAuthority(Permission.CRITERIA_MANAGE.name())
                        .requestMatchers(HttpMethod.GET, "/api/evaluations/**")
                            .hasAuthority(Permission.EVALUATION_READ.name())
                        .requestMatchers("/api/evaluations/**")
                            .hasAuthority(Permission.EVALUATION_WRITE.name())
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void writeError(HttpServletResponse response, int status, String error) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + error + "\"}");
    }
}
