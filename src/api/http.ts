import axios from 'axios'
import router from '@/router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

export type ApiResp<T> = {
  code: number
  msg: string
  data: T
}

// 开发环境建议走 Vite 代理（baseURL 留空），避免 CORS；生产环境用真实后端地址。
const http = axios.create({
  baseURL: import.meta.env.DEV ? '' : (import.meta.env.VITE_API_BASE_URL || ''),
  timeout: 20_000,
})

// 防止重复弹窗
let lastToastAt = 0
function toastOnce(message: string) {
  const now = Date.now()
  if (now - lastToastAt < 1200) return
  lastToastAt = now
  ElMessage.error(message)
}

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  auth.initFromStorage()
  if (auth.token) {
    config.headers = config.headers || {}
    // 后端 SecurityUtil 支持 Authorization: Bearer <token> 或 token 头
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

http.interceptors.response.use(
  (resp) => resp,
  (err) => {
    toastOnce(err?.message || '网络异常')
    return Promise.reject(err)
  }
)

// 业务层统一处理：后端很多 fail 也返回 code=200，但 msg 会提示。
export function assertOk<T>(r: ApiResp<T>): ApiResp<T> {
  if (typeof r?.msg === 'string' && r.msg.includes('登录态已过期')) {
    const auth = useAuthStore()
    auth.logout()
    toastOnce('登录态已过期，请重新登录')
    router.replace('/login')
  }
  return r
}

export default http
