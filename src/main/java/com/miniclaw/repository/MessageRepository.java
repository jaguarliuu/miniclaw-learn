package com.miniclaw.repository;

import com.miniclaw.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Message 数据访问层
 *
 * <p>提供 Message 实体的 CRUD 操作
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * 按执行记录查询所有消息（按创建时间正序）
     *
     * @param runId 执行记录 ID
     * @return 消息列表
     */
    List<Message> findByRunIdOrderByCreatedAtAsc(UUID runId);

    /**
     * 按角色查询消息
     *
     * @param role 消息角色
     * @return 消息列表
     */
    List<Message> findByRoleOrderByCreatedAtDesc(String role);

    /**
     * 按执行记录和角色查询消息
     *
     * @param runId 执行记录 ID
     * @param role  消息角色
     * @return 消息列表
     */
    List<Message> findByRunIdAndRoleOrderByCreatedAtAsc(UUID runId, String role);
}
