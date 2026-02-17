package com.miniclaw;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * MiniClaw 应用程序上下文测试
 *
 * <p>验证 Spring Boot 应用能够正常启动和加载所有组件。
 */
@SpringBootTest
@ActiveProfiles("test")
class MiniClawApplicationTests {

    /**
     * 测试应用上下文加载
     */
    @Test
    void contextLoads() {
        // 如果应用上下文加载成功，测试通过
    }
}
