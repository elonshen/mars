package com.elon.demo.user;

import com.elon.demo.user.model.Role;
import com.elon.demo.user.model.User;
import com.elon.demo.user.model.UserCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateUserByRepeatUserName() throws Exception {
        String token = mockAdminAndGetToken();
        UserCreateRequest userCreateRequest = new UserCreateRequest();
        userCreateRequest.setUsername("foo");
        userCreateRequest.setName("foo");
        userCreateRequest.setPassword("foo");
        System.out.println(token);
        assertThatThrownBy(() ->
                this.mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(userCreateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
        ).hasMessageContaining("用户名已存在");
    }

    private String mockAdminAndGetToken() throws Exception {
        User user = new User();
        user.setUsername("foo");
        user.setPassword("$2a$10$1vMoRhQlmFBosVZQvta28OqXeOl1ybZU0W4L7tuOWpYAZQy4jzRR2");
        user.setName("foo");
        user.setRoles(Set.of(Role.ADMIN));
        given(this.userRepository.findByUsername("foo")).willReturn(Optional.of(user));

        return this.mockMvc.perform(post("/authentication")
                        .with(httpBasic("foo", "foo")))
                .andReturn().getResponse().getContentAsString();
    }
}