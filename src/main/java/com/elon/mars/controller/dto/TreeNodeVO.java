package com.elon.mars.controller.dto;

import java.util.List;

/**
 * 树节点展示对象
 *
 * @param id       节点ID
 * @param parentId 父节点ID
 * @param name     节点名称
 * @param type     节点类型
 * @param children 子节点列表
 */
public record TreeNodeVO(
        String id,
        String parentId,
        String name,
        TreeNodeType type,
        List<TreeNodeVO> children
) {
}