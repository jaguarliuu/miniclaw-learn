package com.miniclaw.repository;

import com.miniclaw.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Session 数据访问层
 *
 * <p>提供 Session 实体的 CRUD 操作
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    /**
     * 按用户查询所有会话（按创建时间倒序）
     *
     * @param userId 用户标识
     * @return 会话列表
     */
    List<Session> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * 按状态查询会话
     *
     * @param status 会话状态
     * @return 会话列表
     */
    List<Session> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * 按用户和状态查询会话
     *
     * @param userId 用户标识
     * @param status 会话状态
     * @return 会话列表
     */
    List<Session> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, String status);
}
