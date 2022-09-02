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

@SuppressWarnings("SpringBootApplicationProperties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"my.tokenExpirationValue=3000"})
public class AuthenticationTest {
    @Autowired
    TestRestTemplate restTemplate;
    @MockBean
    private UserRepository userRepository;
    @SuppressWarnings("SpellCheckingInspection")
    private String mockAdminAndGetToken() {
        User user = new User();
        user.setUsername("foo");
        user.setPassword("$2a$10$1vMoRhQlmFBosVZQvta28OqXeOl1ybZU0W4L7tuOWpYAZQy4jzRR2");
        user.setName("foo");
        user.setRole("ROLE_ADMIN");
        given(this.userRepository.findByUsername("foo")).willReturn(Optional.of(user));

        HttpEntity<AuthenticationRequest> request = new HttpEntity<>(new AuthenticationRequest("foo", "foo"));
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("/authentication", request, String.class);
        return responseEntity.getBody();
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void mockCommonUser() {
        User user = new User();
        user.setUsername("user");
        user.setPassword("$2a$10$ktiha.elFVEC43oGdfr5vOVCbrUfRcYra0OH25LW3vGxNmxDdtOk2");
        user.setName("user");
        given(this.userRepository.findByUsername("user")).willReturn(Optional.of(user));
    }

    private String mockCommonUserAndGetToken() {
        mockCommonUser();

        HttpEntity<AuthenticationRequest> request = new HttpEntity<>(new AuthenticationRequest("user", "user"));
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("/authentication", request, String.class);
        return responseEntity.getBody();
    }

    @Test
    void accessAdminApiWithOutToken() {
        given(this.userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), (Pageable) any())).willReturn(new PageImpl<>(new ArrayList<>()));

        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/users", String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN); //无token也是一种权限，访问认证资源应当返回403
    }

    @Test
    void accessGetCurrentUserApiByErrorToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + "error-code");
        HttpEntity<AuthenticationRequest> request = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange("/users/current", HttpMethod.GET, request, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
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
    void loginByErrorUsername() {
        mockCommonUser();
        HttpEntity<AuthenticationRequest> request = new HttpEntity<>(new AuthenticationRequest("user2", "user"));
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("/authentication", request, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void loginByErrorPassword() {
        mockCommonUser();
        HttpEntity<AuthenticationRequest> request = new HttpEntity<>(new AuthenticationRequest("user", "user2"));
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("/authentication", request, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void accessGetCurrentUserApiByExpiredToken() throws InterruptedException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + mockCommonUserAndGetToken());
        Thread.sleep(3100);
        HttpEntity<AuthenticationRequest> request = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange("/users/current", HttpMethod.GET, request, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
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
