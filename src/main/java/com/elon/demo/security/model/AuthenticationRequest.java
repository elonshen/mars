package com.elon.demo.security.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticationRequest implements Serializable {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
