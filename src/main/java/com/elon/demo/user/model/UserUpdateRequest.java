package com.elon.demo.user.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class UserUpdateRequest {
    @NotNull
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String username;
    private String password;
    private String role;
}
