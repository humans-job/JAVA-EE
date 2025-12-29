import http, { type ApiResp } from './http'

// ==================== 类型定义 ====================

/**
 * 分页响应结构
 */
export type IPage<T> = {
    records: T[]
    total: number
    size?: number
    current?: number
    pages?: number
}

/**
 * 请假记录实体 (对应后端 leaveReport)
 */
export type LeaveRecord = {
    leaveId: number
    userId: number
    deptId: number
    leaveReason: string
    startTime: string
    endTime: string
    applyTime: string
    status: number // 0-待审批 1-已通过 2-已驳回 3-已销假 4-已确认
    approveDept?: number
    approveOpinion?: string
    reportBackTime?: string
    reportBackLocation?: string
    reportBackConfirmDept?: number
    // 扩展字段 (可能由后端联表查询返回)
    username?: string
    deptName?: string
}

/**
 * 请假状态枚举
 */
export enum LeaveStatus {
    PENDING = 0,      // 待审批
    APPROVED = 1,     // 已通过
    REJECTED = 2,     // 已驳回
    REPORTED = 3,     // 已销假(待确认)
    CONFIRMED = 4     // 已确认(归档)
}

/**
 * 请假状态映射
 */
export const LeaveStatusMap: Record<number, { label: string; color: string }> = {
    [LeaveStatus.PENDING]: { label: '待审批', color: 'warning' },
    [LeaveStatus.APPROVED]: { label: '已通过', color: 'success' },
    [LeaveStatus.REJECTED]: { label: '已驳回', color: 'danger' },
    [LeaveStatus.REPORTED]: { label: '待确认', color: 'info' },
    [LeaveStatus.CONFIRMED]: { label: '已归档', color: 'default' }
}

// ==================== 请求 DTO ====================

/**
 * 提交请假申请
 */
export type LeaveSubmitDTO = {
    reason: string           // 请假理由
    startTime: string        // ISO 格式时间字符串
    endTime: string
}

/**
 * 重新提交请假申请 (驳回后修改)
 */
export type LeaveResubmitDTO = {
    leaveId: number
    reason: string
    startTime: string
    endTime: string
}

/**
 * 审批请假
 */
export type LeaveApproveDTO = {
    leaveId: number
    status: 1 | 2            // 1=通过 2=驳回
    approveOpinion?: string  // 审批意见
}

/**
 * 销假打卡
 */
export type LeaveReportBackDTO = {
    leaveId: number
    reportBackLocation: string  // 销假定位
}

/**
 * 确认销假
 */
export type LeaveConfirmDTO = {
    leaveId: number
}

/**
 * 查询请假列表参数
 */
export type LeaveQueryDTO = {
    month?: string           // 格式 "2025-01"
    status?: number
    pageNum?: number
    pageSize?: number
    queryType?: 1 | 2        // 1=查下属(管理员) 2=查自己(民兵)
    onlyWaitConfirm?: boolean // 只看待销假确认
}

/**
 * 请假统计响应
 */
export type LeaveStatsResponse = {
    records: LeaveRecord[]
    total: number
    // 统计字段 (根据后端实际返回调整)
    pendingCount?: number
    approvedCount?: number
    rejectedCount?: number
    reportedCount?: number
    confirmedCount?: number
}

// ==================== API 函数 ====================

/**
 * 提交请假申请
 * POST /api/work/leave/submit
 */
export function apiLeaveSubmit(payload: LeaveSubmitDTO) {
    return http.post<ApiResp<null>>('/api/work/leave/submit', payload)
}

/**
 * 重新提交请假申请 (驳回后修改)
 * PUT /api/work/leave/resubmit
 * 注意：后端 Controller 中未定义此接口，但 Service 中有，需要后端补充
 */
export function apiLeaveResubmit(payload: LeaveResubmitDTO) {
    return http.put<ApiResp<null>>('/api/work/leave/resubmit', payload)
}

/**
 * 审批请假
 * POST /api/work/leave/approve
 */
export function apiLeaveApprove(payload: LeaveApproveDTO) {
    return http.post<ApiResp<null>>('/api/work/leave/approve', payload)
}

/**
 * 销假打卡 (民兵归队时调用)
 * PUT /api/work/leave/report_back
 */
export function apiLeaveReportBack(payload: LeaveReportBackDTO) {
    return http.put<ApiResp<null>>('/api/work/leave/report_back', payload)
}

/**
 * 确认销假 (管理员归档)
 * POST /api/work/leave/confirm
 */
export function apiLeaveConfirm(payload: LeaveConfirmDTO) {
    return http.post<ApiResp<null>>('/api/work/leave/confirm', payload)
}

/**
 * 查询请假列表 (含统计)
 * GET /api/work/leave/list
 */
export function apiLeaveList(params: LeaveQueryDTO) {
    return http.get<ApiResp<LeaveStatsResponse>>('/api/work/leave/list', { params })
}

// ==================== 便捷方法 ====================

/**
 * 获取我的请假记录 (民兵视角)
 */
export function apiMyLeaveList(params: Omit<LeaveQueryDTO, 'queryType'>) {
    return apiLeaveList({ ...params, queryType: 2 })
}

/**
 * 获取下属请假记录 (管理员视角)
 */
export function apiSubordinateLeaveList(params: Omit<LeaveQueryDTO, 'queryType'>) {
    return apiLeaveList({ ...params, queryType: 1 })
}

/**
 * 获取待确认销假列表 (管理员视角)
 */
export function apiPendingConfirmList(params: Omit<LeaveQueryDTO, 'queryType' | 'onlyWaitConfirm'>) {
    return apiLeaveList({ ...params, queryType: 1, onlyWaitConfirm: true })
}

/**
 * 批准请假
 */
export function apiLeavePass(leaveId: number, opinion?: string) {
    return apiLeaveApprove({ leaveId, status: 1, approveOpinion: opinion })
}

/**
 * 驳回请假
 */
export function apiLeaveReject(leaveId: number, opinion?: string) {
    return apiLeaveApprove({ leaveId, status: 2, approveOpinion: opinion })
}
