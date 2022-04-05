package com.elon.demo.authentication.controller;

import com.elon.demo.authentication.model.AuthenticationRequest;
import com.elon.demo.user.UserRepository;
import com.elon.demo.user.model.User;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    String token;
    final Gson gson = new Gson();

    @BeforeEach
    void setUp() throws Exception {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("$2a$10$9G..MOnHMx6/RSU52p1t6.jCtGBE/MOfZf5PaR7mzZGBys4vyg2j6");
        user.setName("foo");
        user.setRole("admin");
        given(this.userRepository.findByUsername("admin")).willReturn(Optional.of(user));

        String body = gson.toJson(new AuthenticationRequest("admin", "123456"));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        token = mvcResult.getResponse().getContentAsString();
    }

    @Test
    void testNormalLogin() throws Exception {
        String body = gson.toJson(new AuthenticationRequest("admin", "123456"));
        this.mockMvc.perform(MockMvcRequestBuilders.post("/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testAccessResourceWithToken() throws Exception {
        this.mockMvc.perform(get("/users/current")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testAccessResourceWithOutToken() throws Exception {
        this.mockMvc.perform(get("/users/current"))
                .andExpect(status().isForbidden());
    }
}