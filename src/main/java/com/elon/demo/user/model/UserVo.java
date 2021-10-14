package com.elon.demo.user.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserVo {
    private Long id;
    private String name;
    private String username;
    private Set<Role> roles;
}
