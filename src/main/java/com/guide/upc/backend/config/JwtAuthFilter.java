package com.guide.upc.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserAuthenticationProvider userAuthenticationProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String header = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
    
        System.out.println("JwtAuthFilter: Processing request for " + httpServletRequest.getRequestURI());
        System.out.println("Authorization header: " + header);
    
        if (header != null) {
            String[] authElements = header.split(" ");
    
            if (authElements.length == 2 && "Bearer".equals(authElements[0])) {
                try {
                    System.out.println("JwtAuthFilter: Attempting to validate token");
                    SecurityContextHolder.getContext().setAuthentication(
                            userAuthenticationProvider.validateToken(authElements[1]));
                    System.out.println("JwtAuthFilter: Token validated successfully");
                } catch (RuntimeException e) {
                    System.out.println("JwtAuthFilter: Token validation failed - " + e.getMessage());
                    SecurityContextHolder.clearContext();
                    httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return; // Importante: detener el procesamiento aquí
                }
            }
        }
    
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
