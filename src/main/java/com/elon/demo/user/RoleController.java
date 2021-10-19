package com.elon.demo.user;

import com.elon.demo.user.model.Role;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/roles")
@Tag(name = "role")
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    public List<Role> findAll() {
        return roleService.findALl();
    }
}
