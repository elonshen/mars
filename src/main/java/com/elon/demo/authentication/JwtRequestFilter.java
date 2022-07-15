package com.elon.demo.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final MyUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper mapper;

    public JwtRequestFilter(MyUserDetailsService userDetailsService, JwtUtil jwtUtil, ObjectMapper mapper) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.mapper = mapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("message", "认证过期");
                response.setStatus(401);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                mapper.writeValue(response.getWriter(), errorDetails);
                return;
            } catch (Exception e) {
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("message", "认证失败");
                response.setStatus(401);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                mapper.writeValue(response.getWriter(), errorDetails);
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }

}
