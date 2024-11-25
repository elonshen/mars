package com.elon.mars.tenant;

import com.elon.mars.domain.*;
import com.elon.mars.repository.PermissionRepository;
import com.elon.mars.repository.RoleRepository;
import com.elon.mars.repository.TenantRepository;
import com.elon.mars.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")  // 使用测试配置文件
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 每个测试类一个实例
public class TenantDataIsolationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("FieldCanBeLocal")
    private Tenant platformTenant;
    private Tenant normalTenant;
    @SuppressWarnings("FieldCanBeLocal")
    private Role platformRole;
    private Role normalRole;
    private String platformUserToken;
    private String normalUserToken;

    @BeforeAll
    void setUp() throws Exception {
        // 清理数据
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        tenantRepository.deleteAll();

        // 创建租户
        platformTenant = tenantRepository.saveAndFlush(Tenant.of("平台租户", "平台租户", TenantType.PLATFORM));
        normalTenant = tenantRepository.saveAndFlush(Tenant.of("普通租户", "普通租户", TenantType.NORMAL));

        // 创建权限
        Permission permission = Permission.of("用户管理", PermissionEnum.USER_MANAGE.name(), platformTenant.getId());
        permission = permissionRepository.saveAndFlush(permission);

        // 创建角色并分配权限
        platformRole = Role.of("平台管理员", Set.of(permission), platformTenant.getId());
        normalRole = Role.of("租户管理员", Set.of(permission), normalTenant.getId());
        platformRole = roleRepository.saveAndFlush(platformRole);
        normalRole = roleRepository.saveAndFlush(normalRole);

        // 创建用户并分配角色
        User platformUser = User.ofNew("platform", "platform", "platform", Set.of(platformRole), new HashSet<>(), platformTenant.getId());
        User normalUser = User.ofNew("normal", "normal", "normal", Set.of(normalRole), new HashSet<>(), normalTenant.getId());
        userRepository.saveAndFlush(platformUser);
        userRepository.saveAndFlush(normalUser);

        // 获取认证token
        platformUserToken = getAuthToken("platform", "platform");
        normalUserToken = getAuthToken("normal", "normal");
    }

    @Test
    void normalUserShouldOnlySeeOwnTenantUsers() throws Exception {
        // 使用普通租户用户token查询用户列表
        MvcResult result = mvc.perform(get("/users")
                        .header("Authorization", "Bearer " + normalUserToken))
                .andExpect(status().isOk())
                .andReturn();

        // 解析分页响应
        Map<String, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        List<Map<String, Object>> users = (List<Map<String, Object>>) response.get("content");

        // 验证只返回了本租户的数据
        assertThat(users).hasSize(1);
    }

    @Test
    void platformUserShouldOnlySeeOwnTenantUsers() throws Exception {
        // 使用普通租户用户token查询用户列表
        MvcResult result = mvc.perform(get("/users")
                        .header("Authorization", "Bearer " + platformUserToken))
                .andExpect(status().isOk())
                .andReturn();

        // 解析分页响应
        Map<String, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        List<Map<String, Object>> users = (List<Map<String, Object>>) response.get("content");

        // 验证只返回了本租户的数据
        assertThat(users).hasSize(1);
    }

    @Test
    void normalUserCanOnlyCreateUserForOwnTenant() throws Exception {
        // 构造新用户数据，尝试指定为平台租户
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("name", "test");
        newUser.put("username", "test");
        newUser.put("password", "test");
        newUser.put("roleIds", Collections.singleton(normalRole.getId()));

        // 使用普通租户用户创建用户
        mvc.perform(post("/users")
                        .header("Authorization", "Bearer " + normalUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());

        // 验证用户创建成功且租户ID被强制设置为当前租户
        User savedUser = userRepository.findFirstByAuth_Username("test").orElseThrow(() -> new AssertionError("User not found"));
        assertThat(savedUser.getTenantId()).isEqualTo(normalTenant.getId());
    }

    private String getAuthToken(String username, String password) throws Exception {
        return mvc.perform(post("/authentication")
                        .with(httpBasic(username, password)))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}