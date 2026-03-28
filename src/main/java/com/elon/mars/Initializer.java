package com.elon.mars;

import com.elon.mars.domain.*;
import com.elon.mars.repository.PermissionRepository;
import com.elon.mars.repository.RoleRepository;
import com.elon.mars.repository.TenantRepository;
import com.elon.mars.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class Initializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final TenantRepository tenantRepository;
    private final Logger logger = Logger.getLogger(Initializer.class.getName());

    public Initializer(UserRepository userRepository, RoleRepository roleRepository, PermissionRepository permissionRepository, TenantRepository tenantRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public void run(String... args) {
        new Thread(() -> {
            try {
                Thread.sleep(5000);  // 延迟5秒

                /* 无认证用户时,默认为平台租户的数据 */
                if (userRepository.count() == 0) {
                    logger.info("初始化数据");
                    Tenant tenant = Tenant.of("平台租户", null, TenantType.PLATFORM);
                    tenant.setId(114166601818112L);
                    tenantRepository.save(tenant);

                    Set<PermissionEnum> permissionEnums = Set.of(
                            PermissionEnum.TENANT_MANAGE,
                            PermissionEnum.USER_MANAGE,
                            PermissionEnum.PERMISSION_MANAGE,
                            PermissionEnum.ROLE_MANAGE,
                            PermissionEnum.DEPARTMENT_MANAGE);

                    Set<Permission> permissions = permissionEnums.stream()
                            .map(p -> {
                                Permission permission = Permission.of(p.getName(), p.name(), tenant.getId());
                                return permissionRepository.save(permission);
                            })
                            .collect(Collectors.toSet());

                    Role role = Role.of("系统管理员", permissions, tenant.getId());
                    role = roleRepository.save(role);

                    User user = User.ofNew("admin", "admin", "admin123",
                            new HashSet<>(List.of(role)), new HashSet<>(), tenant.getId());
                    userRepository.save(user);
                }
            } catch (Exception e) {
                logger.warning("初始化数据失败");
            }
        }).start();
    }

}
