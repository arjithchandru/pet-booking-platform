package com.petcare.booking.security;

import com.petcare.booking.domain.entity.User;
import com.petcare.booking.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/swagger-ui") || path.startsWith("/api-docs") || path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String oktaSubject = request.getHeader("X-Dev-Okta-Subject");

        if (oktaSubject == null || oktaSubject.isBlank()) {
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                oktaSubject = jwt.getSubject();
            } else {
                // Default fallback for dev testing if no header is supplied
                oktaSubject = "okta_happy_paws_admin";
            }
        }

        Optional<User> userOptional = userRepository.findByOktaSubject(oktaSubject);

        if (userOptional.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                {
                    "status": 401,
                    "error": "Unauthorized",
                    "message": "User identity not provisioned in platform"
                }
            """);
            return;
        }

        User user = userOptional.get();
        TenantContext.setCurrentTenant(user.getTenant());
        TenantContext.setCurrentUser(user);
        log.debug("Resolved Tenant: [{}] for User: [{}] ({})", user.getTenant().getName(), user.getEmail(), user.getRole());

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}