package com.miniclaw.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Run 实体类
 *
 * <p>执行记录表：记录每次 Agent 执行过程
 *
 * <p>一个 Run 代表一次完整的 Agent 执行过程，包含多轮对话（Message）
 */
@Entity
@Table(name = "runs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Run {

    /**
     * 执行记录唯一标识（UUID）
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * 关联的会话 ID
     */
    @Column(nullable = false, updatable = false)
    private UUID sessionId;

    /**
     * 执行状态：pending/running/completed/failed
     */
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "pending";

    /**
     * 使用的模型（如：gpt-4、claude-3-opus）
     */
    @Column(length = 100)
    private String model;

    /**
     * 消耗的 Token 数量
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer tokensUsed = 0;

    /**
     * 执行成本（单位：分）
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer costCents = 0;

    /**
     * 开始执行时间
     */
    private LocalDateTime startedAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

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
