-- V1__init_schema.sql
-- MiniClaw 初始化数据库表结构
--
-- 数据模型设计：
-- - Session（会话）：一次完整的对话会话
-- - Run（执行）：一次 Agent 执行过程（一个 Session 可能有多个 Run）
-- - Message（消息）：单条消息（用户输入、AI 回复、系统消息）

-- ============================================
-- 1. sessions 表：会话管理
-- ============================================
CREATE TABLE sessions (
    -- 主键：使用 UUID，避免 ID 冲突
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- 用户标识（可以是用户 ID、设备 ID 等）
    user_id VARCHAR(255) NOT NULL,

    -- 会话标题（可选，用于展示）
    title VARCHAR(500),

    -- 会话状态：active（活跃）、archived（归档）、deleted（已删除）
    status VARCHAR(50) NOT NULL DEFAULT 'active',

    -- 元数据（JSON 格式，存储扩展信息）
    metadata JSONB,

    -- 时间戳
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引：加速按用户查询
CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_status ON sessions(status);
CREATE INDEX idx_sessions_created_at ON sessions(created_at);

-- 注释
COMMENT ON TABLE sessions IS '会话表：管理用户与 Agent 的对话会话';
COMMENT ON COLUMN sessions.id IS '会话唯一标识（UUID）';
COMMENT ON COLUMN sessions.user_id IS '用户标识';
COMMENT ON COLUMN sessions.status IS '会话状态：active/archived/deleted';
COMMENT ON COLUMN sessions.metadata IS '扩展元数据（JSON 格式）';

-- ============================================
-- 2. runs 表：执行记录
-- ============================================
CREATE TABLE runs (
    -- 主键
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- 关联会话（外键）
    session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,

    -- 执行状态：pending（等待中）、running（执行中）、completed（已完成）、failed（失败）
    status VARCHAR(50) NOT NULL DEFAULT 'pending',

    -- 使用的模型（如：gpt-4、claude-3-opus）
    model VARCHAR(100),

    -- Token 消耗统计
    tokens_used INTEGER DEFAULT 0,

    -- 成本统计（单位：分）
    cost_cents INTEGER DEFAULT 0,

    -- 执行时间
    started_at TIMESTAMP,
    completed_at TIMESTAMP,

    -- 时间戳
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引：加速查询
CREATE INDEX idx_runs_session_id ON runs(session_id);
CREATE INDEX idx_runs_status ON runs(status);
CREATE INDEX idx_runs_created_at ON runs(created_at);

-- 注释
COMMENT ON TABLE runs IS '执行记录表：记录每次 Agent 执行过程';
COMMENT ON COLUMN runs.session_id IS '关联的会话 ID';
COMMENT ON COLUMN runs.status IS '执行状态：pending/running/completed/failed';
COMMENT ON COLUMN runs.tokens_used IS '消耗的 Token 数量';
COMMENT ON COLUMN runs.cost_cents IS '执行成本（单位：分）';

-- ============================================
-- 3. messages 表：消息记录
-- ============================================
CREATE TABLE messages (
    -- 主键
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- 关联执行记录（外键）
    run_id UUID NOT NULL REFERENCES runs(id) ON DELETE CASCADE,

    -- 消息角色：user（用户）、assistant（AI）、system（系统）、tool（工具）
    role VARCHAR(50) NOT NULL,

    -- 消息内容
    content TEXT NOT NULL,

    -- Token 数量（用于统计）
    tokens INTEGER,

    -- 元数据（存储额外信息，如工具调用参数）
    metadata JSONB,

    -- 时间戳
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引：加速查询
CREATE INDEX idx_messages_run_id ON messages(run_id);
CREATE INDEX idx_messages_role ON messages(role);
CREATE INDEX idx_messages_created_at ON messages(created_at);

-- 注释
COMMENT ON TABLE messages IS '消息表：记录会话中的每条消息';
COMMENT ON COLUMN messages.role IS '消息角色：user/assistant/system/tool';
COMMENT ON COLUMN messages.content IS '消息内容';
COMMENT ON COLUMN messages.tokens IS '消息的 Token 数量';
