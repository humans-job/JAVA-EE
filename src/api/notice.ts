import http, { assertOk, type ApiResp } from './http'

export type IPage<T> = {
  records: T[]
  total: number
  size?: number
  current?: number
  pages?: number
}

export type NoticeMyListItem = {
  noticeId: number
  title: string
  noticeType: number
  status: number
  createTime: string
  senderDeptId: number
  isRead: number
  readTime?: string | null
  content?: string
}

export type NoticeFeedback = {
  noticeId: number
  total: number
  readCount: number
  unreadCount: number
  deptList: Array<{ userId: number; username?: string; deptId?: number; isRead: number; readTime?: string | null }>
}

export function apiNoticePublish(payload: any) {
  return http.post<ApiResp<number>>('/api/notice', payload)
}

export function apiNoticeMyList(params: any) {
  // ✅ NoticeMyListReq 是 query 参数绑定，所以用 params
  return http.get<ApiResp<IPage<NoticeMyListItem>>>('/api/notice/my', { params })
}

export function apiNoticeRead(noticeId: number) {
  return http.put<ApiResp<null>>(`/api/notice/${noticeId}/read`)
}

export function apiNoticeFeedback(noticeId: number, params: any) {
  return http.get<ApiResp<any>>(`/api/notice/${noticeId}/records`, { params })
}

export function apiNoticeSentList(params: any) {
  return http.get('/api/notice/sent', { params })
}

