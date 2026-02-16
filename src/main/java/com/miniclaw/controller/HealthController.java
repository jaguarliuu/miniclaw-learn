package com.miniclaw.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 *
 * <p>提供应用健康状态检查接口
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * 健康检查接口
     *
     * @return 健康状态
     */
    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "MiniClaw Learn");
        response.put("version", "0.0.1-SNAPSHOT");
        return response;
    }
}
