package com.insurai.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig {

    private final EmployeeJwtAuthenticationFilter employeeFilter;
    private final AgentJwtAuthenticationFilter agentFilter;
    private final HrJwtAuthenticationFilter hrFilter;
    // ⚠️ Admin filter will be added later (optional)

    public SecurityConfig(
            EmployeeJwtAuthenticationFilter employeeFilter,
            AgentJwtAuthenticationFilter agentFilter,
            HrJwtAuthenticationFilter hrFilter) {
        this.employeeFilter = employeeFilter;
        this.agentFilter = agentFilter;
        this.hrFilter = hrFilter;
    }

    // ======================= SECURITY FILTER CHAIN =======================
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF for APIs
                .csrf(csrf -> csrf.disable())

                // Enable CORS (configured below)
                .cors(cors -> {})

                .authorizeHttpRequests(auth -> auth

                        // ================= PUBLIC ENDPOINTS =================
                        .requestMatchers(
                                "/hello",
                                "/auth/**",
                                "/admin/login",
                                "/employee/login",
                                "/employee/register",
                                "/agent/login",
                                "/hr/login"
                        ).permitAll()

                        // ================= ROLE-BASED ACCESS =================
                        .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                        .requestMatchers("/agent/**").hasRole("AGENT")
                        .requestMatchers("/hr/**").hasRole("HR")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // ================= EVERYTHING ELSE =================
                        .anyRequest().authenticated()
                )

                // Disable default auth mechanisms
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable());

        // ================= JWT FILTERS (ORDER MATTERS) =================
        http.addFilterBefore(employeeFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(agentFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(hrFilter, UsernamePasswordAuthenticationFilter.class);
        // AdminJwtAuthenticationFilter can be added here later

        return http.build();
    }

    // ======================= AUTH MANAGER =======================
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ======================= CORS CONFIG =======================
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
