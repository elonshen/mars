package com.elon.mars.controller;

import com.elon.mars.controller.dto.PermissionCreateRequest;
import com.elon.mars.controller.dto.PermissionUpdateRequest;
import com.elon.mars.controller.dto.PermissionVO;
import com.elon.mars.controller.mapper.PermissionMapper;
import com.elon.mars.domain.Permission;
import com.elon.mars.repository.PermissionRepository;
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
import java.util.List;

@RestController
@RequestMapping("/permissions")
@Tag(name = "权限资源")
public class PermissionController {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionController(PermissionRepository permissionRepository, PermissionMapper permissionMapper) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    /**
     * 获取权限列表
     *
     * @param name     权限名称,支持模糊查询,可选
     * @param code     权限代码,支持模糊查询,可选
     * @param pageable 分页参数
     * @return 权限列表分页数据
     */
    @GetMapping
    public Page<PermissionVO> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        Specification<Permission> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 添加查询条件
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (code != null && !code.isEmpty()) {
                predicates.add(cb.like(root.get("code"), "%" + code + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Permission> permissions = permissionRepository.findAll(specification, pageable);
        return permissions.map(permissionMapper::toPermissionVO);
    }

    /**
     * 创建新权限
     *
     * @param request 权限创建请求
     * @return 创建成功的权限信息
     * @throws RuntimeException 当权限名称或代码在租户内重复时抛出异常
     */
    @PostMapping
    public PermissionVO create(@RequestBody @Valid PermissionCreateRequest request) {
        /* 校验权限代码是否在租户内重复 */
        if (permissionRepository.existsByCode(request.code())) {
            throw new RuntimeException("权限代码在租户内重复");
        }

        // 创建权限
        Permission permission = permissionMapper.toPermission(request);

        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionVO(permission);
    }

    /**
     * 更新权限信息
     * 注意：权限代码不允许修改，只能修改权限名称
     *
     * @param id      权限ID
     * @param request 权限更新请求
     * @return 更新后的权限信息
     * @throws RuntimeException 当权限不存在或新权限名称在租户内重复时抛出异常
     */
    @PutMapping("/{id}")
    public PermissionVO update(@PathVariable Long id, @RequestBody @Valid PermissionUpdateRequest request) {
        Permission permission = permissionRepository.findById(id).orElseThrow(() -> new RuntimeException("权限不存在"));
        permission.setName(request.name());

        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionVO(permission);
    }

    /**
     * 删除权限
     *
     * @param id 权限ID
     * @throws RuntimeException 当权限不存在或权限已被角色使用时抛出异常
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        try {
            permissionRepository.deleteById(id);
        } catch (Exception e) {
            if (e.getMessage().contains("foreign key constraint")) {
                throw new RuntimeException("该权限已被角色使用，无法删除");
            }
            throw e;
        }
    }

    /**
     * 获取权限详情
     *
     * @param id 权限ID
     * @return 权限详细信息
     * @throws RuntimeException 当权限不存在时抛出异常
     */
    @GetMapping("/{id}")
    public PermissionVO getById(@PathVariable Long id) {
        Permission permission = permissionRepository.findById(id).orElseThrow(() -> new RuntimeException("权限不存在"));
        return permissionMapper.toPermissionVO(permission);
    }
}