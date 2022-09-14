package com.elon.demo.config;

import com.elon.demo.authentication.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
public class WebSecurityConfig {

    private final UserDetailsService myUserDetailsService;
    private final JwtRequestFilter jwtRequestFilter;

    public WebSecurityConfig(UserDetailsService myUserDetailsService, JwtRequestFilter jwtRequestFilter) {
        this.myUserDetailsService = myUserDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * A custom authenticationManager From <a href="https://stackoverflow.com/questions/71281032/spring-security-exposing-authenticationmanager-without-websecurityconfigureradap">...</a>
     */
    @Bean
    public AuthenticationManager myAuthenticationManagerBean() {
        return authentication -> {
            final UserDetails userDetail;
            try {
                userDetail = myUserDetailsService.loadUserByUsername(authentication.getName());
            } catch (UsernameNotFoundException e) {
                throw new BadCredentialsException(e.getMessage());
            }

            if (!passwordEncoder().matches(authentication.getCredentials().toString(), userDetail.getPassword())) {
                throw new BadCredentialsException("wrong password");
            }
            return new UsernamePasswordAuthenticationToken(userDetail.getUsername(), userDetail.getPassword(), userDetail.getAuthorities());
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable).cors().and()
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAt(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationManager(authentication -> authentication)
                .authorizeHttpRequests((authorize) -> authorize.antMatchers("/authentication",
                                "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**",
                                "/**/*.html", "/**/*.js", "/**/*.css", "/**/*.ico", "/**/*.woff", "/**/*.ttf").permitAll()
                        .antMatchers("/users").hasRole("ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/users/*").hasRole("ADMIN")
                        .anyRequest().authenticated());

        return httpSecurity.build();
    }
}
