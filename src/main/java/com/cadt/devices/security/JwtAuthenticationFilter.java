package com.cadt.devices.security;

import com.cadt.devices.repo.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UserRepository users;

    public JwtAuthenticationFilter(JwtService jwt, UserRepository users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String h = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (h != null && h.startsWith("Bearer ")) {
            try {
                System.out.println("Parsing JWT token: " + h.substring(7, Math.min(h.length(), 50)) + "...");
                var jws = jwt.parse(h.substring(7));
                String userId = jws.getPayload().getSubject();
                String role = (String) jws.getPayload().get("role");
                System.out.println("JWT parsed successfully - userId: " + userId + ", role: " + role);
                
                if (userId == null) {
                    System.out.println("ERROR: JWT subject (userId) is null!");
                    return;
                }
                
                users.findById(userId).ifPresent(u -> {
                    System.out.println("Found user in database: " + u.getId() + ", setting authentication");
                    SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(u.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
                });
            } catch (Exception e) {
                System.out.println("JWT parsing failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
        chain.doFilter(req, res);
    }
}
