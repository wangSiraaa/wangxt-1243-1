-- Security Company On-site Service Management System
-- PostgreSQL Database Schema

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 用户表
CREATE TABLE IF NOT EXISTS sec_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL CHECK (role IN ('PROJECT_MANAGER', 'TEAM_LEADER', 'CUSTOMER')),
    customer_id UUID,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 客户表
CREATE TABLE IF NOT EXISTS sec_customer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    address VARCHAR(255),
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 客户点位表
CREATE TABLE IF NOT EXISTS sec_customer_point (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES sec_customer(id),
    point_name VARCHAR(100) NOT NULL,
    point_code VARCHAR(50) UNIQUE,
    address VARCHAR(255),
    is_key_position BOOLEAN DEFAULT false,
    description TEXT,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 保安人员表
CREATE TABLE IF NOT EXISTS sec_personnel (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_no VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE')),
    phone VARCHAR(20),
    id_card VARCHAR(18) UNIQUE,
    address VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'ON_LEAVE')),
    max_consecutive_night_shifts INTEGER DEFAULT 3,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 资质类型表
CREATE TABLE IF NOT EXISTS sec_qualification_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    required_for_key_position BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 人员资质表
CREATE TABLE IF NOT EXISTS sec_qualification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    personnel_id UUID NOT NULL REFERENCES sec_personnel(id),
    qualification_type_id UUID NOT NULL REFERENCES sec_qualification_type(id),
    certificate_no VARCHAR(100) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    issuing_authority VARCHAR(100),
    status VARCHAR(20) DEFAULT 'VALID' CHECK (status IN ('VALID', 'EXPIRING', 'EXPIRED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 班次模板表
CREATE TABLE IF NOT EXISTS sec_shift_template (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_name VARCHAR(50) NOT NULL,
    shift_type VARCHAR(20) NOT NULL CHECK (shift_type IN ('DAY', 'NIGHT', 'MORNING', 'AFTERNOON')),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    duration_hours NUMERIC(4,1) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 排班表
CREATE TABLE IF NOT EXISTS sec_shift_schedule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    schedule_date DATE NOT NULL,
    customer_point_id UUID NOT NULL REFERENCES sec_customer_point(id),
    shift_template_id UUID NOT NULL REFERENCES sec_shift_template(id),
    personnel_id UUID NOT NULL REFERENCES sec_personnel(id),
    status VARCHAR(20) DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'CHECKED_IN', 'CHECKED_OUT', 'ABSENT', 'EXCHANGED')),
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    is_night_shift BOOLEAN DEFAULT false,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(schedule_date, customer_point_id, shift_template_id, personnel_id)
);

-- 换班申请表
CREATE TABLE IF NOT EXISTS sec_shift_exchange (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_schedule_id UUID NOT NULL REFERENCES sec_shift_schedule(id),
    requester_id UUID NOT NULL REFERENCES sec_personnel(id),
    replacement_id UUID NOT NULL REFERENCES sec_personnel(id),
    exchange_reason TEXT,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    approver_id UUID REFERENCES sec_user(id),
    approval_remarks TEXT,
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 巡更事件表
CREATE TABLE IF NOT EXISTS sec_patrol_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_no VARCHAR(50) UNIQUE NOT NULL,
    customer_point_id UUID NOT NULL REFERENCES sec_customer_point(id),
    schedule_id UUID REFERENCES sec_shift_schedule(id),
    reporter_id UUID NOT NULL REFERENCES sec_personnel(id),
    event_type VARCHAR(50) NOT NULL,
    event_level VARCHAR(20) NOT NULL CHECK (event_level IN ('NORMAL', 'WARNING', 'SEVERE')),
    description TEXT NOT NULL,
    event_time TIMESTAMP NOT NULL,
    customer_confirmed BOOLEAN DEFAULT false,
    customer_confirmer_id UUID REFERENCES sec_user(id),
    customer_confirmed_at TIMESTAMP,
    customer_remarks TEXT,
    status VARCHAR(20) DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'PROCESSING', 'RESOLVED', 'CLOSED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 扣罚类型表
CREATE TABLE IF NOT EXISTS sec_penalty_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type_name VARCHAR(50) NOT NULL UNIQUE,
    default_amount NUMERIC(10,2) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 扣罚记录表
CREATE TABLE IF NOT EXISTS sec_penalty (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    penalty_no VARCHAR(50) UNIQUE NOT NULL,
    personnel_id UUID NOT NULL REFERENCES sec_personnel(id),
    penalty_type_id UUID NOT NULL REFERENCES sec_penalty_type(id),
    patrol_event_id UUID REFERENCES sec_patrol_event(id),
    amount NUMERIC(10,2) NOT NULL,
    penalty_date DATE NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'UNPAID' CHECK (status IN ('UNPAID', 'PAID', 'WAIVED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 结算表
CREATE TABLE IF NOT EXISTS sec_settlement (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    settlement_no VARCHAR(50) UNIQUE NOT NULL,
    customer_id UUID NOT NULL REFERENCES sec_customer(id),
    settlement_month VARCHAR(7) NOT NULL,
    total_shifts INTEGER DEFAULT 0,
    total_amount NUMERIC(12,2) DEFAULT 0,
    penalty_amount NUMERIC(10,2) DEFAULT 0,
    actual_amount NUMERIC(12,2) DEFAULT 0,
    unconfirmed_event_count INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'CONFIRMED', 'BILLED', 'PAID')),
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 结算明细表
CREATE TABLE IF NOT EXISTS sec_settlement_detail (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    settlement_id UUID NOT NULL REFERENCES sec_settlement(id),
    schedule_id UUID NOT NULL REFERENCES sec_shift_schedule(id),
    shift_date DATE NOT NULL,
    shift_amount NUMERIC(10,2) DEFAULT 0,
    included_in_settlement BOOLEAN DEFAULT true,
    exclusion_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 系统配置表
CREATE TABLE IF NOT EXISTS sec_system_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key VARCHAR(50) UNIQUE NOT NULL,
    config_value VARCHAR(255) NOT NULL,
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入初始数据
INSERT INTO sec_system_config (config_key, config_value, description) VALUES
('max_consecutive_night_shifts', '3', '最大连续夜班数'),
('certificate_expiry_warning_days', '30', '证件过期预警天数'),
('shift_unit_price', '200', '班次单价')
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO sec_qualification_type (type_name, description, required_for_key_position) VALUES
('保安证', '保安员资格证书', true),
('消防证', '消防设施操作员证', true),
('上岗证', '岗位培训合格证书', false),
('健康证', '健康体检证明', false)
ON CONFLICT (type_name) DO NOTHING;

INSERT INTO sec_shift_template (template_name, shift_type, start_time, end_time, duration_hours, description) VALUES
('白班', 'DAY', '08:00:00', '20:00:00', 12.0, '标准白班12小时'),
('夜班', 'NIGHT', '20:00:00', '08:00:00', 12.0, '标准夜班12小时'),
('早班', 'MORNING', '07:00:00', '15:00:00', 8.0, '早班8小时'),
('中班', 'AFTERNOON', '15:00:00', '23:00:00', 8.0, '中班8小时')
ON CONFLICT (template_name) DO NOTHING;

INSERT INTO sec_penalty_type (type_name, default_amount, description) VALUES
('迟到', 50.00, '迟到扣罚'),
('早退', 50.00, '早退扣罚'),
('旷工', 200.00, '旷工扣罚'),
('睡岗', 300.00, '在岗睡觉扣罚'),
('脱岗', 200.00, '擅自离岗扣罚'),
('仪容不整', 50.00, '仪容仪表不整扣罚'),
('客户投诉', 500.00, '客户投诉扣罚')
ON CONFLICT (type_name) DO NOTHING;

-- 初始用户（密码都是123456的BCrypt加密值）
INSERT INTO sec_user (username, password, real_name, phone, role, enabled) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6iAt6', '系统管理员', '13800138000', 'PROJECT_MANAGER', true),
('manager1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6iAt6', '项目经理张', '13800138001', 'PROJECT_MANAGER', true),
('leader1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6iAt6', '队长李', '13800138002', 'TEAM_LEADER', true),
('customer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6iAt6', '客户王经理', '13800138003', 'CUSTOMER', true)
ON CONFLICT (username) DO NOTHING;

-- 索引
CREATE INDEX IF NOT EXISTS idx_schedule_date ON sec_shift_schedule(schedule_date);
CREATE INDEX IF NOT EXISTS idx_schedule_personnel ON sec_shift_schedule(personnel_id);
CREATE INDEX IF NOT EXISTS idx_schedule_point ON sec_shift_schedule(customer_point_id);
CREATE INDEX IF NOT EXISTS idx_patrol_event_time ON sec_patrol_event(event_time);
CREATE INDEX IF NOT EXISTS idx_patrol_event_point ON sec_patrol_event(customer_point_id);
CREATE INDEX IF NOT EXISTS idx_qualification_expiry ON sec_qualification(expiry_date);
CREATE INDEX IF NOT EXISTS idx_settlement_customer ON sec_settlement(customer_id);
CREATE INDEX IF NOT EXISTS idx_settlement_month ON sec_settlement(settlement_month);
