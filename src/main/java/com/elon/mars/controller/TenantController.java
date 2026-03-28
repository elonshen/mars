package com.elon.mars.controller;

import com.elon.mars.controller.dto.TenantCreateRequest;
import com.elon.mars.controller.dto.TenantUpdateRequest;
import com.elon.mars.controller.dto.TenantVO;
import com.elon.mars.controller.mapper.TenantMapper;
import com.elon.mars.domain.*;
import com.elon.mars.repository.PermissionRepository;
import com.elon.mars.repository.RoleRepository;
import com.elon.mars.repository.TenantRepository;
import com.elon.mars.repository.UserRepository;
import com.elon.mars.service.JWTService;
import com.elon.mars.service.SecurityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tenants")
@Tag(name = "租户资源")
public class TenantController {
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final SecurityService securityService;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final JWTService jwtService;

    public TenantController(UserRepository userRepository, TenantRepository tenantRepository, TenantMapper tenantMapper, SecurityService securityService, PermissionRepository permissionRepository, RoleRepository roleRepository, JWTService jwtService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.tenantMapper = tenantMapper;
        this.jwtService = jwtService;
        this.securityService = securityService;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * 获取当前用户可访问的租户列表
     */
    @GetMapping("/available")
    public List<TenantVO> getAvailableTenants() {
        User currentUser = securityService.getCurrentUser();

        // 通过查询当前用户的Auth说关联的User获取租户ID
        List<Long> tenantIds = userRepository.findByAuth_IdNative(currentUser.getAuth().getId()).stream()
                .map(User::getTenantId)
                .distinct()
                .collect(Collectors.toList());

        // 查询租户详细信息
        List<Tenant> tenants = tenantRepository.findAllById(tenantIds);
        return tenantMapper.toTenantVOs(tenants);
    }
    /**
     * 获取当前租户信息
     */
    @GetMapping("/current")
    public TenantVO getCurrentTenant() {
        Tenant tenant = securityService.getCurrentTenant();
        return tenantMapper.toTenantVO(tenant);
    }

    /**
     * 切换到指定租户
     */
    @PostMapping("/{tenantId}/switch")
    public String switchTenant(@PathVariable Long tenantId) {
        // 验证用户是否有权限访问目标租户
        Long authId = securityService.getCurrentUser().getAuth().getId();

        User user = userRepository.findByAuthIdAndTenantIdNative(authId, tenantId)
                .orElseThrow(() -> new RuntimeException("您没有访问该租户的权限"));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("租户不存在"));

        return jwtService.generateToken(user, tenant);
    }

    /**
     * 获取租户列表
     */
    @GetMapping
    public Page<TenantVO> list(
            @RequestParam(required = false) String name,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        if (!SecurityService.isPlatformTenant()) {
            throw new RuntimeException("只有平台租户可以查看租户列表");
        }

        Specification<Tenant> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Tenant> tenants = tenantRepository.findAll(specification, pageable);
        return tenants.map(tenantMapper::toTenantVO);
    }

    /**
     * 创建新租户
     */
    @PostMapping
    @Transactional
    public TenantVO create(@RequestBody @Valid TenantCreateRequest request) {
        if (!SecurityService.isPlatformTenant()) {
            throw new RuntimeException("只有平台租户可以创建新租户");
        }

        if (tenantRepository.existsByName(request.name())) {
            throw new RuntimeException("租户名称已存在");
        }

        // 1. 创建租户
        Tenant tenant = new Tenant();
        tenant.setName(request.name());
        tenant.setDescription(request.description());
        tenant.setTenantType(TenantType.valueOf(request.tenantType().name()));
        tenant = tenantRepository.save(tenant);

        // 2. 初始化权限
        Set<PermissionEnum> permissionEnums = switch (request.tenantType()) {
            case NORMAL -> Arrays.stream(PermissionEnum.values())
                    .filter(p -> p != PermissionEnum.TENANT_MANAGE)
                    .collect(Collectors.toSet());
            case REGULATORY -> Set.of(
                    PermissionEnum.USER_MANAGE,
                    PermissionEnum.PERMISSION_MANAGE,
                    PermissionEnum.ROLE_MANAGE,
                    PermissionEnum.DEPARTMENT_MANAGE);
        };

        Tenant finalTenant = tenant;
        Set<Permission> permissions = permissionEnums.stream()
                .map(p -> {
                    Permission permission = Permission.of(p.getName(), p.name(), finalTenant.getId());
                    return permissionRepository.save(permission);
                })
                .collect(Collectors.toSet());

        // 3. 创建管理员角色
        Role adminRole = Role.of("系统管理员", permissions, tenant.getId());
        adminRole = roleRepository.save(adminRole);

        // 4. 创建租户管理员用户 - 使用当前平台管理员的认证账号
        User currentPlatformAdmin = securityService.getCurrentUser();
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);

        User adminUser = new User();
        adminUser.setName(request.adminName());
        adminUser.setAuth(currentPlatformAdmin.getAuth());  // 复用平台管理员的认证账号
        adminUser.setRoles(adminRoles);
        adminUser.setTenantId(tenant.getId());
        userRepository.save(adminUser);

        return tenantMapper.toTenantVO(tenant);
    }

    /**
     * 更新租户信息
     *
     * @param id      租户ID
     * @param request 租户更新请求
     * @return 更新后的租户信息
     * @throws RuntimeException 当非平台租户尝试更新,租户不存在,或新租户名称已存在时抛出异常
     */
    @PutMapping("/{id}")
    public TenantVO update(@PathVariable Long id, @RequestBody @Valid TenantUpdateRequest request) {
        if (!SecurityService.isPlatformTenant()) {
            throw new RuntimeException("只有平台租户可以更新租户信息");
        }

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("租户不存在"));

        if (!tenant.getName().equals(request.name()) &&
                tenantRepository.existsByName(request.name())) {
            throw new RuntimeException("租户名称已存在");
        }

        tenant.setName(request.name());
        tenant.setDescription(request.description());
        tenant = tenantRepository.save(tenant);

        return tenantMapper.toTenantVO(tenant);
    }

    /**
     * 删除租户及其关联数据
     * 仅允许删除用户数量小于2的租户,用于清理误创建的租户
     *
     * @param id 租户ID
     * @throws RuntimeException 当非平台租户尝试删除,租户不存在,尝试删除平台租户,或租户下有多个用户时抛出异常
     */
    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        if (!SecurityService.isPlatformTenant()) {
            throw new RuntimeException("只有平台租户可以删除租户");
        }

        Tenant tenant = tenantRepository.findById(id).orElseThrow(() -> new RuntimeException("租户不存在"));

        if (TenantType.PLATFORM.equals(tenant.getTenantType())) {
            throw new RuntimeException("平台租户不能被删除");
        }

        long userCount = userRepository.countByTenantId(id);
        if (userCount >= 2) {
            throw new RuntimeException("该租户下已有多个用户,不能删除。只能删除用户数量小于2的租户,用于清理误创建的租户。");
        }

        // 删除租户关联的所有数据
        // 1. 删除用户-角色关联
        List<User> users = userRepository.findAllByTenantId(id);
        for (User user : users) {
            user.setRoles(new HashSet<>());
            userRepository.save(user);
        }

        // 2. 删除角色-权限关联
        List<Role> roles = roleRepository.findByTenantId(id);
        for (Role role : roles) {
            role.setPermissions(new HashSet<>());
            roleRepository.save(role);
        }

        // 3. 删除用户(只删除User实体,不删除Auth实体)
        userRepository.deleteByTenantId(id);

        // 4. 删除角色
        roleRepository.deleteByTenantId(id);

        // 5. 删除权限
        permissionRepository.deleteByTenantId(id);

        // 6. 删除租户
        tenantRepository.deleteById(id);
    }

    /**
     * 获取租户详情
     *
     * @param id 租户ID
     * @return 租户详细信息
     * @throws RuntimeException 当非平台租户尝试访问或租户不存在时抛出异常
     */
    @GetMapping("/{id}")
    public TenantVO getById(@PathVariable Long id) {
        if (!SecurityService.isPlatformTenant()) {
            throw new RuntimeException("只有平台租户可以查看租户详情");
        }

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("租户不存在"));

        return tenantMapper.toTenantVO(tenant);
    }
}