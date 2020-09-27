package com.ftcksu.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftcksu.app.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityService securityService;
    private final ObjectMapper mapper;

    @Autowired
    public WebSecurityConfiguration(UserDetailsService userDetailsService,
                                    JWTAuthenticationFilter jwtAuthenticationFilter,
                                    SecurityService securityService,
                                    ObjectMapper mapper) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityService = securityService;
        this.mapper = mapper;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (httpServletRequest, httpServletResponse, e) -> {
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            sendPayload(status, e.getMessage(), httpServletResponse);
        };
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (httpServletRequest, httpServletResponse, e) -> {
            HttpStatus status = HttpStatus.FORBIDDEN;
            sendPayload(status, e.getMessage(), httpServletResponse);
        };
    }

    private void sendPayload(HttpStatus status, String message, HttpServletResponse httpServletResponse)
            throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", new Timestamp(new Date().getTime()));
        payload.put("status", status.value());
        payload.put("error", status.name());
        payload.put("message", message);

        httpServletResponse.setContentType("application/json; charset=UTF-8");
        httpServletResponse.setStatus(status.value());
        httpServletResponse.getWriter().write(mapper.writeValueAsString(payload));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()

                // Swagger Endpoints:
                .mvcMatchers(HttpMethod.GET, "/v2/api-docs", "/swagger-resources/**", "/swagger-ui.html**",
                        "/swagger-ui/**", "/swagger-ui**", "/webjars/**", "favicon.ico")
                .permitAll()

                // Public Endpoints:
                .mvcMatchers(HttpMethod.GET, "/images/{id}", "/images/{id}/thumb", "/users/{id}/image",
                        "/users/{id}/thumb")
                .permitAll()
                .mvcMatchers(HttpMethod.POST, "/login")
                .permitAll()

                // Admin Endpoints:
                .mvcMatchers(HttpMethod.GET, "/users/uotm", "/jobs", "/tasks", "/images", "/images/pending")
                .hasAnyRole("ADMIN", "MAINTAIN")
                .mvcMatchers(HttpMethod.POST, "/users", "/users/{id}/jobs/admin-submit", "/jobs",
                        "/users/{id}/notify", "/notifications/**")
                .hasAnyRole("ADMIN", "MAINTAIN")
                .mvcMatchers(HttpMethod.PUT, "/images/pending/{id}", "/users/{id}/admin-update", "/tasks/{id}/admin-update")
                .hasAnyRole("ADMIN", "MAINTAIN")
                .mvcMatchers(HttpMethod.DELETE, "/users/{id}", "/events/{id}", "/jobs/{id}", "/tasks/{id}",
                        "/images/{id}", "/motd/{id}")
                .hasAnyRole("ADMIN", "MAINTAIN")

                // User Related Protected Endpoints:
                .mvcMatchers(HttpMethod.GET, "/users/{id}", "/users/{id}/image-history", "/users/{id}/jobs")
                .access("hasAnyRole('ADMIN', 'MAINTAIN') or @securityService.isLoggedUser(#id)")
                .mvcMatchers(HttpMethod.POST, "/users/{id}/events", "/images/{id}")
                .access("hasAnyRole('ADMIN', 'MAINTAIN') or @securityService.isLoggedUser(#id)")
                .mvcMatchers(HttpMethod.PUT, "/users/{id}/**")
                .access("hasAnyRole('ADMIN', 'MAINTAIN') or @securityService.isLoggedUser(#id)")

                // Event Related Protected Endpoints:
                .mvcMatchers(HttpMethod.GET, "/events/{id}/jobs")
                .access("hasAnyRole('ADMIN', 'MAINTAIN') or @securityService.isEventLeader(#id)")
                .mvcMatchers(HttpMethod.POST, "/events/{id}/**")
                .access("hasAnyRole('ADMIN', 'MAINTAIN') or @securityService.isEventLeader(#id)")
                .mvcMatchers(HttpMethod.PUT, "/events/{id}/**")
                .access("hasAnyRole('ADMIN', 'MAINTAIN') or @securityService.isEventLeader(#id)")
                .mvcMatchers(HttpMethod.DELETE, "/events/{id}/users")
                .access("hasAnyRole('ADMIN', 'MAINTAIN') or @securityService.isEventLeader(#id)")

                // Job Related Protected Endpoints:
                .mvcMatchers(HttpMethod.GET, "/jobs/{id}/**")
                .access("hasAnyRole('ADMIN', 'MAINTAIN') or @securityService.isJobOwner(#id) or @securityService.isEventLeaderSeeingTasks(#id)")
                .mvcMatchers(HttpMethod.POST, "/jobs/{id}/**")
                .access("hasAnyRole('ADMIN', 'MAINTAIN') or @securityService.isJobOwner(#id)")

                // Task Related Protected Endpoints:
                .mvcMatchers(HttpMethod.GET, "/tasks/{id}")
                .access("hasAnyRole('ADMIN', 'MAINTAIN') or @securityService.isTaskOwner(#id)")
                .mvcMatchers(HttpMethod.PUT, "/tasks/{id}")
                .access("hasAnyRole('ADMIN', 'MAINTAIN') or @securityService.isTaskOwner(#id)")

                .anyRequest().authenticated()
                .and().exceptionHandling().authenticationEntryPoint(customAuthenticationEntryPoint())
                .and().exceptionHandling().accessDeniedHandler(customAccessDeniedHandler())
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public AuthenticationManager getAuthenticationManager() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
