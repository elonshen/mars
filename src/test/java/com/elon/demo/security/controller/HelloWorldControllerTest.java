package com.elon.demo.security.controller;

import com.elon.demo.security.model.AuthenticationRequest;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HelloWorldControllerTest {

    Gson gson = new Gson();
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAuthenticationToken() throws Exception {
        String body = gson.toJson(new AuthenticationRequest("foo", "foo"));
        this.mockMvc.perform(MockMvcRequestBuilders.post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andDo(print()).andExpect(status().isOk());
    }
}