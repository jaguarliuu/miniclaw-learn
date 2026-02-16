-- MiniClaw 数据库初始化脚本
-- 用途：安装必要的 PostgreSQL 扩展

-- pgvector 扩展：用于向量检索（Memory 系统使用）
CREATE EXTENSION IF NOT EXISTS vector;

-- uuid-ossp 扩展：用于自动生成 UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 输出已安装的扩展
\echo 'PostgreSQL extensions installed: vector, uuid-ossp'
