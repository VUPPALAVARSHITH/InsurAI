package com.insurai.backend.config;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HrJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(HrJwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    public HrJwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Only authenticate if not already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() == null &&
                authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7).trim();

            try {
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                // ✅ ONLY HR TOKENS SHOULD PASS HERE
                if (email != null &&
                        "HR".equalsIgnoreCase(role) &&
                        jwtUtil.validateToken(token)) {

                    SimpleGrantedAuthority authority =
                            new SimpleGrantedAuthority("ROLE_HR");

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    null,
                                    Collections.singletonList(authority)
                            );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authenticated HR user: {}", email);
                }

            } catch (Exception e) {
                logger.warn("HR JWT authentication failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
