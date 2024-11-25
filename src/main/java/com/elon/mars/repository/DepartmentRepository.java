package com.elon.mars.repository;

import com.elon.mars.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {
    /**
     * 获取最顶层的部门列表（不带父部门的）
     *
     * @return 顶层部门列表
     */
    List<Department> findByParentIsNull();

    /**
     * 判断部门名称在同级下是否重复
     */
    boolean existsByNameAndParentId(String name, Long parentId);

    /**
     * 判断部门名称在同级下是否重复(排除自身)
     */
    boolean existsByNameAndParentIdAndIdNot(String name, Long parentId, Long id);

    /**
     * 获取部门的所有子部门ID（包括自身）
     */
    @Query(value = "WITH RECURSIVE dept_tree AS (" +
            "  SELECT id FROM department WHERE id = ?1 " +
            "  UNION ALL " +
            "  SELECT d.id FROM department d " +
            "  INNER JOIN dept_tree dt ON d.parent_id = dt.id" +
            ") " +
            "SELECT id FROM dept_tree", nativeQuery = true)
    List<Long> findAllChildrenIds(Long departmentId);
}