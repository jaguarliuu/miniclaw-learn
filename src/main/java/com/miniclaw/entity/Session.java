package com.miniclaw.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Session 实体类
 *
 * <p>会话表：管理用户与 Agent 的对话会话
 *
 * <p>一个 Session 代表一次完整的对话会话，可能包含多次 Agent 执行（Run）
 */
@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    /**
     * 会话唯一标识（UUID）
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * 用户标识
     */
    @Column(nullable = false)
    private String userId;

    /**
     * 会话标题（用于展示）
     */
    @Column(length = 500)
    private String title;

    /**
     * 会话状态：active/archived/deleted
     */
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "active";

    /**
     * 扩展元数据（JSON 格式）
     */
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 持久化前回调：自动设置时间戳
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * 更新前回调：自动更新时间戳
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
