package com.elon.demo.security.controller;

import com.elon.demo.security.model.AuthenticationRequest;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HelloWorldControllerTest {

    @Autowired
    private MockMvc mockMvc;

    String token;
    Gson gson = new Gson();

    @Test
    void createAuthenticationToken() throws Exception {
        String body = gson.toJson(new AuthenticationRequest("foo", "foo"));
        this.mockMvc.perform(MockMvcRequestBuilders.post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @BeforeEach
    void setUp() throws Exception {
        String body = gson.toJson(new AuthenticationRequest("foo", "foo"));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        token = mvcResult.getResponse().getContentAsString();
    }

    @Test
    void firstPage() throws Exception {
        this.mockMvc.perform(get("/hello")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void firstPageWithOutToken() throws Exception {
        this.mockMvc.perform(get("/hello"))
                .andExpect(status().isForbidden());
    }
}