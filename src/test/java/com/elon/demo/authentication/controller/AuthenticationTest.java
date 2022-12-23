package com.elon.demo.authentication.controller;

import com.elon.demo.user.UserRepository;
import com.elon.demo.user.model.Role;
import com.elon.demo.user.model.User;
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

@SuppressWarnings("SpringBootApplicationProperties")
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        given(userRepository.findByUsername(any())).willReturn(Optional.of(new User("foo", "$2a$10$1vMoRhQlmFBosVZQvta28OqXeOl1ybZU0W4L7tuOWpYAZQy4jzRR2", Set.of(Role.ADMIN))));
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
