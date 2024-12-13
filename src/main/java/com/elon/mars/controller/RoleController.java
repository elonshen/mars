package com.elon.mars.controller;

import com.elon.mars.controller.dto.RoleCreateRequest;
import com.elon.mars.controller.dto.RoleUpdateRequest;
import com.elon.mars.controller.dto.RoleVO;
import com.elon.mars.controller.mapper.RoleMapper;
import com.elon.mars.domain.Permission;
import com.elon.mars.domain.Role;
import com.elon.mars.repository.PermissionRepository;
import com.elon.mars.repository.RoleRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/roles")
@Tag(name = "角色资源")
public class RoleController {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;

    public RoleController(RoleRepository roleRepository, RoleMapper roleMapper,
                          PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
        this.permissionRepository = permissionRepository;
    }

    /**
     * 获取角色列表
     *
     * @param name     角色名称,支持模糊搜索,可选
     * @param pageable 分页参数
     * @return 角色列表分页数据
     */
    @GetMapping
    public Page<RoleVO> list(
            @RequestParam(required = false) String name,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        Specification<Role> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 添加名称搜索条件
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Role> roles = roleRepository.findAll(specification, pageable);
        return roleMapper.toRoleVOPage(roles.getContent(), pageable, roles.getTotalElements());
    }

    /**
     * 创建新角色
     *
     * @param request 角色创建请求
     * @return 创建成功的角色信息
     * @throws RuntimeException 当角色名称在租户内重复时抛出异常
     */
    @PostMapping
    public RoleVO create(@RequestBody @Valid RoleCreateRequest request) {
        // 转换请求为实体
        Role role = roleMapper.toRole(request);

        // 设置权限
        if (request.permissionIds() != null && !request.permissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.permissionIds()));
            role.setPermissions(permissions);
        }

        try {
            role = roleRepository.save(role);
            return roleMapper.toRoleVO(role);
        } catch (Exception e) {
            if (e.getMessage().contains("uk_role_name_tenant")) {
                throw new RuntimeException("角色名称在租户内重复");
            }
            throw e;
        }
    }

    /**
     * 更新角色信息
     *
     * @param id      角色ID
     * @param request 角色更新请求
     * @return 更新后的角色信息
     * @throws RuntimeException 当角色不存在或新角色名称在租户内重复时抛出异常
     */
    @PutMapping("/{id}")
    public RoleVO update(@PathVariable Long id, @RequestBody @Valid RoleUpdateRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        // 更新基本信息
        roleMapper.updateRole(request, role);

        // 更新权限
        if (request.permissionIds() != null) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.permissionIds()));
            role.setPermissions(permissions);
        }

        try {
            role = roleRepository.save(role);
            return roleMapper.toRoleVO(role);
        } catch (Exception e) {
            if (e.getMessage().contains("uk_role_name_tenant")) {
                throw new RuntimeException("角色名称在租户内重复");
            }
            throw e;
        }
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @throws RuntimeException 当角色不存在或角色已被用户使用时抛出异常
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        try {
            roleRepository.deleteById(id);
        } catch (Exception e) {
            if (e.getMessage().contains("foreign key constraint")) {
                throw new RuntimeException("该角色已被用户使用，无法删除");
            }
            throw e;
        }
    }

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详细信息
     * @throws RuntimeException 当角色不存在时抛出异常
     */
    @GetMapping("/{id}")
    public RoleVO getById(@PathVariable Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        return roleMapper.toRoleVO(role);
    }
}
