import http, { type ApiResp } from './http'

// ==================== 类型定义 ====================

export type IPage<T> = {
    records: T[]
    total: number
    size?: number
    current?: number
    pages?: number
}

export type LeaveRecord = {
    leaveId: number
    userId: number
    deptId: number
    leaveReason: string
    startTime: string
    endTime: string
    applyTime: string
    status: number // 0-待审批 1-待销假 2-已驳回 3-已销假(待确认) 4-已归档
    approveDept?: number
    approveOpinion?: string
    reportBackTime?: string
    reportBackLocation?: string
    reportBackConfirmDept?: number
    username?: string
    deptName?: string
}

/**
 * 请假状态枚举
 * 0 = 待审批
 * 1 = 审批通过(待销假)
 * 2 = 已驳回
 * 3 = 已销假(待确认)
 * 4 = 已归档
 */
export enum LeaveStatus {
    PENDING = 0,      // 待审批
    APPROVED = 1,     // 审批通过(待销假)
    REJECTED = 2,     // 已驳回
    REPORTED = 3,     // 已销假(待确认)
    ARCHIVED = 4      // 已归档
}

/**
 * 请假状态映射 - 更新标签文字
 */
export const LeaveStatusMap: Record<number, { label: string; color: string }> = {
    [LeaveStatus.PENDING]: { label: '待审批', color: 'warning' },
    [LeaveStatus.APPROVED]: { label: '待销假', color: 'success' },      // 修改：已通过 → 待销假
    [LeaveStatus.REJECTED]: { label: '已驳回', color: 'danger' },
    [LeaveStatus.REPORTED]: { label: '待确认', color: 'info' },         // 已销假，等待管理员确认
    [LeaveStatus.ARCHIVED]: { label: '已归档', color: 'default' }       // 修改：已确认 → 已归档
}

// ==================== 请求 DTO ====================

export type LeaveSubmitDTO = {
    reason: string
    startTime: string
    endTime: string
}

export type LeaveResubmitDTO = {
    leaveId: number
    reason: string
    startTime: string
    endTime: string
}

export type LeaveApproveDTO = {
    leaveId: number
    status: 1 | 2            // 1=通过(变为待销假) 2=驳回
    approveOpinion?: string
}

export type LeaveReportBackDTO = {
    leaveId: number
    reportBackLocation: string
}

export type LeaveConfirmDTO = {
    leaveId: number
}

export type LeaveQueryDTO = {
    month?: string
    status?: number
    pageNum?: number
    pageSize?: number
    queryType?: 1 | 2
    onlyWaitConfirm?: boolean
}

export type LeaveStatsResponse = {
    list: LeaveRecord[]
    total: number
    pendingCount?: number    // 待审批数
    approvedCount?: number   // 待销假数
    rejectedCount?: number   // 已驳回数
    reportedCount?: number   // 待确认数（已销假待归档）
    confirmedCount?: number  // 已归档数
}

// ==================== API 函数 ====================

export function apiLeaveSubmit(payload: LeaveSubmitDTO) {
    return http.post<ApiResp<null>>('/api/work/leave/submit', payload)
}

export function apiLeaveResubmit(payload: LeaveResubmitDTO) {
    return http.put<ApiResp<null>>('/api/work/leave/resubmit', payload)
}

export function apiLeaveApprove(payload: LeaveApproveDTO) {
    return http.post<ApiResp<null>>('/api/work/leave/approve', payload)
}

/**
 * 民兵销假打卡
 * 状态: 1(待销假) → 3(已销假待确认)
 */
export function apiLeaveReportBack(payload: LeaveReportBackDTO) {
    return http.put<ApiResp<null>>('/api/work/leave/report_back', payload)
}

/**
 * 管理员确认归档
 * 状态: 3(已销假待确认) → 4(已归档)
 */
export function apiLeaveConfirm(payload: LeaveConfirmDTO) {
    return http.post<ApiResp<null>>('/api/work/leave/confirm', payload)
}

export function apiLeaveList(params: LeaveQueryDTO) {
    return http.get<ApiResp<LeaveStatsResponse>>('/api/work/leave/list', { params })
}

// ==================== 便捷方法 ====================

export function apiMyLeaveList(params: Omit<LeaveQueryDTO, 'queryType'>) {
    return apiLeaveList({ ...params, queryType: 2 })
}

export function apiSubordinateLeaveList(params: Omit<LeaveQueryDTO, 'queryType'>) {
    return apiLeaveList({ ...params, queryType: 1 })
}

export function apiPendingConfirmList(params: Omit<LeaveQueryDTO, 'queryType' | 'onlyWaitConfirm'>) {
    return apiLeaveList({ ...params, queryType: 1, onlyWaitConfirm: true })
}

export function apiLeavePass(leaveId: number, opinion?: string) {
    return apiLeaveApprove({ leaveId, status: 1, approveOpinion: opinion })
}

export function apiLeaveReject(leaveId: number, opinion?: string) {
    return apiLeaveApprove({ leaveId, status: 2, approveOpinion: opinion })
}
