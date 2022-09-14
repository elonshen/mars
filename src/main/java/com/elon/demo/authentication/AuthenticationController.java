package com.elon.demo.authentication;

import com.elon.demo.authentication.model.AuthenticationRequest;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/authentication")
@Tag(name = "认证")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtTokenUtil;
    private final MyUserDetailsService userDetailsService;

    public AuthenticationController(AuthenticationManager authenticationManager, JwtUtil jwtTokenUtil, MyUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }
    /**
     * 用户登入
     * @return token
     */
    @SuppressWarnings("SpellCheckingInspection")
    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(examples = @ExampleObject(value = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTYzNTc3NjQ2NywiaWF0IjoxNjM0NDgwNDY3fQ.FG1jp0mQodtslGfSHShrgo2DOkKQcj_pCvLRe5Q5t3w"))}),
    })
    public String createAuthenticationToken(@RequestBody @Valid AuthenticationRequest authenticationRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new RuntimeException("账号或密码不正确");
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        return jwtTokenUtil.generateToken(userDetails);
    }
}
