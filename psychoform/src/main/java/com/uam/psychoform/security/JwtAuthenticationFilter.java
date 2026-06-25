package com.uam.psychoform.security;

import com.uam.psychoform.security.service.JwtService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.stream.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final SecurityErrorHandler errors;

    public JwtAuthenticationFilter(JwtService jwt, SecurityErrorHandler errors) {
        this.jwt = jwt;
        this.errors = errors;
    }

    protected void doFilterInternal(HttpServletRequest r, HttpServletResponse s, FilterChain c)
            throws ServletException, IOException {

        String h = r.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer "))
            try {
                var p = jwt.validar(h.substring(7));
                var a = p.permisos().stream().map(x -> new SimpleGrantedAuthority("PERM_" + x))
                        .collect(Collectors.toSet());
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(p, p, a));
            } catch (IllegalArgumentException e) {
                errors.unauthorized(s);
                return;
            }
        c.doFilter(r, s);
    }
}
