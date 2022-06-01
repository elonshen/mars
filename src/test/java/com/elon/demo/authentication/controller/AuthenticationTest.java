package com.elon.demo.authentication.controller;

import com.elon.demo.authentication.model.AuthenticationRequest;
import com.elon.demo.user.UserRepository;
import com.elon.demo.user.model.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationTest {
    @Autowired
    TestRestTemplate restTemplate;
    @MockBean
    private UserRepository userRepository;

    private String mockAdminAndGetToken() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("$2a$10$65uyWEzR6GE9L7vz2O466.IXQA3C9WNNxMpunQhdUlknkgF8Jtxq2");
        user.setName("foo");
        user.setRole("admin");
        given(this.userRepository.findByUsername("admin")).willReturn(Optional.of(user));

        HttpEntity<AuthenticationRequest> request = new HttpEntity<>(new AuthenticationRequest("admin", "admin"));
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("/authentication", request, String.class);
        return responseEntity.getBody();
    }

    private String mockCommonUserAndGetToken() {
        User user = new User();
        user.setUsername("user");
        user.setPassword("$2a$10$ktiha.elFVEC43oGdfr5vOVCbrUfRcYra0OH25LW3vGxNmxDdtOk2");
        user.setName("user");
        given(this.userRepository.findByUsername("user")).willReturn(Optional.of(user));

        HttpEntity<AuthenticationRequest> request = new HttpEntity<>(new AuthenticationRequest("user", "user"));
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("/authentication", request, String.class);
        return responseEntity.getBody();
    }

    @Test
    void accessAdminApiWithOutToken() {
        given(this.userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), (Pageable) any())).willReturn(new PageImpl<>(new ArrayList<>()));

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/users", String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void loginCommonUser() {
        assertThat(mockCommonUserAndGetToken()).isNotBlank();
    }

    @Test
    void loginAdminUser() {
        assertThat(mockAdminAndGetToken()).isNotBlank();
    }

    @Test
    void accessAdminApiByCommonUser() {
        given(this.userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), (Pageable) any())).willReturn(new PageImpl<>(new ArrayList<>()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + mockCommonUserAndGetToken());
        HttpEntity<AuthenticationRequest> request = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange("/users", HttpMethod.GET, request, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void accessAdminApiByAdmin() {
        given(this.userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), (Pageable) any())).willReturn(new PageImpl<>(new ArrayList<>()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + mockAdminAndGetToken());
        HttpEntity<AuthenticationRequest> request = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange("/users", HttpMethod.GET, request, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void accessGetCurrentUserApiByCommonUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + mockCommonUserAndGetToken());
        HttpEntity<AuthenticationRequest> request = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange("/users/current", HttpMethod.GET, request, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void accessDeleteUserApiByCommonUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + mockCommonUserAndGetToken());
        HttpEntity<AuthenticationRequest> request = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange("/users/2", HttpMethod.DELETE, request, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

}
