package com.elon.mars.controller;

import com.elon.mars.controller.dto.*;
import com.elon.mars.controller.mapper.DepartmentMapper;
import com.elon.mars.domain.Department;
import com.elon.mars.domain.User;
import com.elon.mars.repository.DepartmentRepository;
import com.elon.mars.repository.UserRepository;
import com.elon.mars.service.SecurityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/departments")
@Tag(name = "部门管理")
public class DepartmentController {
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final UserRepository userRepository;

    public DepartmentController(DepartmentRepository departmentRepository, DepartmentMapper departmentMapper, UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
        this.userRepository = userRepository;
    }

    /**
     * 获取部门树结构
     *
     * @return 部门树形结构
     */
    @GetMapping("/tree")
    public List<TreeNodeVO> getDepartmentTree() {
        // 获取所有顶层部门
        List<Department> topDepartments = departmentRepository.findByParentIsNull();

        // 递归构建树结构
        return topDepartments.stream().map(this::buildDepartmentTreeNode).collect(Collectors.toList());
    }

    /**
     * 获取部门和用户的树结构
     *
     * @return 部门和用户的树形结构
     */
    @GetMapping("/tree/with-users")
    public List<TreeNodeVO> getDepartmentUserTree() {
        // 获取所有顶层部门
        List<Department> topDepartments = departmentRepository.findByParentIsNull();

        // 递归构建树结构
        return topDepartments.stream()
                .map(this::buildDepartmentUserTreeNode)
                .collect(Collectors.toList());
    }

    /**
     * 创建新部门
     *
     * @param request 部门创建请求
     * @return 创建成功的部门信息
     * @throws RuntimeException 当部门名称在同级部门中重复时抛出异常
     */
    @PostMapping
    @Transactional
    public DepartmentVO create(@RequestBody @Valid DepartmentCreateRequest request) {
        // 检查同级部门下是否有重名
        if (departmentRepository.existsByNameAndParentId(
                request.name(), request.parentId())) {
            throw new RuntimeException("同级部门下已存在同名部门");
        }

        // 创建部门
        Department department = new Department();
        department.setName(request.name());
        department.setTenantId(SecurityService.getCurrentTenantId());

        // 设置父部门
        if (request.parentId() != null) {
            department.setParent(departmentRepository.findById(request.parentId()).orElseThrow(() -> new RuntimeException("父部门不存在")));
        }

        // 关联用户
        if (request.userIds() != null && !request.userIds().isEmpty()) {
            Set<User> users = new HashSet<>(userRepository.findAllById(request.userIds()));
            department.setUsers(users);
        }

        department = departmentRepository.save(department);
        return departmentMapper.toDepartmentVO(department);
    }

    /**
     * 更新部门信息
     *
     * @param id      部门ID
     * @param request 部门更新请求
     * @return 更新后的部门信息
     * @throws RuntimeException 当部门不存在或新部门名称在同级部门中重复时抛出异常
     */
    @PutMapping("/{id}")
    @Transactional
    public DepartmentVO update(@PathVariable Long id, @RequestBody @Valid DepartmentUpdateRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在"));

        // 检查是否存在重名（排除自身）
        if (departmentRepository.existsByNameAndParentIdAndIdNot(request.name(), request.parentId(), id)) {
            throw new RuntimeException("同级部门下已存在同名部门");
        }

        // 更新基本信息
        department.setName(request.name());

        // 更新父部门
        if (!Objects.equals(department.getParent() != null ? department.getParent().getId() : null, request.parentId())) {
            // 检查是否会形成循环依赖
            if (request.parentId() != null && departmentRepository.findAllChildrenIds(id).contains(request.parentId())) {
                throw new RuntimeException("不能将部门移动到其子部门下");
            }

            department.setParent(request.parentId() != null ? departmentRepository.findById(request.parentId()).orElseThrow(() -> new RuntimeException("父部门不存在")) : null);
        }

        // 更新用户关联
        if (request.userIds() != null) {
            Set<User> users = new HashSet<>(userRepository.findAllById(request.userIds()));
            department.setUsers(users);
        }

        department = departmentRepository.save(department);
        return departmentMapper.toDepartmentVO(department);
    }

    /**
     * 删除部门
     * 注意：删除部门时会同时删除其所有子部门
     *
     * @param id 部门ID
     * @throws RuntimeException 当部门不存在时抛出异常
     */
    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        Department department = departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("部门不存在"));

        // 解除与用户的关联
        department.getUsers().clear();
        departmentRepository.save(department);

        // 递归删除子部门
        department.getChildren().forEach(child -> delete(child.getId()));

        // 删除当前部门
        departmentRepository.delete(department);
    }

    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门详细信息
     * @throws RuntimeException 当部门不存在时抛出异常
     */
    @GetMapping("/{id}")
    public DepartmentVO getById(@PathVariable Long id) {
        Department department = departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("部门不存在"));
        return departmentMapper.toDepartmentVO(department);
    }

    // 递归构建部门树节点
    private TreeNodeVO buildDepartmentTreeNode(Department department) {
        List<TreeNodeVO> children = department.getChildren().stream()
                .map(this::buildDepartmentTreeNode)
                .collect(Collectors.toList());

        return new TreeNodeVO(
                department.getId().toString(),
                department.getParent() != null ? department.getParent().getId().toString() : null,
                department.getName(),
                TreeNodeType.DEPARTMENT,
                children
        );
    }

    // 递归构建部门和用户树节点
    private TreeNodeVO buildDepartmentUserTreeNode(Department department) {
        List<TreeNodeVO> children = new ArrayList<>();

        // 添加子部门节点
        children.addAll(department.getChildren().stream()
                .map(this::buildDepartmentUserTreeNode)
                .toList());

        // 添加用户节点
        children.addAll(department.getUsers().stream()
                .map(user -> new TreeNodeVO(String.valueOf(user.getId()), department.getId().toString(), user.getName(), TreeNodeType.USER, Collections.emptyList())).toList());

        return new TreeNodeVO(department.getId().toString(), department.getParent() != null ? department.getParent().getId().toString() : null, department.getName(), TreeNodeType.DEPARTMENT, children);
    }
}