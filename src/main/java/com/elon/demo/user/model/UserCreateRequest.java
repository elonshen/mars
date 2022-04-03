package com.elon.demo.user.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class UserCreateRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String role;
}
