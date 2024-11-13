package com.elon.mars.controller.controller;

import com.elon.mars.domain.*;
import com.elon.mars.repository.TenantRepository;
import com.elon.mars.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    UserRepository userRepository;
    @Autowired
    private TenantRepository tenantRepository;

    @BeforeEach
    void setUp() {
        Tenant tenant = Tenant.of("平台租户", null);
        tenant.setId(1L);

        Permission permission = Permission.of("foo", PermissionEnum.USER_MANAGE.name(), tenant);
        Role role = Role.of("admin", Set.of(permission), tenant);

        User user = User.ofNew("foo", "foo", "foo", Set.of(role), tenant);
        user.setId(1L);
        given(userRepository.findFirstByAuth_Username(any())).willReturn(Optional.of(user));
        given(userRepository.count()).willReturn(1L);
    }

    @Test
    void rootWhenAuthenticatedThenSaysHelloUser() throws Exception {
        // @formatter:off
        MvcResult result = this.mvc.perform(post("/authentication")
                        .with(httpBasic("foo", "foo")))
                .andExpect(status().isOk())
                .andReturn();

        String token = result.getResponse().getContentAsString();
        System.out.println(token);
        this.mvc.perform(get("/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(content().string("Hello, foo!"));
        // @formatter:on
    }

    @Test
    void rootWhenUnauthenticatedThen401() throws Exception {
        // @formatter:off
        this.mvc.perform(get("/"))
                .andExpect(status().isUnauthorized());
        // @formatter:on
    }

    @Test
    void tokenWhenBadCredentialsThen401() throws Exception {
        // @formatter:off
        this.mvc.perform(post("/authentication").with(httpBasic("foo", "foo123")))
                .andExpect(status().isUnauthorized());
        // @formatter:on
    }

    @Test
    void name() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        System.out.println(bCryptPasswordEncoder.encode("foo"));
    }
}
