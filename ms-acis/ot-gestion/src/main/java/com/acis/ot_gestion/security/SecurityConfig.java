package com.acis.ot_gestion.security;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
 @Bean SecurityFilterChain chain(HttpSecurity http, JwtFilter jwt) throws Exception {
  return http.csrf(c->c.disable()).cors(c->{}).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a.requestMatchers("/h2-console/**").permitAll().anyRequest().authenticated())
   .headers(h->h.frameOptions(f->f.sameOrigin())).addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();
 }
}
