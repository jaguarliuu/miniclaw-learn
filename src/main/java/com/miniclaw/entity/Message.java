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
 * Message 实体类
 *
 * <p>消息表：记录会话中的每条消息
 *
 * <p>一个 Message 代表一轮对话中的单条消息（用户输入、AI 回复、系统消息等）
 */
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    /**
     * 消息唯一标识（UUID）
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * 关联的执行记录 ID
     */
    @Column(nullable = false, updatable = false)
    private UUID runId;

    /**
     * 消息角色：user/assistant/system/tool
     */
    @Column(nullable = false, length = 50)
    private String role;

    /**
     * 消息内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 消息的 Token 数量
     */
    private Integer tokens;

    /**
     * 扩展元数据（存储工具调用参数等）
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
     * 持久化前回调：自动设置时间戳
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
