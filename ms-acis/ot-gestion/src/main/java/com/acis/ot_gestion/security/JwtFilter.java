package com.acis.ot_gestion.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final SecretKey key;
    public JwtFilter(@Value("${jwt.secret}") String secret) { this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String h=req.getHeader("Authorization");
        if(h!=null && h.startsWith("Bearer ")) try {
            var c=Jwts.parser().verifyWith(key).build().parseSignedClaims(h.substring(7)).getPayload();
            String role=c.get("rol",String.class), email=c.get("email",String.class);
            if(role!=null && email!=null) SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email,null,List.of(new SimpleGrantedAuthority("ROLE_"+role))));
        } catch(Exception ignored) { SecurityContextHolder.clearContext(); }
        chain.doFilter(req,res);
    }
}
