package com.saraf.security.config;

import com.saraf.security.oauth2.CustomOAuth2SuccessHandler;
import com.saraf.security.oauth2.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import static com.saraf.security.user.Permission.ADMIN_CREATE;
import static com.saraf.security.user.Permission.ADMIN_DELETE;
import static com.saraf.security.user.Permission.ADMIN_READ;
import static com.saraf.security.user.Permission.ADMIN_UPDATE;
import static com.saraf.security.user.Permission.MANAGER_CREATE;
import static com.saraf.security.user.Permission.MANAGER_DELETE;
import static com.saraf.security.user.Permission.MANAGER_READ;
import static com.saraf.security.user.Permission.MANAGER_UPDATE;
import static com.saraf.security.user.Role.*;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final Environment env;
    private static final String[] WHITE_LIST_URL = {"/api/v1/auth/**",
            "/api/v1/rate/**",
            "/api/v1/user/**",
            "/",
            "/login**",
            "/oauth2/**"
    };
    private static final String[] USER_LIST_URL = {
            "/api/v1/recipient/**",
            "/api/v1/transfer/**"
            };

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        .requestMatchers(WHITE_LIST_URL)
                        .permitAll()
                        .requestMatchers(USER_LIST_URL).hasAnyRole(ADMIN.name(), MANAGER.name(), USER.name())
                        .requestMatchers("/api/v1/management/**").hasAnyRole(ADMIN.name(), MANAGER.name())
                        .requestMatchers(GET, "/api/v1/management/**").hasAnyAuthority(ADMIN_READ.name(), MANAGER_READ.name())
                        .requestMatchers(GET, "/api/v1/rate/**").hasAnyAuthority(ADMIN_READ.name(), MANAGER_READ.name(), USER.name())
                        .requestMatchers(POST, "/api/v1/rate/**").hasAnyAuthority(ADMIN_CREATE.name(), MANAGER_CREATE.name())
                        .requestMatchers(PUT, "/api/v1/management/**").hasAnyAuthority(ADMIN_UPDATE.name(), MANAGER_UPDATE.name())
                        .requestMatchers(DELETE, "/api/v1/management/**").hasAnyAuthority(ADMIN_DELETE.name(), MANAGER_DELETE.name())
                        .anyRequest()
                        .authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("http://127.0.0.1:5501/Saraf-BRK/pages/sign-in.html")
                        .defaultSuccessUrl("http://127.0.0.1:5501/Saraf-BRK/pages/transfer-details.html")
                        .userInfoEndpoint(userInfo -> userInfo
                            .userService(customOAuth2UserService))
                        .successHandler(customOAuth2SuccessHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                                .logoutUrl("/api/v1/auth/logout")
                                .addLogoutHandler(logoutHandler)
                                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext()))
                .exceptionHandling(exception -> exception
                        // Redirect to custom 404 page when resource not found
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            response.sendRedirect("http://127.0.0.1:5501/Saraf-BRK/pages/404.html"); // Assuming the 404 page is in the static folder
                        }));

        // Conditionally require HTTPS based on the active profile
        if (isProdProfileActive()) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        return http.build();
    }

    private boolean isProdProfileActive() {
        boolean isTestProfileActive = false;
        for (String profile : env.getActiveProfiles()) {
            if ("prod".equals(profile)) {
                isTestProfileActive = true;
                break;
            }
        }
        return isTestProfileActive;
    }

    @Bean
    public RoleHierarchyImpl roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        String hierarchy = "ROLE_ADMIN > ROLE_MANAGER \n ROLE_MANAGER > ROLE_USER";
        roleHierarchy.setHierarchy(hierarchy);
        return roleHierarchy;
    }

    @Bean
    public GrantedAuthoritiesMapper authoritiesMapper(RoleHierarchyImpl roleHierarchy) {
        return new RoleHierarchyAuthoritiesMapper(roleHierarchy);
    }
}
