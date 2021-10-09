package com.elon.demo.security.controller;

import com.elon.demo.security.model.AuthenticationRequest;
import com.elon.demo.security.service.MyUserDetailsService;
import com.elon.demo.security.util.JwtUtil;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class HelloWorldController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtTokenUtil;
    private final MyUserDetailsService userDetailsService;

    @GetMapping("/hello")
    public String firstPage() {
        return "Hello World";
    }

    @PostMapping(value = "/authenticate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(examples = @ExampleObject(value = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmb28iLCJleHAiOjE2MzM4MzcwMjcsImlhdCI6MTYzMzgwMTAyN30.D6WgrqwSy1l3-w84wvvsXYCGECpZdMLDhjw_SCl0VRk"))}),
            @ApiResponse(responseCode = "403", description = "账号密码不正确")
    })
    public ResponseEntity<String> createAuthenticationToken(@RequestBody @Valid AuthenticationRequest authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtTokenUtil.generateToken(userDetails);
        return ResponseEntity.ok(jwt);
    }
}
