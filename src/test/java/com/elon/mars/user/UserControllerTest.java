package com.elon.mars.user;

import com.elon.mars.controller.dto.UserCreateRequest;
import com.elon.mars.domain.*;
import com.elon.mars.repository.UserRepository;
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
        Tenant tenant = Tenant.of("平台租户", null);
        Permission permission = Permission.of("用户管理", PermissionEnum.USER_MANAGE.name(), tenant);
        Role role = Role.of("admin", Set.of(permission), tenant);
        User user = User.ofNew("foo", "foo", "foo", Set.of(role));
        user.setId(1L);
        given(this.userRepository.findFirstByAuth_Username("foo")).willReturn(Optional.of(user));

        return this.mockMvc.perform(post("/authentication")
                        .with(httpBasic("foo", "foo")))
                .andReturn().getResponse().getContentAsString();
    }
}