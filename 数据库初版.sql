CREATE TABLE sys_dept (
    id          BIGINT PRIMARY KEY COMMENT '主键',
    parent_id   BIGINT COMMENT '父级单位ID，顶级同一记为为0',
    dept_name   VARCHAR(100) COMMENT '部门名称 ',
    dept_type   TINYINT COMMENT '部门类型：1=兵团机关, 2=师, 3=团, 4=营/连/分队',
    ancestors   VARCHAR(200) COMMENT '子级列表 (id查方便查询下级)',
    sort_order  INT COMMENT '显示顺序',
    shape_type VARCHAR(200) COMMENT '图形种类',
    region_center POINT COMMENT '图形中心点，定位用',
    regin_shape GEOMETRY COMMENT '图形形状，点集首尾点相接'
    );

CREATE TABLE sys_dept_belong (
    id BIGINT COMMENT '下级id',
    parent_id BIGINT COMMENT '上级id',
    PRIMARY KEY (id, parent_id)
);

CREATE TABLE sys_user (
    user_id     BIGINT PRIMARY KEY COMMENT '用户ID',
    dept_id     BIGINT COMMENT '归属部门ID (外键，关联 sys_dept表的id)',
    username    VARCHAR(50) COMMENT '登录账号',
    password    VARCHAR(100) COMMENT '密码',
    user_type   TINYINT COMMENT '用户类型，这个另说，看看需不需要分不同平台',
    status      TINYINT DEFAULT 1 COMMENT '登录状态',
    usb_key     VARCHAR(255) COMMENT 'USB Key序列号',
    cert_sn     VARCHAR(255) COMMENT '数字证书序列号',
    login_ip    VARCHAR(50) COMMENT '最后登录IP',
    login_date  DATETIME COMMENT '最后登录时间'
);

CREATE TABLE biz_militia_info (
    id          BIGINT PRIMARY KEY COMMENT '民兵id',
    user_id     BIGINT COMMENT '外键，关联用户表ID',
    dept_id     BIGINT COMMENT '所属单位ID',
    id_card     VARCHAR(20) COMMENT '身份证号',
    phone       VARCHAR(20) COMMENT '联系电话',
    address     VARCHAR(255) COMMENT '家庭住址',
    politic_status VARCHAR(20) COMMENT '政治面貌',
    join_date   DATE COMMENT '入队时间',
    -- 有什么个人信息再加
    audit_status TINYINT DEFAULT 0 COMMENT '状态：0=草稿/导入, 1=待师部审核, 2=已归档, 3=驳回',
    audit_feedback VARCHAR(255) COMMENT '审核反馈意见',
    audit_dept BIGINT COMMENT '审核的师部id',
    create_by   BIGINT COMMENT '录入(团组织ID)',
    create_time DATETIME
);

CREATE TABLE biz_notice (
    notice_id   BIGINT PRIMARY KEY,
    title       VARCHAR(100) COMMENT '标题',
    content     TEXT COMMENT '内容 (富文本)',
    notice_type TINYINT COMMENT '类型：1=通知公告, 2=教育学习, 3=团场内部通知',
    sender_dept_id BIGINT COMMENT '发布单位ID',
    create_time DATETIME COMMENT '创建时间',
    status      TINYINT DEFAULT 0 COMMENT '0=正常, 1=完成'
);

CREATE TABLE biz_notice_record (
    notice_id   BIGINT  COMMENT '档案id',
    user_id     BIGINT COMMENT '接收人ID',
    is_read     TINYINT DEFAULT 0 COMMENT '0=未读, 1=已读',
    read_time   DATETIME COMMENT '阅读时间',
    PRIMARY KEY (notice_id, user_id)
);

CREATE TABLE biz_work_report (
    report_id   BIGINT PRIMARY KEY,
    dept_id     BIGINT COMMENT '提交单位ID (营/连/分队)',
    title       VARCHAR(100) COMMENT '报表标题',
    content     TEXT COMMENT '文本内容',
    file_path   VARCHAR(255) COMMENT '附件地址 (Word/PDF)',
    report_type TINYINT COMMENT '分类：1=月计划, 2=月总结, 3=专项活动',
    report_month VARCHAR(7) COMMENT '所属月份 (如 2023-10)',
    status      TINYINT DEFAULT 0 COMMENT '0=待审批, 1=已审批/归档, 2=驳回',
    approve_dept_id  BIGINT COMMENT '审批人ID (团场干部)',
    approve_time DATETIME,
    create_time DATETIME
);

CREATE TABLE biz_leave (
    leave_id    BIGINT PRIMARY KEY,
    user_id     BIGINT COMMENT '申请人(民兵)ID',
    dept_id     BIGINT COMMENT '所属单位ID',
    reason      VARCHAR(500) COMMENT '请假事由',
    start_time  DATETIME COMMENT '开始时间',
    end_time    DATETIME COMMENT '计划结束时间',
    
    apply_time  DATETIME COMMENT '申请发送时间',
    status      TINYINT DEFAULT 0 COMMENT '状态：0=待审批, 1=审批通过(待销假), 2=已驳回, 3=已销假(归档)',
    approve_by  BIGINT COMMENT '审批单位ID',
    approve_opinion VARCHAR(200) COMMENT '审批意见',
    
    report_back_time DATETIME COMMENT '销假时间',
    report_back_location VARCHAR(100) COMMENT '销假地点',
    report_back_confirm_by BIGINT COMMENT '销假人ID'
);

