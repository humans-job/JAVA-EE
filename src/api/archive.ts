import http, { assertOk, type ApiResp } from './http'

export type MilitiaInfo = {
  id: number
  userId?: number | null
  deptId?: number | null
  name?: string | null
  idCard?: string | null
  phone?: string | null
  address?: string | null
  politicStatus?: string | null
  joinTime?: string | null
  auditStatus?: number | null
  auditFeedback?: string | null
  auditDept?: number | null
  createDept?: number | null
  createTime?: string | null
}

export type MilitiaListResp = {
  list: MilitiaInfo[]
  total: number
}

export type ImportItem = {
  idCard: string
  name?: string
  phone?: string
  address?: string
  politicStatus?: string
  joinTime?: string | null
}

export type ImportFailItem = { idCard: string; reason: string }
export type ImportResp = { total: number; success: number; fail: number; failList: ImportFailItem[] }

export async function apiArchiveList(params: {
  deptId?: number
  auditStatus?: number
  idCard?: string
  pageNum?: number
  pageSize?: number
}) {
  const { data } = await http.get<ApiResp<MilitiaListResp>>('/api/militia/archive/list', { params })
  return assertOk(data)
}

export async function apiArchiveImport(dataRows: ImportItem[]) {
  const { data } = await http.post<ApiResp<ImportResp>>('/api/militia/archive/import', { data: dataRows })
  return assertOk(data)
}

export async function apiArchiveSubmit(ids: number[]) {
  const { data } = await http.post<ApiResp<null>>('/api/militia/archive/submit', { ids })
  return assertOk(data)
}

export async function apiArchiveAudit(payload: { id: number; auditStatus: number; auditFeedback?: string }) {
  const { data } = await http.post<ApiResp<null>>('/api/militia/archive/audit', payload)
  return assertOk(data)
}

export async function apiArchiveUpdate(payload: any) {
  const { data } = await http.put<ApiResp<{ id: number }>>('/api/militia/archive/update', payload)
  return assertOk(data)
}

export async function apiArchiveDelete(id: number) {
  const { data } = await http.delete<ApiResp<null>>(`/api/militia/archive/delete/${id}`)
  return assertOk(data)
}
