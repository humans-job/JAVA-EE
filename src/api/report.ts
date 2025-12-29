import http, { assertOk, type ApiResp } from './http'

// ===================== 枚举定义 =====================

/**
 * 报表类型
 * 1=月计划, 2=月总结, 3=专项
 */
export enum ReportType {
    MONTHLY_PLAN = 1,
    MONTHLY_SUMMARY = 2,
    SPECIAL = 3
}

/**
 * 报表状态
 * 0=待审批, 1=通过, 2=驳回
 */
export enum ReportStatus {
    PENDING = 0,
    APPROVED = 1,
    REJECTED = 2
}

/** 状态文本映射 */
export const ReportStatusMap: Record<number, string> = {
    [ReportStatus.PENDING]: '待审批',
    [ReportStatus.APPROVED]: '已通过',
    [ReportStatus.REJECTED]: '已驳回'
}

/** 类型文本映射 */
export const ReportTypeMap: Record<number, string> = {
    [ReportType.MONTHLY_PLAN]: '月计划',
    [ReportType.MONTHLY_SUMMARY]: '月总结',
    [ReportType.SPECIAL]: '专项报告'
}

// ===================== 请求参数类型 (对应后端 DTO) =====================

/**
 * 提交报表参数 - 对应 ReportSubmitDTO
 */
export interface ReportSubmitParams {
    title: string           // 标题
    content: string         // 内容
    filePath?: string       // 附件路径（可选，先上传文件获取路径）
    reportType: number      // 1=月计划, 2=月总结, 3=专项
    reportMonth: string     // 报表月份，格式如 "2025-01"
}

/**
 * 审批报表参数 - 对应 ReportApproveDTO
 */
export interface ReportApproveParams {
    reportId: number        // 报表ID
    status: number          // 1=通过, 2=驳回
}

/**
 * 查询报表参数 - 对应 ReportQueryDTO
 */
export interface ReportQueryParams {
    deptId?: number         // 部门ID（可选）
    reportType?: number     // 报表类型（可选）
    reportMonth?: string    // 报表月份（可选）
    status?: number         // 状态：0=待审批, 1=通过, 2=驳回（可选）
    pageNum?: number        // 页码，默认1
    pageSize?: number       // 每页条数，默认10
}

// ===================== 响应数据类型 =====================

/**
 * 报表实体 - 对应后端 workReport 实体
 */
export interface WorkReport {
    id: number
    deptId: number          // 提交部门ID
    title: string           // 标题
    content: string         // 内容
    filePath?: string       // 附件路径
    reportType: number      // 报表类型
    reportMonth: string     // 报表月份
    status: number          // 状态
    approveDeptId: number   // 审批部门ID
    approveTime?: string    // 审批时间
    createTime: string      // 创建时间
}

/**
 * 分页列表响应 - 对应后端 list 接口返回的 data 结构
 */
export interface ReportListData {
    list: WorkReport[]
    total: number
}

// ===================== API 方法 =====================

/**
 * 接口1：提交报表
 * POST /api/work/report/submit
 */
export async function apiReportSubmit(data: ReportSubmitParams) {
    const resp = await http.post<ApiResp<null>>('/api/work/report/submit', data)
    return assertOk(resp.data)
}

/**
 * 接口2：审批报表
 * POST /api/work/report/approve
 */
export async function apiReportApprove(data: ReportApproveParams) {
    const resp = await http.post<ApiResp<null>>('/api/work/report/approve', data)
    return assertOk(resp.data)
}

/**
 * 接口3：查询报表列表
 * GET /api/work/report/list
 *
 * 使用说明：
 * - status=0 时查询"待我审批"的报表（当前用户是审批人）
 * - 不传 status 或传其他值时查询"我提交的"报表历史
 */
export async function apiReportList(params: ReportQueryParams) {
    const resp = await http.get<ApiResp<ReportListData>>('/api/work/report/list', { params })
    return assertOk(resp.data)
}

/**
 * 接口4：查看报表详情
 * GET /api/work/report/detail/{id}
 */
export async function apiReportDetail(id: number) {
    const resp = await http.get<ApiResp<WorkReport>>(`/api/work/report/detail/${id}`)
    return assertOk(resp.data)
}

