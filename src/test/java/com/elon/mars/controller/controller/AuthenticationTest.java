package com.elon.mars.controller.controller;

import com.elon.mars.domain.*;
import com.elon.mars.repository.AuthRepository;
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

import java.util.HashSet;
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
    @MockBean
    private TenantRepository tenantRepository;

    @BeforeEach
    void setUp() {
        Tenant tenant = Tenant.of("平台租户", null, TenantType.PLATFORM);
        tenant.setId(1L);

        Permission permission = Permission.of("foo", PermissionEnum.USER_MANAGE.name());
        permission.setTenantId(tenant.getId());
        Role role = Role.of("admin", Set.of(permission));
        role.setId(1L);
        role.setTenantId(tenant.getId());

        User user = User.ofNew("foo", "foo", "Foo@Foo123", Set.of(role), new HashSet<>());
        user.setId(1L);
        user.setTenantId(tenant.getId());

        given(userRepository.findFirstByAuth_Username(any())).willReturn(Optional.of(user));
        given(userRepository.count()).willReturn(1L);
        given(tenantRepository.findById(any())).willReturn(Optional.of(tenant));
    }

    @Test
    void rootWhenAuthenticatedThenSaysHelloUser() throws Exception {
        MvcResult result = this.mvc.perform(post("/authentication")
                        .with(httpBasic("foo", "Foo@Foo123")))
                .andExpect(status().isOk())
                .andReturn();

        String token = result.getResponse().getContentAsString();
        System.out.println(token);
        this.mvc.perform(get("/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(content().string("Hello, foo!"));
    }

    @Test
    void rootWhenUnauthenticatedThen401() throws Exception {
        this.mvc.perform(get("/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWhenBadCredentialsThen401() throws Exception {
        this.mvc.perform(post("/authentication").with(httpBasic("foo", "Foo@Foo1231")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void name() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        System.out.println(bCryptPasswordEncoder.encode("foo"));
    }
}
