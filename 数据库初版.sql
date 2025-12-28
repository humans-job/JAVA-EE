/*
 Navicat Premium Data Transfer

 Source Server         : 自己
 Source Server Type    : MySQL
 Source Server Version : 80403 (8.4.3)
 Source Host           : localhost:3306
 Source Schema         : xxq

 Target Server Type    : MySQL
 Target Server Version : 80403 (8.4.3)
 File Encoding         : 65001

 Date: 28/12/2025 16:40:27
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for biz_leave
-- ----------------------------
DROP TABLE IF EXISTS `biz_leave`;
CREATE TABLE `biz_leave`  (
  `leave_id` bigint NOT NULL,
  `user_id` bigint NULL DEFAULT NULL COMMENT '申请人(民兵)ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '所属单位ID',
  `leave_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请假事由',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '计划结束时间',
  `apply_time` datetime NULL DEFAULT NULL COMMENT '申请发送时间',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0=待审批, 1=审批通过(待销假), 2=已驳回, 3=已销假(归档)',
  `approve_dept` bigint NULL DEFAULT NULL COMMENT '审批单位ID',
  `approve_opinion` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批意见',
  `report_back_time` datetime NULL DEFAULT NULL COMMENT '销假时间',
  `report_back_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '销假地点',
  `report_back_confirm_dept` bigint NULL DEFAULT NULL COMMENT '销假人ID',
  PRIMARY KEY (`leave_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_militia_info
-- ----------------------------
DROP TABLE IF EXISTS `biz_militia_info`;
CREATE TABLE `biz_militia_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '民兵id',
  `user_id` bigint NULL DEFAULT NULL COMMENT '外键，关联用户表ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '所属单位ID',
  `id_card` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '家庭住址',
  `politic_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '政治面貌',
  `join_time` date NULL DEFAULT NULL COMMENT '入队时间',
  `audit_status` tinyint NULL DEFAULT 0 COMMENT '状态：0=草稿/导入, 1=待师部审核, 2=已归档, 3=驳回',
  `audit_feedback` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核反馈意见',
  `audit_dept` bigint NULL DEFAULT NULL COMMENT '审核的师部id',
  `create_dept` bigint NULL DEFAULT NULL COMMENT '录入(团组织ID)',
  `create_time` datetime NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3022 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_notice
-- ----------------------------
DROP TABLE IF EXISTS `biz_notice`;
CREATE TABLE `biz_notice`  (
  `notice_id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容 (富文本)',
  `type` tinyint NULL DEFAULT NULL COMMENT '类型：1=通知公告, 2=教育学习, 3=团场内部通知',
  `sender_id` bigint NULL DEFAULT NULL COMMENT '发布单位ID',
  `send_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `status` tinyint NULL DEFAULT 0 COMMENT '0=正常, 1=完成',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_notice_record
-- ----------------------------
DROP TABLE IF EXISTS `biz_notice_record`;
CREATE TABLE `biz_notice_record`  (
  `notice_id` bigint NOT NULL COMMENT '档案id',
  `user_id` bigint NOT NULL COMMENT '接收人ID',
  `is_read` tinyint NULL DEFAULT 0 COMMENT '0=未读, 1=已读',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`notice_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_work_report
-- ----------------------------
DROP TABLE IF EXISTS `biz_work_report`;
CREATE TABLE `biz_work_report`  (
  `report_id` bigint NOT NULL,
  `dept_id` bigint NULL DEFAULT NULL COMMENT '提交单位ID (营/连/分队)',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '报表标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文本内容',
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件地址 (Word/PDF)',
  `report_month` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属月份 (如 2023-10)',
  `status` tinyint NULL DEFAULT 0 COMMENT '0=待审批, 1=已审批/归档, 2=驳回',
  `approve_dept_id` bigint NULL DEFAULT NULL COMMENT '审批人ID (团场干部)',
  `approve_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `report_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`report_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint NOT NULL COMMENT '主键',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父级单位ID，顶级同一记为为0',
  `dept_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门名称 ',
  `dept_type` tinyint NULL DEFAULT NULL COMMENT '部门类型：1=兵团机关, 2=师, 3=团, 4=营/连/分队',
  `ancestors` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子级列表 (id查方便查询下级)',
  `sort_order` int NULL DEFAULT NULL COMMENT '显示顺序',
  `region_shape` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '管辖区域形状数据 (我也不知道记什么，方形，圆形，不规则图形都得单算)',
  `region_center` point NULL,
  `region_radius` int NULL DEFAULT NULL,
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dept_belong
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept_belong`;
CREATE TABLE `sys_dept_belong`  (
  `child_id` bigint NOT NULL COMMENT '下级id',
  `parent_id` bigint NOT NULL COMMENT '上级id',
  PRIMARY KEY (`child_id`, `parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '归属部门ID (外键，关联 sys_dept表的id)',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `user_type` tinyint NULL DEFAULT NULL COMMENT '用户类型，1=民兵, 2=营/连/分队, 3=团, 4=师, 5=兵团机关',
  `status` tinyint NULL DEFAULT 1 COMMENT '登录状态',
  `usb_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'USB Key序列号',
  `cert_sn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数字证书序列号',
  `login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2010 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
