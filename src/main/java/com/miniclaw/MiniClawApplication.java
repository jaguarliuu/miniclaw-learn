package com.miniclaw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MiniClaw AI Agent 框架主启动类
 *
 * <p>MiniClaw 是一个从零构建的 AI Agent 框架，用于学习 AI Agent 的核心原理。
 *
 * <p>核心特性：
 * <ul>
 *   <li>响应式架构（WebFlux）</li>
 *   <li>多平台消息接入（Telegram、WebSocket）</li>
 *   <li>工具调用与 Skills 系统</li>
 *   <li>Memory 记忆系统</li>
 *   <li>Multi-Agent 协作</li>
 * </ul>
 *
 * @author MiniClaw Learn
 * @version 0.0.1-SNAPSHOT
 */
@SpringBootApplication
public class MiniClawApplication {

    /**
     * 应用启动入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MiniClawApplication.class, args);
    }
}
