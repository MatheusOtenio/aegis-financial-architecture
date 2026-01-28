package com.aegis.backend.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ServiceAuthFilter extends OncePerRequestFilter {

    @Value("${app.service-token}")
    private String expectedToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();

        // Se for endpoint interno, aplica validação rígida
        if (path.startsWith("/internal/")) {
            String requestToken = request.getHeader("X-SERVICE-TOKEN");
            
            if (requestToken == null || !requestToken.equals(expectedToken)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access Denied: Invalid or missing X-SERVICE-TOKEN");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
