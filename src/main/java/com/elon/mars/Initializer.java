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
import java.util.logging.Logger;

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

                    Permission permission = Permission.of("用户管理", PermissionEnum.USER_MANAGE.name());
                    permission = permissionRepository.save(permission);

                    Role role = Role.of("管理员", new HashSet<>(List.of(permission)));
                    role = roleRepository.save(role);

                    User user = User.ofNew("admin", "admin", "admin123", new HashSet<>(List.of(role)), new HashSet<>());
                    userRepository.save(user);
                }
            } catch (Exception e) {
                logger.warning("初始化数据失败");
            }
        }).start();
    }

}
