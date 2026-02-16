package com.miniclaw.repository;

import com.miniclaw.entity.Run;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Run 数据访问层
 *
 * <p>提供 Run 实体的 CRUD 操作
 */
@Repository
public interface RunRepository extends JpaRepository<Run, UUID> {

    /**
     * 按会话查询所有执行记录（按创建时间倒序）
     *
     * @param sessionId 会话 ID
     * @return 执行记录列表
     */
    List<Run> findBySessionIdOrderByCreatedAtDesc(UUID sessionId);

    /**
     * 按状态查询执行记录
     *
     * @param status 执行状态
     * @return 执行记录列表
     */
    List<Run> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * 按会话和状态查询执行记录
     *
     * @param sessionId 会话 ID
     * @param status    执行状态
     * @return 执行记录列表
     */
    List<Run> findBySessionIdAndStatusOrderByCreatedAtDesc(UUID sessionId, String status);
}
